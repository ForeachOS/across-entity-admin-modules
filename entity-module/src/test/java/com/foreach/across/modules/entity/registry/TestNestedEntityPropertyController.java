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

import com.foreach.across.modules.entity.registry.properties.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 3.2.0
 */
@ExtendWith(MockitoExtension.class)
class TestNestedEntityPropertyController
{
	@Mock
	private EntityPropertyController<Object> address;

	@Mock
	private EntityPropertyDescriptor user;

	@Mock
	private EntityPropertyBindingContext pageBindingContext;

	@Mock
	private EntityPropertyBindingContext userBindingContext;

	private NestedEntityPropertyController userAddress;

	@BeforeEach
	void before() {
		userAddress = new NestedEntityPropertyController( user, address );
		when( pageBindingContext.resolvePropertyBindingContext( user ) ).thenReturn( userBindingContext );
	}

	@Test
	void fetchValue() {
		when( address.fetchValue( userBindingContext ) ).thenReturn( "address value" );
		assertThat( userAddress.fetchValue( pageBindingContext ) ).isEqualTo( "address value" );
	}

	@Test
	void applyValue() {
		EntityPropertyValue<Object> value = new EntityPropertyValue<>( "old", "new", false );
		userAddress.applyValue( pageBindingContext, value );
		verify( address ).applyValue( userBindingContext, value );
	}

	@Test
	void createValue() {
		userAddress.createValue( pageBindingContext );
		verify( address ).createValue( userBindingContext );
	}

	@Test
	void createDto() {
		userAddress.createDto( pageBindingContext, "123" );
		verify( address ).createDto( userBindingContext, "123" );
	}

	@Test
	@SuppressWarnings("unchecked")
	void save() {
		EntityPropertyValue<Object> value = new EntityPropertyValue<>( "old", "new", false );
		userAddress.save( pageBindingContext, value );
		verify( address ).save( userBindingContext, value );

		BiConsumer<EntityPropertyBindingContext, EntityPropertyValue<Object>> sf = mock( BiConsumer.class );
		userAddress.saveConsumer( sf );
		userAddress.save( pageBindingContext, value );
		verify( sf ).accept( userBindingContext, value );
	}
}
