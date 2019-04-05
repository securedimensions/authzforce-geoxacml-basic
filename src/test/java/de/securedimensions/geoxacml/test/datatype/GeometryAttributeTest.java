/**
 * Copyright 2019 Secure Dimensions GmbH.
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

package de.securedimensions.geoxacml.test.datatype;

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
public class GeometryAttributeTest
{
	private static final Logger LOGGER = LoggerFactory.getLogger(GeometryAttributeTest.class);
	
	@Parameters
	public static Collection<Object[]> data()
	{
		Map<QName, String> otherXmlAttributes = new HashMap<QName, String>();
		otherXmlAttributes.put(new QName("http://www.opengis.net/geoxacml", "crs"), "EPSG:4326");
		Processor processor = new Processor(false);
		XPathCompiler xPathCompiler = processor.newXPathCompiler();

		List<Serializable> gml2, gml2Swapped, gml3, gml3Swapped;
		gml2 = new ArrayList<Serializable>();
		gml2Swapped = new ArrayList<Serializable>();

		gml3 = new ArrayList<Serializable>();
		gml3Swapped = new ArrayList<Serializable>();

		String gml2String, gml2StringSwapped, gml3String, gml3StringSwapped;
		
		gml2String = "\n"
				+ "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\" gml:id=\"WashingtonMonument\"\n" + 
				"    			 srsName=\"EPSG:4326\"><gml:coord srsDimension=\"2\"><gml:X>38.889444</gml:X><gml:Y>-77.035278</gml:Y></gml:coord>\n" + 
				"  			</gml:Point>";

		gml2StringSwapped = "\n"
				+ "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\" gml:id=\"WashingtonMonument\"\n" + 
				"    			 srsName=\"EPSG:4326\"><gml:coord srsDimension=\"2\"><gml:X>-77.035278</gml:X><gml:Y>38.889444</gml:Y></gml:coord>\n" + 
				"  			</gml:Point>";

		gml3String = "<gml:Point xmlns:gml=\"http://www.opengis.net/gml/3.2\" gml:id=\"WashingtonMonument\"\n" + 
				"    			 srsName=\"EPSG:4326\"><gml:pos srsDimension=\"2\">38.889444 -77.035278</gml:pos>\n" + 
				"  			</gml:Point>";

		gml3StringSwapped = "<gml:Point xmlns:gml=\"http://www.opengis.net/gml/3.2\" gml:id=\"WashingtonMonument\"\n" + 
				"    			 srsName=\"EPSG:4326\"><gml:pos srsDimension=\"2\">-77.035278 38.889444</gml:pos>\n" + 
				"  			</gml:Point>";

		try {
			org.w3c.dom.Document document;
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			
			document = builder.parse(new InputSource(new StringReader(gml2String))); 
			gml2.add((Serializable)document.getDocumentElement());

			document = builder.parse(new InputSource(new StringReader(gml2StringSwapped)));  
			gml2Swapped.add((Serializable)document.getDocumentElement());

			document = builder.parse(new InputSource(new StringReader(gml3String)));  
			gml3.add((Serializable)document.getDocumentElement());
			
			document = builder.parse(new InputSource(new StringReader(gml3StringSwapped)));  
			gml3Swapped.add((Serializable)document.getDocumentElement());

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		final Object[][] data = new Object[][] { 
			
			// GML2 encoding
			{ gml2, null, xPathCompiler, "GML2 encoding with correct axes order", "SRID=4326;POINT (38.889444 -77.035278)", true},
			{ gml2Swapped, null, xPathCompiler, "GML2 encoding with swapped axes order", "SRID=4326;POINT (38.889444 -77.035278)", false},

			// GML3 encoding
			{ gml3, null, xPathCompiler, "GML3 encoding with correct axes order", "SRID=4326;POINT (38.889444 -77.035278)", true},
			{ gml3Swapped, null, xPathCompiler, "GML3 encoding with swapped axes order", "SRID=4326;POINT (38.889444 -77.035278)", false},

			// WKT encoding with CRS in otherXMLAttributes
			{ "POINT(38.889444 -77.035278)", otherXmlAttributes, xPathCompiler, "WKT with using CRS as attribute in AttributeValue", "SRID=4326;POINT (38.889444 -77.035278)", true},

			// WKT encoding plus axes order (CRS=EPSG:4326)
			{ "POINT(38.889444 -77.035278)", otherXmlAttributes, xPathCompiler, "WKT with correct axes order", "SRID=4326;POINT (38.889444 -77.035278)", true},
			{ "POINT(-77.035278 38.889444)", otherXmlAttributes, xPathCompiler, "WKT with swapped axes", "SRID=4326;POINT (38.889444 -77.035278)", false},
						
			// EWKT encoding
			{ "SRID=4326;POINT(38.889444 -77.035278)", null, null, "EWKT with using SRID prefix", "SRID=4326;POINT (38.889444 -77.035278)", true},
			{ "CRS=EPSG:4326;POINT(38.889444 -77.035278)", null, null, "EWKT with using CRS prefix", "SRID=4326;POINT (38.889444 -77.035278)", true},
			
			// EWKT encoding plus axes order tests
			{ "CRS=EPSG:4326;POINT(-77.035278 38.889444)", null, null, "EWKT with swapped axes", "SRID=4326;POINT (38.889444 -77.035278)", false},
			{ "CRS=EPSG:4326;POINT(38.889444 -77.035278)", null, null, "EWKT with correct axes order", "SRID=4326;POINT (38.889444 -77.035278)", true},
			
			{ "CRS=urn:ogc:def:crs:OGC::CRS84;POINT(38.889444 -77.035278)", null, null, "EWKT with swapped axes", "SRID=4326;POINT (38.889444 -77.035278)", false},
			{ "CRS=urn:ogc:def:crs:OGC::CRS84;POINT(-77.035278 38.889444)", null, null, "EWKT with correct axes order", "SRID=4326;POINT (38.889444 -77.035278)", true},
			
			{ "CRS=WGS84;POINT(38.889444 -77.035278)", null, null, "EWKT with swapped axes", "SRID=4326;POINT (38.889444 -77.035278)", false},
			{ "CRS=WGS84;POINT(-77.035278 38.889444)", null, null, "EWKT with correct axes order", "SRID=4326;POINT (38.889444 -77.035278)", true},
			
			{ "CRS=urn:ogc:def:crs:OGC::EPSG:4326;POINT(-77.035278 38.889444)", null, null, "EWKT with swapped axes", "SRID=4326;POINT (38.889444 -77.035278)", false},
			{ "CRS=urn:ogc:def:crs:OGC::EPSG:4326;POINT(38.889444 -77.035278)", null, null, "EWKT with correct axes order", "SRID=4326;POINT (38.889444 -77.035278)", true},
			
			{ "CRS=http://www.opengis.net/def/crs/EPSG/0/4326;POINT(-77.035278 38.889444)", null, null, "EWKT with swapped axes", "SRID=4326;POINT (38.889444 -77.035278)", false},
			{ "CRS=http://www.opengis.net/def/crs/EPSG/0/4326;POINT(38.889444 -77.035278)", null, null, "EWKT with correct axes order", "SRID=4326;POINT (38.889444 -77.035278)", true},
			
			// GeoJSON encoding
			{ "{ \"type\": \"Point\", \"coordinates\": [38.889444, -77.035278] }", null, null, "GeoJSON encoding with swapped axes order", "SRID=4326;POINT (38.889444 -77.035278)", false},
			{ "{ \"type\": \"Point\", \"coordinates\": [-77.035278, 38.889444] }", null, null, "GeoJSON encoding with correct axes order", "SRID=4326;POINT (38.889444 -77.035278)", true}
		};
		return Arrays.asList(data);
	}

	private final Object value;
	private final String comment;
	private final String result;
	private final Boolean isValid;
	private final Map<QName, String> otherXmlAttributes;
	private final XPathCompiler xPathCompiler;
	

	public GeometryAttributeTest(Object geometry, Map<QName, String> otherXmlAttributes, XPathCompiler xPathCompiler, String comment, String result, Boolean isValid)
	{
		this.value = geometry;
		this.otherXmlAttributes = otherXmlAttributes;
		this.xPathCompiler = xPathCompiler;
		this.comment = comment;
		this.result = result;
		this.isValid = isValid;
	}

	@Test
	public void test()
	{
		LOGGER.info("Test Begin: " + comment);
		Boolean isValidResult = false;
		GeometryValue gv;
		
		if (this.value instanceof String)
				gv = GeometryValue.FACTORY.getInstance((String)this.value, this.otherXmlAttributes, this.xPathCompiler);
			else
				gv = GeometryValue.FACTORY.getInstance((List<Serializable>)this.value, this.otherXmlAttributes, this.xPathCompiler);
			
		GeometryValue expected = GeometryValue.FACTORY.getInstance(result, this.otherXmlAttributes, this.xPathCompiler);
		LOGGER.debug("GeometryValue: " + gv);
		LOGGER.debug("Expected result: " + result);
		isValidResult = gv.equals(expected);
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
