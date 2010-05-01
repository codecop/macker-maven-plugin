package org.codehaus.mojo.macker;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.xml.sax.SAXException;

public class MackerMojoTest extends AbstractMojoTestCase
{
    private static final String TEST_TARGET = "target/test/unit/target/";
    private static final String TEST_POM_LOCATION = "src/test/resources/unit/";
    private static final String DEFAULT_DATE = "Sun Apr 25 01:23:20 CEST 2010";

    @Override
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

    public void testDefaultConfiguration() throws Exception
    {
        File testPom = new File( getBasedir(), TEST_POM_LOCATION + "default-configuration-plugin-config.xml" );
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
        File testPom = new File( getBasedir(), TEST_POM_LOCATION + "notfailonviolation-plugin-config.xml" );
        MackerMojo mojo = (MackerMojo) lookupMojo( "macker", testPom );
        mojo.execute();

        // assert XML "macker-out-violations.xml"
        File generatedFile = new File( getBasedir(), TEST_TARGET + "macker-out-violations.xml" );
        assertTrue( "macker-out-violations was not created", FileUtils.fileExists( generatedFile.getAbsolutePath() ) );
        assertOutput( "violation-configuration/macker-out-violations.xml", generatedFile );
    }

    @SuppressWarnings("unchecked")
    private void assertOutput( String controlFile, File generatedFile ) throws SAXException, IOException
    {
        Diff xmlDiff = new Diff( new FileReader( TEST_POM_LOCATION + controlFile ), new FileReader( generatedFile ) );
        DetailedDiff detailedDiff = new DetailedDiff( xmlDiff );
        List<Difference> differences = detailedDiff.getAllDifferences();
        assertEquals( 1, differences.size() );
        Difference diff = differences.get( 0 ); // timestamp
        assertEquals( DEFAULT_DATE, diff.getControlNodeDetail().getValue() );
    }

    public void testFailOnViolation() throws Exception
    {
        File testPom = new File( getBasedir(), TEST_POM_LOCATION + "failonviolation-plugin-config.xml" );
        MackerMojo mojo = (MackerMojo) lookupMojo( "macker", testPom );
        try
        {
            mojo.execute();
            fail( "MojoFailureException should be thrown." );
        }
        catch ( MojoFailureException e )
        {
            // assert XML "macker-out-violations.xml"
            File generatedFile = new File( getBasedir(), TEST_TARGET + "macker-out-violations.xml" );
            assertTrue( "macker-out-violations was not created", FileUtils.fileExists( generatedFile.getAbsolutePath() ) );
            assertOutput( "violation-configuration/macker-out-violations.xml", generatedFile );
        }
    }

    public void testSkipped() throws Exception
    {
        File testPom = new File( getBasedir(), TEST_POM_LOCATION + "skip-plugin-config.xml" );
        MackerMojo mojo = (MackerMojo) lookupMojo( "macker", testPom );
        mojo.execute();

        // would fail, but did not because it's skipped
        File generatedFile = new File( getBasedir(), TEST_TARGET + "macker-out-violations.xml" );
        assertFalse( generatedFile.exists() );
    }

    public void testNotFailInTestClasses() throws Exception
    {
        File testPom = new File( getBasedir(), TEST_POM_LOCATION + "notfailontestclasses-plugin-config.xml" );
        MackerMojo mojo = (MackerMojo) lookupMojo( "macker", testPom );
        mojo.execute();

        //check if the output files were generated
        File generatedFile = new File( getBasedir(), TEST_TARGET + "macker-out.xml" );
        assertTrue( "macker-out.xml was not created", FileUtils.fileExists( generatedFile.getAbsolutePath() ) );
    }

    public void testFailInTestClasses() throws Exception
    {
        File testPom = new File( getBasedir(), TEST_POM_LOCATION + "failontestclasses-plugin-config.xml" );
        MackerMojo mojo = (MackerMojo) lookupMojo( "macker", testPom );
        try
        {
            // fails due to violations in test classes
            mojo.execute();
            fail( "MojoFailureException should be thrown." );
        }
        catch ( MojoFailureException e )
        {
            File generatedFile = new File( getBasedir(), TEST_TARGET + "macker-out-violations.xml" );
            assertTrue( "macker-out-violations was not created", FileUtils.fileExists( generatedFile.getAbsolutePath() ) );
            assertOutput( "testclasses-configuration/macker-out-violations.xml", generatedFile );
        }
    }

}
