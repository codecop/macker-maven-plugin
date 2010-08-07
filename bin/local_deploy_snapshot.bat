@rem deploy the latest snapshot and site of macker-maven-plugin to my local MVN repo.
@cls
@setlocal
@cd ..

@rmdir /S /Q E:\Develop\Java\JDK-1.5_Maven\mvn2repo\snapshots\org\codehaus
@rmdir /S /Q E:\Develop\Java\JDK-1.5_Maven\mvn2repo\sites\macker-maven-plugin

@set MAVEN_OPTS=-Xmx512m
call mvn clean source:jar javadoc:jar deploy site:site site:deploy

@cd bin
@endlocal
@pause
