import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

class Toolbar extends Panel {
    Button[] buttons;
    Toolbar(String[] names) {
        super();
        setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
        buttons=new Button[names.length];
        for(int i=0;i<names.length;i++) {
            add(buttons[i]=new Button(names[i]));
        }
    }
}
