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

import java.awt.Point;
import jpcsp.*;

public class ElfHeaderInfo extends javax.swing.JFrame {

    /** Creates new form ElfHeaderInfo */
    public ElfHeaderInfo() {
        initComponents();
        ELFInfoArea.append(FileManager.PbpInfo);
        ELFInfoArea.append(FileManager.ElfInfo);
        ELFInfoArea.append(FileManager.ProgInfo);
        ELFInfoArea.append(FileManager.SectInfo);

    }
    public void RefreshWindow()
    {
      ELFInfoArea.setText("");
      ELFInfoArea.append(FileManager.PbpInfo);
      ELFInfoArea.append(FileManager.ElfInfo);
      ELFInfoArea.append(FileManager.ProgInfo);
      ELFInfoArea.append(FileManager.SectInfo);
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        ELFInfoArea = new javax.swing.JTextArea();

        setTitle("Elf Header Info");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        ELFInfoArea.setColumns(20);
        ELFInfoArea.setEditable(false);
        ELFInfoArea.setFont(new java.awt.Font("Courier New", 0, 12));
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



private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    Point location = getLocation();
    //location.x
    String[] coord = new String[2];
    coord[0]=Integer.toString(location.x);
    coord[1]=Integer.toString(location.y);
    
    if (Settings.get_instance().readBoolOptions("guisettings/saveWindowPos"))
        Settings.get_instance().writeWindowPos("elfheader", coord);
    
}//GEN-LAST:event_formWindowClosing


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea ELFInfoArea;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

}
