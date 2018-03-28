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
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.data.domain.Sort;

import java.util.List;

import static com.foreach.across.modules.entity.query.EntityQueryOps.*;
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

	@Test(expected = EntityQueryParsingException.IllegalField.class)
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

	@Test
	public void isEmptyOnNonCollectionGetsReplacedByIsNull() {
		EntityPropertyDescriptor cities = mock( EntityPropertyDescriptor.class );
		when( cities.getName() ).thenReturn( "translatedCities" );
		when( cities.getPropertyTypeDescriptor() ).thenReturn( TypeDescriptor.valueOf( String.class ) );
		when( propertyRegistry.getProperty( "cities" ) ).thenReturn( cities );

		EntityQuery query = EntityQuery.and( new EntityQueryCondition( "cities", IS_EMPTY ) );
		EntityQuery translated = EntityQuery.and( new EntityQueryCondition( "translatedCities", IS_NULL ) );
		assertEquals( translated, translator.translate( query ) );

		query = EntityQuery.and( new EntityQueryCondition( "cities", IS_NOT_EMPTY ) );
		translated = EntityQuery.and( new EntityQueryCondition( "translatedCities", IS_NOT_NULL ) );
		assertEquals( translated, translator.translate( query ) );
	}

	@Test
	public void nullInCollectionArgumentResultsInExpandedQuery() {
		EntityPropertyDescriptor cities = mock( EntityPropertyDescriptor.class );
		when( cities.getName() ).thenReturn( "translatedCities" );
		when( cities.getPropertyTypeDescriptor() ).thenReturn( TypeDescriptor.valueOf( String.class ) );
		when( propertyRegistry.getProperty( "cities" ) ).thenReturn( cities );

		when( typeConverter.convertAll( TypeDescriptor.valueOf( String.class ), true, "brussel", "amsterdam" ) )
				.thenReturn( new Object[] { "brussel", "amsterdam" } );
		when( typeConverter.convertAll( TypeDescriptor.valueOf( String.class ), true,
		                                new EQGroup( new EQString( "brussel" ), new EQString( "amsterdam" ) ) ) )
				.thenReturn( new Object[] { "brussel", "amsterdam" } );

		EntityQuery query = EntityQuery.and(
				new EntityQueryCondition( "cities", EntityQueryOps.IN,
				                          new EQGroup( new EQString( "brussel" ), EQValue.NULL, new EQString( "amsterdam" ) ) )
		);
		EntityQuery translated = EntityQuery.and(
				EntityQuery.or(
						new EntityQueryCondition( "translatedCities", EntityQueryOps.IN, "brussel", "amsterdam" ),
						new EntityQueryCondition( "translatedCities", EntityQueryOps.IS_NULL )
				)
		);
		assertEquals( translated, translator.translate( query ) );

		query = EntityQuery.and(
				new EntityQueryCondition( "cities", EntityQueryOps.NOT_IN, "brussel", null, "amsterdam" )
		);
		translated = EntityQuery.and(
				EntityQuery.and(
						new EntityQueryCondition( "translatedCities", EntityQueryOps.NOT_IN, "brussel", "amsterdam" ),
						new EntityQueryCondition( "translatedCities", EntityQueryOps.IS_NOT_NULL )
				)
		);
		assertEquals( translated, translator.translate( query ) );
	}

	@Test
	public void isEmptyOnCollectionOrArrayIsKept() {
		EntityPropertyDescriptor cities = mock( EntityPropertyDescriptor.class );
		when( cities.getName() ).thenReturn( "translatedCities" );
		when( cities.getPropertyTypeDescriptor() )
				.thenReturn( TypeDescriptor.collection( List.class, TypeDescriptor.valueOf( String.class ) ) );
		when( propertyRegistry.getProperty( "cities" ) ).thenReturn( cities );

		EntityQuery query = EntityQuery.and( new EntityQueryCondition( "cities", IS_EMPTY ) );
		EntityQuery translated = EntityQuery.and( new EntityQueryCondition( "translatedCities", IS_EMPTY ) );
		assertEquals( translated, translator.translate( query ) );

		query = EntityQuery.and( new EntityQueryCondition( "cities", IS_NOT_EMPTY ) );
		translated = EntityQuery.and( new EntityQueryCondition( "translatedCities", IS_NOT_EMPTY ) );
		assertEquals( translated, translator.translate( query ) );

		when( cities.getPropertyTypeDescriptor() )
				.thenReturn( TypeDescriptor.array( TypeDescriptor.valueOf( String.class ) ) );

		query = EntityQuery.and( new EntityQueryCondition( "cities", IS_EMPTY ) );
		translated = EntityQuery.and( new EntityQueryCondition( "translatedCities", IS_EMPTY ) );
		assertEquals( translated, translator.translate( query ) );

		query = EntityQuery.and( new EntityQueryCondition( "cities", IS_NOT_EMPTY ) );
		translated = EntityQuery.and( new EntityQueryCondition( "translatedCities", IS_NOT_EMPTY ) );
		assertEquals( translated, translator.translate( query ) );
	}

	@Test
	public void conditionTranslatorIsUsedIfRegisteredOnProperty() {
		EntityQuery raw = EntityQuery.and(
				new EntityQueryCondition( "name", IN, "one", "two" ),
				EntityQuery.or(
						new EntityQueryCondition( "id", EQ, "1" ),
						new EntityQueryCondition( "id", EQ, "2" )
				)
		);

		EntityQueryConditionTranslator conditionTranslator = mock( EntityQueryConditionTranslator.class );

		EntityPropertyDescriptor name = mock( EntityPropertyDescriptor.class );
		when( name.getName() ).thenReturn( "translatedName" );
		when( name.getPropertyTypeDescriptor() ).thenReturn( TypeDescriptor.valueOf( String.class ) );

		EntityPropertyDescriptor id = mock( EntityPropertyDescriptor.class );
		when( id.getName() ).thenReturn( "translatedId" );
		when( id.getPropertyTypeDescriptor() ).thenReturn( TypeDescriptor.valueOf( Integer.class ) );
		when( id.getAttribute( EntityQueryConditionTranslator.class ) ).thenReturn( conditionTranslator );

		when( propertyRegistry.getProperty( "name" ) ).thenReturn( name );
		when( propertyRegistry.getProperty( "id" ) ).thenReturn( id );

		when( typeConverter.convertAll( TypeDescriptor.valueOf( String.class ), true, "one", "two" ) )
				.thenReturn( new Object[] { "three", "four" } );
		when( typeConverter.convertAll( TypeDescriptor.valueOf( Integer.class ), true, "1" ) )
				.thenReturn( new Object[] { 1 } );
		when( typeConverter.convertAll( TypeDescriptor.valueOf( Integer.class ), true, "2" ) )
				.thenReturn( new Object[] { 2 } );

		when( conditionTranslator.translate( new EntityQueryCondition( "translatedId", EQ, 1 ) ) )
				.thenReturn( new EntityQueryCondition( "translatedAgain", NEQ, 1 ) );
		when( conditionTranslator.translate( new EntityQueryCondition( "translatedId", EQ, 2 ) ) )
				.thenReturn( null );

		EntityQuery translated = EntityQuery.and(
				new EntityQueryCondition( "translatedName", IN, "three", "four" ),
				EntityQuery.or( new EntityQueryCondition( "translatedAgain", NEQ, 1 ) )
		);

		assertEquals( translated, translator.translate( raw ) );
	}

	@Test
	public void sortTranslationUsesPropertyOrderAttribute() {
		EntityPropertyDescriptor name = mock( EntityPropertyDescriptor.class );
		when( name.getAttribute( Sort.Order.class ) ).thenReturn(
				( new Sort.Order( Sort.Direction.DESC, "translatedName", Sort.NullHandling.NULLS_LAST ) ).ignoreCase()
		);

		when( propertyRegistry.getProperty( "name" ) ).thenReturn( name );

		EntityQuery rawQuery = EntityQuery.all(
				new Sort( new Sort.Order( Sort.Direction.ASC, "name" ), new Sort.Order( Sort.Direction.DESC, "city" ) )
		);

		EntityQuery translated = EntityQuery.all(
				new Sort(
						new Sort.Order( Sort.Direction.ASC, "translatedName", Sort.NullHandling.NULLS_LAST ).ignoreCase(),
						new Sort.Order( Sort.Direction.DESC, "city" )
				)
		);

		assertEquals( translated, translator.translate( rawQuery ) );
	}
}
