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

package de.securedimensions.geoxacml.datatype;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeometryMetadata {

	private static final long serialVersionUID = 1L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GeometryMetadata.class);
	
	private String nullReason;
	
	private String srs;
	
	public GeometryMetadata()
	{
		// Only defined if GML Null
		nullReason = null;
		
		// defalt SRS as defined in GeoJSON: https://tools.ietf.org/html/rfc7946#section-4
		srs = GeometryValue.DEFAULT_SRS; 
	}
	
	public GeometryMetadata(String srs)
	{
		this.srs = srs;
	}
	
	public GeometryMetadata(String srs, String nullReason)
	{
		this.srs = srs;
		this.nullReason = nullReason;
	}
	
	public String getSRS()
	{
		return srs;
	}

	public String setSRS(String value)
	{
		return srs = value;
	}

	public String getNullReason()
	{
		return nullReason;
	}
}
