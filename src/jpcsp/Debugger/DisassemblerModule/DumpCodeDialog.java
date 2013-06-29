/*
 This file is part of jpcsp.

 Jpcsp is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Jpcsp is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Jpcsp.  If not, see <http://www.gnu.org/licenses/>.
 */
package jpcsp.Debugger.DisassemblerModule;

import java.io.File;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import jpcsp.util.Constants;
import jpcsp.util.JpcspDialogManager;

public class DumpCodeDialog extends javax.swing.JDialog {

    public final static int DUMPCODE_APPROVE = 1;
    int retVal = 0;

    public DumpCodeDialog(java.awt.Frame parent, int start) {
        super(parent, true);
        initComponents();
        setLocationRelativeTo(parent);
        txtStartAddress.setText(String.format("0x%08X", start));
        txtEndAddress.setText(String.format("0x%08X", start));
    }

    public int getStartAddress() {
        return Integer.decode(txtStartAddress.getText());
    }

    public int getEndAddress() {
        return Integer.decode(txtEndAddress.getText());
    }

    public String getFilename() {
        return txtFilename.getText();
    }

    public int getReturnValue() {
        return retVal;
    }

    private class AddressInputVerifier extends InputVerifier {

        @Override
        public boolean verify(JComponent input) {
            try {
                JTextField tf = (JTextField) input;
                int value = Integer.decode(tf.getText());

                // code always starts at DWORD address
                if ((value % 4) != 0) {
                    value /= 4;
                }

                tf.setText(String.format("0x%08X", value));
                return true;
            } catch (NumberFormatException nfe) {
                return false;
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblStartAddress = new javax.swing.JLabel();
        txtStartAddress = new javax.swing.JTextField();
        lblEndAddress = new javax.swing.JLabel();
        lblFilename = new javax.swing.JLabel();
        txtEndAddress = new javax.swing.JTextField();
        txtFilename = new javax.swing.JTextField();
        btnFilename = new javax.swing.JButton();
        btnOk = new javax.swing.JButton();
        btnCancel = new jpcsp.GUI.CancelButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("jpcsp/languages/jpcsp"); // NOI18N
        setTitle(bundle.getString("DumpCodeDialog.title")); // NOI18N
        setResizable(false);

        lblStartAddress.setText(bundle.getString("DumpCodeDialog.lblStartAddress.text")); // NOI18N

        txtStartAddress.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
        txtStartAddress.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtStartAddress.setText("0x00000000"); // NOI18N
        txtStartAddress.setInputVerifier(new AddressInputVerifier());

        lblEndAddress.setText(bundle.getString("DumpCodeDialog.lblEndAddress.text")); // NOI18N

        lblFilename.setText(bundle.getString("DumpCodeDialog.lblFilename.text")); // NOI18N

        txtEndAddress.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
        txtEndAddress.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtEndAddress.setText("0x00000000"); // NOI18N
        txtEndAddress.setInputVerifier(new AddressInputVerifier());

        txtFilename.setEditable(false);
        txtFilename.setText("dump.txt"); // NOI18N

        btnFilename.setText("..."); // NOI18N
        btnFilename.setPreferredSize(new java.awt.Dimension(25, 25));
        btnFilename.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilenameActionPerformed(evt);
            }
        });

        btnOk.setText(bundle.getString("OkButton.text")); // NOI18N
        btnOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOkActionPerformed(evt);
            }
        });

        btnCancel.setText(bundle.getString("CancelButton.text")); // NOI18N
        btnCancel.setParent(this);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblStartAddress)
                            .addComponent(lblEndAddress)
                            .addComponent(lblFilename))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtStartAddress)
                            .addComponent(txtEndAddress)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtFilename, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnFilename, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnOk, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblStartAddress)
                    .addComponent(txtStartAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEndAddress)
                    .addComponent(txtEndAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFilename, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnFilename, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFilename))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOk)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnFilenameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilenameActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(java.util.ResourceBundle.getBundle("jpcsp/languages/jpcsp").getString("DumpCodeDialog.dlgOutputFile.title"));

        fc.setSelectedFile(new File(txtFilename.getText()));
        fc.addChoosableFileFilter(Constants.fltTextFiles);
        fc.setFileFilter(Constants.fltTextFiles);

        int returnVal = fc.showOpenDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }

        txtFilename.setText(fc.getSelectedFile().getAbsolutePath());
    }//GEN-LAST:event_btnFilenameActionPerformed

    private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkActionPerformed
        if (getStartAddress() > getEndAddress()) {
            JpcspDialogManager.showError(
                    this,
                    java.util.ResourceBundle.getBundle("jpcsp/languages/jpcsp").getString("DumpCodeDialog.strErrAddressRange.title"));
            return;
        }

        retVal = DUMPCODE_APPROVE;
        setVisible(false);
    }//GEN-LAST:event_btnOkActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private jpcsp.GUI.CancelButton btnCancel;
    private javax.swing.JButton btnFilename;
    private javax.swing.JButton btnOk;
    private javax.swing.JLabel lblEndAddress;
    private javax.swing.JLabel lblFilename;
    private javax.swing.JLabel lblStartAddress;
    private javax.swing.JTextField txtEndAddress;
    private javax.swing.JTextField txtFilename;
    private javax.swing.JTextField txtStartAddress;
    // End of variables declaration//GEN-END:variables
}
