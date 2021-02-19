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
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wololo.jts2geojson.GeoJSONWriter;
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
	
	private static GeometryFactory gf;
	
	private static final String GML2_POINT = "\n"
			+ " <gml:Point xmlns:gml=\"http://www.opengis.net/gml\" gml:id=\"WashingtonMonument\"\n" + 
			"    			 srsName=\"EPSG:4326\"><gml:coord srsDimension=\"2\"><gml:X>38.889444</gml:X><gml:Y>-77.035278</gml:Y></gml:coord>\n" + 
			"  			</gml:Point>";
	
	private static final String GML32_POINT  = " <gml:Point xmlns:gml=\"http://www.opengis.net/gml/3.2\" gml:id=\"WashingtonMonument\"\n" + 
			"    			 srsName=\"EPSG:4326\"><gml:pos srsDimension=\"2\">38.889444 -77.035278</gml:pos>\n" + 
			"  			</gml:Point>";

	private static final String GML33_POINT  = "<gml:Point xmlns:gml=\"http://www.opengis.net/gml/3.3\" gml:id=\"WashingtonMonument\"\n" + 
			"    			 srsName=\"EPSG:4326\"><gml:pos srsDimension=\"2\">38.889444 -77.035278</gml:pos>\n" + 
			"  			</gml:Point>";

	private static final String GML32_CIRCLE  = "<gml:CircleByCenterPoint xmlns:gml=\"http://www.opengis.net/gml/3.2\" gml:id=\"WashingtonMonument\"\n" + 
			"    			 srsName=\"EPSG:4326\"><gml:pos srsDimension=\"2\">38.889444 -77.035278</gml:pos>\n" + 
			"					<gml:radius> 45 </gml:radius>\n" +
			"  			</gml:CircleByCenterPoint>";

	private static final String GML33_CIRCLE  = "<gml:CircleByCenterPoint xmlns:gml=\"http://www.opengis.net/gml/3.3\" gml:id=\"WashingtonMonument\"\n" + 
			"    			 srsName=\"EPSG:4326\"><gml:pos srsDimension=\"2\">38.889444 -77.035278</gml:pos>\n" + 
			"					<gml:radius> 45 </gml:radius>\n" +
			"  			</gml:CircleByCenterPoint>";

	private static final String GML2_NULL = "<gml:Null xmlns:gml=\"http://www.opengis.net/gml\" gml:id=\"WashingtonMonument\">UNAVAILABLE</gml:Null>";
	
	private static final String GML32_NULL = "<gml:Null xmlns:gml=\"http://www.opengis.net/gml/3.2\" gml:id=\"WashingtonMonument\">UNAVAILABLE</gml:Null>";
	
	private static final String GML33_NULL = "<gml:Null xmlns:gml=\"http://www.opengis.net/gml/3.3\" gml:id=\"WashingtonMonument\">UNAVAILABLE</gml:Null>";


	
	static {
		gf = new GeometryFactory(new PrecisionModel());
	}
	
	@Parameters
	public static Collection<Object[]> data()
	{
		Map<QName, String> otherXmlAttributes = new HashMap<QName, String>();
		otherXmlAttributes.put(new QName("http://www.opengis.net/geoxacml", "crs"), "EPSG:4326");
		Processor processor = new Processor(false);
		XPathCompiler xPathCompiler = processor.newXPathCompiler();

		org.w3c.dom.Document gml2_Point = null, gml32_Point = null, gml33_Point = null, gml2_Null = null, gml32_Null = null, gml33_Null = null, gml32_Circle = null, gml33_Circle = null;
			
		Coordinate c = new Coordinate(-77.035278,38.889444);
		Point p = gf.createPoint(c);

		GeoJSONWriter geojsonWriter = new GeoJSONWriter();
		String geoJSONGeometry = geojsonWriter.write(p).toString();
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			
			gml2_Point = builder.parse(new InputSource(new StringReader(GML2_POINT))); 
			gml32_Point = builder.parse(new InputSource(new StringReader(GML32_POINT)));  
			gml33_Point = builder.parse(new InputSource(new StringReader(GML33_POINT)));  

			gml32_Circle = builder.parse(new InputSource(new StringReader(GML32_CIRCLE)));  
			gml33_Circle = builder.parse(new InputSource(new StringReader(GML33_CIRCLE)));  

			gml2_Null = builder.parse(new InputSource(new StringReader(GML2_NULL))); 
			gml32_Null = builder.parse(new InputSource(new StringReader(GML32_NULL)));  
			gml33_Null = builder.parse(new InputSource(new StringReader(GML33_NULL)));  			
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		final Object[][] data = new Object[][] { 
			
			/*
			 * WKT encoding
			 */
			// WKT encoding with default SRS
			{ "POINT(-77.035278 38.889444)", null, xPathCompiler, "WKT Point with default SRS", true},
			// WKT encoding with SRS in otherXMLAttributes
			{ "POINT(38.889444 -77.035278)", otherXmlAttributes, xPathCompiler, "WKT Point with using SRS as attribute in AttributeValue", true},

			// WKT Circle test
			{ "CIRCLE(-77.035278 38.889444, 45)", null, xPathCompiler, "WKT Circle with 1D center point and radius of 45°", true},
			{ "CIRCLE(-77.035278 38.889444 1, 45)", null, xPathCompiler, "WKT Circle with 1.5D center point at height 1 and radius of 45°", true},

			// WKT Null test
			{ "NULL nothing", null, xPathCompiler, "WKT Null with reason 'nothing'", true},

			// EWKT encoding with SRID
			{ "SRID=4326;POINT(-77.035278 38.889444)", null, xPathCompiler, "WKT Point + SRID with default SRS", true},
			{ "SRID=4326;POINT(38.889444 -77.035278)", otherXmlAttributes, xPathCompiler, "WKT Point + SRID with using SRS as attribute in AttributeValue", true},
			{ "SRID=4326;CIRCLE(-77.035278 38.889444, 45)", null, xPathCompiler, "WKT Circle + SRID with 1D center point and radius of 45°", true},

			// XWKT encoding with SRS
			{ "SRS=urn:ogc:def:crs:OGC::CRS84;POINT(-77.035278 38.889444)", null, xPathCompiler, "WKT Point + SRID with default SRS", true},
			{ "SRS=EPSG:4326;POINT(38.889444 -77.035278)", otherXmlAttributes, xPathCompiler, "WKT Point + SRID with using SRS as attribute in AttributeValue", true},
			{ "SRS=CRS84;CIRCLE(-77.035278 38.889444, 45)", null, xPathCompiler, "WKT Circle + SRID with 1D center point and radius of 45°", true},

			/*
			 * GML encoding as SimpleAttributeValue
			 */
			// GML2 encoding
			{ Arrays.asList((Serializable)gml2_Point.getDocumentElement()), null, xPathCompiler, "GML2 encoding of Point", true},
			{ Arrays.asList((Serializable)gml2_Null.getDocumentElement()), null, xPathCompiler, "GML2 encoding of Null", true},
			
			// GML32 encoding
			{ Arrays.asList((Serializable)gml32_Point.getDocumentElement()), null, xPathCompiler, "GML3.2 encoding of Point", true},
			{ Arrays.asList((Serializable)gml32_Null.getDocumentElement()), null, xPathCompiler, "GML3.2 encoding of Null", true},
			{ Arrays.asList((Serializable)gml32_Circle.getDocumentElement()), null, xPathCompiler, "GML3.2 encoding of CircleByCenterPoint", true},

			// GML33 encoding
			{ Arrays.asList((Serializable)gml33_Point.getDocumentElement()), null, xPathCompiler, "GML3.3 encoding of Point", true},
			{ Arrays.asList((Serializable)gml33_Null.getDocumentElement()), null, xPathCompiler, "GML3.3 encoding of Null", true},
			{ Arrays.asList((Serializable)gml33_Circle.getDocumentElement()), null, xPathCompiler, "GML3.3 encoding of CircleByCenterPoint", true},

			// GeoJSON encoding
			{ geoJSONGeometry, null, null, "GeoJSON encoding of Point", true},	

			// WKT encoding with SRS in otherXMLAttributes
			{ Arrays.asList((Serializable)"POINT(-77.035278 38.889444)"), otherXmlAttributes, null, "WKT Point with using SRS as attribute in AttributeValue", true},
			
			// GeoJSON encoding
			{ Arrays.asList((Serializable)geoJSONGeometry), null, null, "GeoJSON encoding with correct axes order", true},	
			
			
			/*
			 * INCORRECT encoding tests
			 */
			/*
			 * GML encoding using two child elements
			 */
			{ Arrays.asList((Serializable)gml2_Point.getDocumentElement(),(Serializable)gml2_Null.getDocumentElement()), null, xPathCompiler, "GML2 encoding of Point and Null", false},

			// WKT Array
			{ Arrays.asList((Serializable)"POINT(-77.035278 38.889444)",(Serializable)"CIRCLE(-77.035278 38.889444 1, 45)"), null, null, "WKT encoding of Point and Circle", new IllegalArgumentException()},

		};
		return Arrays.asList(data);
	}

	private final Object value;
	private final String comment;
	private final Object isValid;
	private final Map<QName, String> otherXmlAttributes;
	private final XPathCompiler xPathCompiler;
	

	public GeometryAttributeTest(Object geometry, Map<QName, String> otherXmlAttributes, XPathCompiler xPathCompiler, String comment, Object isValid)
	{
		this.value = geometry;
		this.otherXmlAttributes = otherXmlAttributes;
		this.xPathCompiler = xPathCompiler;
		this.comment = comment;
		this.isValid = isValid;
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test()
	{
		LOGGER.info("Test Begin: " + comment);
		Boolean isValidResult = false;
		GeometryValue gv;

		try
		{

			if (this.value instanceof String)
				gv = GeometryValue.FACTORY.getInstance((String)this.value, this.otherXmlAttributes, this.xPathCompiler);
			else if (this.value instanceof List<?>)
				gv = GeometryValue.FACTORY.getInstance((List<Serializable>)this.value, this.otherXmlAttributes, this.xPathCompiler);
			else
				throw new AssertionError("Unknown encoding for GeometryValue: " + this.value);
			
			LOGGER.debug("GeometryValue: " + gv);
			isValidResult = gv.getUnderlyingValue().isValid();
			Assert.assertEquals("Test failed on: '" + this.value + "' (" + this.comment + ")", (Boolean)isValid, isValidResult);
			LOGGER.info("Test Success: " + comment);
		}
		catch (AssertionError e)
		{
			LOGGER.error(e.getLocalizedMessage());
		}
		catch (IllegalArgumentException e)
		{
			LOGGER.info("Test Success: " + comment);
		}
		finally
		{
			LOGGER.info("Test Finished: " + comment);
		}
	}

}
