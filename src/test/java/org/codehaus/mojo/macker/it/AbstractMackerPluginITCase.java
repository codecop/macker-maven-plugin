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

import org.apache.maven.model.DeploymentRepository;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.model.Site;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.test.plugin.BuildTool;
import org.apache.maven.shared.test.plugin.PluginTestTool;
import org.apache.maven.shared.test.plugin.ProjectTool;
import org.apache.maven.shared.test.plugin.RepositoryTool;
import org.apache.maven.shared.test.plugin.TestToolsException;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.codehaus.classworlds.ClassRealm;
import org.codehaus.mojo.macker.XmlComparer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.xml.sax.SAXException;

/**
 * An abstract testcase using the maven-plugin-testing-tools.
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a> (original Eclipse AbstractEclipsePluginIT)
 * @author <a href="mailto:fgiust@apache.org">Fabrizio Giustina</a> (original Eclipse AbstractEclipsePluginIT)
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a> (Modified for ounce-maven-plugin)
 * @author <a href="http://www.code-cop.org/">Peter Kofler</a>
 */
public abstract class AbstractMackerPluginITCase
    extends AbstractMojoTestCase
{

    private BuildTool buildTool;

    private ProjectTool projectTool;

    private RepositoryTool repositoryTool;

    /**
     * Test repository directory.
     */
    private static File localRepositoryDirectory;

    /**
     * Pom File.
     */
    private static File pomFile = new File( getBasedir(), "pom.xml" );

    /**
     * Version under which the plugin was installed to the test-time local repository for running test builds.
     */
    protected static final String VERSION = "test";

    private static final String BUILD_OUTPUT_DIRECTORY = "target/test-build-logs";

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
        repositoryTool = (RepositoryTool) lookup(RepositoryTool.ROLE, "default");

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
        new File(BUILD_OUTPUT_DIRECTORY).mkdirs();

        synchronized (AbstractMackerPluginITCase.class)
        {
            if ( !installed )
            {
                PluginTestTool pluginTestTool = (PluginTestTool) lookup( PluginTestTool.ROLE, "default" );
                localRepositoryDirectory = pluginTestTool.preparePluginForUnitTestingWithMavenBuilds( pomFile, VERSION,
                        localRepositoryDirectory );
                System.out.println( "*** Installed test-version of the plugin to: " + localRepositoryDirectory );
                installed = true;
                // fails if the local repo is not in default place. use setting -Dmaven.repo.local=<.repository> to fix this
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
        File baseDir = getTestModuleBaseDir( projectName );
        testProject( baseDir, new Properties(), goalList );
    }

    private File getTestModuleBaseDir( String projectName )
    {
        return getTestFile( "target/it-build-target/test-classes/it/" + projectName );
    }

    /**
     * Execute the plugin.
     * @param testModuleBaseDir Execute the plugin goal on a test project and verify generated files.
     * @param properties additional properties
     * @param goalList comma separated list of goals to execute
     * @throws Exception any exception generated during test
     */
    protected void testProject( File testModuleBaseDir, Properties properties, String goalList )
        throws Exception
    {
        File pom = new File( testModuleBaseDir, "pom.xml" );
        // this pom needs to be mangled, so it uses localAsRemote repository!
        File mangledPom = manglePomForTestModule(pom);

        String[] goal = goalList.split( "\\s*,\\s*" );
        List/*<String>*/ goals = new ArrayList/*<String>*/( Arrays.asList( goal ) );

        System.out.println( "Now Building the test module " + testModuleBaseDir.getName() + "..." );
        System.out.println( "Using staged module-pom: " + mangledPom.getAbsolutePath() );
        executeMaven( mangledPom, properties, goals, true );

        MavenProject project = readProject( mangledPom );
        File projectOutputDir = new File( project.getBuild().getDirectory() );

        compareMackerOutput( testModuleBaseDir, projectOutputDir );
    }

    /**
     * Inject the local repository into the test module project's POM.
     *
     * @param testModulePomFile The test module project POM
     * @return the temporary file to which the mangled POM was written.
     * @throws TestToolsException if any
     */
    private File manglePomForTestModule( File testModulePomFile ) throws TestToolsException
    {
        Model model = null;

        Reader reader = null;
        try
        {
            reader = ReaderFactory.newXmlReader( testModulePomFile );
            model = new MavenXpp3Reader().read( reader );
        }
        catch ( IOException e )
        {
            throw new TestToolsException( "Error creating test-time version of POM for: " + testModulePomFile, e );
        }
        catch ( XmlPullParserException e )
        {
            throw new TestToolsException( "Error creating test-time version of POM for: " + testModulePomFile, e );
        }
        finally
        {
            IOUtil.close( reader );
        }

        File output = new File( testModulePomFile.getParentFile(), "pom-test-module.xml" );
        output.deleteOnExit();
        Writer writer = null;
        try
        {
            Repository localAsRemote = new Repository();
            localAsRemote.setId( "testing.mainLocalAsRemote" );
            File localRepoDir = repositoryTool.findLocalRepositoryDirectory();
            localAsRemote.setUrl( localRepoDir.toURI().toURL().toExternalForm() );

            model.addRepository( localAsRemote );
            model.addPluginRepository( localAsRemote );

            DeploymentRepository deployRepo = new DeploymentRepository();
            deployRepo.setId( "integration-test.output" );
            File tmpDir = FileUtils.createTempFile( "integration-test-repo", "", null );
            String tmpUrl = tmpDir.toURI().toURL().toExternalForm();
            deployRepo.setUrl( tmpUrl );

            DistributionManagement distMgmt = new DistributionManagement();
            distMgmt.setRepository( deployRepo );
            distMgmt.setSnapshotRepository( deployRepo );

            Site site = new Site();
            site.setId( "integration-test.output" );
            site.setUrl( tmpUrl );
            distMgmt.setSite( site );

            model.setDistributionManagement( distMgmt );
            model.addProperty( "integration-test.deployment.repo.url", tmpUrl );

            writer = WriterFactory.newXmlWriter( output );
            new MavenXpp3Writer().write( writer, model );
        }
        catch ( IOException e )
        {
            throw new TestToolsException( "Error creating test-time version of POM for: " + testModulePomFile, e );
        }
        finally
        {
            IOUtil.close( writer );
        }
        return output;
    }
    
    private void executeMaven( File pom, Properties properties, List goals, boolean switchLocalRepo )
         throws TestToolsException
    {
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

        final Properties invocationProperties = new Properties();
        if ( properties != null )
        {
            invocationProperties.putAll( properties );
        }
        InvocationRequest request = buildTool.createBasicInvocationRequest( pom, invocationProperties, goals, buildLog );
        request.setUpdateSnapshots( false );
        request.setShowErrors( true );
        request.getProperties().setProperty( "downloadSources", "false" );
        request.getProperties().setProperty( "downloadJavadocs", "false" );
        if ( System.getProperty( MavenSettingsBuilder.ALT_USER_SETTINGS_XML_LOCATION ) != null )
        {
            File settings = new File( System.getProperty( MavenSettingsBuilder.ALT_USER_SETTINGS_XML_LOCATION ) );
            if ( settings.exists() )
            {
                request.setUserSettingsFile( settings );
            } // else allow empty setting to have default behaviour too
        }

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
            throw new TestToolsException( "Failed to execute build.\nPOM: " + pom + "\nGoals: "
                    + StringUtils.join( goals.iterator(), ", " ) + "\nExit Code: " + result.getExitCode() + "\nError: "
                    + result.getExecutionException() + "\nBuild Log: " + buildLogUrl + "\n", result.getExecutionException() );
        }
    }

    private MavenProject readProject( File pom )
        throws TestToolsException
    {
        return projectTool.readProject( pom, localRepositoryDirectory );
    }

    /**
     * @param baseDir the base directory of the project
     * @param projectOutputDir the directory where the plugin will write the output files.
     */
    private void compareMackerOutput( File baseDir, File projectOutputDir )
            throws IOException, SAXException
    {
        File generatedFile = new File( projectOutputDir, "macker-out.xml" );
        assertTrue( "macker-out was not created", FileUtils.fileExists( generatedFile.getAbsolutePath() ) );
        new XmlComparer( baseDir.toString() + File.separator ).compareXml( EXPECTED_DIRECTORY_NAME + File.separator
                + "macker-out.xml", generatedFile );
    }
}
