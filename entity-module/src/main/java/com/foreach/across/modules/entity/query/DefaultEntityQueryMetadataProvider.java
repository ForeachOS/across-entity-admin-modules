/*
 * Copyright 2014 the original author or authors
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

package com.foreach.across.modules.entity.query;

import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyRegistry;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.Assert;

import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

import static com.foreach.across.modules.entity.query.EntityQueryOps.*;

/**
 * Default implementation of {@link EntityQueryMetadataProvider} that validates properties
 * using a {@link EntityPropertyRegistry}.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
public class DefaultEntityQueryMetadataProvider implements EntityQueryMetadataProvider
{
	private static final EntityQueryOps[] STRING_OPS =
			new EntityQueryOps[] { EQ, NEQ, IN, NOT_IN, LIKE, NOT_LIKE, LIKE_IC, NOT_LIKE_IC, IS_NULL, IS_NOT_NULL, IS_EMPTY, IS_NOT_EMPTY, CONTAINS,
			                       NOT_CONTAINS };
	private static final EntityQueryOps[] NUMBER_OPS =
			new EntityQueryOps[] { EQ, NEQ, IN, NOT_IN, GT, GE, LT, LE, IS_NULL, IS_NOT_NULL, IS_EMPTY, IS_NOT_EMPTY };
	private static final EntityQueryOps[] COLLECTION_OPS =
			new EntityQueryOps[] { CONTAINS, NOT_CONTAINS, IS_NULL, IS_NOT_NULL, IS_EMPTY, IS_NOT_EMPTY };
	private static final EntityQueryOps[] ENTITY_OPS =
			new EntityQueryOps[] { EQ, NEQ, IN, NOT_IN, IS_NULL, IS_NOT_NULL, IS_EMPTY, IS_NOT_EMPTY };

	private static final TypeDescriptor EQ_GROUP_TYPE = TypeDescriptor.valueOf( EQGroup.class );
	private static final TypeDescriptor EQ_FUNCTION_TYPE = TypeDescriptor.valueOf( EQFunction.class );

	private final EntityPropertyRegistry propertyRegistry;

	public DefaultEntityQueryMetadataProvider( EntityPropertyRegistry propertyRegistry ) {
		this.propertyRegistry = propertyRegistry;
	}

	@Override
	public boolean isValidProperty( String property ) {
		return propertyRegistry.contains( property );
	}

	@Override
	public boolean isValidOperatorForProperty( EntityQueryOps operator, String property ) {
		EntityPropertyDescriptor descriptor = propertyRegistry.getProperty( property );
		Assert.notNull( descriptor, () -> "No descriptor for property '" + property + "', unable to validate EntityQuery" );
		TypeDescriptor typeDescriptor = descriptor.getPropertyTypeDescriptor();
		Assert.notNull( typeDescriptor, () -> "No type descriptor for property '" + property + "', unable to validate EntityQuery" );
		return ArrayUtils.contains( retrieveOperandsForType( typeDescriptor ), operator );
	}

	@Override
	public boolean isValidValueForPropertyAndOperator( Object value, String property, EntityQueryOps operator ) {
		if ( operator == CONTAINS || operator == NOT_CONTAINS ) {
			return true;
		}

		TypeDescriptor valueType = TypeDescriptor.forObject( value );
		return isValidGroupOrNonGroupOperation( valueType, operator );
	}

	@Override
	public void validatePropertyForCondition( EntityQueryCondition condition ) {
		if ( !isValidProperty( condition.getProperty() ) ) {
			throw new EntityQueryParsingException.IllegalField( condition.getProperty() );
		}
	}

	@Override
	public void validateOperatorForCondition( EntityQueryCondition condition ) {
		if ( !isValidOperatorForProperty( condition.getOperand(), condition.getProperty() ) ) {
			EntityPropertyDescriptor descriptor = propertyRegistry.getProperty( condition.getProperty() );
			TypeDescriptor typeDescriptor = descriptor.getPropertyTypeDescriptor();
			throw new EntityQueryParsingException.IllegalOperator( condition.getOperand().getToken() + ", supported operators are: " +
					                                                       Arrays.stream(
							                                                       retrieveOperandsForType( typeDescriptor ) ).map( EntityQueryOps::getToken )
					                                                             .collect(
							                                                             Collectors.joining( ", " ) ),
			                                                       condition.getProperty() );
		}
	}

	@Override
	public void validateValueForCondition( EntityQueryCondition condition ) {
		if ( !isValidValueForPropertyAndOperator( condition.getFirstArgument(),
		                                          condition.getProperty(),
		                                          condition.getOperand() ) ) {
			throw new EntityQueryParsingException.IllegalValue( condition.getOperand().getToken(),
			                                                    condition.getProperty() );
		}
	}

	private boolean isValidGroupOrNonGroupOperation( TypeDescriptor valueType, EntityQueryOps operator ) {
		if ( EQType.class.isAssignableFrom( valueType.getType() ) ) {
			if ( operator == IN || operator == NOT_IN ) {
				return EQ_GROUP_TYPE.equals( valueType ) || EQ_FUNCTION_TYPE.equals( valueType );
			}

			return !EQ_GROUP_TYPE.equals( valueType );
		}

		return true;
	}

	private EntityQueryOps[] retrieveOperandsForType( TypeDescriptor type ) {
		Class<?> objectType = type.getObjectType();

		if ( String.class.equals( objectType ) ) {
			return STRING_OPS;
		}
		if ( type.isPrimitive()
				|| Number.class.isAssignableFrom( objectType )
				|| Date.class.isAssignableFrom( objectType )
				|| Temporal.class.isAssignableFrom( objectType ) ) {
			return NUMBER_OPS;
		}
		if ( type.isArray() || type.isCollection() || type.isMap() ) {
			return COLLECTION_OPS;
		}

		return ENTITY_OPS;
	}

}
