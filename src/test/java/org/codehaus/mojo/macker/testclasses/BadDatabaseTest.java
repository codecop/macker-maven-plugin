package org.codehaus.mojo.macker.testclasses;

import java.sql.SQLException;

public class BadDatabaseTest
{

    public void testDatabase() throws SQLException
    {
        throw new SQLException( "dummy" );
    }
}
