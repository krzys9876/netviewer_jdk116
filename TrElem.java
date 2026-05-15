import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class TrElem extends DivElem {
    TrElem(Tag tag) {
        super(tag);
    }

    public void init0() {
        if(ends) return;

        if(divalign!=DEFAULT) setAlign(divalign); //To pole moglo byc zmodyfikowane wczesniej (np. przez ParagraphElem) wiec trzeba przypisac wartosc tutaj
        Elem el=gn();
        while(el!=null && el.getType()!=type+ENDS && el.getType()!=TABLE+ENDS) {
            if(getAlign()!=DEFAULT)el.setAlign(getAlign());
            el=el.gn();
        }
    }
}
