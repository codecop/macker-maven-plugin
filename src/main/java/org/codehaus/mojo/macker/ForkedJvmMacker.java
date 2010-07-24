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
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.plugin.MojoExecutionException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.plexus.util.cli.Commandline;

/**
 * Forking to invoke the Macker tool. This uses a special kind of Shell that
 * invokes the JVM directly.
 * In Windows XP this has a max of 32kb command line arguments.
 */
public class ForkedJvmMacker
    extends ForkedMacker
{

    protected Commandline createCommandLine()
        throws MojoExecutionException
    {
        List/*<String>*/jvmArguments = new ArrayList/*<String>*/();
        jvmArguments.add( "-cp" );
        jvmArguments.add( createClasspath() );
        if ( maxmem != null )
        {
            jvmArguments.add( "-Xmx" + maxmem );
        }
        Commandline cl = new Commandline( new JavaShell( jvmArguments ) );
        cl.setExecutable( taskClass );
        for ( Iterator/*<String>*/it = options.iterator(); it.hasNext(); )
        {
            cl.createArg().setValue( (String) it.next() );
        }
        for ( Iterator/*<String>*/it = rules.iterator(); it.hasNext(); )
        {
            cl.createArg().setValue( (String) it.next() );
        }
        for ( Iterator/*<String>*/it = classes.iterator(); it.hasNext(); )
        {
            cl.createArg().setValue( (String) it.next() );
        }
        return cl;
    }

}
