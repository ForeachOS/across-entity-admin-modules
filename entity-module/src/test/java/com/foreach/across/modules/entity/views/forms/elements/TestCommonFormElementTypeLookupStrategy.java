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
package com.foreach.across.modules.entity.views.forms.elements;

import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.registry.EntityRegistry;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.testmodules.springdata.Client;
import com.foreach.across.modules.entity.testmodules.springdata.CompanyStatus;
import com.foreach.common.test.MockedLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = TestCommonFormElementTypeLookupStrategy.Config.class, loader = MockedLoader.class)
public class TestCommonFormElementTypeLookupStrategy
{
	@Autowired
	private CommonFormElementTypeLookupStrategy strategy;

	@Autowired
	private EntityRegistry entityRegistry;

	private EntityConfiguration entityConfiguration = mock( EntityConfiguration.class );
	private EntityPropertyDescriptor descriptor = mock( EntityPropertyDescriptor.class );

	@Before
	public void resetMocks() {
		reset( entityConfiguration, descriptor );

		when( descriptor.isWritable() ).thenReturn( true );
	}

	@Test
	public void hiddenType() {
		when( descriptor.isHidden() ).thenReturn( true );
		assertEquals( CommonFormElements.HIDDEN, lookup() );
	}

	@Test
	public void textboxTypeForPrimitives() {
		assertEquals( CommonFormElements.TEXTBOX, lookup( String.class ) );
		assertEquals( CommonFormElements.TEXTBOX, lookup( Integer.class ) );
		assertEquals( CommonFormElements.TEXTBOX, lookup( Long.class ) );
		assertEquals( CommonFormElements.TEXTBOX, lookup( BigDecimal.class ) );
	}

	@Test
	public void enumValueShouldReturnSelectType() {
		assertEquals( CommonFormElements.SELECT, lookup( CompanyStatus.class ) );
	}

	@Test
	@SuppressWarnings("unchecked")
	public void singleEntityTypeShouldReturnSelectType() {
		EntityConfiguration clientConfig = mock( EntityConfiguration.class );

		when( entityConfiguration.getEntityType() ).thenReturn( (Class) Client.class );
		when( entityRegistry.getEntityConfiguration( Client.class ) ).thenReturn( clientConfig );

		assertEquals( CommonFormElements.SELECT, lookup( Client.class ) );
	}

	@Test
	@SuppressWarnings("unchecked")
	public void collectionEntityTypeShouldReturnMultiCheckbox() {
		EntityConfiguration clientConfig = mock( EntityConfiguration.class );

		when( entityConfiguration.getEntityType() ).thenReturn( (Class) Client.class );
		when( entityRegistry.getEntityConfiguration( Client.class ) ).thenReturn( clientConfig );

		when( descriptor.getPropertyType() ).thenReturn( (Class) List.class );
		when( descriptor.getPropertyResolvableType() )
				.thenReturn( ResolvableType.forClassWithGenerics( List.class, Client.class ) );

		assertEquals( CommonFormElements.MULTI_CHECKBOX, strategy.findElementType( entityConfiguration, descriptor ) );
	}

	@Test
	public void unknownType() {
		assertNull( lookup() );
	}

	@SuppressWarnings("unchecked")
	private String lookup( Class propertyType ) {
		when( descriptor.getPropertyType() ).thenReturn( propertyType );
		return strategy.findElementType( entityConfiguration, descriptor );
	}

	private String lookup() {
		return strategy.findElementType( entityConfiguration, descriptor );
	}

	@Configuration
	protected static class Config
	{
		@Bean
		public CommonFormElementTypeLookupStrategy lookupStrategy() {
			return new CommonFormElementTypeLookupStrategy();
		}
	}
}
