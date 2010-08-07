rem download the SVN trunk of macker-maven-plugin and convert to Mercurial
call hg convert http://svn.codehaus.org/mojo/trunk/sandbox/macker-maven-plugin/ trunk-hg
cd trunk-hg
call hg up
cd ..
pause
