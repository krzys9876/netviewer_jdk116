import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class mCanvas extends Panel {
    int docHeight=0,docWidth=0;
    int originX, originY;
    String docLoc=null;
    String docRef=null;
    String docServ=null;
    String docFile=null;
    boolean initflag=false; //Czy dokument zostal zainicjalizowany, wazne dla updateImage
    boolean formatflag=false;
    Props props=null;
    LinkRect mouseOnLink=null;
    LinkArea larea=new LinkArea(); //Obszar - lista prostokatow skrotow
    ImgElemList imglist=new ImgElemList();
    AnchorList anchlist=new AnchorList();
    String title=null;
    RepaintThread repthread=null;
    //InitThread initthread=null;

    int docw,doch;

    //Stack initStack=new Stack();
    //boolean initreq=false;



    HTMLDoc doc=null;

    mCanvas(int w, int h) {
        super();
        setLayout(null);
        reshape(0,0,w,h);
        docHeight=0;
        docWidth=0;
        originX=originY=0;
        setBackground(Color.lightGray);
        props=new Props(this);
        doc=new HTMLDoc(props);
    }

    public boolean mouseExit(Event evt,int x, int y) {
        if(mouseOnLink!=null) getParent().postEvent(new Event(this,Event.ACTION_EVENT,new StatusBarData("")));
        mouseOnLink=null;
        getParent().postEvent(new Event(this,Event.ACTION_EVENT,"mouse out"));
        return true;
    }



    public boolean mouseMove(Event evt,int x, int y) {
        int x0=x-originX-props.marg,y0=y-originY-props.marg;
        boolean mouseOnL=mouseOnLink!=null; //Czy kursor jest w polu odnosnika

        int x0old=larea.x0,y0old=larea.y0; //Poprzednia pozycja kursora
        mouseOnLink=larea.inside(x0,y0);
        if(mouseOnL) {
            if(mouseOnLink==null) { //Kursor opuscil pole odnosnika
                getParent().postEvent(new Event(this,Event.ACTION_EVENT,"mouse out"));
                getParent().postEvent(new Event(this,Event.ACTION_EVENT,new StatusBarData("")));
            }
        } else {
            if(mouseOnLink!=null) { //Kursor wszedl w pole odnosnika
                getParent().postEvent(new Event(this,Event.ACTION_EVENT,"mouse in"));
            }
        }
        if(mouseOnLink!=null && (x0!=x0old || y0!=y0old)) { //Jezeli kursor sie ruszyl (np. po porzednim nacisnieciu) i jest nad odnosnikiem
            String shortloc=makeLoc(mouseOnLink.getLink().getLoc());
            getParent().postEvent(new Event(this,Event.ACTION_EVENT,new StatusBarData("Shortcut to: "+shortloc)));
        }
        return true;
    }

    public boolean mouseDown(Event evt,int x, int y) {
        int x0=x-originX-props.marg,y0=y-originY-props.marg;

        if(mouseOnLink!=null) { //Kursor w polu odnosnika
            String loc=mouseOnLink.getLink().getLoc();
            loc=makeLoc(loc);
            System.out.println("link - "+loc);
            load(loc);
        }
        return true;
    }
/*
    public boolean action(Event evt, Object ob) {
        if(ob instanceof RegLinkArgs) {
            larea.add((RegLinkArgs)ob);
            return true;
        }

        if(ob instanceof AnchorArgs) {
            anchlist.add((AnchorArgs)ob);
            return true;
        }
        return super.action(evt,ob);
    }
*/
    public void setTitle(String tit) {
        title=tit;
        getParent().postEvent(new Event(this,Event.ACTION_EVENT,new TitleData(title)));
        System.out.println("Title - "+title);//!!!
    }

    public void load(String loc) {
        getParent().postEvent(new Event(this,Event.ACTION_EVENT,
                        new StatusBarData("Contacting: "+loc)));

        loc=loc.replace('\\','/');

       // System.out.println("LOC="+loc);
        if(loc==null) return;

        getParent().postEvent(new Event(this,Event.ACTION_EVENT,"start waiting"));
        URL url=null;


        loc=loc.trim();

        String docLoctmp=null,docFiletmp=null,docServtmp=null;

        //Poniewaz plik zostal zaladowany, nalezy uaktualnic pola docLoc i docFile
        //System.out.println("url - "+url); //!!!


        boolean reflink=false;
        boolean loaderror=false;
        int l0=loc.lastIndexOf("#");

        if(l0!=-1) {
            docRef=loc.substring(l0);
            if(docRef!=null && docRef.equals(loc)) reflink=true;//tylko wewnatrzdokumentowuy odsylacz
            else loc=loc.substring(0,l0);//Odrzucenie wewnatrzdokumentowego odsylacza
        } else docRef=null;
        if(!reflink) {
            try {
                url=new URL(loc);
            } catch (Exception e) {
                //System.out.println("no URL - "+loc);//!!!!
                getParent().postEvent(new Event(this,Event.ACTION_EVENT,
                        new StatusBarData("Error in URL: "+loc)));
                loaderror=true;
            }
        }

        String loc1=new String(url==null ? loc : url.toString());

        l0=loc1.lastIndexOf('/');

        if(l0!=-1 && l0<loc1.length()) {
        //Podano plik - sa znaki za ostatnim '/' (odrzucenie nazwy pliku daje aktualny katalog)
            docLoctmp=loc1.substring(0,l0+1);
            docFiletmp=loc1.substring(l0+1);
            l0=docFiletmp.lastIndexOf("#");
            if(l0!=-1) docFiletmp=docFiletmp.substring(0,l0);
        } else {
            docLoctmp=loc1; //Brak podanego pliku
            docFiletmp=null;
        }
        //fileprot="file".equals(url.getProtocol());

        if(docLoctmp!=null) {

            l0=docLoctmp.indexOf(":");
            if(l0!=-1) {
                l0++;
                while(l0<docLoctmp.length() && '/'==docLoctmp.charAt(l0))l0++;
                int l1=docLoctmp.indexOf("/",l0);
                if(l1!=-1)docServtmp=docLoctmp.substring(0,l1)+"/";
            } else docServtmp=null;

        }

       // System.out.println((reflink ? "Only" : "Also")+" docRef: "+docRef);//!!!!

        String loadedStr=null;
        byte[] loadedData=null;



        boolean diffloc=(((docLoctmp!=null && !docLoctmp.equals(docLoc)) ||
           (docFiletmp!=null && !docFiletmp.equals(docFile))) && !reflink && url!=null);

        if(diffloc && !loaderror) {
            loadedData=getDoc(url);
            if(loadedData!=null) {
                Props.putFile(url.toString(),loadedData); //!!!
                loadedStr=new String(loadedData,0);
                if(loadedStr!=null) {
                    docLoc=docLoctmp;
                    docFile=docFiletmp;
                    docServ=docServtmp;

             //   System.out.println(loc); //!!!
             //   System.out.println("docLoc = "+docLoc); //!!!
             //   System.out.println("docFile= "+docFile);//!!!
             //   System.out.println("docServ= "+docServ);//!!!

                    format(loadedStr);
                    originX=originY=0;

                    init();
                } else loaderror=true;
            } else loaderror=true;
        }



        if((docRef!=null || !diffloc) && !loaderror) {
            int pos=-1;
            if(docRef!=null) {
            pos=anchlist.namePos(docRef.substring(1)); //Bez znaku '#'
          //  System.out.println("Anchor pos="+pos);//!!!
            } else if(!diffloc) pos=0; //Ten sam plik bez odnosnika, wiec pos=0
            if(pos!=-1) {
                originY=-pos-props.marg+size().height/10;
                if(originY>-props.marg) originY=-props.marg;//Troche wiekszy margines (10% ekranu)
                getParent().postEvent(new Event(this,Event.ACTION_EVENT,"canv initialized"));
            }
        }

        if((loadedStr!=null || docRef!=null || !diffloc) && !loaderror) {

            //Poinformowanie rodzica o kolejnym dokumencie
            getParent().postEvent(new Event(this,Event.ACTION_EVENT,
                        new RegLocData(docLoc+docFile+(docRef==null ? "" : docRef))));
            repaint();
        }

        getParent().postEvent(new Event(this,Event.ACTION_EVENT,"stop waiting"));
    }

    private byte[] getDoc(URL url) {
        if(url==null) return null;

        initflag=false;//Spowoduje to, ze ladowanie obrazkow dotad nie zaladowanych zostanie przerwane

        byte[] b=null;
        if(Props.isCached(url.toString())) {
            b=Props.getFile(url.toString());
            if(b!=null) {
                imglist.clear();
                return b;
            }
        }

        DataInputStream in=null;
        try {
            in=new DataInputStream(url.openStream());
        } catch (Exception e) {
            getParent().postEvent(new Event(this,Event.ACTION_EVENT,
                        new StatusBarData("Error opening: "+url.toString())));
            return null;
        }

        if(in==null) return null;

        getParent().postEvent(new Event(this,Event.ACTION_EVENT,
                    new StatusBarData("Loading: "+url.toString())));


        LoaderThread loader=new LoaderThread(in);
        int count=0;
        int lastcount=-1;

        loader.start();
        while(!loader.loadedAll()) {
            count=loader.getLength();
            if(count>lastcount) {
                System.out.println("loaded so far: "+count);
                lastcount=count;
            }
            try { Thread.sleep(10); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
        }
        loader.interrupt();

        b=loader.getBytes();

        if(b==null) {
            getParent().postEvent(new Event(this,Event.ACTION_EVENT,
                        new StatusBarData("Error loading: "+url.toString())));
            return null;
        }

        getParent().postEvent(new Event(this,Event.ACTION_EVENT,
                    new StatusBarData("Loaded: "+url.toString())));

        return b;
    }

    private void format(String str) {
        formatflag=false;
       // if(initthread!=null) initthread.stop();

        removeAll();
        imglist.clear();

        //"Formatting..." na linii statusu
        getParent().postEvent(new Event(this,Event.ACTION_EVENT,
                    new StatusBarData("Formatting: "+docLoc+(docFile!=null ? docFile : ""))));

        doc.format(str);
        setTitle(doc.getTitle());

        //"Done." na linii statusu
        getParent().postEvent(new Event(this,Event.ACTION_EVENT,
                    new StatusBarData("Done.")));
        imglist.startLoading();
        formatflag=true;

    }

    public void init() {

        int w=size().width-2*Props.marg;
        int maxw=w;

        larea.clear();
        anchlist.clear();

        Elem ep=doc.getElem();
        while(ep!=null) { //Inicjowanie elementow w celu ustalenia ich rozmiarow
            ep.init(w);
            ep=ep.gn();
        }

        int y=0;
        ElemLine line;
        ep=doc.getElem();

        while(ep!=null) {
            line=new ElemLine(ep,w,0,y);
            y+=line.getHeight();
            //if(line.getMaxWidth()>maxw)maxw=line.getMaxWidth();
            if(line.getWidth()>maxw)maxw=line.getWidth();
            ep=line.getEnd();
        }
        ep=doc.getElem();

        while(ep!=null) {
            ep.registerLink();
            ep=ep.gn();
        }

        docHeight=y+2*props.marg;
        docWidth=maxw+2*props.marg;

        getParent().postEvent(new Event(this,Event.ACTION_EVENT,"canv initialized"));
        initflag=true;
    }

   /* public synchronized void init() {
        if(!formatflag) return;
        if(initthread!=null) {
            initreq=true;
           // initStack.push(new Object());
            return;
        }

      //  if(initthread!=null) initthread.interrupt();
        initflag=false;

        if(repthread!=null) repthread.interrupt();

//        initStack.removeAllElements();
        initreq=false;

        System.out.println("start init");//!!!

        int w=size().width-2*Props.marg;
 //       int maxw=w;

        larea.clear();
        anchlist.clear();

        initthread=new InitThread(w,this);
        initthread.start();
        //while(!initthread.end);
    }

    public void initend(int docw,int doch) {
       // initthread.interrupt();

        System.out.println("end init0");//!!!

      //  initthread.stop();
        docHeight=doch+2*props.marg;
        docWidth=docw+2*props.marg;




        System.out.println("end init");//!!!
        initthread=null;

        //layout();
        //if(!initStack.empty()) {

        if(initreq) {
           // initthread=null;

            init();
        }
        else {
            initflag=true;
            repaint();


        }
        getParent().postEvent(new Event(this,Event.ACTION_EVENT,"canv initialized"));



    }

*/
    public void paint(Graphics g) {
        if(!initflag) return;
        g=getGraphics();
        g.translate(originX+Props.marg, originY+Props.marg);
        rep(g.create());
    }

    private void rep(Graphics g) {


        //Prostokat okna dokumentu
        Rectangle boundrect=bounds();
        //Powiekszenie obszaru o 5% z kazdej strony (na wszelki wypadek)
        boundrect.grow(boundrect.width/20,boundrect.height/20);
        //Przesuniecie obszaru w odpowiednie miejsce
        boundrect.translate(-originX, -originY);

        Elem ep=doc.getElem();
        while(ep!=null) {
            ep.initComp(boundrect); //Inicjalizacja elementow kontrolnych
            ep=ep.gn();
        }

        if(repthread!=null) repthread.interrupt();
      //  g.setColor(Color.green);
        g.clearRect(boundrect.x,boundrect.y,boundrect.width,boundrect.height);
      //  g.setColor(Color.black);

        repthread=new RepaintThread(doc,g,boundrect);
        repthread.start();
    }

    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
        if(!initflag) return false;
//try{
//    Thread.currentThread().sleep(200);//!!!
//}catch(Exception e) {e.printStackTrace(); System.out.println("-- sleep (imageUpdate)");}

//int chk=infoflags;
//        System.out.println(
//           (((chk & ImageObserver.ABORT)==ImageObserver.ABORT) ? "ABORT " : "")+
//           (((chk & ImageObserver.ERROR)==ImageObserver.ERROR) ? "ERROR " : "")+
//           (((chk & ImageObserver.ALLBITS)==ImageObserver.ALLBITS) ? "ALLBITS " : "")+
//           (((chk & ImageObserver.FRAMEBITS)==ImageObserver.FRAMEBITS) ? "FRAMEBITS " : "")+
//           (((chk & ImageObserver.SOMEBITS)==ImageObserver.SOMEBITS) ? "SOMEBITS " : "")+
//           (((chk & ImageObserver.PROPERTIES)==ImageObserver.PROPERTIES) ? "PROPERTIES " : "")+
//           (((chk & ImageObserver.HEIGHT)==ImageObserver.HEIGHT) ? "HEIGHT " : "")+
//           (((chk & ImageObserver.WIDTH)==ImageObserver.WIDTH) ? "WIDTH " : ""));


        //Blad, koniec ladowania obrazka
        if(initflag && (infoflags & ABORT)>0) {
            return false;
        }

        //Zmiana wymiarow obrazka, trzeba caly dokument zainicjalizowac
        if(initflag && (infoflags & (WIDTH | HEIGHT))>0 && width>0 && height>0) {
            getParent().postEvent(new Event(this, Event.ACTION_EVENT,"update from canv"));
            return true;
        }
//try{

        //Kolejna czesc obrazka wczytana, trzeba go uaktualnic
       // if(initflag && ((infoflags & SOMEBITS)>0 || (infoflags & ALLBITS)>0)) {
            Graphics g=getGraphics();
            g.translate(originX+props.marg, originY+props.marg);
            imglist.redraw(g,img);
            if((infoflags & ALLBITS)>0) return false;
            return true;
       // }

//} catch(Exception e) {e.printStackTrace(); System.out.println("-- imageUpdate");}
        //Obrazek caly zaladowany, koniec "obserwacji"
      //  if(initflag && (infoflags & ALLBITS)>0) {
      //      return false;
      //  }


      //  return true;
    }

    public String makeLoc(String loc) {
        //System.out.println("--- "+docLoc+" + "+loc);
        if(loc!=null) {
            loc=loc.replace('\\','/');
            if(loc.startsWith("http:") || loc.startsWith("ftp:") ||
               loc.startsWith("mailto:") || loc.startsWith("gopher:") ||
               loc.startsWith("news:") || loc.startsWith("#")) {
                return loc;
            }
            String base=new String(docLoc);
            base=base.trim();
            if(base.endsWith("/")) base=base.substring(0,base.length()-1);
            if(loc.startsWith("/")) { //"/" na poczatku oznacza odnosnik wykorzystujacy jako baze katalog glowny serwera
                return docServ+loc.substring(1); //Ominiecie pierwszego '/'
            }

            while(loc.startsWith("../")) {
                loc=loc.substring(3);
                int l0=base.lastIndexOf("/");
                if(l0==-1) base=new String("");
                else base=base.substring(0,l0);
            }
            return base+"/"+loc;
        }
        return null;
    }
}


/*
class InitThread extends Thread {
    int prefw=-1;
    HTMLDoc doc=null;
    mCanvas canv=null;
    int y=0,maxw=0;
    boolean end=false;

    InitThread(int pw, mCanvas c) {// HTMLDoc ndoc) {
        canv=c;
        doc=canv.doc;
        prefw=pw;
    }

    public void run() {
try{
        maxw=prefw;
        y=0;

        Elem ep=doc.getElem();
        while(ep!=null) { //Inicjowanie elementow w celu ustalenia ich rozmiarow
            ep.init(prefw);
            ep=ep.gn();
        }


        ElemLine line;
        ep=doc.getElem();

        while(ep!=null) {
            line=new ElemLine(ep,prefw,0,y);
            y+=line.getHeight();
            //if(line.getMaxWidth()>maxw)maxw=line.getMaxWidth();
            if(line.getWidth()>maxw)maxw=line.getWidth();
            ep=line.getEnd();
        }
        ep=doc.getElem();

        while(ep!=null) {
            ep.registerLink();
            ep=ep.gn();
        }
        System.out.println("end run0");//!!!
        canv.initend(maxw,y);
        System.out.println("end run");//!!!
}catch(Exception e) {e.printStackTrace();}
    }
}*/