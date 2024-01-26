set JPDA_ADDRESS="8093"
set CATALINA_OPTS=%CATALINA_OPTS% -Duser.timezone=Asia/Kolkata -Xmx1024m -Xms1024m -XX:+HeapDumpOnOutOfMemoryError -XX:PermSize=256M -Djdk.http.auth.tunneling.disabledSchemes= -Djdk.http.auth.proxying.disabledSchemes=
cd %MY_HOME%\tomcat_build\bin


set COMMAND=%1

if "%COMMAND%" == "" (
    set COMMAND=run
)

if "%COMMAND%" == "stop" (
    .\catalina.bat stop
)
else (
    .\catalina.bat jpda %COMMAND%
)