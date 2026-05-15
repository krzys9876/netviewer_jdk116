import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class LinkArea extends Object {
    LinkRect rect=null;
    int x0=0,y0=0;

    public synchronized void add(RegLinkArgs r) {
        rect=new LinkRect(r,rect);
    }

    public LinkRect inside(int x, int y) {
        x0=x;y0=y;
        LinkRect rtmp=rect;
        while(rtmp!=null && !rtmp.inside(x,y)) rtmp=rtmp.gn();
        return rtmp;
    }

    public void clear() {
        rect=null;
    }
}
