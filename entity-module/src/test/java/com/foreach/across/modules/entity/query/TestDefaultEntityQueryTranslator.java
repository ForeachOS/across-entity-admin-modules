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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.convert.TypeDescriptor;

import static com.foreach.across.modules.entity.query.EntityQueryOps.EQ;
import static com.foreach.across.modules.entity.query.EntityQueryOps.IN;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultEntityQueryTranslator
{
	@Mock
	private EQTypeConverter typeConverter;

	@Mock
	private EntityPropertyRegistry propertyRegistry;

	private DefaultEntityQueryTranslator translator;

	@Before
	public void reset() {
		translator = new DefaultEntityQueryTranslator();
		translator.setPropertyRegistry( propertyRegistry );
		translator.setTypeConverter( typeConverter );
		translator.validateProperties();
	}

	@Test(expected = IllegalArgumentException.class)
	public void validatePropertiesRequiresTypeConverter() {
		translator.setTypeConverter( null );
		translator.validateProperties();
	}

	@Test(expected = IllegalArgumentException.class)
	public void validatePropertiesRequiresQueryTranslator() {
		translator.setPropertyRegistry( null );
		translator.validateProperties();
	}

	@Test(expected = IllegalArgumentException.class)
	public void exceptionIfPropertyNotFound() {
		EntityQuery query = EntityQuery.and(
				new EntityQueryCondition( "name", IN, "one", "two" ),
				EntityQuery.or(
						new EntityQueryCondition( "id", EQ, 1 ),
						new EntityQueryCondition( "id", EQ, 2 )
				)
		);

		translator.translate( query );
	}

	@Test
	public void validQueryTranslation() {
		EntityQuery raw = EntityQuery.and(
				new EntityQueryCondition( "name", IN, "one", "two" ),
				EntityQuery.or(
						new EntityQueryCondition( "id", EQ, "1" ),
						new EntityQueryCondition( "id", EQ, "2" )
				)
		);

		EntityPropertyDescriptor name = mock( EntityPropertyDescriptor.class );
		when( name.getName() ).thenReturn( "translatedName" );
		when( name.getPropertyTypeDescriptor() ).thenReturn( TypeDescriptor.valueOf( String.class ) );

		EntityPropertyDescriptor id = mock( EntityPropertyDescriptor.class );
		when( id.getName() ).thenReturn( "translatedId" );
		when( id.getPropertyTypeDescriptor() ).thenReturn( TypeDescriptor.valueOf( Integer.class ) );

		when( propertyRegistry.getProperty( "name" ) ).thenReturn( name );
		when( propertyRegistry.getProperty( "id" ) ).thenReturn( id );

		when( typeConverter.convertAll( TypeDescriptor.valueOf( String.class ), true, "one", "two" ) )
				.thenReturn( new Object[] { "three", "four" } );
		when( typeConverter.convertAll( TypeDescriptor.valueOf( Integer.class ), true, "1" ) )
				.thenReturn( new Object[] { 1 } );
		when( typeConverter.convertAll( TypeDescriptor.valueOf( Integer.class ), true, "2" ) )
				.thenReturn( new Object[] { 2 } );

		EntityQuery translated = EntityQuery.and(
				new EntityQueryCondition( "translatedName", IN, "three", "four" ),
				EntityQuery.or(
						new EntityQueryCondition( "translatedId", EQ, 1 ),
						new EntityQueryCondition( "translatedId", EQ, 2 )
				)
		);

		assertEquals( translated, translator.translate( raw ) );
	}
}
