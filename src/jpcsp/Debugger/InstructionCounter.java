/* This file is part of jpcsp.
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

import com.jidesoft.utils.SwingWorker;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFileChooser;
import jpcsp.Emulator;
import jpcsp.Memory;
import jpcsp.Allegrex.Instructions.*;
import jpcsp.Allegrex.Decoder;
import jpcsp.Allegrex.Common.Instruction;

/**
 *
 * @author  George
 */
public class InstructionCounter extends javax.swing.JFrame implements PropertyChangeListener {

    private Task task;
    Emulator emu;

    /** Creates new form InstructionCounter */
    public InstructionCounter(Emulator emu) {
        this.emu = emu;
        initComponents();
        RefreshWindow();
    }

    public void RefreshWindow() {
        resetcounts();
        initcheck.setSelected(false);
        finicheck.setSelected(false);
        textcheck.setSelected(false);
        stubtextcheck.setSelected(false);
        areastatus.setText("");
        if (emu.initsection[0] == 0) {
            initcheck.setEnabled(false);
        } else {
            initcheck.setEnabled(true);
        }
        if (emu.finisection[0] == 0) {
            finicheck.setEnabled(false);
        } else {
            finicheck.setEnabled(true);
        }
        if (emu.textsection[0] == 0) {
            textcheck.setEnabled(false);
        } else {
            textcheck.setEnabled(true);
        }
        if (emu.Stubtextsection[0] == 0) {
            stubtextcheck.setEnabled(false);
        } else {
            stubtextcheck.setEnabled(true);
        }
        areastatus.append("Found init section at " + Integer.toHexString(emu.initsection[0]) + " size " + emu.initsection[1] + "\n");
        areastatus.append("Found fini section at " + Integer.toHexString(emu.finisection[0]) + " size " + emu.finisection[1] + "\n");
        areastatus.append("Found text section at " + Integer.toHexString(emu.textsection[0]) + " size " + emu.textsection[1] + "\n");
        areastatus.append("Found stubtext section at " + Integer.toHexString(emu.Stubtextsection[0]) + " size " + emu.Stubtextsection[1] + "\n");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        textcheck = new javax.swing.JCheckBox();
        initcheck = new javax.swing.JCheckBox();
        finicheck = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        startbutton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        areastatus = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        OpcodeTable = new javax.swing.JTable();
        stubtextcheck = new javax.swing.JCheckBox();
        Save = new javax.swing.JButton();

        setTitle("Instruction Counter");
        setResizable(false);

        textcheck.setText(".text");

        initcheck.setText(".init");

        finicheck.setText(".fini");

        jLabel1.setText("Choose the sections you want to count:");

        startbutton.setText("Start Counting");
        startbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startbuttonActionPerformed(evt);
            }
        });

        areastatus.setColumns(20);
        areastatus.setFont(new java.awt.Font("Courier New", 0, 12));
        areastatus.setRows(5);
        jScrollPane1.setViewportView(areastatus);

        OpcodeTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [jpcsp.Allegrex.Common.instructions().length][3],
            new String [] {
                "Opcode", "Category", "Count"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(OpcodeTable);

        stubtextcheck.setText(".Stub.text");

        Save.setText("Save to file");
        Save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveActionPerformed(evt);
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
                        .addComponent(startbutton)
                        .addGap(18, 18, 18)
                        .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(stubtextcheck)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(textcheck)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(initcheck)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(finicheck))))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Save, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(finicheck)
                    .addComponent(initcheck)
                    .addComponent(textcheck)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(stubtextcheck)
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(startbutton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 402, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addComponent(Save))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void startbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startbuttonActionPerformed
    startbutton.setEnabled(false);
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    //Instances of javax.swing.SwingWorker are not reusuable, so
    //we create new instances as needed.
    task = new Task();
    task.addPropertyChangeListener(this);
    task.execute();

}//GEN-LAST:event_startbuttonActionPerformed

private void SaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveActionPerformed
    File file;
    final JFileChooser fc = new JFileChooser();
    fc.setDialogTitle("Save Instructions to File");
    fc.setCurrentDirectory(new java.io.File("."));
    fc.setSelectedFile(new File("instructionoutput.txt"));
    int returnvalue = fc.showSaveDialog(this);
    if (returnvalue == JFileChooser.APPROVE_OPTION) {
        file = fc.getSelectedFile();
    } else {
        return;
    }
    BufferedWriter bufferedWriter = null;
    try {

        //Construct the BufferedWriter object
        bufferedWriter = new BufferedWriter(new FileWriter(file));

        //Start writing to the output stream
        for (int i = 0; i < OpcodeTable.getRowCount(); i++) {

            OpcodeTable.getValueAt(i, 1);
            bufferedWriter.write(OpcodeTable.getValueAt(i, 0) + "  " + OpcodeTable.getValueAt(i, 1) + "  " + OpcodeTable.getValueAt(i, 2));
            bufferedWriter.newLine();
        }
    } catch (FileNotFoundException ex) {
        ex.printStackTrace();
    } catch (IOException ex) {
        ex.printStackTrace();
    } finally {
        //Close the BufferedWriter
        try {
            if (bufferedWriter != null) {
                bufferedWriter.flush();
                bufferedWriter.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}//GEN-LAST:event_SaveActionPerformed
   /**
     * Invoked when task's progress property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
        } 
    }
    public void proccesssections()
    {
        resetcounts();
        if(initcheck.isSelected()) findinitsections();
        if(textcheck.isSelected()) findfinisections();
        if(finicheck.isSelected()) findtextsections();
        if(stubtextcheck.isSelected()) findstubtextsections();
     }
            
    public void findinitsections()
    {
       for(int i =0; i< emu.initsection[1]; i+=4)
       {
          int memread32 = Memory.get_instance().read32(emu.initsection[0]+i);      
          jpcsp.Allegrex.Decoder.instruction(memread32).increaseCount();
       }   
    }
    public void findfinisections()
    {
       for(int i =0; i< emu.finisection[1]; i+=4)
       {
          int memread32 = Memory.get_instance().read32(emu.finisection[0]+i);
          jpcsp.Allegrex.Decoder.instruction(memread32).increaseCount();
       }   
    }
    public void findtextsections()
    {
       for(int i =0; i< emu.textsection[1]; i+=4)
       {
          int memread32 = Memory.get_instance().read32(emu.textsection[0]+i);
          jpcsp.Allegrex.Decoder.instruction(memread32).increaseCount();
       }   
    }
    public void findstubtextsections()
    {
        for(int i =0; i< emu.Stubtextsection[1]; i+=4)
       {
          int memread32 = Memory.get_instance().read32(emu.Stubtextsection[0]+i);
          jpcsp.Allegrex.Decoder.instruction(memread32).increaseCount();
       }   
        
    }
    class Task extends SwingWorker<Void, Void> {
        /*
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() {
            setProgress(0);
            progressBar.setIndeterminate(true);
            proccesssections();
            refreshCounter();
            return null;
        }

        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            startbutton.setEnabled(true);
            setCursor(null); //turn off the wait cursor
            progressBar.setIndeterminate(false);
        }
    }
    // Let's instanciate this private member so the two following methods
    // can retrieve the right opcodes.  
    public static jpcsp.Allegrex.Instructions INSTRUCTIONS = new jpcsp.Allegrex.Instructions();
    
    public void refreshCounter()
    {
        int i = 0;
        for (Instruction insn : jpcsp.Allegrex.Common.instructions()) {
            if (insn != null) {
                OpcodeTable.setValueAt(insn.name(), i, 0);
                OpcodeTable.setValueAt(insn.category(), i, 1);
                OpcodeTable.setValueAt(insn.getCount(), i, 2);
                i++;
            }
        }
    }
    public void resetcounts()
    {
        for (Instruction insn : jpcsp.Allegrex.Common.instructions()) {
            if (insn != null) {
                insn.resetCount();
            }
        }
    }
   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable OpcodeTable;
    private javax.swing.JButton Save;
    private javax.swing.JTextArea areastatus;
    private javax.swing.JCheckBox finicheck;
    private javax.swing.JCheckBox initcheck;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton startbutton;
    private javax.swing.JCheckBox stubtextcheck;
    private javax.swing.JCheckBox textcheck;
    // End of variables declaration//GEN-END:variables

}


