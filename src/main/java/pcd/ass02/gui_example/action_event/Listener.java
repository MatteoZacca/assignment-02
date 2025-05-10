package pcd.ass02.gui_example.action_event;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Listener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
        JButton button = (JButton) event.getSource();
        JOptionPane.showMessageDialog(null,
                "E stato premuto il tasto: " + button.getText());
    }
}
