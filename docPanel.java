import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import java.util.*;

/*
class mPanel extends Panel {
    mCanvas canv=null;

    mPanel(int w, int h) {
        super();

        setLayout(null);

        canv=new mCanvas(w,h);
        add("Center",canv);
        canv.reshape(0,0,w,h);
        reshape(0,0,w,h);
    }
}
*/

class docPanel extends Panel {
    //Vector locvect=new Vector(0,1); //Lista kolejno przegladanych dokumentow
    Stack st1=new Stack(),st2=new Stack();
    int locnum=-1; //locnum - aktualna pozycja w locvect
    mCanvas canv=null;
    boolean versb=false,horsb=false;
    Scrollbar sbVer = new Scrollbar(Scrollbar.VERTICAL);
    Scrollbar sbHor = new Scrollbar(Scrollbar.HORIZONTAL);

    docPanel(int w, int h) {
        super();
        setLayout(new BorderLayout());
      //  mPanel p=new mPanel(w,h);
      //  canv=p.canv;
      canv=new mCanvas(w,h);
        add("Center",canv);
        sbVer.setBackground(Color.lightGray);///!!!
        sbHor.setBackground(Color.lightGray);///!!!
    }

    public boolean action(Event evt, Object ob) {
   //     if(ob instanceof StatusBarData) {
   //         getParent().action(evt,ob);
   //         return true;
   //     }


        //Nowy dokument
        if(ob instanceof LoadLocData) {
            //locvect.setSize(locnum+2); //Usuniecie elementow mogacych znajdowac sie dalej (jezeli wczesniej nastepowalo Back)
            load(((LoadLocData)ob).data);
            return true;
        }

        //Odswiezenie
        if("update from canv".equals(ob)) {
            layout();
            return true;
        }

        //Back
        if("Back".equals(ob)) {
            if(st1.size()>1) {
                Object el=st1.pop();
                st2.push(el);
                el=st1.peek();

                load((String)el);
            }
           /* if(locnum>1) {
                locnum-=1;  //-2, bo po load locnum jest zawsze inkrementowane
                load((String)locvect.elementAt(locnum-1));
            }*/
            return true;
        }

        //Forward
        if("Forward".equals(ob)) {
            if(!st2.empty()) {
                Object el=st2.pop();
                st1.push(el);
                load((String)el);
            }

            /*if(locnum-1<locvect.size()) {
                locnum++;
                load((String)locvect.elementAt(locnum-1));//po load locnum jest zawsze inkrementowane
            }*/
            return true;
        }

        //Nowy dokument zaladowany w liscie dokumentow
        if(ob instanceof RegLocData) {

            Object el=null;
            if(!st1.empty()) el=st1.peek();
            if(!(el instanceof String && ((String)el).equals((((RegLocData)ob).data)))) {
                st1.push(((RegLocData)ob).data);
                st2.removeAllElements();
            }

/*
            if(locvect.size()<=locnum) {
                locvect.setSize(locnum+1); //Zapewnienie minimalnego rozmiaru
            }


//            locvect.setElementAt(((RegLocData)ob).data,locnum);

            Object el=locvect.elementAt(locnum+1);
            if(!(el instanceof String && ((String)el).equals(((RegLocData)ob).data))) {
                locnum++;
                locvect.setElementAt(((RegLocData)ob).data,locnum);


            }
*/






            return true;
        }

        //Tytul dokumentu
        if(ob instanceof TitleData) {
            if(getParent() instanceof Frame) { //Tylko jezeli rodzic jest glownym oknem
                getParent().postEvent(evt);
            }
            return true;
        }

        //uaktualnienie rozmiarow dokumentu (wiadomosc z canv)
        if ("canv initialized".equals(ob)) {
            sbChanged();
          //  System.out.println("docHeight="+canv.docHeight);
            if(canv.docHeight<=(size().height - (horsb ? sbHor.size().height : 0)) && versb) {
                versb=false;
                remove(sbVer);
                layout();
            }
            else
            if(canv.docHeight>(size().height - (horsb ? sbHor.size().height : 0)) && !versb) {
                versb=true;
                add("East", sbVer);
                layout();

            }
            if(canv.docWidth<=(size().width - (versb ? sbVer.size().width : 0)) && horsb) {
                horsb=false;
                remove(sbHor);
                layout();

            }
            else
            if(canv.docWidth>(size().width - (versb ? sbVer.size().width : 0)) &&!horsb) {
                horsb=true;
                add("South", sbHor);
                layout();

            }
          //  sbChanged(); //Wywo³ywane za kazdym razem, choc zmiany zachodza tylko
                         //dla powyzszych przypadkow i przy otwieraniu nowego dokumentu
            return true;
        }
        return super.action(evt,ob);
    }

    public boolean handleEvent(Event evt) {
        boolean hor=true;
        // zmiana polozenia paskow przewijania
        if (evt.target == sbVer || evt.target == sbHor) {
            hor=evt.target == sbHor;
        }
        if (evt.id == Event.SCROLL_LINE_DOWN) {
            scLineDown(hor);
            return true;
        }
        if (evt.id == Event.SCROLL_LINE_UP) {
            scLineUp(hor);
            return true;
        }
        if (evt.id == Event.SCROLL_PAGE_DOWN) {
            scPageDown(hor);
            return true;
        }
        if (evt.id == Event.SCROLL_PAGE_UP) {
            scPageUp(hor);
            return true;
        }
        if (evt.id == Event.SCROLL_ABSOLUTE) {
            scAbsolute();
            return true;
        }
        return super.handleEvent(evt);
    }

    private void sbChanged() {
        int w = canv.size().width;
        int h = canv.size().height;

        sbVer.setValues(-canv.originY, h, sbVer.getMinimum(),canv.docHeight-h);
        sbVer.setPageIncrement(h/10*9);
        sbVer.setLineIncrement(h/10);

        sbHor.setValues(-canv.originX, w, sbHor.getMinimum(), canv.docWidth-w);
        sbHor.setPageIncrement(w/10*9);
        sbHor.setLineIncrement(w/10);

        canv.originX = -Math.max(0, Math.min(canv.docWidth-w,-canv.originX));
        canv.originY = -Math.max(0, Math.min(canv.docHeight-h,-canv.originY));
    }

    private void scAbsolute() {
        canv.originY = -sbVer.getValue();
        canv.originX = -sbHor.getValue();
        sbChanged();
        canv.repaint();
    }

    private void scLineDown(boolean hor) {
        if(hor) canv.originX-=sbHor.getLineIncrement();
        else canv.originY-=sbVer.getLineIncrement();
        sbChanged();
        canv.repaint();
    }

    private void scLineUp(boolean hor) {
        if(hor) canv.originX+=sbHor.getLineIncrement();
        else canv.originY+=sbVer.getLineIncrement();
        sbChanged();
        canv.repaint();
    }

    private void scPageDown(boolean hor) {
        if(hor) canv.originX-=sbHor.getPageIncrement();
        else canv.originY-=sbVer.getPageIncrement();
        sbChanged();
        canv.repaint();
    }

    private void scPageUp(boolean hor) {
        if(hor) canv.originX+=sbHor.getPageIncrement();
        else canv.originY+=sbVer.getPageIncrement();
        sbChanged();
        canv.repaint();
    }

    private void load(String loc) {
        canv.load(loc);
    }

    public void layout() {
        //remove(canv);


        super.layout();
        canv.init();
        canv.repaint();
       // canv.hide();
        sbChanged();
        //canv.repaint();


    }
}
