import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import java.util.*;

class LoaderThread extends Thread {
    InputStream in=null;
    byte[] b=null;
    volatile boolean loaded=false;

    LoaderThread(InputStream instr) {
       in=instr;
       b=new byte[0];
       loaded=false;
    }

    public synchronized void addBytes(byte[] buf,int count) {
        byte[] b1=b;
        b=new byte[b1.length+count];
        System.arraycopy(b1,0,b,0,b1.length);
        System.arraycopy(buf,0,b,b1.length,count);
    }


    public void run() {
        int res=0; //Ilosc odczytanych bajtow, koniec odczytu dla res==-1
        byte[] b1=null;     //Dane pomocnicze przy przepisywaniu
        byte[] buf=new byte[8192];  //Bufor
        try {
            while(res!=-1 && !isInterrupted()) {
                res=in.read(buf,0,buf.length);
                if(res>0) {
                    addBytes(buf,res);
                }
                //sleep(500);//!!!
            }
            in.close();
        } catch (Exception e) {
            b=null;
        }
        loaded=true;
    }

    public byte[] getBytes() {
        return b;
    }

    public int getLength() {
        return b.length;
    }

    public boolean loadedAll() {
        return loaded;
    }
}
