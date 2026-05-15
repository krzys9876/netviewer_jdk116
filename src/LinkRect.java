import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class LinkRect extends Object {
    RegLinkArgs linkargs=null;
    LinkRect n=null;

    LinkRect(RegLinkArgs link,LinkRect nn) {
        linkargs=link;
        n=nn;
    }

    public LinkRect gn() {
        return n;
    }

    public void sn(LinkRect nn) {
        n=nn;
    }

    public RegLinkArgs getLink() {
        return linkargs;
    }

    public boolean inside(int x,int y) {
        return linkargs.inside(x,y);
    }
}

