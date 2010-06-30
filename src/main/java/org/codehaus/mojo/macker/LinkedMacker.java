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

import java.io.File;
import java.io.IOException;

import net.innig.macker.event.ListenerException;
import net.innig.macker.event.MackerIsMadException;
import net.innig.macker.rule.RuleSeverity;
import net.innig.macker.rule.RulesException;
import net.innig.macker.structure.ClassParseException;

/**
 * Delegator to the Macker tool.
 */
public class LinkedMacker implements Macker
{
    private final net.innig.macker.Macker macker = new net.innig.macker.Macker();

    public void addClass( File clazz ) throws IOException, MojoExecutionException
    {
        try
        {
            macker.addClass( clazz );
        }
        catch ( ClassParseException ex )
        {
            throw new MojoExecutionException( "Macker problem parsing a class: " + ex.getMessage(), ex );
        }
    }

    public void addRulesFile( File rule ) throws IOException, MojoExecutionException
    {
        try
        {
            macker.addRulesFile( rule );
        }
        catch ( RulesException ex )
        {
            throw new MojoExecutionException( "Macker rules are not properly defined: " + ex.getMessage(), ex );
        }
    }

    public void check() throws MojoExecutionException, MojoFailureException
    {
        try
        {
            macker.check();
        }
        catch ( MackerIsMadException ex )
        {
            throw new MojoFailureException( "MackerIsMadException during Macker execution: " + ex.getMessage() );
        }
        catch ( RulesException ex )
        {
            throw new MojoExecutionException( "Macker rules are not properly defined: " + ex.getMessage(), ex );
        }
        catch ( ListenerException ex )
        {
            throw new MojoExecutionException( "Error during Macker execution: " + ex.getMessage(), ex );
        }
    }

    public void setAngerThreshold( String anger )
    {
        macker.setAngerThreshold( RuleSeverity.fromName( anger ) );
    }

    public void setPrintMaxMessages( int maxMsg )
    {
        macker.setPrintMaxMessages( maxMsg );
    }

    public void setPrintThreshold( String print )
    {
        macker.setPrintThreshold( RuleSeverity.fromName( print ) );
    }

    public void setVariable( String name, String value )
    {
        macker.setVariable( name, value );
    }

    public void setVerbose( boolean verbose )
    {
        macker.setVerbose( verbose );
    }

    public void setXmlReportFile( File report )
    {
        macker.setXmlReportFile( report );
    }

}
