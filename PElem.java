import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class PElem extends TextElem {
    boolean nextNewLn=true;

    PElem(Props pr) {
        this(pr,true);
    }

    PElem(Props pr,boolean newln) {
        super(NORMAL,"",pr);
        newLn=true;
        nextNewLn=newln;
    }

    public void init0() {
        if(!nextNewLn)return;

        Elem el=gn();
        if(el!=null) el.newLn=nextNewLn;
    }
}
