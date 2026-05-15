import java.util.*;

class Tag extends Object {
    String tagStr=new String();
    String name=new String();
    Hashtable params=new Hashtable();
    boolean end=false;

    public Tag(String str) {
        if(str!=null) {
            tagStr=str;
            translate();
        }
    }

    public String getName() {
        return new String(name);
    }

    public boolean ends() {
        return end;
    }

    public Hashtable getParams() {
        return (Hashtable)params.clone();
    }

    public String toString() {
        return new String(tagStr);
    }

    private void translate() {
	    int status=0;
	    String tmp;
	    Object tmpo;
	    String par=null;
	    StringTokenizer strt=new StringTokenizer(tagStr,"\t\n\r ="+'"',true);
	    while(strt.hasMoreTokens()) {
	        if((tmp=skipDelim(strt))==null) return;
	        if(status==0) {
	            name=tmp.toUpperCase();
	            if(name.startsWith("!")) {   //Komentarz
	                name="!";
	                return;
	            }
	            if(name.startsWith("/")) {   //Znacznik koncowy
	                name=name.substring(1);///!!!.toUpperCase();
	                end=true;
	            }
	            status=1;
	        } else {
	            if(status==2 && "=".equals(tmp) && strt.hasMoreTokens()) {
	                tmpo=getValue(strt);
	                if(tmpo==null) tmpo=new Object();
	                if(par!=null) {
	                    params.remove(par);
	                    params.put(par,tmpo);
	                }
	                status=1;
	            } else {        //Niezale¿nie od wart. zm. status tu musi byc parametr,
	                par=tmp.toUpperCase();    //nawet jesli oczekiwana jest wartosc
	                if(par!=null) {
	                    params.put(par,new Object());
	                }
	                status=2;
	            }
	        }
	    }
	}

	private String skipDelim(StringTokenizer strt) {
	    String tmp=null;
	    while(strt.hasMoreTokens() && (" ".equals(tmp=strt.nextToken()) || "\n".equals(tmp) || "\t".equals(tmp) ||"\r".equals(tmp)));
	    return tmp;
	}

	private Object getValue(StringTokenizer strt) {
	    String tmp;
	    Object val=null;
	    if((tmp=skipDelim(strt))==null) return null;
	    if((""+'"').equals(tmp)) {
	        val=getStr(strt);
	        tmp=(String)val; //Wartosc znacznika bez cudzyslowiow
	    }
	    try {
	        val=Integer.valueOf(tmp); //Sprawdzenie, czy wartosc jest liczba
	    } catch (NumberFormatException e) {
	        val=tmp;//!!!!.toUpperCase(); //Nie jest to liczba
	    }
	    return val;
	}

	private String getStr(StringTokenizer strt) {
	    String tmp,str=new String();
	    char[] cdz={'"'};
	    while(strt.hasMoreTokens() && !((""+'"').equals(tmp=strt.nextToken()))) {
	        if(!("\n".equals(tmp) || "\r".equals(tmp))) {
	            str=str.concat(tmp);
	        }
	    }
	    return str;
	}
}
