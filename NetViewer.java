import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class NetViewer extends Frame {
    String[] buttonNames = {"Back","Forward","Open","Home","Stop"};
    Stack cursor=new Stack();
    Toolbar toolbar;
    docPanel doc1,doc2;
    StatusBar status;
    Panel client;
    boolean d=false;
    String dir;
    String loc=null;

    NetViewer() {
        super("NetViewer");
        setLayout(new BorderLayout());
        addNotify();

        MenuBar bar=new MenuBar();

        Menu m=new Menu("Okno &1");
        m.add("Form");
        m.add("The Java Tutorial - index");
        bar.add(m);

        m=new Menu("Okno &2");
        m.add("Form");
        m.add("The Java Tutorial - index");
        bar.add(m);

        m=new Menu("&Widok");
        m.add("100% - 0%");
        m.add("80% - 20%");
        m.add("65% - 35%");
        m.add("50% - 50%");
        m.add("35% - 65%");
        m.add("20% - 80%");
        m.add("0% - 100%");
        bar.add(m);



        setMenuBar(bar);

        add("North",toolbar=new Toolbar(buttonNames));

        status=new StatusBar();
        add("South",status);

        client=doc1=doc2=new docPanel(500,400);
        add("Center",client);
        /*client=new Panel();
        add("Center",client);

        GridBagLayout gridbaglay=new GridBagLayout();
        GridBagConstraints gridconst=new GridBagConstraints();
        client.setLayout(gridbaglay);
        gridconst.fill=GridBagConstraints.BOTH;
        gridconst.gridheight=1;
        gridconst.weighty=1.0;
        gridconst.gridwidth=1;
        int w1=500,w2=200;

        doc1=new docPanel(w1,400);
        doc2=doc1;new docPanel(w2,400);

        gridconst.weightx=(double)w1/(double)(w1+w2);
        gridbaglay.setConstraints(doc1,gridconst);
        client.add(doc1);

        gridconst.weightx=(double)w2/(double)(w1+w2);
        gridbaglay.setConstraints(doc2,gridconst);
        client.add(doc2);*/

       // move(200,200);
        pack();
        show();
        dir=System.getProperty("user.dir");
    }

    public boolean handleEvent(Event evt) {
        if (evt.id==Event.WINDOW_DESTROY) {
            dispose();
            System.exit(0);
            return true;
        }

        if (evt.id==Event.WINDOW_EXPOSE) {
            repaint();
            return false;
        }

        return super.handleEvent(evt);
    }

    public boolean action(Event evt, Object ob) {
        //Kursor w polu odnosnika
        if ("mouse in".equals(ob)) {
            cursor.push(new Integer(getCursorType()));
            setCursor(HAND_CURSOR);
            return true;
        }

        //Kursor w czasie oczekiwania
        if ("start waiting".equals(ob)) {
            cursor.push(new Integer(getCursorType()));
            setCursor(WAIT_CURSOR);
            return true;
        }

        //Przywrocenie poprzedniego kursora
        if ("mouse out".equals(ob) || "stop waiting".equals(ob)) {
            try {
                setCursor(((Integer)cursor.pop()).intValue());
            } catch (EmptyStackException e) {
                setCursor(DEFAULT_CURSOR);
            }
            return true;
        }

        //Tytul dokumentu w oknie glownym
        if (ob instanceof TitleData) {
            String t=((TitleData)ob).data;
            if(t==null || "".equals(t)) t=new String("[untitled]");
            setTitle(t+" - NetViewer");
            return true;
        }

        //Tytul dokumentu w oknie glownym
        if (ob instanceof StatusBarData) {
            String t=((StatusBarData)ob).data;
            if(t!=null) {
                status.setText(t);
            }
            return true;
        }

        // zamkniecie OpenDialog
        if (ob instanceof OpenDialogBuf) {
            OpenDialogBuf buf=(OpenDialogBuf)ob;
            if (buf.getOpen1() || buf.getOpen2()) {
                dir=buf.getDir();
                loc=buf.getLoc();
                if(buf.getOpen1()) doc1.postEvent(new Event(this,Event.ACTION_EVENT,new LoadLocData(loc)));
                else doc2.postEvent(new Event(this,Event.ACTION_EVENT,new LoadLocData(loc)));
            }
            return true;
        }

        //Open - przycisk
        if ("Open".equals(ob)) {
            new OpenDialog(this,new OpenDialogBuf(dir,loc));
            return true;
        }

        //Back - przycisk
        if ("Back".equals(ob)) {
            client.postEvent(new Event(this,Event.ACTION_EVENT,"Back"));
            return true;
        }

        //Forward - przycisk
        if ("Forward".equals(ob)) {
            client.postEvent(new Event(this,Event.ACTION_EVENT,"Forward"));
            return true;
        }

        // - przycisk
        if ("Home".equals(ob)) {
            status.setBackground(Color.red);
        status.setForeground(Color.blue);

            return true;
        }



        //Menu
        if ("Form".equals(ob)) {
            //loc=new String("file:///D:\\JAV\\NetViewer\\Html\\Form.htm");
            loc=new String("file:///C:\\Data1\\JAVA\\Html\\tree.html");

            Menu m=(Menu)((MenuItem)evt.target).getParent();
            if("Okno &1".equals(m.getLabel())) {
                doc1.postEvent(new Event(this,Event.ACTION_EVENT,new LoadLocData(loc)));
            } else {
                doc2.postEvent(new Event(this,Event.ACTION_EVENT,new LoadLocData(loc)));
            }
            return true;
        }

        if ("The Java Tutorial - index".equals(ob)) {
            loc=new String("file:///D:\\JAV\\NetViewer\\Tutorial\\index.html");

            Menu m=(Menu)((MenuItem)evt.target).getParent();
            if("Okno &1".equals(m.getLabel())) {
                doc1.postEvent(new Event(this,Event.ACTION_EVENT,new LoadLocData(loc)));
            } else {
                doc2.postEvent(new Event(this,Event.ACTION_EVENT,new LoadLocData(loc)));
            }
            return true;
        }


       /* if ("100% - 0%".equals(ob)) { setView(1); return true;}
        if ("80% - 20%".equals(ob)) { setView(0.8); return true;}
        if ("65% - 35%".equals(ob)) { setView(0.65); return true;}
        if ("50% - 50%".equals(ob)) { setView(0.5); return true;}
        if ("35% - 65%".equals(ob)) { setView(0.35); return true;}
        if ("20% - 80%".equals(ob)) { setView(0.2); return true;}
        if ("0% - 100%".equals(ob)) { setView(0); return true;}*/

        return super.action(evt,ob);
    }


  /*  public void paint(Graphics g) {
        status.repaint();
    }*/

    protected void setView(double d1) {
        GridBagLayout gblay=(GridBagLayout)client.getLayout();
        int w=client.size().width;
        int h=client.size().height;

        GridBagConstraints gridconst=gblay.getConstraints(doc1);
        doc1.resize((int)(w*d1),h);
        gridconst.weightx=(double)(w*d1/w);
        gblay.setConstraints(doc1,gridconst);

        gridconst=gblay.getConstraints(doc2);
        doc2.resize((int)(w*(1-d1)),h);
        gridconst.weightx=(double)(w*(1-d1)/w);
        gblay.setConstraints(doc2,gridconst);

        int oldw1=doc1.size().width;
        int oldw2=doc2.size().width;

        doc1.layout();
        doc2.layout();
        client.layout();
            //Jezeli po powyzszych layout() rozmiary sie roznia, to trzeba je powtorzyc
        if(oldw1!=doc1.size().width || oldw2!=doc2.size().width) {
            doc1.layout();
            doc2.layout();
            client.layout();
            //Teraz juz na pewno jest dobrze
        }
    }

    public void layout() {
        super.layout();
        if (status != null) status.repaint();
    }

    static public void main(String[] args) {
        try {
            new NetViewer();
        } catch(Exception e) {
            e.printStackTrace();
        }


    }
}



/*
class FramesetPanel extends Panel {
    //Panel[] frames=new Panel[0];
    Panel fr1=null,fr2=null;
    int num=1;

    FramesetPanel(int width,int height) {
        //frames=new Panel[1];
        fr1=new docPanel(width,height);
    }

    void split(double ratio) {
        fr1=new FramesetPanel;
        fr2
    }
}
*/

