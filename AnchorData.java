import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import java.util.*;

class AnchorData extends Object {
    AnchorArgs args=null;
    AnchorData n=null;

    AnchorData(AnchorArgs a,AnchorData nn) {
        args=a;
        n=nn;
    }

    public AnchorData gn() {
        return n;
    }

    public synchronized void sn(AnchorData nn) {
        n=nn;
    }

    public AnchorArgs getAnch() {
        return args;
    }

    public int namePos(String name) {
        if (name==null) return -1;
        if(args==null) return -1;

        if(name.equalsIgnoreCase(args.getName())) return args.getPos();

        return -1;
    }
}
