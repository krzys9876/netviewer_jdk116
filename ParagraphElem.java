import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class ParagraphElem extends TagElem {
    int partype=NORMAL;

    ParagraphElem(Tag tag) {
        super(tag,true);
        partype=texttype;
    }

    public void init0() {
        if(ends) return;

        Elem el=gn();
        while(el!=null && el.getType()!=type+ENDS && el.getTexttype()!=partype) {
            el.setTexttype(partype);
            if(align!=DEFAULT && el.getAlign()==DEFAULT)el.setAlign(align);
            el=el.gn();
        }
    }
}




/*
class PreElem extends TagElem {
    PreElem(Tag tag) {
        super(tag,true);
    }

    public void init0() {
        if(!ends) {
            Elem el=gn();
            while(el!=null && el.getType()!=type+ENDS && el.getTexttype()!=texttype) {
                el.setTexttype(texttype);
                if(el.ge() instanceof String) {
                    if(((String

                el=el.gn();
            }
        }
    }
}*/