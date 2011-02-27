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

import java.util.Calendar;
import java.util.GregorianCalendar;

import jpcsp.Emulator;
import jpcsp.Memory;
import jpcsp.Processor;
import jpcsp.Allegrex.CpuState;
import jpcsp.HLE.Modules;
import jpcsp.HLE.kernel.types.ScePspDateTime;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;
import jpcsp.util.Utilities;

import org.apache.log4j.Logger;

public class sceRtc implements HLEModule {
    protected static Logger log = Modules.getLogger("sceRtc");

    @Override
    public String getName() { return "sceRtc"; }

    @Override
    public void installModule(HLEModuleManager mm, int version) {
        if (version >= 150) {

            mm.addFunction(0xC41C2853, sceRtcGetTickResolutionFunction);
            mm.addFunction(0x3F7AD767, sceRtcGetCurrentTickFunction);
            mm.addFunction(0x011F03C1, sceRtcGetAccumulativeTimeFunction);
            mm.addFunction(0x029CA3B3, sceRtcGetAccumlativeTimeFunction);
            mm.addFunction(0x4CFA57B0, sceRtcGetCurrentClockFunction);
            mm.addFunction(0xE7C27D1B, sceRtcGetCurrentClockLocalTimeFunction);
            mm.addFunction(0x34885E0D, sceRtcConvertUtcToLocalTimeFunction);
            mm.addFunction(0x779242A2, sceRtcConvertLocalTimeToUTCFunction);
            mm.addFunction(0x42307A17, sceRtcIsLeapYearFunction);
            mm.addFunction(0x05EF322C, sceRtcGetDaysInMonthFunction);
            mm.addFunction(0x57726BC1, sceRtcGetDayOfWeekFunction);
            mm.addFunction(0x4B1B5E82, sceRtcCheckValidFunction);
            mm.addFunction(0x3A807CC8, sceRtcSetTime_tFunction);
            mm.addFunction(0x27C4594C, sceRtcGetTime_tFunction);
            mm.addFunction(0xF006F264, sceRtcSetDosTimeFunction);
            mm.addFunction(0x36075567, sceRtcGetDosTimeFunction);
            mm.addFunction(0x7ACE4C04, sceRtcSetWin32FileTimeFunction);
            mm.addFunction(0xCF561893, sceRtcGetWin32FileTimeFunction);
            mm.addFunction(0x7ED29E40, sceRtcSetTickFunction);
            mm.addFunction(0x6FF40ACC, sceRtcGetTickFunction);
            mm.addFunction(0x9ED0AE87, sceRtcCompareTickFunction);
            mm.addFunction(0x44F45E05, sceRtcTickAddTicksFunction);
            mm.addFunction(0x26D25A5D, sceRtcTickAddMicrosecondsFunction);
            mm.addFunction(0xF2A4AFE5, sceRtcTickAddSecondsFunction);
            mm.addFunction(0xE6605BCA, sceRtcTickAddMinutesFunction);
            mm.addFunction(0x26D7A24A, sceRtcTickAddHoursFunction);
            mm.addFunction(0xE51B4B7A, sceRtcTickAddDaysFunction);
            mm.addFunction(0xCF3A2CA8, sceRtcTickAddWeeksFunction);
            mm.addFunction(0xDBF74F1B, sceRtcTickAddMonthsFunction);
            mm.addFunction(0x42842C77, sceRtcTickAddYearsFunction);
            mm.addFunction(0xC663B3B9, sceRtcFormatRFC2822Function);
            mm.addFunction(0x7DE6711B, sceRtcFormatRFC2822LocalTimeFunction);
            mm.addFunction(0x0498FB3C, sceRtcFormatRFC3339Function);
            mm.addFunction(0x27F98543, sceRtcFormatRFC3339LocalTimeFunction);
            mm.addFunction(0xDFBC5F16, sceRtcParseDateTimeFunction);
            mm.addFunction(0x28E1E988, sceRtcParseRFC3339Function);

        }
    }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) {
        if (version >= 150) {

            mm.removeFunction(sceRtcGetTickResolutionFunction);
            mm.removeFunction(sceRtcGetCurrentTickFunction);
            mm.removeFunction(sceRtcGetAccumulativeTimeFunction);
            mm.removeFunction(sceRtcGetAccumlativeTimeFunction);
            mm.removeFunction(sceRtcGetCurrentClockFunction);
            mm.removeFunction(sceRtcGetCurrentClockLocalTimeFunction);
            mm.removeFunction(sceRtcConvertUtcToLocalTimeFunction);
            mm.removeFunction(sceRtcConvertLocalTimeToUTCFunction);
            mm.removeFunction(sceRtcIsLeapYearFunction);
            mm.removeFunction(sceRtcGetDaysInMonthFunction);
            mm.removeFunction(sceRtcGetDayOfWeekFunction);
            mm.removeFunction(sceRtcCheckValidFunction);
            mm.removeFunction(sceRtcSetTime_tFunction);
            mm.removeFunction(sceRtcGetTime_tFunction);
            mm.removeFunction(sceRtcSetDosTimeFunction);
            mm.removeFunction(sceRtcGetDosTimeFunction);
            mm.removeFunction(sceRtcSetWin32FileTimeFunction);
            mm.removeFunction(sceRtcGetWin32FileTimeFunction);
            mm.removeFunction(sceRtcSetTickFunction);
            mm.removeFunction(sceRtcGetTickFunction);
            mm.removeFunction(sceRtcCompareTickFunction);
            mm.removeFunction(sceRtcTickAddTicksFunction);
            mm.removeFunction(sceRtcTickAddMicrosecondsFunction);
            mm.removeFunction(sceRtcTickAddSecondsFunction);
            mm.removeFunction(sceRtcTickAddMinutesFunction);
            mm.removeFunction(sceRtcTickAddHoursFunction);
            mm.removeFunction(sceRtcTickAddDaysFunction);
            mm.removeFunction(sceRtcTickAddWeeksFunction);
            mm.removeFunction(sceRtcTickAddMonthsFunction);
            mm.removeFunction(sceRtcTickAddYearsFunction);
            mm.removeFunction(sceRtcFormatRFC2822Function);
            mm.removeFunction(sceRtcFormatRFC2822LocalTimeFunction);
            mm.removeFunction(sceRtcFormatRFC3339Function);
            mm.removeFunction(sceRtcFormatRFC3339LocalTimeFunction);
            mm.removeFunction(sceRtcParseDateTimeFunction);
            mm.removeFunction(sceRtcParseRFC3339Function);

        }
    }

    final static int PSP_TIME_INVALID_YEAR = -1;
    final static int PSP_TIME_INVALID_MONTH = -2;
    final static int PSP_TIME_INVALID_DAY = -3;
    final static int PSP_TIME_INVALID_HOUR = -4;
    final static int PSP_TIME_INVALID_MINUTES = -5;
    final static int PSP_TIME_INVALID_SECONDS = -6;
    final static int PSP_TIME_INVALID_MICROSECONDS = -7;

    // Statics verified on PSP.
    final static int PSP_TIME_SECONDS_IN_MINUTE = 60;
    final static int PSP_TIME_SECONDS_IN_HOUR = 3600;
    final static int PSP_TIME_SECONDS_IN_DAY = 86400;
    final static int PSP_TIME_SECONDS_IN_WEEK = 604800;
    final static int PSP_TIME_SECONDS_IN_MONTH = 2629743;
    final static int PSP_TIME_SECONDS_IN_YEAR = 31556926;

    private long rtcMagicOffset = 62135596800000000L;

    protected long hleGetCurrentTick() {
        return Emulator.getClock().microTime();
    }

    /** 64 bit addend */
    protected void hleRtcTickAdd64(Processor processor, long multiplier) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int dest_addr = cpu.gpr[4];
        int src_addr = cpu.gpr[5];
        long value = ((((long)cpu.gpr[6]) & 0xFFFFFFFFL) | (((long)cpu.gpr[7])<<32));

        log.debug("hleRtcTickAdd64 " + multiplier + " * " + value);

        if (Memory.isAddressGood(src_addr) && Memory.isAddressGood(dest_addr)) {
            long src = mem.read64(src_addr);
            mem.write64(dest_addr, src + multiplier * value);
            cpu.gpr[2] = 0;
        } else {
            log.warn("hleRtcTickAdd64 bad address "
                + String.format("0x%08X 0x%08X", src_addr, dest_addr));
            cpu.gpr[2] = -1;
        }
    }

    /** 32 bit addend */
    protected void hleRtcTickAdd32(Processor processor, long multiplier) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int dest_addr = cpu.gpr[4];
        int src_addr = cpu.gpr[5];
        int value = cpu.gpr[6];

        log.debug("hleRtcTickAdd32 " + multiplier + " * " + value);

        if (Memory.isAddressGood(src_addr) && Memory.isAddressGood(dest_addr)) {
            long src = mem.read64(src_addr);
            mem.write64(dest_addr, src + multiplier * value);
            cpu.gpr[2] = 0;
        } else {
            log.warn("hleRtcTickAdd32 bad address "
                + String.format("0x%08X 0x%08X", src_addr, dest_addr));
            cpu.gpr[2] = -1;
        }
    }

    public void sceRtcGetTickResolution(Processor processor) {
        CpuState cpu = processor.cpu;

        // resolution = micro seconds
        cpu.gpr[2] = 1000000;
    }

    public void sceRtcGetCurrentTick(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int addr = cpu.gpr[4];
        mem.write64(addr, hleGetCurrentTick());

        if (Modules.log.isDebugEnabled()) {
        	Modules.log.debug(String.format("sceRtcGetCurrentTick 0x%08X, returning %d", addr, mem.read64(addr)));
        }

        cpu.gpr[2] = 0;
    }

    public void sceRtcGetAccumulativeTime(Processor processor) {
        CpuState cpu = processor.cpu;

        if (Modules.log.isDebugEnabled()) {
        	Modules.log.debug("sceRtcGetAccumulativeTime");
        }
        // Returns the difference between the last reincarnated time and the current tick.
        // Just return our current tick, since there's no need to mimick such behaviour.
        long accumTick = hleGetCurrentTick();

        cpu.gpr[2] = (int)(accumTick & 0xffffffffL);
        cpu.gpr[3] = (int)((accumTick >> 32) & 0xffffffffL);
    }

    public void sceRtcGetAccumlativeTime(Processor processor) {
        CpuState cpu = processor.cpu;

        // Typo. Refers to the same function.
        if (Modules.log.isDebugEnabled()) {
        	Modules.log.debug("sceRtcGetAccumlativeTime");
        }
        long accumTick = hleGetCurrentTick();

        cpu.gpr[2] = (int)(accumTick & 0xffffffffL);
        cpu.gpr[3] = (int)((accumTick >> 32) & 0xffffffffL);
    }

    public void sceRtcGetCurrentClock(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int addr = cpu.gpr[4];
        int tz = cpu.gpr[5]; //Time Zone (minutes from UTC)
        ScePspDateTime pspTime = new ScePspDateTime(tz);
        pspTime.write(mem, addr);

        log.debug("sceRtcGetCurrentClock addr=" + Integer.toHexString(addr) + " time zone=" + tz);

        cpu.gpr[2] = 0;
    }

    public void sceRtcGetCurrentClockLocalTime(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int addr = cpu.gpr[4];
        ScePspDateTime pspTime = new ScePspDateTime();
        pspTime.write(mem, addr);

        cpu.gpr[2] = 0;
    }

    public void sceRtcConvertUtcToLocalTime(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int utc_addr = cpu.gpr[4];
        int local_addr = cpu.gpr[5];

        log.debug("PARTIAL: sceRtcConvertUtcToLocalTime");

        long utc = mem.read64(utc_addr);
        long local = utc; // TODO
        mem.write64(local_addr, local);

        cpu.gpr[2] = 0;
    }

    public void sceRtcConvertLocalTimeToUTC(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int local_addr = cpu.gpr[4];
        int utc_addr = cpu.gpr[5];

        log.debug("PARTIAL: sceRtcConvertLocalTimeToUTC");

        long local = mem.read64(local_addr);
        long utc = local; // TODO
        mem.write64(utc_addr, utc);

        cpu.gpr[2] = 0;
    }

    public void sceRtcIsLeapYear(Processor processor) {
        CpuState cpu = processor.cpu;

        log.debug("sceRtcIsLeapYear");

        int year = cpu.gpr[4];

        if((year % 4 == 0) && (year % 100 != 0) || (year % 400 == 0))
            cpu.gpr[2] = 1;
        else
            cpu.gpr[2] = 0;
    }

    public void sceRtcGetDaysInMonth(Processor processor) {
        CpuState cpu = processor.cpu;

        int year = cpu.gpr[4];
        int month = cpu.gpr[5];

        Calendar cal = new GregorianCalendar(year, month - 1, 1);

        int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        log.debug(String.format("sceRtcGetDaysInMonth %04d-%02d ret:%d", year, month, days));
        cpu.gpr[2] = days;
    }

    // pspsdk says 0=monday but I tested and 0=sunday... (fiveofhearts)
    public void sceRtcGetDayOfWeek(Processor processor) {
        CpuState cpu = processor.cpu;
        int year = cpu.gpr[4];
        int month = cpu.gpr[5];
        int day = cpu.gpr[6];

        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day);

        int number = cal.get(Calendar.DAY_OF_WEEK);
        number = (number - 1 + 7) % 7;

        log.debug(String.format("sceRtcGetDayOfWeek %04d-%02d-%02d ret:%d", year, month, day, number));
        cpu.gpr[2] = number;
    }

    /**
     * Validate pspDate component ranges
     *
     * @param date - pointer to pspDate struct to be checked
     * @return 0 on success, one of PSP_TIME_INVALID_* on error
     */
    public void sceRtcCheckValid(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int time_addr = cpu.gpr[4];

        if (Memory.isAddressGood(time_addr)) {
            ScePspDateTime time = new ScePspDateTime();
            time.read(mem, time_addr);
            Calendar cal = new GregorianCalendar(time.year, time.month - 1, time.day,
                time.hour, time.minute, time.second);
            int result = 0;
            if (time.year < 1582 || time.year > 3000) {	// What are valid years?
            	result = PSP_TIME_INVALID_YEAR;
            } else if (time.month < 1 || time.month > 12) {
            	result = PSP_TIME_INVALID_MONTH;
            } else if (time.day < 1 || time.day > 31) {
            	result = PSP_TIME_INVALID_DAY;
            } else if (time.hour < 0 || time.hour > 23) {
            	result = PSP_TIME_INVALID_HOUR;
            } else if (time.minute < 0 || time.minute > 59) {
            	result = PSP_TIME_INVALID_MINUTES;
            } else if (time.second < 0 || time.second > 59) {
            	result = PSP_TIME_INVALID_SECONDS;
            } else if (time.microsecond < 0 || time.microsecond >= 1000000) {
            	result = PSP_TIME_INVALID_MICROSECONDS;
            } else if (cal.get(Calendar.DAY_OF_MONTH) != time.day) { // Check if this is a valid day of the month
            	result = PSP_TIME_INVALID_DAY;
            }

            if (log.isDebugEnabled()) {
            	log.debug("sceRtcCheckValid " + time.toString() + ", cal: " + cal + ", result: " + result);
            }

            cpu.gpr[2] = result;
        } else {
            log.warn("sceRtcGetTick bad address " + String.format("0x%08X", time_addr));
            cpu.gpr[2] = -1;
        }
    }

    public void sceRtcSetTime_t(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int date_addr = cpu.gpr[4];
        int time = cpu.gpr[5];

        if (Memory.isAddressGood(date_addr)) {
            ScePspDateTime dateTime = ScePspDateTime.fromUnixTime(time);
            dateTime.write(mem, date_addr);
            cpu.gpr[2] = 0;
        } else {
            log.warn("sceRtcSetTime_t bad address " + String.format("0x%08X", date_addr));
            cpu.gpr[2] = -1;
        }
    }

    public void sceRtcGetTime_t(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int date_addr = cpu.gpr[4];
        int time_addr = cpu.gpr[5];

        if (Memory.isAddressGood(date_addr) && Memory.isAddressGood(time_addr)) {
            ScePspDateTime dateTime = new ScePspDateTime();
            dateTime.read(mem, date_addr);
            Calendar cal = Calendar.getInstance();
            cal.set(dateTime.year, dateTime.month - 1, dateTime.day, dateTime.hour, dateTime.minute, dateTime.second);
            int unixtime = (int)(cal.getTime().getTime() / 1000L);
            log.debug("sceRtcGetTime_t psptime:" + dateTime + " unixtime:" + unixtime);
            mem.write32(time_addr, unixtime);
            cpu.gpr[2] = 0;
        } else {
            log.warn("sceRtcGetTime_t bad address " + String.format("0x%08X 0x%08X", date_addr, time_addr));
            cpu.gpr[2] = -1;
        }
    }

    public void sceRtcSetDosTime(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int date_addr = cpu.gpr[4];
        int time = cpu.gpr[5];

        if (Memory.isAddressGood(date_addr)) {
            ScePspDateTime dateTime = ScePspDateTime.fromMSDOSTime(time);
            dateTime.write(mem, date_addr);
            cpu.gpr[2] = 0;
        } else {
            log.warn("sceRtcSetDosTime bad address " + String.format("0x%08X", date_addr));
            cpu.gpr[2] = -1;
        }
    }

    public void sceRtcGetDosTime(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int date_addr = cpu.gpr[4];
        int time_addr = cpu.gpr[5];

        if (Memory.isAddressGood(date_addr) && Memory.isAddressGood(time_addr)) {
            ScePspDateTime dateTime = new ScePspDateTime();
            dateTime.read(mem, date_addr);
            Calendar cal = Calendar.getInstance();
            cal.set(dateTime.year, dateTime.month - 1, dateTime.day, dateTime.hour, dateTime.minute, dateTime.second);
            int dostime = (int)(cal.getTime().getTime() / 1000L);
            log.debug("sceRtcGetDosTime psptime:" + dateTime + " dostime:" + dostime);
            mem.write32(time_addr, dostime);
            cpu.gpr[2] = 0;
        } else {
            log.warn("sceRtcGetDosTime bad address " + String.format("0x%08X 0x%08X", date_addr, time_addr));
            cpu.gpr[2] = -1;
        }
    }

    public void sceRtcSetWin32FileTime(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int date_addr = cpu.gpr[4];
        long time = Utilities.getRegister64(cpu, cpu.gpr[5]);

        if (Memory.isAddressGood(date_addr)) {
            ScePspDateTime dateTime = ScePspDateTime.fromFILETIMETime(time);
            dateTime.write(mem, date_addr);
            cpu.gpr[2] = 0;
        } else {
            log.warn("sceRtcSetWin32FileTime bad address " + String.format("0x%08X", date_addr));
            cpu.gpr[2] = -1;
        }
    }

    public void sceRtcGetWin32FileTime(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int date_addr = cpu.gpr[4];
        int time_addr = cpu.gpr[5];

        if (Memory.isAddressGood(date_addr) && Memory.isAddressGood(time_addr)) {
            ScePspDateTime dateTime = new ScePspDateTime();
            dateTime.read(mem, date_addr);
            Calendar cal = Calendar.getInstance();
            cal.set(dateTime.year, dateTime.month - 1, dateTime.day, dateTime.hour, dateTime.minute, dateTime.second);
            int filetimetime = (int)(cal.getTime().getTime() / 1000L);
            log.debug("sceRtcGetWin32FileTime psptime:" + dateTime + " filetimetime:" + filetimetime);
            mem.write64(time_addr, filetimetime);
            cpu.gpr[2] = 0;
        } else {
            log.warn("sceRtcGetWin32FileTime bad address " + String.format("0x%08X 0x%08X", date_addr, time_addr));
            cpu.gpr[2] = -1;
        }
    }

    /** Set a pspTime struct based on ticks. */
    public void sceRtcSetTick(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int time_addr = cpu.gpr[4];
        int ticks_addr = cpu.gpr[5];

        log.debug("sceRtcSetTick");

        if (Memory.isAddressGood(time_addr) && Memory.isAddressGood(ticks_addr)) {
            long ticks = mem.read64(ticks_addr) - rtcMagicOffset;
            ScePspDateTime time = ScePspDateTime.fromMicros(ticks);
            time.write(mem, time_addr);
            cpu.gpr[2] = 0;
        } else {
            log.warn("sceRtcSetTick bad address "
                + String.format("0x%08X 0x%08X", time_addr, ticks_addr));
            cpu.gpr[2] = -1;
        }
    }

    /** Set ticks based on a pspTime struct. */
    public void sceRtcGetTick(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int time_addr = cpu.gpr[4];
        int ticks_addr = cpu.gpr[5];

        if (Memory.isAddressGood(time_addr) && Memory.isAddressGood(ticks_addr)) {
            // use java library to convert a date to seconds, then multiply it by the tick resolution
            ScePspDateTime time = new ScePspDateTime();
            time.read(mem, time_addr);
            Calendar cal = new GregorianCalendar(time.year, time.month - 1, time.day,
                time.hour, time.minute, time.second);
            long ticks = rtcMagicOffset + (cal.getTimeInMillis() * 1000) + (time.microsecond % 1000);
            mem.write64(ticks_addr, ticks);

            log.debug("sceRtcGetTick " + time.toString() + " -> tick:" + ticks + " saved to 0x" + Integer.toHexString(ticks_addr));
            cpu.gpr[2] = 0;
        } else {
            log.warn("sceRtcGetTick bad address "
                + String.format("0x%08X 0x%08X", time_addr, ticks_addr));
            cpu.gpr[2] = -1;
        }
    }

    public void sceRtcCompareTick(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int first = cpu.gpr[4];
        int second = cpu.gpr[5];

        log.debug("sceRtcCompareTick");

        if (Memory.isAddressGood(first) && Memory.isAddressGood(second)) {
            long tick1 = mem.read64(first);
            long tick2 = mem.read64(second);

            if (tick1 == tick2)
                cpu.gpr[2] = 0;
            else if (tick1 < tick2)
                cpu.gpr[2] = -1;
            else if (tick1 > tick2)
                cpu.gpr[2] = 1;
        } else {
            log.warn("sceRtcCompareTick bad address "
                + String.format("0x%08X 0x%08X", first, second));
            cpu.gpr[2] = -1;
        }
    }

    public void sceRtcTickAddTicks(Processor processor) {
        log.debug("sceRtcTickAddTicks redirecting to hleRtcTickAdd64(1)");
        hleRtcTickAdd64(processor, 1);
    }

    public void sceRtcTickAddMicroseconds(Processor processor) {
        log.debug("sceRtcTickAddMicroseconds redirecting to hleRtcTickAdd64(1)");
        hleRtcTickAdd64(processor, 1);
    }

    public void sceRtcTickAddSeconds(Processor processor) {
        log.debug("sceRtcTickAddSeconds redirecting to hleRtcTickAdd64(1000000)");
        hleRtcTickAdd64(processor, 1000000L);
    }

    public void sceRtcTickAddMinutes(Processor processor) {
        log.debug("sceRtcTickAddMinutes redirecting to hleRtcTickAdd64(60*1000000)");
        hleRtcTickAdd64(processor, PSP_TIME_SECONDS_IN_MINUTE*1000000L);
    }

    public void sceRtcTickAddHours(Processor processor) {
        log.debug("sceRtcTickAddHours redirecting to hleRtcTickAdd32(60*60*1000000)");
        hleRtcTickAdd32(processor, PSP_TIME_SECONDS_IN_HOUR*1000000L);
    }

    public void sceRtcTickAddDays(Processor processor) {
        log.debug("sceRtcTickAddDays redirecting to hleRtcTickAdd32(24*60*60*1000000)");
        hleRtcTickAdd32(processor, PSP_TIME_SECONDS_IN_DAY*1000000L);
    }

    public void sceRtcTickAddWeeks(Processor processor) {
        log.debug("sceRtcTickAddWeeks redirecting to hleRtcTickAdd32(7*24*60*60*1000000)");
        hleRtcTickAdd32(processor, PSP_TIME_SECONDS_IN_WEEK*1000000L);
    }

    public void sceRtcTickAddMonths(Processor processor) {
        log.debug("sceRtcTickAddMonths redirecting to hleRtcTickAdd32(30*24*60*60*1000000)");
        hleRtcTickAdd32(processor, PSP_TIME_SECONDS_IN_MONTH*1000000L);
    }

    public void sceRtcTickAddYears(Processor processor) {
        log.debug("sceRtcTickAddYears redirecting to hleRtcTickAdd32(365*24*60*60*1000000)");
        hleRtcTickAdd32(processor, PSP_TIME_SECONDS_IN_YEAR*1000000L);
    }

    public void sceRtcFormatRFC2822(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("Unimplemented NID function sceRtcFormatRFC2822 [0xC663B3B9]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceRtcFormatRFC2822LocalTime(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("Unimplemented NID function sceRtcFormatRFC2822LocalTime [0x7DE6711B]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceRtcFormatRFC3339(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("Unimplemented NID function sceRtcFormatRFC3339 [0x0498FB3C]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceRtcFormatRFC3339LocalTime(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("Unimplemented NID function sceRtcFormatRFC3339LocalTime [0x27F98543]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceRtcParseDateTime(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("Unimplemented NID function sceRtcParseDateTime [0xDFBC5F16]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceRtcParseRFC3339(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("Unimplemented NID function sceRtcParseRFC3339 [0x28E1E988]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public final HLEModuleFunction sceRtcGetTickResolutionFunction = new HLEModuleFunction("sceRtc", "sceRtcGetTickResolution") {
        @Override
        public final void execute(Processor processor) {
            sceRtcGetTickResolution(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcGetTickResolution(processor);";
        }
    };

    public final HLEModuleFunction sceRtcGetCurrentTickFunction = new HLEModuleFunction("sceRtc", "sceRtcGetCurrentTick") {
        @Override
        public final void execute(Processor processor) {
            sceRtcGetCurrentTick(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcGetCurrentTick(processor);";
        }
    };

    public final HLEModuleFunction sceRtcGetAccumulativeTimeFunction = new HLEModuleFunction("sceRtc", "sceRtcGetAccumulativeTime") {
        @Override
        public final void execute(Processor processor) {
            sceRtcGetAccumulativeTime(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcGetAccumulativeTime(processor);";
        }
    };

    public final HLEModuleFunction sceRtcGetAccumlativeTimeFunction = new HLEModuleFunction("sceRtc", "sceRtcGetAccumlativeTime") {
        @Override
        public final void execute(Processor processor) {
            sceRtcGetAccumlativeTime(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcGetAccumlativeTime(processor);";
        }
    };

    public final HLEModuleFunction sceRtcGetCurrentClockFunction = new HLEModuleFunction("sceRtc", "sceRtcGetCurrentClock") {
        @Override
        public final void execute(Processor processor) {
            sceRtcGetCurrentClock(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcGetCurrentClock(processor);";
        }
    };

    public final HLEModuleFunction sceRtcGetCurrentClockLocalTimeFunction = new HLEModuleFunction("sceRtc", "sceRtcGetCurrentClockLocalTime") {
        @Override
        public final void execute(Processor processor) {
            sceRtcGetCurrentClockLocalTime(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcGetCurrentClockLocalTime(processor);";
        }
    };

    public final HLEModuleFunction sceRtcConvertUtcToLocalTimeFunction = new HLEModuleFunction("sceRtc", "sceRtcConvertUtcToLocalTime") {
        @Override
        public final void execute(Processor processor) {
            sceRtcConvertUtcToLocalTime(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcConvertUtcToLocalTime(processor);";
        }
    };

    public final HLEModuleFunction sceRtcConvertLocalTimeToUTCFunction = new HLEModuleFunction("sceRtc", "sceRtcConvertLocalTimeToUTC") {
        @Override
        public final void execute(Processor processor) {
            sceRtcConvertLocalTimeToUTC(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcConvertLocalTimeToUTC(processor);";
        }
    };

    public final HLEModuleFunction sceRtcIsLeapYearFunction = new HLEModuleFunction("sceRtc", "sceRtcIsLeapYear") {
        @Override
        public final void execute(Processor processor) {
            sceRtcIsLeapYear(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcIsLeapYear(processor);";
        }
    };

    public final HLEModuleFunction sceRtcGetDaysInMonthFunction = new HLEModuleFunction("sceRtc", "sceRtcGetDaysInMonth") {
        @Override
        public final void execute(Processor processor) {
            sceRtcGetDaysInMonth(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcGetDaysInMonth(processor);";
        }
    };

    public final HLEModuleFunction sceRtcGetDayOfWeekFunction = new HLEModuleFunction("sceRtc", "sceRtcGetDayOfWeek") {
        @Override
        public final void execute(Processor processor) {
            sceRtcGetDayOfWeek(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcGetDayOfWeek(processor);";
        }
    };

    public final HLEModuleFunction sceRtcCheckValidFunction = new HLEModuleFunction("sceRtc", "sceRtcCheckValid") {
        @Override
        public final void execute(Processor processor) {
            sceRtcCheckValid(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcCheckValid(processor);";
        }
    };

    public final HLEModuleFunction sceRtcSetTime_tFunction = new HLEModuleFunction("sceRtc", "sceRtcSetTime_t") {
        @Override
        public final void execute(Processor processor) {
            sceRtcSetTime_t(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcSetTime_t(processor);";
        }
    };

    public final HLEModuleFunction sceRtcGetTime_tFunction = new HLEModuleFunction("sceRtc", "sceRtcGetTime_t") {
        @Override
        public final void execute(Processor processor) {
            sceRtcGetTime_t(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcGetTime_t(processor);";
        }
    };

    public final HLEModuleFunction sceRtcSetDosTimeFunction = new HLEModuleFunction("sceRtc", "sceRtcSetDosTime") {
        @Override
        public final void execute(Processor processor) {
            sceRtcSetDosTime(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcSetDosTime(processor);";
        }
    };

    public final HLEModuleFunction sceRtcGetDosTimeFunction = new HLEModuleFunction("sceRtc", "sceRtcGetDosTime") {
        @Override
        public final void execute(Processor processor) {
            sceRtcGetDosTime(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcGetDosTime(processor);";
        }
    };

    public final HLEModuleFunction sceRtcSetWin32FileTimeFunction = new HLEModuleFunction("sceRtc", "sceRtcSetWin32FileTime") {
        @Override
        public final void execute(Processor processor) {
            sceRtcSetWin32FileTime(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcSetWin32FileTime(processor);";
        }
    };

    public final HLEModuleFunction sceRtcGetWin32FileTimeFunction = new HLEModuleFunction("sceRtc", "sceRtcGetWin32FileTime") {
        @Override
        public final void execute(Processor processor) {
            sceRtcGetWin32FileTime(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcGetWin32FileTime(processor);";
        }
    };

    public final HLEModuleFunction sceRtcSetTickFunction = new HLEModuleFunction("sceRtc", "sceRtcSetTick") {
        @Override
        public final void execute(Processor processor) {
            sceRtcSetTick(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcSetTick(processor);";
        }
    };

    public final HLEModuleFunction sceRtcGetTickFunction = new HLEModuleFunction("sceRtc", "sceRtcGetTick") {
        @Override
        public final void execute(Processor processor) {
            sceRtcGetTick(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcGetTick(processor);";
        }
    };

    public final HLEModuleFunction sceRtcCompareTickFunction = new HLEModuleFunction("sceRtc", "sceRtcCompareTick") {
        @Override
        public final void execute(Processor processor) {
            sceRtcCompareTick(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcCompareTick(processor);";
        }
    };

    public final HLEModuleFunction sceRtcTickAddTicksFunction = new HLEModuleFunction("sceRtc", "sceRtcTickAddTicks") {
        @Override
        public final void execute(Processor processor) {
            sceRtcTickAddTicks(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcTickAddTicks(processor);";
        }
    };

    public final HLEModuleFunction sceRtcTickAddMicrosecondsFunction = new HLEModuleFunction("sceRtc", "sceRtcTickAddMicroseconds") {
        @Override
        public final void execute(Processor processor) {
            sceRtcTickAddMicroseconds(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcTickAddMicroseconds(processor);";
        }
    };

    public final HLEModuleFunction sceRtcTickAddSecondsFunction = new HLEModuleFunction("sceRtc", "sceRtcTickAddSeconds") {
        @Override
        public final void execute(Processor processor) {
            sceRtcTickAddSeconds(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcTickAddSeconds(processor);";
        }
    };

    public final HLEModuleFunction sceRtcTickAddMinutesFunction = new HLEModuleFunction("sceRtc", "sceRtcTickAddMinutes") {
        @Override
        public final void execute(Processor processor) {
            sceRtcTickAddMinutes(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcTickAddMinutes(processor);";
        }
    };

    public final HLEModuleFunction sceRtcTickAddHoursFunction = new HLEModuleFunction("sceRtc", "sceRtcTickAddHours") {
        @Override
        public final void execute(Processor processor) {
            sceRtcTickAddHours(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcTickAddHours(processor);";
        }
    };

    public final HLEModuleFunction sceRtcTickAddDaysFunction = new HLEModuleFunction("sceRtc", "sceRtcTickAddDays") {
        @Override
        public final void execute(Processor processor) {
            sceRtcTickAddDays(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcTickAddDays(processor);";
        }
    };

    public final HLEModuleFunction sceRtcTickAddWeeksFunction = new HLEModuleFunction("sceRtc", "sceRtcTickAddWeeks") {
        @Override
        public final void execute(Processor processor) {
            sceRtcTickAddWeeks(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcTickAddWeeks(processor);";
        }
    };

    public final HLEModuleFunction sceRtcTickAddMonthsFunction = new HLEModuleFunction("sceRtc", "sceRtcTickAddMonths") {
        @Override
        public final void execute(Processor processor) {
            sceRtcTickAddMonths(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcTickAddMonths(processor);";
        }
    };

    public final HLEModuleFunction sceRtcTickAddYearsFunction = new HLEModuleFunction("sceRtc", "sceRtcTickAddYears") {
        @Override
        public final void execute(Processor processor) {
            sceRtcTickAddYears(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcTickAddYears(processor);";
        }
    };

    public final HLEModuleFunction sceRtcFormatRFC2822Function = new HLEModuleFunction("sceRtc", "sceRtcFormatRFC2822") {
        @Override
        public final void execute(Processor processor) {
            sceRtcFormatRFC2822(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcFormatRFC2822(processor);";
        }
    };

    public final HLEModuleFunction sceRtcFormatRFC2822LocalTimeFunction = new HLEModuleFunction("sceRtc", "sceRtcFormatRFC2822LocalTime") {
        @Override
        public final void execute(Processor processor) {
            sceRtcFormatRFC2822LocalTime(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcFormatRFC2822LocalTime(processor);";
        }
    };

    public final HLEModuleFunction sceRtcFormatRFC3339Function = new HLEModuleFunction("sceRtc", "sceRtcFormatRFC3339") {
        @Override
        public final void execute(Processor processor) {
            sceRtcFormatRFC3339(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcFormatRFC3339(processor);";
        }
    };

    public final HLEModuleFunction sceRtcFormatRFC3339LocalTimeFunction = new HLEModuleFunction("sceRtc", "sceRtcFormatRFC3339LocalTime") {
        @Override
        public final void execute(Processor processor) {
            sceRtcFormatRFC3339LocalTime(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcFormatRFC3339LocalTime(processor);";
        }
    };

    public final HLEModuleFunction sceRtcParseDateTimeFunction = new HLEModuleFunction("sceRtc", "sceRtcParseDateTime") {
        @Override
        public final void execute(Processor processor) {
            sceRtcParseDateTime(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcParseDateTime(processor);";
        }
    };

    public final HLEModuleFunction sceRtcParseRFC3339Function = new HLEModuleFunction("sceRtc", "sceRtcParseRFC3339") {
        @Override
        public final void execute(Processor processor) {
            sceRtcParseRFC3339(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceRtcModule.sceRtcParseRFC3339(processor);";
        }
    };
}