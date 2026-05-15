import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class FontModElem extends TagElem {
    int mod=0;

    FontModElem(Tag tag) {
        super(tag,false);
    }

    public void init0() {
        if(ends) return;

        Elem el=gn();
        while(el!=null && !(el.getMod()==mod && el.getType()==type+ENDS)
              && el.getType()!=TABDAT+ENDS && el.getType()!=TABLE+ENDS
              && el.getType()!=TABDAT && el.getType()!=TABLE
              && el.getType()!=TABROW && el.getType()!=TABROW+ENDS) {
            el.modTexttype(mod);
            el=el.gn();
        }
    }

    public void setMod(int m) {
        mod=m;
    }

    public int getMod() {
        return mod;
    }
}
