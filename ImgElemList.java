import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class ImgElemList extends Object {
    ImgElemData list=null;
    ImgElemData current=null;


    public synchronized void add(ImgElem el) {
        if(list==null) {
            list=new ImgElemData(el, null);
            current=list;
        } else {
            ImgElemData tmp=new ImgElemData(el, null);
            current.sn(tmp);
            current=tmp;
        }

    }

    public void redraw(Graphics g, Image img) {
        if(img==null || g==null) return;

        ImgElemData el=list;
        while(el!=null) {
            //!!! Tymczasowo sprawdzanie wszystkich elementow, bo obrazki moga sie powtarzac (dla tego samego pliku obrazka)
            if(img.equals(el.getImg())) el.redraw(g);
            el=el.gn();
        }
    }

    public void startLoading() {
     //   System.out.println("startloading");

        ImgElemData tmp=list;
        while(tmp!=null) {
            tmp.el.startLoading();
            tmp=tmp.gn();
        }
    }

    public void clear() {
        list=null;
    }
}
