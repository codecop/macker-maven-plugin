rem download the latest SVN trunk commits to a clone of macker-maven-plugin and convert to Mercurial
cd ..

rmdir /S /Q macker4update

call hg clone macker-maven-plugin macker4update

call hg convert --authors macker4update\.authormap --branchmap macker4update\.branchmap http://svn.codehaus.org/mojo/trunk/sandbox/macker-maven-plugin/ macker4update macker4update\.shamap

cd macker4update
call hg ci -m "update shamap after conversion"
cd ..

cd macker4update

rem update to last CodehausTrunk revision
call hg up CodehausTrunk

rem merge with the new SVN revision
rem call hg merge -r TODO
