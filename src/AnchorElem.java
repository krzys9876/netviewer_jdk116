import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class AnchorElem  extends TagElem {
    String name=null;
    Props props=null;

    AnchorElem(Tag tag,Props p) {
        super(tag,false);

        props=p;

        Hashtable par=tag.getParams();
        Object oname=par.get("NAME");
        Object ohref=par.get("HREF");


        if(ohref!=null && ohref instanceof String) {
            setLink((String)ohref);
        }

        if(oname!=null) {
            if(oname instanceof String) {
                name=(String)oname;
            //System.out.println("A NAME="+name);//!!!
            } else
            if(oname instanceof Integer) {
                name=String.valueOf(((Integer)oname).intValue());
            }
        }
    }

    public void init0() {
        if(ends || name!=null || linkloc==null) return;

        Elem el=gn();
        while(el!=null && el.getType()!=type+ENDS) {
            el.setLink(linkloc);
            el=el.gn();
        }
    }

    public void setXY(int nx, int ny) {
        super.setXY(nx,ny);
       // if(name!=null)  System.out.println("A NAME="+name);//!!!

        if(name!=null) {
            props.owner.anchlist.add(new AnchorArgs(name,y));
          //  props.owner.postEvent(new Event(this,Event.ACTION_EVENT,new AnchorArgs(name,y)));
        }
    }
}
