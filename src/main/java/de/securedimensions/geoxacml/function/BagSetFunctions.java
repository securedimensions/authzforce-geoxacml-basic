/**
 * Copyright 2019 Secure Dimensions GmbH.
 *
 * This file is part of GeoXACML 3 Community Version.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.securedimensions.geoxacml.function;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderBagFunctions;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.value.AttributeDatatype;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.BagDatatype;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.BaseFirstOrderFunctionCall.EagerBagEval;
import org.ow2.authzforce.core.pdp.api.func.BaseFirstOrderFunctionCall.EagerPartlyBagEval;
import org.ow2.authzforce.core.pdp.api.func.BaseFirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.Value;

import com.google.common.collect.Sets;

import de.securedimensions.geoxacml.datatype.GeometryValue;

/**
 *
 * @author Andreas Matheus, Secure Dimensions GmbH. 
 *
 */

public class BagSetFunctions
{

    private static final Logger LOGGER = LoggerFactory.getLogger(GeometryValue.class);

    public static final AttributeDatatype<GeometryValue> DATATYPEX =
            new AttributeDatatype<GeometryValue>(GeometryValue.class, "urn:ogc:def:dataType:geoxacml:1.0:geometry", "urn:ogc:def:function:geoxacml:1.0:geometry-bag");

    final static AttributeDatatype<GeometryValue> paramType = GeometryValue.FACTORY.getDatatype();
    final static BagDatatype<GeometryValue> paramBagType = paramType.getBagDatatype();
    final static Class<GeometryValue[]> paramArrayClass = paramType.getArrayClass();

    // For use in Intersection and Union
    final static BagDatatype<GeometryValue> paramBagTypeX = DATATYPEX.getBagDatatype();
    final static Class<GeometryValue[]> paramArrayClassX = DATATYPEX.getArrayClass();


    public static class GeometryFromBag extends SingleParameterTypedFirstOrderFunction<GeometryValue, Bag<GeometryValue>>
    {
        /**
         * Function ID for 'urn:ogc:def:function:geoxacml:3.0:geometry-from-bag' function
         */
        public static final String ID = "urn:ogc:def:function:geoxacml:3.0:geometry-from-bag";

        /**
         * Constructor
         *
         */
        public GeometryFromBag()
        {
            super(ID, GeometryValue.DATATYPE, true, Collections.singletonList(GeometryValue.DATATYPE.getBagDatatype()));
        }

        @Override
        public FirstOrderFunctionCall<GeometryValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes) throws IllegalArgumentException
        {
            return new EagerBagEval<>(functionSignature, argExpressions)
            {

                @Override
                protected final GeometryValue evaluate(final Bag<GeometryValue>[] bagArgs) throws IndeterminateEvaluationException
                {
                    ArrayList<Geometry> geometries = new ArrayList<>(bagArgs[0].size());

                    Iterator<GeometryValue> i = bagArgs[0].iterator();

					while (i.hasNext())
					{
						geometries.add(i.next().getUnderlyingValue());
					}

                    return GeometryValue.FACTORY.getInstance(geometries);
                }
            };
        }

    }

    public static class SingletonBagToPrimitive extends FirstOrderBagFunctions.SingletonBagToPrimitive<GeometryValue>
    {

        /**
         * Function identifier
         * <li>{@code -one-and-only}: converts a given singleton bag to a the single primitive value in the bag</li>
         * public static final String ID = "urn:ogc:def:function:geoxacml:1.0:geometry-one-and-only";
         */

        public SingletonBagToPrimitive()
        {
            super(paramType, paramBagType);
        }
    }

    public static class BagSize extends FirstOrderBagFunctions.BagSize<GeometryValue>
    {
        /**
         * Function identifier
         * public static final String ID = "urn:ogc:def:function:geoxacml:1.0:geometry-bag-size";
         * <li>{@code -bag-size}: gives the size of a given bag</li>
         */

        public BagSize()
        {
            super(paramBagType);
        }

    }

    public static class BagContains extends FirstOrderBagFunctions.BagContains<GeometryValue>
    {
        /**
         * Function identifier
         * public static final String ID = "urn:ogc:def:function:geoxacml:1.0:geometry-is-in";
         * <li>{@code -is-in}: tests whether the bag contains a given primitive value</li>
         */

        public BagContains()
        {
            super(paramType, paramBagType, paramArrayClass);
        }

        public static <V extends AttributeValue> boolean eval(final V arg0, final Bag<V> bag)
        {
            LOGGER.debug("eval function for Geometry datatype");
            return bag.contains(arg0);
        }

    }

    public static class PrimitiveToBag extends FirstOrderBagFunctions.PrimitiveToBag<GeometryValue>
    {
        /**
         * Function identifier
         * public static final String ID = "urn:ogc:def:function:geoxacml:1.0:geometry-bag";
         * <li>{@code -bag}: creates a singleton bag from a given primitive value</li>
         */

        public PrimitiveToBag()
        {
            super(paramType, paramBagType);
        }

    }


    public static class AtLeastOneMemberOf extends FirstOrderBagFunctions.AtLeastOneMemberOf<GeometryValue>
    {
        /**
         * Function identifier
         * public static final String ID = "urn:ogc:def:function:geoxacml:1.0:geometry-at-least-one-member-of";
         * <li>{@code -at-least-one-member-of}: tests whether one of the values in a given bag is in another given bag</li>
         */

        public AtLeastOneMemberOf()
        {
            super(paramBagTypeX);
        }

    }

    public static class Intersection extends FirstOrderBagFunctions.Intersection<GeometryValue>
    {
        /**
         * Function identifier
         * public static final String ID = "urn:ogc:def:function:geoxacml:1.0:geometry-set-equals";
         * <li>{@code -set-equals}: tests whether bags are equal regardless of order</li>
         */

        public Intersection()
        {
            super(DATATYPEX, paramBagTypeX);
        }

    }

    public static class Union extends FirstOrderBagFunctions.Union<GeometryValue>
    {
        /**
         * Function identifier
         * public static final String ID = "urn:ogc:def:function:geoxacml:1.0:geometry-set-equals";
         * <li>{@code -set-equals}: tests whether bags are equal regardless of order</li>
         */

        public Union()
        {
            super(DATATYPEX, paramBagTypeX);
        }

    }

    public static class Subset extends FirstOrderBagFunctions.Subset<GeometryValue>
    {
        /**
         * Function identifier
         * public static final String ID = "urn:ogc:def:function:geoxacml:1.0:geometry-subset";
         * <li>{@code -subset}: tests whether all values of a given bag are in another given bag</li>
         */

        public Subset()
        {
            super(paramBagTypeX);
        }

    }

    public static class SetEquals extends FirstOrderBagFunctions.SetEquals<GeometryValue>
    {
        /**
         * Function identifier
         * public static final String ID = "urn:ogc:def:function:geoxacml:1.0:geometry-set-equals";
         * <li>{@code -set-equals}: tests whether bags are equal regardless of order</li>
         */

        public SetEquals()
        {
            super(paramBagType);
        }

    }

}
