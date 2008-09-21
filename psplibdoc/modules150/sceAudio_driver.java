/* This autogenerated file is part of jpcsp. */
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

import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;

import jpcsp.Memory;
import jpcsp.Processor;

import jpcsp.Allegrex.CpuState; // New-Style Processor

public class sceAudio_driver implements HLEModule {
	@Override
	public String getName() { return "sceAudio_driver"; }
	
	@Override
	public void installModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
			
			mm.addFunction(sceAudioInitFunction, 0x80F1F7E0);
			
			mm.addFunction(sceAudioEndFunction, 0x210567F7);
			
			mm.addFunction(sceAudioSetFrequencyFunction, 0xA2BEAA6C);
			
			mm.addFunction(sceAudioLoopbackTestFunction, 0xB61595C0);
			
			mm.addFunction(sceAudioSetVolumeOffsetFunction, 0x927AC32B);
			
			mm.addFunction(sceAudioOutputFunction, 0x8C1009B2);
			
			mm.addFunction(sceAudioOutputBlockingFunction, 0x136CAF51);
			
			mm.addFunction(sceAudioOutputPannedFunction, 0xE2D56B2D);
			
			mm.addFunction(sceAudioOutputPannedBlockingFunction, 0x13F592BC);
			
			mm.addFunction(sceAudioChReserveFunction, 0x5EC81C55);
			
			mm.addFunction(sceAudioOneshotOutputFunction, 0x41EFADE7);
			
			mm.addFunction(sceAudioChReleaseFunction, 0x6FC46853);
			
			mm.addFunction(sceAudioGetChannelRestLengthFunction, 0xB011922F);
			
			mm.addFunction(sceAudioSetChannelDataLenFunction, 0xCB2E439E);
			
			mm.addFunction(sceAudioChangeChannelConfigFunction, 0x95FD0C2D);
			
			mm.addFunction(sceAudioChangeChannelVolumeFunction, 0xB7E1D8E7);
			
			mm.addFunction(sceAudioSRCChReserveFunction, 0x38553111);
			
			mm.addFunction(sceAudioSRCChReleaseFunction, 0x5C37C0AE);
			
			mm.addFunction(sceAudioSRCOutputBlockingFunction, 0xE0727056);
			
			mm.addFunction(sceAudioInputBlockingFunction, 0x086E5895);
			
			mm.addFunction(sceAudioInputFunction, 0x6D4BEC68);
			
			mm.addFunction(sceAudioGetInputLengthFunction, 0xA708C6A6);
			
			mm.addFunction(sceAudioWaitInputEndFunction, 0x87B2E651);
			
			mm.addFunction(sceAudioInputInitFunction, 0x7DE61688);
			
			mm.addFunction(sceAudioInputInitExFunction, 0xE926D3FB);
			
			mm.addFunction(sceAudioPollInputEndFunction, 0xA633048E);
			
			mm.addFunction(sceAudioGetChannelRestLenFunction, 0xE9D97901);
			
		}
	}
	
	@Override
	public void uninstallModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
			
			mm.removeFunction(sceAudioInitFunction);
			
			mm.removeFunction(sceAudioEndFunction);
			
			mm.removeFunction(sceAudioSetFrequencyFunction);
			
			mm.removeFunction(sceAudioLoopbackTestFunction);
			
			mm.removeFunction(sceAudioSetVolumeOffsetFunction);
			
			mm.removeFunction(sceAudioOutputFunction);
			
			mm.removeFunction(sceAudioOutputBlockingFunction);
			
			mm.removeFunction(sceAudioOutputPannedFunction);
			
			mm.removeFunction(sceAudioOutputPannedBlockingFunction);
			
			mm.removeFunction(sceAudioChReserveFunction);
			
			mm.removeFunction(sceAudioOneshotOutputFunction);
			
			mm.removeFunction(sceAudioChReleaseFunction);
			
			mm.removeFunction(sceAudioGetChannelRestLengthFunction);
			
			mm.removeFunction(sceAudioSetChannelDataLenFunction);
			
			mm.removeFunction(sceAudioChangeChannelConfigFunction);
			
			mm.removeFunction(sceAudioChangeChannelVolumeFunction);
			
			mm.removeFunction(sceAudioSRCChReserveFunction);
			
			mm.removeFunction(sceAudioSRCChReleaseFunction);
			
			mm.removeFunction(sceAudioSRCOutputBlockingFunction);
			
			mm.removeFunction(sceAudioInputBlockingFunction);
			
			mm.removeFunction(sceAudioInputFunction);
			
			mm.removeFunction(sceAudioGetInputLengthFunction);
			
			mm.removeFunction(sceAudioWaitInputEndFunction);
			
			mm.removeFunction(sceAudioInputInitFunction);
			
			mm.removeFunction(sceAudioInputInitExFunction);
			
			mm.removeFunction(sceAudioPollInputEndFunction);
			
			mm.removeFunction(sceAudioGetChannelRestLenFunction);
			
		}
	}
	
	
	public void sceAudioInit(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioInit [0x80F1F7E0]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioEnd(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioEnd [0x210567F7]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioSetFrequency(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioSetFrequency [0xA2BEAA6C]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioLoopbackTest(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioLoopbackTest [0xB61595C0]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioSetVolumeOffset(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioSetVolumeOffset [0x927AC32B]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioOutput(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioOutput [0x8C1009B2]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioOutputBlocking(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioOutputBlocking [0x136CAF51]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioOutputPanned(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioOutputPanned [0xE2D56B2D]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioOutputPannedBlocking(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioOutputPannedBlocking [0x13F592BC]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioChReserve(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioChReserve [0x5EC81C55]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioOneshotOutput(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioOneshotOutput [0x41EFADE7]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioChRelease(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioChRelease [0x6FC46853]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioGetChannelRestLength(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioGetChannelRestLength [0xB011922F]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioSetChannelDataLen(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioSetChannelDataLen [0xCB2E439E]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioChangeChannelConfig(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioChangeChannelConfig [0x95FD0C2D]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioChangeChannelVolume(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioChangeChannelVolume [0xB7E1D8E7]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioSRCChReserve(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioSRCChReserve [0x38553111]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioSRCChRelease(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioSRCChRelease [0x5C37C0AE]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioSRCOutputBlocking(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioSRCOutputBlocking [0xE0727056]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioInputBlocking(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioInputBlocking [0x086E5895]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioInput(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioInput [0x6D4BEC68]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioGetInputLength(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioGetInputLength [0xA708C6A6]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioWaitInputEnd(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioWaitInputEnd [0x87B2E651]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioInputInit(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioInputInit [0x7DE61688]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioInputInitEx(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioInputInitEx [0xE926D3FB]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioPollInputEnd(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioPollInputEnd [0xA633048E]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceAudioGetChannelRestLen(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceAudioGetChannelRestLen [0xE9D97901]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public final HLEModuleFunction sceAudioInitFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioInit") {
		@Override
		public final void execute(Processor processor) {
			sceAudioInit(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioInitFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioEndFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioEnd") {
		@Override
		public final void execute(Processor processor) {
			sceAudioEnd(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioEndFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioSetFrequencyFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioSetFrequency") {
		@Override
		public final void execute(Processor processor) {
			sceAudioSetFrequency(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioSetFrequencyFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioLoopbackTestFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioLoopbackTest") {
		@Override
		public final void execute(Processor processor) {
			sceAudioLoopbackTest(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioLoopbackTestFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioSetVolumeOffsetFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioSetVolumeOffset") {
		@Override
		public final void execute(Processor processor) {
			sceAudioSetVolumeOffset(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioSetVolumeOffsetFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioOutputFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioOutput") {
		@Override
		public final void execute(Processor processor) {
			sceAudioOutput(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioOutputFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioOutputBlockingFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioOutputBlocking") {
		@Override
		public final void execute(Processor processor) {
			sceAudioOutputBlocking(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioOutputBlockingFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioOutputPannedFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioOutputPanned") {
		@Override
		public final void execute(Processor processor) {
			sceAudioOutputPanned(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioOutputPannedFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioOutputPannedBlockingFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioOutputPannedBlocking") {
		@Override
		public final void execute(Processor processor) {
			sceAudioOutputPannedBlocking(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioOutputPannedBlockingFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioChReserveFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioChReserve") {
		@Override
		public final void execute(Processor processor) {
			sceAudioChReserve(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioChReserveFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioOneshotOutputFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioOneshotOutput") {
		@Override
		public final void execute(Processor processor) {
			sceAudioOneshotOutput(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioOneshotOutputFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioChReleaseFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioChRelease") {
		@Override
		public final void execute(Processor processor) {
			sceAudioChRelease(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioChReleaseFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioGetChannelRestLengthFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioGetChannelRestLength") {
		@Override
		public final void execute(Processor processor) {
			sceAudioGetChannelRestLength(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioGetChannelRestLengthFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioSetChannelDataLenFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioSetChannelDataLen") {
		@Override
		public final void execute(Processor processor) {
			sceAudioSetChannelDataLen(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioSetChannelDataLenFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioChangeChannelConfigFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioChangeChannelConfig") {
		@Override
		public final void execute(Processor processor) {
			sceAudioChangeChannelConfig(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioChangeChannelConfigFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioChangeChannelVolumeFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioChangeChannelVolume") {
		@Override
		public final void execute(Processor processor) {
			sceAudioChangeChannelVolume(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioChangeChannelVolumeFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioSRCChReserveFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioSRCChReserve") {
		@Override
		public final void execute(Processor processor) {
			sceAudioSRCChReserve(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioSRCChReserveFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioSRCChReleaseFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioSRCChRelease") {
		@Override
		public final void execute(Processor processor) {
			sceAudioSRCChRelease(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioSRCChReleaseFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioSRCOutputBlockingFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioSRCOutputBlocking") {
		@Override
		public final void execute(Processor processor) {
			sceAudioSRCOutputBlocking(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioSRCOutputBlockingFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioInputBlockingFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioInputBlocking") {
		@Override
		public final void execute(Processor processor) {
			sceAudioInputBlocking(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioInputBlockingFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioInputFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioInput") {
		@Override
		public final void execute(Processor processor) {
			sceAudioInput(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioInputFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioGetInputLengthFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioGetInputLength") {
		@Override
		public final void execute(Processor processor) {
			sceAudioGetInputLength(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioGetInputLengthFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioWaitInputEndFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioWaitInputEnd") {
		@Override
		public final void execute(Processor processor) {
			sceAudioWaitInputEnd(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioWaitInputEndFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioInputInitFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioInputInit") {
		@Override
		public final void execute(Processor processor) {
			sceAudioInputInit(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioInputInitFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioInputInitExFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioInputInitEx") {
		@Override
		public final void execute(Processor processor) {
			sceAudioInputInitEx(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioInputInitExFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioPollInputEndFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioPollInputEnd") {
		@Override
		public final void execute(Processor processor) {
			sceAudioPollInputEnd(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioPollInputEndFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceAudioGetChannelRestLenFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioGetChannelRestLen") {
		@Override
		public final void execute(Processor processor) {
			sceAudioGetChannelRestLen(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceAudio_driver.sceAudioGetChannelRestLenFunction.execute(processor);";
		}
	};
    
};
