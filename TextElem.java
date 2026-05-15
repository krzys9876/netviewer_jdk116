import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class TextElem extends Elem {
    FontMetrics fm=null;
    Font font=null;
    Props props=null;
    boolean needsFormat=false;
    Stack fontMod=new Stack();

    TextElem (String text, Props p,boolean form) {
        this(NORMAL,text,p);

        needsFormat=form;
    }

    TextElem (int tt, String text, Props p) {
        super(TEXT);
        props=p;
        setTexttype(tt);//setTexttype(tt);
        se(text);
    }

    public void init0() {
        if(!(elem instanceof String)) return;

        String str=(String)elem;

        if("".equals(str)) System.out.println("xxx");//!!!

        if(!needsFormat) {
            se(Props.encode(str));//Odkodowanie znakow specjalnych
            return;
        }

        Elem e0=new Elem(NOTHING),e1=null;
        String strtmp=null;
        StringTokenizer strt=null;

        e1=e0;
        boolean pre=getTexttype()==PRE;
        boolean delimflag=false;//Czy wystapil znak "\n", "\r", "\t" lub " "
        boolean textflag=false; //Czy wystapil inny tekst niz powyzej

        strt=new StringTokenizer(str,"\n\r\t ",true);
        while(strt.hasMoreElements()) {
            strtmp=strt.nextToken();
            if(!pre) {
                if("\t".equals(strtmp) || "\n".equals(strtmp) || "\r".equals(strtmp) || " ".equals(strtmp)) {
                    delimflag=true;
                } else {
                    if(delimflag && textflag) e1=add(new TextElem(getTexttype()," ",props),e1);
                    e1=add(new TextElem(getTexttype(),strtmp,props),e1);
                    textflag=true;
                    delimflag=false;
                }
            } else {
                if("\t".equals(strtmp)) {
                    e1=add(new TextElem(getTexttype(),"    ",props),e1);
                } else
                if("\n".equals(strtmp)) {
                    e1=add(new PElem(props,false),e1);
                } else
                if(!"\r".equals(strtmp)) {
                    e1=add(new TextElem(getTexttype(),strtmp,props),e1);
                }
            }
        }
        if(textflag) {
            Elem n=gn();
            boolean nl=false;
            while(n!=null) {
                if(n.newLine())nl=true;
                if(n.getType()==TEXT) n=null;
                else n=n.gn();
            }
            if(!pre && !nl) {
                Elem etmp=new TextElem(getTexttype()," ",props);
                e1=add(etmp,e1);
                etmp.setLink(null);
            }
        }

        if(e1!=e0 && e1!=null) { //Dodawanie stworzonych elementow do listy
            e1.sn(gn());
            sn(e0.gn());
        }
        se(null); //Usuniecie sformatowanego tekstu
    }

    public void setFont(Font f) {
        font=new Font(f.getName(),f.getStyle(),f.getSize());
        updateMetrics();
    }

    private Elem add(Elem e, Elem e0) {
        e.setLink(getLink());
        e.setAlign(align);
        ((TextElem)e).setFont(font);
        e0.sn(e);
        e0=e;
        return e0;
    }

    public void modTexttype(int tm) {
        fontMod.push(new Integer(tm));
        updateFont();
    }

    private void updateFont() {
        font=Props.getFont(getTexttype());
        int mod=0;

        Stack fontModtmp=new Stack();
        while(!fontMod.empty()) {
            mod=((Integer)fontMod.pop()).intValue();
            fontModtmp.push(new Integer(mod));
            if(mod==B || mod==DFN || mod==STRONG) {
                font=new Font(font.getName(),font.getStyle() | Font.BOLD,font.getSize());
            } else
            if(mod==I || mod==ADDRESS || mod==EM || mod==VAR) {
                font=new Font(font.getName(),font.getStyle() | Font.ITALIC,font.getSize());
            } else
            if(mod==BIG) {
                font=new Font(font.getName(),font.getStyle(),font.getSize()+2);
            } else
            if(mod==CODE || mod==KBD || mod==TT) {
                font=new Font("Courier",font.getStyle(),font.getSize());
            } else
            if(mod==SMALL) {
                font=new Font(font.getName(),font.getStyle(),font.getSize()>6 ? font.getSize()-2 : font.getSize());
            }
        }
        fontMod=fontModtmp;
    }


    public void setTexttype(int tt) {
        super.setTexttype(tt);
        updateFont();
        updateMetrics();
    }

    private void updateMetrics() {
        fm=Toolkit.getDefaultToolkit().getFontMetrics(font);
    }

    public int gWidth() {
        if(elem==null) return 0;

        return fm.stringWidth((String)elem);
    }

    public int gHeight() {
        if(elem==null) return 0;

        return fm.getHeight();
    }

    public int gAsc() {
        if(elem==null) return 0;

        return fm.getAscent();
    }

    public int gDesc(int asc) {
        if(elem==null) return 0;
        return fm.getDescent();
    }

    public void registerLink() {
        if(elem==null) return;



        if(isLink() && props!=null) {
            props.owner.larea.add(new RegLinkArgs(getBounds(),linkloc));
           // props.owner.postEvent(new Event(this,Event.ACTION_EVENT,
           //             new RegLinkArgs(getBounds(),linkloc)));
        }
    }

    public void draw(Graphics g) {
        if(elem==null) return;


        g.setFont(font);
        if(isLink()) {
            g.setColor(Color.blue);
            g.drawString((String)elem,x,y);
            g.drawLine(x,y+1,x+getWidth(),y+1);
            g.setColor(Color.black);
        } else {
            g.setColor(Color.black);
            g.drawString((String)elem,x,y);
        }
    }
}