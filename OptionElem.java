import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class OptionElem extends TagElem {
    String text=null;
    String value=null;
    boolean selected=false;

    OptionElem(Tag tag) {
        super(tag,false);

        Hashtable par=tag.getParams();

        Object ovalue=par.get("VALUE");
        Object oselect=par.get("SELECTED");

        value=new String();

        if(ovalue instanceof String) value=new String((String)ovalue);
        if(oselect instanceof Object) selected=true;
    }

    public void init0() {
        Elem el1=null,el=gn();
        String str=new String();
        if(el!=null && el.getType()!=SELECT && el.getType()!=SELECT+ENDS && el.getType()!=OPTION && el.getType()!=OPTION+ENDS) {
            el1=el;
            while(el!=null && el.getType()!=SELECT && el.getType()!=SELECT+ENDS && el.getType()!=OPTION && el.getType()!=OPTION+ENDS) {
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
        text=str;

//        System.out.println("Value="+value+" Text="+str);//!!!
    }
}
