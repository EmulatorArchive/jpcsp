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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import jpcsp.Resource;
import jpcsp.Settings;
import jpcsp.util.Utilities;

/**
 *
 * @author  shadow
 */
public class ConsoleWindow extends javax.swing.JFrame {

	private static final long serialVersionUID = 1L;

	private transient PrintStream  m_stdoutPS = new PrintStream( 
        new JTextAreaOutStream( new ByteArrayOutputStream() ) );

    /**
     * Display infinite characters in the textarea, no limit.
     * <p>
     * <b>NOTE:</b> Will slow down your application if a lot of messages
     * are to be displayed to the textarea (more than a couple of Kbytes).
     */
    private int m_maxChars = -1;


    /** Creates new form LoggingWindow */
    public ConsoleWindow() {
        initComponents();
        System.setOut( m_stdoutPS );
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        talogging = new javax.swing.JTextArea();
        ClearMessageButton = new javax.swing.JButton();
        SaveMessageToFileButton = new javax.swing.JButton();

        setTitle(Resource.get("logger"));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
			public void windowDeactivated(java.awt.event.WindowEvent evt) {
                formWindowDeactivated(evt);
            }
        });

        talogging.setColumns(20);
        talogging.setFont(new java.awt.Font("Courier New", 0, 12));
        talogging.setRows(5);
        jScrollPane1.setViewportView(talogging);

        ClearMessageButton.setText(Resource.get("clearmessages"));
        ClearMessageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClearMessageButtonActionPerformed(evt);
            }
        });

        SaveMessageToFileButton.setText(Resource.get("savemessages"));
        SaveMessageToFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveMessageToFileButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(317, Short.MAX_VALUE)
                .addComponent(SaveMessageToFileButton)
                .addGap(18, 18, 18)
                .addComponent(ClearMessageButton))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 583, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ClearMessageButton)
                    .addComponent(SaveMessageToFileButton)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void ClearMessageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClearMessageButtonActionPerformed
   clearScreenMessages();
}//GEN-LAST:event_ClearMessageButtonActionPerformed

private void SaveMessageToFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveMessageToFileButtonActionPerformed
    JFileChooser m_fileChooser = new JFileChooser();
    m_fileChooser.setSelectedFile(new File("logoutput.txt"));
    m_fileChooser.setDialogTitle(Resource.get("savelogging"));
    m_fileChooser.setCurrentDirectory(new java.io.File("."));
    int returnVal = m_fileChooser.showSaveDialog(this);
    if (returnVal != JFileChooser.APPROVE_OPTION) {
        return;
    }
    File f = m_fileChooser.getSelectedFile();
    BufferedWriter out = null;
    try {
        if (f.exists()) {
            int res = JOptionPane.showConfirmDialog(
                    this,Resource.get("existFile"),
                    "Already Exists Message",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (res != 0) {
                return;
            }
        }

        //IOHelper.saveTxtFile(f, ta_messages.getText(), false);
        out = new BufferedWriter(new FileWriter(f));
        out.write(talogging.getText());
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        Utilities.close(out);
    }
}//GEN-LAST:event_SaveMessageToFileButtonActionPerformed

private void formWindowDeactivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowDeactivated
    //Called when the mainWindow is closed
    if (Settings.getInstance().readBool("gui.saveWindowPos"))
        Settings.getInstance().writeWindowPos("logwindow", getLocation());
}//GEN-LAST:event_formWindowDeactivated
    /**
     * Clears only the messages that are displayed in the textarea.
     */
    public synchronized void clearScreenMessages()
    {
        talogging.setText("");
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ClearMessageButton;
    private javax.swing.JButton SaveMessageToFileButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea talogging;
    // End of variables declaration//GEN-END:variables
 /**
     * Private inner class. Filter to redirect the data to the textarea.
     */
    private final class JTextAreaOutStream extends FilterOutputStream {

        
        /**
         * Constructor.
         * <p>
         * @param aStream   The <code>OutputStream</code>.
         */
        public JTextAreaOutStream( OutputStream aStream )
        {
            super( aStream );
        }


        /**
         * Writes the messages.
         * <p>
         * @param b     The message in a <code>byte[]</code> array.
         * <p>
         * @throws IOException
         */
        @Override
		public synchronized void write( byte b[] )
            throws IOException
        {
            String s = new String( b );
            appendMessage( s );
            flushTextArea();
        }


        /**
         * Writes the messages.
         * <p>
         * @param b     The message in a <code>byte[]</code> array.
         * @param off   The offset.
         * @param len   Length.
         * <p>
         * @throws IOException
         */
        @Override
		public synchronized void write( byte b[], int off, int len )
            throws IOException
        {
            String s = new String(b, off, len);
            appendMessage( s );
            flushTextArea();
        }

        
        /**
         * Appends a message to the textarea and the 
         * <p>
         * @param s     The message.
         */
        private synchronized void appendMessage(String s)
        {
            talogging.append( s );
        }


        private synchronized void flushTextArea()
        {
            int len = talogging.getText().length();

            // Always scroll down to the last line
            talogging.setCaretPosition( len );

            // if we have set a maximum characters limit and 
            // we have exceeded that limit, clear the messages
            if( m_maxChars>0 && len > m_maxChars )
            {
                clearScreenMessages();
            }
        }

    }
}

