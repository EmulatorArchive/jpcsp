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

import javax.swing.JOptionPane;
import jpcsp.Memory;
import jpcsp.Processor;

/**
 *
 * @author  George
 */
public class MemoryViewer extends javax.swing.JInternalFrame {
    Processor cpu;
    int startaddress; 
    /** Creates new form MemoryViewer */
    public MemoryViewer(Processor c) {
        this.cpu = c;
        startaddress=c.pc;
        initComponents();
        
        RefreshMemory();
    }
    public char converttochar(int address)
    {
        int character = Memory.get_instance().read8(address);
      //char newone = (char)Memory.get_instance().read8(address);
      //if(newone <32 || newone >127)
      //    return (byte)32;
     // else
        if (character < 0x020 || character >= 0x07f && character <= 0x0a0 ||character == 0x0ad)
            return (char)'.';
        else
          return (char)(character & 0x0ff);
      
    }
    public void RefreshMemory()
    {
      int addr = startaddress;
      for(int y=0; y<22; y++)//21 lines
      {
                memoryview.append(String.format("%08x : %02x %02x %02x %02x %02x %02x " +
                                  "%02x %02x %02x %02x %02x %02x %02x %02x " +
                                   "%02x %02x %c %c %c %c %c %c %c %c %c %c %c %c %c %c %c %c", addr,
                                               (byte)Memory.get_instance().read8(addr),
                                               (byte)Memory.get_instance().read8(addr+1),
                                               (byte)Memory.get_instance().read8(addr+2),
                                               (byte)Memory.get_instance().read8(addr+3),
                                               (byte)Memory.get_instance().read8(addr+4),
                                               (byte)Memory.get_instance().read8(addr+5),
                                               (byte)Memory.get_instance().read8(addr+6),
                                               (byte)Memory.get_instance().read8(addr+7),
                                               (byte)Memory.get_instance().read8(addr+8),
                                               (byte)Memory.get_instance().read8(addr+9),
                                               (byte)Memory.get_instance().read8(addr+10),
                                               (byte)Memory.get_instance().read8(addr+11),
                                               (byte)Memory.get_instance().read8(addr+12),
                                               (byte)Memory.get_instance().read8(addr+13),
                                               (byte)Memory.get_instance().read8(addr+14),
                                               (byte)Memory.get_instance().read8(addr+15),
                                               converttochar(addr),
                                               converttochar(addr+1),
                                               converttochar(addr+2),
                                               converttochar(addr+3),
                                               converttochar(addr+4),
                                               converttochar(addr+5),
                                               converttochar(addr+6),
                                               converttochar(addr+7),
                                               converttochar(addr+8),
                                               converttochar(addr+9),
                                               converttochar(addr+10),
                                               converttochar(addr+11),
                                               converttochar(addr+12),
                                               converttochar(addr+13),
                                               converttochar(addr+14),
                                               converttochar(addr+15)
                                               
                                               )
                                               );
                if(y !=21) memoryview.append("\n");
           // }
            addr +=16;
          
      }
        
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
        memoryview = new javax.swing.JTextArea();
        AddressField = new javax.swing.JTextField();
        GoToButton = new javax.swing.JButton();

        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        setTitle("Memory Viewer");

        memoryview.setColumns(20);
        memoryview.setEditable(false);
        memoryview.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
        memoryview.setRows(5);
        memoryview.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                memoryviewMouseWheelMoved(evt);
            }
        });
        memoryview.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                memoryviewKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(memoryview);

        GoToButton.setText("Go to Address");
        GoToButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GoToButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(AddressField, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(GoToButton))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 646, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(28, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(AddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(GoToButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 314, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void memoryviewKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_memoryviewKeyPressed
   if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN){
       startaddress +=16;
       evt.consume();
       memoryview.setText("");
       RefreshMemory();
   }
   else if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_UP){
       startaddress -=16;
       evt.consume();
       memoryview.setText("");
       RefreshMemory();
   }
    else if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_PAGE_UP)
    {
       startaddress -=352;
       evt.consume();
       memoryview.setText("");
       RefreshMemory();
    }
    else if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_PAGE_DOWN)
    {
       startaddress +=352;
       evt.consume();
       memoryview.setText("");
       RefreshMemory();
    }
}//GEN-LAST:event_memoryviewKeyPressed

private void GoToButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GoToButtonActionPerformed
         String gettext = AddressField.getText();
         int value;
         try {
            value = Integer.parseInt(gettext, 16);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "The Number you enter is not correct");
            return;
        }
         startaddress = value;
         memoryview.setText("");
         RefreshMemory();
}//GEN-LAST:event_GoToButtonActionPerformed

private void memoryviewMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_memoryviewMouseWheelMoved
// TODO add your handling code here:
       if (evt.getWheelRotation() > 0){
       startaddress +=16;
       evt.consume();
       memoryview.setText("");
       RefreshMemory();
   }
   else {
       startaddress -=16;
       evt.consume();
       memoryview.setText("");
       RefreshMemory();
   }
}//GEN-LAST:event_memoryviewMouseWheelMoved


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField AddressField;
    private javax.swing.JButton GoToButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea memoryview;
    // End of variables declaration//GEN-END:variables

}
