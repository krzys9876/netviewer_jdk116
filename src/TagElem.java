import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class TagElem extends Elem {
    boolean ends=false; //Dla znacznikow true oznacza znacznik koncowy, dla innych ignorowany

    TagElem(Tag tag,boolean newl) {
        super(TAG,tag,newl);

        ends=tag.ends();

        if("H1".equalsIgnoreCase(tag.getName())) {
            setType(PARAGRAPH);
            setTexttype(H1);
        } else
        if("H2".equalsIgnoreCase(tag.getName())) {
            setType(PARAGRAPH);
            setTexttype(H2);
        } else
        if("H3".equalsIgnoreCase(tag.getName())) {
            setType(PARAGRAPH);
            setTexttype(H3);
        } else
        if("H4".equalsIgnoreCase(tag.getName())) {
            setType(PARAGRAPH);
            setTexttype(H4);
        } else
        if("H5".equalsIgnoreCase(tag.getName())) {
            setType(PARAGRAPH);
            setTexttype(H5);
        } else
        if("H6".equalsIgnoreCase(tag.getName())) {
            setType(PARAGRAPH);
            setTexttype(H6);
        } else
        if("PRE".equalsIgnoreCase(tag.getName())) {
            setType(PARAGRAPH);
            setTexttype(PRE);
        } else
        if("HR".equalsIgnoreCase(tag.getName())) {
            setType(HR);
        } else
        if("B".equalsIgnoreCase(tag.getName())) {
            setType(FONTMOD);
            setMod(B);
        } else
        if("DFN".equalsIgnoreCase(tag.getName())) {
            setType(FONTMOD);
            setMod(DFN);
        } else
        if("STRONG".equalsIgnoreCase(tag.getName())) {
            setType(FONTMOD);
            setMod(STRONG);
        } else
        if("I".equalsIgnoreCase(tag.getName())) {
            setType(FONTMOD);
            setMod(I);
        } else
        if("ADDRESS".equalsIgnoreCase(tag.getName())) {
            setType(FONTMOD);
            setMod(ADDRESS);
        } else
        if("EM".equalsIgnoreCase(tag.getName())) {
            setType(FONTMOD);
            setMod(EM);
        } else
        if("VAR".equalsIgnoreCase(tag.getName())) {
            setType(FONTMOD);
            setMod(VAR);
        } else
        if("BIG".equalsIgnoreCase(tag.getName())) {
            setType(FONTMOD);
            setMod(BIG);
        } else
        if("CODE".equalsIgnoreCase(tag.getName())) {
            setType(FONTMOD);
            setMod(CODE);
        } else
        if("KBD".equalsIgnoreCase(tag.getName())) {
            setType(FONTMOD);
            setMod(KBD);
        } else
        if("TT".equalsIgnoreCase(tag.getName())) {
            setType(FONTMOD);
            setMod(TT);
        } else
        if("SMALL".equalsIgnoreCase(tag.getName())) {
            setType(FONTMOD);
            setMod(SMALL);
        } else
        if("TD".equalsIgnoreCase(tag.getName())) {
            setType(TABDAT);
        } else
        if("TH".equalsIgnoreCase(tag.getName())) {
            setType(TABDAT);
        } else
        if("TR".equalsIgnoreCase(tag.getName())) {
            setType(TABROW);
        } else
        if("TABLE".equalsIgnoreCase(tag.getName())) {
            setType(TABLE);
        } else
        if("DIV".equalsIgnoreCase(tag.getName())) {
            setType(DIV);
        } else
        if("CENTER".equalsIgnoreCase(tag.getName())) {
            setType(CENTERPAR);
        } else
        if("A".equalsIgnoreCase(tag.getName())) {
            setType(ANCHOR);
        } else
        if("SELECT".equalsIgnoreCase(tag.getName())) {
            setType(SELECT);
        } else
        if("TEXTAREA".equalsIgnoreCase(tag.getName())) {
            setType(TEXTAREA);
        } else
        if("OPTION".equalsIgnoreCase(tag.getName())) {
            setType(OPTION);
        } else
        if("INPUT".equalsIgnoreCase(tag.getName())) {
            Hashtable par=tag.getParams();

            Object otype=par.get("TYPE");

            setType(INPUT); //Domyslnie

            if(otype instanceof String) {
                String stype=(String)otype;

                if(stype.equalsIgnoreCase("TEXT")) setType(INPUTTEXT);
                else if(stype.equalsIgnoreCase("PASSWORD")) setType(INPUTPASS);
                else if(stype.equalsIgnoreCase("CHECKBOX")) setType(INPUTCHECK);
                else if(stype.equalsIgnoreCase("RADIO")) setType(INPUTRADIO);
                else if(stype.equalsIgnoreCase("SUBMIT")) setType(INPUTSUBMIT);
                else if(stype.equalsIgnoreCase("RESET")) setType(INPUTRESET);
                else if(stype.equalsIgnoreCase("HIDDEN")) setType(INPUTHIDDEN);
            }
        } else
        if("IMG".equalsIgnoreCase(tag.getName())) {
            setType(IMG);
        }

    }

    public int getType() {
        return type+(ends ? ENDS : 0);
    }
}