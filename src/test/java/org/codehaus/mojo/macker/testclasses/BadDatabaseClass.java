package org.codehaus.mojo.macker.testclasses;

import java.sql.SQLException;

public class BadDatabaseClass
{

    public void workOnDatabase() throws SQLException
    {
        throw new SQLException( "dummy" );
    }
}
