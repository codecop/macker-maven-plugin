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

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;

public class CommandlineTest extends TestCase
{

    public CommandlineTest( String name )
    {
        super( name );
    }

    public void testDefaultShellVersion() throws CommandLineException
    {
        Commandline cl = new Commandline();
        cl.setExecutable( "java" );
        cl.createArg().setValue( "-version" );

        StringStreamConsumer stdout = new StringStreamConsumer();
        StringStreamConsumer stderr = new StringStreamConsumer();
        int exitCode = CommandLineUtils.executeCommandLine( cl, stdout, stderr );
        assertEquals( 0, exitCode );
    }

    public void testDefaultShellCall() throws IOException, CommandLineException
    {
        Commandline cl = new Commandline();
        cl.setExecutable( "java" );
        cl.createArg().setValue( "-cp" );
        cl.createArg().setValue( new File( "./target/test-classes" ).getCanonicalPath() );
        cl.createArg().setValue( "org.codehaus.mojo.macker.ExitArgs" );
        cl.createArg().setValue( "oneArg" );
        cl.createArg().setValue( "2Arg" );
        cl.createArg().setValue( "3 Arg" );

        StringStreamConsumer stdout = new StringStreamConsumer();
        StringStreamConsumer stderr = new StringStreamConsumer();
        int exitCode = CommandLineUtils.executeCommandLine( cl, stdout, stderr );
        assertEquals( 3, exitCode );
    }

    public void skip_testDefaultShellMaxArguments() throws IOException, CommandLineException
    {
        Commandline cl = new Commandline();
        cl.setExecutable( "java" );
        cl.createArg().setValue( "-cp" );
        cl.createArg().setValue( new File( "./target/test-classes" ).getCanonicalPath() );
        cl.createArg().setValue( "org.codehaus.mojo.macker.ExitArgs" );
        final int max = 807; // win XP approx. 8kb
        for ( int i = 0; i < max; i++ )
        {
            cl.createArg().setValue( "a2b4c6d8x" ); // 9 plus blank is 10
        }

        StringStreamConsumer stdout = new StringStreamConsumer();
        StringStreamConsumer stderr = new StringStreamConsumer();
        int exitCode = CommandLineUtils.executeCommandLine( cl, stdout, stderr );
        assertEquals( max, exitCode );
    }

    public void testJavaShellVersion() throws CommandLineException
    {
        Commandline cl = new Commandline( new JavaShell( new String[0] ) );
        cl.createArg().setValue( "-version" );

        StringStreamConsumer stdout = new StringStreamConsumer();
        StringStreamConsumer stderr = new StringStreamConsumer();
        int exitCode = CommandLineUtils.executeCommandLine( cl, stdout, stderr );
        assertEquals( 0, exitCode );
    }

    public void testJavaShellCall() throws IOException, CommandLineException
    {
        Commandline cl = new Commandline( new JavaShell( new String[] { "-cp", new File( "./target/test-classes" ).getCanonicalPath() } ) );
        cl.setExecutable( "org.codehaus.mojo.macker.ExitArgs" );
        cl.createArg().setValue( "oneArg" );
        cl.createArg().setValue( "2Arg" );
        cl.createArg().setValue( "3 Arg" );

        StringStreamConsumer stdout = new StringStreamConsumer();
        StringStreamConsumer stderr = new StringStreamConsumer();
        int exitCode = CommandLineUtils.executeCommandLine( cl, stdout, stderr );
        assertEquals( 3, exitCode );
    }

    public void testJavaShellMaxArguments() throws IOException, CommandLineException
    {
        Commandline cl = new Commandline( new JavaShell( new String[] { "-cp", new File( "./target/test-classes" ).getCanonicalPath() } ) );
        cl.setExecutable( "org.codehaus.mojo.macker.ExitArgs" );
        final int max = 3261; // win XP approx. 32kb
        for ( int i = 0; i < max; i++ )
        {
            cl.createArg().setValue( "a2b4c6d8x" );
        }

        StringStreamConsumer stdout = new StringStreamConsumer();
        StringStreamConsumer stderr = new StringStreamConsumer();
        int exitCode = CommandLineUtils.executeCommandLine( cl, stdout, stderr );
        assertEquals( max, exitCode );
    }

}
