package org.codehaus.mojo.macker.it;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.test.plugin.BuildTool;
import org.apache.maven.shared.test.plugin.PluginTestTool;
import org.apache.maven.shared.test.plugin.ProjectTool;
import org.apache.maven.shared.test.plugin.TestToolsException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import junit.framework.AssertionFailedError;

import org.codehaus.classworlds.ClassRealm;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a> (Copied from the ounce-maven-plugin
 * copied from the Eclipse AbstractEclipsePluginIT)
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:fgiust@apache.org">Fabrizio Giustina</a>
 */
public abstract class AbstractMackerPluginITCase
    extends AbstractMojoTestCase
{

    private BuildTool buildTool;

    private ProjectTool projectTool;

    /**
     * Test repository directory.
     */
    protected static File localRepositoryDirectory;

    /**
     * Pom File.
     */
    protected static File pomFile = new File( getBasedir(), "pom.xml" );

    /**
     * Group-Id for running test builds.
     */
    protected static final String GROUP_ID = "org.codehaus.mojo";

    /**
     * Artifact-Id for running test builds.
     */
    protected static final String ARTIFACT_ID = "macker-maven-plugin";

    /**
     * Version under which the plugin was installed to the test-time local repository for running test builds.
     */
    protected static final String VERSION = "test";

    private static final String BUILD_OUTPUT_DIRECTORY = "target/failsafe-reports/maven-output";

    private static boolean installed = false;

    /**
     * The name of the directory used for comparison of expected output.
     */
    private static final String EXPECTED_DIRECTORY_NAME = "expected";

    /**
     * @see org.codehaus.plexus.PlexusTestCase#setUp()
     */
    protected void setUp()
        throws Exception
    {
        if ( !installed )
        {
            System.out.println( "*** Running integation test builds; output will be directed to: "
                    + BUILD_OUTPUT_DIRECTORY );
        }

        super.setUp();

        buildTool = (BuildTool) lookup( BuildTool.ROLE, "default" );
        projectTool = (ProjectTool) lookup( ProjectTool.ROLE, "default" );
        // repositoryTool = (RepositoryTool) lookup( RepositoryTool.ROLE, "default" );

        String mavenHome = System.getProperty( "maven.home" );
        // maven.home is set by surefire when the test is run with maven, but better make the test
        // run in IDEs without the need of additional properties
        if ( mavenHome == null )
        {
            String path = System.getProperty( "java.library.path" );
            String[] paths = StringUtils.split( path, System.getProperty( "path.separator" ) );
            for ( int j = 0; j < paths.length; j++ )
            {
                String pt = paths[j];
                if ( new File( pt, "mvn" ).exists() )
                {
                    System.setProperty( "maven.home", new File( pt ).getAbsoluteFile().getParent() );
                    break;
                }

            }
        }

        System.setProperty( "MAVEN_TERMINATE_CMD", "on" );

        synchronized (AbstractMackerPluginITCase.class)
        {
            if ( !installed )
            {
                PluginTestTool pluginTestTool = (PluginTestTool) lookup( PluginTestTool.ROLE, "default" );
                localRepositoryDirectory = pluginTestTool.preparePluginForUnitTestingWithMavenBuilds( pomFile, VERSION,
                        localRepositoryDirectory );
                System.out.println( "*** Installed test-version of the Macker plugin to: " + localRepositoryDirectory );
                installed = true;
            }
        }

    }

    /**
     * @see org.codehaus.plexus.PlexusTestCase#tearDown()
     */
    protected void tearDown()
        throws Exception
    {
        super.tearDown();

        List/*<PlexusContainer>*/ containers = new ArrayList/*<PlexusContainer>*/();
        containers.add( getContainer() );
        for ( Iterator iter = containers.iterator(); iter.hasNext(); )
        {
            PlexusContainer cont = (PlexusContainer) iter.next();
            if ( cont != null )
            {
                cont.dispose();
                ClassRealm realm = cont.getContainerRealm();
                if ( realm != null )
                {
                    realm.getWorld().disposeRealm( realm.getId() );
                }
            }
        }
    }

    /**
     * Execute the plugin with no properties
     * @param projectName project directory
     * @param goalList comma separated list of goals to execute
     * @throws Exception any exception generated during test
     */
    protected void testProject( String projectName, String goalList )
        throws Exception
    {
        testProject( projectName, new Properties(), goalList );
    }

    /**
     * Execute the plugin.
     * @param projectName project directory
     * @param properties additional properties
     * @param goalList comma separated list of goals to execute
     * @throws Exception any exception generated during test
     */
    protected void testProject( String projectName, Properties properties, String goalList )
        throws Exception
    {
        File baseDir = getOutputDirectory( projectName );
        testProject( baseDir, new Properties(), goalList );
    }

    /**
     * Execute the plugin.
     * @param baseDir Execute the plugin goal on a test project and verify generated files.
     * @param properties additional properties
     * @param goalList comma separated list of goals to execute
     * @throws Exception any exception generated during test
     */
    protected void testProject( File baseDir, Properties properties, String goalList )
        throws Exception
    {
        File pom = new File( baseDir, "pom.xml" );

        String[] goal = goalList.split( "," );
        List/*<String>*/ goals = new ArrayList/*<String>*/();
        for ( int i = 0; i < goal.length; i++ )
        {
            goals.add( goal[i] );
        }
        executeMaven( pom, properties, goals );

        MavenProject project = readProject( pom );
        File projectOutputDir = new File( project.getBuild().getDirectory() );

        compareDirectoryContent( baseDir, projectOutputDir );
    }

    protected File getOutputDirectory( String projectName )
    {
        return getTestFile( "target/test-classes/it/" + projectName );
    }

    protected void executeMaven( File pom, Properties properties, List goals )
        throws TestToolsException, ExecutionFailedException
    {
        executeMaven( pom, properties, goals, true );
    }

    protected void executeMaven( File pom, Properties properties, List goals, boolean switchLocalRepo )
         throws TestToolsException, ExecutionFailedException
    {
        System.out.println( "  Building " + pom.getParentFile().getName() );

        new File( BUILD_OUTPUT_DIRECTORY ).mkdirs();

        NullPointerException npe = new NullPointerException();
        StackTraceElement[] trace = npe.getStackTrace();

        File buildLog = null;

        for ( int i = 0; i < trace.length; i++ )
        {
            StackTraceElement element = trace[i];
            String methodName = element.getMethodName();
            if ( methodName.startsWith( "test" ) && !methodName.equals( "testProject" ) )
            {
                String classname = element.getClassName();
                buildLog = new File( BUILD_OUTPUT_DIRECTORY, classname + "_" + element.getMethodName() + ".build.log" );
                break;
            }
        }

        if ( buildLog == null )
        {
            buildLog = new File( BUILD_OUTPUT_DIRECTORY, "unknown.build.log" );
        }

        if (properties == null)
        {
            properties = new Properties();
        }
        InvocationRequest request = buildTool.createBasicInvocationRequest( pom, properties, goals, buildLog );
        request.setUpdateSnapshots( false );
        request.setShowErrors( true );
        request.getProperties().setProperty( "downloadSources", "false" );
        request.getProperties().setProperty( "downloadJavadocs", "false" );

        // request.setDebug( true );
        if ( switchLocalRepo )
        {
            request.setLocalRepositoryDirectory( localRepositoryDirectory );
        }
        InvocationResult result = buildTool.executeMaven( request );

        if ( result.getExitCode() != 0 )
        {
            String buildLogUrl = buildLog.getAbsolutePath();
            try
            {
                buildLogUrl = buildLog.toURL().toExternalForm();
            }
            catch ( MalformedURLException e )
            {
            }
            throw new ExecutionFailedException( "Failed to execute build.\nPOM: " + pom + "\nGoals: "
                    + StringUtils.join( goals.iterator(), ", " ) + "\nExit Code: " + result.getExitCode() + "\nError: "
                    + result.getExecutionException() + "\nBuild Log: " + buildLogUrl + "\n", result );
        }
    }

    protected MavenProject readProject( File pom )
        throws TestToolsException
    {
        return projectTool.readProject( pom, localRepositoryDirectory );
    }

    protected String getPluginCLISpecification()
    {
        return GROUP_ID + ":" + ARTIFACT_ID + ":" + VERSION + ":";
    }

    /**
     * @param baseDir the base directory of the project
     * @param projectOutputDir the directory where the plugin will write the output files.
     */
    private void compareDirectoryContent( File baseDir, File projectOutputDir )
            throws IOException, MojoExecutionException
    {
        File expectedConfigDir = new File( baseDir, EXPECTED_DIRECTORY_NAME + File.separator );
        if ( expectedConfigDir.isDirectory() )
        {
            File[] expectedFilesToCompare = expectedConfigDir.listFiles( new FileFilter()
            {
                public boolean accept( File file )
                {
                    return !file.isDirectory();
                }
            } );

            for ( int j = 0; j < expectedFilesToCompare.length; j++ )
            {
                File expectedFile = expectedFilesToCompare[j];
                File actualFile = new File( projectOutputDir, expectedFile.getName() ).getCanonicalFile();

                if ( !actualFile.exists() )
                {
                    throw new AssertionFailedError( "Expected file not found: " + actualFile.getAbsolutePath() );
                }

                assertFileEquals( expectedFile, actualFile, baseDir );
            }
        }
    }

    protected void assertFileEquals( File expectedFile, File actualFile, File baseDir )
            throws IOException, MojoExecutionException
    {
        if ( !actualFile.exists() )
        {
            throw new AssertionFailedError( "Expected file not found: " + actualFile.getAbsolutePath() );
        }

        List expectedLines = getLines( expectedFile );
        List actualLines = getLines( actualFile );
        for ( int i = 0; i < expectedLines.size(); i++ )
        {
            String expected = expectedLines.get( i ).toString();

            // replace some vars in the expected line, to account for absolute paths that are different on each
            // installation.
            expected = StringUtils.replace( expected, "${basedir}", baseDir.getCanonicalPath() );
            expected = StringUtils.replace( expected, "${M2_TEST_REPO}", localRepositoryDirectory.getCanonicalPath() );

            if ( actualLines.size() <= i )
            {
                fail( "Too few lines in the actual file. Was " + actualLines.size() + ", expected: "
                        + expectedLines.size() );
            }
            String actual = actualLines.get( i ).toString();
            if ( expected.startsWith( "#" ) && actual.startsWith( "#" ) )
            {
                // ignore comments, for settings file
                continue;
            }
            assertEquals( "Comparing '" + actualFile.getName() + "' against '"
                + expectedFile.getName() + "' at line #" + ( i + 1 ), expected, actual );
        }
        assertTrue( "Unequal number of lines.", expectedLines.size() == actualLines.size() );
    }

    private List getLines( File file )
        throws MojoExecutionException
    {
        try
        {
            List/*<String>*/lines = new ArrayList/*<String>*/();
            BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( file ), "UTF-8" ) );
            String line;
            while ( (line = reader.readLine()) != null )
            {
                lines.add( line );
            }
            IOUtil.close( reader );
            return lines;
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "failed to getLines", e );
        }
    }

}
