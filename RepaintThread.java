import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import java.util.*;

class RepaintThread extends Thread {
    HTMLDoc doc=null;
    Graphics g=null;
    Rectangle bounds=null;

    RepaintThread(HTMLDoc ndoc, Graphics ng, Rectangle nbounds) {
        doc=ndoc;
        g=ng;
        bounds=nbounds;
    }

    public void run() {
        if(doc==null || g==null || bounds==null) return;

//        long t0=System.currentTimeMillis();
//        for(int a=0;a<1;a++) { Petla do pomiaru czasu
        Elem ep=doc.getElem();
        while(ep!=null && !isInterrupted()) {
            ep.checkEnable(bounds); //Sprawdzenie, czy element miesci sie na ekranie
            ep.redraw(g);
            ep=ep.gn();
        }

//        }   Koniec petli do pomiaru czasu

//        System.out.println("Time="+(System.currentTimeMillis()-t0));
    }
}
