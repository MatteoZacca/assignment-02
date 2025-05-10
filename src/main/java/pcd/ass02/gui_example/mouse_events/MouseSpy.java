package pcd.ass02.gui_example.mouse_events;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class MouseSpy extends MouseAdapter {
    public void mouseClicked(MouseEvent event) {
        System.out.println("Click su: (" + event.getX() + ", " + event.getY() + ")");
    }
}
