import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class OpenDialog extends Dialog {
    OpenDialogBuf buf;
    TextField tloc;

    OpenDialog(Frame parent, OpenDialogBuf b) {
        super(parent,"Open Location",true);
        buf=b;

        Button bopen1=new Button("Open 1");
        Button bopen2=new Button("Open 2");
        Button bcancel=new Button("Cancel");
        Button bbrowse=new Button("Browse");
        tloc=new TextField(buf.loc);

        bopen1.reshape(50,50+20,50,20);
        bopen2.reshape(50,75+20,50,20);
        bcancel.reshape(110,50+20,50,20);
        bbrowse.reshape(200,20+20,50,20);
        tloc.reshape(10,20+20,190,20);

        setLayout(null);
        setBackground(Color.lightGray);
        setResizable(true);

        add(bopen1);
        add(bopen2);
        add(bcancel);
        add(bbrowse);
        add(tloc);

    	reshape(getParent().bounds().x+getParent().insets().right,
    	        getParent().bounds().y+getParent().insets().top,
    	        insets().left-insets().right+300,
    	        insets().top-insets().bottom+120);

    	show();
    }

    public String getDir() {
        return buf.dir;
    }

    public String getLoc() {
        return buf.loc;
    }

    public boolean handleEvent(Event evt) {
        if (evt.id==Event.WINDOW_DESTROY) {
            postEvent(new Event(this,Event.ACTION_EVENT,"Cancel"));
            return true;
        }

        return super.handleEvent(evt);
    }

    public boolean action(Event evt, Object ob) {
        if ("Open 1".equals(ob)) {
            buf.open1=true;
            buf.open2=false;
            buf.loc=tloc.getText();
            dispose();
            getParent().postEvent(new Event(this,Event.ACTION_EVENT,buf));

            return true;
        }

        if ("Open 2".equals(ob)) {
            buf.open1=false;
            buf.open2=true;
            buf.loc=tloc.getText();
            dispose();
            getParent().postEvent(new Event(this,Event.ACTION_EVENT,buf));

            return true;
        }

        if ("Cancel".equals(ob)) {
            buf.open1=buf.open2=false;
            dispose();
            getParent().postEvent(new Event(this,Event.ACTION_EVENT,buf));
            return true;
        }

        if ("Browse".equals(ob)) {
            FileDialog fd = new FileDialog((Frame)getParent(),null,FileDialog.LOAD);
            fd.setDirectory(buf.dir);
            fd.setFile("");
            //fd.setFilenameFilter(this);
            ((Frame)getParent()).disable();
            hide();
            fd.show();
            System.out.println("po fd.show()");
            if(fd.getFile()!=null && !fd.getFile().equals("binnull")) {
                buf.dir=fd.getDirectory();
                tloc.setText(new String("file:///"+buf.dir+fd.getFile()));
            }
            show();
            ((Frame)getParent()).enable();
            return true;
        }
        return false;
    }
}
