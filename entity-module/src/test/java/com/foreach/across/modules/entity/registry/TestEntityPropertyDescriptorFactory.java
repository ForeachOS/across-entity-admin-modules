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

import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.registry.properties.*;
import org.junit.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;

import java.beans.PropertyDescriptor;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
public class TestEntityPropertyDescriptorFactory
{
	private EntityPropertyDescriptorFactory descriptorFactory = new EntityPropertyDescriptorFactoryImpl();

	@Test
	public void createWithParent() {
		SimpleEntityPropertyDescriptor one = new SimpleEntityPropertyDescriptor( "address" );
		one.setDisplayName( "Address" );
		one.setAttribute( Sort.Order.class, Sort.Order.by( "address" ) );
		( (GenericEntityPropertyController) one.getController() ).order( 1000 );

		MutableEntityPropertyDescriptor merged = descriptorFactory.createWithOriginal( "street", one );
		merged.setAttribute( Sort.Order.class, Sort.Order.by( "street" ) );

		assertEquals( 1000, merged.getController().getOrder() );
		assertEquals( "street", merged.getName() );
		assertEquals( "Address", merged.getDisplayName() );
		assertEquals( Sort.Order.by( "street" ), merged.getAttribute( Sort.Order.class ) );
	}

	@Test
	public void readableAndWritableProperty() {
		PropertyDescriptor nativeDescriptor = BeanUtils.getPropertyDescriptor( Instance.class, "name" );
		EntityPropertyDescriptor descriptor = descriptorFactory.create( nativeDescriptor, Instance.class );

		assertTrue( descriptor.isReadable() );
		assertTrue( descriptor.isWritable() );
		assertFalse( descriptor.isHidden() );
		assertSame( nativeDescriptor, descriptor.getAttribute( EntityAttributes.NATIVE_PROPERTY_DESCRIPTOR ) );

		EntityPropertyController controller = descriptor.getController();
		assertNotNull( controller );
		assertEquals( EntityPropertyController.BEFORE_ENTITY, controller.getOrder() );

		Instance instance = new Instance();
		EntityPropertyBindingContext context = EntityPropertyBindingContext.forReading( instance );

		assertNull( instance.getName() );
		assertNull( controller.fetchValue( context ) );
		instance.setName( "original" );
		assertEquals( "original", controller.fetchValue( context ) );
		assertTrue( controller.applyValue( context, new EntityPropertyValue<>( null, "my name", false ) ) );
		assertEquals( "my name", instance.getName() );
		assertEquals( "my name", controller.fetchValue( context ) );

		assertFalse( controller.applyValue( EntityPropertyBindingContext.forReading( null ), new EntityPropertyValue<>( null, "my name", false ) ) );
	}

	@Test
	public void nonWritablePropertyIsHiddenByDefault() {
		EntityPropertyDescriptor descriptor = descriptorFactory.create(
				BeanUtils.getPropertyDescriptor( Instance.class, "readonly" ), Instance.class
		);

		assertTrue( descriptor.isReadable() );
		assertFalse( descriptor.isWritable() );
		assertTrue( descriptor.isHidden() );

		EntityPropertyController controller = descriptor.getController();
		assertNotNull( controller );
		assertEquals( EntityPropertyController.BEFORE_ENTITY, controller.getOrder() );

		Instance instance = new Instance();
		EntityPropertyBindingContext context = EntityPropertyBindingContext.forReading( instance );

		assertEquals( 0, instance.readonly );
		assertEquals( Integer.valueOf( 0 ), controller.fetchValue( context ) );
		assertFalse( controller.applyValue( context, new EntityPropertyValue<>( null, 123, false ) ) );
		assertEquals( 0, instance.readonly );

		instance.readonly = 456;
		assertEquals( Integer.valueOf( 456 ), controller.fetchValue( context ) );
	}

	@Test
	public void nonReadablePropertyIsHiddenByDefault() {
		EntityPropertyDescriptor descriptor = descriptorFactory.create(
				BeanUtils.getPropertyDescriptor( Instance.class, "writeonly" ), Instance.class
		);

		assertFalse( descriptor.isReadable() );
		assertTrue( descriptor.isWritable() );
		assertTrue( descriptor.isHidden() );

		EntityPropertyController controller = descriptor.getController();
		assertNotNull( controller );
		assertEquals( EntityPropertyController.BEFORE_ENTITY, controller.getOrder() );

		Date now = new Date();

		Instance instance = new Instance();
		EntityPropertyBindingContext context = EntityPropertyBindingContext.forReading( instance );

		assertNull( instance.writeonly );
		assertNull( controller.fetchValue( context ) );
		assertTrue( controller.applyValue( context, new EntityPropertyValue<>( null, now, false ) ) );
		assertNull( controller.fetchValue( context ) );
		assertSame( now, instance.writeonly );
	}

	@SuppressWarnings("unused")
	private static class Instance
	{
		private String name;
		private int readonly;
		private Date writeonly;

		public String getName() {
			return name;
		}

		public void setName( String name ) {
			this.name = name;
		}

		public int getReadonly() {
			return readonly;
		}

		public void setWriteonly( Date writeonly ) {
			this.writeonly = writeonly;
		}
	}
}
