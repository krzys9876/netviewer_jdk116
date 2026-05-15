import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class Array extends Object {
    int rows=1,cols=1;
    Object[] array=null;

    Array() {
        this(1,1);
    }

    Array(int r, int c) {
        rows=r;
        cols=c;
        array=newArray(r,c);
    }

    public static Object[] newArray(int r, int c) {
        Object[] a=new Object[r*c];
        for(int i=0;i<r*c;i++) {
            a[i]=null;
        }

        return a;
    }

    public void ensure(int r, int c) {
        if(r>rows) {
            Object[] arrtmp=array;
            array=newArray(r,cols);
            System.arraycopy(arrtmp,0,array,0,arrtmp.length);
            rows=r;
        }
        if(c>cols) {
            Object[] arrtmp=array;
            array=newArray(rows,c);
            for(int i=0;i<rows;i++) {
                System.arraycopy(arrtmp,i*cols,array,i*c,cols);
            }
            cols=c;
        }
       // System.out.println("rows="+rows+" cols="+cols+" length="+array.length);//!!!
    }

    public void setElem(Object el,int r, int c) {
       // System.out.println("--r="+r+" c="+c+" length="+array.length);//!!!
//try{
        array[r*cols+c]=el;
//}catch(Exception e){System.out.println("exeption!!!! --r="+r+" c="+c+"rows="+rows+" cols="+cols+" length="+array.length);}
    }

    public int setElem(Object el,int r, int c,int rsp,int csp) {
        int i=0,j=0,ctmp=c;
        ensure(r+rsp,ctmp+csp);
        while(j<csp) {

            if(array[r*cols+ctmp+j]==null) {
                j++;
            } else {
                j=0;ctmp++;
                 ensure(r+rsp,ctmp+csp);
            }
        }

       // ensure(r+rsp,c+csp);
        for(i=0;i<rsp;i++) {
            for(j=0;j<csp;j++) {
               // ensure(r+i+1,c+j+1);
                setElem(el,r+i,ctmp+j);
            }
        }
        return ctmp;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }
}
