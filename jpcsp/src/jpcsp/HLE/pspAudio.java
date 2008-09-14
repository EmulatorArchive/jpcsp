/*
Function:
- HLE everything in http://psp.jim.sh/pspsdk-doc/group__Audio.html


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
package jpcsp.HLE;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import jpcsp.Emulator;
import jpcsp.Memory;
import jpcsp.Processor;
/**
 *
 * @author shadow
 */
public class pspAudio {
    
    private class pspChannelInfo {
        
        public boolean reserved;
        public int allocatedSamples;
        public int format;
        public int lvol;
        public int rvol;
        public SourceDataLine outputDataLine;
        
        public pspChannelInfo()
        {
            reserved=false;
            allocatedSamples = 0;
            format = 0;
            lvol = 0x8000;
            rvol = 0x8000;
            outputDataLine = null;
        }
    }
    
    private pspChannelInfo[] pspchannels; // psp channels
    private int sampleRate;

    private static pspAudio instance;
    
    public static pspAudio get_instance() {
        if (instance == null) {
            instance = new pspAudio();
        }
        return instance;
    }
    public pspAudio()
    {
        pspchannels = new pspChannelInfo[8];
        for(int i=0;i<8;i++)
        {
            pspchannels[i]=new pspChannelInfo();
        }

        sampleRate = 48000;
    }
    
    final int PSP_AUDIO_VOLUME_MAX = 0x8000;
    final int PSP_AUDIO_CHANNEL_MAX = 8;
    final int PSP_AUDIO_NEXT_CHANNEL = (-1);
    final int PSP_AUDIO_SAMPLE_MIN = 64;
    final int PSP_AUDIO_SAMPLE_MAX = 65472;

    final int PSP_AUDIO_FORMAT_STEREO = 0;
    final int PSP_AUDIO_FORMAT_MONO = 0x10; 
    
    final int PSP_AUDIO_FREQ_44K = 44100;
    final int PSP_AUDIO_FREQ_48K = 48000;
    
    public void sceAudioSetFrequency (int frequency)
    {
        int ret = -1;
        if(frequency==44100 || frequency==48000)
        {
            sampleRate = frequency;
            ret = 0;
        }
        PSPThread.currentPSPThread().processor.gpr[2] = ret; //just return the first channel
    }    
    
    //Allocate and initialize a hardware output channel.
    public void sceAudioChReserve (int channel, int samplecount, int format)
    {
        if(true)
        {
            System.out.println("IGNORED sceAudioChReserve channel= " + channel + " samplecount = " + samplecount + " format = " + format);
            PSPThread.currentPSPThread().processor.gpr[2] = -1;
        }
        else
        {
            System.out.println("sceAudioChReserve channel= " + channel + " samplecount = " + samplecount + " format = " + format);

            if(channel!=-1) // use specified channel, if available
            {
                if(pspchannels[channel].reserved)
                {
                    channel = -1;
                }        
            }
            else // find first free channel
            {
                for(int i=0;i<8;i++)
                {
                    if(!pspchannels[i].reserved)
                    {
                        channel=i;
                        break;
                    }
                }

            }            

            if(channel!=-1) // if channel == -1 here, it means we couldn't use any.
            {
                pspchannels[channel].reserved = true;
                pspchannels[channel].outputDataLine = null; // delay creation until first use.
                pspchannels[channel].allocatedSamples = samplecount;
                pspchannels[channel].format = format;
            }
            PSPThread.currentPSPThread().processor.gpr[2] = channel;
        }
    }

    //Release a hardware output channel.
    public void sceAudioChRelease (int channel)
    {
        int ret = -1;
        if(pspchannels[channel].reserved)
        {
            if(pspchannels[channel].outputDataLine!=null)
            {
                pspchannels[channel].outputDataLine.stop();
                pspchannels[channel].outputDataLine.close();
            }
            pspchannels[channel].outputDataLine=null;
            pspchannels[channel].reserved=false;
            ret = 0;
        }
        PSPThread.currentPSPThread().processor.gpr[2] = ret; //just return the first channel
    }

    private int doAudioOutput (int channel, int pvoid_buf)
    {
        int ret = -1;
        
        if(pspchannels[channel].reserved)
        {
            if(pspchannels[channel].outputDataLine == null) // if not yet initialized, do it now.
            {
                try {
                    pspchannels[channel].outputDataLine = AudioSystem.getSourceDataLine(new AudioFormat(sampleRate, 16, 2, true, false));
                    sceAudioChangeChannelVolume(channel,pspchannels[channel].lvol,pspchannels[channel].rvol);
                }
                catch(LineUnavailableException e)
                {
                    System.out.println("Exception trying to create channel output line. Channel " + channel + " will be silent.");
                    pspchannels[channel].outputDataLine = null;
                    channel=-1;
                }
            }
            
            if(pspchannels[channel].outputDataLine != null) // if we couldn't initialize the audio line, just ignore the audio output.
            {
                int channels = ((pspchannels[channel].format&0x10)==0x10)?1:2;
                int bytespersample = 4; //2*channels;
                
                int bytes = bytespersample * pspchannels[channel].allocatedSamples;
                
                byte[] data = new byte[bytes];
                
                /*
                for(int i=0;i<bytes;i++)
                {
                    data[i] = (byte)Emulator.getMemory().read8(pvoid_buf+i);
                }
                 */
                
                // process audio volumes ourselves for now
                if(channels == 1)
                {
                    int nsamples = pspchannels[channel].allocatedSamples;
                    for(int i=0;i<nsamples;i++)
                    {
                        short lval = (short) Memory.read16(pvoid_buf+i);
                        short rval = lval;

                        lval = (short)((((int)lval)*pspchannels[channel].lvol)>>16);
                        rval = (short)((((int)rval)*pspchannels[channel].rvol)>>16);

                        data[i*4+0] = (byte)(lval);
                        data[i*4+1] = (byte)(lval>>8);
                        data[i*4+2] = (byte)(rval);
                        data[i*4+3] = (byte)(rval>>8);
                    }
                }
                else
                {
                    int nsamples = pspchannels[channel].allocatedSamples;
                    for(int i=0;i<nsamples;i++)
                    {
                        short lval = (short)Emulator.getMemory().read16(pvoid_buf+i*2);
                        short rval = (short)Emulator.getMemory().read16(pvoid_buf+i*2+1);

                        lval = (short)((((int)lval)*pspchannels[channel].lvol)>>16);
                        rval = (short)((((int)rval)*pspchannels[channel].rvol)>>16);

                        data[i*4+0] = (byte)(lval);
                        data[i*4+1] = (byte)(lval>>8);
                        data[i*4+2] = (byte)(rval);
                        data[i*4+3] = (byte)(rval>>8);
                    }
                }
                
                pspchannels[channel].outputDataLine.write(data,0,data.length);
                pspchannels[channel].outputDataLine.start();

                ret = 0;
            }
        }
        
        return ret; //just return the first channel
    }
    
    private int doAudioFlush(int channel)
    {
        if(pspchannels[channel].outputDataLine != null)
        {
            pspchannels[channel].outputDataLine.drain();
            return 0;
        }
        return -1;
    }
    
    //Output audio of the specified channel.
    public void sceAudioOutput (int channel, int vol, int pvoid_buf)
    {
        int ret = -1;

        sceAudioChangeChannelVolume(channel, vol, vol);
        ret = doAudioOutput(channel, pvoid_buf);
        
        PSPThread.currentPSPThread().processor.gpr[2] = ret; //just return the first channel
    }

    //Output audio of the specified channel (blocking).
    public void sceAudioOutputBlocking (int channel, int vol, int pvoid_buf)
    {
        int ret = 0;

        sceAudioChangeChannelVolume(channel, vol, vol);
        ret = doAudioOutput(channel, pvoid_buf);
        if(ret>=0)
        {
            ret = doAudioFlush(channel);
        }

        PSPThread.currentPSPThread().processor.gpr[2] = ret; //just return the first channel
    }

    //Output panned audio of the specified channel.
    public void sceAudioOutputPanned (int channel, int leftvol, int rightvol, int pvoid_buf)
    {
        int ret = -1;
        sceAudioChangeChannelVolume(channel, leftvol, rightvol);
        ret = doAudioOutput(channel,pvoid_buf);
        PSPThread.currentPSPThread().processor.gpr[2] = ret;
    }

    //Output panned audio of the specified channel (blocking).
    public void sceAudioOutputPannedBlocking (int channel, int leftvol, int rightvol, int pvoid_buf)
    {
        int ret = -1;
        sceAudioChangeChannelVolume(channel, leftvol, rightvol);
        ret = doAudioOutput(channel,pvoid_buf);
        if(ret>=0) ret=doAudioFlush(channel);
        PSPThread.currentPSPThread().processor.gpr[2] = ret;
    }
    
    //Get count of unplayed samples remaining.
    public void sceAudioGetChannelRestLen (int channel)
    {
        int ret = -1;
        if(pspchannels[channel].outputDataLine != null)
        {
            int bytespersample = 4;
            
            if((pspchannels[channel].format&0x10)==0x10) bytespersample=2;
            
            ret = pspchannels[channel].outputDataLine.available() / (bytespersample);
        }
        
        PSPThread.currentPSPThread().processor.gpr[2] = ret;
    }

    //Change the output sample count, after it's already been reserved.
    public void sceAudioSetChannelDataLen (int channel, int samplecount)
    {
        pspchannels[channel].allocatedSamples = samplecount;
        PSPThread.currentPSPThread().processor.gpr[2] = 0;
    }
    
    //Change the format of a channel.
    public void sceAudioChangeChannelConfig (int channel, int format)
    {
        pspchannels[channel].format = format;
        PSPThread.currentPSPThread().processor.gpr[2] = 0; //just return the first channel
    }    

    //Change the volume of a channel.
    public void sceAudioChangeChannelVolume (int channel, int leftvol, int rightvol)
    {
        int ret = -1;
    
        if(pspchannels[channel].reserved)
        {
            /* doing the audio processing myself on doAudioOutput for now
             
            if(pspchannels[channel].outputDataLine != null)
            {
                
                FloatControl cvolume  = (FloatControl)pspchannels[channel].outputDataLine.getControl(FloatControl.Type.VOLUME);
                FloatControl cpanning;
                
                if((pspchannels[channel].format&0x10)==0x10)
                     cpanning = (FloatControl)pspchannels[channel].outputDataLine.getControl(FloatControl.Type.PAN);
                else
                     cpanning = (FloatControl)pspchannels[channel].outputDataLine.getControl(FloatControl.Type.BALANCE);

                float vvolume  = Math.max(leftvol,rightvol);
                float balance = (rightvol-leftvol)/vvolume;
                
                cvolume.setValue(vvolume/32768.0f);
                cpanning.setValue(balance);
                
                ret = 0;
            }
            else*/
            {
                pspchannels[channel].lvol = leftvol;
                pspchannels[channel].rvol = rightvol;
                ret = 0;
            }
        }
        PSPThread.currentPSPThread().processor.gpr[2] = ret; //just return the first channel
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Functions after this point are not yet implemented.
    
    //Reserve the audio output and set the output sample count.
    public void sceAudioOutput2Reserve (int samplecount)
    {
    PSPThread.currentPSPThread().processor.gpr[2] = -1; //just return the first channel
    }
    //Release the audio output.
    public void sceAudioOutput2Release ()
    {
    PSPThread.currentPSPThread().processor.gpr[2] = -1; //just return the first channel
    }
    //Change the output sample count, after it's already been reserved.
    public void sceAudioOutput2ChangeLength (int samplecount)
    {
    PSPThread.currentPSPThread().processor.gpr[2] = -1; //just return the first channel
    }
    //Output audio (blocking).
    public void sceAudioOutput2OutputBlocking (int vol, int pvoid_buf)
    {
    PSPThread.currentPSPThread().processor.gpr[2] = -1; //just return the first channel
    }
    //Get count of unplayed samples remaining.
    public void sceAudioOutput2GetRestSample ()
    {
    PSPThread.currentPSPThread().processor.gpr[2] = -1; //just return the first channel
    }
    //Reserve the audio output.
    public void sceAudioSRCChReserve (int samplecount, int freq, int channels)
    {
    PSPThread.currentPSPThread().processor.gpr[2] = -1; //just return the first channel
    }
    //Release the audio output.
    public void sceAudioSRCChRelease ()
    {
    PSPThread.currentPSPThread().processor.gpr[2] = -1; //just return the first channel
    }
    //Output audio.
    public void sceAudioSRCOutputBlocking (int vol, int pvoid_buf)
    {
    PSPThread.currentPSPThread().processor.gpr[2] = -1; //just return the first channel
    }
    //Init audio input.
    public void sceAudioInputInit (int unknown1, int gain, int unknown2)
    {
    PSPThread.currentPSPThread().processor.gpr[2] = -1; //just return the first channel
    }
    //Init audio input (with extra arguments).
    public void sceAudioInputInitEx (int p_pspAudioInputParams_params)
    {
    PSPThread.currentPSPThread().processor.gpr[2] = -1; //just return the first channel
    }
    //Perform audio input (blocking).
    public void sceAudioInputBlocking (int samplecount, int freq, int pvoid_buf)
    {
    PSPThread.currentPSPThread().processor.gpr[2] = -1; //just return the first channel
    }
    //Perform audio input.
    public void sceAudioInput (int samplecount, int freq, int pvoid_buf)
    {
    PSPThread.currentPSPThread().processor.gpr[2] = -1; //just return the first channel
    }
    //Get the number of samples that were acquired.
    public void sceAudioGetInputLength ()
    {
    PSPThread.currentPSPThread().processor.gpr[2] = -1; //just return the first channel
    }
    //Wait for non-blocking audio input to complete.
    public void sceAudioWaitInputEnd ()
    {
    PSPThread.currentPSPThread().processor.gpr[2] = -1; //just return the first channel
    }
    //Poll for non-blocking audio input status.
    public void sceAudioPollInputEnd ()
    {
    PSPThread.currentPSPThread().processor.gpr[2] = -1; //just return the first channel
    }

}
