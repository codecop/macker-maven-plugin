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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.resource.ResourceManager;
import org.codehaus.plexus.resource.loader.FileResourceCreationException;
import org.codehaus.plexus.resource.loader.FileResourceLoader;
import org.codehaus.plexus.resource.loader.ResourceNotFoundException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

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
     * The directories containing the test-classes to be analyzed.
     *
     * @parameter expression="${project.build.testOutputDirectory}"
     * @required
     * @readonly
     */
    private File testClassesDirectory;

    /**
     * A list of files to exclude from checking. Can contain Ant-style wildcards and double wildcards. Note that these
     * exclusion patterns only operate on the path of a source file relative to its source root directory. In other
     * words, files are excluded based on their package and/or class name. If you want to exclude entire root
     * directories, use the parameter <code>excludeRoots</code> instead.
     *
     * @parameter
     */
    private String[] excludes;

    /**
     * A list of files to include from checking. Can contain Ant-style wildcards and double wildcards.
     * Defaults to **\/*.class.
     *
     * @parameter
     */
    private String[] includes;

    /**
     * Run Macker on the tests.
     *
     * @parameter default-value="false"
     */
    private boolean includeTests;

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
     * @component
     * @required
     * @readonly
     */
    private ResourceManager locator;

    /**
     * Variables map that will be passed to Macker.
     *
     * @parameter expression="${variables}"
     */
    private Map/*<String, String>*/ variables = new HashMap/*<String, String>*/();

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
     * Skip the checks.  Most useful on the command line
     * via "-Dmacker.skip=true".
     *
     * @parameter expression="${macker.skip}" default-value="false"
     */
    private boolean skip;

    /**
     * <i>Maven Internal</i>: Project to interact with.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @throws MojoExecutionException if a error occurs during Macker execution
     * @throws MojoFailureException if Macker detects a failure.
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        //configure ResourceManager
        locator.addSearchPath( FileResourceLoader.ID, project.getFile().getParentFile().getAbsolutePath() );
        locator.addSearchPath( "url", "" );
        locator.setOutputDirectory( new File( project.getBuild().getDirectory() ) );

        if ( skip )
        {
            return;
        }

        // check if rules were specified
        if ( null == rules || 0 == rules.length )
        {
            if ( null == rule )
            {
               throw new MojoExecutionException( "Error during Macker execution: no rule file defined" );
            }
            rules = new String[1];
            rules[0] = rule;
        }
        if ( classesDirectory==null || !classesDirectory.isDirectory() )
        {
           throw new MojoExecutionException( "Error during Macker execution: " + classesDirectory.getAbsolutePath() + " is not a directory" );
        }
        if ( includeTests && (testClassesDirectory == null || !testClassesDirectory.isDirectory()) )
        {
           throw new MojoExecutionException( "Error during Macker execution: " + testClassesDirectory.getAbsolutePath() + " is not a directory" );
        }
        if ( rulesDirectory != null && !rulesDirectory.isDirectory() )
        {
           throw new MojoExecutionException( "Error during Macker execution: " + rulesDirectory.getAbsolutePath() + " is not a directory" );
        }

        // check if there are class files to analyze
        List/*<File>*/ files;
        try
        {
            files = getFilesToProcess();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error during Macker execution: error in file selection", e );
        }
        if ( files == null || files.size() == 0 )
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
    private void launchMacker( File outputFile, List/*<File>*/ files )
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
                throw new MojoFailureException( "MackerIsMadException during Macker execution: " + ex.getMessage() );
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
    private void specifyClassFilesToAnalyse( List/*<File>*/ files, Macker macker )
        throws IOException, ClassParseException
    {
        for ( Iterator/*<File>*/ i = files.iterator(); i.hasNext(); )
        {
            macker.addClass( (File) i.next() );
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
            Iterator/*<String>*/ it = variables.keySet().iterator();
            while ( it.hasNext() )
            {
                String key = (String) it.next();
                macker.setVariable( key, (String) variables.get( key ) );
            }
        }
    }

    /**
     * Configure Macker with the rule files specified in the POM.
     *
     * @param macker the Macker instance
     * @throws IOException if there's a problem reading a file
     * @throws RulesException if there's a problem parsing a rule file
     * @throws MojoExecutionException if a error occurs during Macker execution
     */
    private void configureRules( Macker macker )
        throws IOException, RulesException, MojoExecutionException
    {
        try
        {
            for ( int i = 0; i < rules.length; i++ )
            {
                String set = rules[i];
                File ruleFile = new File( rulesDirectory, set );
                if ( ruleFile.exists() )
                {
                    getLog().debug( "Add rules file: " + rulesDirectory + File.separator + rules[i] );
                }
                else
                {
                    getLog().debug( "Preparing ruleset: " + set );
                    ruleFile = locator.getResourceAsFile( set, getLocationTemp( set ) );

                    if ( null == ruleFile )
                    {
                        throw new MojoExecutionException( "Could not resolve " + set );
                    }
                }
                macker.addRulesFile( ruleFile );
            }
        }
        catch ( ResourceNotFoundException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( FileResourceCreationException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }

    /**
     * Convenience method to get the location of the specified file name.
     *
     * @param name the name of the file whose location is to be resolved
     * @return a String that contains the absolute file name of the file
     */
    private String getLocationTemp( String name )
    {
        String loc = name;
        if ( loc.indexOf( '/' ) != -1 )
        {
            loc = loc.substring( loc.lastIndexOf( '/' ) + 1 );
        }
        if ( loc.indexOf( '\\' ) != -1 )
        {
            loc = loc.substring( loc.lastIndexOf( '\\' ) + 1 );
        }
        getLog().debug( "Before: " + name + " After: " + loc );
        return loc;
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

    /**
     * Convenience method to get the list of files where the PMD tool will be executed
     *
     * @return a List of the files where the MACKER tool will be executed
     * @throws IOException if there's a problem scanning the directories
     */
    private List/*<File>*/ getFilesToProcess()
        throws IOException
    {
        List/*<File>*/ directories = new ArrayList/*<File>*/();
        directories.add( classesDirectory );
        if ( includeTests )
        {
            directories.add( testClassesDirectory );
        }

        String excluding = getExcludes();
        getLog().debug( "Exclusions: " + excluding );
        String including = getIncludes();
        getLog().debug( "Inclusions: " + including );

        List/*<File>*/ files = new LinkedList/*<File>*/();

        for ( Iterator/*<File>*/ i = directories.iterator(); i.hasNext(); )
        {
            File sourceDirectory = (File) i.next();
            if ( sourceDirectory.isDirectory() )
            {
                List/*<File>*/ newfiles = FileUtils.getFiles( sourceDirectory, including, excluding );
                files.addAll( newfiles );
            }
        }

        return files;
    }

    /**
     * Gets the comma separated list of effective include patterns.
     *
     * @return The comma separated list of effective include patterns, never <code>null</code>.
     */
    private String getIncludes()
    {
        Collection/*<String>*/ patterns = new LinkedHashSet/*<String>*/();
        if ( includes != null )
        {
            patterns.addAll( Arrays.asList( includes ) );
        }
        if ( patterns.isEmpty() )
        {
            patterns.add( "**/*.class" );
        }
        return StringUtils.join( patterns.iterator(), "," );
    }

    /**
     * Gets the comma separated list of effective exclude patterns.
     *
     * @return The comma separated list of effective exclude patterns, never <code>null</code>.
     */
    private String getExcludes()
    {
        Collection/*<String>*/ patterns = new LinkedHashSet/*<String>*/( FileUtils.getDefaultExcludesAsList() );
        if ( excludes != null )
        {
            patterns.addAll( Arrays.asList( excludes ) );
        }
        return StringUtils.join( patterns.iterator(), "," );
    }

    public void setRules( String[] ruleSets )
    {
        rules = ruleSets;
    }

}
