package pcd.ass02.gui_example;

import javax.swing.*;
import java.awt.*;

public class FirstExample {
    public static void main(String[] args) {

        JFrame window = new JFrame("Parsing Tree");

        JTextField textField = new JTextField(4);
        JLabel labelField = new JLabel("Dipendenze:");
        JButton submitButton = new JButton("Submit");

        String[] lista = new String[10];
        for (int i = 0; i < lista.length; i++){
            lista[i] = "Elemento numero " + i;
        }
        JComboBox comboBox = new JComboBox<>(lista);
        Container c = window.getContentPane();
        c.add(comboBox);
        //c.add(new JLabel("AST"));

        window.setVisible(true);
        window.setResizable(true);
        window.setSize(400, 400);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
