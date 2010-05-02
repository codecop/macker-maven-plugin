package org.codehaus.mojo.macker.stubs;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;

import java.io.File;

public class ProjectStub extends MavenProjectStub
{
    private static final String TEST_PROJECT = "target/test/unit";
    private static final String TEST_TARGET = TEST_PROJECT + "/target/";

    private Build buildStub;

    public ProjectStub()
    {
        setFile( new File( getBasedir(), TEST_PROJECT + "/pom.xml" ) );

        Build build = new Build();
        build.setDirectory( getBasedir() + "/" + TEST_TARGET );
        setBuild( build );
    }

    public Build getBuild()
    {
        return buildStub;
    }

    public void setBuild( Build build )
    {
        buildStub = build;
    }

}
