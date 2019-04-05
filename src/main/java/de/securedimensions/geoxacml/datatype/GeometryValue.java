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

package de.securedimensions.geoxacml.datatype;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.ow2.authzforce.core.pdp.api.value.AttributeDatatype;
import org.ow2.authzforce.core.pdp.api.value.BaseAttributeValueFactory;
import org.ow2.authzforce.core.pdp.api.value.SimpleValue;

import org.w3c.dom.Node;
import org.wololo.jts2geojson.GeoJSONReader;
import org.xml.sax.SAXException;

import de.securedimensions.geoxacml.crs.SwapAxesCoordinateFilter;
import de.securedimensions.geoxacml.io.gml3.GMLWriter;

import net.sf.saxon.s9api.XPathCompiler;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the Geometry datatype <i>GeoXACML 1.0 Data Type<i>. 
 * <p>
 * It is basically the same as XACML structured datatype except that the value is interpreted as a Geometry. 
 * <p>
 * Encoding as Well Known Text or Extended Well Known Text and GeoJSON supported.
 * <p>
 * Encoding as GML2 or GML3 supported.
 * <p>
 * Used here for a geographic Authzforce datatype extension mechanism to plugin into into the PDP engine. 
 * With the combination of the GeoXACML Geometry functions extension, this allows to derive authorization decisions based on geographic conditions.
 *
 * 
 * 
 * @author Andreas Matheus, Secure Dimensions GmbH. 
 *
 */
public final class GeometryValue extends SimpleValue<Geometry>
{
	/**
	 * General Problem when implementing software that creates a geometry from any encoding 
	 * is the axis order problem:
	 * http://docs.geotools.org/latest/userguide/library/referencing/order.html
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GeometryValue.class);

	/**
	 * Data type Geometry
	 */
	public static final AttributeDatatype<GeometryValue> DATATYPE = 
			new AttributeDatatype<GeometryValue>(GeometryValue.class, "urn:ogc:def:dataType:geoxacml:1.0:geometry", "urn:ogc:def:function:geoxacml:1.0:geometry");

	public static final class Factory extends BaseAttributeValueFactory<GeometryValue>
	{


		private static GeometryFactory gf;
		private static SAXParserFactory fact;
		private static TransformerFactory tf;
		
		public Factory ()
		{
			super(DATATYPE);
			gf = new GeometryFactory(new PrecisionModel());
			fact = SAXParserFactory.newInstance();
			tf = TransformerFactory.newInstance();
		}
				
		public GeometryValue getInstance(Serializable value, Map<QName, String> otherXmlAttributes,
				XPathCompiler xPathCompiler) 
		{
			
			LOGGER.debug("getInstance(Serializable value): {}", value);
			
			/*
			 * The encoding of a geometry must be String
			 */
			if (!(value instanceof String))
			{
				LOGGER.error("Geometry encoding must be a String. But type is: " + value.getClass().getName());
				throw new IllegalArgumentException("Geometry encoding must be a String. But type is: " + value.getClass().getName());
			}
			
			final String encoding = (String)value;
			
			try {
				Geometry g = null;
				// container to keep all the metadata for the Geometry
				WKTReader wktReader = new WKTReader(gf);
				String crsName = null;

				if(encoding.isEmpty())
				{
					// The empty string is represented by an empty GeometryCollection
					g = wktReader.read("GEOMETRYCOLLECTION EMPTY");
					
					// to be GML:Null compatible, we add a nullReason
					/* Null Reason per GML schema
				    -	inapplicable 	there is no value
					-	missing 			the correct value is not readily available to the sender of this data. Furthermore, a correct value may not exist
					-	template 		the value will be available later
					-	unknown 			the correct value is not known to, and not computable by, the sender of this data. However, a correct value probably exists
					-	withheld 		the value is not divulged
					-	other:text 		other brief explanation, where text is a string of two or more characters with no included spaces
					 */
					g.setUserData("inapplicable");
					g.setSRID(0);
				}
				else if(
						(encoding.substring(0, "SRID=".length()).equalsIgnoreCase("SRID=")) ||
						(encoding.substring(0, "CRS=".length()).equalsIgnoreCase("CRS=")))
				{
					String[] st = encoding.split(";");
					
					if (st.length != 2)
					{
						throw new IllegalArgumentException("EWKT syntax error: ';' missing?");
					}
						
					g = wktReader.read(st[1]);
					
					if (encoding.substring(0, "SRID=".length()).equalsIgnoreCase("SRID="))
					{
						crsName = "EPSG:" + st[0].substring("SRID=".length());
					}
					else if (encoding.substring(0, "CRS=".length()).equalsIgnoreCase("CRS="))
					{
						crsName = st[0].substring("CRS=".length());
					}
					else
						throw new IllegalArgumentException("Geometry based on WKT without SRID or CRS cannot be instantiated");

					g.setSRID(getSRID(crsName));
					g.setUserData(null);
				}				
				else if(
						encoding.substring(0, "NULL".length()).equalsIgnoreCase("NULL")
						)
				{
					// Encoding NULL<space>null reason
					final String nullReason = encoding.substring("NULL ".length());
					
					// The Null geometry is represented by an empty Point
					g = gf.createPoint();
					g.setSRID(0);
					g.setUserData(nullReason);
				}
				else if(
						((encoding.length() >= "POINT".length()) && encoding.substring(0, "POINT".length()).equalsIgnoreCase("POINT")) ||
						((encoding.length() >= "POLYGON".length()) &&encoding.substring(0, "POLYGON".length()).equalsIgnoreCase("POLYGON")) ||
						((encoding.length() >= "LINESTRING".length()) &&encoding.substring(0, "LINESTRING".length()).equalsIgnoreCase("LINESTRING")) ||
						((encoding.length() >= "LINEARRING".length()) &&encoding.substring(0, "LINEARRING".length()).equalsIgnoreCase("LINEARRING")) ||
						((encoding.length() >= "MULTIPOLYGON".length()) &&encoding.substring(0, "MULTIPOLYGON".length()).equalsIgnoreCase("MULTIPOLYGON")) ||
						((encoding.length() >= "MULTILIPOINT".length()) &&encoding.substring(0, "MULTILIPOINT".length()).equalsIgnoreCase("MULTILIPOINT")) ||
						((encoding.length() >= "MULTILINESTRING".length()) &&encoding.substring(0, "MULTILINESTRING".length()).equalsIgnoreCase("MULTILINESTRING")) ||
						((encoding.length() >= "GEOMETRYCOLLECTION".length()) &&encoding.substring(0, "GEOMETRYCOLLECTION".length()).equalsIgnoreCase("GEOMETRYCOLLECTION"))
					)
				{
					if (otherXmlAttributes == null)
						throw new IllegalArgumentException("WKT geometry requires CRS definition as attribute in AttributeValue!");
					
					LOGGER.debug("otherXmlAttributes: " + otherXmlAttributes);
					
					QName crsAttr = new QName("http://www.opengis.net/geoxacml","crs");
					crsName = otherXmlAttributes.get(crsAttr);
					
					if (crsName == null)
						throw new IllegalArgumentException("WKT geometry encoding with no crs defined!");

					g = wktReader.read(encoding);
					if (g.isEmpty())
					{
						g.setSRID(0);
						g.setUserData("inapplicable");
					}
					else
					{
						g.setSRID(getSRID(crsName));
						g.setUserData(null);
					}
				}
				else if (encoding.substring(0, "{".length()).equalsIgnoreCase("{")) {
					try
					{
						GeoJSONReader geojsonReader = new GeoJSONReader();
						g = geojsonReader.read(encoding);

						/* 
						 * Axis order as defined in IETF 7946: LON/LAT
						 * "The coordinate reference system for all GeoJSON coordinates is a
						   geographic coordinate reference system, using the World Geodetic
						   System 1984 (WGS 84) [WGS84] datum, with longitude and latitude units
						   of decimal degrees.  This is equivalent to the coordinate reference
						   system identified by the Open Geospatial Consortium (OGC) URN
						   urn:ogc:def:crs:OGC::CRS84."[https://tools.ietf.org/html/rfc7946#section-4]
						 * 
						 */
						// The internal code -4326 is used in the constructor to correct the axes order
						g.setSRID(-4326);
						g.setUserData(null);
					}
					catch (RuntimeException e) {
						e.printStackTrace();
						throw new IllegalArgumentException("RuntimeException: " + e.getMessage());
					}
				}
				else
					throw new IllegalArgumentException("Unknown geometry encoding");
								
				return new GeometryValue(g);
			}
			catch (ParseException e) {
				e.printStackTrace();
				throw new IllegalArgumentException("ParseException: " + e.getMessage());
			}
			catch (RuntimeException e) {
				e.printStackTrace();
				throw new IllegalArgumentException("RuntimeException: " + e.getMessage());
			} 
			
		}
		
		@Override
		public GeometryValue getInstance(final List<Serializable> content, final Map<QName, String> otherXmlAttributes, final XPathCompiler xPathCompiler) throws IllegalArgumentException
		{
			Geometry g;
			String crsName = null;
				
			LOGGER.debug("getInstance(List<Serializable> content)");
			if (content == null)
			{
				throw new IllegalArgumentException("Invalid content for datatype '" + DATATYPE.getId() + "': empty");
			}

			try
			{
			
				/*
				 * If content is empty, e.g. <AttributeValue DataType="urn:ogc:def:dataType:geoxacml:1.0:geometry"/>, 
				 * then the content is invalid
				 */
				final Iterator<?> contentIterator = content.iterator();
				if (!contentIterator.hasNext())
				{
					throw new IllegalArgumentException("Invalid content for datatype '" + DATATYPE.getId() + "': empty");
				}

				/*
				 * 2 possibilities:
				 * 
				 * 1) first item is Policy element
				 * 
				 * 2) first item is String (if there is some whitespace for instance before the XML tag), then one Policy,...
				 */

				/*
				 * Tricky part: The content is supposed to be a GML encoded geometry
				 */
				Object x = contentIterator.next();
				if (x instanceof String)
				{
					// In case whitespace is the first element, lets check for more content...
					if (!contentIterator.hasNext())
					{
						// Strange but not sure this could happen. The actual getInstance(Serializable value, ...)
						// should have caught this already...
						LOGGER.debug("Geometry using String encoding");
						return getInstance((String)x, otherXmlAttributes, null);
					}
					else
					{
						// In case there is more content behind the String, let's increase the pointer
						x = contentIterator.next();
					}
				}
				 
				// Test if the geometry has a GML encoding. Then, the node 'x' should be an implementation of Node...
				if (x instanceof Node)
				{
					Node gmlNode = (Node)x;
					String name = gmlNode.getNodeName();
					String namespace = gmlNode.getNamespaceURI();
					LOGGER.debug("node name: {}", name);
					LOGGER.debug("namespace: {}", namespace);
					
					// Dealing with GML Null geometries
					if (gmlNode.getLocalName().equalsIgnoreCase("Null"))
					{
						// The Null geometry is represented by an empty Point
						g = gf.createPoint();
						g.setSRID(0);
						g.setUserData(gmlNode.getTextContent());

						return new GeometryValue(g);
					}

					// We have to process a real GML geometry
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					Source xmlSource = new DOMSource(gmlNode);
					Result outputTarget = new StreamResult(outputStream);
					tf.newTransformer().transform(xmlSource, outputTarget);
					InputStream is = new ByteArrayInputStream(outputStream.toByteArray());

					if (namespace.equalsIgnoreCase("http://www.opengis.net/gml"))
					{
						// GML2
						SAXParser parser = fact.newSAXParser();

						org.locationtech.jts.io.gml2.GMLHandler gh = new org.locationtech.jts.io.gml2.GMLHandler(gf,null);
						parser.parse(is, gh);
						g = gh.getGeometry();

					}
					else if (namespace.equalsIgnoreCase("http://www.opengis.net/gml/3.2"))
					{
						// GML3
						SAXParser parser = fact.newSAXParser();
						
						de.securedimensions.geoxacml.io.gml3.GMLHandler gh = new de.securedimensions.geoxacml.io.gml3.GMLHandler(gf,null);
						parser.parse(is, gh);
						g = gh.getGeometry();

					}
					else
					{
						// no other encoding supported...
						throw new IllegalArgumentException("Namespace is neither GML2 nor GML3");						
					}
	                
					// We need to get the CRS name
                    Node srsNode = gmlNode.getAttributes().getNamedItem("srsName");
                    if (srsNode != null)
                    {
            				crsName = srsNode.getNodeValue().trim();

            				LOGGER.debug("crs from GML element: " + crsName);
                    }
                    else
                    {
                    		LOGGER.error("crs from GML element missing");
                    		throw new IllegalArgumentException("crs from GML element missing");
                    }
                                        
                    g.setSRID(getSRID(crsName));
                    g.setUserData(null);

                    return new GeometryValue(g);
	                    
				}
				else
				{
					throw new IllegalArgumentException("Unknown Geometry encoding");
				}
			} catch (TransformerFactoryConfigurationError e) {
				throw new IllegalArgumentException("TransformerFactoryConfigurationError: " + e.getMessage());
			} catch (RuntimeException e) {
				throw new IllegalArgumentException("RuntimeException: " + e.getMessage());
			} catch (TransformerConfigurationException e) {
				throw new IllegalArgumentException("TransformerFactoryConfigurationError: " + e.getMessage());
			} catch (TransformerException e) {
				throw new IllegalArgumentException("TransformerException: " + e.getMessage());
			} catch (ParserConfigurationException e) {
				throw new IllegalArgumentException("ParserConfigurationException: " + e.getMessage());
			} catch (SAXException e) {
				throw new IllegalArgumentException("SAXException: " + e.getMessage());
			} catch (IOException e) {
				throw new IllegalArgumentException("IOException: " + e.getMessage());
			} 
		}
	}
		
	public static final Factory FACTORY = new Factory();
	
	/**
	 * Returns a new <code>GeometryValue</code> that represents the name indicated by the <code>Geometry</code> provided.
	 * @param val
	 *            a geometry instance
	 * @throws java.lang.IllegalArgumentException
	 *             if format of {@code val} does not comply with the geometry datatype definition
	 */
	public GeometryValue(Geometry g) throws IllegalArgumentException
	{
		super(g);
									
		/* 
		 * GEOMETRY NORMALIZATION
		 * 
		 * In order to deal with the axis order confusion for code 4326 and be able to compare geometries independent of encoding,
		 * this implementation normalizes all geometry encodings to LAT/LON using EASTING or NORTHING
		 * So for example for a geometry encoded with 
		 *  - 'EPSG:4326' will not be processed as this implementation ASSUMES that the axis order is LAT/LON
		 *  - 'urn:ogc:def:crs:OGC::CRS84' (LON/LAT) will have the axis swapped to make it LAT/LON
		 *  - '' (southing) will have the LAT value inverted
		 *  - '' (westing) will have the LON value inverted
		 */
		if (g.getSRID() == -4326)
		{
			value.apply(new SwapAxesCoordinateFilter());
			value.geometryChanged();
			value.setSRID(4326);
		}

		
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode()
	{
		return value.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 * 
	 * We override the equals because for geometry, we have to use the JTS topological test function g1.equals(g2)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		
		if (!(obj instanceof GeometryValue))
		{
			return false;
		}

		Geometry g1 = this.getUnderlyingValue();
		Geometry g2 = ((GeometryValue) obj).getUnderlyingValue();

		// Test for exact equal - NOT for topological equals. That is done via the geometry-equals function.
		// This function is the basic primitive that is used e.g. with Bag/Set functions
		return g1.equalsExact(g2);
	}

	private static int getSRID(String srsName) throws IllegalArgumentException
	{
		
		String []parts = srsName.split("[/,:]");
		String srsCode = parts[parts.length-1];
		
		if (srsCode.equalsIgnoreCase("CRS84"))
			return -4326;
		else if (srsCode.equalsIgnoreCase("84"))
			return -4326;
		else if (srsCode.equalsIgnoreCase("WGS84"))
			return -4326;
		else	
		{
			int srid = 0;
			try {
				srid = Integer.valueOf(srsCode); 
				return srid;
			}
			catch (NumberFormatException e)
			{
				throw new IllegalArgumentException("Unknown CRS: " + srsName);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public String printXML()
	{
		final Geometry g = this.getUnderlyingValue();

		// GML3
		GMLWriter writer = new GMLWriter();
		writer.setNamespace(true);
		writer.setSrsName("EPSG:" + g.getSRID());
		return writer.write(g);
	}

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		WKTWriter wkt = new WKTWriter();
		
		final Geometry g = this.getUnderlyingValue();

		return "SRID=" + String.valueOf(g.getSRID()) + ";" + wkt.write(g);
	}
	
	@Override
	public Map<QName, String> getXmlAttributes() {
		return new HashMap<QName, String>();
	}
}
