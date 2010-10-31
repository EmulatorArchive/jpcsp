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

package jpcsp.GUI;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.MutableComboBoxModel;
import javax.swing.GroupLayout.Alignment;

import jpcsp.Resource;
import jpcsp.Settings;

import com.jidesoft.swing.FolderChooser;

/**
 *
 * @author  shadow
 */
public class SettingsGUI extends javax.swing.JFrame {
	private static final long serialVersionUID = -732715495873159718L;
    
    /** Creates new form SettingsGUI */
    public SettingsGUI() {
        initComponents();
        
        boolean enabled = Settings.getInstance().readBool("emu.pbpunpack");
        pbpunpackcheck.setSelected(enabled);
        
        enabled = Settings.getInstance().readBool("gui.saveWindowPos");
        saveWindowPosCheck.setSelected(enabled);
                
        enabled = Settings.getInstance().readBool("emu.compiler");
        compilerCheck.setSelected(enabled);
        
        enabled = Settings.getInstance().readBool("emu.profiler");
        profilerCheck.setSelected(enabled);
        
        enabled = Settings.getInstance().readBool("emu.useshaders");
        shadersCheck.setSelected(enabled);
        
        enabled = Settings.getInstance().readBool("emu.useGeometryShader");
        geometryShaderCheck.setSelected(enabled);
        
        enabled = Settings.getInstance().readBool("emu.debug.enablefilelogger");
        filelogCheck.setSelected(enabled);
        
        enabled = Settings.getInstance().readBool("emu.savedataSizes");
        savedatasizesCheck.setSelected(enabled);

        int language = Settings.getInstance().readInt("emu.impose.language");
        languageBox.setSelectedIndex(language);

        enabled = Settings.getInstance().readBool("emu.disablevbo");
        disableVBOCheck.setSelected(enabled);

        enabled = Settings.getInstance().readBool("emu.onlyGEGraphics");
        onlyGEGraphicsCheck.setSelected(enabled);

        enabled = Settings.getInstance().readBool("emu.useConnector");
        useConnector.setSelected(enabled);

        enabled = Settings.getInstance().readBool("emu.useMediaEngine");
        useMediaEngine.setSelected(enabled);

        enabled = Settings.getInstance().readBool("emu.useVertexCache");
        useVertexCache.setSelected(enabled);

        enabled = Settings.getInstance().readBool("emu.ignoreInvalidMemoryAccess");
        invalidMemoryCheck.setSelected(enabled);
        
        enabled = Settings.getInstance().readBool("emu.disablesceAudio");
        DisableSceAudioCheck.setSelected(enabled);
        
        enabled = Settings.getInstance().readBool("emu.ignoreaudiothreads");
        IgnoreAudioThreadsCheck.setSelected(enabled);
        
        enabled = Settings.getInstance().readBool("emu.disableblockingaudio");
        disableBlockingAudioCheck.setSelected(enabled);
        
        enabled = Settings.getInstance().readBool("emu.ignoreUnmappedImports");
        ignoreUnmappedImports.setSelected(enabled);
        
        enabled = Settings.getInstance().readBool("emu.umdbrowser");
        if(enabled)
            umdBrowser.setSelected(true);
        else
            ClassicOpenDialogumd.setSelected(true);
        
        umdpath.setText(Settings.getInstance().readString("emu.umdpath"));
    }

    private ComboBoxModel makeLanguageComboBoxModel() {
        MutableComboBoxModel comboBox = new DefaultComboBoxModel();
        comboBox.addElement(Resource.get("japanese"));
        comboBox.addElement(Resource.get("english"));
        comboBox.addElement(Resource.get("french"));
        comboBox.addElement(Resource.get("spanish"));
        comboBox.addElement(Resource.get("german"));
        comboBox.addElement(Resource.get("italian"));
        comboBox.addElement(Resource.get("dutch"));
        comboBox.addElement(Resource.get("portuguese"));
        comboBox.addElement(Resource.get("russian"));
        comboBox.addElement(Resource.get("korean"));
        comboBox.addElement(Resource.get("traditionalChinese"));
        comboBox.addElement(Resource.get("simplifiedChinese"));

        return comboBox;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jButtonOK = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        generalPanel = new javax.swing.JPanel();
        pbpunpackcheck = new javax.swing.JCheckBox();
        saveWindowPosCheck = new javax.swing.JCheckBox();
        compilerCheck = new javax.swing.JCheckBox();
        profilerCheck = new javax.swing.JCheckBox();
        umdBrowser = new javax.swing.JRadioButton();
        ClassicOpenDialogumd = new javax.swing.JRadioButton();
        umdpath = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        filelogCheck = new javax.swing.JCheckBox();
        savedatasizesCheck = new javax.swing.JCheckBox();
        languageBox = new JComboBox();
        languageLabel = new JLabel();
        VideoPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        disableVBOCheck = new javax.swing.JCheckBox();
        onlyGEGraphicsCheck = new javax.swing.JCheckBox();
        useVertexCache = new javax.swing.JCheckBox();
        shadersCheck = new javax.swing.JCheckBox();
        geometryShaderCheck = new javax.swing.JCheckBox();
        AudioPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        IgnoreAudioThreadsCheck = new javax.swing.JCheckBox();
        disableBlockingAudioCheck = new javax.swing.JCheckBox();
        DisableSceAudioCheck = new javax.swing.JCheckBox();
        MemoryPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        invalidMemoryCheck = new javax.swing.JCheckBox();
        ignoreUnmappedImports = new javax.swing.JCheckBox();
        MiscPanel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        useMediaEngine = new javax.swing.JCheckBox();
        useConnector = new javax.swing.JCheckBox();

        setTitle("Configuration");
        setResizable(false);

        jButtonOK.setText(Resource.get("ok"));
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });

        jButtonCancel.setText(Resource.get("cancel"));
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        pbpunpackcheck.setText(Resource.get("unpackpbp"));

        saveWindowPosCheck.setText(Resource.get("saveposition"));

        compilerCheck.setText(Resource.get("compiler"));

        profilerCheck.setText(Resource.get("outputprofiler"));

        buttonGroup1.add(umdBrowser);
        umdBrowser.setText(Resource.get("useUMDBrowser"));

        buttonGroup1.add(ClassicOpenDialogumd);
        ClassicOpenDialogumd.setText(Resource.get("useclassicUMD"));

        umdpath.setEditable(false);

        jLabel1.setText(Resource.get("UMDpath"));

        jButton1.setText("...");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        filelogCheck.setText(Resource.get("enablefileIO"));
        savedatasizesCheck.setText(Resource.get("savedatasizes"));

        languageBox.setModel(makeLanguageComboBoxModel());
        languageLabel.setText(Resource.get("language"));

        javax.swing.GroupLayout generalPanelLayout = new javax.swing.GroupLayout(generalPanel);
        generalPanel.setLayout(generalPanelLayout);
        generalPanelLayout.setHorizontalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(generalPanelLayout.createSequentialGroup()
                        .addComponent(ClassicOpenDialogumd, javax.swing.GroupLayout.DEFAULT_SIZE, 665, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(generalPanelLayout.createSequentialGroup()
                        .addComponent(umdBrowser, javax.swing.GroupLayout.DEFAULT_SIZE, 665, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(generalPanelLayout.createSequentialGroup()
                        .addComponent(filelogCheck)
                        .addContainerGap())
                    .addGroup(generalPanelLayout.createSequentialGroup()
                        .addComponent(savedatasizesCheck)
                        .addContainerGap())
                    .addGroup(generalPanelLayout.createSequentialGroup()
                        .addComponent(languageLabel)
                        .addGap(5)
                        .addComponent(languageBox, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(generalPanelLayout.createSequentialGroup()
                        .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, generalPanelLayout.createSequentialGroup()
                                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(generalPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(umdpath, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(pbpunpackcheck, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(saveWindowPosCheck, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(compilerCheck, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE))
                                .addGap(91, 91, 91))
                            .addComponent(profilerCheck, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(304, 304, 304))))
        );
        generalPanelLayout.setVerticalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(pbpunpackcheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveWindowPosCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(compilerCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(profilerCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filelogCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(savedatasizesCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(generalPanelLayout.createParallelGroup(Alignment.BASELINE)
                        .addComponent(languageLabel)
                        .addComponent(languageBox))
                .addGap(51, 51, 51)
                .addComponent(umdBrowser)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ClassicOpenDialogumd)
                .addGap(17, 17, 17)
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(umdpath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap(51, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(Resource.get("general"), generalPanel);

        disableVBOCheck.setText(Resource.get("disablevbo"));

        onlyGEGraphicsCheck.setText(Resource.get("onlyGeGraphics"));

        useVertexCache.setText(Resource.get("usevertex"));

        shadersCheck.setText(Resource.get("useshader"));

        geometryShaderCheck.setText(Resource.get("useGeometryShader"));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(disableVBOCheck)
                    .addComponent(onlyGEGraphicsCheck)
                    .addComponent(useVertexCache)
                    .addComponent(shadersCheck)
                    .addComponent(geometryShaderCheck, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(disableVBOCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(onlyGEGraphicsCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(useVertexCache)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(shadersCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(geometryShaderCheck)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout VideoPanelLayout = new javax.swing.GroupLayout(VideoPanel);
        VideoPanel.setLayout(VideoPanelLayout);
        VideoPanelLayout.setHorizontalGroup(
            VideoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(VideoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(574, Short.MAX_VALUE))
        );
        VideoPanelLayout.setVerticalGroup(
            VideoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(VideoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(191, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(Resource.get("video"), VideoPanel);

        IgnoreAudioThreadsCheck.setText(Resource.get("disableaudiothreads"));

        disableBlockingAudioCheck.setText(Resource.get("disableaudiotblocking"));

        DisableSceAudioCheck.setText(Resource.get("disableaudiotchannels"));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(IgnoreAudioThreadsCheck)
            .addComponent(DisableSceAudioCheck)
            .addComponent(disableBlockingAudioCheck)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(IgnoreAudioThreadsCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(DisableSceAudioCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(disableBlockingAudioCheck))
        );

        javax.swing.GroupLayout AudioPanelLayout = new javax.swing.GroupLayout(AudioPanel);
        AudioPanel.setLayout(AudioPanelLayout);
        AudioPanelLayout.setHorizontalGroup(
            AudioPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AudioPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(580, Short.MAX_VALUE))
        );
        AudioPanelLayout.setVerticalGroup(
            AudioPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AudioPanelLayout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(248, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(Resource.get("audio"), AudioPanel);

        invalidMemoryCheck.setText(Resource.get("ignoreinvalidmemory"));

        ignoreUnmappedImports.setText(Resource.get("ignoreUnmaped"));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ignoreUnmappedImports)
                    .addComponent(invalidMemoryCheck))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(invalidMemoryCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ignoreUnmappedImports)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout MemoryPanelLayout = new javax.swing.GroupLayout(MemoryPanel);
        MemoryPanel.setLayout(MemoryPanelLayout);
        MemoryPanelLayout.setHorizontalGroup(
            MemoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MemoryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(574, Short.MAX_VALUE))
        );
        MemoryPanelLayout.setVerticalGroup(
            MemoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MemoryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(260, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Memory", MemoryPanel);

        useMediaEngine.setText(Resource.get("useMediaEngine"));

        useConnector.setText(Resource.get("useConnector"));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(useMediaEngine, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(useConnector))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(useMediaEngine)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(useConnector)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout MiscPanelLayout = new javax.swing.GroupLayout(MiscPanel);
        MiscPanel.setLayout(MiscPanelLayout);
        MiscPanelLayout.setHorizontalGroup(
            MiscPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MiscPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(406, Short.MAX_VALUE))
        );
        MiscPanelLayout.setVerticalGroup(
            MiscPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MiscPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(260, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(Resource.get("misc"), MiscPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButtonOK, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 686, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonCancel)
                    .addComponent(jButtonOK))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
public void RefreshWindow() {
	boolean enabled = Settings.getInstance().readBool("emu.pbpunpack");
	pbpunpackcheck.setSelected(enabled);
	
	enabled = Settings.getInstance().readBool("gui.saveWindowPos");
	saveWindowPosCheck.setSelected(enabled);
	
	enabled = Settings.getInstance().readBool("emu.compiler");
	compilerCheck.setSelected(enabled);
	
	enabled = Settings.getInstance().readBool("emu.profiler");
	profilerCheck.setSelected(enabled);
	
	enabled = Settings.getInstance().readBool("emu.useshaders");
	shadersCheck.setSelected(enabled);
	
	enabled = Settings.getInstance().readBool("emu.useGeometryShader");
	geometryShaderCheck.setSelected(enabled);
	
	enabled = Settings.getInstance().readBool("emu.debug.enablefilelogger");
	filelogCheck.setSelected(enabled);
	
	enabled = Settings.getInstance().readBool("emu.savedataSizes");
	savedatasizesCheck.setSelected(enabled);

	int language = Settings.getInstance().readInt("emu.impose.language");
	languageBox.setSelectedItem(language);

	enabled = Settings.getInstance().readBool("emu.disablevbo");
	disableVBOCheck.setSelected(enabled);
	
	enabled = Settings.getInstance().readBool("emu.onlyGEGraphics");
	onlyGEGraphicsCheck.setSelected(enabled);
	
	enabled = Settings.getInstance().readBool("emu.useConnector");
	useConnector.setSelected(enabled);

    enabled = Settings.getInstance().readBool("emu.useMediaEngine");
    useMediaEngine.setSelected(enabled);
	
	enabled = Settings.getInstance().readBool("emu.useVertexCache");
	useVertexCache.setSelected(enabled);
	
	enabled = Settings.getInstance().readBool("emu.ignoreInvalidMemoryAccess");
	invalidMemoryCheck.setSelected(enabled);
	
	enabled = Settings.getInstance().readBool("emu.disablesceAudio");
	DisableSceAudioCheck.setSelected(enabled);
	
	enabled = Settings.getInstance().readBool("emu.ignoreaudiothreads");
	IgnoreAudioThreadsCheck.setSelected(enabled);
		
	enabled = Settings.getInstance().readBool("emu.disableblockingaudio");
	disableBlockingAudioCheck.setSelected(enabled);
		
	enabled = Settings.getInstance().readBool("emu.ignoreUnmappedImports");
	ignoreUnmappedImports.setSelected(enabled);
	
	enabled = Settings.getInstance().readBool("emu.umdbrowser");
	if(enabled)
		umdBrowser.setSelected(true);
	else
		ClassicOpenDialogumd.setSelected(true);
	
	umdpath.setText(Settings.getInstance().readString("emu.umdpath"));
}

private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
   Settings.getInstance().writeBool("emu.pbpunpack", pbpunpackcheck.isSelected());
   Settings.getInstance().writeBool("gui.saveWindowPos", saveWindowPosCheck.isSelected());
   Settings.getInstance().writeBool("emu.compiler", compilerCheck.isSelected());
   Settings.getInstance().writeBool("emu.profiler", profilerCheck.isSelected());
   Settings.getInstance().writeBool("emu.useshaders", shadersCheck.isSelected());
   Settings.getInstance().writeBool("emu.useGeometryShader", geometryShaderCheck.isSelected());
   Settings.getInstance().writeBool("emu.debug.enablefilelogger", filelogCheck.isSelected());
   Settings.getInstance().writeBool("emu.savedataSizes", savedatasizesCheck.isSelected());
   Settings.getInstance().writeInt("emu.impose.language", languageBox.getSelectedIndex());
   Settings.getInstance().writeBool("emu.disablevbo", disableVBOCheck.isSelected());
   Settings.getInstance().writeBool("emu.onlyGEGraphics", onlyGEGraphicsCheck.isSelected());
   Settings.getInstance().writeBool("emu.useConnector",useConnector.isSelected());
   Settings.getInstance().writeBool("emu.useMediaEngine",useMediaEngine.isSelected());
   Settings.getInstance().writeBool("emu.useVertexCache",useVertexCache.isSelected());
   Settings.getInstance().writeBool("emu.ignoreInvalidMemoryAccess", invalidMemoryCheck.isSelected());
   Settings.getInstance().writeBool("emu.disablesceAudio", DisableSceAudioCheck.isSelected());
   Settings.getInstance().writeBool("emu.ignoreaudiothreads",IgnoreAudioThreadsCheck.isSelected());
   Settings.getInstance().writeBool("emu.disableblockingaudio",disableBlockingAudioCheck.isSelected());
   Settings.getInstance().writeBool("emu.ignoreUnmappedImports",ignoreUnmappedImports.isSelected());
   
   if(umdBrowser.isSelected())
      Settings.getInstance().writeBool("emu.umdbrowser", true);
   else
      Settings.getInstance().writeBool("emu.umdbrowser", false);
   Settings.getInstance().writeString("emu.umdpath", umdpath.getText());

   dispose();
}//GEN-LAST:event_jButtonOKActionPerformed

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
  FolderChooser folderChooser = new FolderChooser("select folder");
  int result = folderChooser.showSaveDialog(jButton1.getTopLevelAncestor());
  if (result == FolderChooser.APPROVE_OPTION) {
       umdpath.setText(folderChooser.getSelectedFile().getPath());
  }
}//GEN-LAST:event_jButton1ActionPerformed

private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
    RefreshWindow();
    dispose();
}//GEN-LAST:event_jButtonCancelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AudioPanel;
    private javax.swing.JRadioButton ClassicOpenDialogumd;
    private javax.swing.JCheckBox DisableSceAudioCheck;
    private javax.swing.JCheckBox IgnoreAudioThreadsCheck;
    private javax.swing.JPanel MemoryPanel;
    private javax.swing.JPanel MiscPanel;
    private javax.swing.JPanel VideoPanel;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox compilerCheck;
    private javax.swing.JCheckBox disableBlockingAudioCheck;
    private javax.swing.JCheckBox disableVBOCheck;
    private javax.swing.JCheckBox filelogCheck;
    private javax.swing.JCheckBox savedatasizesCheck;
    private javax.swing.JComboBox languageBox;
    private javax.swing.JLabel languageLabel;
    private javax.swing.JPanel generalPanel;
    private javax.swing.JCheckBox ignoreUnmappedImports;
    private javax.swing.JCheckBox invalidMemoryCheck;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JCheckBox onlyGEGraphicsCheck;
    private javax.swing.JCheckBox pbpunpackcheck;
    private javax.swing.JCheckBox profilerCheck;
    private javax.swing.JCheckBox saveWindowPosCheck;
    private javax.swing.JCheckBox shadersCheck;
    private javax.swing.JCheckBox geometryShaderCheck;
    private javax.swing.JRadioButton umdBrowser;
    private javax.swing.JTextField umdpath;
    private javax.swing.JCheckBox useConnector;
    private javax.swing.JCheckBox useMediaEngine;
    private javax.swing.JCheckBox useVertexCache;
    // End of variables declaration//GEN-END:variables

}
