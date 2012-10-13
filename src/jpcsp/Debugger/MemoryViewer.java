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
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

import jpcsp.Emulator;
import jpcsp.Memory;
import jpcsp.Resource;
import jpcsp.settings.Settings;
import jpcsp.util.Utilities;

/**
 *
 * @author  George
 */
public class MemoryViewer extends javax.swing.JFrame {

	private static final long serialVersionUID = 1L;
	//Processor cpu;
    private int startaddress;
    private Point lastLocation = null;


    /** Creates new form MemoryViewer */
    public MemoryViewer() {
        //this.cpu = c;
        startaddress=Emulator.getProcessor().cpu.pc;
        initComponents();

        RefreshMemory();
    }
    public static char converttochar(int character)
    {
      //char newone = (char)Memory.getInstance().read8(address);
      //if(newone <32 || newone >127)
      //    return (byte)32;
     // else
        if (character < 0x020 || character >= 0x07f && character <= 0x0a0 ||character == 0x0ad)
            return '.';
		return (char)(character & 0x0ff);

    }

    private static byte safeRead8(Memory mem, int address) {
        byte value = 0;
        if (Memory.isAddressGood(address)) {
            value = (byte)mem.read8(address);
        }

        return value;
    }

    public static String getMemoryView(int addr) {
    	byte[] line = new byte[16];
    	Memory mem = Memory.getInstance();

    	for (int i = 0; i < line.length; i++) {
            line[i] = safeRead8(mem, addr + i);
    	}

    	return String.format("%08x : %02x %02x %02x %02x %02x %02x " +
                "%02x %02x %02x %02x %02x %02x %02x %02x " +
                "%02x %02x %c %c %c %c %c %c %c %c %c %c %c %c %c %c %c %c", addr,
                line[0], line[1], line[2], line[3], line[4], line[5], line[6], line[7],
                line[8], line[9], line[10], line[11], line[12], line[13], line[14], line[15],
                converttochar(line[0]), converttochar(line[1]),
                converttochar(line[2]), converttochar(line[3]),
                converttochar(line[4]), converttochar(line[5]),
                converttochar(line[6]), converttochar(line[7]),
                converttochar(line[8]), converttochar(line[9]),
                converttochar(line[10]), converttochar(line[11]),
                converttochar(line[12]), converttochar(line[13]),
                converttochar(line[14]), converttochar(line[15])
                );
    }

    public void RefreshMemory()
    {
        int addr = startaddress;
        memoryview.setText("");
        for (int y = 0; y < 22; y++) { //21 lines
        	if (y > 0) {
                memoryview.append("\n");
        	}
            memoryview.append(getMemoryView(addr));
            addr +=16;
        }
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        memoryview = new javax.swing.JTextArea();
        AddressField = new javax.swing.JTextField();
        GoToButton = new javax.swing.JButton();
        GoToSP = new javax.swing.JButton();
        DumpRawRam = new javax.swing.JButton();
        GoToButton1 = new javax.swing.JButton();

        setTitle(Resource.get("memoryviewer"));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowDeactivated(java.awt.event.WindowEvent evt) {
                formWindowDeactivated(evt);
            }
        });

        memoryview.setColumns(20);
        memoryview.setEditable(false);
        memoryview.setFont(new java.awt.Font("Courier New", 0, 12));
        memoryview.setRows(5);
        memoryview.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            @Override
			public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                memoryviewMouseWheelMoved(evt);
            }
        });
        memoryview.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                memoryviewKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(memoryview);

        AddressField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                onKeyPressed(evt);
            }
        });

        GoToButton.setText(Resource.get("gotoaddress"));
        GoToButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GoToButtonActionPerformed(evt);
            }
        });

        GoToSP.setText(Resource.get("gotosp"));
        GoToSP.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GoToSPActionPerformed(evt);
            }
        });

        DumpRawRam.setText(Resource.get("dumprawram"));
        DumpRawRam.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DumpRawRamActionPerformed(evt);
            }
        });

        GoToButton1.setText(Resource.get("gotovram"));
        GoToButton1.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GoToButton1ActionPerformed(evt);
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
                        .addComponent(AddressField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(GoToButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(GoToButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(GoToSP)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 114, Short.MAX_VALUE)
                        .addComponent(DumpRawRam))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 646, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(DumpRawRam)
                    .addComponent(AddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(GoToButton)
                    .addComponent(GoToSP)
                    .addComponent(GoToButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 314, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void memoryviewKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_memoryviewKeyPressed
   if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN){
       startaddress +=16;
       evt.consume();
       RefreshMemory();
   }
   else if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_UP){
       startaddress -=16;
       evt.consume();
       RefreshMemory();
   }
    else if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_PAGE_UP)
    {
       startaddress -=352;
       evt.consume();
       RefreshMemory();
    }
    else if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_PAGE_DOWN)
    {
       startaddress +=352;
       evt.consume();
       RefreshMemory();
    }
}//GEN-LAST:event_memoryviewKeyPressed

private void GoToButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GoToButtonActionPerformed
    GoToAddress();
}//GEN-LAST:event_GoToButtonActionPerformed

private void GoToAddress() {
    String gettext = AddressField.getText();
    int value;
    try {
        value = Utilities.parseAddress(gettext);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, Resource.get("numbernotcorrect"));
        return;
    }
    startaddress = value;
    RefreshMemory();
}

private void memoryviewMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_memoryviewMouseWheelMoved
// TODO add your handling code here:
       if (evt.getWheelRotation() > 0){
       startaddress +=16;
       evt.consume();
       RefreshMemory();
   }
   else {
       startaddress -=16;
       evt.consume();
       RefreshMemory();
   }
}//GEN-LAST:event_memoryviewMouseWheelMoved

private void formWindowDeactivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowDeactivated
    //Called when the mainWindow is closed
    if (Settings.getInstance().readBool("gui.saveWindowPos")) {
        Point location = getLocation();
        if (lastLocation == null || location.x != lastLocation.x || location.y != lastLocation.y) {
            Settings.getInstance().writeWindowPos("memoryview", location);
            lastLocation = location;
        }
    }
}//GEN-LAST:event_formWindowDeactivated

private void GoToSPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GoToSPActionPerformed
    startaddress = Emulator.getProcessor().cpu._sp;
    RefreshMemory();
}//GEN-LAST:event_GoToSPActionPerformed

private void DumpRawRamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DumpRawRamActionPerformed
   File f = new File("ramdump.bin");
   BufferedWriter out = null;
   try
   {
       out = new BufferedWriter( new FileWriter(f) );
       Memory mem = Memory.getInstance();
       for(int i = 0x08000000; i<=0x09ffffff; i++ )
       {
          out.write(safeRead8(mem, i));
       }

   }
   catch(IOException e)
   {

   }
   finally
   {
        Utilities.close(out);
   }


}//GEN-LAST:event_DumpRawRamActionPerformed

private void onKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_onKeyPressed
    if (evt.getKeyCode() == KeyEvent.VK_ENTER)
        GoToAddress();
}//GEN-LAST:event_onKeyPressed

private void GoToButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GoToButton1ActionPerformed
    // TODO add your handling code here:
    startaddress = 0x04000000;
    RefreshMemory();
}//GEN-LAST:event_GoToButton1ActionPerformed


	@Override
	public void dispose() {
		Emulator.getMainGUI().endWindowDialog();
		super.dispose();
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField AddressField;
    private javax.swing.JButton DumpRawRam;
    private javax.swing.JButton GoToButton;
    private javax.swing.JButton GoToButton1;
    private javax.swing.JButton GoToSP;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea memoryview;
    // End of variables declaration//GEN-END:variables

}
