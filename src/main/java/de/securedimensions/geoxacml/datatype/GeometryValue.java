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
import java.util.Collection;
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
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.wololo.jts2geojson.GeoJSONReader;
import org.xml.sax.SAXException;

import de.securedimensions.geoxacml.crs.SwapAxesCoordinateFilter;
import de.securedimensions.geoxacml.io.gml3.GMLWriter;

import net.sf.saxon.s9api.XPathCompiler;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
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

	public static final String DEFAULT_SRS = "urn:ogc:def:crs:OGC::CRS84";
	public static final int DEAFAULT_QUADRANT_SEGMENTS = 50;
	
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
				
		public GeometryValue getInstance(Collection<Geometry> geometries)
		{
			LOGGER.debug("getInstance(Array<Geometry>)");
			
			return new GeometryValue(gf.buildGeometry(geometries));
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
				String srsName = null;

				if(encoding.isEmpty())
				{
					LOGGER.error("Geometry encoding via String must not be empty");
					throw new IllegalArgumentException("Geometry encoding via String must not be empty");
				}
			
					// to be GML:Null compatible, we add a nullReason
					/* Null Reason per GML schema
				    -	inapplicable 	there is no value
					-	missing 			the correct value is not readily available to the sender of this data. Furthermore, a correct value may not exist
					-	template 		the value will be available later
					-	unknown 			the correct value is not known to, and not computable by, the sender of this data. However, a correct value probably exists
					-	withheld 		the value is not divulged
					-	other:text 		other brief explanation, where text is a string of two or more characters with no included spaces
					 */

				if(encoding.substring(0, "SRID=".length()).equalsIgnoreCase("SRID="))
				{
					String[] st = encoding.split(";");
					
					if (st.length != 2)
					{
						throw new IllegalArgumentException("extended WKT syntax error: ';' missing?");
					}
						
					if ((st[1].length() >= "CIRCLE".length()) && st[1].substring(0, "CIRCLE".length()).equalsIgnoreCase("CIRCLE"))
					{
						g = parseCircle(gf, st[1]);
					}
					else
					{
						g = wktReader.read(st[1]);
					}
					srsName = "EPSG:" + st[0].substring("SRID=".length());
					
					g.setSRID(getSRID(srsName));
					g.setUserData(new GeometryMetadata(srsName));
				}
				else if(encoding.substring(0, "SRS=".length()).equalsIgnoreCase("SRS="))
				{
					String[] st = encoding.split(";");
					
					if (st.length != 2)
					{
						throw new IllegalArgumentException("extended WKT syntax error: ';' missing?");
					}
						
					if ((st[1].length() >= "CIRCLE".length()) && st[1].substring(0, "CIRCLE".length()).equalsIgnoreCase("CIRCLE"))
					{
						g = parseCircle(gf, st[1]);
					}
					else
					{
						g = wktReader.read(st[1]);
					}					
					srsName = st[0].substring("SRS=".length());
					
					g.setSRID(getSRID(srsName));
					g.setUserData(new GeometryMetadata(srsName));
					
					srsName = st[0].substring("SRS=".length());
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
					{
						LOGGER.info("Constructing geometry with default SRS: " + DEFAULT_SRS);
						srsName = DEFAULT_SRS;
					}
					else
					{
				
						LOGGER.debug("otherXmlAttributes: " + otherXmlAttributes);
					
						QName srsAttr = new QName("http://www.opengis.net/geoxacml","srs");
						srsName = otherXmlAttributes.get(srsAttr);
					
						if (srsName == null)
						{
							LOGGER.info("Constructing geometry with default SRS: " + DEFAULT_SRS);
							srsName = DEFAULT_SRS;
						}
					}
					
					
					g = wktReader.read(encoding);
					if (g.isEmpty())
					{
						g.setSRID(0);
						g.setUserData(new GeometryMetadata(DEFAULT_SRS,"inapplicable"));
					}
					else
					{
						g.setSRID(getSRID(srsName));
						g.setUserData(new GeometryMetadata(srsName));
					}
				}
				else if(encoding.substring(0, "NULL".length()).equalsIgnoreCase("NULL"))
				{
					// Encoding NULL<space>null reason
					final String nullReason = encoding.substring("NULL ".length());
					
					// The Null geometry is represented by an empty Point
					g = gf.createEmpty(0);
					g.setSRID(0);
					g.setUserData(new GeometryMetadata("NULL",nullReason));
				}
				else if(encoding.substring(0, "CIRCLE".length()).equalsIgnoreCase("CIRCLE"))
				{
					g = parseCircle(gf, encoding);
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
						g.setUserData(new GeometryMetadata(DEFAULT_SRS));
					}
					catch (RuntimeException e) {
						e.printStackTrace();
						throw new IllegalArgumentException(e.getMessage());
					}
				}
				else
					throw new IllegalArgumentException("Unknown geometry encoding");
								
				return new GeometryValue(g);
			}
			catch (ParseException e) {
				e.printStackTrace();
				throw new IllegalArgumentException(e.getMessage());
			}
			catch (RuntimeException e) {
				e.printStackTrace();
				throw new IllegalArgumentException(e.getMessage());
			} 
			
		}
		
		@Override
		public GeometryValue getInstance(final List<Serializable> content, final Map<QName, String> otherXmlAttributes, final XPathCompiler xPathCompiler) throws IllegalArgumentException
		{
			Geometry g;
			String srsName = null;
				
			LOGGER.debug("getInstance(List<Serializable> content)");
			if (content == null)
			{
				throw new IllegalArgumentException("Invalid content for datatype '" + DATATYPE.getId() + "': empty");
			}

			try
			{
			
				final Iterator<?> contentIterator = content.iterator();
				if (!contentIterator.hasNext())
				{
					/*
					 * If content is empty, e.g. <AttributeValue DataType="urn:ogc:def:dataType:geoxacml:1.0:geometry"/>, 
					 * then the content is invalid
					 */
					throw new IllegalArgumentException("Invalid content for XML encoded datatype '" + DATATYPE.getId() + "': empty");
				}
						
				Object x = contentIterator.next();
				if ((content.size() == 1) && (x instanceof String))
				{
					/*
					 * The content may use WKT encoding <AttributeValue DataType="urn:ogc:def:dataType:geoxacml:1.0:geometry"/> 
					 */
					return getInstance((String)x, otherXmlAttributes, null);
				}
				
				do
				{
					if (x instanceof Element)
						break;
					
					x = contentIterator.next();
				} while (contentIterator.hasNext());
				
				// Test if the geometry has a GML encoding. Then, the node 'x' should be an implementation of Node...
				if (x instanceof Element)
				{
					Element gmlNode = (Element)x;
					String name = gmlNode.getNodeName();
					String namespace = gmlNode.getNamespaceURI();
					LOGGER.debug("node name: {}", name);
					LOGGER.debug("namespace: {}", namespace);
					
					// Dealing with GML Null geometries
					if (gmlNode.getLocalName().equalsIgnoreCase("Null"))
					{
						// The Null geometry is represented by Empty
						g = gf.createEmpty(0);
						g.setUserData(new GeometryMetadata("NULL",gmlNode.getTextContent()));

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
					else if (namespace.equalsIgnoreCase("http://www.opengis.net/gml/3.2") || namespace.equalsIgnoreCase("http://www.opengis.net/gml/3.3"))
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
	                
					// We need to check for the SRS name
					NamedNodeMap attributes = gmlNode.getAttributes();
					for (int ix=0; ix < attributes.getLength(); ix++)
					{
						if (attributes.item(ix).getNodeName().contains("srsName"))
						{
							srsName = attributes.item(ix).getNodeValue().trim();
							break;
						}
					}
                    
					if (srsName == null)
                    {
        				srsName = "urn:ogc:def:crs:OGC::CRS84";
        				LOGGER.debug("srs set to default: " + srsName);
                    }
                    else
                    {
        				LOGGER.debug("srs from GML element: " + srsName);
                    }
                                        
                    g.setSRID(getSRID(srsName));
                    g.setUserData(new GeometryMetadata(srsName));

				}
				else
				{
					throw new IllegalArgumentException("Unknown Geometry encoding");
				}
		            
				return new GeometryValue(g);
				
			} catch (TransformerFactoryConfigurationError e) {
				throw new IllegalArgumentException(e.getMessage());
			} catch (RuntimeException e) {
				throw new IllegalArgumentException(e.getMessage());
			} catch (TransformerConfigurationException e) {
				throw new IllegalArgumentException(e.getMessage());
			} catch (TransformerException e) {
				throw new IllegalArgumentException(e.getMessage());
			} catch (ParserConfigurationException e) {
				throw new IllegalArgumentException(e.getMessage());
			} catch (SAXException e) {
				throw new IllegalArgumentException(e.getMessage());
			} catch (IOException e) {
				throw new IllegalArgumentException(e.getMessage());
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
		 * The EPSG registry defines LAT/LON using EASTING or NORTHING for code 4326.
		 * Assuming that most geometry processing via ADRs use the GeoJSON encoding,
		 * this implementation normalizes all geometry encodings for EPSG:4326 to the
		 * SRS defined in GeoJSON:
		 *  - SRS: urn:ogc:def:crs:OGC::CRS84
		 *  - axis order: LON/LAT
		 *  The SRID is set to -4326 to indicate the axis order to be inverse to the EPSG registry
		 */
		if (g.getSRID() == 4326)
		{
			value.apply(new SwapAxesCoordinateFilter());
			value.geometryChanged();
			value.setSRID(-4326);
			((GeometryMetadata)g.getUserData()).setSRS("urn:ogc:def:crs:OGC::CRS84");
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
		else if (srsCode.equalsIgnoreCase("urn:ogc:def:crs:OGC::CRS84"))
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
				throw new IllegalArgumentException("SRS format exception: " + srsName);
			}
		}
	}

	private static Geometry parseCircle(GeometryFactory gf, String encoding)
	{
		String circle = encoding.substring("CIRCLE(".length(), encoding.length() - 1);
		
		// Encoding CIRCLE(X Y,r) or CIRCLE(X Y Z,r)
		String[] st = circle.split(",");
		if (st.length != 2)
			throw new IllegalArgumentException("extended WKT syntax error for CIRCLE: Not in format CIRCLE(X Y,r)?");
		
		double r = Double.parseDouble(st[1].trim());
		 
		Point center;
		String[] ct = st[0].split(" ");
		if (ct.length == 2)
		{
			double x = Double.parseDouble(ct[0]);
			double y = Double.parseDouble(ct[1]);
			center = gf.createPoint(new Coordinate(x,y));
		}
		else if (ct.length == 3)
		{	
			double x = Double.parseDouble(ct[0]);
			double y = Double.parseDouble(ct[1]);
			double z = Double.parseDouble(ct[2]);
			center = gf.createPoint(new Coordinate(x,y,z));
		}
		else
			throw new IllegalArgumentException("extended WKT syntax error for CIRCLE: Not in format CIRCLE(X Y,r) or CIRCLE(X Y Z,r)?");

		center.setSRID(-4326);
		center.setUserData(new GeometryMetadata(DEFAULT_SRS));
		
		// The Circle geometry is represented by a Buffer
		Geometry g = center.buffer(r, DEAFAULT_QUADRANT_SEGMENTS);
		g.setSRID(-4326);
		g.setUserData(new GeometryMetadata(DEFAULT_SRS));
		
		return g;

	}
	
	/** {@inheritDoc} */
	@Override
	public String printXML()
	{
		final Geometry g = this.getUnderlyingValue();

		GeometryMetadata gm = (GeometryMetadata) g.getUserData();
		
		// GML3
		GMLWriter writer = new GMLWriter();
		writer.setNamespace(true);
		writer.setSrsName(gm.getSRS());
		return writer.write(g);
	}

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		WKTWriter wkt = new WKTWriter();
		
		final Geometry g = this.getUnderlyingValue();
		
		GeometryMetadata gm = (GeometryMetadata) g.getUserData();
		
		return "SRS=" + gm.getSRS() + ";" + wkt.write(g);
		
	}
	
	@Override
	public Map<QName, String> getXmlAttributes() {
		return new HashMap<QName, String>();
	}
}
