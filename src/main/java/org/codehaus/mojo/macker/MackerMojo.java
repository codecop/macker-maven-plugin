package org.codehaus.mojo.macker;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Copyright 2007 Wayne Fay.
 * Created on August 16, 2007
 */

import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;
import net.innig.macker.Macker;
import net.innig.macker.rule.RuleSeverity;
import net.innig.macker.rule.RulesException;
import net.innig.macker.event.ListenerException;
import net.innig.macker.event.MackerIsMadException;
import net.innig.macker.structure.ClassParseException;

/**
 * Runs Macker against the compiled classes of the project.
 *
 * @goal macker
 * @description Executes Macker against the classes.
 * @execute phase="compile"
 * @requiresDependencyResolution
 * @requiresProject
 *
 * @author <a href="mailto:wfay@codehaus.org">Wayne Fay</a>
 */
public class MackerMojo extends AbstractMojo
{
    /**
     * Directory containing the class files for Macker to analyze.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     * @readonly
     */
    private File classesDirectory;

    /**
     * Directory where the Macker output file will be generated.
     *
     * @parameter default-value="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * Name of the Macker output file.
     *
     * @parameter expression="${outputName}" default-value="macker-out.xml"
     * @required
     */
    private String outputName;

    /**
     * Print max messages.
     *
     * @parameter expression="${maxmsg}" default-value="0"
     */
    private int printMaxMessages;

    /**
     * Print threshold. Valid options are error, warning, info, and debug.
     *
     * @parameter expression="${print}" 
     */
    private String printThreshold;

    /**
     * Anger threshold. Valid options are error, warning, info, and debug.
     *
     * @parameter expression="${anger}" 
     */
    private String angerThreshold;

    /**
     * Name of the Macker rules file.
     *
     * @parameter expression="${rule}" default-value="macker-rules.xml"
     */
    private String rule;

    /**
     * Variables map that will be passed to Macker.
     *
     * @parameter expression="${variables}" 
     */
    private Map variables = new HashMap();

    /**
     * Verbose setting for Macker tool execution.
     *
     * @parameter expression="${verbose}" default-value="false"
     */
    private boolean verbose;

    /**
     * <i>Maven Internal</i>: Project to interact with.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @throws MojoExecutionException
     * See org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException
    {
        File outputFile = new File( outputDirectory, outputName );
        File ruleFile = new File( classesDirectory, rule );

        String files[] = FileUtils.getFilesFromExtension( classesDirectory.getPath(), new String[]{"class"} );
        if ( ( files == null ) || ( files.length == 0 ) )
        {
            System.out.println( "No class files in specified directory " + classesDirectory );
        }
        else
        {
            try
            {
                Macker macker = new net.innig.macker.Macker();
                macker.setVerbose( verbose );
                macker.setXmlReportFile( outputFile );
                if ( printMaxMessages > 0 )
                {
                    macker.setPrintMaxMessages( printMaxMessages );
                }
                if ( printThreshold != null )
                {
                    macker.setPrintThreshold( RuleSeverity.fromName( printThreshold ) );
                }
                if ( angerThreshold != null )
                {
                    macker.setAngerThreshold( RuleSeverity.fromName( angerThreshold ) );
                }
                macker.addRulesFile( ruleFile );

                if ( ( variables != null ) && ( variables.size() > 0 ) )
                {
                    Iterator it = variables.keySet().iterator();
                    while ( it.hasNext() )
                    {
                        String key = (String) it.next();
                        macker.setVariable( key, (String) variables.get( key ) );
                    }
                }

                for ( int i = 0; i < files.length; i++ )
                {
                    macker.addClass( new File( files[i] ) );
                }
                macker.check();
            }
            catch ( MackerIsMadException ex )
            {
                System.out.println( ex.getMessage() );
                throw new MojoExecutionException( "MackerIsMadException during Macker execution.", ex );
            }
            catch ( RulesException ex )
            {
                System.out.println( ex.getMessage() );
                throw new MojoExecutionException( "RulesException during Macker execution.", ex );
            }
            catch ( ListenerException ex )
            {
                System.out.println( ex.getMessage() );
                throw new MojoExecutionException( "ListenerException during Macker execution.", ex );
            }
            catch ( ClassParseException ex )
            {
                System.out.println( ex.getMessage() );
                throw new MojoExecutionException( "ClassParseException during Macker execution.", ex );
            }
            catch ( IOException ex )
            {
                System.out.println( ex.getMessage() );
                throw new MojoExecutionException( "IOException during Macker execution.", ex );
            }
            /*
            catch ( Exception ex )
            {
                throw new MojoExecutionException( "Exception during Macker execution.", ex );
            }
            */
        }
    }

    /**
     * Returns the MavenProject object.
     *
     * @return MavenProject
     */
    public MavenProject getProject()
    {
        return this.project;
    }
}