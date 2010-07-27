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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.xml.sax.SAXException;

public class XmlComparer
    extends Assert
{

    private static final String DEFAULT_DATE = "Sun Apr 25 01:23:20 CEST 2010";
    private final String controlFileFolder;

    public XmlComparer( String folder )
    {
        controlFileFolder = folder;
    }

    //private String readCleanedXml(File name) throws IOException
    //{
    //  return org.apache.commons.io.FileUtils.readFileToString( name ).replaceAll("<timestamp>.*?</timestamp>", "" );
    //}
    //
    //private void compareXml( String controlName, File generatedFile ) throws IOException
    //{
    //  File controlFile = new File( controlFileFolder + controlName );
    //  String controlText = readCleanedXml( controlFile );
    //  String generatedText = readCleanedXml( generatedFile );
    //  assertEquals(controlText, generatedText );
    //}

    public void compareXml( String controlFile, File generatedFile )
        throws SAXException, IOException
    {
        Diff xmlDiff = new Diff( new FileReader( controlFileFolder + controlFile ), new FileReader( generatedFile ) );
        DetailedDiff detailedDiff = new DetailedDiff( xmlDiff );
        List/*<Difference>*/differences = detailedDiff.getAllDifferences();
        assertEquals( differences.toString(), 1, differences.size() );
        Difference diff = (Difference) differences.get( 0 ); // timestamp
        assertEquals( DEFAULT_DATE, diff.getControlNodeDetail().getValue() );
    }

}
