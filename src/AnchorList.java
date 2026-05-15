import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import java.util.*;

class AnchorList extends Object {
    AnchorData list=null;

    public synchronized void add(AnchorArgs args) {
        //System.out.println("name="+args.name);//!!!
        list=new AnchorData(args,list);
    }

    public int namePos(String name) {
        int pos=-1;
        AnchorData tmp=list;

        while(tmp!=null && pos==-1) {
            pos=tmp.namePos(name);
            tmp=tmp.gn();
        }

        if(pos!=-1) return pos;

        return -1;
    }

    public void clear() {
        list=null;
    }
}
