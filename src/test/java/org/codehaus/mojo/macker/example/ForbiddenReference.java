package org.codehaus.mojo.macker.example;

import java.security.InvalidParameterException;

public class ForbiddenReference
{
    public void calculate() throws InvalidParameterException
    {
        throw new InvalidParameterException( "what a bad guy" );
    }
}
