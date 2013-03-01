@echo off
rem CD to the path of the command line, this is required when running as an administrator
cd /D "%~dp0"

set PATH=%PATH%;lib\;lib\windows-x86\

if NOT EXIST "%SystemRoot%\SysWOW64" goto JAVA32
set key=HKEY_LOCAL_MACHINE\SOFTWARE\Wow6432Node\JavaSoft\Java Runtime Environment
goto JAVA

:JAVA32
set key=HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment

:JAVA
set JAVA_CMD=%ProgramFiles%\Java\jre7\bin\java.exe
if not exist "%JAVA_CMD%" set JAVA_CMD=java.exe

rem Checking if the "reg" command is available
reg /? >NUL 2>NUL
if ERRORLEVEL 1 goto RUN

set JAVA_VERSION=
set JAVA_HOME=
for /f "tokens=3* skip=2" %%a in ('reg query "%key%" /v CurrentVersion') do set JAVA_VERSION=%%a
for /f "tokens=2* skip=2" %%a in ('reg query "%key%\%JAVA_VERSION%" /v JavaHome') do set JAVA_HOME=%%b

set JAVA_CMD=%JAVA_HOME%\bin\java.exe
if not exist "%JAVA_CMD%" goto JAVAMISSING

:RUN

rem Use -Xmx768m for Windows XP, -Xmx1024m for all other Windows versions
set MAX_MEM_SIZE=1024m
ver | findstr "5\.1\." > nul
if %ERRORLEVEL% EQU 0 set MAX_MEM_SIZE=768m

echo Running Jpcsp 32bit...
"%JAVA_CMD%" -Xmx%MAX_MEM_SIZE% -Xss2m -XX:MaxPermSize=128m -XX:ReservedCodeCacheSize=64m -Djava.library.path=lib/windows-x86 -jar bin/jpcsp.jar %*
if ERRORLEVEL 1 goto PAUSE
goto END

:JAVAMISSING
echo The required version of Java has not been installed or isn't recognized.
echo Go to
echo     http://www.oracle.com/technetwork/java/javase/downloads/index.html
echo to install the "Windows x86" Java JRE.

:PAUSE
pause

:END