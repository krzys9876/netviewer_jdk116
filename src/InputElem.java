import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class InputElem extends CompElem {
    String value=null;
    boolean checked=false;
    int size=0;
    static Hashtable grouptable=new Hashtable();//Dla ramek trzeba to zmienic!!!!
    //static Hashtable grouptable=new Hashtable();

    InputElem(Tag tag, Props pr) {
        super(tag, pr);

        Hashtable par=tag.getParams();

        Object ocheck=par.get("CHECKED");
        Object osize=par.get("SIZE");
        Object ovalue=par.get("VALUE");

        if(ocheck instanceof Object) checked=true;
        if(osize instanceof Integer) size=((Integer)osize).intValue();
        if(ovalue instanceof String) value=new String((String)ovalue);
    }

    public void init0() {
        if(ends) return;

        if(getType()==INPUTTEXT || getType()==INPUT) { //Domyslnie pole tekstowe
            if(size==0) size=20;
            comp=new TextField(value,size);
        } else
        if(getType()==INPUTSUBMIT) {
            if(value==null || "".equals(value)) value=new String("Submit");
            comp=new Button(value);
        } else
        if(getType()==INPUTRESET) {
            if(value==null || "".equals(value)) value=new String("Reset");
            comp=new Button(value);
        } else
        if(getType()==INPUTPASS) {
            if(size==0) size=20;
            comp=new TextField(value,size);
            ((TextField)comp).setEchoCharacter('*');
        } else
        if(getType()==INPUTCHECK) {
            comp=new Checkbox();
            ((Checkbox)comp).setState(checked);
        } else
        if(getType()==INPUTRADIO) {
            CheckboxGroup group=null;
            Object ogroup=grouptable.get(name);
            if(ogroup instanceof CheckboxGroup) group=(CheckboxGroup)ogroup;
            if(group==null) {
                group=new CheckboxGroup();
                grouptable.put(name,group);
            }

          //  Components[] comps=props.owner.getComponents();
          //  for(int i=0;i<comps.length(); i++) {
          //      if(comps[i] instanceof Checkbox) {
          //          Checkbox cb=(Checkbox)comps[i];
          //          if(cb.getCheckboxGroup!=null) {
          //              group=grouptable.

            comp=new Checkbox(null,group,checked);
        }
        if(getType()==INPUTHIDDEN) {
            if(value==null) value=new String();
        }


        if(comp==null) return;

        comp.setFont(Props.getFont(INPUT));
        comp.hide();
        parent.add(comp);
        //con.add(comp,con.countComponents()-1);
        //props.owner.getParent().add(comp);

        width=comp.preferredSize().width;
        height=comp.preferredSize().height;
    }
}
