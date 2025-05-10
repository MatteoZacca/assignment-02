package pcd.ass02.gui_example.mouse_events;

import javax.swing.*;

public class MyFrame extends JFrame {
    public MyFrame() {
        super("Mouse Test");
        this.addMouseListener(new MouseSpy());
        this.setSize(400, 400);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
