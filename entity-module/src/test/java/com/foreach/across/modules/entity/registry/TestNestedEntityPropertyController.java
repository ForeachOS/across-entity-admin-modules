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

import com.foreach.across.modules.entity.registry.properties.EntityPropertyBindingContext;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyController;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyValue;
import com.foreach.across.modules.entity.registry.properties.NestedEntityPropertyController;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 3.2.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestNestedEntityPropertyController
{
	@Mock
	private EntityPropertyController user;

	@Mock
	private EntityPropertyController address;

	private NestedEntityPropertyController userAddress;

	private EntityPropertyBindingContext originalContext;
	private EntityPropertyBindingContext childContext;
	private EntityPropertyBindingContext finalContext;

	@Before
	public void before() {
		userAddress = new NestedEntityPropertyController( "user", user, address );

		originalContext = EntityPropertyBindingContext.forReading( "page" );
		when( user.fetchValue( originalContext ) ).thenReturn( 123L );

		finalContext = EntityPropertyBindingContext.forReading( "page" );

		childContext = finalContext.getOrCreateChildContext( "user", ( p, b ) -> b.entity( 123L ).target( 123L ).controller( user ) );
	}

	@Test
	public void fetchValue() {
		when( address.fetchValue( childContext ) ).thenReturn( "address value" );
		assertThat( userAddress.fetchValue( originalContext ) ).isEqualTo( "address value" );
		verify( user ).fetchValue( originalContext );

		assertThat( userAddress.fetchValue( originalContext ) ).isEqualTo( "address value" );
		verify( user, times( 1 ) ).fetchValue( originalContext );

		assertThat( originalContext ).isEqualTo( finalContext );
	}

	@Test
	public void applyValue() {
		EntityPropertyValue value = new EntityPropertyValue<>( "old", "new", false );
		userAddress.applyValue( originalContext, value );
		verify( address ).applyValue( childContext, value );
	}

	@Test
	public void createValue() {
		userAddress.createValue( originalContext );
		verify( address ).createValue( childContext );
	}

	@Test
	public void save() {
		EntityPropertyValue value = new EntityPropertyValue<>( "old", "new", false );
		userAddress.save( originalContext, value );
		verify( address ).save( childContext, value );

		val sf = mock( BiConsumer.class );
		userAddress.saveConsumer( sf );
		userAddress.save( originalContext, value );
		verify( sf ).accept( childContext, value );
	}
}
