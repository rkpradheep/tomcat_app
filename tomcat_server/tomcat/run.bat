set JPDA_ADDRESS="8093"
set CATALINA_OPTS="%CATALINA_OPTS% -Duser.timezone=Asia/Kolkata"
cd %MY_HOME%\tomcat_build\bin


set COMMAND=%1

if %COMMAND% == "" (
    set COMMAND = "run"
)

.\catalina.bat jpda %COMMAND%

