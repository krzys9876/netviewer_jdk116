import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import java.util.*;

class HTMLDoc extends Object {
    Elem elStart=new Elem(Elem.NOTHING);
    Elem elEnd=null;
    String title=null;
    Props props=null;

    HTMLDoc(Props pr) {
        super();
        props=pr;
        elEnd=elStart;
    }

    public void add(Elem e) {
        if(e==null) return;
        elEnd.sn(e);
        elEnd=elEnd.gn();
    }

    public Elem getElem() {
        return elStart;
    }

    public void format(String str) {
        if(str==null) return;

        clear();

        int elType=Elem.NORMAL;
        boolean maketitle=false;
        title=new String();
        Tag tag=null;
        String z,tmp,strtmp;

        StringTokenizer strt=new StringTokenizer(str,"<>\n",true);

        while(strt.hasMoreElements()) {
            strtmp=strt.nextToken();
            if("<".equals(strtmp)) {
                z=new String();
                boolean comment=false,cend=true; //cend oznacza pozwolenie na zakonczenie petli po znalezieniu ">"
                tmp=strt.nextToken();
                if(tmp!=null && tmp.startsWith("!--")) comment=true;
                while(strt.hasMoreTokens() && (!">".equals(tmp) || !cend)) {
                    if(comment) {
                        cend=tmp.endsWith("--"); //Zakonczyc komentarz mozna tylko dla "--" przed ">"
                    }
                    else {
                        z=z.concat(tmp);
                    }
                    tmp=strt.nextToken();
                }
                tag=new Tag(z);

                if("TITLE".equalsIgnoreCase(tag.getName())) {
                    maketitle=!tag.ends();
                } else
                if("H1".equalsIgnoreCase(tag.getName()) ||
                   "H2".equalsIgnoreCase(tag.getName()) ||
                   "H3".equalsIgnoreCase(tag.getName()) ||
                   "H4".equalsIgnoreCase(tag.getName()) ||
                   "H5".equalsIgnoreCase(tag.getName()) ||
                   "H6".equalsIgnoreCase(tag.getName()) ||
                   "PRE".equalsIgnoreCase(tag.getName())) {
                    add(new ParagraphElem(tag));
                } else
                if("B".equalsIgnoreCase(tag.getName()) ||
                   "DFN".equalsIgnoreCase(tag.getName()) ||
                   "STRONG".equalsIgnoreCase(tag.getName()) ||
                   "I".equalsIgnoreCase(tag.getName()) ||
                   "ADDRESS".equalsIgnoreCase(tag.getName()) ||
                   "EM".equalsIgnoreCase(tag.getName()) ||
                   "VAR".equalsIgnoreCase(tag.getName()) ||
                   "BIG".equalsIgnoreCase(tag.getName()) ||
                   "CODE".equalsIgnoreCase(tag.getName()) ||
                   "KBD".equalsIgnoreCase(tag.getName()) ||
                   "TT".equalsIgnoreCase(tag.getName()) ||
                   "SMALL".equalsIgnoreCase(tag.getName())) {
                    add(new FontModElem(tag));
                } else
                if("P".equalsIgnoreCase(tag.getName())) {
                    add(new PElem(props));
                } else
                if("BR".equalsIgnoreCase(tag.getName())) {
                    add(new BrElem());
                } else
                if("HR".equalsIgnoreCase(tag.getName())) {
                    add(new HrElem(tag));
                } else
                if("BLOCKQUOTE".equalsIgnoreCase(tag.getName()) ||
                   "DD".equalsIgnoreCase(tag.getName()) ||
                   "DFN".equalsIgnoreCase(tag.getName()) ||
                   "DIR".equalsIgnoreCase(tag.getName()) ||
                   "DL".equalsIgnoreCase(tag.getName()) ||
                   "DT".equalsIgnoreCase(tag.getName()) ||
                   "FORM".equalsIgnoreCase(tag.getName()) ||
                   "LI".equalsIgnoreCase(tag.getName()) ||
                   "MENU".equalsIgnoreCase(tag.getName()) ||
                   "NOBR".equalsIgnoreCase(tag.getName()) ||
                   "OL".equalsIgnoreCase(tag.getName()) ||
                   "SAMP".equalsIgnoreCase(tag.getName()) ||
                   "UL".equalsIgnoreCase(tag.getName())) {
                    add(new BrElem());
                } else
                if("SELECT".equalsIgnoreCase(tag.getName())) {
                    add(new SelectElem(tag,props));
                } else
                if("OPTION".equalsIgnoreCase(tag.getName())) {
                    add(new OptionElem(tag));
                } else
                if("TEXTAREA".equalsIgnoreCase(tag.getName())) {
                    add(new TextAreaElem(tag,props));
                } else
                if("INPUT".equalsIgnoreCase(tag.getName())) {
                    add(new InputElem(tag,props));
                } else
                if("DIV".equalsIgnoreCase(tag.getName())) {
                    add(new DivElem(tag));
                } else
                if("CENTER".equalsIgnoreCase(tag.getName())) {
                    add(new CenterElem(tag));
                } else
                if("IMG".equalsIgnoreCase(tag.getName())) {
                    add(new ImgElem(tag,props));
                } else
                if("A".equalsIgnoreCase(tag.getName())) {
                    add(new AnchorElem(tag,props));
                } else
                if("TABLE".equalsIgnoreCase(tag.getName())) {
                    add(new TableElem(tag));
                } else
                if("TR".equalsIgnoreCase(tag.getName())) {
                    add(new TrElem(tag));
                } else
                if("TD".equalsIgnoreCase(tag.getName())) {
                    add(new TabDatElem(tag));
                } else
                if("TH".equalsIgnoreCase(tag.getName())) {
                    add(new TabHeadElem(tag));
                }

            } else {
                if(maketitle) {
                    StringTokenizer strtt=new StringTokenizer(strtmp);
                    while(strtt.hasMoreTokens()) {
                        title=title+strtt.nextToken()+" ";
                    }
                } else {
                    formatStr(strtmp,elType);
                }
            }
        }

        Elem ep=getElem();
        while(ep!=null) {
            ep.format();
            ep=ep.gn();
        }

        ep=getElem();
        while(ep!=null) {
            ep.init0();
            ep=ep.gn();
        }
    }

    private void formatStr(String str,int elType) {
      //  if(elType!=Elem.PRE) {
            add(new TextElem(str,props,true));
            return;
      //  }

/*
        String strtmp=null;
        StringTokenizer strt=null;
        //Dla preformatowanego tekstu liczy sie tylko znak konca wiersza \n
        // (przy okazji pomija sie znak \r)
        if(elType==Elem.PRE) strt=new StringTokenizer(str,"\n\r\t",true);
        else strt=new StringTokenizer(str);
        while(strt.hasMoreElements()) {
            strtmp=strt.nextToken();
            if("\t".equals(strtmp)) {
                add(new TextElem(elType,"    ",props));
            } else
            if("\n".equals(strtmp) && elType==Elem.PRE) {
                add(new PElem(props,false));
            } else
            if(!"\r".equals(strtmp)) {
                add(new TextElem(elType,strtmp+" ",props));
            }
        }*/
    }

    public String getTitle() {
        return title;
    }

    public void clear() {
        elStart=new Elem(Elem.NOTHING);
        elEnd=elStart;
    }
}