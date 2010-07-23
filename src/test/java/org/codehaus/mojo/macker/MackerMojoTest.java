package org.codehaus.mojo.macker;

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.xml.sax.SAXException;

public class MackerMojoTest
    extends AbstractMojoTestCase
{
    private static final String TEST_PROJECT = "target/test/unit";
    private static final String TEST_TARGET = TEST_PROJECT + "/target/";
    private static final String TEST_POM_LOCATION = "src/test/resources/unit/";
    private static final String DEFAULT_DATE = "Sun Apr 25 01:23:20 CEST 2010";

    protected void setUp() throws Exception
    {
        super.setUp();

        File testTarget = new File( getBasedir(), TEST_TARGET );
        FileUtils.deleteDirectory( testTarget );
        testTarget.mkdirs();

        final String examplesPath = "org/codehaus/mojo/macker/example";
        File examplesTarget = new File( testTarget, "classes/" + examplesPath );
        examplesTarget.mkdirs();
        FileUtils.copyDirectory( new File( getBasedir(), "target/test-classes/" + examplesPath ), examplesTarget );

        final String testClassesPath = "org/codehaus/mojo/macker/testclasses";
        File testClassesTarget = new File( testTarget, "test-classes/" + testClassesPath );
        testClassesTarget.mkdirs();
        FileUtils.copyDirectory( new File( getBasedir(), "target/test-classes/" + testClassesPath ), testClassesTarget );
    }

    private File copyPom( String source ) throws IOException
    {
        final File testPom = new File( getBasedir(), TEST_PROJECT + "/pom.xml" );
        FileUtils.copyFile( new File( getBasedir(), TEST_POM_LOCATION + source ), testPom );
        return testPom;
    }

    public void testDefaultConfiguration() throws Exception
    {
        // POM configures a ruleset that does not fail on the given classes
        File testPom = copyPom( "default-configuration-plugin-config.xml" );
        MackerMojo mojo = (MackerMojo) lookupMojo( "macker", testPom );
        assertNotNull( mojo );
        mojo.execute();

        //check if the output files were generated
        File generatedFile = new File( getBasedir(), TEST_TARGET + "macker-out.xml" );
        assertTrue( "macker-out.xml was not created", FileUtils.fileExists( generatedFile.getAbsolutePath() ) );
        assertTrue( "macker-out.xml is empty", generatedFile.length() > 0 );
    }

    public void testNotFailOnViolation() throws Exception
    {
        // POM configures a ruleset that fails on the given classes
        // but failOnError is false
        File testPom = copyPom( "notfailonviolation-plugin-config.xml" );
        MackerMojo mojo = (MackerMojo) lookupMojo( "macker", testPom );
        mojo.execute();

        // assert XML "macker-out.xml"
        File generatedFile = new File( getBasedir(), TEST_TARGET + "macker-out.xml" );
        assertTrue( "macker-out was not created", FileUtils.fileExists( generatedFile.getAbsolutePath() ) );
        assertOutput( "violation-configuration/macker-out.xml", generatedFile );
    }

    public void testNotFailOnViolationButBroken() throws Exception
    {
        // POM configures plugin with a wrong value
        File testPom = copyPom( "broken-notfailon-plugin-config.xml" );
        MackerMojo mojo = (MackerMojo) lookupMojo( "macker", testPom );
        try
        {
            mojo.execute();
            fail( "MojoExecutionException should be thrown." );
        }
        catch ( MojoExecutionException e )
        {
            File generatedFile = new File( getBasedir(), TEST_TARGET + "macker-out.xml" );
            assertFalse( generatedFile.exists() );
        }
    }

    private void assertOutput( String controlFile, File generatedFile ) throws SAXException, IOException
    {
        Diff xmlDiff = new Diff( new FileReader( TEST_POM_LOCATION + controlFile ), new FileReader( generatedFile ) );
        DetailedDiff detailedDiff = new DetailedDiff( xmlDiff );
        List/*<Difference>*/differences = detailedDiff.getAllDifferences();
        assertEquals( 1, differences.size() );
        Difference diff = (Difference) differences.get( 0 ); // timestamp
        assertEquals( DEFAULT_DATE, diff.getControlNodeDetail().getValue() );
    }

    public void testFailOnViolation() throws Exception
    {
        // POM configures a ruleset that fails on the given classes
        File testPom = copyPom( "failonviolation-plugin-config.xml" );
        MackerMojo mojo = (MackerMojo) lookupMojo( "macker", testPom );
        try
        {
            mojo.execute();
            fail( "MojoFailureException should be thrown." );
        }
        catch ( MojoFailureException e )
        {
            // assert XML "macker-out.xml"
            File generatedFile = new File( getBasedir(), TEST_TARGET + "macker-out.xml" );
            assertTrue( "macker-out was not created", FileUtils.fileExists( generatedFile.getAbsolutePath() ) );
            assertOutput( "violation-configuration/macker-out.xml", generatedFile );
        }
    }

    public void testFailOnBroken() throws Exception
    {
        // POM configures plugin with a wrong value
        File testPom = copyPom( "broken-configuration-plugin-config.xml" );
        MackerMojo mojo = (MackerMojo) lookupMojo( "macker", testPom );
        try
        {
            mojo.execute();
            fail( "MojoExecutionException should be thrown." );
        }
        catch ( MojoExecutionException e )
        {
            File generatedFile = new File( getBasedir(), TEST_TARGET + "macker-out.xml" );
            assertFalse( generatedFile.exists() );
        }
    }

    public void testSkipped() throws Exception
    {
        // POM configures a ruleset that fails on the given classes
        // but the whole check is skipped
        File testPom = copyPom( "skip-plugin-config.xml" );
        MackerMojo mojo = (MackerMojo) lookupMojo( "macker", testPom );
        mojo.execute();

        // would fail, but did not because it's skipped
        File generatedFile = new File( getBasedir(), TEST_TARGET + "macker-out.xml" );
        assertFalse( generatedFile.exists() );
    }

    public void testIgnoreTestClasses() throws Exception
    {
        // POM configures a ruleset that fails on the given test classes
        // but the test classes are not configured zo execute
        File testPom = copyPom( "notfailontestclasses-plugin-config.xml" );
        MackerMojo mojo = (MackerMojo) lookupMojo( "macker", testPom );
        mojo.execute();

        //check if the output files were generated
        File generatedFile = new File( getBasedir(), TEST_TARGET + "macker-out.xml" );
        assertTrue( "macker-out.xml was not created", FileUtils.fileExists( generatedFile.getAbsolutePath() ) );
    }

    public void testFailInTestClasses() throws Exception
    {
        // POM configures a ruleset that fails on the given test classes
        File testPom = copyPom( "failontestclasses-plugin-config.xml" );
        MackerMojo mojo = (MackerMojo) lookupMojo( "macker", testPom );
        try
        {
            mojo.execute();
            fail( "MojoFailureException should be thrown." );
        }
        catch ( MojoFailureException e )
        {
            File generatedFile = new File( getBasedir(), TEST_TARGET + "macker-out.xml" );
            assertTrue( "macker-out was not created", FileUtils.fileExists( generatedFile.getAbsolutePath() ) );
            assertOutput( "testclasses-configuration/macker-out.xml", generatedFile );
        }
    }

    public void testIgnoreMissingTestClassesWhenIncluded() throws Exception
    {
        // POM configures a include tests
        // but test-classes folder is not there
        File testPom = copyPom( "includetestswithoutclasses-configuration-plugin-config.xml" );
        MackerMojo mojo = (MackerMojo) lookupMojo( "macker", testPom );
        mojo.execute();

        //check if the output files were generated
        File generatedFile = new File( getBasedir(), TEST_TARGET + "macker-out.xml" );
        assertTrue( "macker-out.xml was not created", FileUtils.fileExists( generatedFile.getAbsolutePath() ) );
    }

    public void testSingleRuleInList() throws Exception
    {
        // POM configures two rulesets that each fail on the given classes
        File testPom = copyPom( "onerule-configuration-plugin-config.xml" );
        MackerMojo mojo = (MackerMojo) lookupMojo( "macker", testPom );
        mojo.execute();

        //check if the output files were generated
        File generatedFile = new File( getBasedir(), TEST_TARGET + "macker-out.xml" );
        assertTrue( "macker-out.xml was not created", FileUtils.fileExists( generatedFile.getAbsolutePath() ) );
    }

    public void testMultipleRules() throws Exception
    {
        // POM configures two rulesets that each fail on the given classes
        File testPom = copyPom( "tworule-configuration-plugin-config.xml" );
        MackerMojo mojo = (MackerMojo) lookupMojo( "macker", testPom );
        try
        {
            mojo.execute();
            fail( "MojoFailureException should be thrown." );
        }
        catch ( MojoFailureException e )
        {
            // assert XML "macker-out.xml"
            File generatedFile = new File( getBasedir(), TEST_TARGET + "macker-out.xml" );
            assertTrue( "macker-out was not created", FileUtils.fileExists( generatedFile.getAbsolutePath() ) );
            assertOutput( "double-configuration/macker-out.xml", generatedFile );
        }
    }

    public void testExcludes() throws Exception
    {
        // POM configures a ruleset that fails on the given classes
        // but the offending class is excluded
        File testPom = copyPom( "excludefailonviolation-plugin-config.xml" );
        MackerMojo mojo = (MackerMojo) lookupMojo( "macker", testPom );
        mojo.execute();

        //check if the output files were generated
        File generatedFile = new File( getBasedir(), TEST_TARGET + "macker-out.xml" );
        assertTrue( "macker-out.xml was not created", FileUtils.fileExists( generatedFile.getAbsolutePath() ) );
    }

    public void testFileURL() throws Exception
    {
        // POM configures a ruleset that fails on the given classes
        File testPom = copyPom( "norule-configuration-plugin-config.xml" );
        MackerMojo mojo = (MackerMojo) lookupMojo( "macker", testPom );
        URL url = getClass().getClassLoader().getResource( "unit/default-configuration/macker-rules.xml" );
        mojo.setRules( new String[] { url.toString() } );
        mojo.execute();

        //check if the output files were generated
        File generatedFile = new File( getBasedir(), TEST_TARGET + "macker-out.xml" );
        assertTrue( "macker-out.xml was not created", FileUtils.fileExists( generatedFile.getAbsolutePath() ) );
    }

    public void testClasspathRules() throws Exception
    {
        // POM configures a rulesets from classpath that fails on the given classes
        File testPom = copyPom( "classpath-configuration-plugin-config.xml" );
        MackerMojo mojo = (MackerMojo) lookupMojo( "macker", testPom );
        try
        {
            mojo.execute();
            fail( "MojoFailureException should be thrown." );
        }
        catch ( MojoFailureException e )
        {
            // assert XML "macker-out.xml"
            File generatedFile = new File( getBasedir(), TEST_TARGET + "macker-out.xml" );
            assertTrue( "macker-out was not created", FileUtils.fileExists( generatedFile.getAbsolutePath() ) );
            assertOutput( "violation-configuration/macker-out.xml", generatedFile );
        }
    }

}
