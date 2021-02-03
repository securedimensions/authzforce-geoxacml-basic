# Build and Install
This GeoXACML Basic implementation can be used as an additional JAR file with the FIWARE AUTHZFORE CE SERVER. The deployment of the `authzforce-geoxacml-basic-<version>.jar` file will enable the functionality available as defined in the [GeoXACML 1.0 BASIC conformance class, Annex A](http://portal.opengeospatial.org/files/?artifact_id=42734). The MAVEN pom.xml compiles for a Java 11 target.

# Build the GeoXACML extension
Clone this repository and run `mvn install`. This generates the `authzforce-geoxacml-basic-0.4.jar` in `target` directory.
Part of the install procedure is also that the dependency libraries are all copied into the `target/lib` directory.

## Installation
This implementation compiles as a JAR file which can be used as an extension to the FIWARE AUTHZFORCE PDP.

### Install the AUTHZFORCE CE SERVER
Please follow the descriptions form the [AUTHZFORCE CE SERVER installation](https://authzforce-ce-fiware.readthedocs.io/en/latest/InstallationAndAdministrationGuide.html) to install the AUTHZFORCE CE SERVER using the `DIST` package. *Note: This GeoXACML implementation was tested successfully with AUTZFORCE CE SERVER version 9.0.1.*

### Deploy the GeoXACML AUTHZFORE PDP extension
Copy from the `target` directory the file `authzforce-geoxacml-basic-<version>.jar` into the FIWARE AUTHZFORCE CE SERVER directory `webapps/WEB-INF/lib`.

Copy from the `target/lib` directory the following files into the FIWARE AUTHZFORCE CE SERVER directory `webapps/WEB-INF/lib`

* jts2geojson-0.14.3.jar
* jts-core-1.18.0.jar
* jts-io-common-1.18.0.jar

Then restart the AUTHZFORCE CE SERVER. E.g. `service tomcat9 restart`.

### Activate the GeoXACML datatype and functions
In order to active the GeoXACML specific datatype and functions, you must update the `pdp.properties` file as described in the [AUTHZFORCE CE SERVER documentation section 5.3.5.6](https://authzforce-ce-fiware.readthedocs.io/en/latest/UserAndProgrammersGuide.html#policy-decision-pdp-properties). You must active the `Geometry` datatype and a set of functions as required. We recommend that you use the following pdpPropertiesUpdate XML as a starting point:


````
<?xml version="1.0" encoding="UTF-8"?>
<pdpPropertiesUpdate xmlns="http://authzforce.github.io/rest-api-model/xmlns/authz/5">
  <feature type="urn:ow2:authzforce:feature-type:pdp:core" enabled="true"
    >urn:ow2:authzforce:feature:pdp:core:xpath-eval</feature>
  <!-- GeoXACML data type -->
  <feature type="urn:ow2:authzforce:feature-type:pdp:data-type" enabled="true"
    >urn:ogc:def:dataType:geoxacml:1.0:geometry</feature>
  <!-- GeoXACML Topological Functions -->
  <feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-equals</feature>
  <feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-disjoint</feature>
  <feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-touches</feature> 
  <feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-crosses</feature> 
  <feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-within</feature>
  <feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-contains</feature>
  <feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-overlaps</feature>
  <feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-intersects</feature>
  <!-- GeoXACML Bag Functions --> 
  <feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-one-and-only</feature>
  <feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-bag-size</feature>
  <feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-is-in</feature>
  <feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-bag</feature>  
  <feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-bag-intersection</feature>
  <feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-bag-at-least-one-member-of</feature>
  <feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-bag-union</feature>
  <feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-bag-subset</feature>
  <!-- GeoXACML Set Functions -->
  <feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-set-equals</feature>
  <!-- PDP default policy that can be updated later -->
   <rootPolicyRefExpression>root</rootPolicyRefExpression>
</pdpPropertiesUpdate>
````

To verify that the GeoXACML processing is accepted, you should see the following result

````
<?xml version="1.0" encoding="UTF-8"?>
<ns3:pdpProperties xmlns="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17"
  xmlns:ns2="http://www.w3.org/2005/Atom"
  xmlns:ns3="http://authzforce.github.io/rest-api-model/xmlns/authz/5"
  xmlns:ns4="http://authzforce.github.io/core/xmlns/pdp/6.0"
  xmlns:ns5="http://authzforce.github.io/pap-dao-flat-file/xmlns/properties/3.6"
  lastModifiedTime="2019-04-04T15:30:21.278Z">
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:core" enabled="true"
    >urn:ow2:authzforce:feature:pdp:core:xpath-eval</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:core" enabled="false"
    >urn:ow2:authzforce:feature:pdp:core:strict-attribute-issuer-match</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:data-type" enabled="true"
    >urn:ogc:def:dataType:geoxacml:1.0:geometry</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-equals</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-disjoint</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-touches</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-crosses</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-within</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-contains</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-overlaps</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-intersects</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-one-and-only</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-bag-size</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-is-in</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-bag</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-bag-intersection</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-bag-at-least-one-member-of</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-bag-union</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-bag-subset</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:function" enabled="true"
    >urn:ogc:def:function:geoxacml:1.0:geometry-set-equals</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:request-preproc" enabled="true"
    >urn:ow2:authzforce:feature:pdp:request-preproc:xacml-xml:default-lax</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:request-preproc" enabled="true"
    >urn:ow2:authzforce:feature:pdp:request-preproc:xacml-json:default-lax</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:request-preproc" enabled="false"
    >urn:ow2:authzforce:feature:pdp:request-preproc:xacml-xml:multiple:repeated-attribute-categories-strict</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:request-preproc" enabled="false"
    >urn:ow2:authzforce:feature:pdp:request-preproc:xacml-xml:multiple:repeated-attribute-categories-lax</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:request-preproc" enabled="false"
    >urn:ow2:authzforce:feature:pdp:request-preproc:xacml-xml:default-strict</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:request-preproc" enabled="false"
    >urn:ow2:authzforce:feature:pdp:request-preproc:xacml-json:default-strict</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:result-postproc" enabled="false"
    >urn:ow2:authzforce:feature:pdp:result-postproc:xacml-xml:default</ns3:feature>
  <ns3:feature type="urn:ow2:authzforce:feature-type:pdp:result-postproc" enabled="false"
    >urn:ow2:authzforce:feature:pdp:result-postproc:xacml-json:default</ns3:feature>
  <ns3:rootPolicyRefExpression>root</ns3:rootPolicyRefExpression>
  <ns3:applicablePolicies>
    <ns3:rootPolicyRef Version="0.1.0">root</ns3:rootPolicyRef>
  </ns3:applicablePolicies>
</ns3:pdpProperties>
```` 
