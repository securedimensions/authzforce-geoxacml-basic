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

package de.securedimensions.geoxacml.function;

import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall;
import org.ow2.authzforce.core.pdp.api.func.SingleParameterTypedFirstOrderFunction;
import org.ow2.authzforce.core.pdp.api.func.BaseFirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

import de.securedimensions.geoxacml.datatype.GeometryMetadata;
import de.securedimensions.geoxacml.datatype.GeometryValue;

public class GeometryFunctions {
	
	protected static Geometry checkArgument(final Object arg, final String ID) throws IndeterminateEvaluationException
	{
		
		if ((arg != null) && (arg instanceof GeometryValue))
			return ((GeometryValue)arg).getUnderlyingValue();
		else
			throw new IndeterminateEvaluationException("Funtcion " + ID + " expects Geometry datatype as first argument but given " + arg.getClass().getName(), XacmlStatusCode.PROCESSING_ERROR.name());

	}

	public final static class GeometryFromString extends SingleParameterTypedFirstOrderFunction<GeometryValue, StringValue>
	{
		public static final String ID = "urn:ogc:def:function:geoxacml:3.0:geometry-from-string";

		public GeometryFromString()
		{
			super(ID, GeometryValue.DATATYPE, true, Arrays.asList(StandardDatatypes.STRING));
		}

		@Override
		public FirstOrderFunctionCall<GeometryValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes)
		{

			return new EagerSinglePrimitiveTypeEval<GeometryValue, StringValue>(functionSignature, argExpressions, remainingArgTypes)
			{

				@Override
				protected GeometryValue evaluate(final Deque<StringValue> args) throws IndeterminateEvaluationException
				{
					return GeometryValue.FACTORY.getInstance(args.poll().getContent(), null, null);
				}

			};
		}
	}

	public final static class IsSimple extends SingleParameterTypedFirstOrderFunction<BooleanValue, GeometryValue>
	{
		public static final String ID = "urn:ogc:def:function:geoxacml:1.0:geometry-is-simple";

		public IsSimple()
		{
			super(ID, StandardDatatypes.BOOLEAN, true, Arrays.asList(GeometryValue.DATATYPE));
		}

		@Override
		public FirstOrderFunctionCall<BooleanValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes)
		{

			return new EagerSinglePrimitiveTypeEval<BooleanValue, GeometryValue>(functionSignature, argExpressions, remainingArgTypes)
			{

				@Override
				protected BooleanValue evaluate(final Deque<GeometryValue> args) throws IndeterminateEvaluationException
				{
					return new BooleanValue(args.poll().getUnderlyingValue().isSimple());
				}

			};
		}
	}

	public final static class IsValid extends SingleParameterTypedFirstOrderFunction<BooleanValue, GeometryValue>
	{
		public static final String ID = "urn:ogc:def:function:geoxacml:1.0:geometry-is-valid";

		public IsValid()
		{
			super(ID, StandardDatatypes.BOOLEAN, true, Arrays.asList(GeometryValue.DATATYPE));
		}

		@Override
		public FirstOrderFunctionCall<BooleanValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes)
		{

			return new EagerSinglePrimitiveTypeEval<BooleanValue, GeometryValue>(functionSignature, argExpressions, remainingArgTypes)
			{

				@Override
				protected BooleanValue evaluate(final Deque<GeometryValue> args) throws IndeterminateEvaluationException
				{
					return new BooleanValue(args.poll().getUnderlyingValue().isValid());
				}

			};
		}
	}

	public final static class IsEmpty extends SingleParameterTypedFirstOrderFunction<BooleanValue, GeometryValue>
	{
		public static final String ID = "urn:ogc:def:function:geoxacml:1.0:geometry-is-empty";

		public IsEmpty()
		{
			super(ID, StandardDatatypes.BOOLEAN, true, Arrays.asList(GeometryValue.DATATYPE));
		}

		@Override
		public FirstOrderFunctionCall<BooleanValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes)
		{

			return new EagerSinglePrimitiveTypeEval<BooleanValue, GeometryValue>(functionSignature, argExpressions, remainingArgTypes)
			{

				@Override
				protected BooleanValue evaluate(final Deque<GeometryValue> args) throws IndeterminateEvaluationException
				{
					return new BooleanValue(args.poll().getUnderlyingValue().isEmpty());
				}

			};
		}
	}

	public final static class IsClosed extends SingleParameterTypedFirstOrderFunction<BooleanValue, GeometryValue>
	{
		public static final String ID = "urn:ogc:def:function:geoxacml:1.0:geometry-is-closed";

		public IsClosed()
		{
			super(ID, StandardDatatypes.BOOLEAN, true, Arrays.asList(GeometryValue.DATATYPE));
		}

		@Override
		public FirstOrderFunctionCall<BooleanValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes)
		{

			return new EagerSinglePrimitiveTypeEval<BooleanValue, GeometryValue>(functionSignature, argExpressions, remainingArgTypes)
			{

				@Override
				protected BooleanValue evaluate(final Deque<GeometryValue> args) throws IndeterminateEvaluationException
				{
					final Geometry g = args.poll().getUnderlyingValue();

					if (g instanceof GeometryCollection)
					{
						for (int ix = 0; ix < g.getNumGeometries(); ix++)
						{
							Geometry gx = g.getGeometryN(ix);
								if (gx.getCoordinates()[0] != gx.getCoordinates()[gx.getNumPoints()-1])
									return BooleanValue.FALSE;
						}
						return BooleanValue.TRUE;
					}
					else
					{
						return new BooleanValue(g.getCoordinates()[0] == g.getCoordinates()[g.getNumPoints()-1]);
					}
					
				}

			};
		}
	}

	public final static class IsRectangle extends SingleParameterTypedFirstOrderFunction<BooleanValue, GeometryValue>
	{
		public static final String ID = "urn:ogc:def:function:geoxacml:1.0:geometry-is-rectangle";

		public IsRectangle()
		{
			super(ID, StandardDatatypes.BOOLEAN, true, Arrays.asList(GeometryValue.DATATYPE));
		}

		@Override
		public FirstOrderFunctionCall<BooleanValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes)
		{

			return new EagerSinglePrimitiveTypeEval<BooleanValue, GeometryValue>(functionSignature, argExpressions, remainingArgTypes)
			{

				@Override
				protected BooleanValue evaluate(final Deque<GeometryValue> args) throws IndeterminateEvaluationException
				{
					return new BooleanValue(args.poll().getUnderlyingValue().isRectangle());
				}

			};
		}
	}

	public final static class IsNull extends SingleParameterTypedFirstOrderFunction<BooleanValue, GeometryValue>
	{
		public static final String ID = "urn:ogc:def:function:geoxacml:1.0:geometry-is-null";

		public IsNull()
		{
			super(ID, StandardDatatypes.BOOLEAN, true, Arrays.asList(GeometryValue.DATATYPE));
		}

		@Override
		public FirstOrderFunctionCall<BooleanValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes)
		{

			return new EagerSinglePrimitiveTypeEval<BooleanValue, GeometryValue>(functionSignature, argExpressions, remainingArgTypes)
			{

				@Override
				protected BooleanValue evaluate(final Deque<GeometryValue> args) throws IndeterminateEvaluationException
				{
					GeometryMetadata gm = (GeometryMetadata)args.poll().getUnderlyingValue().getUserData();
					return new BooleanValue(gm.getSRS().equalsIgnoreCase("NULL"));
				}

			};
		}
	}
	
	public final static class Dimension extends SingleParameterTypedFirstOrderFunction<IntegerValue, GeometryValue>
	{
		public static final String ID = "urn:ogc:def:function:geoxacml:3.0:geometry-dimension";

		public Dimension()
		{
			super(ID, StandardDatatypes.INTEGER, true, Arrays.asList(GeometryValue.DATATYPE));
		}

		@Override
		public FirstOrderFunctionCall<IntegerValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes)
		{

			return new EagerSinglePrimitiveTypeEval<IntegerValue, GeometryValue>(functionSignature, argExpressions, remainingArgTypes)
			{

				@Override
				protected IntegerValue evaluate(final Deque<GeometryValue> args) throws IndeterminateEvaluationException
				{
					return IntegerValue.valueOf(args.poll().getUnderlyingValue().getDimension());
				}

			};
		}
	}

	public final static class GeometryType extends SingleParameterTypedFirstOrderFunction<StringValue, GeometryValue>
	{
		public static final String ID = "urn:ogc:def:function:geoxacml:3.0:geometry-type";

		public GeometryType()
		{
			super(ID, StandardDatatypes.STRING, true, Arrays.asList(GeometryValue.DATATYPE));
		}

		@Override
		public FirstOrderFunctionCall<StringValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes)
		{

			return new EagerSinglePrimitiveTypeEval<StringValue, GeometryValue>(functionSignature, argExpressions, remainingArgTypes)
			{

				@Override
				protected StringValue evaluate(final Deque<GeometryValue> args) throws IndeterminateEvaluationException
				{
					return new StringValue(args.poll().getUnderlyingValue().getGeometryType());
				}

			};
		}
	}

	public final static class SRS extends SingleParameterTypedFirstOrderFunction<StringValue, GeometryValue>
	{
		public static final String ID = "urn:ogc:def:function:geoxacml:3.0:geometry-srs";

		public SRS()
		{
			super(ID, StandardDatatypes.STRING, true, Arrays.asList(GeometryValue.DATATYPE));
		}

		@Override
		public FirstOrderFunctionCall<StringValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes)
		{

			return new EagerSinglePrimitiveTypeEval<StringValue, GeometryValue>(functionSignature, argExpressions, remainingArgTypes)
			{

				@Override
				protected StringValue evaluate(final Deque<GeometryValue> args) throws IndeterminateEvaluationException
				{
					GeometryMetadata gm = (GeometryMetadata)args.poll().getUnderlyingValue().getUserData();
					return new StringValue(gm.getSRS());
				}

			};
		}
	}

	public final static class SRID extends SingleParameterTypedFirstOrderFunction<IntegerValue, GeometryValue>
	{
		public static final String ID = "urn:ogc:def:function:geoxacml:3.0:geometry-dimension";

		public SRID()
		{
			super(ID, StandardDatatypes.INTEGER, true, Arrays.asList(GeometryValue.DATATYPE));
		}

		@Override
		public FirstOrderFunctionCall<IntegerValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes)
		{

			return new EagerSinglePrimitiveTypeEval<IntegerValue, GeometryValue>(functionSignature, argExpressions, remainingArgTypes)
			{

				@Override
				protected IntegerValue evaluate(final Deque<GeometryValue> args) throws IndeterminateEvaluationException
				{
					return IntegerValue.valueOf(args.poll().getUnderlyingValue().getSRID());
				}

			};
		}
	}

	public final static class AsText extends SingleParameterTypedFirstOrderFunction<StringValue, GeometryValue>
	{
		public static final String ID = "urn:ogc:def:function:geoxacml:3.0:geometry-as-text";

		public AsText()
		{
			super(ID, StandardDatatypes.STRING, true, Arrays.asList(GeometryValue.DATATYPE));
		}

		@Override
		public FirstOrderFunctionCall<StringValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes)
		{

			return new EagerSinglePrimitiveTypeEval<StringValue, GeometryValue>(functionSignature, argExpressions, remainingArgTypes)
			{

				@Override
				protected StringValue evaluate(final Deque<GeometryValue> args) throws IndeterminateEvaluationException
				{
					return new StringValue(args.poll().toString());
				}

			};
		}
	}


}
