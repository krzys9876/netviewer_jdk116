import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class StatusBar extends Canvas {
    String text=null;

    StatusBar() {
        super();
        setBackground(Color.lightGray);
        setForeground(Color.black);
        reshape(0,0,10,20);
        setText("");
    }

    public void setText(String t) {
        if(t!=null && !t.equals(text)) {
            text=t;
            Graphics g=getGraphics();
            if(g!=null) {
                eraseText(g);
                drawText(g); //Trzeba wywolac te metody wprost, bo efekt ma byc natychmiastowy
            }
        }
    }

    public void paint(Graphics g) {
        eraseText(g);
        drawText(g);
        Graph.draw3DFrame(g,0,0,size().width-1,size().height-1,2,true);
    }

    private void eraseText(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(3,3,size().width-6,size().height-6);
    }

    private void drawText(Graphics g) {
        g.setColor(Color.black);
        g.setFont(new Font("DialogInput",Font.PLAIN,12));
        if(text!=null) {
            g.drawString(text,10,g.getFontMetrics().getAscent()+3);
        }
    }
}
