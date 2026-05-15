import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class CompElem extends TagElem {
    Props props=null;
    Component comp=null;
    String name=null;
    int width=0,height=0;
    Container parent=null;

    CompElem(Tag tag, Props pr) {
        super(tag,false);
        props=pr;

        parent=props.owner;

        Hashtable par=tag.getParams();

        Object oname=par.get("NAME");
        if(oname instanceof String) name=new String((String)oname);
    }

    public void initComp(Rectangle rect) {//Dla elementow kontrolnych
        if(ends || comp==null) return;

        super.checkEnable(rect);
        //System.out.println("enabled="+enabled);//!!!

        if(enabled) {
            if(!comp.isVisible()) comp.show();
            //comp.move(x+props.owner.originX+props.marg,
            //             y-getAsc()+props.owner.originY+props.marg);
            comp.reshape(x+props.owner.originX+props.marg,
                         y-getAsc()+props.owner.originY+props.marg,
                         getWidth(),getHeight());
        } else if(comp.isVisible()) comp.hide();
    }

    public int gWidth() {
        return ends ? 0 : width;
    }

    public int gHeight() {
        return ends ? 0 : height;
    }

    public int gAsc() {
        return ends ? 0 : height*3/4;
    }

    public int gDesc(int asc) {
        return ends ? 0 : height/4;
    }
}
