@cls
@setlocal
@set MAVEN_OPTS=-Xmx512m
call mvn clean source:jar javadoc:jar deploy site:site site:deploy
@endlocal
@pause
