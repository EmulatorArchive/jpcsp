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

package jpcsp.HLE.modules150;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import jpcsp.Settings;
import jpcsp.HLE.kernel.types.SceIoStat;
import jpcsp.HLE.kernel.types.SceUtilityMsgDialogParams;
import jpcsp.HLE.kernel.types.SceUtilityOskParams;
import jpcsp.HLE.kernel.types.SceUtilitySavedataParam;
import jpcsp.HLE.kernel.types.pspAbstractMemoryMappedStructure;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;
import jpcsp.HLE.Modules;
import jpcsp.HLE.pspdisplay;
import jpcsp.HLE.pspiofilemgr;
import jpcsp.graphics.VideoEngine;
import jpcsp.hardware.MemoryStick;
import jpcsp.util.Utilities;

import jpcsp.Memory;
import jpcsp.Processor;
import jpcsp.State;

import jpcsp.Allegrex.CpuState;
import static jpcsp.HLE.kernel.types.SceKernelErrors.*;

public class sceUtility implements HLEModule {
	@Override
	public String getName() { return "sceUtility"; }

	@Override
	public void installModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
			mm.addFunction(sceUtilityGameSharingInitStartFunction, 0xC492F751);
			mm.addFunction(sceUtilityGameSharingShutdownStartFunction, 0xEFC6F80F);
			mm.addFunction(sceUtilityGameSharingUpdateFunction, 0x7853182D);
			mm.addFunction(sceUtilityGameSharingGetStatusFunction, 0x946963F3);
			mm.addFunction(sceNetplayDialogInitStartFunction, 0x3AD50AE7);
			mm.addFunction(sceNetplayDialogShutdownStartFunction, 0xBC6B6296);
			mm.addFunction(sceNetplayDialogUpdateFunction, 0x417BED54);
			mm.addFunction(sceNetplayDialogGetStatusFunction, 0xB6CEE597);
			mm.addFunction(sceUtilityNetconfInitStartFunction, 0x4DB1E739);
			mm.addFunction(sceUtilityNetconfShutdownStartFunction, 0xF88155F6);
			mm.addFunction(sceUtilityNetconfUpdateFunction, 0x91E70E35);
			mm.addFunction(sceUtilityNetconfGetStatusFunction, 0x6332AA39);
			mm.addFunction(sceUtilitySavedataInitStartFunction, 0x50C4CD57);
			mm.addFunction(sceUtilitySavedataShutdownStartFunction, 0x9790B33C);
			mm.addFunction(sceUtilitySavedataUpdateFunction, 0xD4B95FFB);
			mm.addFunction(sceUtilitySavedataGetStatusFunction, 0x8874DBE0);
			mm.addFunction(sceUtility_2995D020Function, 0x2995D020);
			mm.addFunction(sceUtility_B62A4061Function, 0xB62A4061);
			mm.addFunction(sceUtility_ED0FAD38Function, 0xED0FAD38);
			mm.addFunction(sceUtility_88BC7406Function, 0x88BC7406);
			mm.addFunction(sceUtilityMsgDialogInitStartFunction, 0x2AD8E239);
			mm.addFunction(sceUtilityMsgDialogShutdownStartFunction, 0x67AF3428);
			mm.addFunction(sceUtilityMsgDialogUpdateFunction, 0x95FC253B);
			mm.addFunction(sceUtilityMsgDialogGetStatusFunction, 0x9A1C91D7);
			mm.addFunction(sceUtilityOskInitStartFunction, 0xF6269B82);
			mm.addFunction(sceUtilityOskShutdownStartFunction, 0x3DFAEBA9);
			mm.addFunction(sceUtilityOskUpdateFunction, 0x4B85C861);
			mm.addFunction(sceUtilityOskGetStatusFunction, 0xF3F76017);
			mm.addFunction(sceUtilitySetSystemParamIntFunction, 0x45C18506);
			mm.addFunction(sceUtilitySetSystemParamStringFunction, 0x41E30674);
			mm.addFunction(sceUtilityGetSystemParamIntFunction, 0xA5DA2406);
			mm.addFunction(sceUtilityGetSystemParamStringFunction, 0x34B78343);
			mm.addFunction(sceUtilityCheckNetParamFunction, 0x5EEE6548);
			mm.addFunction(sceUtilityGetNetParamFunction, 0x434D4B3A);

            gameSharingState    = new NotImplementedUtilityDialogState("sceUtilityGameSharing");
            netplayDialogState  = new NotImplementedUtilityDialogState("sceNetplayDialog");
            netconfState        = new NotImplementedUtilityDialogState("sceUtilityNetconf");
			savedataState       = new UtilityDialogState("sceUtilitySavedata");
			msgDialogState      = new UtilityDialogState("sceUtilityMsgDialog");
			oskState            = new UtilityDialogState("sceUtilityOsk");
		}
	}

	@Override
	public void uninstallModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
			mm.removeFunction(sceUtilityGameSharingInitStartFunction);
			mm.removeFunction(sceUtilityGameSharingShutdownStartFunction);
			mm.removeFunction(sceUtilityGameSharingUpdateFunction);
			mm.removeFunction(sceUtilityGameSharingGetStatusFunction);
			mm.removeFunction(sceNetplayDialogInitStartFunction);
			mm.removeFunction(sceNetplayDialogShutdownStartFunction);
			mm.removeFunction(sceNetplayDialogUpdateFunction);
			mm.removeFunction(sceNetplayDialogGetStatusFunction);
			mm.removeFunction(sceUtilityNetconfInitStartFunction);
			mm.removeFunction(sceUtilityNetconfShutdownStartFunction);
			mm.removeFunction(sceUtilityNetconfUpdateFunction);
			mm.removeFunction(sceUtilityNetconfGetStatusFunction);
			mm.removeFunction(sceUtilitySavedataInitStartFunction);
			mm.removeFunction(sceUtilitySavedataShutdownStartFunction);
			mm.removeFunction(sceUtilitySavedataUpdateFunction);
			mm.removeFunction(sceUtilitySavedataGetStatusFunction);
			mm.removeFunction(sceUtility_2995D020Function);
			mm.removeFunction(sceUtility_B62A4061Function);
			mm.removeFunction(sceUtility_ED0FAD38Function);
			mm.removeFunction(sceUtility_88BC7406Function);
			mm.removeFunction(sceUtilityMsgDialogInitStartFunction);
			mm.removeFunction(sceUtilityMsgDialogShutdownStartFunction);
			mm.removeFunction(sceUtilityMsgDialogUpdateFunction);
			mm.removeFunction(sceUtilityMsgDialogGetStatusFunction);
			mm.removeFunction(sceUtilityOskInitStartFunction);
			mm.removeFunction(sceUtilityOskShutdownStartFunction);
			mm.removeFunction(sceUtilityOskUpdateFunction);
			mm.removeFunction(sceUtilityOskGetStatusFunction);
			mm.removeFunction(sceUtilitySetSystemParamIntFunction);
			mm.removeFunction(sceUtilitySetSystemParamStringFunction);
			mm.removeFunction(sceUtilityGetSystemParamIntFunction);
			mm.removeFunction(sceUtilityGetSystemParamStringFunction);
			mm.removeFunction(sceUtilityCheckNetParamFunction);
			mm.removeFunction(sceUtilityGetNetParamFunction);
		}
	}

    public static final int PSP_SYSTEMPARAM_ID_STRING_NICKNAME = 1;
    public static final int PSP_SYSTEMPARAM_ID_INT_ADHOC_CHANNEL = 2;
    public static final int PSP_SYSTEMPARAM_ID_INT_WLAN_POWERSAVE = 3;
    public static final int PSP_SYSTEMPARAM_ID_INT_DATE_FORMAT = 4;
    public static final int PSP_SYSTEMPARAM_ID_INT_TIME_FORMAT = 5;
    public static final int PSP_SYSTEMPARAM_ID_INT_TIMEZONE = 6;
    public static final int PSP_SYSTEMPARAM_ID_INT_DAYLIGHTSAVINGS = 7;
    public static final int PSP_SYSTEMPARAM_ID_INT_LANGUAGE = 8;
    public static final int PSP_SYSTEMPARAM_ID_INT_BUTTON_PREFERENCE = 9;

    public static final int PSP_SYSTEMPARAM_LANGUAGE_ENGLISH = 1;
    public static final int PSP_SYSTEMPARAM_LANGUAGE_FRENCH = 2;
    public static final int PSP_SYSTEMPARAM_LANGUAGE_SPANISH = 3;
    public static final int PSP_SYSTEMPARAM_LANGUAGE_GERMAN = 4;
    public static final int PSP_SYSTEMPARAM_LANGUAGE_ITALIAN = 5;
    public static final int PSP_SYSTEMPARAM_LANGUAGE_DUTCH = 6;
    public static final int PSP_SYSTEMPARAM_LANGUAGE_PORTUGUESE = 7;
    public static final int PSP_SYSTEMPARAM_LANGUAGE_RUSSIAN = 8;
    public static final int PSP_SYSTEMPARAM_LANGUAGE_KOREAN = 9;
    public static final int PSP_SYSTEMPARAM_LANGUAGE_CHINESE_TRADITIONAL = 10;
    public static final int PSP_SYSTEMPARAM_LANGUAGE_CHINESE_SIMPLIFIED = 11;

    public static final int PSP_SYSTEMPARAM_DATE_FORMAT_YYYYMMDD = 0;
    public static final int PSP_SYSTEMPARAM_DATE_FORMAT_MMDDYYYY = 1;
    public static final int PSP_SYSTEMPARAM_DATE_FORMAT_DDMMYYYY = 2;

    public static final int PSP_SYSTEMPARAM_TIME_FORMAT_24HR = 0;
    public static final int PSP_SYSTEMPARAM_TIME_FORMAT_12HR = 1;

    public static final int PSP_UTILITY_ERROR_NOT_INITED = 0x80110005; // might not be correct name

    public static final int PSP_NETPARAM_ERROR_BAD_NETCONF = 0x80110601;
    public static final int PSP_NETPARAM_ERROR_BAD_PARAM = 0x80110604;

    public static final int PSP_UTILITY_DIALOG_NONE = 0;
    public static final int PSP_UTILITY_DIALOG_INIT = 1;
    public static final int PSP_UTILITY_DIALOG_VISIBLE = 2;
    public static final int PSP_UTILITY_DIALOG_QUIT = 3;
    public static final int PSP_UTILITY_DIALOG_FINISHED = 4;

    protected static final int maxLineLengthForDialog = 80;

    protected UtilityDialogState gameSharingState;
    protected UtilityDialogState netplayDialogState;
    protected UtilityDialogState netconfState;
    protected UtilityDialogState savedataState;
    protected SceUtilitySavedataParam savedataParams;
    protected UtilityDialogState msgDialogState;
    protected SceUtilityMsgDialogParams msgDialogParams;
    protected UtilityDialogState oskState;
    protected SceUtilityOskParams oskParams;

    // TODO expose via settings GUI
    protected String systemParam_nickname = "JPCSP";
    protected int systemParam_dateFormat = PSP_SYSTEMPARAM_DATE_FORMAT_YYYYMMDD;
    protected int systemParam_timeFormat = PSP_SYSTEMPARAM_TIME_FORMAT_24HR;
    protected int systemParam_timeZone = 0; // TODO probably minutes west or east of UTC
    protected int systemParam_language = PSP_SYSTEMPARAM_LANGUAGE_ENGLISH;
    protected int systemParam_buttonPreference = 0;

    // Save list vars.
    protected Object saveListSelection;
    protected boolean saveListSelected;

    protected static class UtilityDialogState {
    	protected String name;
    	private pspAbstractMemoryMappedStructure params;
    	private int paramsAddr;
    	private int status;
    	private boolean displayLocked;

    	public UtilityDialogState(String name) {
    		this.name = name;
    		status = PSP_UTILITY_DIALOG_NONE;
    		displayLocked = false;
    	}

    	public void executeInitStart(Processor processor, pspAbstractMemoryMappedStructure params) {
    		CpuState cpu = processor.cpu;
    		Memory mem = Memory.getInstance();

    		paramsAddr = cpu.gpr[4];
    		if (!mem.isAddressGood(paramsAddr)) {
    			Modules.log.error(name + "InitStart bad address " + String.format("0x%08X", paramsAddr));
    			cpu.gpr[2] = -1;
    		} else {
    			this.params = params;

    			params.read(mem, paramsAddr);

	    		if (Modules.log.isInfoEnabled()) {
					Modules.log.info("PARTIAL:" + name + "InitStart " + params.toString());
				}

	            // Start with INIT
	    		status = PSP_UTILITY_DIALOG_INIT;

	    		cpu.gpr[2] = 0;
    		}
    	}

    	public void executeGetStatus(Processor processor) {
    		CpuState cpu = processor.cpu;

    		if (Modules.log.isDebugEnabled()) {
                Modules.log.debug(name + "GetStatus status " + status);
            }

            cpu.gpr[2] = status;

            // after returning FINISHED once, return NONE on following calls
            if (status == PSP_UTILITY_DIALOG_FINISHED) {
                status = PSP_UTILITY_DIALOG_NONE;
            } else if (status == PSP_UTILITY_DIALOG_INIT) {
            	// Move from INIT to VISIBLE
            	status = PSP_UTILITY_DIALOG_VISIBLE;
            }
    	}

    	public void executeShutdownStart(Processor processor) {
            CpuState cpu = processor.cpu;

            if (Modules.log.isDebugEnabled()) {
            	Modules.log.debug(name + "ShutdownStart");
            }

            status = PSP_UTILITY_DIALOG_FINISHED;

            cpu.gpr[2] = 0;
    	}

    	public boolean tryUpdate(Processor processor) {
    		CpuState cpu = processor.cpu;

            int unk = cpu.gpr[4];
            if (Modules.log.isDebugEnabled()) {
                Modules.log.debug(name + "Update unk=" + unk);
            }

            boolean canDisplay = false;

            if (status == PSP_UTILITY_DIALOG_INIT) {
            	// Move from INIT to VISIBLE
            	status = PSP_UTILITY_DIALOG_VISIBLE;
            } else if (status == PSP_UTILITY_DIALOG_VISIBLE) {
	            // A call to the GUI (JOptionPane) is only possible when the VideoEngine is not
	            // busy waiting on a sync: call JOptionPane only when the display is not locked.
            	while (true) {
		            canDisplay = pspdisplay.getInstance().tryLockDisplay();
		            if (canDisplay) {
		            	displayLocked = true;
		            	break;
		            } else if (VideoEngine.getInstance().getCurrentList() == null) {
		            	// Check if the VideoEngine is not processing a list: in that case,
		            	// this could mean the display will be soon available for locking
		            	// (e.g. list processing is done, but still copying the graphics
		            	//  to PSP memory in pspdisplay.display()).
		            	// Wait a little bit and try again to lock the display.
		            	if (Modules.log.isDebugEnabled()) {
		            		Modules.log.debug(name + "Update : could not lock the display but VideoEngine not displayed, waiting a while...");
		            	}

		            	try {
		                    Thread.sleep(1);
		                } catch (InterruptedException e) {
		                	// Ignore exception
		                }
		            } else {
		            	if (Modules.log.isDebugEnabled()) {
		            		Modules.log.debug(name + "Update : could not lock the display");
		            	}
		            	break;
		            }
            	}
            }

            if (canDisplay) {
                // Some games reach sceUtilitySavedataInitStart with empty params which only
                // get filled with a subsquent call to sceUtilitySavedataUpdate (eg.: To Love-Ru).
                // This is why we have to re-read the params here.
            	params.read(Memory.getInstance(), paramsAddr);
            }

            cpu.gpr[2] = 0;

            return canDisplay;
    	}

    	public void endUpdate(Processor processor) {
    		if (displayLocked) {
    			pspdisplay.getInstance().unlockDisplay();
    			displayLocked = false;
    		}

    		if (status == PSP_UTILITY_DIALOG_VISIBLE) {
    			// Dialog has completed
    			status = PSP_UTILITY_DIALOG_QUIT;
    		}
    	}
    }

    protected static class NotImplementedUtilityDialogState extends UtilityDialogState {
		public NotImplementedUtilityDialogState(String name) {
			super(name);
		}

		@Override
    	public void executeInitStart(Processor processor, pspAbstractMemoryMappedStructure params) {
			CpuState cpu = processor.cpu;

			Modules.log.warn("Unimplemented: " + name + "InitStart");

			cpu.gpr[2] = 0xDEADC0DE;
		}

		@Override
		public void executeShutdownStart(Processor processor) {
			CpuState cpu = processor.cpu;

			Modules.log.warn("Unimplemented: " + name + "ShutdownStart");

			cpu.gpr[2] = 0xDEADC0DE;
		}

		@Override
		public void executeGetStatus(Processor processor) {
			CpuState cpu = processor.cpu;

			Modules.log.warn("Unimplemented: " + name + "GetStatus");

			cpu.gpr[2] = PSP_UTILITY_ERROR_NOT_INITED;
		}

		@Override
		public boolean tryUpdate(Processor processor) {
			CpuState cpu = processor.cpu;

			Modules.log.warn("Unimplemented: " + name + "Update");

			cpu.gpr[2] = 0xDEADC0DE;

			return false;
		}

		@Override
		public void endUpdate(Processor processor) {
			// Do nothing
		}
    }

    protected String formatMessageForDialog(String message) {
		StringBuilder formattedMessage = new StringBuilder();

		for (int i = 0; i < message.length(); ) {
			String rest = message.substring(i);
			if (rest.length() > maxLineLengthForDialog) {
				int lastSpace = rest.lastIndexOf(' ', maxLineLengthForDialog);
				rest = rest.substring(0, (lastSpace >= 0 ? lastSpace : maxLineLengthForDialog));
				formattedMessage.append(rest);
				i += rest.length() + 1;
				formattedMessage.append("\n");
			} else {
				formattedMessage.append(rest);
				i += rest.length();
			}
		}

		return formattedMessage.toString();
	}

    protected void showSavedataList(String[] options) {
        final JFrame listFrame = new JFrame();
        listFrame.setTitle("Savedata List");
        listFrame.setSize(200, 220);
        listFrame.setResizable(false);
        int pos[] = Settings.getInstance().readWindowPos("savedata");
        listFrame.setLocation(pos[0], pos[1]);
        listFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        final JList saveList = new JList(options);
        JScrollPane listScroll = new JScrollPane(saveList);
        JButton selectButton = new JButton("Select");

        listFrame.setLayout(new BorderLayout(5,5));
        listFrame.getContentPane().add(listScroll, BorderLayout.CENTER);
        listFrame.getContentPane().add(selectButton, BorderLayout.SOUTH);
        listFrame.setVisible(true);

        saveListSelected = false;

        //Wait for user selection.
        while(!saveListSelected) {
            if(!listFrame.isVisible())
                break;

            selectButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    if(saveList.getSelectedIndex() != -1) {
                        saveListSelection = saveList.getSelectedValue();
                        listFrame.dispose();
                        saveListSelected = true;
                    }
                }
            });
        }
        
        Settings.getInstance().writeWindowPos("savedata", listFrame.getLocation());
    }

	public void sceUtilityGameSharingInitStart(Processor processor) {
		gameSharingState.executeInitStart(processor, null);
	}

	public void sceUtilityGameSharingShutdownStart(Processor processor) {
		gameSharingState.executeShutdownStart(processor);
	}

	public void sceUtilityGameSharingUpdate(Processor processor) {
		if (gameSharingState.tryUpdate(processor)) {
			gameSharingState.endUpdate(processor);
		}
	}

	public void sceUtilityGameSharingGetStatus(Processor processor) {
		gameSharingState.executeGetStatus(processor);
	}

	public void sceNetplayDialogInitStart(Processor processor) {
		netplayDialogState.executeInitStart(processor, null);
	}

	public void sceNetplayDialogShutdownStart(Processor processor) {
		netplayDialogState.executeShutdownStart(processor);
	}

	public void sceNetplayDialogUpdate(Processor processor) {
		if (netplayDialogState.tryUpdate(processor)) {
			netplayDialogState.endUpdate(processor);
		}
	}

	public void sceNetplayDialogGetStatus(Processor processor) {
		netplayDialogState.executeGetStatus(processor);
	}

	public void sceUtilityNetconfInitStart(Processor processor) {
		netconfState.executeInitStart(processor, null);
	}

	public void sceUtilityNetconfShutdownStart(Processor processor) {
		netconfState.executeShutdownStart(processor);
	}

	public void sceUtilityNetconfUpdate(Processor processor) {
		if (netconfState.tryUpdate(processor)) {
			netconfState.endUpdate(processor);
		}
	}

	public void sceUtilityNetconfGetStatus(Processor processor) {
		netconfState.executeGetStatus(processor);
	}

	private int computeMemoryStickRequiredSpaceKb(int sizeByte) {
	    int sizeKb = (sizeByte + 1023) / 1024;
	    int sectorSizeKb = MemoryStick.getSectorSizeKb();
	    int numberSectors = (sizeKb + sectorSizeKb - 1) / sectorSizeKb;

	    return numberSectors * sectorSizeKb;
	}

    private void hleUtilitySavedataDisplay() {
        Memory mem = Processor.memory;

        switch (savedataParams.mode) {
            case SceUtilitySavedataParam.MODE_AUTOLOAD:
            case SceUtilitySavedataParam.MODE_LOAD:
                if (savedataParams.saveName == null || savedataParams.saveName.length() == 0) {
                    if (savedataParams.saveNameList != null && savedataParams.saveNameList.length > 0) {
                        savedataParams.saveName = savedataParams.saveNameList[0];
                    } else {
                        savedataParams.saveName = "-000";
                    }
                }

                try {
                    savedataParams.load(mem, pspiofilemgr.getInstance());
                    savedataParams.base.result = 0;
                    savedataParams.write(mem);
                } catch (IOException e) {
                    savedataParams.base.result = ERROR_SAVEDATA_LOAD_NO_DATA;
                } catch (Exception e) {
                    savedataParams.base.result = ERROR_SAVEDATA_LOAD_NO_DATA;
                    e.printStackTrace();
                }
                break;

            case SceUtilitySavedataParam.MODE_LISTLOAD:
                //Search for valid saves.
                String[] validNames = new String[savedataParams.saveNameList.length];

                for(int i = 0; i < savedataParams.saveNameList.length; i++) {
                    savedataParams.saveName = savedataParams.saveNameList[i];

                    if(savedataParams.isPresent(pspiofilemgr.getInstance()))
                        validNames[i] = savedataParams.saveName;
                    else
                        validNames[i] = "NOT PRESENT";
                }

                showSavedataList(validNames);
                if (saveListSelection == null || saveListSelection.equals("NOT PRESENT")) {
                    Modules.log.warn("Savedata MODE_LISTLOAD no save selected");
                    savedataParams.base.result = ERROR_SAVEDATA_LOAD_NO_DATA;
                }

                else {
                    savedataParams.saveName = saveListSelection.toString();
                    try {
                        savedataParams.load(mem, pspiofilemgr.getInstance());
                        savedataParams.base.result = 0;
                        savedataParams.write(mem);
                    } catch (IOException e) {
                        savedataParams.base.result = ERROR_SAVEDATA_LOAD_NO_DATA;
                    } catch (Exception e) {
                        savedataParams.base.result = ERROR_SAVEDATA_LOAD_NO_DATA;
                        e.printStackTrace();
                    }
                }
                break;

            case SceUtilitySavedataParam.MODE_AUTOSAVE:
            case SceUtilitySavedataParam.MODE_SAVE:
                try {
                    if (savedataParams.saveName == null || savedataParams.saveName.length() == 0) {
                        if (savedataParams.saveNameList != null && savedataParams.saveNameList.length > 0) {
                            savedataParams.saveName = savedataParams.saveNameList[0];
                        } else {
                            savedataParams.saveName = "-000";
                        }
                    }

                    savedataParams.save(mem, pspiofilemgr.getInstance());
                    savedataParams.base.result = 0;
                } catch (IOException e) {
                    savedataParams.base.result = ERROR_SAVEDATA_SAVE_ACCESS_ERROR;
                } catch (Exception e) {
                    savedataParams.base.result = ERROR_SAVEDATA_SAVE_ACCESS_ERROR;
                    e.printStackTrace();
                }
                break;

            case SceUtilitySavedataParam.MODE_LISTSAVE:
                showSavedataList(savedataParams.saveNameList);
                if (saveListSelection == null) {
                    Modules.log.warn("Savedata MODE_LISTSAVE no save selected");
                    savedataParams.base.result = ERROR_SAVEDATA_SAVE_NO_MEMSTICK;
                }

                else {
                    savedataParams.saveName = saveListSelection.toString();
                    try {
                        savedataParams.save(mem, pspiofilemgr.getInstance());
                        savedataParams.base.result = 0;
                    } catch (IOException e) {
                        savedataParams.base.result = ERROR_SAVEDATA_SAVE_ACCESS_ERROR;
                    } catch (Exception e) {
                        savedataParams.base.result = ERROR_SAVEDATA_SAVE_ACCESS_ERROR;
                        e.printStackTrace();
                    }
                }
                break;

            case SceUtilitySavedataParam.MODE_TRY: {
                Modules.log.warn("PARTIAL:Savedata mode 8");
                int buffer1Addr = savedataParams.buffer1Addr;
                if (mem.isAddressGood(buffer1Addr)) {
                    String memoryStickFreeSpaceString = MemoryStick.getSizeKbString(MemoryStick.getFreeSizeKb());

                    mem.write32(buffer1Addr +  0, MemoryStick.getSectorSize());
                    mem.write32(buffer1Addr +  4, MemoryStick.getFreeSizeKb() / MemoryStick.getSectorSizeKb());
                    mem.write32(buffer1Addr +  8, MemoryStick.getFreeSizeKb());
                    Utilities.writeStringNZ(mem, buffer1Addr +  12, 8, memoryStickFreeSpaceString);

                    Modules.log.debug("Memory Stick Free Space = " + memoryStickFreeSpaceString);
                }
                int buffer2Addr = savedataParams.buffer2Addr;
                if (mem.isAddressGood(buffer2Addr)) {
                    String gameName = Utilities.readStringNZ(mem, buffer2Addr, 16);
                    String saveName = Utilities.readStringNZ(mem, buffer2Addr + 16, 16);

                    mem.write32(buffer2Addr + 52, MemoryStick.getSectorSize());   //Games that use buffer2Addr require this.

                    savedataParams.gameName = gameName;
                    savedataParams.saveName = saveName;

                    Modules.log.debug("Changing saveName to= " + saveName);
                }
                int buffer3Addr = savedataParams.buffer3Addr;
                if (mem.isAddressGood(buffer3Addr)) {
                    int memoryStickRequiredSpaceKb = 0;
                    memoryStickRequiredSpaceKb += MemoryStick.getSectorSizeKb(); // Assume 1 sector for SFO-Params
                    memoryStickRequiredSpaceKb += computeMemoryStickRequiredSpaceKb(savedataParams.dataSize);
                    memoryStickRequiredSpaceKb += computeMemoryStickRequiredSpaceKb(savedataParams.icon0FileData.size);
                    memoryStickRequiredSpaceKb += computeMemoryStickRequiredSpaceKb(savedataParams.icon1FileData.size);
                    memoryStickRequiredSpaceKb += computeMemoryStickRequiredSpaceKb(savedataParams.pic1FileData.size);
                    memoryStickRequiredSpaceKb += computeMemoryStickRequiredSpaceKb(savedataParams.snd0FileData.size);
                    String memoryStickRequiredSpaceString = MemoryStick.getSizeKbString(memoryStickRequiredSpaceKb);

                    mem.write32(buffer3Addr +  0, memoryStickRequiredSpaceKb / MemoryStick.getSectorSizeKb());
                    mem.write32(buffer3Addr +  4, memoryStickRequiredSpaceKb);
                    Utilities.writeStringNZ(mem, buffer3Addr +  8, 8, memoryStickRequiredSpaceString);
                    mem.write32(buffer3Addr + 16, memoryStickRequiredSpaceKb);
                    Utilities.writeStringNZ(mem, buffer3Addr + 20, 8, memoryStickRequiredSpaceString);

                    Modules.log.debug("Memory Stick Required Space = " + memoryStickRequiredSpaceString);
                }

                savedataParams.base.result = 0;
                break;
            }

            case SceUtilitySavedataParam.MODE_LIST: {
                Modules.log.debug("Savedata mode 11");
                int buffer4Addr = savedataParams.buffer4Addr;
                if (mem.isAddressGood(buffer4Addr)) {
                	int maxEntries = mem.read32(buffer4Addr + 0);
                	int entriesAddr = mem.read32(buffer4Addr + 8);
                	String saveName = savedataParams.saveName;
                	// PSP file name pattern:
                	//   '?' matches one character
                	//   '*' matches any character sequence
                	// To convert to regular expressions:
                	//   replace '?' with '.'
                	//   replace '*' with '.*'
                	String pattern = saveName.replace('?', '.');
                	pattern = pattern.replace("*", ".*");
                	pattern = savedataParams.gameName + pattern;

                	pspiofilemgr fileManager = pspiofilemgr.getInstance();
                	String[] entries = fileManager.listFiles(SceUtilitySavedataParam.savedataPath, pattern);
                	Modules.log.debug("Entries: " + entries);
                	int numEntries = entries == null ? 0 : entries.length;
                	numEntries = Math.min(numEntries, maxEntries);
                	for (int i = 0; i < numEntries; i++) {
                		String filePath = SceUtilitySavedataParam.savedataPath + "/" + entries[i];
                		SceIoStat stat = fileManager.statFile(filePath);
                		int entryAddr = entriesAddr + i * 72;
                		if (stat != null) {
                			mem.write32(entryAddr + 0, stat.mode);
                			stat.ctime.write(mem, entryAddr + 4);
                			stat.atime.write(mem, entryAddr + 20);
                			stat.mtime.write(mem, entryAddr + 36);
                		}
                		String entryName = entries[i].substring(savedataParams.gameName.length());
                		Utilities.writeStringNZ(mem, entryAddr + 52, 20, entryName);
                	}
                	mem.write32(buffer4Addr + 4, numEntries);
                }
        		savedataParams.base.result = 0;
                break;
            }

            case SceUtilitySavedataParam.MODE_TEST: {
            	boolean isPresent = false;

            	try {
                    isPresent = savedataParams.test(mem, pspiofilemgr.getInstance());
                } catch (FileNotFoundException e) {
                	isPresent = false;
                } catch (Exception e) {
                }

                if (isPresent) {
                	savedataParams.base.result = ERROR_SAVEDATA_MODE15_SAVEDATA_PRESENT;
                } else {
                    savedataParams.base.result = ERROR_SAVEDATA_MODE15_SAVEDATA_NOT_PRESENT;
                }
                break;
            }

            default:
                Modules.log.warn("Savedata - Unsupported mode " + savedataParams.mode);
                savedataParams.base.result = -1;
                break;
        }

        savedataParams.base.writeResult(mem);
        if (Modules.log.isDebugEnabled()) {
            Modules.log.debug("hleUtilitySavedataDisplay savedResult:0x" + Integer.toHexString(savedataParams.base.result));
        }
    }

	public void sceUtilitySavedataInitStart(Processor processor) {
        savedataParams = new SceUtilitySavedataParam();
        savedataState.executeInitStart(processor, savedataParams);
    }

    public void sceUtilitySavedataShutdownStart(Processor processor) {
    	savedataState.executeShutdownStart(processor);
    }

    public void sceUtilitySavedataUpdate(Processor processor) {
    	if (savedataState.tryUpdate(processor)) {
    		hleUtilitySavedataDisplay();

    		savedataState.endUpdate(processor);
    	}
	}

    public void sceUtilitySavedataGetStatus(Processor processor) {
    	savedataState.executeGetStatus(processor);
    }

	public void sceUtility_2995D020(Processor processor) {
		CpuState cpu = processor.cpu;

        Modules.log.warn("Unimplemented NID function sceUtility_2995D020 [0x2995D020]");

		cpu.gpr[2] = 0xDEADC0DE;
	}

	public void sceUtility_B62A4061(Processor processor) {
		CpuState cpu = processor.cpu;

		Modules.log.warn("Unimplemented NID function sceUtility_B62A4061 [0xB62A4061]");

		cpu.gpr[2] = 0xDEADC0DE;
	}

	public void sceUtility_ED0FAD38(Processor processor) {
		CpuState cpu = processor.cpu;

		Modules.log.warn("Unimplemented NID function sceUtility_ED0FAD38 [0xED0FAD38]");

		cpu.gpr[2] = 0xDEADC0DE;
	}

	public void sceUtility_88BC7406(Processor processor) {
		CpuState cpu = processor.cpu;

		Modules.log.warn("Unimplemented NID function sceUtility_88BC7406 [0x88BC7406]");

		cpu.gpr[2] = 0xDEADC0DE;
	}

    protected void hleUtilityMsgDialogDisplay() {
        Memory mem = Processor.memory;

        String title = String.format("Message from %s", State.title);
        if (msgDialogParams.isOptionYesNo()) {
            int result = JOptionPane.showConfirmDialog(null, formatMessageForDialog(msgDialogParams.message), null, JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                msgDialogParams.buttonPressed = 1;
            } else if (result == JOptionPane.NO_OPTION) {
                msgDialogParams.buttonPressed = 2;
            } else if (result == JOptionPane.CANCEL_OPTION) {
                msgDialogParams.buttonPressed = 3;
            }
        } else if (msgDialogParams.mode == SceUtilityMsgDialogParams.PSP_UTILITY_MSGDIALOG_MODE_TEXT) {
            JOptionPane.showMessageDialog(null, formatMessageForDialog(msgDialogParams.message), title, JOptionPane.INFORMATION_MESSAGE);
        }
        msgDialogParams.base.result = 0;
        msgDialogParams.write(mem);
    }

    public void sceUtilityMsgDialogInitStart(Processor processor) {
        msgDialogParams = new SceUtilityMsgDialogParams();
        msgDialogState.executeInitStart(processor, msgDialogParams);
    }

    public void sceUtilityMsgDialogShutdownStart(Processor processor) {
    	msgDialogState.executeShutdownStart(processor);
    }

	public void sceUtilityMsgDialogUpdate(Processor processor) {
		if (msgDialogState.tryUpdate(processor)) {
			hleUtilityMsgDialogDisplay();
			msgDialogState.endUpdate(processor);
		}
	}

    public void sceUtilityMsgDialogGetStatus(Processor processor) {
    	msgDialogState.executeGetStatus(processor);
    }

    protected void hleUtilityOskDisplay() {
        Memory mem = Processor.memory;

        oskParams.oskData.outText = JOptionPane.showInputDialog(oskParams.oskData.desc, oskParams.oskData.inText);
        oskParams.base.result = 0;
        oskParams.oskData.result = 2; // Unknown value, but required by "SEGA Rally"
        oskParams.write(mem);
        Modules.log.info("hleUtilityOskDisplay returning '" + oskParams.oskData.outText + "'");
    }

	public void sceUtilityOskInitStart(Processor processor) {
        oskParams = new SceUtilityOskParams();
        oskState.executeInitStart(processor, oskParams);
	}

	public void sceUtilityOskShutdownStart(Processor processor) {
		oskState.executeShutdownStart(processor);
	}

	public void sceUtilityOskUpdate(Processor processor) {
		if (oskState.tryUpdate(processor)) {
			hleUtilityOskDisplay();
			oskState.endUpdate(processor);
		}
	}

	public void sceUtilityOskGetStatus(Processor processor) {
		oskState.executeGetStatus(processor);
	}

	public void sceUtilitySetSystemParamInt(Processor processor) {
		CpuState cpu = processor.cpu;

		Modules.log.warn("Unimplemented NID function sceUtilitySetSystemParamInt [0x45C18506]");

		cpu.gpr[2] = 0xDEADC0DE;
	}

	public void sceUtilitySetSystemParamString(Processor processor) {
		CpuState cpu = processor.cpu;

		Modules.log.warn("Unimplemented NID function sceUtilitySetSystemParamString [0x41E30674]");

		cpu.gpr[2] = 0xDEADC0DE;
	}

	public void sceUtilityGetSystemParamInt(Processor processor) {
		CpuState cpu = processor.cpu;
		Memory mem = Processor.memory;

        int id = cpu.gpr[4];
        int value_addr = cpu.gpr[5];

        if (!mem.isAddressGood(value_addr)) {
            Modules.log.warn("sceUtilityGetSystemParamInt(id=" + id + ",value=0x" + Integer.toHexString(value_addr) + ") bad address");
            cpu.gpr[2] = -1;
        } else {
            Modules.log.debug("PARTIAL:sceUtilityGetSystemParamInt(id=" + id + ",value=0x" + Integer.toHexString(value_addr) + ")");

            cpu.gpr[2] = 0;
            switch(id) {
                case PSP_SYSTEMPARAM_ID_INT_DATE_FORMAT:
                    mem.write32(value_addr, systemParam_dateFormat);
                    break;

                case PSP_SYSTEMPARAM_ID_INT_TIME_FORMAT:
                    mem.write32(value_addr, systemParam_timeFormat);
                    break;

                case PSP_SYSTEMPARAM_ID_INT_TIMEZONE:
                    mem.write32(value_addr, systemParam_timeZone);
                    break;

                case PSP_SYSTEMPARAM_ID_INT_LANGUAGE:
                    mem.write32(value_addr, systemParam_language);
                    break;

                case PSP_SYSTEMPARAM_ID_INT_BUTTON_PREFERENCE:
                    mem.write32(value_addr, systemParam_buttonPreference);
                    break;

                default:
                    Modules.log.warn("UNIMPLEMENTED:sceUtilityGetSystemParamInt(id=" + id + ",value=0x" + Integer.toHexString(value_addr) + ") unhandled id");
                    cpu.gpr[2] = -1;
                    break;
            }
        }
	}

	public void sceUtilityGetSystemParamString(Processor processor) {
		CpuState cpu = processor.cpu;
		Memory mem = Processor.memory;

        int id = cpu.gpr[4];
        int str_addr = cpu.gpr[5];
        int len = cpu.gpr[6];

        if (!mem.isAddressGood(str_addr)) {
            Modules.log.warn("sceUtilityGetSystemParamString(id=" + id + ",str=0x" + Integer.toHexString(str_addr) + ",len=" + len + ") bad address");
            cpu.gpr[2] = -1;
        } else {
            Modules.log.debug("PARTIAL:sceUtilityGetSystemParamString(id=" + id + ",str=0x" + Integer.toHexString(str_addr) + ",len=" + len + ")");

            cpu.gpr[2] = 0;
            switch(id) {
                case PSP_SYSTEMPARAM_ID_STRING_NICKNAME:
                    Utilities.writeStringNZ(mem, str_addr, len, systemParam_nickname);
                    break;

                default:
                    Modules.log.warn("UNIMPLEMENTED:sceUtilityGetSystemParamString(id=" + id + ",str=0x" + Integer.toHexString(str_addr) + ",len=" + len + ") unhandled id");
                    cpu.gpr[2] = -1;
                    break;
            }
        }
	}

	public void sceUtilityCheckNetParam(Processor processor) {
		CpuState cpu = processor.cpu;

		int id = cpu.gpr[4];

		Modules.log.warn("UNIMPLEMENTED:sceUtilityCheckNetParam(id=" + id + ")");

		cpu.gpr[2] = PSP_NETPARAM_ERROR_BAD_PARAM;
	}

	public void sceUtilityGetNetParam(Processor processor) {
		CpuState cpu = processor.cpu;

		Modules.log.warn("Unimplemented NID function sceUtilityGetNetParam [0x434D4B3A]");

		cpu.gpr[2] = 0xDEADC0DE;
	}

	public final HLEModuleFunction sceUtilityGameSharingInitStartFunction = new HLEModuleFunction("sceUtility", "sceUtilityGameSharingInitStart") {
		@Override
		public final void execute(Processor processor) {
			sceUtilityGameSharingInitStart(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilityGameSharingInitStart(processor);";
		}
	};

	public final HLEModuleFunction sceUtilityGameSharingShutdownStartFunction = new HLEModuleFunction("sceUtility", "sceUtilityGameSharingShutdownStart") {
		@Override
		public final void execute(Processor processor) {
			sceUtilityGameSharingShutdownStart(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilityGameSharingShutdownStart(processor);";
		}
	};

	public final HLEModuleFunction sceUtilityGameSharingUpdateFunction = new HLEModuleFunction("sceUtility", "sceUtilityGameSharingUpdate") {
		@Override
		public final void execute(Processor processor) {
			sceUtilityGameSharingUpdate(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilityGameSharingUpdate(processor);";
		}
	};

	public final HLEModuleFunction sceUtilityGameSharingGetStatusFunction = new HLEModuleFunction("sceUtility", "sceUtilityGameSharingGetStatus") {
		@Override
		public final void execute(Processor processor) {
			sceUtilityGameSharingGetStatus(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilityGameSharingGetStatus(processor);";
		}
	};

	public final HLEModuleFunction sceNetplayDialogInitStartFunction = new HLEModuleFunction("sceUtility", "sceNetplayDialogInitStart") {
		@Override
		public final void execute(Processor processor) {
			sceNetplayDialogInitStart(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceNetplayDialogInitStart(processor);";
		}
	};

	public final HLEModuleFunction sceNetplayDialogShutdownStartFunction = new HLEModuleFunction("sceUtility", "sceNetplayDialogShutdownStart") {
		@Override
		public final void execute(Processor processor) {
			sceNetplayDialogShutdownStart(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceNetplayDialogShutdownStart(processor);";
		}
	};

	public final HLEModuleFunction sceNetplayDialogUpdateFunction = new HLEModuleFunction("sceUtility", "sceNetplayDialogUpdate") {
		@Override
		public final void execute(Processor processor) {
			sceNetplayDialogUpdate(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceNetplayDialogUpdate(processor);";
		}
	};

	public final HLEModuleFunction sceNetplayDialogGetStatusFunction = new HLEModuleFunction("sceUtility", "sceNetplayDialogGetStatus") {
		@Override
		public final void execute(Processor processor) {
			sceNetplayDialogGetStatus(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceNetplayDialogGetStatus(processor);";
		}
	};

	public final HLEModuleFunction sceUtilityNetconfInitStartFunction = new HLEModuleFunction("sceUtility", "sceUtilityNetconfInitStart") {
		@Override
		public final void execute(Processor processor) {
			sceUtilityNetconfInitStart(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilityNetconfInitStart(processor);";
		}
	};

	public final HLEModuleFunction sceUtilityNetconfShutdownStartFunction = new HLEModuleFunction("sceUtility", "sceUtilityNetconfShutdownStart") {
		@Override
		public final void execute(Processor processor) {
			sceUtilityNetconfShutdownStart(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilityNetconfShutdownStart(processor);";
		}
	};

	public final HLEModuleFunction sceUtilityNetconfUpdateFunction = new HLEModuleFunction("sceUtility", "sceUtilityNetconfUpdate") {
		@Override
		public final void execute(Processor processor) {
			sceUtilityNetconfUpdate(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilityNetconfUpdate(processor);";
		}
	};

	public final HLEModuleFunction sceUtilityNetconfGetStatusFunction = new HLEModuleFunction("sceUtility", "sceUtilityNetconfGetStatus") {
		@Override
		public final void execute(Processor processor) {
			sceUtilityNetconfGetStatus(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilityNetconfGetStatus(processor);";
		}
	};

	public final HLEModuleFunction sceUtilitySavedataInitStartFunction = new HLEModuleFunction("sceUtility", "sceUtilitySavedataInitStart") {
		@Override
		public final void execute(Processor processor) {
			sceUtilitySavedataInitStart(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilitySavedataInitStart(processor);";
		}
	};

	public final HLEModuleFunction sceUtilitySavedataShutdownStartFunction = new HLEModuleFunction("sceUtility", "sceUtilitySavedataShutdownStart") {
		@Override
		public final void execute(Processor processor) {
			sceUtilitySavedataShutdownStart(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilitySavedataShutdownStart(processor);";
		}
	};

	public final HLEModuleFunction sceUtilitySavedataUpdateFunction = new HLEModuleFunction("sceUtility", "sceUtilitySavedataUpdate") {
		@Override
		public final void execute(Processor processor) {
			sceUtilitySavedataUpdate(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilitySavedataUpdate(processor);";
		}
	};

	public final HLEModuleFunction sceUtilitySavedataGetStatusFunction = new HLEModuleFunction("sceUtility", "sceUtilitySavedataGetStatus") {
		@Override
		public final void execute(Processor processor) {
			sceUtilitySavedataGetStatus(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilitySavedataGetStatus(processor);";
		}
	};

	public final HLEModuleFunction sceUtility_2995D020Function = new HLEModuleFunction("sceUtility", "sceUtility_2995D020") {
		@Override
		public final void execute(Processor processor) {
			sceUtility_2995D020(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtility_2995D020(processor);";
		}
	};

	public final HLEModuleFunction sceUtility_B62A4061Function = new HLEModuleFunction("sceUtility", "sceUtility_B62A4061") {
		@Override
		public final void execute(Processor processor) {
			sceUtility_B62A4061(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtility_B62A4061(processor);";
		}
	};

	public final HLEModuleFunction sceUtility_ED0FAD38Function = new HLEModuleFunction("sceUtility", "sceUtility_ED0FAD38") {
		@Override
		public final void execute(Processor processor) {
			sceUtility_ED0FAD38(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtility_ED0FAD38(processor);";
		}
	};

	public final HLEModuleFunction sceUtility_88BC7406Function = new HLEModuleFunction("sceUtility", "sceUtility_88BC7406") {
		@Override
		public final void execute(Processor processor) {
			sceUtility_88BC7406(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtility_88BC7406(processor);";
		}
	};

	public final HLEModuleFunction sceUtilityMsgDialogInitStartFunction = new HLEModuleFunction("sceUtility", "sceUtilityMsgDialogInitStart") {
		@Override
		public final void execute(Processor processor) {
			sceUtilityMsgDialogInitStart(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilityMsgDialogInitStart(processor);";
		}
	};

	public final HLEModuleFunction sceUtilityMsgDialogShutdownStartFunction = new HLEModuleFunction("sceUtility", "sceUtilityMsgDialogShutdownStart") {
		@Override
		public final void execute(Processor processor) {
			sceUtilityMsgDialogShutdownStart(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilityMsgDialogShutdownStart(processor);";
		}
	};

	public final HLEModuleFunction sceUtilityMsgDialogUpdateFunction = new HLEModuleFunction("sceUtility", "sceUtilityMsgDialogUpdate") {
		@Override
		public final void execute(Processor processor) {
			sceUtilityMsgDialogUpdate(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilityMsgDialogUpdate(processor);";
		}
	};

	public final HLEModuleFunction sceUtilityMsgDialogGetStatusFunction = new HLEModuleFunction("sceUtility", "sceUtilityMsgDialogGetStatus") {
		@Override
		public final void execute(Processor processor) {
			sceUtilityMsgDialogGetStatus(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilityMsgDialogGetStatus(processor);";
		}
	};

	public final HLEModuleFunction sceUtilityOskInitStartFunction = new HLEModuleFunction("sceUtility", "sceUtilityOskInitStart") {
		@Override
		public final void execute(Processor processor) {
			sceUtilityOskInitStart(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilityOskInitStart(processor);";
		}
	};

	public final HLEModuleFunction sceUtilityOskShutdownStartFunction = new HLEModuleFunction("sceUtility", "sceUtilityOskShutdownStart") {
		@Override
		public final void execute(Processor processor) {
			sceUtilityOskShutdownStart(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilityOskShutdownStart(processor);";
		}
	};

	public final HLEModuleFunction sceUtilityOskUpdateFunction = new HLEModuleFunction("sceUtility", "sceUtilityOskUpdate") {
		@Override
		public final void execute(Processor processor) {
			sceUtilityOskUpdate(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilityOskUpdate(processor);";
		}
	};

	public final HLEModuleFunction sceUtilityOskGetStatusFunction = new HLEModuleFunction("sceUtility", "sceUtilityOskGetStatus") {
		@Override
		public final void execute(Processor processor) {
			sceUtilityOskGetStatus(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilityOskGetStatus(processor);";
		}
	};

	public final HLEModuleFunction sceUtilitySetSystemParamIntFunction = new HLEModuleFunction("sceUtility", "sceUtilitySetSystemParamInt") {
		@Override
		public final void execute(Processor processor) {
			sceUtilitySetSystemParamInt(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilitySetSystemParamInt(processor);";
		}
	};

	public final HLEModuleFunction sceUtilitySetSystemParamStringFunction = new HLEModuleFunction("sceUtility", "sceUtilitySetSystemParamString") {
		@Override
		public final void execute(Processor processor) {
			sceUtilitySetSystemParamString(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilitySetSystemParamString(processor);";
		}
	};

	public final HLEModuleFunction sceUtilityGetSystemParamIntFunction = new HLEModuleFunction("sceUtility", "sceUtilityGetSystemParamInt") {
		@Override
		public final void execute(Processor processor) {
			sceUtilityGetSystemParamInt(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilityGetSystemParamInt(processor);";
		}
	};

	public final HLEModuleFunction sceUtilityGetSystemParamStringFunction = new HLEModuleFunction("sceUtility", "sceUtilityGetSystemParamString") {
		@Override
		public final void execute(Processor processor) {
			sceUtilityGetSystemParamString(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilityGetSystemParamString(processor);";
		}
	};

	public final HLEModuleFunction sceUtilityCheckNetParamFunction = new HLEModuleFunction("sceUtility", "sceUtilityCheckNetParam") {
		@Override
		public final void execute(Processor processor) {
			sceUtilityCheckNetParam(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilityCheckNetParam(processor);";
		}
	};

	public final HLEModuleFunction sceUtilityGetNetParamFunction = new HLEModuleFunction("sceUtility", "sceUtilityGetNetParam") {
		@Override
		public final void execute(Processor processor) {
			sceUtilityGetNetParam(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUtilityModule.sceUtilityGetNetParam(processor);";
		}
	};

};