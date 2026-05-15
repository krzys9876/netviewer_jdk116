import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class ImgElemData extends Object { //Lista obrazkow wykorzystywana przy ladowaniu dokumentu
    ImgElem el=null;
    ImgElemData n=null;

    ImgElemData(ImgElem e,ImgElemData nn) {
        el=e;
        n=nn;
    }

    public ImgElemData gn() {
        return n;
    }

    public synchronized void sn(ImgElemData nn) {
        n=nn;
    }

    public synchronized Image getImg() {
        if(el==null) return null;
        Object o=el.ge();
        if(o instanceof Image) return (Image)el.ge();
        return null;
    }

    public void redraw (Graphics g) {
        if(el==null) return;
        el.redraw(g);//!!!
    }
}