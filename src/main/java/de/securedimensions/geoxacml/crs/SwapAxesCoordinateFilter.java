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

package de.securedimensions.geoxacml.crs;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Andreas Matheus, Secure Dimensions GmbH. 
 *
 */
public class SwapAxesCoordinateFilter implements CoordinateFilter
{
	private static final Logger LOGGER = LoggerFactory.getLogger(SwapAxesCoordinateFilter.class);

	public SwapAxesCoordinateFilter()
	{
		super();
	}
	
	@Override
	public void filter(Coordinate coord) {
		double tmp = coord.y;
		coord.y = coord.x;
		coord.x = tmp;
	}
	
	public boolean isDone() {
		return true;
	}

	public boolean isGeometryChanged() {
		return true;
	}
}
