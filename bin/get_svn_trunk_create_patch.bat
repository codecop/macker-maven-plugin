setlocal
set S=macker-maven-plugin\codehaus.org
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
del .\src\main\java\org\codehaus\mojo\macker\LinkedMacker.java
cd ..

rem test it
cd %P%
call mvn clean verify site
cd ..
pause

rem clean in again and create patch
cd %P%
call mvn clean
call svn add src\main\java\org\codehaus\mojo\macker\*.java
call svn add src\site\apt\examples
call svn add src\test
call svn st
pause

call svn diff > ..\%S%\ExtendedConfAndForked.diff
cd ..

cd %S%

endlocal
pause
