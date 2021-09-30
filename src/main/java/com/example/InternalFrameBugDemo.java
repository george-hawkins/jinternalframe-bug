package com.example;

import javax.swing.*;
import java.awt.*;

public class InternalFrameBugDemo extends JFrame {
    JDesktopPane desktop;

    public InternalFrameBugDemo() {
        super("InternalFrameDemo");

        setBounds(16, 16, 1024, 512);

        desktop = new JDesktopPane();

        JInternalFrame emptyFrame = new JInternalFrame("Alpha");
        JInternalFrame comboFrame = new JInternalFrame("Beta");

        comboFrame.setContentPane(createComboPanel());

        placeAt(emptyFrame, 16, 16);
        placeAt(comboFrame, 256, 16);

        desktop.add(emptyFrame);
        desktop.add(comboFrame);

        setContentPane(desktop);
    }

    private void placeAt(JInternalFrame frame, int x, int y) {
        frame.setSize(128, 128);
        frame.setLocation(x, y);
        frame.setVisible(true);
    }

    private JPanel createComboPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] petStrings = { "Bird", "Cat", "Dog", "Rabbit", "Pig" };

        JComboBox<String> petList = new JComboBox<>(petStrings);

        panel.add(petList, BorderLayout.PAGE_START);
        panel.add(new JPanel(), BorderLayout.PAGE_END);
        panel.setOpaque(true);

        return panel;
    }

    private static void createAndShowGUI() {
        JFrame.setDefaultLookAndFeelDecorated(true);

        InternalFrameBugDemo frame = new InternalFrameBugDemo();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(InternalFrameBugDemo::createAndShowGUI);
    }
}
