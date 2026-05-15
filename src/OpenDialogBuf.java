import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class OpenDialogBuf extends Object {
    boolean open1=false;
    boolean open2=false;
    String loc=null;
    String dir=null;

    OpenDialogBuf(String d, String l) {
        dir=d;
        loc=l;
    }

    public String getDir() {
        return dir;
    }

    public String getLoc() {
        return loc;
    }

    public boolean getOpen1() {
        return open1;
    }

    public boolean getOpen2() {
        return open2;
    }
}