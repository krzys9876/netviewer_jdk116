import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class TextAreaElem extends CompElem {
    int rows=0,cols=0;

    TextAreaElem(Tag tag, Props pr) {
        super(tag,pr);

        Hashtable par=tag.getParams();

        Object orows=par.get("ROWS");
        Object ocols=par.get("COLS");


        if(orows instanceof Integer) rows=((Integer)orows).intValue();
        if(ocols instanceof Integer) cols=((Integer)ocols).intValue();
    }

    public void init0() {
        if(ends) return;

        if(rows==0) rows=8;
        if(cols==0) rows=40;
        comp=new TextArea(rows,cols);

        comp.setFont(Props.getFont(INPUT));
        comp.hide();
        parent.add(comp);

        Elem el1=null,el=gn();
        String str=new String();
        if(el!=null && el.getType()!=TEXTAREA && el.getType()!=TEXTAREA+ENDS) {
            el1=el;
            while(el!=null && el.getType()!=TEXTAREA && el.getType()!=TEXTAREA+ENDS) {
                el.init0();
                if(el.getType()==TEXT) {
                    Object oel=el.ge();
                    if(oel instanceof String) {
                        str=str.concat((String)oel);
                        el.se(null);
                    }
                } else {
                    el1=el;
                }
                el=el.gn();

                if(el==null) { //Ominiecie elementow TEXT
                    el1.sn(null);
                } else
                if(el.getType()!=TEXT) {
                    el1.sn(el);
                }
            }
            sn(el);
        }
        ((TextArea)comp).appendText(str);

        width=comp.preferredSize().width;
        height=comp.preferredSize().height;
    }

    public int gAsc() {
        if(ends) return 0;

      //  if(rows<2) return super.getAsc();

        return height/2;
    }

    public int gDesc(int asc) {
        if(ends) return 0;

       // if(rows<2) return super.getDesc(asc);

        return height/2;
    }
}
