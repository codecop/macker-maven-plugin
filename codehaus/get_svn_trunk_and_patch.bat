rmdir /S /Q trunk-svn-patch

rem get current trunk
call svn co http://svn.codehaus.org/mojo/trunk/sandbox/macker-maven-plugin/ trunk-svn-patch

rem apply patch
cd trunk-svn-patch
copy ..\..\pom.xml
xcopy ..\..\src .\src /V /S /Y
cd ..

pause
