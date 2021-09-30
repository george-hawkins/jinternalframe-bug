package com.example;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.beans.BeanProperty;
import java.beans.PropertyVetoException;

public class InternalFrameBugFixDemo extends JFrame {
    JDesktopPane desktop;

    public InternalFrameBugFixDemo() {
        super("InternalFrameDemo");

        setBounds(16, 16, 1024, 512);

        desktop = new JDesktopPane();

        JInternalFrame emptyFrame = new ClassicJInternalFrame("Alpha");
        JInternalFrame comboFrame = new ClassicJInternalFrame("Beta");

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

        InternalFrameBugFixDemo frame = new InternalFrameBugFixDemo();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(InternalFrameBugFixDemo::createAndShowGUI);
    }

    public static class ClassicJInternalFrame extends JInternalFrame {
        public ClassicJInternalFrame() {
            super();
        }
        public ClassicJInternalFrame(String title) {
            super(title);
        }
        public ClassicJInternalFrame(String title, boolean resizable) {
            super(title, resizable);
        }
        public ClassicJInternalFrame(String title, boolean resizable, boolean closable) {
            super(title, resizable, closable);
        }
        public ClassicJInternalFrame(String title, boolean resizable, boolean closable, boolean maximizable) {
            super(title, resizable, closable, maximizable);
        }
        public ClassicJInternalFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable) {
            super(title, resizable, closable, maximizable, iconifiable);
        }

        // -----------------------------------------------------------------

        // The two methods below, `setSelected` and `dispose`, are copied from `JInternalFrame` as it existed here:
        // https://github.com/openjdk/jdk/blob/jdk-11%2B12/src/java.desktop/share/classes/javax/swing/JInternalFrame.java
        // They've then been modified to back out this change:
        // https://github.com/openjdk/jdk/commit/4b8cfe5a6058cdada1ad6efbb8f81e2f8f53a177
        // The ticket for that change is https://bugs.openjdk.java.net/browse/JDK-8173739
        //
        // On moving to Java 11 we found that if you had two windows, A and B, where A currently has focus and
        // B has a combo-box, if you click on the combo-box of B you see the combo's dropdown list appear
        // and then immediately disappear - the trigger for this disappearance is an ungrab events generated
        // by A, i.e. the window losing focus as a result of you clicking on a component in window B.
        // The generation of ungrab events in this situation was introduced in the change linked to above.

        @BeanProperty(description
                = "Indicates whether this internal frame is currently the active frame.")
        public void setSelected(boolean selected) throws PropertyVetoException {
            // The InternalFrame may already be selected, but the focus
            // may be outside it, so restore the focus to the subcomponent
            // which previously had it. See Bug 4302764.
            if (selected && isSelected) {
                restoreSubcomponentFocus();
                return;
            }
            // The internal frame or the desktop icon must be showing to allow
            // selection.  We may deselect even if neither is showing.
            if ((isSelected == selected) || (selected &&
                    (isIcon ? !desktopIcon.isShowing() : !isShowing()))) {
                return;
            }

            Boolean oldValue = isSelected ? Boolean.TRUE : Boolean.FALSE;
            Boolean newValue = selected ? Boolean.TRUE : Boolean.FALSE;
            fireVetoableChange(IS_SELECTED_PROPERTY, oldValue, newValue);

        /* We don't want to leave focus in the previously selected
           frame, so we have to set it to *something* in case it
           doesn't get set in some other way (as if a user clicked on
           a component that doesn't request focus).  If this call is
           happening because the user clicked on a component that will
           want focus, then it will get transfered there later.

           We test for parent.isShowing() above, because AWT throws a
           NPE if you try to request focus on a lightweight before its
           parent has been made visible */

            if (selected) {
                restoreSubcomponentFocus();
            }

            isSelected = selected;
            firePropertyChange(IS_SELECTED_PROPERTY, oldValue, newValue);
            if (isSelected)
                fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_ACTIVATED);
            else {
                fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_DEACTIVATED);
            }
            repaint();
        }

        public void dispose() {
            if (isVisible()) {
                setVisible(false);
            }
            if (!isClosed) {
                firePropertyChange(IS_CLOSED_PROPERTY, Boolean.FALSE, Boolean.TRUE);
                isClosed = true;
            }
            fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_CLOSED);
        }
    }
}
