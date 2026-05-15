import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class TabHeadElem extends TabDatElem {
    TabHeadElem(Tag tag) {
        super(tag);
        divalign=CENTER;
    }

    public void init0() {
        if(ends) return;

        if(getAlign()==DEFAULT) divalign=CENTER;
        else divalign=DEFAULT;

        super.init0();
        Elem ep=eldat;
        while(ep!=null) {
            if(ep.getType()==TEXT && ep.getTexttype()==NORMAL) {
                ep.setTexttype(TABHEADER);
            }
            ep=ep.gn();
        }
    }
}
