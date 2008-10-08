package jpcsp.Debugger.DisassemblerModule;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jpcsp.Settings;
import jpcsp.Allegrex.CpuState;

public class VfpuFrame extends JFrame {

	private static final long serialVersionUID = -3354614570041807689L;
	
	JTextField registers[][][] = new JTextField[8][4][4];
	JPanel panels[] = new JPanel[8];
	static private VfpuFrame instance;  
	
	static public VfpuFrame getInstance() {
		if(instance == null)
			instance = new VfpuFrame();
		return instance;
	}
	
	private VfpuFrame() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("VFPU registers");
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowDeactivated(WindowEvent e) {
				if (Settings.get_instance().readBoolOptions("guisettings/saveWindowPos")) {
					Settings.get_instance().writeWindowPos("vfpuregisters", getLocation());

					/* save window size */
					String[] windowSize = new String[2];
					windowSize[0] = Integer.toString(getWidth());
					windowSize[1] = Integer.toString(getHeight());
					Settings.get_instance().writeWindowSize("vfpuregisters", windowSize);
				}
			}});
		
		for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 4; ++j) {
                for(int k = 0; k < 4; ++k)
                	registers[i][j][k] = new JTextField();
            }
        }
		
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		
		for(int i = 0; i < panels.length; ++i) {
			JPanel panel = new JPanel();
			panels[i] = panel;
			
			GroupLayout l = new GroupLayout(panel);
			panel.setLayout(l);
			panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Block " + i),
                    null));

			l.setHorizontalGroup(l.createSequentialGroup()
					.addGroup(l.createParallelGroup()
							.addComponent(registers[i][0][0])
							.addComponent(registers[i][1][0])
							.addComponent(registers[i][2][0])
							.addComponent(registers[i][3][0]))
					.addGroup(l.createParallelGroup()
							.addComponent(registers[i][0][1])
							.addComponent(registers[i][1][1])
							.addComponent(registers[i][2][1])
							.addComponent(registers[i][3][1]))
					.addGroup(l.createParallelGroup()
							.addComponent(registers[i][0][2])
							.addComponent(registers[i][1][2])
							.addComponent(registers[i][2][2])
							.addComponent(registers[i][3][2]))
					.addGroup(l.createParallelGroup()
							.addComponent(registers[i][0][3])
							.addComponent(registers[i][1][3])
							.addComponent(registers[i][2][3])
							.addComponent(registers[i][3][3])));
			l.setVerticalGroup(l.createSequentialGroup()
					.addGroup(l.createParallelGroup()
							.addComponent(registers[i][0][0])
							.addComponent(registers[i][0][1])
							.addComponent(registers[i][0][2])
							.addComponent(registers[i][0][3]))
					.addGroup(l.createParallelGroup()
							.addComponent(registers[i][1][0])
							.addComponent(registers[i][1][1])
							.addComponent(registers[i][1][2])
							.addComponent(registers[i][1][3]))
					.addGroup(l.createParallelGroup()
							.addComponent(registers[i][2][0])
							.addComponent(registers[i][2][1])
							.addComponent(registers[i][2][2])
							.addComponent(registers[i][2][3]))
					.addGroup(l.createParallelGroup()
							.addComponent(registers[i][3][0])
							.addComponent(registers[i][3][1])
							.addComponent(registers[i][3][2])
							.addComponent(registers[i][3][3])));
		}
		
		layout.setHorizontalGroup(layout.createParallelGroup()
					.addGroup(layout.createSequentialGroup()
							.addComponent(panels[0])
							.addComponent(panels[1]))
					.addGroup(layout.createSequentialGroup()
							.addComponent(panels[2])
							.addComponent(panels[3]))
					.addGroup(layout.createSequentialGroup()
							.addComponent(panels[4])
							.addComponent(panels[5]))
					.addGroup(layout.createSequentialGroup()
							.addComponent(panels[6])
							.addComponent(panels[7]))
				);
		layout.setVerticalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
						.addComponent(panels[0])
						.addComponent(panels[2])
						.addComponent(panels[4])
						.addComponent(panels[6]))
				.addGroup(layout.createSequentialGroup()
						.addComponent(panels[1])
						.addComponent(panels[3])
						.addComponent(panels[5])
						.addComponent(panels[7]))		
				);
		setSize(Settings.get_instance().readWindowSize("vfpuregisters")[0], Settings.get_instance().readWindowSize("vfpuregisters")[1]);
	}
	
	public void updateRegisters(CpuState cpu) {
		for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 4; ++j) {
                for(int k = 0; k < 4; ++k) {
                	registers[i][k][j].setText("" + cpu.vpr[i][j][k]);
                	registers[i][k][j].setCaretPosition(0);
                }
            }
        }
	}
}
