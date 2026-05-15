import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.net.*;
import java.io.*;

class ImgElem extends TagElem {
    ImageObserver imgobs=null;
    Props props=null;
    int width=-1,height=-1;
    int imgAsc=0;
    TextElem altText=null;
    boolean altactive=false;
    boolean dimset=false;
    String loc=null;
   // ImgLoaderThread loader=null;

    ImgElem(Tag tag, Props pr) {
        super(tag,false);

        props=pr;

        imgobs=props.owner;

        Hashtable par=tag.getParams();
        Object ow=par.get("WIDTH");
        Object oh=par.get("HEIGHT");
        Object osrc=par.get("SRC");
        Object oalt=par.get("ALT");

        int imgw=-1,imgh=-1;
        if(ow!=null && ow instanceof Integer) imgw=((Integer)ow).intValue();
        if(oh!=null && oh instanceof Integer) imgh=((Integer)oh).intValue();
        if(osrc instanceof String) loc=(String)osrc;
        loc=props.owner.makeLoc(loc);
      //  System.out.println("loc="+loc);//!!!

        if(oalt instanceof String) {
            altactive=true;
            altText=new TextElem(NORMAL,Props.encode((String)oalt),props);
            //System.out.println("alt="+(String)altText.ge());//!!!
        }
        dimset=(imgw!=-1) && (imgh!=-1);
        if(dimset) {
            width=imgw;
            height=imgh;
        }

    }

    public void init0() {
        props.owner.imglist.add(this);//new ImgElemData(,imglist);
        if(altText!=null && !dimset) {//(width==-1 || height==-1)) { //Gdyby wymiary nie zostaly ustalone
            width=altText.getWidth()+4; //4-margines
            height=altText.getHeight()+4;
            if(width<15) width=15;
            if(height<15) height=15;
        }

        if(width==-1 || height==-1) { //Gdyby jeszcze wymiary nie byly ustalone...
            width=15; //15-wielkosc nie istniejacego obrazka
            height=15;
        }
    }

    public void startLoading() {
        URL url=null;
        try {
            url=new URL(loc);
        } catch(Exception e) {}

        if(url==null) return;

        Image img=props.owner.getToolkit().getImage(url);
        props.owner.prepareImage(img,props.owner);
        se(img);

        if(!dimset) {
            int imgw=img.getWidth(imgobs);
            if(imgw!=-1) width=imgw;
            int imgh=img.getHeight(imgobs);
            if(imgh!=-1) height=imgh;
        }
    }

    public void init(int pw) { //preffered width
        if(!(ge() instanceof Image)) return;

        if(!dimset) {
            int imgw=((Image)ge()).getWidth(imgobs);
            if(imgw!=-1) width=imgw;
            int imgh=((Image)ge()).getHeight(imgobs);
            if(imgh!=-1) height=imgh;
        }
        imgAsc=-1;
    }

    public int gWidth() {
        return width;
    }

    public int gHeight() {
        return height;
    }

    public int gAsc() {

        if(valign==MIDDLE) imgAsc=getHeight()/2;
        else if(valign==TOP) imgAsc= imgAsc==-1 ? 0 : imgAsc;
        else /*if (valign==BOTTOM || valign==DEFAULT)*/ imgAsc=height;//BOTTOM domyslnie
        return imgAsc;
        //return height;
    }


    public int gDesc(int asc) {
        if(valign==MIDDLE) return getHeight()/2;
        if(valign==TOP) {
            imgAsc=asc;
            return getHeight()-imgAsc;
        }
       /* if(valign==BOTTOM || valign==DEFAULT) */return 0;//BOTTOM domyslnie
    }

    public void registerLink() {
        if(isLink()) {
            props.owner.larea.add(new RegLinkArgs(getBounds(),linkloc));
        }
    }

    public void setXY(int nx, int ny) {
        super.setXY(nx,ny);
        if(altactive) {
            altText.setXY(x+2,y-getAsc()+altText.getHeight()-2);
        }
    }

    public void draw(Graphics g) {
        int y0=y-getAsc();






        if(ge() instanceof Image && (props.owner.checkImage((Image)ge(),props.owner) & (ImageObserver.ERROR | ImageObserver.ABORT))==0) {// && imgw!=-1 && imgh!=-1) {//!!!!
            //if() {
               // Image imgtmp=props.owner.createImage(getWidth(),getHeight());
               // Graphics gr=imgtmp.getGraphics();
                g.setColor(props.owner.getBackground());
                g.fillRect(x,y0,getWidth(),getHeight());
                g.drawImage((Image)ge(),x,y0,getWidth(),getHeight(),imgobs);
                //g.drawImage(imgtmp,x,y0,imgobs);
               // g.setColor(Color.black);
          //  }
            //System.out.println("x="+x+" y="+y0+" w="+getWidth()+" h="+getHeight());
        } else {
            Graph.draw3DRect(g,x+1,y0+1,getWidth()-3,getHeight()-3,false);
            if(altactive) {
                Graphics gr=g.create(); //Zeby tekst nie wychodzil poza ramke
                gr.clipRect(x,y0,getWidth(),getHeight());

                altText.redraw(gr);

                g.setColor(Color.black);
            }
        }

        if(isLink()) {
                g.setColor(Color.blue);
                g.drawRect(x,y0,getWidth()-1,getHeight()-1);
                g.setColor(Color.black);
        }
    }
}
/*
class ImgLoaderThread extends LoaderThread {
    ImgElem owner=null;

    ImgLoaderThread(InputStream in, ImgElem own) {
        super(in);
        owner=own;
    }

    public void run() {
        super.run();
        try{
        sleep(2000);
        }catch(Exception e) {}
       // owner.imageLoaded(b);
    }
}
*/