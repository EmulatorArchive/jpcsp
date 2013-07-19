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
package jpcsp.Debugger.FileLogger;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import jpcsp.Emulator;
import jpcsp.HLE.Modules;
import jpcsp.HLE.VFS.IVirtualFile;
import jpcsp.HLE.modules150.IoFileMgrForUser.IIoListener;
import jpcsp.State;
import jpcsp.WindowPropSaver;
import jpcsp.filesystems.SeekableDataInput;
import jpcsp.filesystems.umdiso.UmdIsoReader;
import jpcsp.settings.Settings;
import jpcsp.util.Constants;

/**
 *
 * @author fiveofhearts
 */
public class FileLoggerFrame extends javax.swing.JFrame implements Runnable, IIoListener {
    
    private static final long serialVersionUID = 8455039521164613143L;
    private FileHandleModel fileHandleModel;
    private FileCommandModel fileCommandModel;
    private Thread refreshThread;
    private volatile boolean dirty;
    private volatile boolean sortRequired;

    /**
     * Creates new form FileLoggerFrame
     */
    public FileLoggerFrame() {
        fileHandleModel = new FileHandleModel();
        fileCommandModel = new FileCommandModel();
        
        initComponents();
        postInit();
        
        refreshThread = new Thread(this, "FileLogger");
        refreshThread.start();
        
        if (Settings.getInstance().readBool("emu.debug.enablefilelogger")) {
            cbFileTrace.setSelected(true);
            Modules.IoFileMgrForUserModule.registerIoListener(this);
        }
        
        WindowPropSaver.loadWindowProperties(this);
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        
        synchronized (getInstance()) {
            if (!dirty) {
                dirty = true;
                getInstance().notify();
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        copyItem = new javax.swing.JMenuItem();
        saveAsItem = new javax.swing.JMenuItem();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        commandLogTable = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        fileHandleTable = new javax.swing.JTable();
        cbFileTrace = new javax.swing.JCheckBox();

        copyItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("jpcsp/languages/jpcsp"); // NOI18N
        copyItem.setText(bundle.getString("FileLoggerFrame.copyItem.text")); // NOI18N
        copyItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyItemActionPerformed(evt);
            }
        });
        jPopupMenu1.add(copyItem);

        saveAsItem.setText(bundle.getString("FileLoggerFrame.saveAsItem.text")); // NOI18N
        saveAsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsItemActionPerformed(evt);
            }
        });
        jPopupMenu1.add(saveAsItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(bundle.getString("FileLoggerFrame.title")); // NOI18N
        setMinimumSize(new java.awt.Dimension(400, 200));

        jSplitPane1.setDividerLocation(100);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setMinimumSize(new java.awt.Dimension(179, 100));

        commandLogTable.setModel(fileCommandModel);
        commandLogTable.setInheritsPopupMenu(true);
        commandLogTable.setMinimumSize(new java.awt.Dimension(200, 100));
        commandLogTable.setName(bundle.getString("FileLoggerFrame.commandLogTable.name")); // NOI18N
        commandLogTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tableMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tableMouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(commandLogTable);

        jSplitPane1.setBottomComponent(jScrollPane1);

        fileHandleTable.setModel(fileHandleModel);
        fileHandleTable.setInheritsPopupMenu(true);
        fileHandleTable.setMinimumSize(new java.awt.Dimension(200, 100));
        fileHandleTable.setName(bundle.getString("FileLoggerFrame.fileHandleTable.name")); // NOI18N
        fileHandleTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tableMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tableMouseReleased(evt);
            }
        });
        jScrollPane2.setViewportView(fileHandleTable);

        jSplitPane1.setTopComponent(jScrollPane2);

        cbFileTrace.setText(bundle.getString("FileLoggerFrame.cbFileTrace.text")); // NOI18N
        cbFileTrace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbFileTraceActionPerformed(evt);
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
                        .addComponent(cbFileTrace)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbFileTrace)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMousePressed
        if (evt.isPopupTrigger()) {
            jPopupMenu1.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_tableMousePressed
    
    private void tableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseReleased
        if (evt.isPopupTrigger()) {
            jPopupMenu1.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_tableMouseReleased
    
    private void copyItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyItemActionPerformed
        JTable source = (JTable) ((JPopupMenu) ((JMenuItem) evt.getSource()).getParent()).getInvoker();
        
        ActionEvent ae = new ActionEvent(source, ActionEvent.ACTION_PERFORMED, "");
        source.getActionMap().get("copy").actionPerformed(ae);
    }//GEN-LAST:event_copyItemActionPerformed
    
    private void saveAsItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsItemActionPerformed
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("jpcsp/languages/jpcsp");
        final JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(bundle.getString("FileLoggerFrame.strSaveTable.text"));
        fc.setSelectedFile(new File(State.discId + "_fileio.txt"));
        fc.setCurrentDirectory(new java.io.File("."));
        fc.addChoosableFileFilter(Constants.fltTextFiles);
        fc.setFileFilter(Constants.fltTextFiles);
        
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (f.exists()) {
                
                int rc = JOptionPane.showConfirmDialog(
                        this,
                        bundle.getString("ConsoleWindow.strFileExists.text"),
                        bundle.getString("ConsoleWindow.strFileExistsTitle.text"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                
                if (rc != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            try {
                JTable source = (JTable) ((JPopupMenu) ((JMenuItem) evt.getSource()).getParent()).getInvoker();
                String data = "";

                // list column headers
                for (int j = 0; j < source.getColumnCount(); j++) {
                    data += source.getColumnName(j) + ";";
                }
                // strip last semicolon and put a newline there instead
                data = data.substring(0, data.length() - 1) + System.getProperty("line.separator");

                // list table content
                for (int i = 0; i < source.getRowCount(); i++) {
                    for (int j = 0; j < source.getColumnCount(); j++) {
                        
                        data += source.getModel().getValueAt(i, j) + ";";
                    }
                    // strip last semicolon and put a newline there instead
                    data = data.substring(0, data.length() - 1) + System.getProperty("line.separator");
                }
                
                FileWriter os = new FileWriter(f);
                os.write(data);
                os.close();
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(
                        this,
                        bundle.getString("FileLoggerFrame.strSaveFailed.text") + ioe.getLocalizedMessage());
            }
        }
    }//GEN-LAST:event_saveAsItemActionPerformed
    
    private FileLoggerFrame getInstance() {
        return this;
    }
    
    private void cbFileTraceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbFileTraceActionPerformed
        if (cbFileTrace.isSelected()) {
            Modules.IoFileMgrForUserModule.registerIoListener(this);
            Settings.getInstance().writeBool("emu.debug.enablefilelogger", true);
        } else {
            Modules.IoFileMgrForUserModule.unregisterIoListener(this);
            Settings.getInstance().writeBool("emu.debug.enablefilelogger", false);
        }
    }//GEN-LAST:event_cbFileTraceActionPerformed

    // TODO does fireTableDataChanged need to be in the swing thread?
    // if not we could just call fireTableDataChanged(); from the logging functions
    @Override
    public void run() {
        Runnable refresher = new Runnable() {
            @Override
            public void run() {
                if (sortRequired) {
                    sortRequired = false;
                    sortLists();
                }

                // Scroll to bottom of the tables
                int max = jScrollPane1.getVerticalScrollBar().getMaximum();
                jScrollPane1.getVerticalScrollBar().setValue(max);
                
                max = jScrollPane2.getVerticalScrollBar().getMaximum();
                jScrollPane2.getVerticalScrollBar().setValue(max);

                // Tell the tables to redraw
                fileHandleModel.fireTableDataChanged();
                fileCommandModel.fireTableDataChanged();
            }
        };
        
        while (true) {
            try {
                synchronized (this) {
                    while (!dirty) {
                        wait();
                    }
                    dirty = false;
                }
                
                if (getInstance().isVisible()) {
                    SwingUtilities.invokeAndWait(refresher);
                }

                // Cap update frequency
                Thread.sleep(200);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Renders closed files in gray.
     */
    public class FileHandleRenderer extends DefaultTableCellRenderer {
        
        private static final long serialVersionUID = -792377736132676194L;
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            c.setForeground(Color.black);
            
            if (fileHandleList != null) {
                FileHandleInfo info = fileHandleList.get(row);
                if (!info.isOpen()) {
                    c.setForeground(Color.gray);
                }
            }
            
            return c;
        }
    }
    
    private class FileHandleModel extends AbstractTableModel {
        
        private static final long serialVersionUID = -109193689444035593L;
        
        @Override
        public int getColumnCount() {
            return 4;
        }
        
        @Override
        public int getRowCount() {
            if (fileHandleList != null) {
                return fileHandleList.size();
            }
            return 0;
        }
        
        @Override
        public String getColumnName(int columnIndex) {
            java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("jpcsp/languages/jpcsp");
            switch (columnIndex) {
                case 0:
                    return bundle.getString("FileLoggerFrame.strFileID.text");
                case 1:
                    return bundle.getString("FileLoggerFrame.strFileName.text");
                case 2:
                    return bundle.getString("FileLoggerFrame.strRead.text");
                case 3:
                    return bundle.getString("FileLoggerFrame.strWrite.text");
                default:
                    throw new IllegalArgumentException("invalid column index");
            }
        }
        
        @Override
        public Object getValueAt(int row, int col) {
            FileHandleInfo info = fileHandleList.get(row);
            if (info != null) {
                switch (col) {
                    case 0:
                        return String.format("0x%04X", info.fd);
                    case 1:
                        return info.filename;
                    case 2:
                        return info.bytesRead;
                    case 3:
                        return info.bytesWritten;
                }
            }
            return null;
        }
    };
    
    private class FileCommandModel extends AbstractTableModel {
        
        private static final long serialVersionUID = -5088674695489235024L;
        
        @Override
        public int getColumnCount() {
            return 6;
        }
        
        @Override
        public int getRowCount() {
            if (fileCommandList != null) {
                return fileCommandList.size();
            }
            return 0;
        }
        
        @Override
        public String getColumnName(int columnIndex) {
            java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("jpcsp/languages/jpcsp");
            switch (columnIndex) {
                case 0:
                    return bundle.getString("FileLoggerFrame.strThreadID.text");
                case 1:
                    return bundle.getString("FileLoggerFrame.strThreadName.text");
                case 2:
                    return bundle.getString("FileLoggerFrame.strFileID.text");
                case 3:
                    return bundle.getString("FileLoggerFrame.strCommand.text");
                case 4:
                    return bundle.getString("FileLoggerFrame.strResult.text");
                case 5:
                    return bundle.getString("FileLoggerFrame.strParameters.text");
                default:
                    throw new IllegalArgumentException("invalid column index");
            }
        }
        
        @Override
        public Object getValueAt(int row, int col) {
            FileCommandInfo info = fileCommandList.get(row);
            if (info != null) {
                switch (col) {
                    case 0:
                        return String.format("0x%08X", info.threadId);
                    case 1:
                        return info.threadName;
                    case 2:
                        return (info.hasFd) ? String.format("0x%04X", info.fd) : "";
                    case 3:
                        return (info.occurences == 1) ? info.command : info.command + " " + info.occurences + "x";
                    case 4:
                        return String.format("0x%08X", info.result);
                    case 5:
                        return info.parameters;
                }
            }
            return null;
        }
    };
    
    final public void postInit() {
        TableColumnModel columns;

        // We want the middle column to be the widest
        columns = fileHandleTable.getColumnModel();
        columns.getColumn(0).setPreferredWidth(75);
        columns.getColumn(1).setPreferredWidth(500);
        columns.getColumn(2).setPreferredWidth(75);
        columns.getColumn(3).setPreferredWidth(75);
        
        fileHandleTable.setDefaultRenderer(Object.class, new FileHandleRenderer());
        
        columns = commandLogTable.getColumnModel();
        columns.getColumn(0).setPreferredWidth(75);
        columns.getColumn(1).setPreferredWidth(90);
        columns.getColumn(2).setPreferredWidth(50);
        columns.getColumn(4).setPreferredWidth(75);
        columns.getColumn(5).setPreferredWidth(275);
        
        resetLogging();

        // uncomment this for testing purposes
        // test();
    }
    
    public void test() {
        System.err.println("test start");
        
        resetLogging();

        // file handle table
        sceIoOpen(1, 0x08800000, "test1.txt", 0xFF, 0xFF, "rw");
        sceIoOpen(2, 0x08800000, "test2.txt", 0xFF, 0xFF, "rw");
        sceIoOpen(3, 0x08800000, System.currentTimeMillis() + ".txt", 0xFF, 0xFF, "rw");
        sceIoClose(0, 1);
        sceIoClose(0, 2);
        sceIoOpen(1, 0x08800000, "test1.txt", 0xFF, 0xFF, "rw");

        // file command table
        sceIoRead(0x0, 1, 0x08800000, 0x400, 0x0, 0, null, null);
        
        System.err.println("test done");
    }
    
    private class FileCommandInfo {
        
        public final boolean hasFd;
        public final int threadId;
        public final String threadName;
        public final int fd;
        public final String command;
        public final int result;
        public final String parameters;
        public int occurences;
        
        private FileCommandInfo(boolean hasFd, int fd, String command,
                int result, String parameters) {
            this.hasFd = hasFd;
            
            threadId = Modules.ThreadManForUserModule.getCurrentThreadID();
            threadName = Modules.ThreadManForUserModule.getThreadName(threadId);
            this.fd = fd;
            this.command = command;
            this.result = result;
            this.parameters = parameters;
            occurences = 1;
            
            synchronized (getInstance()) {
                if (!dirty) {
                    dirty = true;
                    getInstance().notify();
                }
            }
        }

        /**
         * Example: 0x1001, "close", 0x0, ""
         */
        public FileCommandInfo(int fd, String command, int result,
                String parameters) {
            this(true, fd, command, result, parameters);
        }

        /**
         * Example: "open", 0x1001, "path='test.txt' flags=0xFF perm=0777"
         */
        public FileCommandInfo(String command, int result, String parameters) {
            this(false, -2, command, result, parameters);
        }
        
        @Override
        public boolean equals(Object _obj) {
            if (_obj instanceof FileCommandInfo) {
                FileCommandInfo obj = (FileCommandInfo) _obj;
                return threadId == obj.threadId
                        && fd == obj.fd
                        && command.equals(obj.command)
                        && result == obj.result
                        && parameters.equals(obj.parameters);
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 43 * hash + threadId;
            hash = 43 * hash + fd;
            hash = 43 * hash + (command != null ? command.hashCode() : 0);
            hash = 43 * hash + result;
            hash = 43 * hash + (parameters != null ? parameters.hashCode() : 0);
            return hash;
        }
    }
    // Emu interface
    private HashMap<Integer, FileHandleInfo> fileHandleIdMap;
    private List<FileHandleInfo> fileHandleList; // Cached sorted version of fileHandleIdMap
    private List<FileCommandInfo> fileCommandList;
    
    public synchronized void resetLogging() {
        fileHandleIdMap = new HashMap<Integer, FileHandleInfo>();
        fileHandleList = new LinkedList<FileHandleInfo>();
        
        fileCommandList = new LinkedList<FileCommandInfo>();
        
        if (!dirty) {
            dirty = true;
            getInstance().notify();
        }
    }
    
    private void sortLists() {
        // File handles
        Collection<FileHandleInfo> c = fileHandleIdMap.values();
        fileHandleList = new LinkedList<FileHandleInfo>(c);
        Collections.sort(fileHandleList);
    }
    /**
     * Handles repeated commands
     */
    private FileCommandInfo lastFileCommand;
    
    private void logFileCommand(FileCommandInfo info) {
        if (lastFileCommand != null
                && info.equals(lastFileCommand)) {
            lastFileCommand.occurences++;
        } else {
            fileCommandList.add(info);
            lastFileCommand = info;
        }
    }
    
    @Override
    public void sceIoSync(int result, int device_addr, String device, int unknown) {
        logFileCommand(new FileCommandInfo(
                "sync", result,
                String.format("device=0x%08X('%s') unknown=0x%08X",
                device_addr, device, unknown)));
    }
    
    @Override
    public void sceIoPollAsync(int result, int uid, int res_addr) {
        logFileCommand(new FileCommandInfo(
                uid, "poll async", result,
                String.format("result=0x%08X", res_addr)));
    }
    
    @Override
    public void sceIoWaitAsync(int result, int uid, int res_addr) {
        logFileCommand(new FileCommandInfo(
                uid, "wait async", result,
                String.format("result=0x%08X", res_addr)));
    }
    
    @Override
    public void sceIoOpen(int result, int filename_addr, String filename, int flags, int permissions, String mode) {
        // File handle list
        if (result >= 0) {
            FileHandleInfo info = new FileHandleInfo(result, filename);
            fileHandleIdMap.put(result, info);
            sortRequired = true;
        }
        
        String filelog = String.format("path=0x%08X('%s')",
                filename_addr, filename);
        if (filename.startsWith("disc0:/sce_lbn")) {
            // try to resolve LBA addressing if possible
            UmdIsoReader iso = Modules.IoFileMgrForUserModule.getIsoReader();
            if (iso != null) {
                String filePath = filename;
                filePath = filePath.substring(14); // length of "disc0:/sce_lba"
                int sep = filePath.indexOf("_size");
                int fileStart = Integer.decode(filePath.substring(0, sep));
                String resolved = iso.getFileName(fileStart);
                if (resolved != null) {
                    filelog = String.format("path=0x%08X('%s', '%s')",
                            filename_addr, filename, resolved);
                }
            }
        }
        filelog += String.format(" flags=0x%04X, permissions=0x%04X(%s)",
                flags, permissions, mode);

        // File Command list
        logFileCommand(new FileCommandInfo("open", result, filelog));
    }
    
    @Override
    public void sceIoClose(int result, int uid) {
        // File handle list
        if (result >= 0) {
            FileHandleInfo info = fileHandleIdMap.get(uid);
            if (info != null) {
                info.isOpen(false);
            }
        }

        // File Command list
        logFileCommand(new FileCommandInfo(uid, "close", result, ""));
    }
    
    @Override
    public void sceIoWrite(int result, int uid, int data_addr, int size, int bytesWritten) {
        FileHandleInfo info = fileHandleIdMap.get(uid);
        if (result >= 0 && info != null) {
            info.bytesWritten += bytesWritten;
        }
        
        logFileCommand(new FileCommandInfo(
                uid, "write", result,
                String.format("data=0x%08X size=0x%08X",
                data_addr, size)));
    }
    
    @Override
    public void sceIoRead(int result, int uid, int data_addr, int size, int bytesRead, long position, SeekableDataInput dataInput, IVirtualFile vFile) {
        FileHandleInfo info = fileHandleIdMap.get(uid);
        if (result >= 0 && info != null) {
            info.bytesRead += bytesRead;
        }
        
        logFileCommand(new FileCommandInfo(
                uid, "read", result,
                String.format("data=0x%08X size=0x%08X",
                data_addr, size)));
    }
    
    @Override
    public void sceIoCancel(int result, int uid) {
        logFileCommand(new FileCommandInfo(uid, "cancel", result, ""));
    }
    
    private String getWhenceName(int whence) {
        switch (whence) {
            case jpcsp.HLE.modules150.IoFileMgrForUser.PSP_SEEK_SET:
                return whence + "(set)";
            case jpcsp.HLE.modules150.IoFileMgrForUser.PSP_SEEK_CUR:
                return whence + "(cur)";
            case jpcsp.HLE.modules150.IoFileMgrForUser.PSP_SEEK_END:
                return whence + "(end)";
            default:
                return "" + whence;
        }
    }
    
    @Override
    public void sceIoSeek32(int result, int uid, int offset, int whence) {
        logFileCommand(new FileCommandInfo(
                uid, "seek32", result,
                String.format("offset=0x%08X whence=%s",
                offset, getWhenceName(whence))));
    }
    
    @Override
    public void sceIoSeek64(long result, int uid, long offset, int whence) {
        logFileCommand(new FileCommandInfo(
                uid, "seek64", (int) result, // HACK back to 32bit result
                String.format("offset=0x%08X whence=%s", offset, getWhenceName(whence))));
    }
    
    @Override
    public void sceIoMkdir(int result, int path_addr, String path, int permissions) {
        logFileCommand(new FileCommandInfo(
                "mkdir", result,
                String.format("path=0x%08X('%s') permissions=%04X", path_addr, path, permissions)));
    }
    
    @Override
    public void sceIoRmdir(int result, int path_addr, String path) {
        logFileCommand(new FileCommandInfo(
                "rmdir", result,
                String.format("path=0x%08X('%s')", path_addr, path)));
    }
    
    @Override
    public void sceIoChdir(int result, int path_addr, String path) {
        logFileCommand(new FileCommandInfo(
                "chdir", result,
                String.format("path=0x%08X('%s')", path_addr, path)));
    }
    
    @Override
    public void sceIoDopen(int result, int path_addr, String path) {
        logFileCommand(new FileCommandInfo(
                "dopen", result,
                String.format("path=0x%08X('%s')", path_addr, path)));
    }
    
    @Override
    public void sceIoDread(int result, int uid, int dirent_addr) {
        logFileCommand(new FileCommandInfo(uid, "dread", result, String.format("dirent=0x%08X", dirent_addr)));
    }
    
    @Override
    public void sceIoDclose(int result, int uid) {
        logFileCommand(new FileCommandInfo(uid, "dclose", result, ""));
    }
    
    @Override
    public void sceIoDevctl(int result, int device_addr, String device, int cmd,
            int indata_addr, int inlen, int outdata_addr, int outlen) {
        logFileCommand(new FileCommandInfo(
                "devctl", result,
                String.format("device=0x%08X('%s') cmd=0x%08X indata=0x%08X inlen=0x%08X outdata=0x%08X outlen=0x%08X",
                device_addr, device, cmd, indata_addr, inlen, outdata_addr, outlen)));
    }
    
    @Override
    public void sceIoIoctl(int result, int uid, int cmd,
            int indata_addr, int inlen, int outdata_addr, int outlen) {
        logFileCommand(new FileCommandInfo(
                uid, "ioctl", result,
                String.format("cmd=0x%08X indata=0x%08X inlen=0x%08X outdata=0x%08X outlen=0x%08X",
                cmd, indata_addr, inlen, outdata_addr, outlen)));
    }
    
    @Override
    public void sceIoAssign(int result, int dev1_addr, String dev1,
            int dev2_addr, String dev2, int dev3_addr, String dev3,
            int mode, int unk1, int unk2) {
        logFileCommand(new FileCommandInfo(
                "assign", result,
                String.format("dev1=0x%08X('%s') dev2=0x%08X('%s') dev3=0x%08X('%s') mode=0x%08X unk1=0x%08X unk2=0x%08X",
                dev1_addr, dev1, dev2_addr, dev2, dev3_addr, dev3, mode, unk1, unk2)));
    }
    
    @Override
    public void sceIoGetStat(int result, int path_addr, String path, int stat_addr) {
        logFileCommand(new FileCommandInfo(
                "stat", result,
                String.format("path=0x%08X('%s') stat=0x%08X", path_addr, path, stat_addr)));
    }
    
    @Override
    public void sceIoRemove(int result, int path_addr, String path) {
        logFileCommand(new FileCommandInfo(
                "remove", result,
                String.format("path=0x%08X('%s')", path_addr, path)));
    }
    
    @Override
    public void sceIoChstat(int result, int path_addr, String path, int stat_addr, int bits) {
        logFileCommand(new FileCommandInfo(
                "chstat", result,
                String.format("path=0x%08X('%s') stat=0x%08X bits=0x%08X",
                path_addr, path, stat_addr, bits)));
    }
    
    @Override
    public void sceIoRename(int result, int path_addr, String path, int new_path_addr, String newpath) {
        logFileCommand(new FileCommandInfo(
                "rename", result,
                String.format("path=0x%08X('%s') newpath=0x%08X('%s')",
                path_addr, path, new_path_addr, newpath)));
    }
    
    @Override
    public void dispose() {
        Emulator.getMainGUI().endWindowDialog();
        super.dispose();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbFileTrace;
    private javax.swing.JTable commandLogTable;
    private javax.swing.JMenuItem copyItem;
    private javax.swing.JTable fileHandleTable;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JMenuItem saveAsItem;
    // End of variables declaration//GEN-END:variables
}
