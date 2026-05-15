import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class HrElem extends TagElem {
    int size=0,abswidth=-1,relwidth=-1;
    int width=0;
    boolean noshade=false;

    HrElem(Tag tag) {
        super(tag,true);

        Hashtable par=tag.getParams();

        Object owid=par.get("WIDTH");
        Object osize=par.get("SIZE");
        Object onosh=par.get("NOSHADE");

        size=0;relwidth=-1;abswidth=-1;noshade=false;

        if(owid!=null) {
            if(owid instanceof String) {
                String widstr=(String)owid;
                if(widstr.endsWith("%")) {
                    widstr=widstr.substring(0,widstr.lastIndexOf("%"));
                    relwidth=Integer.valueOf(widstr).intValue();
                }
            } else
            if(owid instanceof Integer) {
                abswidth=((Integer)owid).intValue();
            }
        }

        if(osize instanceof Integer) size=((Integer)osize).intValue();
        if(onosh instanceof Object) noshade=true;

        if(size==0) size=2;
    }

    public void init0() {
        if(ends) return;

        Elem el=gn();
        if(el!=null) el.newLn=true;
    }

    public void init(int pw) {

        if(ends) return;

        if(relwidth>0) width=relwidth*pw/100;
        else if(relwidth>0) width=abswidth;
        else width=pw;
    }

    public int gAsc() {
        return ends ? 0 : size+5;
    }

    public int gDesc(int asc) {
        return ends ? 0 : size+5;
    }

    public int gWidth() {
        return ends ? 0 : width;
    }

    public int gHeight() {
        return ends ? 0 : size+10;
    }

    public void draw(Graphics g) {
        if(ends) return;

        if(noshade) {
            g.setColor(Color.darkGray);
            g.fillRect(x,y-getAsc(),getWidth(),size);
            g.setColor(Color.black);
        } else {
            Graph.draw3DRect(g,x,y-size/2,getWidth(),size,false);
        }
    }
}
