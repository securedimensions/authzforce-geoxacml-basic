# GeoXACML implementation for FIWARE AuthZForce PDP Community Version
This implementation extends the FIWARE AuthZForce PDP implementation with a `Geometry` data type and related functions as specified in the OGC Implementation Standard [GeoXACML 1.0.1](http://portal.opengeospatial.org/files/?artifact_id=42734). We call a GeoXACML enhanced PDP a GeoPDP.

FIWARE AuthZForce is the project of a comprehensive XACML 3 open source implementation. The implementation is accessible via the project page 
[AuthZForce Project](https://authzforce-ce-fiware.readthedocs.io). Important for this implementation is the ability to implement extensions as enabled via the XACML 3 standard. Of particular interest is the PDP implementation.

[XACML 3](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html) is an OASIS standard.

The implementation can be deployed as an extension to the FIWARE AuthZForce PDP and therefore leverages the XACML 3 syntax for policies, request and response messages. Until the final release of the [GeoXACML 3 Implementation Standard ](https://portal.opengeospatial.org/files/?artifact_id=55231), which currently is a draft OGC standard, this implementation implements the GeoXACML 1.0 datatype and functions from the BASIC Conformance Class based on XACML 3.

The Open Geospatial Consortium [GeoXACML 1.0 standard](http://www.opengeospatial.org/standards/geoxacml) defines two implementation options via a so called `BASIC` and `STANDARD` conformance class.
Secure Dimensions offers support for the functionality outlined in both conformance classes. The implementation of the `BASIC` conformance class is available for free - this implementation.

In case you are interested in the `STANDARD` implementation, please contact us at [support@secure-dimensions.de](mailto:support@secure-dimensions.de).

## About this BASIC implementation
The license for this implementation is Apache 2. The licensing on the dependencies can be reviewed by opening `site/dependencies.html` in your favorite Web Browser.

This implementation supports all GeoXACML functions from the [GeoXACML 1.0 standard, conformance class BASIC as defined in Annex A](http://portal.opengeospatial.org/files/?artifact_id=42734)

This implementation does not undertake geometry transformation. Each function requires that both geometries can be compared. The decision is true iff both geometries are based on (i) the same datum and (ii) the same ellipsoid for geodetic CRSs and on the same CRSID for projected CRSs. 

This implementation uses - in compliance with the [EPSG Registry] (https://www.epsg-registry.org/) - the LAT/LON axes order for the CRS identified by the string "EPSG:4326". In other words, if a geometry encoding references "EPSG:4326" as the CRS, then the axes order on the coordinates SHALL be **Lat/Lon**.
Please see section "Axes Order Confusion" below for more details.

## About the STANDARD implementation
Secure Dimensions offers a full GeoXACML implementation as standardized in [GeoXACML 1.0 standard, conformance class STANDARD as defined in Annex A](http://portal.opengeospatial.org/files/?artifact_id=42734). The implementation does support dynamic geometry transformation to enable the `constructive` and `geometric` functions.

The STANDARD implementation enables the following functions on the dataype `Geometry`:

|Function URN|
|:-|
|urn:ogc:def:function:geoxacml:1.0:geometry-equals|
|urn:ogc:def:function:geoxacml:1.0:geometry-disjoint|
|urn:ogc:def:function:geoxacml:1.0:geometry-touches|
|urn:ogc:def:function:geoxacml:1.0:geometry-crosses|
|urn:ogc:def:function:geoxacml:1.0:geometry-within|
|urn:ogc:def:function:geoxacml:1.0:geometry-contains|
|urn:ogc:def:function:geoxacml:1.0:geometry-overlaps|
|urn:ogc:def:function:geoxacml:1.0:geometry-intersects|
|urn:ogc:def:function:geoxacml:1.0:geometry-one-and-only|
|urn:ogc:def:function:geoxacml:1.0:geometry-bag-size|
|urn:ogc:def:function:geoxacml:1.0:geometry-is-in|
|urn:ogc:def:function:geoxacml:1.0:geometry-bag|
|urn:ogc:def:function:geoxacml:1.0:geometry-bag-intersection|
|urn:ogc:def:function:geoxacml:1.0:geometry-bag-at-least-one-member-of|
|urn:ogc:def:function:geoxacml:1.0:geometry-bag-union|
|urn:ogc:def:function:geoxacml:1.0:geometry-bag-subset|
|urn:ogc:def:function:geoxacml:1.0:geometry-set-equals|
|urn:ogc:def:function:geoxacml:1.0:geometry-buffer|
|urn:ogc:def:function:geoxacml:1.0:geometry-boundary|
|urn:ogc:def:function:geoxacml:1.0:geometry-convex-hull|
|urn:ogc:def:function:geoxacml:1.0:geometry-centroid|
|urn:ogc:def:function:geoxacml:1.0:geometry-difference|
|urn:ogc:def:function:geoxacml:1.0:geometry-sym-difference|
|urn:ogc:def:function:geoxacml:1.0:geometry-intersection|
|urn:ogc:def:function:geoxacml:1.0:geometry-union|
|urn:ogc:def:function:geoxacml:1.0:geometry-area|
|urn:ogc:def:function:geoxacml:1.0:geometry-distance|
|urn:ogc:def:function:geoxacml:1.0:geometry-is-within-distance|
|urn:ogc:def:function:geoxacml:1.0:geometry-length|
|urn:ogc:def:function:geoxacml:1.0:geometry-is-simple|
|urn:ogc:def:function:geoxacml:1.0:geometry-is-closed|
|urn:ogc:def:function:geoxacml:1.0:geometry-is-valid|
|urn:ogc:def:function:geoxacml:1.0:convert-to-metre|
|urn:ogc:def:function:geoxacml:1.0:convert-to-square-metre|



## Axes Order Confusion
This implementation must be able to derive access control decisions based on geometries encoded by potentially different people and organizations. Usually, a policy editor would create geometry based conditions using the WKT (Well Known Text) or EWKT (Extended Well Known Text) encodings. Then during runtime, a PEP (Policy Enforcement Point) - or any other software - would send Authorization Decision Requests to the GeoPDP. If at this point, the editor of the policy and the implementer of the PEP had used different axes order for the same CRS identifier, then the PDP consequently will derive a wrong access control decision! Important to note is that the GeoPDP has no option to determine the different axes order use.

Therefore, this implementation applies the axes order as defined below:

| CRS definition                        | SRID | axes order | 
|:-------------------------------------:|:----:|:----------:|
| n/a (default for WKT)                 | 4326 | LON/LAT    | 
| urn:ogc:def:crs:OGC::CRS84            | 4326 | LON/LAT    |
| CRS84                                 | 4326 | LON/LAT    |
| WGS84                                 | 4326 | LON/LAT    |
| EPSG:4326                             | 4326 | LAT/LON    |
| urn:ogc:def:crs:OGC::EPSG:4326        | 4326 | LAT/LON    |
| http://www.opengis.net/def/crs/EPSG/0/4326       | 4326 | LAT/LON    |

*GeoXACML implementation and axes order for CRS identifiers*

This implementation uses `urn:ogc:def:crs:OGC::EPSG:<code>` and `http://www.opengis.net/def/crs/EPSG/0/<code>` as aliases for `EPSG:<code>`.

**This implies that using `SRID=4326` with EWKT results in the axes order LAT/LON!**

| Example encoding for point location of Washington Monument located at LAT=38.889444 and LON=-77.035278| encoding | axes order |
|:-|:-:|:-:|
| `{"type": "Point", "coordinates": [-77.035278, 38.889444]}` | GeoJSON | LON/LAT |
| `POINT(-77.035278 38.889444)` and `CRS=WGS84`  | WKT | LON/LAT |
| `POINT(38.889444 -77.035278)` and `CRS=EPSG:4326`  | WKT | LAT/LON |
| `SRID=4326;POINT(38.889444 -77.035278)`        | EWKT | LAT/LON |
| `CRS=EPSG:4326;POINT(38.889444 -77.035278)`    | EWKT | LAT/LON |
| `CRS=urn:ogc:def:crs:OGC:EPSG:4326;POINT(38.889444 -77.035278)`    | EWKT | LAT/LON |
| `CRS=urn:ogc:def:crs:OGC:CRS84;POINT(-77.035278 38.889444)`    | EWKT | LON/LAT |
| `CRS=WGS84;POINT(-77.035278 38.889444)`    | EWKT | LON/LAT |

*WKT and EWKT encoding examples with resulting axes order*


## GeoXACML 1.0 and 3.0
GeoXACML 3.0 is an OGC draft standard defining an extension to the OASIS XACML 3 standard that leverages the data type `Geometry` and geometry specific functions based on the GeoXACML 1.0 definition. The geometry model in GeoXACML 1.0 is the one defined in ISO 19107 (identical to OGC Simple Features).
In a nutshell, all geometry types are based on linear interpolation between points. Therefore, no native support for Arcs, Curves and Circle exist.

### Geometry data type
Geometry data type as defined in the GeoXACML 1.0 standard. Therefore, the implementation accepts the following URN to specify a 
XACML 3 AttributeValue of type geometry:

````
urn:ogc:def:dataType:geoxacml:1.0:geometry
````

### Geometry based functions
The GeoXACML 1.0 standard defines the following functions that are implemented:

|Function URN|
|:-|
|urn:ogc:def:function:geoxacml:1.0:geometry-equals|
|urn:ogc:def:function:geoxacml:1.0:geometry-disjoint|
|urn:ogc:def:function:geoxacml:1.0:geometry-touches|
|urn:ogc:def:function:geoxacml:1.0:geometry-crosses|
|urn:ogc:def:function:geoxacml:1.0:geometry-within|
|urn:ogc:def:function:geoxacml:1.0:geometry-contains|
|urn:ogc:def:function:geoxacml:1.0:geometry-overlaps|
|urn:ogc:def:function:geoxacml:1.0:geometry-intersects|
|urn:ogc:def:function:geoxacml:1.0:geometry-one-and-only|
|urn:ogc:def:function:geoxacml:1.0:geometry-bag-size|
|urn:ogc:def:function:geoxacml:1.0:geometry-is-in|
|urn:ogc:def:function:geoxacml:1.0:geometry-bag|
|urn:ogc:def:function:geoxacml:1.0:geometry-bag-intersection|
|urn:ogc:def:function:geoxacml:1.0:geometry-bag-at-least-one-member-of|
|urn:ogc:def:function:geoxacml:1.0:geometry-bag-union|
|urn:ogc:def:function:geoxacml:1.0:geometry-bag-subset|
|urn:ogc:def:function:geoxacml:1.0:geometry-set-equals|

The functions on bags and sets inherited from XACML do **not** use the `urn:ogc:def:function:geoxacml:1.0:geometry-equals` function, which means topologically equals. Instead, the equals function used for bag and set functions uses **exact** equality which means that each coordinate of the geometries must be identical order and value.

### Geometry encoding introduction
The different kinds of geometry encoding result from the supported encodings of XACML 3. XACML 3 supports XML encoding for policy 
Authorization Decision Request (ADR) and Authorization Decision (AD). In addition, the XACML 3 JSON profile supports JSON style
for ADR and AD.

Therefore, this implementation supports the following different `Geometry` encoding options:

1. XML based geometry encoding via GML2 and GML3 but also String based encodings via WKT or EWKT (Because XACML policies are encoded in XML, this encoding is the only option) 
1. JSON based geometry encoding via WKT, EWKT and GeoJSON (These options are available for JSON based ADR and AD).

It is important to emphasize that the most flexible encoding is GML but that requires the ADR/AD to be in XML encoding.
In environments where JSON alike encoding is preferred (for performance or easier processing), some restrictions and conventions to encode a geometry exist.

**Some notes on WKT, EWKT and GeoJSON encoding**

1. This implementation uses the WKT definition from [OpenGIS Implementation Specification for Geographic information - Simple feature access - Part 1: Common architecture ] (http://portal.opengeospatial.org/files/?artifact_id=25355). Example: `POINT(0 0)`
1. The use of Extended WKT (EWKT) is based on two options: 

(i) [PostGIS definition for EWKT] (https://postgis.net/docs/using_postgis_dbmanagement.html): The string _SRID=<CRS number>;_ is prefixing the WKT. Example `SRID=32632;POINT(0 0)`. 

(ii) It is possible to also use the CRS string identifier as a prefix. Example `CRS=WGS84;POINT(0 0)` 
1. The GeoJSON geometry encoding support is based on [IETF RFC 7946, section 4](https://tools.ietf.org/html/rfc7946#section-4). Example: `{"type": "Point", "coordinates": [0 0]}`

This implementation supports the following WKT (and EWKT) geometry representations:

* POINT
* MULTILIPOINT
* LINESTRING
* MULTILINESTRING
* LINEARRING
* POLYGON
* GEOMETRYCOLLECTION

This implementation also supports the WKT / EWKT extension:

* EMPTY


### XML based Geometry encoding
XML based geometry encodings can be used when the ADR (and therefore the AD) is in XML encoding. Basically, two different options can be used:
1) Simple XACML AttributeValue: The content of the XACML AttributeValue is a String (WKT, EWT)
2) Structured XACML AttributeValue: The content of the XACML AttributeValue is an W3C Element Node (GML2, GML3)

#### Geometry encoding using a simple AttributeValue
The restriction is that the content of the AttributeValue must a String. GeoXACML 1.0 does **not** only supports WKT and EWKT. However, this implementation does. Official support will be created with GeoXACML 3.

##### WKT based geometry encoding
When using the WKT geometry encoding, there is no CRS defined. In order to be compatible with GeoJSON, this implementation uses the GeoJSON 
Coordinate Reference System and axes order as the default: `SRID=4326` / `CRS=urn:ogc:def:crs:OGC::CRS84` with axes order LON/LAT

````
<Attribute IncludeInResult="true" AttributeId="urn:SD:washingtonMonument">
    <AttributeValue DataType="urn:ogc:def:dataType:geoxacml:1.0:geometry"
        >POINT(-77.035278 38.889444)</AttributeValue>
</Attribute>
````

*Example: WKT based geometry using XACML 3 XML encoding with **default** CRS (urn:ogc:def:crs:OGC::CRS84)*

But, it is possible to provide a CRS identifier or a SRID with the XACML AttributeValue. The CRS and SRID must be encoded as a GeoXACML specific attribute 
of the XACML 3 AttributeValue element with name _crs_ for CRS or name _srid_ for the SRID to represent the integer number. Both attributes must use 
the namespace `http://www.opengis.net/geoxacml` 

The following example illustrates a WKT based point encoding using the optional _crs_ attribute.

```` 
<Attribute IncludeInResult="true" AttributeId="urn:SD:washingtonMonument">
    <AttributeValue xmlns:geoxacml="http://www.opengis.net/geoxacml"
        DataType="urn:ogc:def:dataType:geoxacml:1.0:geometry"
        geoxacml:crs="EPSG:4326"
        >POINT(38.889444 -77.035278)</AttributeValue>
</Attribute>
````

*Example: WKT based geometry using XACML 3 XML encoding with crs definition*

The following example illustrates a WKT based point encoding using the optional _srid_ attribute.

````
<Attribute IncludeInResult="true" AttributeId="urn:SD:washingtonMonument">
    <AttributeValue xmlns:geoxacml="http://www.opengis.net/geoxacml"
        DataType="urn:ogc:def:dataType:geoxacml:1.0:geometry"
        geoxacml:srid="4326"
        >POINT(-77.035278 38.889444)</AttributeValue>
</Attribute>
````

*Example: WKT based geometry using XACML 3 XML encoding with srid definition*

##### EWKT based geometry encoding
When using the EWKT geometry encoding, the CRS or SRID must be prefixed to the WKT as described in the WKT geometry encoding.

The following example illustrates an EWKT based point encoding using the optional _crs_ attribute.

````
<Attribute IncludeInResult="true" AttributeId="urn:SD:washingtonMonument">
    <AttributeValue DataType="urn:ogc:def:dataType:geoxacml:1.0:geometry"
        >CRS=EPSG:4326;POINT(38.889444 -77.035278)</AttributeValue>
</Attribute>
````

*Example: EWKT based geometry using XACML 3 XML encoding with crs definition*

The following example illustrates an EWKT based point encoding using the optional _srid_ attribute.

````
<Attribute IncludeInResult="true" AttributeId="urn:SD:washingtonMonument">
    <AttributeValue DataType="urn:ogc:def:dataType:geoxacml:1.0:geometry"
        >SRID=4326;POINT(-77.035278 38.889444)</AttributeValue>
</Attribute>
````

*Example: EWKT based geometry using XACML 3 XML encoding with srid definition*


#### Geometry encoding using a structured AttributeValue
This is the most flexible encoding of a geometry. The enclosed W3C element must either be a GML2 or GML3 encoded geometry. 

##### GML 2.1.2
Even though GML2 is deprecated, this implementation supports GML2 geometry encoding.
Example:

````
<Attribute IncludeInResult="true" AttributeId="urn:SD:washingtonMonument">
    <AttributeValue DataType="urn:ogc:def:dataType:geoxacml:1.0:geometry">
        <gml:Point xmlns:gml="http://www.opengis.net/gml" gml:id="WashingtonMonument" srsName="EPSG:4326">
            <gml:coord srsDimension="2"><gml:X>38.889444</gml:X><gml:Y>-77.035278</gml:Y></gml:coord>
        </gml:Point>
    </AttributeValue>
</Attribute>
````

*Example: XACML 3 structured Attribute using a GML2 geometry encoding*

##### GML 3.3
GML3 is currently the defacto XML encoding for geometries. 

````
<Attribute IncludeInResult="true" AttributeId="urn:SD:washingtonMonument">
    <AttributeValue DataType="urn:ogc:def:dataType:geoxacml:1.0:geometry">
        <gml:Point xmlns:gml="http://www.opengis.net/gml/3.2" gml:id="WashingtonMonument" srsName="EPSG:4326">
            <gml:pos srsDimension="2">38.889444 -77.035278</gml:pos>
        </gml:Point>
    </AttributeValue>
</Attribute>
````

*Example: XACML 3 strucuted Attribute using a GML3 geometry encoding*

#### Geometry encoding in JSON based ADR and AD
When using the JSON encoding for the Authorization Decision Request, the response from the PDP will also use the JSON encoding.
The use of this encoding simplifies the structure of the ADR (and the AD) to fit best for simple use cases and Web-based scenarios.

The implication is that less options exist for encoding a geometry. Basically, a geoemtry must be encoded as a string using WKT, EWKT or GeoJSON.

##### WKT
The use of WKT based encoding with JSON based ADR does **not** support the specification of a CRS. In order to support this simple encoding and to
increase the compatebility with GeoJSON encoded geometries, this implementation associates the GeoJSON CRS and axis order for the WKT representation.

````
"Attribute": [
          {
            "IncludeInResult": true,
            "AttributeId": "urn:SD:washingtonMonument",
            "DataType": "urn:ogc:def:dataType:geoxacml:1.0:geometry",
            "Value": ["POINT(-77.035278, 38.889444)"]
          }
        ]
````

*Example: XACML 3 JSON encoded Attribute of data type geometry using WKT representation* 

##### GeoJSON
Any geometry represented as GeoJSON has a pre-defined CRS and axes order: [WGS 84](http://spatialreference.org/ref/epsg/wgs-84/) with Longitude / Latitude.

This implementation leverages the exact same axis order as defined in GeoJSON section 4 (Coordinate Reference System): "`World Geodetic System 1984 (WGS 84) [WGS84] 
datum, with longitude and latitude units of decimal degrees.`"[IETF RFC 7946] (https://tools.ietf.org/html/rfc7946).

````
"Attribute": [
          {
            "IncludeInResult": true,
            "AttributeId": "urn:SD:washingtonMonument",
            "DataType": "urn:ogc:def:dataType:geoxacml:1.0:geometry",
            "Value": ["{\n\"type\": \"Point\",\n\"coordinates\": [-77.035278, 38.889444]\n}"]
          }
        ]
````
 
*Example: XACML 3 JSON encoded Attribute od data type geometry using GeoJSON representation*


##### EWKT
The use of EWKT allows to specify the CRS associated with the geometry that is represented as WKT. This implementation supports the simple - community practise -
which prefixes the string *SRID=<integer number>;* before the WKT. But it also supports the full string identifier prefixed to the WKT: *CRS=<string identifier>;*.

````
"Attribute": [
          {
            "IncludeInResult": true,
            "AttributeId": "urn:SD:washingtonMonument",
            "DataType": "urn:ogc:def:dataType:geoxacml:1.0:geometry",
            "Value": ["SRID=4326;POINT(-77.035278 38.889444)"]
          }
        ]
````
 
*Example: Extended WKT using SRID to specify the CRS **Note: The axes order is Lon/Lat***

````
"Attribute": [
          {
            "IncludeInResult": true,
            "AttributeId": "urn:SD:washingtonMonument",
            "DataType": "urn:ogc:def:dataType:geoxacml:1.0:geometry",
            "Value": ["CRS=EPSG:4326;POINT(38.889444 -77.035278)"]
          }
        ]
````

*Example: Extended WKT using CRS to specify the CRS **Note: The axes order is Lat/Lon***


## Null Geometry support
**Important: The Simple Features specification - which is the normative reference for the GeoXACML 1.0 geometry model - does not support geometry type Null.**

But for access control use cases, support for Null geometry is essential. A Null geometry could for example indicate that for a particular attribute of type 
geometry no location data was available/known/not computable/etc.

Therefore, this implementation leverages the semantics of a Null Geometry from GML and implements a Null geometry by constructing an empty Geometry via the JTS library support.
Any GeoXACML operation that involves a Null geometry leverages the JTS library semantics as the OGC Simple Features specification does not define the semantics of a Null geometry.

This implementation also supports a simplified representation of a Null geometry using the WKT "EMPTY" approach. 

**Important: The implementation does not store a reason for null when using the WKT encoding as this is cannot be specified**

|Function URN| Result if one o both geometries are NULL or EMPTY|
|:-|:-|
|urn:ogc:def:function:geoxacml:1.0:geometry-equals| FALSE |
|urn:ogc:def:function:geoxacml:1.0:geometry-disjoint| TRUE |
|urn:ogc:def:function:geoxacml:1.0:geometry-touches| FALSE |
|urn:ogc:def:function:geoxacml:1.0:geometry-crosses| FALSE |
|urn:ogc:def:function:geoxacml:1.0:geometry-within| FALSE |
|urn:ogc:def:function:geoxacml:1.0:geometry-contains| FALSE |
|urn:ogc:def:function:geoxacml:1.0:geometry-overlaps| FALSE |
|urn:ogc:def:function:geoxacml:1.0:geometry-intersects| FALSE |
|urn:ogc:def:function:geoxacml:1.0:geometry-one-and-only| FALSE |
|urn:ogc:def:function:geoxacml:1.0:geometry-bag-size| FALSE |
|urn:ogc:def:function:geoxacml:1.0:geometry-is-in| FALSE |
|urn:ogc:def:function:geoxacml:1.0:geometry-bag| FALSE |
|urn:ogc:def:function:geoxacml:1.0:geometry-bag-intersection| FALSE |
|urn:ogc:def:function:geoxacml:1.0:geometry-bag-at-least-one-member-of| FALSE |
|urn:ogc:def:function:geoxacml:1.0:geometry-bag-union| FALSE |
|urn:ogc:def:function:geoxacml:1.0:geometry-bag-subset| FALSE |
|urn:ogc:def:function:geoxacml:1.0:geometry-set-equals| FALSE |


### WKT encoding for Null geometry
The WKT encoding for a Null geometry is supported via the EMPTY declaration after the geometry type declaration. 

````
<Attribute IncludeInResult="true" AttributeId="urn:SD:washingtonMonument">
    <AttributeValue DataType="urn:ogc:def:dataType:geoxacml:1.0:geometry"
        >POINT EMPTY</AttributeValue>
</Attribute>
````

*Example: Simple XACML 3 Attribute with WKT based Null geometry representation*

````
"Attribute": [
          {
            "IncludeInResult": true,
            "AttributeId": "urn:SD:washingtonMonument",
            "DataType": "urn:ogc:def:dataType:geoxacml:1.0:geometry",
            "Value": ["POINT EMPTY"]
          }
        ]
````

*Example: XACML 3 JSON encoded Attribute of data type geometry using WKT Null representation* 

### EWKT
There is no support using EWKT because it does not make sense to specify a CRS

### NULL support via string representation
In order to support equal expressiveness with String encoded geometries - and to be able to compare Null reasons with geometries encoded as GML Null - this
implementation also supports the following string structure and leveraging the GML nullReasonType:

-	_inapplicable_ there is no value
-	_missing_ the correct value is not readily available to the sender of this data. Furthermore, a correct value may not exist
-	_template_ the value will be available later
-	_unknown_ the correct value is not known to, and not computable by, the sender of this data. However, a correct value probably exists
-	_withheld_ the value is not divulged
-	_other:text_ other brief explanation, where text is a string of two or more characters with no included spaces

````
NULL {inapplicable, missing, template, unknown, withheld, other:text}
````

*GeoXACML specific support for Null geometry representation using a string*

````
NULL other:ThisExampleIsFictitious
````

*Example: Implementation specific support for GML alike Null geometry using a string representation*

````
<Attribute IncludeInResult="true" AttributeId="urn:SD:washingtonMonument">
    <AttributeValue DataType="urn:ogc:def:dataType:geoxacml:1.0:geometry"
        >NULL other:ThisExampleIsFictitious</AttributeValue>
</Attribute>
````

*Example: Simple XACML 3 Attribute with GeoXACML based Null geometry representation*

````
"Attribute": [
          {
            "IncludeInResult": true,
            "AttributeId": "urn:SD:washingtonMonument",
            "DataType": "urn:ogc:def:dataType:geoxacml:1.0:geometry",
            "Value": ["NULL other:ThisExampleIsFictitious"]
          }
        ]
````

*Example: XACML 3 JSON encoded Attribute with GeoXACML based Null geometry representation* 



### GML2 encoding for Null geometry

````
<Attribute IncludeInResult="true" AttributeId="urn:SD:washingtonMonument">
    <AttributeValue DataType="urn:ogc:def:dataType:geoxacml:1.0:geometry">
        <gml:Null xmlns:gml="http://www.opengis.net/gml" gml:id="WashingtonMonument">unknown</gml:Null>
    </AttributeValue>
</Attribute>
````

*Example: Structured XACML 3 Attribute using the GML2 Null encoding*

### GML3 encoding for Null geometry

````
<Attribute IncludeInResult="true" AttributeId="urn:SD:washingtonMonument">
    <AttributeValue DataType="urn:ogc:def:dataType:geoxacml:1.0:geometry">
        <gml:Null xmlns:gml="http://www.opengis.net/gml/3.2" gml:id="WashingtonMonument">unknown</gml:Null>
    </AttributeValue>
</Attribute>
````

*Example: Structured XACML 3 Attribute using the GML3 Null encoding*
