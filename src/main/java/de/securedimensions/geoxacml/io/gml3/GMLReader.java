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
 
package de.securedimensions.geoxacml.io.gml3;

import java.io.*; 

import javax.xml.parsers.*;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.xml.sax.InputSource; 
import org.xml.sax.SAXException; 
import org.xml.sax.helpers.DefaultHandler; 
 
/**
 * Adapted from org.locationtech.jts.io.gml2.GMLReader to support GML3 node names.
 * 
 * Reads a GML3 geometry from an XML fragment into a {@link Geometry}. 
 * <p> 
 * 
 * The reader ignores namespace prefixes,  
 * and disables both the validation and namespace options on the <tt>SAXParser</tt>. 
 * This class requires the presence of a SAX Parser available via the  
 * {@link javax.xml.parsers.SAXParserFactory#newInstance()} 
 * method. 
 * <p> 
 * A specification of the GML XML format  
 * can be found at the OGC web site: <a href='http://www.opengeospatial.org/'>http://www.opengeospatial.org/</a>. 
 * <p> 
 * It is the caller's responsibility to ensure that the supplied {@link PrecisionModel} 
 * matches the precision of the incoming data. 
 * If a lower precision for the data is required, a subsequent 
 * process must be run on the data to reduce its precision. 
 * <p> 
 * To parse and build geometry directly from a SAX stream, see {@link GMLHandler}. 
 * 
 * @author David Zwiers, Vivid Solutions. 
 * @author Andreas Matheus, Secure Dimensions GmbH
 *  
 * @see GMLHandler 
 */ 
public class GMLReader  
{ 
	
 /**
  * Reads a GML3 Geometry from a <tt>String</tt> into a single {@link Geometry} 
  * 
  * If a collection of geometries is found, a {@link GeometryCollection} is returned. 
  * 
  * @param gml The GML String to parse 
  * @param geometryFactory When null, a default will be used. 
  * @return the resulting JTS Geometry 
  *  
  * @throws ParserConfigurationException 
  * @throws IOException 
  * @throws SAXException 
  * 
  * @see #read(Reader, GeometryFactory) 
  */ 
 public Geometry read(String gml, GeometryFactory geometryFactory) throws SAXException, IOException, ParserConfigurationException{ 
  return read(new StringReader(gml),geometryFactory); 
 } 
 
 /**
  * Reads a GML3 Geometry from a {@link Reader} into a single {@link Geometry} 
  * 
  * If a collection of Geometries is found, a {@link GeometryCollection} is returned. 
  * 
  * @param reader The input source 
  * @param geometryFactory When null, a default will be used. 
  * @return The resulting JTS Geometry 
  * @throws SAXException 
  * @throws IOException 
  */ 
 public Geometry read(Reader reader, GeometryFactory geometryFactory) throws SAXException, IOException, ParserConfigurationException{ 
  SAXParserFactory fact = SAXParserFactory.newInstance(); 
 
  fact.setNamespaceAware(false); 
  fact.setValidating(false); 
 
  SAXParser parser = fact.newSAXParser(); 
 
  if(geometryFactory == null) 
   geometryFactory = new GeometryFactory(); 
 
  GMLHandler gh = new GMLHandler(geometryFactory,null); 
  parser.parse(new InputSource(reader), gh); 
 
  return gh.getGeometry(); 
 } 
 
}