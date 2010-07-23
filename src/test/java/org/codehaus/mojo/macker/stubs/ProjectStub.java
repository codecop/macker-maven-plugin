package org.codehaus.mojo.macker.stubs;

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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;

import java.io.File;

public class ProjectStub
    extends MavenProjectStub
{
    private static final String TEST_PROJECT = "target/test/unit";
    private static final String TEST_TARGET = TEST_PROJECT + "/target/";

    private Artifact artifactStub;
    private Build buildStub;

    public ProjectStub()
    {
        setFile( new File( getBasedir(), TEST_PROJECT + "/pom.xml" ) );

        Build build = new Build();
        build.setDirectory( getBasedir() + "/" + TEST_TARGET );
        setBuild( build );

        setArtifact( new ArtifactStub() );
    }

    public Build getBuild()
    {
        return buildStub;
    }

    public void setBuild( Build build )
    {
        buildStub = build;
    }

    public Artifact getArtifact()
    {
        return artifactStub;
    }

    public void setArtifact( Artifact artifact )
    {
        artifactStub = artifact;
    }

}
