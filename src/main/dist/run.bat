@echo off
cls
:menu
echo.
echo 1 - Run on console
echo 2 - Install service
echo 3 - Uninstall service
echo 4 - EXIT
echo.
set /P M=Type 1, 2, 3, or 4 then press ENTER:
if %M%==1 goto RUN
if %M%==2 goto INSTALL
if %M%==3 goto UNINSTALL
if %M%==4 goto EOF
:RUN
java -Xmx32M -Dlogging.config="./config/logback.xml" -jar "@projectName@-@projectVersion@.jar"
goto EOF
:INSTALL
@projectName@.exe install
@projectName@.exe start
goto EOF
:UNINSTALL
@projectName@.exe stop
@projectName@.exe uninstall
:EOF
