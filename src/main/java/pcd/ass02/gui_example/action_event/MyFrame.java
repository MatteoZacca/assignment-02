package pcd.ass02.gui_example.action_event;

import javax.swing.*;
import java.awt.*;

public class MyFrame extends JFrame {
    private JButton uno = new JButton("Uno");
    private JButton due = new JButton("Due");
    private JButton tre = new JButton("Tre");
    Listener listener = new Listener();

    public MyFrame() {
        super("Buttons");
        setLayout(new FlowLayout());
        Container c = this.getContentPane();

        c.add(uno);
        uno.addActionListener(listener);
        c.add(due);
        due.addActionListener(listener);
        c.add(tre);
        tre.addActionListener(listener);
        setSize(200, 200);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
