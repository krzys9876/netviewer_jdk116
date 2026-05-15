import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class DivElem extends TagElem {
    int divalign=LEFT;

    DivElem(Tag tag) {
        super(tag,true);
        divalign=align;
    }

    public void init0() {
        if(ends) return;

        align=divalign; //To pole moglo byc zmodyfikowane wczesniej (np. przez ParagraphElem) wiec trzeba przypisac wartosc tutaj
        Elem el=gn();
        while(el!=null && el.getType()!=type+ENDS) {
            if(getAlign()!=DEFAULT)el.setAlign(getAlign());
            el=el.gn();
        }
    }
}

