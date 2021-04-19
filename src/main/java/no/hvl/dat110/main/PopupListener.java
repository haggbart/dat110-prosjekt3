/**
 *
 */
package no.hvl.dat110.main;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author tdoy
 *
 */
public class PopupListener extends MouseAdapter {

    private final JPopupMenu popup;

    public PopupListener(JPopupMenu popup) {
        this.popup = popup;
    }

    @Override
    public void mousePressed(MouseEvent e) {

        showPopup(e);

    }

    @Override
    public void mouseReleased(MouseEvent e) {

        showPopup(e);

    }


    private void showPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            popup.show(e.getComponent(),
                    e.getX(), e.getY());
        }
    }

}
