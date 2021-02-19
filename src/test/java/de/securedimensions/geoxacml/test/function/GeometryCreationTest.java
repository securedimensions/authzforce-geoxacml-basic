/**
 * Copyright 2021 Secure Dimensions GmbH.
 *
 * This file is part of GeoXACML 3 Community Version.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.securedimensions.geoxacml.test.function;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.securedimensions.geoxacml.datatype.GeometryValue;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XPathCompiler;

/**
 * 
 * GeoXACML3 GeometryAttribute validation test. 
 */
@RunWith(value = Parameterized.class)
public class GeometryCreationTest
{
	private static final Logger LOGGER = LoggerFactory.getLogger(GeometryCreationTest.class);
	
	@Parameters
	public static Collection<Object[]> data()
	{

		final Object[][] data = new Object[][] { 
			
			// WKT encoding
			{ "POINT(38.889444 -77.035278)", "WKT encoding with default SRS", true},
						
			// EWKT encoding
			{ "SRID=4326;POINT(38.889444 -77.035278)", "WKT encoding with using SRID prefix", true},
			
			// XWKT encoding
			{ "SRS=EPSG:4326;POINT(38.889444 -77.035278)", "WKT encoding with using SRID prefix", true},
			
			// GeoJSON encoding
			{ "{ \"type\": \"Point\", \"coordinates\": [-77.035278, 38.889444] }", "GeoJSON encoding", true}
		};
		return Arrays.asList(data);
	}

	private final Object value;
	private final String comment;
	private final Boolean isValid;
	

	public GeometryCreationTest(Object geometry, String comment, Boolean isValid)
	{
		this.value = geometry;
		this.comment = comment;
		this.isValid = isValid;
	}

	@Test
	public void test()
	{
		LOGGER.info("Test Begin: " + comment);
		Boolean isValidResult = false;
		GeometryValue gv;
		
		if (this.value instanceof String)
			gv = GeometryValue.FACTORY.getInstance((String)this.value, null, null);
		else
			throw new IllegalArgumentException();
			
		isValidResult = gv.getUnderlyingValue().isValid();
		
		
		try
		{
			Assert.assertEquals("Test failed on: '" + this.value + "' (" + this.comment + ")", isValid, isValidResult);
			LOGGER.info("Test Success\n");
		}
		catch (AssertionError e)
		{
			LOGGER.error(e.getLocalizedMessage() + '\n');
		}
		finally
		{
			
		}
	}

}
