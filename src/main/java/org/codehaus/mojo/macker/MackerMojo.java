package org.codehaus.mojo.macker;

/*
 * Copyright 2007 Wayne Fay. Created August 16, 2007.
 *
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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.plexus.util.FileUtils;

import net.innig.macker.Macker;
import net.innig.macker.event.ListenerException;
import net.innig.macker.event.MackerIsMadException;
import net.innig.macker.rule.RuleSeverity;
import net.innig.macker.rule.RulesException;
import net.innig.macker.structure.ClassParseException;

/**
 * Runs Macker against the compiled classes of the project.
 *
 * @goal macker
 * @description Executes Macker against the classes.
 * @execute phase="compile"
 * @requiresDependencyResolution compile
 * @requiresProject
 * @author <a href="http://www.codehaus.org/~wfay/">Wayne Fay</a>
 * @author <a href="http://people.apache.org/~bellingard/">Fabrice Bellingard</a>
 */
public class MackerMojo
    extends AbstractMojo
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
     * Directory containing the rules files for Macker.
     *
     * @parameter expression="${basedir}/src/main/config"
     * @required
     */
    private File rulesDirectory;

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
    private int maxmsg;

    /**
     * Print threshold. Valid options are error, warning, info, and debug.
     *
     * @parameter expression="${print}"
     */
    private String print;

    /**
     * Anger threshold. Valid options are error, warning, info, and debug.
     *
     * @parameter expression="${anger}"
     */
    private String anger;

    /**
     * Name of the Macker rules file.
     *
     * @parameter expression="${rule}" default-value="macker-rules.xml"
     */
    private String rule;

    /**
     * Name of the Macker rules files.
     *
     * @parameter expression="${rules}"
     */
    private String[] rules;

    /**
     * Variables map that will be passed to Macker.
     *
     * @parameter expression="${variables}"
     */
    private Map<String, String> variables = new HashMap<String, String>();

    /**
     * Verbose setting for Macker tool execution.
     *
     * @parameter expression="${verbose}" default-value="false"
     */
    private boolean verbose;

    /**
     * Fail the build on an error.
     *
     * @parameter default-value="true"
     */
    private boolean failOnError;

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
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        // check if rules were specified
        if ( null == rules || 0 == rules.length )
        {
            rules = new String[1];
            rules[0] = rule;
        }

        if ( !classesDirectory.isDirectory() )
        {
           throw new MojoExecutionException( "Error during Macker execution: " + classesDirectory.getAbsolutePath() + " is not a directory" );
        }

        // check if there are class files to analyze
        String files[] = FileUtils.getFilesFromExtension( classesDirectory.getPath(), new String[] { "class" } );
        if ( files == null || files.length == 0 )
        {
            // no class file, we can't do anything
            getLog().info( "No class files in specified directory " + classesDirectory );
        }
        else
        {
            if ( !outputDirectory.exists() )
            {
               if ( !outputDirectory.mkdirs() )
               {
                  throw new MojoExecutionException( "Error during Macker execution: Could not create directory " + outputDirectory.getAbsolutePath() );
               }
            }

            // let's go!
            File outputFile = new File( outputDirectory, outputName );
            launchMacker( outputFile, files );
        }
    }

    /**
     * Executes Macker as requested.
     *
     * @param outputFile the result file that will should produced by macker
     * @param files classes files that should be analysed
     * @throws MojoExecutionException if a error occurs during Macker execution
     * @throws MojoFailureException if Macker detects a failure.
     */
    private void launchMacker( File outputFile, String[] files )
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            Macker macker = createMacker( outputFile );
            configureRules( macker );
            initMackerVariables( macker );
            specifyClassFilesToAnalyse( files, macker );
            // we're OK with configuration, let's run Macker
            macker.check();
            // if we're here, then everything went fine
            getLog().info( "Macker has not found any violation." );
        }
        catch ( MackerIsMadException ex )
        {
            getLog().warn( "Macker has detected violations. Please refer to the XML report for more information." );
            if ( failOnError )
            {
                throw new MojoFailureException( "MackerIsMadException during Macker execution.", ex );
            }
        }
        catch ( RulesException ex )
        {
            throw new MojoExecutionException( "Macker rules are not properly defined: " + ex.getMessage(), ex );
        }
        catch ( ListenerException ex )
        {
            throw new MojoExecutionException( "Error during Macker execution: " + ex.getMessage(), ex );
        }
        catch ( ClassParseException ex )
        {
            throw new MojoExecutionException( "Error during Macker execution: " + ex.getMessage(), ex );
        }
        catch ( IOException ex )
        {
            throw new MojoExecutionException( "Error during Macker execution: " + ex.getMessage(), ex );
        }
    }

    /**
     * Tell Macker where to look for Class files to analyze.
     *
     * @param files the ".class" files to analyze
     * @param macker the Macker instance
     * @throws IOException if there's a problem reading a file
     * @throws ClassParseException if there's a problem parsing a class
     */
    private void specifyClassFilesToAnalyse( String[] files, Macker macker )
        throws IOException, ClassParseException
    {
        for ( int i = 0; i < files.length; i++ )
        {
            macker.addClass( new File( files[i] ) );
        }
    }

    /**
     * If specific variables are set in the POM, give them to Macker.
     *
     * @param macker the Macker isntance
     */
    private void initMackerVariables( Macker macker )
    {
        if ( variables != null && variables.size() > 0 )
        {
            Iterator<String> it = variables.keySet().iterator();
            while ( it.hasNext() )
            {
                String key = it.next();
                macker.setVariable( key, variables.get( key ) );
            }
        }
    }

    /**
     * Configure Macker with the rule files specified in the POM.
     *
     * @param macker the Macker instance
     * @throws IOException if there's a problem reading a file
     * @throws RulesException if there's a problem parsing a rule file
     */
    private void configureRules( Macker macker )
        throws IOException, RulesException
    {
        File ruleFile = null;
        for ( int i = 0; i < rules.length; i++ )
        {
            getLog().debug( "Add rules file: " + rulesDirectory + File.separator + rules[i] );
            ruleFile = new File( rulesDirectory, rules[i] );
            macker.addRulesFile( ruleFile );
        }
    }

    /**
     * Prepares Macker for the analysis.
     *
     * @param outputFile the result file that will should produced by Macker
     * @return the new instance of Macker
     */
    private Macker createMacker( File outputFile )
    {
        Macker macker = new net.innig.macker.Macker();
        macker.setVerbose( verbose );
        macker.setXmlReportFile( outputFile );
        if ( maxmsg > 0 )
        {
            macker.setPrintMaxMessages( maxmsg );
        }
        if ( print != null )
        {
            macker.setPrintThreshold( RuleSeverity.fromName( print ) );
        }
        if ( anger != null )
        {
            macker.setAngerThreshold( RuleSeverity.fromName( anger ) );
        }
        return macker;
    }

    /**
     * Returns the MavenProject object.
     *
     * @return MavenProject
     */
    public MavenProject getProject()
    {
        return project;
    }
}
