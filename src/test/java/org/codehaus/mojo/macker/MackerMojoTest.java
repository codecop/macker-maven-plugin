package org.codehaus.mojo.macker;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import java.io.File;

import org.codehaus.plexus.util.FileUtils;

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

    public void testNotFailOnViolation() throws Exception
    {
        File testPom = new File( getBasedir(), "src/test/resources/unit/notfailonviolation-plugin-config.xml" );
        MackerMojo mojo = (MackerMojo) lookupMojo( "macker", testPom );
        mojo.execute();

        File generatedFile = new File( getBasedir(), "target/test/unit/target/macker-out-violations.xml" );
        assertTrue( "macker-out-violations was not created", FileUtils.fileExists( generatedFile.getAbsolutePath() ) );
        // TODO assert XML
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

}
