@echo off
if not exist lib\windows-x86\atrac3plus2wav.exe goto at3tool
lib\windows-x86\atrac3plus2wav.exe %1
if not exist "%1.wav" goto at3tool
move %1.wav %2
goto end

:at3tool
if not exist lib\windows-x86\at3tool.exe goto himdrender
lib\windows-x86\at3tool.exe -d -repeat 1 %3 %2
if not exist "%2" goto himdrender
goto end

:himdrender
lib\windows-x86\HIMDRender.exe -e -i %1 -o %2
goto end

:end
