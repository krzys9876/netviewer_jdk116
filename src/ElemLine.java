import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;


class ElemLine extends Object {
    Elem begin=null,end=null;
    int minwidth=0,maxwidth=0,height=0,width=0,lineAsc=0,lineDesc=0,align=Elem.LEFT,x0;

    ElemLine(Elem beg,int w,int nx0,int ny0) {
        this(beg,w,nx0,ny0,beg.align);
    }

    ElemLine(Elem beg,int w,int nx0,int ny0,int algn) {
        align= algn==Elem.DEFAULT ? beg.align : algn; //Domyslnie linia wyrownana do lewej
        if(align==Elem.DEFAULT) align=Elem.LEFT;
        begin=beg;
        int maxw=0;
        //align=LEFT;//!!!!(parametr align ma byc w pierwszym elemencie linii
        int x=0,y=ny0,xtmp=0,dl,dyAsc,dyDesc;
        Elem etmp,e0;
        etmp=e0=begin;
        boolean endl=false;
        endl=false;
        x=0;
        dyAsc=0;
        while(etmp!=null && !endl) { //Ustalenie konca i _ascent_
            xtmp=x;
            dl=etmp.getWidth();
            x+=dl;
            if (dl>maxw) { //maxw - dlugosc najszerszego elementu lub szerokosc okna
                maxw=dl;
            }

            if((x>w && w!=-1) || ((e0!=etmp) && etmp.newLine())) { //Przekroczenie szerokosci okna
                endl=true;                              //  lub element rozpoczynajacy nastepna linie
                if (e0==etmp || (xtmp==0 && !etmp.newLine())) {//Jezeli jest to tylko jeden element lub wszystkie wczesniejsze w linii mialy szerokosc 0, a element nie jest od nowej linii
                    dyAsc=etmp.getAsc(); //Wysokosc jedynego elementu w linii
                    etmp=etmp.gn();
                }
            } else {
                if(etmp.getAsc()>dyAsc)dyAsc=etmp.getAsc();
                etmp=etmp.gn();
            }
        }

        if(align==Elem.RIGHT) {
            if(etmp==null) x0=w-x; //RIGHT //xtmp==0 gdy element jest szerszy od okna
            else x0= xtmp==0 ? 0 : w-xtmp;
        } else
        if(align==Elem.CENTER) {
            if(etmp==null) x0=(w-x)/2; //CENTER
            else x0= xtmp==0 ? 0 : (w-xtmp)/2;
        } else {
            x0=0;
        }

        y+=dyAsc;
        lineAsc=dyAsc;
        dyDesc=0;
        x=x0;

        while(e0!=etmp) {  //Ustalenie _descent_
            dl=e0.getWidth();
            if(e0.getDesc(dyAsc)>dyDesc)dyDesc=e0.getDesc(dyAsc);
            e0.setXY(x+nx0,y);
            e0=e0.gn();
            x+=dl;
        }

        lineDesc=dyDesc;
        height=dyAsc+dyDesc;
        minwidth=maxw;
        maxwidth= maxw>w ? maxw : w;
        width= xtmp==0 ? maxw : x;
       // if(align!=LEFT && width<w) {
       //     width=w;
       //     if(maxwidth<w) maxwidth=w;
       // }

        end=etmp;
    }

    public void paintLine(Graphics g) {
        g.setColor(Color.black);
        int dl,x=x0,y=lineAsc;
        Elem etmp,e0;
        etmp=e0=begin;

        while(e0!=end) {
            dl=e0.getWidth();

            e0.redraw(g);
            e0=e0.gn();
            x+=dl;
        }
    }


    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getMinWidth() {
        return minwidth;
    }

    public int getMaxWidth() {
        return maxwidth;
    }

    public Elem getEnd() {
        return end;
    }
}
