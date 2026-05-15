import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class Props extends Object {
    private static Hashtable fonts=new Hashtable();
    private static Hashtable signs=new Hashtable();
    private static Hashtable files=new Hashtable();
    private static int filenum=0;
    private static File cachedir=new File(System.getProperty("user.dir")+"/cache/");
    mCanvas owner=null;
    static int marg=10;

    Props(mCanvas own) {
        owner=own;
        if(fonts.isEmpty()) {
            fonts.put(new Integer(Elem.NORMAL),new Font("TimesRoman",Font.PLAIN,13));
            fonts.put(new Integer(Elem.H1),new Font("Helvetica",Font.BOLD,25));
            fonts.put(new Integer(Elem.H2),new Font("Helvetica",Font.BOLD,22));
            fonts.put(new Integer(Elem.H3),new Font("Helvetica",Font.BOLD,19));
            fonts.put(new Integer(Elem.H4),new Font("Helvetica",Font.BOLD,17));
            fonts.put(new Integer(Elem.H5),new Font("Helvetica",Font.BOLD,15));
            fonts.put(new Integer(Elem.H6),new Font("Helvetica",Font.BOLD,14));
            fonts.put(new Integer(Elem.TABHEADER),new Font("TimesRoman",Font.BOLD,13));
            fonts.put(new Integer(Elem.PRE),new Font("Courier",Font.PLAIN,12));
            fonts.put(new Integer(Elem.INPUT),new Font("DialogInput",Font.PLAIN,12));
        }
        if(signs.isEmpty()) {
            signs.put(new String("nbsp"),new Character((char)32));
            signs.put(new String("quot"),new Character((char)34));
            signs.put(new String("amp"),new Character((char)38));
            signs.put(new String("lt"),new Character((char)60));
            signs.put(new String("gt"),new Character((char)62));
            signs.put(new String("copy"),new Character((char)169));
            signs.put(new String("Agrave"),new Character((char)192));
            signs.put(new String("Aacute"),new Character((char)193));
            signs.put(new String("Acirc"),new Character((char)194));
            signs.put(new String("Atilde"),new Character((char)195));
            signs.put(new String("Auml"),new Character((char)196));
            signs.put(new String("Aring"),new Character((char)197));
            signs.put(new String("AElig"),new Character((char)198));
            signs.put(new String("Ccedil"),new Character((char)199));
            signs.put(new String("Egrave"),new Character((char)200));
            signs.put(new String("Eacute"),new Character((char)201));
            signs.put(new String("Ecirc"),new Character((char)202));
            signs.put(new String("Euml"),new Character((char)203));
            signs.put(new String("Igrave"),new Character((char)204));
            signs.put(new String("Iacute"),new Character((char)205));
            signs.put(new String("Icirc"),new Character((char)206));
            signs.put(new String("Iuml"),new Character((char)207));
            signs.put(new String("ETH"),new Character((char)208));
            signs.put(new String("Ntilde"),new Character((char)209));
            signs.put(new String("Ograve"),new Character((char)210));
            signs.put(new String("Oacute"),new Character((char)211));
            signs.put(new String("Ocirc"),new Character((char)212));
            signs.put(new String("Otilde"),new Character((char)213));
            signs.put(new String("Ouml"),new Character((char)214));
            signs.put(new String("Oslash"),new Character((char)216));
            signs.put(new String("Ugrave"),new Character((char)217));
            signs.put(new String("Uacute"),new Character((char)218));
            signs.put(new String("Ucirc"),new Character((char)219));
            signs.put(new String("Uuml"),new Character((char)220));
            signs.put(new String("Yacute"),new Character((char)221));
            signs.put(new String("THORN"),new Character((char)222));
            signs.put(new String("szlig"),new Character((char)223));
            signs.put(new String("agrave"),new Character((char)224));
            signs.put(new String("aacute"),new Character((char)225));
            signs.put(new String("acirc"),new Character((char)226));
            signs.put(new String("atilde"),new Character((char)227));
            signs.put(new String("auml"),new Character((char)228));
            signs.put(new String("aring"),new Character((char)229));
            signs.put(new String("aelig"),new Character((char)230));
            signs.put(new String("ccedil"),new Character((char)231));
            signs.put(new String("egrave"),new Character((char)232));
            signs.put(new String("eacute"),new Character((char)233));
            signs.put(new String("ecirc"),new Character((char)234));
            signs.put(new String("euml"),new Character((char)235));
            signs.put(new String("igrave"),new Character((char)236));
            signs.put(new String("iacute"),new Character((char)237));
            signs.put(new String("icirc"),new Character((char)238));
            signs.put(new String("iuml"),new Character((char)239));
            signs.put(new String("eth"),new Character((char)240));
            signs.put(new String("ntilde"),new Character((char)241));
            signs.put(new String("ograve"),new Character((char)242));
            signs.put(new String("oacute"),new Character((char)243));
            signs.put(new String("ocirc"),new Character((char)244));
            signs.put(new String("otilde"),new Character((char)245));
            signs.put(new String("ouml"),new Character((char)246));
            signs.put(new String("oslash"),new Character((char)248));
            signs.put(new String("ugrave"),new Character((char)249));
            signs.put(new String("uacute"),new Character((char)250));
            signs.put(new String("ucirc"),new Character((char)251));
            signs.put(new String("uuml"),new Character((char)252));
            signs.put(new String("yacute"),new Character((char)253));
            signs.put(new String("thorn"),new Character((char)254));
            signs.put(new String("yuml"),new Character((char)255));
        }

        try {
            if(!cachedir.exists()) {
                cachedir.mkdir();
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Cant create cache dir");
        }
    }

    static Font getFont(int type) {
        return (Font)fonts.get(new Integer(type));
    }

    static public String encode(String str) {
        int ind1=0,ind2=0;

        while(ind1!=-1) {
            ind1=str.indexOf("&",ind1);
          //  ind2=str.indexOf(";",ind1);
            if(ind1!=-1) {
                String sign=str.substring(ind1+1);

                if(sign!=null) {
                    if(sign.startsWith("#")) {
                        String snum=sign.substring(1);

                        ind2=0;
                        while(ind2<snum.length() && Character.isDigit(snum.charAt(ind2))) {
                            ind2++;
                        }
                        Integer num=Integer.valueOf(snum.substring(0,ind2));

                        if(num!=null) {
                            str=str.substring(0,ind1)+(char)num.intValue();
                            if(ind2<snum.length()) {
                                if(snum.charAt(ind2)==';') ind2++;
                                str=str+snum.substring(ind2);
                            }
                        }
                    } else {
                        Enumeration e=signs.keys();
                        boolean found=false;
                        String key=null;

                        while(e.hasMoreElements() && !found) {
                            key=(String)e.nextElement();

                            if(sign.startsWith(key)) {
                                found=true;
                                ind2=key.length();
                                str=str.substring(0,ind1)+((Character)signs.get(key)).charValue();
                                if(ind2<sign.length()) {
                                    if(sign.charAt(ind2)==';') ind2++;
                                    str=str+sign.substring(ind2);
                                }
                            }
                        }
                    }
                }
            ind1++;
            }
        }
        return str;
    }

    static public void putFile(String loc,byte[] data) {
        if(files.get(loc) instanceof String) return;

        String filename=null;
        try {
            filename=Integer.toString(filenum);
            filename=filename.concat(".nvc");

            FileOutputStream out=new FileOutputStream(new File(cachedir,filename));
            out.write(data);
            out.close();
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Cant write: "+cachedir.toString()+filename);
            return;
        }

        if(filename==null) return;

        files.put(new String(loc),new String(filename));
        filenum++;
    }

    static public boolean isCached(String loc) {
        Object ofile=files.get(new String(loc));
        return ofile instanceof String;
    }

    static public String getCachedName(String loc) {
        Object ofile=files.get(new String(loc));
        if(ofile instanceof String) return (new File(cachedir,(String)ofile)).toString();

        return null;
    }


    static public byte[] getFile(String loc) {
        Object ofile=files.get(new String(loc));
        byte[] b=null;
        File f=null;
        if(ofile instanceof String) {
            try{
                f=new File(cachedir,(String)ofile);
                FileInputStream in=new FileInputStream(f);
                b=new byte[(int)f.length()];
                in.read(b);
                in.close();
            } catch(Exception e) {
                e.printStackTrace();
                System.out.println("Cant read: "+f.toString());
                return null;
            }
            return b;
        }
        return null;
    }
}