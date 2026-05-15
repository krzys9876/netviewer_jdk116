# Too cool to be true

This is just crazy! While preparing to a lecture about Java and programming in general
I came across my old Java code from 1997 or 1998 (yes, don't bother file timestamps) that I abandoned
just before I graduated from my university.

Back then I've had an idea to write my own simplistic web browser, which at that time meant
a visualization layer for HTML files. I tried to run it but it was constantly failing. 
Fortunatelly with a little help of AI and just a few amendments that were related to window
repainting, I was able to actually run it and travel in time almost 30 years back!

## Net Viewer

The NetViewer browser was supposed to be an educational exercise. First and foremost I wanted
just to write a decent browser that would be able to display an average page from that time. 
I remember that it was a bit of a race against progress in Web tech stack. Secondly I wanted 
to describe what I had learned in a form of a book (I wrote one before so I knew how to tackle
it). Unfortunatelly t didn't work out and I basically forgot about it until now.

## How to run

This is simple. Using Java 25 (how surrealistic this is) just go to `src` folder and type

```
   java NetViewer
```

When main window pops up, press **Open** and choose a file. I generated (again, with use of AI) 
a documentation that resembles JavaDoc from JDK 1.1.6 era. Then again **Open 1** (the other button
does not work for some reason) and you should see the page.

The look and feel of the page remainds me of Netscape Navigator on Sun workstations that we were 
using in Computer Centre in Silesian Technical University. Feels like a visit to a virtual museum.

<img width="1190" height="993" alt="netviewer_screen" src="https://github.com/user-attachments/assets/a95758c3-012d-4866-aa2d-7d3f99d9e369" />

### Issues

There are probably numerous issues in the code, which is quite unterstandable. I spotted improper scrolling,
but hey, it is not about correctness now, rather about Java being (almost) immortal. 


