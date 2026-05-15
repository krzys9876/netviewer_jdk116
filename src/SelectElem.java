import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class SelectElem extends CompElem {
    boolean multiple=false;
    int size=0;
    Hashtable values=new Hashtable();

    SelectElem(Tag tag, Props pr) {
        super(tag, pr);

     //   props=pr;

        Hashtable par=tag.getParams();

        Object omulti=par.get("MULTIPLE");
        Object osize=par.get("SIZE");

        size=0;

        if(omulti instanceof Object) multiple=true;
        if(osize instanceof Integer) size=((Integer)osize).intValue();
    }


    public void init0() {
        if(ends) return;

        if(size<2 && !multiple) {
            comp=new Choice();
            size=1;
        } else {
            if(size==0) size=3;
            comp=new List(size,multiple); //Domyslnie 3 opcje widoczne
        }

        comp.setFont(Props.getFont(INPUT));
        comp.hide();
        parent.add(comp);

        int items=0;

        Elem el=gn();
        while(el!=null && el.getType()!=SELECT && el.getType()!=SELECT+ENDS) {
            el.init0();
            if(el.getType()==OPTION) {
                String strtext=((OptionElem)el).text;
                String strval=((OptionElem)el).value;
                boolean sel=((OptionElem)el).selected;
                if(strval!=null) {
                    items++;
                    if(strtext==null) strtext=new String();
                    if(comp instanceof List) {
                        List l=(List)comp;
                        l.addItem(strtext);
                        if(sel) {
                            l.select(l.countItems()-1);
                        }
                    }
                    else if (comp instanceof Choice) {
                        Choice c=(Choice)comp;
                        c.addItem(strtext);
                        if(sel) {
                            c.select(c.countItems()-1);
                        }
                    }
                    values.put(strtext,strval);
                }

            }
            el=el.gn();
        }

        if(items<2) comp.disable();  //Dla mniej niz 2 opcji nie ma co wybierac

        sn(el);

        width=comp.preferredSize().width;
        height=comp.preferredSize().height;
    }

   public int gAsc() {
        if(ends) return 0;

        if(size<2) return super.gAsc();

        return height/2;
    }

    public int gDesc(int asc) {
        if(ends) return 0;

        if(size<2) return super.gDesc(asc);

        return height/2;
    }
}
