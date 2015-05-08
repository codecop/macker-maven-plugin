# Macker Maven Plugin #
CodeHaus hosts a simple [Macker MOJO](http://mojo.codehaus.org/macker-maven-plugin/). (Read what the plugin is supposed to do [here](http://mojo.codehaus.org/macker-maven-plugin/).) It's still in the MOJO sandbox and has not been released yet. Here is a clone of this Maven Macker MOJO. It contains all the original [Subversion history](http://svn.codehaus.org/mojo/trunk/sandbox/macker-maven-plugin/) together with ~~some changes~~ a lot of changes.


## Modifications ##
The CodeHaus Maven Macker plugin only supports a very basic configuration. For example, there is no way to skip it's execution or to allow multiple rule-set files. I added more configuration options, e.g.

 * Skip execution (`<skip>`).
 * Work on test classes as well (`<includeTests>`).
 * Allow multiple rule-set files.
 * Find rules in the classpath.
 * Include and exclude files.

In fact, this is everything the [Maven PMD plugin](http://maven.apache.org/plugins/maven-pmd-plugin/) is able to do and I copied the source [from there](http://svn.apache.org/viewvc/maven/plugins/trunk/maven-pmd-plugin/) ;-) Further more I added the following:

 * Some unit test cases for old and new functionality.
 * Made the whole thing Java 1.4 compatible to fix dependency problems.
 * It now skips the execution of the plugin for projects which are not Java, so it skips POM projects.
 * I fixed the `classesDirectory` problem with Sonar.
 * Added the missing examples page to the Maven site. I copied it from the current site, but it wasn't in the SVN trunk.
 * Fixed all PMD and almost all Checkstyle errors shown in the site report.
 * Had to change all license headers to the current Checkstyle format.

Later, Mark noticed some license problems: Macker is only GPL (without the classpath exception) and must not be called from an ASL licensed plugin, but only with a forked JVM command-line as is done in the Cobertura plugin. So, I did the following:

 * Refactored the plugin to allow different implementations of calling Macker.
 * Added a forking implementation, which was mainly copied from the [Cobertura plugin](http://mojo.codehaus.org/cobertura-maven-plugin/).
 * Finally, I added some integration tests, which set-up I borrowed from the [Eclipse plugin](http://maven.apache.org/plugins/maven-eclipse-plugin/). See [my post about Maven plugin testing](http://blog.code-cop.org/2010/09/maven-plugin-testing-tools.html) for more details on using the Maven Plugin Testing Tools.


## Macker ##
[Macker](http://www.innig.net/macker/) is a build-time architectural rule checking utility for Java developers. It's meant to model the architectural rules and helps to keep code clean and consistent. You can tailor a rules file to suit the structure of a specific project. Writing a rules file is part of the development process for each unique project. Macker was originally developed by Paul Cantrell. (This introduction is an excerpt from the [Macker FAQ](http://www.innig.net/macker/faq.html).)

Macker is quite old, but we have been using it for years and it works well for us. See [what happens if you do not use it](http://blog.code-cop.org/2007/09/macker-check.html). It comes with Ant support, ~~but Maven integration has been lacking~~ and Maven integration is available as well.


## Usage ##
 * See the current [usage page](http://www.code-cop.org/mvn2repo/sites/macker-maven-plugin/usage.html) of Macker Maven plugin's Maven site.
 * [Original plugin site](http://mojo.codehaus.org/macker-maven-plugin/).


## Releases ##
A `1.0.0-SNAPSHOT` of my latest changes is available in my [Mvn2Repo](https://bitbucket.org/pkofler/mvn2repo). There is also a `0.9.0` unofficial release which does not fork Macker. For the current state of the patch see [ticket MOJO-1529](http://jira.codehaus.org/browse/MOJO-1529). The plugin has not been released.