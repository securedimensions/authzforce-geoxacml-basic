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

import java.util.*;


import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.xml.sax.Attributes; 
import org.xml.sax.SAXException; 

import de.securedimensions.geoxacml.io.gml3.GMLHandler.Handler;

 
/**
 * Container for GML3 Geometry parsing strategies which can be represented in JTS. 
 * Adapted from GML2 GeometryStrategies by Vivid Solutions.
 * 
 * @author Andreas Matheus, Secure Dimensions GmbH. 
 * @author David Zwiers, Vivid Solutions. 
 */ 
public class GeometryStrategies
{ 

 /**
  * This set of strategies is not expected to be used directly outside of this distribution. 
  *  
  * The implementation of this class are intended to be used as static function points in C. These strategies should be associated with an element when the element begins. The strategy is utilized at the end of the element to create an object of value to the user.  
  *  
  * In this case all the objects are either java.lang.* or JTS Geometry objects 
  * 
  * @author Andreas Matheus, Secure Dimensions GmbH. 
  * @author David Zwiers, Vivid Solutions. 
  */ 
 static interface ParseStrategy{ 
  /**
   * @param arg Value to interpret 
   * @param gf GeometryFactory 
   * @return The interpreted value 
   * @throws SAXException  
   */ 
  Object parse(Handler arg, GeometryFactory gf) throws SAXException; 
 } 
  
 private static HashMap<String, ParseStrategy> strategies = loadStrategies(); 
 private static HashMap<String, ParseStrategy> loadStrategies(){ 
  HashMap<String, ParseStrategy> strats = new HashMap<String, ParseStrategy>(); 
   
  // point 
  strats.put(GMLConstants.GML_POINT.toLowerCase(),new ParseStrategy(){ 
 
   @Override
public Object parse(Handler arg, GeometryFactory gf) throws SAXException { 
    // one child, either a coord 
    // or a coordinate sequence 
     
    if(arg.children.size()!=1) 
     throw new SAXException("Cannot create a point without exactly one coordinate"); 
 
    int srid = getSrid(arg.attrs,gf.getSRID()); 
 
    Object c = arg.children.get(0); 
    Point p = null; 
    if(c instanceof Coordinate){ 
     p = gf.createPoint((Coordinate)c); 
    }else{ 
     p = gf.createPoint((CoordinateSequence)c); 
    } 
    if(p.getSRID()!=srid) 
     p.setSRID(srid); 
     
    return p; 
   } 
  }); 
   
  // linestring 
  strats.put(GMLConstants.GML_LINESTRING.toLowerCase(),new ParseStrategy(){ 
 
   @Override
public Object parse(Handler arg, GeometryFactory gf) throws SAXException { 
    // one child, either a coord 
    // or a coordinate sequence 
     
    if(arg.children.size()<1) 
     throw new SAXException("Cannot create a linestring without atleast two coordinates or one coordinate sequence"); 
 
    int srid = getSrid(arg.attrs,gf.getSRID()); 
     
    LineString ls = null; 
    if(arg.children.size() == 1){ 
     // coord set 
     try{ 
      CoordinateSequence cs = (CoordinateSequence) arg.children.get(0); 
      ls = gf.createLineString(cs); 
     }catch(ClassCastException e){ 
      throw new SAXException("Cannot create a linestring without atleast two coordinates or one coordinate sequence",e); 
     } 
    }else{ 
     try{ 
      Coordinate[] coords = (Coordinate[]) arg.children.toArray(new Coordinate[arg.children.size()]); 
      ls = gf.createLineString(coords); 
     }catch(ClassCastException e){ 
      throw new SAXException("Cannot create a linestring without atleast two coordinates or one coordinate sequence",e); 
     } 
    } 
     
    if(ls.getSRID()!=srid) 
     ls.setSRID(srid); 
     
    return ls; 
   } 
  }); 
    
  // linearring 
  strats.put(GMLConstants.GML_LINEARRING.toLowerCase(),new ParseStrategy(){ 
 
   @Override
public Object parse(Handler arg, GeometryFactory gf) throws SAXException { 
    // one child, either a coord 
    // or a coordinate sequence 
     
    if(arg.children.size()!=1 && arg.children.size()<4) 
     throw new SAXException("Cannot create a linear ring without atleast four coordinates or one coordinate sequence"); 
 
    int srid = getSrid(arg.attrs,gf.getSRID()); 
     
    LinearRing ls = null; 
    if(arg.children.size() == 1){ 
     // coord set 
     try{ 
      CoordinateSequence cs = (CoordinateSequence) arg.children.get(0); 
      ls = gf.createLinearRing(cs); 
     }catch(ClassCastException e){ 
      throw new SAXException("Cannot create a linear ring without atleast four coordinates or one coordinate sequence",e); 
     } 
    }else{ 
     try{ 
      Coordinate[] coords = (Coordinate[]) arg.children.toArray(new Coordinate[arg.children.size()]); 
      ls = gf.createLinearRing(coords); 
     }catch(ClassCastException e){ 
      throw new SAXException("Cannot create a linear ring without atleast four coordinates or one coordinate sequence",e); 
     } 
    } 
     
    if(ls.getSRID()!=srid) 
     ls.setSRID(srid); 
     
    return ls; 
   } 
  }); 

  // circle-by-centerpoint 
  strats.put(GMLConstants.GML_CIRCLE_BY_CENTER.toLowerCase(),new ParseStrategy(){ 
 
   @Override
public Object parse(Handler arg, GeometryFactory gf) throws SAXException { 
    // one child, either a coord 
    // or a coordinate sequence 
     
    if(arg.children.size()<2) 
     throw new SAXException("Cannot create a circle without atleast one point and a radius"); 
 
    int srid = getSrid(arg.attrs,gf.getSRID()); 
     
    Point center = gf.createPoint((Coordinate)arg.children.get(0)); // will be the center of the circle 
    double r = Double.parseDouble((String)arg.children.get(1));
 
    Geometry g = center.buffer(r);
     
    if(g.getSRID()!=srid) 
     g.setSRID(srid); 
     
    return g; 
   } 
  }); 

  // polygon 
  strats.put(GMLConstants.GML_POLYGON.toLowerCase(),new ParseStrategy(){ 
 
   @Override
public Object parse(Handler arg, GeometryFactory gf) throws SAXException { 
    // one child, either a coord 
    // or a coordinate sequence 
     
    if(arg.children.size()<1) 
     throw new SAXException("Cannot create a polygon without atleast one linear ring"); 
 
    int srid = getSrid(arg.attrs,gf.getSRID()); 
     
    LinearRing outer = (LinearRing) arg.children.get(0); // will be the first 
    List t = arg.children.size()>1?arg.children.subList(1,arg.children.size()):null; 
    LinearRing[] inner = t==null?null:(LinearRing[]) t.toArray(new LinearRing[t.size()]); 
     
    Polygon p = gf.createPolygon(outer,inner); 
     
    if(p.getSRID()!=srid) 
     p.setSRID(srid); 
     
    return p; 
   } 
  }); 

  // envelope 
  strats.put(GMLConstants.GML_ENVELOPE.toLowerCase(),new ParseStrategy(){ 
 
   @Override
public Object parse(Handler arg, GeometryFactory gf) throws SAXException { 
    // one child, either a coord 
    // or a coordinate sequence 
     
    if(arg.children.size()<1 || arg.children.size()>2) 
     throw new SAXException("Cannot create a box without either two coords or one coordinate sequence"); 
 
    int srid = getSrid(arg.attrs,gf.getSRID()); 
     
    Coordinate c[] = new Coordinate[2];
    if(arg.children.size() == 1){ 
     CoordinateSequence cs = (CoordinateSequence) arg.children.get(0); 
     c[0] = cs.getCoordinate(0);
     c[1] = cs.getCoordinate(1);
    }else{ 
    	c[0] = new Coordinate((Coordinate)arg.children.get(0));
    	c[1] = new Coordinate((Coordinate)arg.children.get(1)); 
    } 
    
    Coordinate[] p = new Coordinate[5];
    p[0] = p[4] = c[0];
    p[2] = c[1];
    p[1] = new Coordinate(c[1].x,c[0].y);
    p[3] = new Coordinate(c[0].x,c[1].y);
    
    return gf.createPolygon(p); 
   } 
  }); 
   
  // multi-point 
  strats.put(GMLConstants.GML_MULTI_POINT.toLowerCase(),new ParseStrategy(){ 
 
   @Override
public Object parse(Handler arg, GeometryFactory gf) throws SAXException { 
    // one child, either a coord 
    // or a coordinate sequence 
     
    if(arg.children.size()<1) 
     throw new SAXException("Cannot create a multi-point without atleast one point"); 
 
    int srid = getSrid(arg.attrs,gf.getSRID()); 
     
    Point[] pts = (Point[]) arg.children.toArray(new Point[arg.children.size()]); 
     
    MultiPoint mp = gf.createMultiPoint(pts); 
     
    if(mp.getSRID()!=srid) 
     mp.setSRID(srid); 
     
    return mp; 
   } 
  }); 
   
  // multi-linestring 
  strats.put(GMLConstants.GML_MULTI_LINESTRING.toLowerCase(),new ParseStrategy(){ 
 
   @Override
public Object parse(Handler arg, GeometryFactory gf) throws SAXException { 
    // one child, either a coord 
    // or a coordinate sequence 
     
    if(arg.children.size()<1) 
     throw new SAXException("Cannot create a multi-linestring without atleast one linestring"); 
 
    int srid = getSrid(arg.attrs,gf.getSRID()); 
     
    LineString[] lns = (LineString[]) arg.children.toArray(new LineString[arg.children.size()]); 
     
    MultiLineString mp = gf.createMultiLineString(lns); 
     
    if(mp.getSRID()!=srid) 
     mp.setSRID(srid); 
     
    return mp; 
   } 
  }); 
   
  // multi-poly 
  strats.put(GMLConstants.GML_MULTI_POLYGON.toLowerCase(),new ParseStrategy(){ 
 
   @Override
public Object parse(Handler arg, GeometryFactory gf) throws SAXException { 
    // one child, either a coord 
    // or a coordinate sequence 
     
    if(arg.children.size()<1) 
     throw new SAXException("Cannot create a multi-polygon without atleast one polygon"); 
 
    int srid = getSrid(arg.attrs,gf.getSRID()); 
     
    Polygon[] plys = (Polygon[]) arg.children.toArray(new Polygon[arg.children.size()]); 
     
    MultiPolygon mp = gf.createMultiPolygon(plys); 
     
    if(mp.getSRID()!=srid) 
     mp.setSRID(srid); 
     
    return mp; 
   } 
  }); 
   
  // multi-geom 
  strats.put(GMLConstants.GML_MULTI_GEOMETRY.toLowerCase(),new ParseStrategy(){ 
 
   @Override
public Object parse(Handler arg, GeometryFactory gf) throws SAXException { 
    // one child, either a coord 
    // or a coordinate sequence 
     
    if(arg.children.size()<1) 
     throw new SAXException("Cannot create a multi-polygon without atleast one geometry"); 
     
    Geometry[] geoms = (Geometry[]) arg.children.toArray(new Geometry[arg.children.size()]); 
     
    GeometryCollection gc = gf.createGeometryCollection(geoms); 
         
    return gc; 
   } 
  }); 
   
  // posList 
  strats.put(GMLConstants.GML_COORDINATES.toLowerCase(),new ParseStrategy(){ 
 
   @Override
public Object parse(Handler arg, GeometryFactory gf) throws SAXException { 
    // one child, either a coord 
    // or a coordinate sequence 
 
    if(arg.text == null || "".equals(arg.text)) 
     throw new SAXException("Cannot create a coordinate sequence without text to parse"); 
     
    int dimension;
    if(arg.attrs.getIndex("dimension")>=0) 
    	dimension = Integer.parseInt(arg.attrs.getValue("dimension")); 
    else 
    	dimension = 2;

    // now to start parse
    String t = arg.text.toString().trim(); 
    // remove tab and line breaks
    t = t.replaceAll("[\\t\\n\\r]+"," "); 
    // then replace all space sequences to one space
    t = t.replaceAll("\\s+"," "); 
     
    StringTokenizer tokenizer = new StringTokenizer(t);
    int nTuples = tokenizer.countTokens() / 2;
 
    CoordinateSequence cs = gf.getCoordinateSequenceFactory().create(nTuples, dimension); 
    
    int ix = 0;
    while (tokenizer.hasMoreTokens())
    {
    	cs.setOrdinate(ix,0,Double.parseDouble(tokenizer.nextToken()));
    	cs.setOrdinate(ix,1,Double.parseDouble(tokenizer.nextToken()));
    	if (dimension == 3)
    		cs.setOrdinate(ix,2,Double.parseDouble(tokenizer.nextToken()));
    	else
    		cs.setOrdinate(ix,2,Double.NaN);
    	
    	ix++;
    }
    
    return cs;
   } 
  }); 
   
  // pos 
  strats.put(GMLConstants.GML_COORD.toLowerCase(),new ParseStrategy(){ 
 
   @Override
public Object parse(Handler arg, GeometryFactory gf) throws SAXException { 
    // x SPACE y and optional SPACE z 
 
    // now to start parse
    String t = arg.text.toString().trim(); 
    // remove tab and line breaks
    t = t.replaceAll("[\\t\\n\\r]+"," "); 
    // then replace all space sequences to one space
    t = t.replaceAll("\\s+"," "); 

    String[] axis = t.split(" ");
    Coordinate c = new Coordinate(); 
    c.x = Double.parseDouble(axis[0]); 
    if(axis.length>1) 
     c.y = Double.parseDouble(axis[1]); 
    if(axis.length>2) 
     c.z = Double.parseDouble(axis[2]); 
     
    return c; 
   } 
  }); 
   
  // radius 
  strats.put(GMLConstants.GML_RADIUS.toLowerCase(),new ParseStrategy(){ 
 
   @Override
public Object parse(Handler arg, GeometryFactory gf) throws SAXException { 
    // x SPACE y and optional SPACE z 
 
    return arg.text.toString().trim(); 
   } 
  }); 
   
  // lowerCorner 
  strats.put(GMLConstants.GML_LOWER_CORNER.toLowerCase(),new ParseStrategy(){ 
 
   @Override
public Object parse(Handler arg, GeometryFactory gf) throws SAXException { 
    // x SPACE y and optional SPACE z 
 
    // now to start parse
    String t = arg.text.toString().trim(); 
    // remove tab and line breaks
    t = t.replaceAll("[\\t\\n\\r]+"," "); 
    // then replace all space sequences to one space
    t = t.replaceAll("\\s+"," "); 

    String[] axis = t.split(" ");
    Coordinate c = new Coordinate(); 
    c.x = Double.parseDouble(axis[0]); 
    if(axis.length>1) 
     c.y = Double.parseDouble(axis[1]); 
    if(axis.length>2) 
     c.z = Double.parseDouble(axis[2]); 
     
    return c; 
   } 
  }); 
   
  // upperCorner 
  strats.put(GMLConstants.GML_UPPER_CORNER.toLowerCase(),new ParseStrategy(){ 
 
   @Override
public Object parse(Handler arg, GeometryFactory gf) throws SAXException { 
    // x SPACE y and optional SPACE z 
 
    // now to start parse
    String t = arg.text.toString().trim(); 
    // remove tab and line breaks
    t = t.replaceAll("[\\t\\n\\r]+"," "); 
    // then replace all space sequences to one space
    t = t.replaceAll("\\s+"," "); 

    String[] axis = t.split(" ");
    Coordinate c = new Coordinate(); 
    c.x = Double.parseDouble(axis[0]); 
    if(axis.length>1) 
     c.y = Double.parseDouble(axis[1]); 
    if(axis.length>2) 
     c.z = Double.parseDouble(axis[2]); 
     
    return c; 
   } 
  }); 
   
  ParseStrategy coord_child = new ParseStrategy(){ 
 
   @Override
public Object parse(Handler arg, GeometryFactory gf) throws SAXException { 
    if(arg.text == null) 
     return null; 
    return new Double((arg.text.toString())); 
   } 
  }; 
      
  ParseStrategy member = new ParseStrategy(){ 
 
   @Override
public Object parse(Handler arg, GeometryFactory gf) throws SAXException { 
    if(arg.children.size()!=1) 
     throw new SAXException("Geometry Members may only contain one geometry."); 
     
    // type checking will occur in the parent geom collection. 
    // may wish to add this in the future 
     
    return arg.children.get(0); 
   } 
  };
  
  // outerBoundary - linear ring member 
  strats.put(GMLConstants.GML_EXTERIOR.toLowerCase(),member); 
   
  // innerBoundary - linear ring member 
  strats.put(GMLConstants.GML_INTERIOR.toLowerCase(),member); 

  // point member 
  strats.put(GMLConstants.GML_POINT_MEMBER.toLowerCase(),member); 
   
  // line string member 
  strats.put(GMLConstants.GML_LINESTRING_MEMBER.toLowerCase(),member); 
   
  // polygon member 
  strats.put(GMLConstants.GML_POLYGON_MEMBER.toLowerCase(),member); 
   
  return strats; 
 } 
  
 static int getSrid(Attributes attrs, int defaultValue){ 
  String srs = null; 
  if(attrs.getIndex(GMLConstants.GML_ATTR_SRSNAME)>=0) 
   srs = attrs.getValue(GMLConstants.GML_ATTR_SRSNAME); 
  else if(attrs.getIndex(GMLConstants.GML_NAMESPACE,GMLConstants.GML_ATTR_SRSNAME)>=0) 
   srs = attrs.getValue(GMLConstants.GML_NAMESPACE,GMLConstants.GML_ATTR_SRSNAME); 
   
  if(srs != null){ 
   srs = srs.trim(); 
   if(srs != null && !"".equals(srs)){ 
    try{ 
     return Integer.parseInt(srs); 
    }catch(NumberFormatException e){ 
     // rip out the end, uri's are used here sometimes 
     int index = srs.lastIndexOf('#'); 
     if(index > -1) 
      srs = srs.substring(index); 
     try{ 
      return Integer.parseInt(srs); 
     }catch(NumberFormatException e2){ 
      // ignore 
     } 
    } 
   } 
  } 
   
  return defaultValue; 
 } 
  
 /**
  * @param uri Not currently used, included for future work 
  * @param localName Used to look up an appropriate parse strategy 
  * @return The ParseStrategy which should be employed 
  *  
  * @see ParseStrategy 
  */ 
 public static ParseStrategy findStrategy(String uri,String localName){ 
  return localName == null?null:(ParseStrategy) strategies.get(localName.toLowerCase()); 
 } 
}