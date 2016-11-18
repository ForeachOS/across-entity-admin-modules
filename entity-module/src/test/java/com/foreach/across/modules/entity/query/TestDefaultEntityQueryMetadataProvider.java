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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.convert.TypeDescriptor;

import java.util.ArrayList;
import java.util.Collections;

import static com.foreach.across.modules.entity.query.EntityQueryOps.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultEntityQueryMetadataProvider
{
	@Mock
	private EntityPropertyRegistry propertyRegistry;

	@Mock
	private EntityPropertyDescriptor descriptor;

	private EntityQueryMetadataProvider metadataProvider;

	@Before
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
		expectedOperands( EQ, NEQ, IN, NOT_IN );
	}

	@Test
	public void allowedStringValues() {
		when( descriptor.getPropertyTypeDescriptor() ).thenReturn( TypeDescriptor.valueOf( String.class ) );
		expectedValidValue( new EQString( "text" ), DefaultEntityQueryMetadataProvider.STRING_OPS );
		expectedInvalidValue( new EQGroup( Collections.singleton( "text" ) ), EntityQueryOps.EQ, EntityQueryOps.NEQ );
		expectedValidValue( new EQGroup( Collections.singleton( "text" ) ), EntityQueryOps.IN, EntityQueryOps.NOT_IN );
		expectedValidValue( new EQFunction( "text" ), DefaultEntityQueryMetadataProvider.STRING_OPS );
		// wrong type of function
		// collection returning function (only for IN)

	}

	@Test
	public void numberOperands() {
		when( descriptor.getPropertyTypeDescriptor() ).thenReturn( TypeDescriptor.valueOf( Integer.class ) );
		expectedOperands( EQ, NEQ, IN, NOT_IN );
	}

	@Test
	public void entityOperands() {
		when( descriptor.getPropertyTypeDescriptor() ).thenReturn( TypeDescriptor.valueOf( Object.class ) );
		expectedOperands( EQ, NEQ, IN, NOT_IN );
	}

	@Test
	public void collectionOperands() {
		when( descriptor.getPropertyTypeDescriptor() ).thenReturn( TypeDescriptor.valueOf( ArrayList.class ) );
		expectedOperands( CONTAINS, NOT_CONTAINS );
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
		for ( EntityQueryOps op : EntityQueryOps.values() ) {
			if ( ArrayUtils.contains( allowed, op ) ) {
				assertTrue(
						"Expected operand was not allowed: " + op,
						metadataProvider.isValidOperatorForProperty( op, "existing" )
				);
			}
			else {
				assertFalse(
						"Operand should not be allowed: " + op,
						metadataProvider.isValidOperatorForProperty( op, "existing" )
				);
			}
		}
	}
}
