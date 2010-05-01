package org.codehaus.mojo.macker;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;

public class MackerMojoTest extends AbstractMojoTestCase
{
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        File target = new File( getBasedir(), "target/test/unit/target" );
        FileUtils.deleteDirectory( target );
        target.mkdirs();

        File examples = new File( target, "classes/" + "org/codehaus/mojo/macker/example" );
        examples.mkdirs();
        FileUtils.copyDirectory( new File( getBasedir(), "target/test-classes/" + "org/codehaus/mojo/macker/example" ),
                examples );
    }

    public void testDefaultConfiguration() throws Exception
    {
        File testPom = new File( getBasedir(), "src/test/resources/unit/default-configuration-plugin-config.xml" );
        MackerMojo mojo = (MackerMojo) lookupMojo( "macker", testPom );
        assertNotNull( mojo );
        mojo.execute();

        //check if the output files were generated
        File generatedFile = new File( getBasedir(), "target/test/unit/target/macker-out.xml" );
        assertTrue( "macker-out.xml was not created", FileUtils.fileExists( generatedFile.getAbsolutePath() ) );
        assertTrue( "macker-out.xml is empty", generatedFile.length() > 0 );
    }

    @SuppressWarnings("unchecked")
    public void testNotFailOnViolation() throws Exception
    {
        File testPom = new File( getBasedir(), "src/test/resources/unit/notfailonviolation-plugin-config.xml" );
        MackerMojo mojo = (MackerMojo) lookupMojo( "macker", testPom );
        mojo.execute();

        File generatedFile = new File( getBasedir(), "target/test/unit/target/macker-out-violations.xml" );
        assertTrue( "macker-out-violations was not created", FileUtils.fileExists( generatedFile.getAbsolutePath() ) );

        // assert XML "macker-out-violations.xml"
        Diff xmlDiff = new Diff( new FileReader(
                "src/test/resources/unit/violation-configuration/macker-out-violations.xml" ), new FileReader(
                generatedFile ) );
        DetailedDiff detailedDiff = new DetailedDiff( xmlDiff );
        List<Difference> differences = detailedDiff.getAllDifferences();
        assertEquals( 1, differences.size() );
        Difference diff = differences.get( 0 ); // timestamp
        assertEquals( "Sun Apr 25 01:23:20 CEST 2010", diff.getControlNodeDetail().getValue() );
    }

    public void testFailOnViolation() throws Exception
    {
        File testPom = new File( getBasedir(), "src/test/resources/unit/failonviolation-plugin-config.xml" );
        MackerMojo mojo = (MackerMojo) lookupMojo( "macker", testPom );

        try
        {
            mojo.execute();
            fail( "MojoFailureException should be thrown." );
        }
        catch ( MojoFailureException e )
        {
            assertTrue( true );
        }
    }

    public void testSkipped() throws Exception
    {
        File testPom = new File( getBasedir(), "src/test/resources/unit/skip-plugin-config.xml" );
        MackerMojo mojo = (MackerMojo) lookupMojo( "macker", testPom );
        mojo.execute();

        File generatedFile = new File( getBasedir(), "target/test/unit/target/macker-out-violations.xml" );
        assertFalse( generatedFile.exists() );
    }

}
