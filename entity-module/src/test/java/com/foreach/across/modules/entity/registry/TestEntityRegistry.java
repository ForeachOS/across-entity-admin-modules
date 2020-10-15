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

package com.foreach.across.modules.entity.registry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestEntityRegistry
{
	private MutableEntityRegistry registry;
	private MutableEntityConfiguration entityConfiguration;

	@BeforeEach
	public void reset() {
		registry = new EntityRegistryImpl();
		entityConfiguration = mock( MutableEntityConfiguration.class );
	}

	@Test
	public void nullEntityConfigurationThrowsException() {
		assertThrows( IllegalArgumentException.class, () -> {
			registry.register( null );
		} );
	}

	@Test
	public void nameIsRequiredOnAnEntityConfiguration() {
		assertThrows( IllegalArgumentException.class, () -> {
			registry.register( entityConfiguration );
		} );
	}

	@Test
	public void typeIsRequiredOnAnEntityConfiguration() {
		assertThrows( IllegalArgumentException.class, () -> {
			when( entityConfiguration.getName() ).thenReturn( "entityName" );
			registry.register( entityConfiguration );
		} );
	}

	@Test
	public void registeringEntityConfiguration() {
		when( entityConfiguration.getEntityType() ).thenReturn( BigDecimal.class );
		when( entityConfiguration.getName() ).thenReturn( "entityName" );

		assertFalse( registry.contains( BigDecimal.class ) );
		assertFalse( registry.contains( "entityName" ) );
		assertNull( registry.getEntityConfiguration( BigDecimal.class ) );
		assertNull( registry.getEntityConfiguration( "entityName" ) );

		registry.register( entityConfiguration );
		assertEquals( 1, registry.getEntities().size() );
		assertThat( registry.getEntities(), hasItem( entityConfiguration ) );

		MutableEntityConfiguration other = mock( MutableEntityConfiguration.class );
		when( other.getEntityType() ).thenReturn( String.class );
		when( other.getName() ).thenReturn( "otherEntity" );

		assertFalse( registry.contains( String.class ) );
		assertFalse( registry.contains( "otherEntity" ) );
		assertNull( registry.getEntityConfiguration( String.class ) );
		assertNull( registry.getEntityConfiguration( "otherEntity" ) );

		registry.register( other );

		assertTrue( registry.contains( BigDecimal.class ) );
		assertTrue( registry.contains( "entityName" ) );
		assertSame( entityConfiguration, registry.getEntityConfiguration( BigDecimal.class ) );
		assertSame( entityConfiguration, registry.getEntityConfiguration( "entityName" ) );

		assertTrue( registry.contains( String.class ) );
		assertTrue( registry.contains( "otherEntity" ) );
		assertSame( other, registry.getEntityConfiguration( String.class ) );
		assertSame( other, registry.getEntityConfiguration( "otherEntity" ) );

		assertEquals( 2, registry.getEntities().size() );
		assertThat( registry.getEntities(),
		            hasItems( (EntityConfiguration) entityConfiguration, other ) );
	}

	@Test
	public void registeringSecondEntityWithSameNameIsNotAllowed() {
		assertThrows( IllegalArgumentException.class, () -> {
			when( entityConfiguration.getEntityType() ).thenReturn( BigDecimal.class );
			when( entityConfiguration.getName() ).thenReturn( "entityName" );

			registry.register( entityConfiguration );

			MutableEntityConfiguration other = mock( MutableEntityConfiguration.class );
			when( other.getEntityType() ).thenReturn( BigDecimal.class );
			when( other.getName() ).thenReturn( "entityName" );

			registry.register( other );
		} );
	}

	@Test
	public void registeringSecondEntityWithSameTypeIsNotAllowed() {
		assertThrows( IllegalArgumentException.class, () -> {
			when( entityConfiguration.getEntityType() ).thenReturn( BigDecimal.class );
			when( entityConfiguration.getName() ).thenReturn( "entityName" );

			registry.register( entityConfiguration );

			MutableEntityConfiguration other = mock( MutableEntityConfiguration.class );
			when( other.getEntityType() ).thenReturn( BigDecimal.class );
			when( other.getName() ).thenReturn( "otherEntity" );

			registry.register( other );
		} );
	}

	@Test
	public void reRegisteringEntityConfiguration() {
		when( entityConfiguration.getEntityType() ).thenReturn( BigDecimal.class );
		when( entityConfiguration.getName() ).thenReturn( "entityName" );

		registry.register( entityConfiguration );
		registry.register( entityConfiguration );

		assertEquals( 1, registry.getEntities().size() );
	}

	@Test
	public void removingAndReRegisteringEntityConfiguration() {
		when( entityConfiguration.getEntityType() ).thenReturn( BigDecimal.class );
		when( entityConfiguration.getName() ).thenReturn( "entityName" );

		registry.register( entityConfiguration );

		assertSame( entityConfiguration, registry.remove( BigDecimal.class ) );
		assertTrue( registry.getEntities().isEmpty() );
		assertFalse( registry.contains( "entityName" ) );
		assertFalse( registry.contains( BigDecimal.class ) );

		MutableEntityConfiguration other = mock( MutableEntityConfiguration.class );
		when( other.getEntityType() ).thenReturn( BigDecimal.class );
		when( other.getName() ).thenReturn( "entityName" );

		registry.register( other );
		assertTrue( registry.contains( BigDecimal.class ) );

		assertSame( other, registry.remove( "entityName" ) );
		assertTrue( registry.getEntities().isEmpty() );
		assertFalse( registry.contains( "entityName" ) );
		assertFalse( registry.contains( BigDecimal.class ) );

		registry.register( entityConfiguration );
		assertTrue( registry.contains( "entityName" ) );
	}

	@Test
	public void entityListIsSortedAccordingToDisplayName() {
		when( entityConfiguration.getEntityType() ).thenReturn( BigDecimal.class );
		when( entityConfiguration.getName() ).thenReturn( "entity1" );
		when( entityConfiguration.getDisplayName() ).thenReturn( "Second entity registered first" );

		registry.register( entityConfiguration );

		MutableEntityConfiguration other = mock( MutableEntityConfiguration.class );
		when( other.getEntityType() ).thenReturn( String.class );
		when( other.getName() ).thenReturn( "entity2" );
		when( other.getDisplayName() ).thenReturn( "First entity registered second" );

		registry.register( other );

		assertEquals( registry.getEntities(), (Collection) Arrays.asList( other, entityConfiguration ) );
	}
}
