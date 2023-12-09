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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.convert.TypeDescriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static com.foreach.across.modules.entity.query.EntityQueryOps.*;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TestDefaultEntityQueryMetadataProvider
{
	@Mock
	private EntityPropertyRegistry propertyRegistry;

	@Mock
	private EntityPropertyDescriptor descriptor;

	private EntityQueryMetadataProvider metadataProvider;

	@BeforeEach
	public void reset() {
		metadataProvider = new DefaultEntityQueryMetadataProvider( propertyRegistry );
		when( propertyRegistry.getProperty( "existing" ) ).thenReturn( descriptor );
		when( propertyRegistry.contains( "existing" ) ).thenReturn( true );
	}

	@Test
	public void onlyExistingPropertyIsNotAllowed() {
		assertFalse( metadataProvider.isValidProperty( "bad" ) );
		assertTrue( metadataProvider.isValidProperty( "existing" ) );
	}

	@Test
	public void stringOperands() {
		when( descriptor.getPropertyTypeDescriptor() ).thenReturn( TypeDescriptor.valueOf( String.class ) );
		expectedOperands( EQ, NEQ, IN, NOT_IN, LIKE, NOT_LIKE, LIKE_IC, NOT_LIKE_IC, IS_NULL, IS_NOT_NULL, IS_EMPTY, IS_NOT_EMPTY, CONTAINS, NOT_CONTAINS );
	}

	@Test
	public void allowedStringValues() {
		expectedValidValue( new EQString( "text" ), EQ, NEQ, LIKE, NOT_LIKE );
		expectedInvalidValue( new EQGroup( Collections.singleton( new EQString( "text" ) ) ), EQ, NEQ );
		expectedValidValue( new EQGroup( Collections.singleton( new EQString( "text" ) ) ), IN, NOT_IN );
		expectedValidValue( new EQFunction( "text" ), EQ, NEQ, IN, NOT_IN, LIKE, NOT_LIKE, LIKE_IC, NOT_LIKE_IC, IS_NULL, IS_NOT_NULL, IS_EMPTY, IS_NOT_EMPTY,
		                    CONTAINS, NOT_CONTAINS );
	}

	@Test
	public void inOperatorsRequireEQGroupOrFunctionReturningCollection() {
		expectedInvalidValue( new EQValue( "" ), EntityQueryOps.IN, EntityQueryOps.NOT_IN );
		expectedInvalidValue( new EQString( "" ), EntityQueryOps.IN, EntityQueryOps.NOT_IN );
		expectedValidValue( new EQGroup( Collections.emptyList() ), EntityQueryOps.IN, EntityQueryOps.NOT_IN );
		expectedValidValue( new EQFunction( "" ), EntityQueryOps.IN, EntityQueryOps.NOT_IN );
	}

	@Test
	public void groupOperatorIsAllowedForInAndContains() {
		expectedInvalidValue( new EQGroup( Collections.emptyList() ), EQ, NEQ, GT, GE, LT, LE, LIKE );
	}

	@Test
	public void numberOperands() {
		when( descriptor.getPropertyTypeDescriptor() ).thenReturn( TypeDescriptor.valueOf( Integer.class ) );
		expectedOperands( EQ, NEQ, IN, NOT_IN, GT, GE, LT, LE, IS_NULL, IS_NOT_NULL, IS_EMPTY, IS_NOT_EMPTY );
	}

	@Test
	public void entityOperands() {
		when( descriptor.getPropertyTypeDescriptor() ).thenReturn( TypeDescriptor.valueOf( Object.class ) );
		expectedOperands( EQ, NEQ, IN, NOT_IN, IS_NULL, IS_NOT_NULL, IS_EMPTY, IS_NOT_EMPTY );
	}

	@Test
	public void collectionOperands() {
		when( descriptor.getPropertyTypeDescriptor() ).thenReturn( TypeDescriptor.valueOf( ArrayList.class ) );
		expectedOperands( CONTAINS, NOT_CONTAINS, IS_NULL, IS_NOT_NULL, IS_EMPTY, IS_NOT_EMPTY );
	}

	private void expectedValidValue( Object value, EntityQueryOps... allowed ) {
		for ( EntityQueryOps op : allowed ) {
			assertTrue( metadataProvider.isValidValueForPropertyAndOperator( value, "existing", op ) );
		}
	}

	private void expectedInvalidValue( Object value, EntityQueryOps... notAllowed ) {
		for ( EntityQueryOps op : notAllowed ) {
			assertFalse( metadataProvider.isValidValueForPropertyAndOperator( value, "existing", op ) );
		}
	}

	private void expectedOperands( EntityQueryOps... allowed ) {
		String supported = Arrays.stream( allowed ).map( EntityQueryOps::getToken ).collect( Collectors.joining( ", " ) );
		for ( EntityQueryOps op : EntityQueryOps.values() ) {
			EntityQueryCondition condition = new EntityQueryCondition( "existing", op, Collections.emptyList() );
			if ( ArrayUtils.contains( allowed, op ) ) {
				assertTrue(
						metadataProvider.isValidOperatorForProperty( op, "existing" ),
						"Expected operand was not allowed: " + op
				);
				assertThatNoException().isThrownBy( () -> metadataProvider.validateOperatorForCondition( condition ) );
			}
			else {
				assertFalse(
						metadataProvider.isValidOperatorForProperty( op, "existing" ),
						"Operand should not be allowed: " + op
				);
				assertThatThrownBy( () -> metadataProvider.validateOperatorForCondition( condition ) ).hasMessage(
						"Invalid operator for field existing: " + op.getToken() + ", supported operators are: " + supported );
			}
		}
	}
}
