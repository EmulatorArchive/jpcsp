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

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jpcsp.log;

import java.awt.event.ItemEvent;
import java.io.PrintStream;
import javax.swing.DefaultComboBoxModel;
import javax.swing.UIManager;
import jpcsp.settings.Settings;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class LogWindow extends javax.swing.JFrame {

    private static String confFile = "LogSettings.xml";
    private final String[] loglevels = {"ALL", "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "OFF"};

    public LogWindow() {
        initComponents();

        TextPaneAppender textPaneAppender = (TextPaneAppender) Logger.getRootLogger().getAppender("JpcspAppender");
        if (textPaneAppender != null) {
            textPaneAppender.setTextPane(tpLog);
        }

        getLogLevelFromConfig();
        setSize(Settings.getInstance().readWindowSize("logwindow", 500, 300));
    }

    public static void setConfXMLFile(String path) {
        confFile = path;
    }

    public void clearScreenMessages() {
        synchronized (tpLog) {
            tpLog.setText("");
        }
    }

    private void getLogLevelFromConfig() {
        final Logger rootLogger = Logger.getRootLogger();
        Level lvlConfig = rootLogger.getLevel();

        if (lvlConfig.equals(Level.ALL)) {
            cmbLogLevel.setSelectedIndex(0);
        }
        if (lvlConfig.equals(Level.TRACE)) {
            cmbLogLevel.setSelectedIndex(1);
        }
        if (lvlConfig.equals(Level.DEBUG)) {
            cmbLogLevel.setSelectedIndex(2);
        }
        if (lvlConfig.equals(Level.INFO)) {
            cmbLogLevel.setSelectedIndex(3);
        }
        if (lvlConfig.equals(Level.WARN)) {
            cmbLogLevel.setSelectedIndex(4);
        }
        if (lvlConfig.equals(Level.ERROR)) {
            cmbLogLevel.setSelectedIndex(5);
        }
        if (lvlConfig.equals(Level.FATAL)) {
            cmbLogLevel.setSelectedIndex(6);
        }
        if (lvlConfig.equals(Level.OFF)) {
            cmbLogLevel.setSelectedIndex(7);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.setProperty("log4j.properties", confFile);
        DOMConfigurator.configure(confFile);

        System.setOut(new PrintStream(new LoggingOutputStream(Logger.getLogger("sysout"), Level.INFO)));
        new LogWindow().setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();
        lblLevel = new javax.swing.JLabel();
        cmbLogLevel = new javax.swing.JComboBox();
        btnClear = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Logger");
        setMinimumSize(new java.awt.Dimension(400, 300));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowDeactivated(java.awt.event.WindowEvent evt) {
                formWindowDeactivated(evt);
            }
        });

        scrollPane.setViewportView(tpLog);

        lblLevel.setText("Log Level:");

        cmbLogLevel.setModel(new DefaultComboBoxModel(loglevels));
        cmbLogLevel.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbLogLevelItemStateChanged(evt);
            }
        });

        btnClear.setText("Clear");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPane)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblLevel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbLogLevel, 0, 209, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnClear)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblLevel)
                        .addComponent(cmbLogLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnClear, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowDeactivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowDeactivated
        if (Settings.getInstance().readBool("gui.saveWindowPos")) {
            Settings.getInstance().writeWindowPos("logwindow", getLocation());
            Settings.getInstance().writeWindowSize("logwindow", getSize());
        }
    }//GEN-LAST:event_formWindowDeactivated

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearScreenMessages();
    }//GEN-LAST:event_btnClearActionPerformed

    private void cmbLogLevelItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbLogLevelItemStateChanged
        final Logger rootLogger = Logger.getRootLogger();
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            if (evt.getItem().equals(loglevels[0])) {
                rootLogger.setLevel(Level.ALL);
            }
            if (evt.getItem().equals(loglevels[1])) {
                rootLogger.setLevel(Level.TRACE);
            }
            if (evt.getItem().equals(loglevels[2])) {
                rootLogger.setLevel(Level.DEBUG);
            }
            if (evt.getItem().equals(loglevels[3])) {
                rootLogger.setLevel(Level.INFO);
            }
            if (evt.getItem().equals(loglevels[4])) {
                rootLogger.setLevel(Level.WARN);
            }
            if (evt.getItem().equals(loglevels[5])) {
                rootLogger.setLevel(Level.ERROR);
            }
            if (evt.getItem().equals(loglevels[6])) {
                rootLogger.setLevel(Level.FATAL);
            }
            if (evt.getItem().equals(loglevels[7])) {
                rootLogger.setLevel(Level.OFF);
            }
        }
    }//GEN-LAST:event_cmbLogLevelItemStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClear;
    private javax.swing.JComboBox cmbLogLevel;
    private javax.swing.JLabel lblLevel;
    private javax.swing.JScrollPane scrollPane;
    private final javax.swing.JTextPane tpLog = new javax.swing.JTextPane();
    // End of variables declaration//GEN-END:variables
}
