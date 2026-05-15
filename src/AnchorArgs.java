import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import java.util.*;

class AnchorArgs extends Object {
    String name=null;
    int pos=0;

    AnchorArgs(String n, int p) {
        name=n;
        pos=p;
    }

    public String getName() {
        return name;
    }

    public int getPos() {
        return pos;
    }
}
