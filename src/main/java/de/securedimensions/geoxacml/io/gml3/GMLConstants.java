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

/**
 * Various constant strings associated with GML3 format.
 * Adapted from the GML2 GMLConstants by Vivid Solutions.
 */
final public class GMLConstants{
	
	  // Namespace constants
	  public static final String GML_NAMESPACE = "http://www.opengis.net/gml/3.2";
	  public static final String GML_PREFIX = "gml";

	  // Source Coordinate System
	  public static final String GML_ATTR_SRSNAME = "srsName";

	  // GML associative types
	  public static final String GML_GEOMETRY_MEMBER = "geometryMember";
	  public static final String GML_POINT_MEMBER = "pointMember";
	  public static final String GML_POLYGON_MEMBER = "polygonMember";
	  public static final String GML_LINESTRING_MEMBER = "lineStringMember";
	  public static final String GML_EXTERIOR = "exterior";
	  public static final String GML_INTERIOR = "interior";

	  // Primitive Geometries
	  public static final String GML_POINT = "Point";
	  public static final String GML_LINESTRING = "LineString";
	  public static final String GML_LINEARRING = "LinearRing";
	  public static final String GML_POLYGON = "Polygon";
	  public static final String GML_ENVELOPE = "Envelope";
	  
	  public static final String GML_CIRCLE_BY_CENTER = "CircleByCenterPoint";
	  public static final String GML_RADIUS = "radius";

	  // Aggregate Ggeometries
	  public static final String GML_MULTI_GEOMETRY = "MultiGeometry";
	  public static final String GML_MULTI_POINT = "MultiPoint";
	  public static final String GML_MULTI_LINESTRING = "MultiLineString";
	  public static final String GML_MULTI_POLYGON = "MultiPolygon";

	  // Coordinates
	  public static final String GML_COORDINATES = "posList";
	  public static final String GML_COORD = "pos";
	  public static final String GML_LOWER_CORNER = "lowerCorner";
	  public static final String GML_UPPER_CORNER = "upperCorner";
}