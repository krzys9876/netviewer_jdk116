import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class TabDatElem extends DivElem {
    Elem eldat=null;
    int height=0,width=0,heighti=0;//heighti - wysokosc wnetrza - wykorzystywana do wyrownania pionowego
    int abswidth=-1,relwidth=-1;
    int colsp=1,rowsp=1;
    int ascentlin=0; //Parametr lineAsc z pierwszej linii (wykorzystywany do wyrownania pionowego)
    int defvalign=MIDDLE;
    int padding=4; //wartosc domyslna
    int border=0; //Wartosc domyslna - brak ramki

    TabDatElem(Tag tag) {
        super(tag);

        Hashtable par=tag.getParams();
        Object cob=par.get("COLSPAN");
        Object rob=par.get("ROWSPAN");
        Object wid=par.get("WIDTH");

        if(cob instanceof Integer) colsp=((Integer)cob).intValue();
        if(rob instanceof Integer) rowsp=((Integer)rob).intValue();

        if(colsp<1)colsp=1;
        if(rowsp<1)rowsp=1;

        if(wid!=null) {
            if(wid instanceof String) {
                String widstr=(String)wid;
                if(widstr.endsWith("%")) {
                    widstr=widstr.substring(0,widstr.lastIndexOf("%"));
                    relwidth=Integer.valueOf(widstr).intValue();
                }
            } else
            if(wid instanceof Integer) {
                abswidth=((Integer)wid).intValue();
               // System.out.println("abswidth="+abswidth);
            }
        }
    }

    public void registerLink() {
        if(ends) return;

        Elem ep=eldat;
        while(ep!=null) {
            ep.registerLink();
            ep=ep.gn();
        }
    }

    public void initComp(Rectangle rect) {//Dla elementow kontrolnych
        Elem ep=eldat;
        while(ep!=null) {
            ep.initComp(rect);
            ep=ep.gn();
        }
    }

    public void checkEnable(Rectangle rect) {
        if(ends) {
            enabled=false;
            return;
        }

        super.checkEnable(rect);
        Elem ep=eldat;
        while(ep!=null) {
            ep.checkEnable(rect);
            ep=ep.gn();
        }
    }


    public void init0() {
        if(ends) return;

        if(divalign!=DEFAULT) setAlign(divalign);


        Elem el1=null,el=gn();
        if(el!=null && el.getType()!=TABDAT && el.getType()!=TABDAT+ENDS && el.getType()!=TABROW && el.getType()!=TABROW+ENDS && el.getType()!=TABLE+ENDS) {
            eldat=el;
            el1=el;
            while(el!=null && el.getType()!=TABDAT && el.getType()!=TABDAT+ENDS && el.getType()!=TABROW && el.getType()!=TABROW+ENDS && el.getType()!=TABLE+ENDS) {

              //  if(el.getAlign()==DEFAULT ) { //Ustalenie wyrownania
                //    if(getAlign()==DEFAULT) el.setAlign(defalign);
                //    else el.setAlign(getAlign());
               // }
                el.init0();
                if(getAlign()!=DEFAULT)el.setAlign(getAlign());
                el1=el;
                el=el.gn();
            }
            el1.sn(null);
            sn(el);
        }

    }

    public synchronized void init(int pw) {
        if(ends) return;

        if(pw!=-1 && abswidth>0 && pw>abswidth) pw=width=abswidth; //Na razie tylko abswidth

        int prefw=pw;
        int datw=0;
        ElemLine line=null;
        Elem ep=eldat;

        while(ep!=null) {
            ep.init(pw);
            ep=ep.gn();
        }
        ep=eldat;

        //int algn= align==DEFAULT ? defalign : align;
        boolean flin=true; //Flaga pierwszej linii

        if(pw!=-1) {
            setXY(0,0);
        } else { //Po ustaleniu minimalnego rozmiaru te parametry pozwalaja ustalic rozmiar dokladny
            prefw=width;
        }

        int ypos=y;//+padding;

        prefw-=2*padding;
        if(prefw<1)prefw=1;
        ep=eldat;

        while(ep!=null) {
            ep.init(prefw);

            line=pw==-1 ? new ElemLine(ep,prefw,x+padding,ypos)       //Rozmiar ustalony, mozna wyrownywac inaczej
                        : new ElemLine(ep,prefw,x+padding,ypos,LEFT); //Ustalanie rozmiaru dla LEFT (brak wciecia z lewej)
            ep=line.getEnd();
            ypos+=line.getHeight();
            if(flin)ascentlin=line.lineAsc;
            if(line.getWidth()>datw) datw=line.getWidth();
            flin=false;
        }
        heighti=ypos-y;//-padding; //Rzeczywisty rozmiar wnetrza komorki
        if(heighti>0)heighti+=2*padding;
        height=heighti/rowsp; //Rozmiar widziany na zewnatrz
        if(pw!=-1) {
            if(datw>0)datw+=2*padding;
            width= (datw>width ? datw : width)/colsp; //Dla pw==-1 width jest ustalane z zewnatrz
            //if(colsp>1) System.out.println(width);
        }
    }

    public void inity(int asc) { //Wyrownanie pionowe po ustaleniu wszystkich rozmiarow
        if(ends) return;

        Elem ep=eldat;
        int valgn= valign==DEFAULT ? defvalign : valign;
        int dy=0;

        if(valgn==MIDDLE) {
            dy=(height-heighti)/2; //Wysrodkowanie
        } else
        if(valgn==BOTTOM) {
            dy=height-heighti; //Do dolu
        }
        else if(valgn==BASELINE && asc>0) {
            dy=asc-ascentlin;
        }

        if(dy<0) dy=0;
        else if(dy>height-heighti) dy=height-heighti;

        dy+=padding;

        while(ep!=null) {
            ep.translate(0,dy);
            ep=ep.gn();
        }
    }

    public void setAlign(int a) {
        super.setAlign(a);
        Elem ep=eldat;
        while(ep!=null) {
            ep.setAlign(a);
            ep=ep.gn();
        }
    }


    public int gHeight() {
        return ends ? 0 : height;
    }

    public int gWidth() {
        return ends ? 0 : width;
    }

    public void sHeight(int h) {
        if(ends) return;

        height=h;
    }

    public void sWidth(int w) {
        if(ends) return;

        width=w;
    }

    public int getInAsc() { //Wykorzystywane tylko do wyrownania pionowego
        if(ends) return 0;

        int asctmp=0;
        int valgn= valign==DEFAULT ? defvalign : valign;
        if(height-heighti>0) {
            if(valgn==MIDDLE) {
                asctmp=(height-heighti)/2; //Wysrodkowanie
            } else
            if(valgn==BOTTOM) {
                asctmp=height-heighti; //Do dolu
            }
        }
        return ascentlin+asctmp;
    }

    public void setXY(int nx, int ny) {
        if(ends) return;

        int dx=nx-x,dy=ny-y;

        super.setXY(nx,ny);

       // translate(dx,dy);
        Elem ep=eldat;
        while(ep!=null) {
            ep.translate(dx,dy); //Nalezy przesunac podelementy
            ep=ep.gn();
        }
    }

    /*public void translate(int dx, int dy) {
        if(ends) return;

        super.translate(dx,dy);
        Elem ep=eldat;
        while(ep!=null) {
            ep.translate(dx,dy); //Nalezy przesunac podelementy
            ep=ep.gn();
        }
    }*/

    public void draw(Graphics g) {
        if(ends) return;

        Elem ep=eldat;
        while(ep!=null) {
            ep.redraw(g);
            ep=ep.gn();
        }
        if(border>0) {
            Graph.draw3DRect(g,x,y,getWidth(),getHeight(),false);
            g.setColor(Color.black);
        }
    }
}
