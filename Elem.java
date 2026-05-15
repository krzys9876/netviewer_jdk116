import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class Elem extends Object {
    Object elem=null;
    Elem next=null;
    int type=TEXT;
    int texttype=NORMAL;
    int align=DEFAULT;
    int valign=DEFAULT;
    boolean newLn=false;
    String linkloc=null;
    boolean enabled=true;
    int x=0,y=0;

    //int param=0;


    //Typy elementow
    public static int NOTHING=-2;
    public static int TAG=-1;

    public static int TEXT=0;
    public static int PARAGRAPH=1;
    public static int DIV=2;
    public static int CENTERPAR=3;
    public static int FONTMOD=10;
    public static int HR=25;
    public static int IMG=50;
    public static int TABLE=100;
    public static int TABROW=101;
    public static int TABDAT=102;
    public static int ANCHOR=150;
    public static int SELECT=200;
    public static int TEXTAREA=201;
    public static int INPUT=202;
    public static int INPUTTEXT=250;
    public static int INPUTPASS=251;
    public static int INPUTCHECK=252;
    public static int INPUTRADIO=253;
    public static int INPUTSUBMIT=254;
    public static int INPUTRESET=255;
    public static int INPUTHIDDEN=256;

    public static int OPTION=300;

    public static int ENDS=1000;

    //Typy elementow tekstowych
    public static int NORMAL=0;
    public static int H1=1;
    public static int H2=2;
    public static int H3=3;
    public static int H4=4;
    public static int H5=5;
    public static int H6=6;
    public static int TABHEADER=7;
    public static int PRE=8;

    //Modyfikatory typow elementow tekstowych
    public static int NOMOD=0;
    public static int B=1;
    public static int DFN=2;
    public static int STRONG=3;
    public static int I=4;
    public static int ADDRESS=5;
    public static int EM=6;
    public static int VAR=7;
    public static int BIG=8;
    public static int CODE=9;
    public static int KBD=10;
    public static int TT=11;
    public static int SMALL=12;

    //Parametry wyrownania pionowego i poziomego
    public static int DEFAULT=-1;

    public static int LEFT=0;
    public static int RIGHT=1;
    public static int CENTER=2;

    public static int TOP=3;
    public static int BOTTOM=4;
    public static int MIDDLE=5;
    public static int BASELINE=6;
    public static int ABSMIDDLE=7;
    public static int ABSBOTTOM=8;


    Elem(int t,Tag tag,boolean newl) {
        this(t,newl);

        int al=DEFAULT,val=DEFAULT; //Komorka domyslnie jest wyrownana do lewej i wysrodkowana pionowo

        Hashtable par=tag.getParams();
        Object algn=par.get("ALIGN");
        Object valgn=par.get("VALIGN");

        String salgn=null,svalgn=null;
        if(algn instanceof String) salgn=(String)algn;
        if(valgn instanceof String) svalgn=(String)valgn;

        if(salgn!=null) {
            if("LEFT".equalsIgnoreCase(salgn)) al=LEFT;
            else if ("RIGHT".equalsIgnoreCase(salgn)) al=RIGHT;
            else if ("CENTER".equalsIgnoreCase(salgn)) al=CENTER;
            else if ("BOTTOM".equalsIgnoreCase(salgn)) val=BOTTOM;
            else if ("MIDDLE".equalsIgnoreCase(salgn)) val=MIDDLE;
            else if("TOP".equalsIgnoreCase(salgn)) val=Elem.TOP;
        }

        if(svalgn!=null) {
            if("TOP".equalsIgnoreCase(svalgn)) val=Elem.TOP;
            else if ("BOTTOM".equalsIgnoreCase(svalgn)) val=BOTTOM;
            else if ("MIDDLE".equalsIgnoreCase(svalgn)) val=MIDDLE;
            else if ("CENTER".equalsIgnoreCase(svalgn)) val=MIDDLE; //Czasem sie zdarza
            else if ("BASELINE".equalsIgnoreCase(svalgn)) val=BASELINE;
        }

        align=al;
        valign=val;
    }

 //   Elem(int t,Tag tag,boolean newl) {
 //       this(t,tag,newl);
 //   }

    Elem(int t,Tag tag) {
        this(t,tag,false);
    }

    Elem(Tag tag,boolean newl) {
        this(TAG,tag,newl);
    }

    Elem(int t,boolean newl) {
        type=t;
        newLn=newl;
    }

  //  Elem(int t,boolean newl) {
  //      this(t,newl,null);
  //  }

    Elem(int t) {
        this(t,false);
    }

    public void setLink(String link) {
        linkloc=link;
    }

    public String getLink() {
        return linkloc;
    }

   // public void init() {
   // }

    public void format() {
    }

    public void init0() {
    }

    public void init(int pw) { //preffered width
    }

    public Object ge() {
        return elem;
    }

    public void se(Object el) {
        elem=el;
    }

    public Elem gn() {
        return next;
    }

    public synchronized void sn(Elem el) {
        next=el;
    }

    public int getType() {
        return type;
    }

    public void setType(int t) {
        type=t;
    }

    public int getAlign() {
        return align;
    }

    public void setAlign(int a) {
        align=a;
    }

    public int getValign() {
        return valign;
    }

    public void setValign(int a) {
        valign=a;
    }

    public void setTexttype(int tt) {
        texttype=tt;
    }

    public void modTexttype(int tm) {
    }

    public void setMod(int m) {
    }

    public int getMod() {
        return NOMOD;
    }

    public final int getTexttype() {
        return texttype;
    }

    public final int getWidth() {
        //return enabled ? gWidth() : 0;
        return gWidth();
    }

    public final int getHeight() {
        //return enabled ? gHeight() : 0;
        return gHeight();
    }

    public final void setWidth(int w) {
        //return enabled ? gWidth() : 0;
        sWidth(w);
    }

    public final void setHeight(int h) {
        //return enabled ? gHeight() : 0;
        sHeight(h);
    }

    public final int getAsc() {
        //return enabled ? gAsc() : 0;
        return gAsc();
    }

    public final int getDesc(int asc) {
        //return enabled ? gDesc(asc) : 0;
        return gDesc(asc);
    }

    public boolean newLine() {
        //return enabled ? newLn : false;
        return newLn;
    }

    public  int gWidth() {
        return 0;
    }

    public int gHeight() {
        return 0;
    }

    public void sWidth(int w) {
    }

    public void sHeight(int h) {
    }

    public int gAsc() {
        return 0;
    }

    public int gDesc(int asc) {
        return 0;
    }

    public Rectangle getBounds() {
        return new Rectangle(x,y-getAsc(),getWidth(),getHeight());
    }

    public void registerLink() {
    }

    public void checkEnable(Rectangle rect) {
        enabled=getBounds().intersects(rect);
    }

    public void initComp(Rectangle rect) {//Dla elementow kontrolnych
    }

   /* public void disable() {
        enabled=false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isDisabled() {
        return !enabled;
    }*/

    public void setXY(int nx, int ny) {
     //   if(enabled) {
            x=nx;
            y=ny;
     //   }
    }

    public void translate(int dx, int dy) {
        setXY(x+dx,y+dy);
    }

    public final void redraw(Graphics g) {
        if(enabled) {
            draw(g);
        }
    }

    public void draw(Graphics g) {
    }

    public boolean isLink() {
        return linkloc!=null;
    }

 /*   public void setParam(int nparam) {
        param=nparam;
    }

    public int getParam() {
        return param;
    }*/
}

