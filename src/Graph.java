import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class Graph extends Object {

    public static void draw3DRect(Graphics g, int x, int y, int width, int height, boolean raised) {
        draw3DRect(g,x,y,width,height,raised,1);
    }

    public static void draw3DRect(Graphics g, int x, int y, int width, int height, boolean raised,int border) {
        Color coltmp=g.getColor();
        for(int i=0;i<border;i++) {



        g.setColor(raised ? Color.white : Color.darkGray);
        g.drawLine(x+i,y+i,x+i,y+height-i);
        g.drawLine(x+i,y+i,x+width-i,y+i);
        g.setColor(raised ? Color.darkGray : Color.white);
        g.drawLine(x+width-i,y+i,x+width-i,y+height-i);
        g.drawLine(x+i,y+height-i,x+width-i,y+height-i);

        }
        g.setColor(coltmp);
    }

    public static void draw3DFrame(Graphics g, int x, int y, int width, int height, int weight, boolean raised) {
        draw3DFrame(g,x,y,width,height,weight,raised,1);
    }

    public static void draw3DFrame(Graphics g, int x, int y, int width, int height, int weight, boolean raised, int border) {
        draw3DRect(g,x,y,width,height,raised,border);
        draw3DRect(g,x+weight,y+weight,width-2*weight,height-2*weight,!raised,border);
    }
}