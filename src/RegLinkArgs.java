import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class RegLinkArgs extends Object {
    Rectangle rect=null;
    String loc=null;

    RegLinkArgs(Rectangle r, String nloc) {
        rect=r;
        loc=nloc;
    }

    public boolean inside(int x, int y) {
        return rect.inside(x,y);
    }

    public Rectangle getRect() {
        return rect;
    }

    public String getLoc() {
        return loc;
    }
}