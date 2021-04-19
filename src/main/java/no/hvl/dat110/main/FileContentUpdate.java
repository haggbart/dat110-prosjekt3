package no.hvl.dat110.main;


import no.hvl.dat110.middleware.Message;
import no.hvl.dat110.rpc.interfaces.NodeInterface;
import no.hvl.dat110.util.FileManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.Set;


public class FileContentUpdate extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final JTextArea txtArea;
    private final JScrollPane sp;

    private final JButton btnUpdate = new JButton("Update");
    private final FileManager filemanager;
    private final NodeInterface selectedpeer;
    private final Message selectedpeerdata;
    //private BigInteger fileID;
    private final JButton btnClose = new JButton("Close");

    /**
     * Create the frame.
     */
    public FileContentUpdate(FileManager filemanager, NodeInterface selectedpeer, Message selectedpeerdata) {

        this.filemanager = filemanager;
        this.selectedpeer = selectedpeer;
        this.selectedpeerdata = selectedpeerdata;

        //this.fileID = fileID;

        setBounds(100, 100, 400, 300);
        setLayout(new GridBagLayout());

        // add list components
        txtArea = new JTextArea();
        sp = new JScrollPane(txtArea);
        txtArea.setEditable(true);                    // use this for read/write access
        txtArea.setLineWrap(true);
        txtArea.setWrapStyleWord(true);

        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        sp.setPreferredSize(new Dimension(250, 250));

        // add action listeners
        btnClose.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                btnCloseActionPerformed();
            }

        });

        // add action listeners
        btnUpdate.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                btnUpdateContent();
            }

        });

        // define layouts
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 5, 5, 5);

        // add components to frame and position them properly
        addComponentsToFrame(constraints);

        pack();
        setLocationRelativeTo(null);            // center on screen
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);                    // disable resizing of form
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    FileContentUpdate frame = new FileContentUpdate(null, null, null);
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void addContentToList(String txt) {

        txtArea.setText(txt);
    }

    private void addComponentsToFrame(GridBagConstraints constraints) {

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 4;
        constraints.weightx = 1.0;
        constraints.weighty = 0.5;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(sp, constraints);

        constraints.gridx = 2;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 0.5;
        constraints.fill = GridBagConstraints.NONE;
        add(btnUpdate, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 0.5;
        constraints.fill = GridBagConstraints.NONE;
        add(btnClose, constraints);

    }

    private void btnUpdateContent() {

        String newcontent = txtArea.getText();

        try {
            Set<Message> activepeers = null;

            // let's see if activepeers holding file is not null. If null, request newly
            if (filemanager.getActiveNodesforFile() == null)
                activepeers = filemanager.requestActiveNodesForFile(selectedpeerdata.getNameOfFile());
            else
                activepeers = filemanager.getActiveNodesforFile();

            boolean reply = selectedpeer.requestMutexWriteOperation(selectedpeerdata, newcontent.getBytes(), activepeers);

            JOptionPane.showMessageDialog(null, "Access granted? " + reply, "Message", JOptionPane.INFORMATION_MESSAGE);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void btnCloseActionPerformed() {
        this.dispose();
    }


}
