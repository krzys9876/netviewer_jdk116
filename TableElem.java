import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class TableElem extends TagElem {
    int height=0,width=0,cols=0,rows=0;
    int abswidth=-1,relwidth=-1;
    int spacing=2,padding=2,border=0; //Wartosci domyslne
    int defalign=LEFT;
    Elem eltab=null;
    TabDatElem[] cell=null;

    TableElem(Tag tag) {
        super(tag,true);

        Hashtable par=tag.getParams();
        Object cellsp=par.get("CELLSPACING");
        Object cellpa=par.get("CELLPADDING");
        Object bord=par.get("BORDER");
        Object wid=par.get("WIDTH");

        if(cellsp instanceof Integer) spacing+=((Integer)cellsp).intValue();
        if(cellpa instanceof Integer) padding+=((Integer)cellpa).intValue();

        if(bord instanceof Integer) border=((Integer)bord).intValue();
        else if(bord!=null) border=1;
        if(wid!=null) {
            if(wid instanceof String) {
                String widstr=(String)wid;
                if(widstr.endsWith("%")) {
                    widstr=widstr.substring(0,widstr.lastIndexOf("%"));
                    relwidth=Integer.valueOf(widstr).intValue();
                }
            } else
            if(wid instanceof Integer) {
                abswidth=((Integer)wid).intValue();
            }
        }
       // border=1;//!!!
    }

    public void init0() {
        if(ends) return;

        Elem etmp=gn();
        Elem etmp1=null;

        while(etmp!=null && etmp.getType()!=TABLE+ENDS) {
            etmp.init0(); //Inicjacja elementow zagniezdzonych
            if(etmp.getAlign()==DEFAULT ) { //Ustalenie wyrownania
                if(getAlign()!=DEFAULT) etmp.setAlign(getAlign());//etmp.setAlign(defalign);
                //else etmp.setAlign(getAlign());
            }

            etmp1=etmp;
            etmp=etmp.gn();

        }
        eltab=gn();

        //Wprowadzenie elementu TEXT zamiast TABLE+ENDS pozwala zaczac nowa linie za tabela
        Elem etmp2=new Elem(NOTHING,true); //Pusty element od nowej linii
        etmp2.sn(etmp==null ? null : etmp.gn());
        sn(etmp!=null ? etmp2 : null); //Element TABLE+ENDS zostaje ominiety

        if(etmp1!=null) etmp1.sn(null); //Na koncu listy elementow tabeli jest null

        count();

    }

    public synchronized void init(int pw) {
        if(ends) return;

        if(cols==0) cols=1;



        if(abswidth>0 && pw>abswidth) pw=abswidth; //abswidth podaje szerokosc w pixelach
        else if(relwidth>0) pw=pw*relwidth/100; //relwidth podaje szerokosc w procentach
        TabDatElem ep=null;
        ElemLine line=null;

        int prefw=pw-(cols+1)*spacing-2*border; //Uwzglednienie odstepow miedzy komorkami
        if(prefw<1)prefw=1;



        int maxw=0,space=prefw;

        //System.out.println("prefw="+prefw+" space="+space);//!!!!
       // System.out.println("cols="+cols);

        int colprefw=space/cols;
        int x0=0,y0=0,minw=0,minwold=0;
        int r=0,c=0,i=0,j=0;
        int[] colw=new int[cols];
        for(i=0;i<cols;i++) colw[i]=0;
        boolean[] colfl=new boolean[cols]; //Mowi, czy dana kolumna ma juz ustalona szerokosc i czy nalezy ja inicjalizowac
        for(i=0;i<cols;i++) colfl[i]=true;
        int[] rowh=new int[rows];
        for(i=0;i<rows;i++) rowh[i]=0;

        //Inicjalizacja polozenia (wszystkie elementy zainicjalizowane
        //sa wzgledem punktu (0,0) a potem tylko przesuwane
        setXY(0,0);

        //Ustalenie szerokosci kolumn
        minw=-1;


        for(i=0;i<cols;i++) {
//try{
            colprefw=space/(cols-i);
          //  System.out.println("cols-i="+(cols-i));
//}catch(Exception e) {e.printStackTrace();}
            if(colprefw<1) colprefw=1;
            for(c=0;c<cols;c++) {
                if(colfl[c]) {
                    for(r=0;r<rows;r++) {
                        ep=cell[c+r*cols];
                        if(ep!=null) {
                            ep.setWidth(0);
                            ep.init(colprefw);
                            if(ep.getWidth()>colw[c]) colw[c]=ep.getWidth();
                        }
                    }
                }
            }
            //Znajdowanie szerokosci kolejnej najwezszej kolumny, szerszej od minw
            //Jesli istnieje kolumna tak samo szeroka jak poprzednia najwezsza,
            //mowi o tym flaga kolumny, ktora pozwoli ja znowu zainicjalizowac
            minwold=minw;
            for(j=0;j<cols;j++) { //Najpierw kolumna najszersza
                if(colw[j]>minw)minw=colw[j];
            }
            for(j=0;j<cols;j++) { //Potem kolejna najwezsza
                if(minwold<=colw[j] && colw[j]<=minw && colfl[j])minw=colw[j];
            }
            for(j=0;j<cols;j++) { //Zerowanie flagi kolejnej najwezszej kolumny
                if(colw[j]==minw && colfl[j]) {
                    colfl[j]=false;
                    break;
                }
            }
            //Obliczanie wolnego miejsca, czyli szerokosci kolumn jeszcze nie do konca zainicjalizowanych
            space=prefw;
            for(j=0;j<cols;j++) {
                if(colw[j]>colprefw)space-=colw[j]-colprefw; //Kazde przekroczenie szerokosci powoduje zmniejszenie ilosci wolnego miejsca
                if(!colfl[j])space-=colw[j]; //Odjecie szerokosci juz zainicjalizowanych kolumn
            }
        }






        //Rozszerzenie tabeli do zadanej szerokosci (jesli potrzebne)
        if(space>0 && (abswidth>0 || relwidth>0)) {
            int tabwid=0,dcolw=0;
            for(c=0;c<cols;c++) tabwid+=colw[c]; //Ustalenie szerokosci aktualnej

            if(tabwid>0) {
                for(c=0;c<cols;c++) {
                    dcolw=space*colw[c]/tabwid;
                    colw[c]+=dcolw; //Proporcjonalne rozszerzenie komorki
                }
            }
        }

        //Ustalenie wysokosci wierszy i polozenia komorek
        y0=spacing;
        TabDatElem epc=null,epr=null;

        for(r=0;r<rows;r++) {
            x0=spacing;
            for(c=0;c<cols;c++) {
                ep=cell[c+r*cols];
                if(ep!=null) {
                    ep.setWidth(0);
                }
            }
            epc=epr=null;
            for(c=0;c<cols;c++) { //Inicjalizacja z podaniem zadanej szerokosci
                ep=cell[c+r*cols];
                epc=c>0 ? cell[c-1+r*cols] : null;
                epr=r>0 ? cell[c+(r-1)*cols] : null;
                if(ep!=null) {
                    ep.setWidth(ep.getWidth()+colw[c]);
                    ep.setHeight(0);
                    ep.init(-1);
                    if(ep!=epc && ep!=epr) {
                        ep.setXY(0,0);
                        ep.translate(x0,y0); //Jesli poprzednia komorka nie jest ta sama
                    } else
                    if(ep==epc) {
                        ep.setWidth(ep.getWidth()+spacing);
                        ep.init(-1);
                    }
                    if(colw[c]>0) {
                        x0+=colw[c]+spacing; //Przesuniecie w prawo do nastepnej kolumny
                    }
                }
            }

            for(c=0;c<cols;c++) { //Ustalenie wysokosci wiersza (nie moze zawierac sie
                                //w poprzedniej petli, bo komorki z colsp>1 rozszerzaja sie
                ep=cell[c+r*cols];
                if(ep!=null && ep.getHeight()>rowh[r]) rowh[r]=ep.getHeight();
            }
            if(rowh[r]>0) {
                y0+=rowh[r]+spacing; //Przesuniecie w dol do nastepnego wiersza
            }
        }

        //Przypisanie wysokosci wierszy do poszczegolnych komorek
        for(c=0;c<cols;c++) {
            for(r=0;r<rows;r++) { //Zerowanie wysokosci komorek (musi byc w osobnej petli)
                ep=cell[c+r*cols];
                if(ep!=null) {
                    ep.setHeight(0);
                }
            }
            for(r=0;r<rows;r++) { //Dodawanie wysokosci - poniewaz dla komorki z rowsp>1 nalezy uwzglednic wykokosc kilku wierszy
                ep=cell[c+r*cols];
                if(ep!=null) {
                    ep.setHeight(ep.getHeight()+rowh[r]);
                    if(r>0 && ep==cell[c+(r-1)*cols]) {
                        ep.setHeight(ep.getHeight()+spacing);
                    }

                }
            }
        }

        //Wyrownanie pionowe komorek
        int asc=0;

        for(r=0;r<rows;r++) {
            ep=cell[r*cols];
            if(ep!=null) { //Pierwsza kolumna wyrownywana inaczej ze wzgledu na wyrownanie BASELINE
                epr=r>0 ? cell[(r-1)*cols] : null;
                if(ep!=epr) {
                    ep.inity(0);
                    asc=ep.getInAsc();
                }
            }
            else asc=0;
            for(c=1;c<cols;c++) {
                ep=cell[c+r*cols];
                if(ep!=null) {
                    epc=cell[c-1+r*cols];
                    epr=r>0 ? cell[c+(r-1)*cols] : null;
                    if(ep!=epc && ep!=epr) {
                        ep.inity(asc);
                    }
                }
            }
        }

        //Ustalenie ostatecznych rozmiarow tabeli
        height=spacing;
        for(r=0;r<rows;r++) if(rowh[r]>0) height+=rowh[r]+spacing;
        width=spacing;
        for(c=0;c<cols;c++) if(colw[c]>0) width+=colw[c]+spacing;


//if(abswidth>0) System.out.println("abswidth="+abswidth+" width="+width);//!!!!

    }

    public void registerLink() {
        if(ends) return;

        Elem ep=eltab;
        while(ep!=null) {
            ep.registerLink();
            ep=ep.gn();
        }
    }

    public void setXY(int nx,int ny) {
        if(ends) return;

        int xtmp=x; //Zapamietanie starych wspolrzednych
        int ytmp=y;
        super.setXY(nx,ny);
        Elem ep=eltab;
        while(ep!=null) {
            //Przesuniecie wszystkich podelementow o roznice wspolrzednych
            ep.translate(nx-xtmp,ny-ytmp);
            ep=ep.gn();
        }
    }

    public void initComp(Rectangle rect) {//Dla elementow kontrolnych
        Elem ep=eltab;
        while(ep!=null) {
            ep.initComp(rect);
            ep=ep.gn();
        }
    }

    public void checkEnable(Rectangle rect) {
        if(ends) {
            enabled=false;
            return;
        }

        super.checkEnable(rect);

        Elem ep=eltab;
        while(ep!=null) {
            ep.checkEnable(rect);
            ep=ep.gn();
        }
    }

    public void draw(Graphics g) {
        if(ends) return;

        TabDatElem ep=null;
        for(int r=0;r<rows;r++) {
            for(int c=0;c<cols;c++) {
                ep=cell[c+r*cols];
                if(ep!=null) {
                    ep.redraw(g);
                }
            }
        }
        if(border>0) {
            Graph.draw3DRect(g,x,y,getWidth(),getHeight(),true,border);

            /*g.setColor(Color.white);
            g.drawLine(x,y,x,y+getHeight());
            g.drawLine(x,y,x+getWidth(),y);
            g.setColor(Color.darkGray);
            g.drawLine(x+getWidth(),y,x+getWidth(),y+getHeight());
            g.drawLine(x,y+getHeight(),x+getWidth(),y+getHeight());*/

            g.setColor(Color.black);
        }
    }

    public int gAsc() {
        return 0;
    }

    public int gDesc(int asc) {
        return ends ? 0 : height;
    }

    public int gHeight() {
        return ends ? 0 : height;
    }

    public int gWidth() {
        return ends ? 0 : width;
    }

    private void count() {
        if(ends) return;

        Elem ep=eltab;//Ustalenie liczby kolumn i wierszy
        int coltmp=0,rowtmp=0,i=0,r=-1,c=-1;
        cols=rows=0;
        int rowalign=DEFAULT;
        int rowvalign=DEFAULT;

        Array a=new Array();
        boolean row=false; //true miedzy <tr>..</tr>

        while(ep!=null) {
            if(ep.getType()==TABROW+ENDS) {
                row=false;
            } else
            if(ep.getType()==TABROW || (ep.getType()==TABDAT && !row)) {
                row=true;
                r++;
                c=-1;
                rowalign=ep.getAlign();
                rowvalign=ep.valign;
            }
            if(ep.getType()==TABDAT) {
                c++;

                //Wymuszenie wyrownania w calym wierszu
                if(rowalign!=DEFAULT && ep.getAlign()==DEFAULT)ep.setAlign(rowalign);
                if(rowvalign!=DEFAULT && ep.getValign()==DEFAULT)ep.setValign(rowvalign);

                TabDatElem tdel=(TabDatElem)ep;
                tdel.padding=padding; //Przekazanie parametrow do komorek
                tdel.border=border;

//                a.ensure(r+1,c+1);
                c=a.setElem(ep,r,c,((TabDatElem)ep).rowsp,((TabDatElem)ep).colsp);
            }
            ep=ep.gn();
        }

     //   System.out.println("r="+r+" c="+c);

        cell=new TabDatElem[a.getRows()*a.getCols()];
        System.arraycopy(a.array,0,cell,0,a.array.length);
        cols=a.getCols();
        rows=a.getRows();




       /* while(ep!=null) {
            if(ep.getType()==TABROW) {
                if(coltmp>cols)cols=coltmp;
                coltmp=0;
                rows++;
                rowtmp=1;
            } else
            if(ep.getType()==TABDAT) {
                int csp=((TabDatElem)ep).colsp;
                int rsp=((TabDatElem)ep).rowsp;
                if(rsp>rowtmp)rowtmp=rsp;
                coltmp+=csp;
            }
            ep=ep.gn();
        }
        if(coltmp>cols)cols=coltmp; // W przypadku, gdy ostatni wiersz byl dluzszy
        rows+=rowtmp-1; //Dodanie maksymalnej wartosci rowsp z komorek ostatniego wiersza
                        //(-1 bo dodajemy tylko komorki wykraczajace ponizej wiersza)
        if(rows==0) rows=1;//Sytuacja bledna, nie bylo znacznikow TR
        if(cols==0) cols=1;//Sytuacja bledna, nie bylo znacznikow TD

        int i=0,j=0;

        cell=new TabDatElem[cols*rows];
        for(i=0;i<cols*rows;i++)cell[i]=null;
        int c=0,r=-1;
        int rowalign=DEFAULT;
        int rowvalign=DEFAULT;
        ep=eltab;
        while(ep!=null) {
            if(ep.getType()==TABROW) {
                r++;
                c=0;
             //   rowalign=ep.getAlign();
             //   rowvalign=ep.valign;
            } else
            if(ep.getType()==TABDAT) {
                if(r==-1) r=0;//Sytuacje bledne, TR powinno poprzedzac pierwszy wiersz
              //  if(rowalign!=DEFAULT && ep.getAlign()==DEFAULT)ep.setAlign(rowalign); //Wymuszenie wyrownania w calym wierszu
              //  if(rowvalign!=DEFAULT && ep.getValign()==DEFAULT)ep.setValign(rowvalign); //Wymuszenie wyrownania w calym wierszu

                TabDatElem tdel=(TabDatElem)ep;
                tdel.padding=padding; //Przekazanie parametrow do komorek
                tdel.border=border;
             //   if(border>0) System.out.println("border="+border);//!!!

                while(cell[c+r*cols]!=null && c<cols) {
                    c++;
                }

                if(c>=cols) { //Wszystkie komorki w wierszu mialy rowsp>1 lub bledna sytuacja
                              //(np. komorka bez poczatku wiersza) - nalezy dodac wiersz
                    c=0;r++;
                    rows++;
                    TabDatElem[] celltmp=new TabDatElem[cols*rows];
                    System.arraycopy(cell,0,celltmp,0,cell.length);
                    cell=celltmp;

                }

                for(j=0;j<tdel.colsp;j++) { //Kolejne komorki w poziomie
                    cell[c+j+r*cols]=tdel;
                }
                for(j=0;j<tdel.rowsp;j++) { //Kolejne komorki w pionie
                    cell[c+(r+j)*cols]=tdel;
                }

                c+=tdel.colsp;
            }

            ep=ep.gn();
         }*/
         System.out.println("count - cols="+cols+" rows="+rows);//!!!

    }
}

