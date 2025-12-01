package main;

import javax.swing.SwingUtilities;
import view.BenhNhanForm;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BenhNhanForm().setVisible(true);
        });
    }
}