#!/usr/bin/env python3
"""Generate HTML 3.2 javadoc-style pages for NetViewer.

Output renders inside NetViewer itself: tables, dl/dt/dd, pre blocks,
classic Sun Java 1.1 javadoc icons (colored balls + section headers).
No CSS, no DIVs unless useful, no JavaScript.
"""

import os, re, subprocess, sys, html, shutil
from pathlib import Path

JAVAP = "/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home/bin/javap"
SRC_DIR = Path(__file__).parent
OUT_DIR = SRC_DIR / "docs"
SRC_IMG_DIR = SRC_DIR.parent / "html" / "images"

# ---------------------------------------------------------------------------
# Hand-written descriptions, grouped by category. (category, description)
# Descriptions are written in English and stick to plain ASCII so they render
# correctly in NetViewer regardless of platform encoding.
# ---------------------------------------------------------------------------
CLASS_INFO = {
    # Application entry point
    "NetViewer": ("Application",
        "Main application window. A two-pane HTML browser built on AWT. "
        "Owns the toolbar, the status bar, and a docPanel containing two "
        "independent document views. Entry point: <tt>main(String[])</tt>."),
    "docPanel": ("Application",
        "Container that hosts the two document views side by side and "
        "dispatches Open / Back / Reload commands from the toolbar to "
        "whichever pane has focus."),

    # Document model
    "HTMLDoc": ("Document model",
        "Parses an HTML source string into a singly-linked list of "
        "<a href=\"Elem.html\">Elem</a> objects. The list is the in-memory "
        "document model that the renderer walks during paint."),
    "Tag": ("Document model",
        "Represents one HTML tag occurrence: name, attribute hashtable, "
        "and a flag for whether it is an opening or closing tag. Built "
        "by tokenising the input between &lt; and &gt;."),
    "Elem": ("Document model",
        "Base class for every document element. A linked list node holding "
        "its element type code, text-style, alignment, link target, and "
        "rendered (x, y). Defines the integer type constants used throughout "
        "the renderer (TEXT, PARAGRAPH, TABLE, ANCHOR, INPUT, ...)."),
    "ElemLine": ("Document model",
        "Helper that groups consecutive Elem nodes into one rendered line, "
        "computing the line width, height, and the position where the next "
        "line should begin. The core of NetViewer's line-breaking layout."),
    "TagElem": ("Document model",
        "Common superclass for every element that is created from a parsed "
        "tag. Stores the originating Tag and exposes its attributes to "
        "subclasses."),
    "CompElem": ("Document model",
        "Composite element: a TagElem that owns a real AWT Component "
        "(button, text field, etc.). Used as a base for the form widgets."),
    "TextElem": ("Document model",
        "Element holding a run of plain text along with its current "
        "font/style attributes."),

    # Element implementations by HTML tag
    "BrElem":        ("HTML element classes", "Implements the <tt>&lt;br&gt;</tt> tag: forces a line break."),
    "HrElem":        ("HTML element classes", "Implements the <tt>&lt;hr&gt;</tt> tag: draws a horizontal rule."),
    "PElem":         ("HTML element classes", "Implements the <tt>&lt;p&gt;</tt> tag: a text paragraph with extra vertical spacing."),
    "DivElem":       ("HTML element classes", "Implements the <tt>&lt;div&gt;</tt> tag: a generic block container. Base for table cells and rows in this renderer."),
    "CenterElem":    ("HTML element classes", "Implements <tt>&lt;center&gt;</tt>: a DivElem that centres its content horizontally."),
    "ParagraphElem": ("HTML element classes", "Container element representing a generic paragraph-level block."),
    "FontModElem":   ("HTML element classes", "Inline font modifier (<tt>&lt;b&gt;</tt>, <tt>&lt;i&gt;</tt>, <tt>&lt;em&gt;</tt>, <tt>&lt;tt&gt;</tt>, <tt>&lt;code&gt;</tt>, ...). Pushes a style change onto subsequent text."),
    "ImgElem":       ("HTML element classes", "Implements the <tt>&lt;img&gt;</tt> tag: loads an image asynchronously and renders it once enough data has arrived."),
    "TextAreaElem":  ("HTML element classes", "Implements <tt>&lt;textarea&gt;</tt>: wraps an AWT TextArea inside the document flow."),

    # Tables
    "TableElem":   ("Tables", "Implements <tt>&lt;table&gt;</tt>. Holds rows, performs cell sizing, and draws borders."),
    "TrElem":      ("Tables", "Implements <tt>&lt;tr&gt;</tt>: one table row, a DivElem subclass."),
    "TabDatElem":  ("Tables", "Implements <tt>&lt;td&gt;</tt>: one ordinary data cell."),
    "TabHeadElem": ("Tables", "Implements <tt>&lt;th&gt;</tt>: a header cell; a TabDatElem that draws its content in bold and centred."),

    # Forms
    "InputElem":  ("Forms", "Implements <tt>&lt;input&gt;</tt>: text, password, checkbox, radio, submit, reset, or hidden, switched on the TYPE attribute."),
    "SelectElem": ("Forms", "Implements <tt>&lt;select&gt;</tt>: a drop-down list whose options are added by child OptionElems."),
    "OptionElem": ("Forms", "Implements <tt>&lt;option&gt;</tt>: one entry inside a SelectElem."),

    # Anchors / links
    "AnchorElem": ("Anchors and links", "Implements <tt>&lt;a&gt;</tt>. Holds HREF (link target) and NAME (anchor) and toggles link styling on the contained text."),
    "AnchorArgs": ("Anchors and links", "Argument bundle passed when registering a new anchor with the document during parsing."),
    "AnchorData": ("Anchors and links", "Stored anchor record: name plus the y coordinate to scroll to when the anchor is targeted."),
    "AnchorList": ("Anchors and links", "Collection of <a href=\"AnchorData.html\">AnchorData</a> entries: the document's index of named anchors."),
    "LinkRect":   ("Anchors and links", "One clickable rectangle within a rendered link, with the URL it points to."),
    "LinkArea":   ("Anchors and links", "Collection of LinkRects: the document's hit-test map. Used by mouse handlers to detect which link was clicked or hovered."),
    "RegLinkArgs":("Anchors and links", "Argument bundle for registering a new clickable region while the renderer walks the document."),

    # Images
    "ImgElemData": ("Image handling", "Metadata for one image: URL, requested width and height, attribute string. Persists even before pixels arrive."),
    "ImgElemList": ("Image handling", "List of <a href=\"ImgElem.html\">ImgElem</a> objects currently loading; lets the renderer ask 'is everything in?' before declaring layout final."),

    # UI components
    "Toolbar":       ("UI components", "Top button bar (Open, Back, Reload, ...). A FlowLayout Panel."),
    "StatusBar":     ("UI components", "Bottom status line: a Canvas that draws a single string with a sunken 3D border."),
    "StatusBarData": ("UI components", "Wrapper carrying the new status message as an AWT Event payload."),
    "OpenDialog":    ("UI components", "Modal 'Open Location' dialog with a URL text field plus a Browse button that pops a native FileDialog."),
    "OpenDialogBuf": ("UI components", "Result buffer for OpenDialog: which button was pressed, the chosen location, and the last-used directory."),
    "TitleData":     ("UI components", "Event payload used to tell the frame to update its window title from a parsed &lt;title&gt; tag."),
    "LoadLocData":   ("UI components", "Event payload requesting that the canvas load a new URL."),
    "RegLocData":    ("UI components", "Event payload registering the URL of the document currently being loaded."),

    # Threads
    "LoaderThread":  ("Background threads", "Reads bytes from an InputStream in the background, appending them to a growing buffer. Cooperatively cancellable via interrupt()."),
    "RepaintThread": ("Background threads", "Walks the element list and paints each visible Elem. Cooperatively cancellable so a fresh paint can pre-empt a stale one."),

    # Utility
    "Props":   ("Utilities", "Application-wide properties: font table per text style, special-character glyph table, on-disk page cache. Owned by an mCanvas."),
    "Graph":   ("Utilities", "Static helpers for drawing 3D-styled rectangles (raised/sunken) of arbitrary border thickness."),
    "Array":   ("Utilities", "Plain 2-D array of Objects with bounds-checked accessors. Used to back table cell grids."),
    "mCanvas": ("Application", "Custom Canvas that displays one parsed document. Fetches the source URL, calls HTMLDoc to parse it, owns the link hit-test map, and starts a RepaintThread for incremental paint."),
}

# ---------------------------------------------------------------------------
# javap-driven parsing
# ---------------------------------------------------------------------------
HEADER_RE = re.compile(
    r"^(?P<mods>(?:public |protected |private |abstract |final |static |synchronized )*)"
    r"(?P<kind>class|interface)\s+(?P<name>\S+)"
    r"(?:\s+extends\s+(?P<ext>\S+))?"
    r"(?:\s+implements\s+(?P<impl>[^{]+?))?\s*\{?\s*$"
)

def list_classes():
    return sorted(p.stem for p in SRC_DIR.glob("*.class") if "$" not in p.stem)

def run_javap(cls):
    return subprocess.run([JAVAP, "-p", cls], cwd=SRC_DIR,
                          capture_output=True, text=True).stdout

def parse_javap(text, cls):
    lines = [ln for ln in text.splitlines() if ln.strip()]
    if not lines: return None
    idx = 1 if lines[0].startswith("Compiled from") else 0
    m = HEADER_RE.match(lines[idx])
    header = {"mods": "", "kind": "class", "name": cls, "extends": "", "implements": ""}
    if m:
        header.update({
            "mods": m.group("mods").strip(),
            "kind": m.group("kind"),
            "name": m.group("name"),
            "extends": m.group("ext") or "",
            "implements": (m.group("impl") or "").strip(),
        })
    fields, ctors, methods = [], [], []
    for ln in lines[idx+1:]:
        s = ln.strip().rstrip(";")
        if not s or s in ("{", "}"): continue
        if "(" not in s:
            fields.append(s); continue
        # Distinguish constructor: name starts with class name followed by '('
        head = s.split("(",1)[0]
        last_word = re.split(r"\s+", head.strip())[-1]
        if last_word == cls:
            ctors.append(s)
        else:
            methods.append(s)
    return {"name": cls, "header": header, "fields": fields, "ctors": ctors, "methods": methods}

# ---------------------------------------------------------------------------
# HTML helpers
# ---------------------------------------------------------------------------
def linkify(text, local_classes):
    """Wrap any bare identifier that names a local class in an <a href>."""
    def repl(m):
        word = m.group(0)
        if word in local_classes:
            return f'<a href="{word}.html">{word}</a>'
        return word
    return re.sub(r"\b[A-Za-z_][A-Za-z0-9_]*\b", repl, html.escape(text))

def inheritance_chain(name, parsed_index):
    """Walk extends links upward, returning a list from root-most to `name`."""
    chain = [name]
    seen = {name}
    while True:
        info = parsed_index.get(chain[-1])
        if not info: break
        ext = info["header"]["extends"]
        if not ext: break
        if ext in seen: break
        chain.append(ext)
        seen.add(ext)
        if ext not in parsed_index:
            # non-local ancestor; show java.lang.Object as the universal root
            if ext != "java.lang.Object" and "Object" not in ext.split("."):
                chain.append("java.lang.Object")
            break
    return list(reversed(chain))

def hierarchy_pre(name, parsed_index, local_classes):
    chain = inheritance_chain(name, parsed_index)
    out = []
    indent = ""
    for i, cls in enumerate(chain):
        if i == 0:
            out.append(html.escape(cls))
        else:
            indent += "   "
            out.append(indent + "|")
            out.append(indent + "+----" + linkify(cls, local_classes))
            indent += "    "
    return "<pre>\n" + "\n".join(out) + "\n</pre>"

def member_dl(items, ball_img, local_classes, anchor_prefix=""):
    rows = []
    for s in items:
        rows.append(f'  <dt> <img src="images/{ball_img}" width=6 height=6 alt=" o ">')
        rows.append(f'       <tt>{linkify(s, local_classes)}</tt>')
    return "<dl>\n" + "\n".join(rows) + "\n</dl>"

NAV = ('<pre><a href="index.html">Index</a>   '
       '<a href="hierarchy.html">Class Hierarchy</a></pre>')

def class_page(info, parsed_index, local_classes):
    name = info["name"]
    h = info["header"]
    cat, desc = CLASS_INFO.get(name, ("", ""))

    parts = [NAV]
    parts.append(f'<h1>{html.escape("Class " + name)}</h1>')
    parts.append(hierarchy_pre(name, parsed_index, local_classes))
    decl = []
    decl.append(f"  <dt> {h['mods']+' ' if h['mods'] else ''}{h['kind']} <b>{name}</b>")
    if h["extends"]:
        decl.append(f"  <dt> extends {linkify(h['extends'], local_classes)}")
    if h["implements"]:
        decl.append(f"  <dt> implements {linkify(h['implements'], local_classes)}")
    parts.append("<dl>\n" + "\n".join(decl) + "\n</dl>")

    if desc:
        parts.append(f"<p>{desc}</p>")
    if cat:
        parts.append(f"<dl><dt><b>Category:</b><dd>{html.escape(cat)}</dl>")

    if info["ctors"]:
        parts.append('<h2><img src="images/constructor-index.gif" '
                     'width=275 height=38 alt="Constructor Index"></h2>')
        parts.append(member_dl(info["ctors"], "yellow-ball-small.gif", local_classes))
    if info["methods"]:
        parts.append('<h2><img src="images/method-index.gif" '
                     'width=207 height=38 alt="Method Index"></h2>')
        parts.append(member_dl(info["methods"], "red-ball-small.gif", local_classes))
    if info["fields"]:
        parts.append('<h2><img src="images/variable-index.gif" '
                     'width=265 height=38 alt="Variable Index"></h2>')
        parts.append(member_dl(info["fields"], "blue-ball-small.gif", local_classes))

    parts.append("<hr>")
    parts.append(NAV)

    return (f"<html><head><title>Class {html.escape(name)}</title></head>\n"
            f"<body>\n" + "\n".join(parts) + "\n</body></html>\n")

# ---------------------------------------------------------------------------
# Index and hierarchy pages
# ---------------------------------------------------------------------------
PROJECT_OVERVIEW = """
<p>
<b>NetViewer</b> is an experimental HTML 3.2 web browser written in pure
Java 1.1 AWT, dating from 1999. It parses HTML into a linked list of
element objects, computes a flow layout, renders incrementally on a
background thread, and supports clickable anchors, images, tables, and
form widgets.
</p>
<p>
This documentation is itself rendered <i>inside</i> NetViewer, using
only the tag subset the parser recognises:
<tt>&lt;p&gt;</tt>, <tt>&lt;h1..h6&gt;</tt>, <tt>&lt;pre&gt;</tt>,
<tt>&lt;dl&gt; / &lt;dt&gt; / &lt;dd&gt;</tt>, <tt>&lt;table&gt;</tt>,
<tt>&lt;a&gt;</tt>, <tt>&lt;img&gt;</tt>, <tt>&lt;b&gt;</tt>, <tt>&lt;i&gt;</tt>,
<tt>&lt;tt&gt;</tt>, <tt>&lt;hr&gt;</tt>.
</p>
<p>
The codebase is organised around a small number of recurring patterns:
a linked-list document model, an <a href="Elem.html">Elem</a> base class
with one subclass per supported HTML element, two background threads
(loading and painting), and a couple of small data-carrier classes that
travel inside AWT events.
</p>
"""

def index_page(parsed_index, local_classes):
    # Group by category
    by_cat = {}
    for name, info in parsed_index.items():
        cat = CLASS_INFO.get(name, ("Uncategorised", ""))[0]
        by_cat.setdefault(cat, []).append(name)

    order = ["Application", "Document model", "HTML element classes",
             "Tables", "Forms", "Anchors and links", "Image handling",
             "UI components", "Background threads", "Utilities",
             "Uncategorised"]

    parts = []
    parts.append('<h1>NetViewer source documentation</h1>')
    parts.append('<p><img src="images/class-index.gif" width=210 height=38 '
                 'alt="Class Index"></p>')
    parts.append(PROJECT_OVERVIEW)

    for cat in order:
        names = sorted(by_cat.get(cat, []))
        if not names: continue
        parts.append(f"<hr><h2>{html.escape(cat)}</h2>")
        parts.append("<dl>")
        for n in names:
            _, desc = CLASS_INFO.get(n, ("", ""))
            parts.append(f'  <dt><img src="images/green-ball-small.gif" '
                         f'width=6 height=6 alt=" o "> '
                         f'<a href="{n}.html"><b>{n}</b></a>')
            if desc:
                parts.append(f"  <dd>{desc}")
        parts.append("</dl>")

    parts.append('<hr><p><a href="hierarchy.html">Class hierarchy</a></p>')
    return ("<html><head><title>NetViewer documentation</title></head>\n"
            "<body>\n" + "\n".join(parts) + "\n</body></html>\n")

def hierarchy_page(parsed_index, local_classes):
    """Render a simple class tree: java.* roots, then local subclasses indented."""
    children = {}
    for n, info in parsed_index.items():
        parent = info["header"]["extends"] or "java.lang.Object"
        children.setdefault(parent, []).append(n)

    visited = set()
    def render(node, depth):
        out = []
        indent = "  " * depth
        link = linkify(node, local_classes) if node in local_classes else html.escape(node)
        out.append(f"{indent}<dt><img src=\"images/cyan-ball-small.gif\" width=6 height=6 alt=\" o \"> {link}")
        kids = sorted(children.get(node, []))
        if kids:
            out.append(f"{indent}<dd><dl>")
            for k in kids:
                if k in visited: continue
                visited.add(k)
                out.extend(render(k, depth+1))
            out.append(f"{indent}</dl>")
        return out

    # Roots: any parent that is not itself in parsed_index
    roots = sorted({p for p in children if p not in parsed_index})
    body = ["<h1>NetViewer class hierarchy</h1>", NAV, "<dl>"]
    for r in roots:
        body.extend(render(r, 0))
    body.append("</dl>")
    body.append("<hr>"); body.append(NAV)
    return ("<html><head><title>Class hierarchy</title></head>\n"
            "<body>\n" + "\n".join(body) + "\n</body></html>\n")

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
def main():
    if OUT_DIR.exists(): shutil.rmtree(OUT_DIR)
    OUT_DIR.mkdir()
    # Copy the classic Sun javadoc icons
    dest_img = OUT_DIR / "images"
    shutil.copytree(SRC_IMG_DIR, dest_img)

    names = list_classes()
    parsed_index = {}
    for n in names:
        info = parse_javap(run_javap(n), n)
        if info: parsed_index[n] = info
    local = set(parsed_index.keys())

    for n, info in parsed_index.items():
        (OUT_DIR / f"{n}.html").write_text(
            class_page(info, parsed_index, local), encoding="utf-8")
    (OUT_DIR / "index.html").write_text(
        index_page(parsed_index, local), encoding="utf-8")
    (OUT_DIR / "hierarchy.html").write_text(
        hierarchy_page(parsed_index, local), encoding="utf-8")

    print(f"Wrote {len(parsed_index)} class pages, index.html, hierarchy.html")
    print(f"Images copied to {dest_img}")

if __name__ == "__main__":
    main()
