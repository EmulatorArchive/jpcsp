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
package jpcsp.Debugger;

import jpcsp.Emulator;
import jpcsp.WindowPropSaver;

public class ElfHeaderInfo extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;
    public static String PbpInfo;
    public static String ElfInfo;
    public static String ProgInfo;
    public static String SectInfo;

    /**
     * Creates new form ElfHeaderInfo
     */
    public ElfHeaderInfo() {
        initComponents();
        ELFInfoArea.append(PbpInfo);
        ELFInfoArea.append(ElfInfo);
        ELFInfoArea.append(ProgInfo);
        ELFInfoArea.append(SectInfo);

        WindowPropSaver.loadWindowProperties(this);
    }

    public void RefreshWindow() {
        ELFInfoArea.setText("");
        ELFInfoArea.append(PbpInfo);
        ELFInfoArea.append(ElfInfo);
        ELFInfoArea.append(ProgInfo);
        ELFInfoArea.append(SectInfo);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        ELFInfoArea = new javax.swing.JTextArea();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("jpcsp/languages/jpcsp"); // NOI18N
        setTitle(bundle.getString("ElfHeaderInfo.title")); // NOI18N
        setResizable(false);

        ELFInfoArea.setEditable(false);
        ELFInfoArea.setColumns(20);
        ELFInfoArea.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
        ELFInfoArea.setLineWrap(true);
        ELFInfoArea.setRows(5);
        jScrollPane1.setViewportView(ELFInfoArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void dispose() {
        Emulator.getMainGUI().endWindowDialog();
        super.dispose();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea ELFInfoArea;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
