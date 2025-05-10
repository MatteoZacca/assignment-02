package pcd.ass02.gui_example.frame_inheritance;

import javax.swing.*;
import java.awt.*;

public class MyFrame extends JFrame{
    JLabel label = new JLabel("AST");

    public MyFrame() {
        super("Parsing Tree");
        Container c = this.getContentPane();
        c.add(label);
        this.setSize(400, 400);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}
