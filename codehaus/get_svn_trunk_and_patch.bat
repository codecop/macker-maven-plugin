setlocal
set S=macker-maven-plugin\codehaus
set P=macker-maven-plugin_patch
set MAVEN_OPTS=-Xmx512m

cd ..\..

rmdir /S /Q %P%

rem get current trunk
call svn co http://svn.codehaus.org/mojo/trunk/sandbox/macker-maven-plugin/ %P%

rem apply patch
cd %P%
copy ..\macker-maven-plugin\pom.xml
xcopy ..\macker-maven-plugin\src .\src /V /S /Y
rem copy ..\%S%\patch.project .project
cd ..

rem test it
cd %P%
call mvn clean install site
cd ..
pause

rem clean in again and create patch
cd %P%
rem del .project
call mvn clean
cd src
call svn add test
cd ..
call svn diff > ..\%S%\ExtendedConfAndTestCases.diff
cd ..

cd %S%

endlocal
pause
