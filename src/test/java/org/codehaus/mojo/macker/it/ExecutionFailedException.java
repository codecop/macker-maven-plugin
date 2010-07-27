package org.codehaus.mojo.macker.it;

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

import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a> (Copied from the ounce-maven-plugin
 * copied from the Eclipse AbstractEclipsePluginTestCase v2.4)
 */
public class ExecutionFailedException
    extends Exception
{

    private static final long serialVersionUID = 1L;

    private InvocationResult result;

    public ExecutionFailedException( String message, MavenInvocationException cause )
    {
        super( message + " (Maven invoker threw an exception.)", cause );
    }

    public ExecutionFailedException( String message, InvocationResult result )
    {
        super( message + " (Resulting exit code: " + result.getExitCode() + ")" );

        this.result = result;
    }

    public InvocationResult getInvocationResult()
    {
        return result;
    }

}