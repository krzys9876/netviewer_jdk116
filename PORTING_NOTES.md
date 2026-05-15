# NetViewer — JDK 1.1.6 → JDK 25 porting notes

Original code: written ~1999 against JDK 1.1.6.
Target: macOS 26.4 on Apple Silicon, Eclipse Temurin JDK 25.

The bytecode itself still loads on a modern JVM — class file v45 is accepted
by every JVM up to 25. What broke was a mix of (a) removed/now-throwing
APIs, (b) JIT/JMM rules that are stricter than they were in 1.1, and
(c) AWT lifecycle and modality details that changed.

Run with:

```sh
/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home/bin/java NetViewer
```

JDK 17 must **not** be used on macOS — its AWT OpenGL pipeline (`libawt_lwawt`)
crashes in `OGLTR_DrawGlyphList` when rendering the fonts this program asks
for. JDK 21 and later default to the Metal pipeline, which is unaffected.


## Changes, by file

### 1. `NetViewer.java` — null-check in overridden `layout()`

```diff
   public void layout() {
       super.layout();
-      status.repaint();
+      if (status != null) status.repaint();
   }
```

**Why.** `layout()` is overridden on a `Frame` and dereferences the field
`status` (a `StatusBar` assigned in the constructor). In JDK 1.1 the AWT
peer was created lazily inside `show()`, so `layout()` was only ever
called after the constructor had finished. In modern AWT the
`validate()` → `doLayout()` → `layout()` path can be triggered during
`add()` or during the call chain inside `show()` itself, before the
subclass has finished initialising its fields. Result: `NullPointerException`
at startup. The null check is the minimal safe fix; AWT will paint
the status bar again on the next valid pass.


### 2. `mCanvas.java:443` — replace `Thread.stop()` on the paint thread

```diff
-  if(repthread!=null) repthread.stop();
+  if(repthread!=null) repthread.interrupt();
```

**Why.** `Thread.stop()` was deprecated in JDK 1.2 (it can leave shared
state in inconsistent states because it raises a `ThreadDeath` from any
bytecode boundary). In JDK 20 it was finally hard-removed — calling it
now throws `UnsupportedOperationException` unconditionally. The
JDK 1.1 idiom *"kill the previous paint thread before starting a fresh
one"* must be reworked as cooperative cancellation via `interrupt()`.


### 3. `RepaintThread.java:24` — honour `isInterrupted()`

```diff
   Elem ep=doc.getElem();
-  while(ep!=null) {
+  while(ep!=null && !isInterrupted()) {
       ep.checkEnable(bounds);
       ep.redraw(g);
       ep=ep.gn();
   }
```

**Why.** `interrupt()` only sets a flag; the thread must actually check
that flag for cancellation to take effect. Without this check, the
old paint thread keeps walking the element list even after a fresh
paint has started, leading to overdraw and (occasionally) graphics
context corruption when both threads draw simultaneously.


### 4. `mCanvas.java:278` — replace `Thread.stop()` on the loader thread

```diff
-  loader.stop();
+  loader.interrupt();
```

**Why.** Same removed-API issue as #2. Here the caller has already
spun until `loader.loadedAll()` is true, so the thread is effectively
finished. `interrupt()` on a terminated thread is a harmless no-op; on
a still-running thread the read loop now respects it (see #6).


### 5. `LoaderThread.java:10` — make `loaded` `volatile`

```diff
-  boolean loaded=false;
+  volatile boolean loaded=false;
```

**Why.** The single biggest source of *random freezes* on modern Java
in this codebase. The caller in `mCanvas.getDoc()` does

```java
while(!loader.loadedAll()) { ... }
```

on the EDT. The reader thread (EDT) and the writer thread (loader)
have no synchronisation between them. Under the modern Java Memory
Model the JIT is fully entitled to hoist the read of `loaded` out of
the loop, producing the equivalent of

```java
if(!loader.loadedAll()) while(true) { ... }
```

— and on JDK 25's C2 compiler that *does* happen. The original code
worked by accident on JDK 1.1 because the 1.x JIT was much less
aggressive. Marking the field `volatile` forces a fresh load on every
iteration and is the canonical fix for this idiom.


### 6. `LoaderThread.java:31` — honour `isInterrupted()` in the read loop

```diff
-  while(res!=-1) {
+  while(res!=-1 && !isInterrupted()) {
       res=in.read(buf,0,buf.length);
       if(res>0) {
           addBytes(buf,res);
       }
   }
```

**Why.** Mirrors #3: `interrupt()` does nothing unless the loop looks
at the flag. Without this, a slow or hung URL fetch could not be
abandoned even when the caller asks it to stop.


### 7. `mCanvas.java:271-277` — yield inside the load busy-wait

```diff
   loader.start();
   while(!loader.loadedAll()) {
       count=loader.getLength();
       if(count>lastcount) {
           System.out.println("loaded so far: "+count);
           lastcount=count;
       }
+      try { Thread.sleep(10); }
+      catch (InterruptedException ie) {
+          Thread.currentThread().interrupt(); break;
+      }
   }
```

**Why.** Even with #5 in place, the busy-wait pins an entire CPU core
at 100% on the EDT while a page is loading. In practice this starves
the loader thread (especially on battery, with OS thermal throttling),
which made click-on-link freezes much more frequent. A 10 ms sleep
yields the scheduler, drops CPU use to near zero, and is invisible
to the user (page load is I/O-bound anyway). The `catch` block
preserves interrupt semantics in case the EDT itself is being shut
down.

> Strictly speaking the right fix is to remove the busy-wait entirely
> and drive the load from an AWT event posted by the loader on
> completion. That would be a structural change, however; this is
> the minimum diff that makes it behave.


### 8. `OpenDialog.java:36-41` — show *after* reshape, not before

```diff
-  show();
-
-  reshape(getParent().bounds().x+getParent().insets().right,
-          getParent().bounds().y+getParent().insets().top,
-          insets().left-insets().right+300,
-          insets().top-insets().bottom+120);
+  reshape(getParent().bounds().x+getParent().insets().right,
+          getParent().bounds().y+getParent().insets().top,
+          insets().left-insets().right+300,
+          insets().top-insets().bottom+120);
+
+  show();
```

**Why.** In JDK 1.1, `Dialog.show()` on a modal dialog returned
immediately after pumping the dialog onto the screen; the EDT-blocking
modal semantics happened deeper inside. The lines following `show()`
therefore ran straight away. In modern AWT, `show()` on a modal
`Dialog` **blocks** the calling thread until the dialog is dismissed,
so the `reshape()` call only ran *after* the user closed the dialog —
by which time the size no longer matters. The dialog appeared at
its preferred-zero default size. Swapping the order makes the reshape
take effect before the dialog is visible.


### 9. `OpenDialog.java:95-104` — read FileDialog result before re-showing

```diff
   ((Frame)getParent()).disable();
   hide();
   fd.show();
-  show();
-  ((Frame)getParent()).enable();
   System.out.println("po fd.show()");
   if(fd.getFile()!=null && !fd.getFile().equals("binnull")) {
       buf.dir=fd.getDirectory();
       tloc.setText(new String("file:///"+buf.dir+fd.getFile()));
   }
+  show();
+  ((Frame)getParent()).enable();
   return true;
```

**Why.** Same root cause as #8. Inside the Browse handler, the
re-`show()` of the OpenDialog blocks the EDT, so everything after
it — including `tloc.setText(...)` — only runs once the OpenDialog
is dismissed for good. The user perceived this as "the Browse
button does nothing": the text field never reflected the chosen
file. Reading the result and updating the text field *before*
re-showing the dialog fixes it.


## Recompile

After any of the source edits:

```sh
/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home/bin/javac \
    NetViewer.java mCanvas.java RepaintThread.java \
    LoaderThread.java OpenDialog.java
```

Deprecation warnings (`size()`, `bounds()`, `reshape()`, `show()`,
`disable()`, `enable()`, `insets()`, `Frame.layout()`, `Event` 1.0 model)
are expected — these are 1.1 APIs that still work but have modern
replacements (`getSize`, `getBounds`, `setBounds`, `setVisible`,
`setEnabled`, `getInsets`, `doLayout`, the 1.1 event listener model).
None of them are scheduled for removal.


## Themes & generalisable lessons

1. **Removed APIs are surgical fixes.** `Thread.stop()` is the only
   member of this codebase that the JDK has actually removed. Two
   call sites; two-line fix each.

2. **The Java Memory Model is the silent killer.** Modern JITs make
   stronger optimisations than 1.x ones did. Any field shared between
   threads without `synchronized` or `volatile` is a latent bug. The
   busy-wait in `mCanvas.getDoc()` is the textbook case.

3. **AWT modality changed.** In JDK 1.1, `Dialog.show()` returned
   quickly; in modern AWT it blocks the EDT until dismissal. Code
   written assuming the old behaviour reorders incorrectly.

4. **AWT validation ordering changed.** Overridden lifecycle
   methods (`layout()`, `paint()`, `addNotify()`, ...) can fire
   earlier than they did in 1.1. Defensive null-checks on subclass
   fields are cheap insurance.

5. **macOS rendering pipeline matters.** JDK 17 on Apple Silicon
   uses the OpenGL pipeline by default and crashes in native code
   on the fonts this app picks. JDK 21+ default to Metal and work.
   `-Dsun.java2d.opengl=false` is *not* respected on macOS in JDK
   17 (there is no working software pipeline on macOS in that
   release) — the upgrade is the actual fix.

6. **No JDK 1.x binary exists for macOS.** JDK 1.1 was 32-bit and
   shipped for Solaris, Windows, and Linux x86 only. "Just install
   the old JDK" is not a path on this platform; modernising the
   code (as above) is the only realistic route.
