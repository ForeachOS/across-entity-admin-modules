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

package test.bind;

import com.foreach.across.modules.entity.bind.EntityPropertiesBinder;
import com.foreach.across.modules.entity.config.builders.EntityPropertyDescriptorBuilder;
import com.foreach.across.modules.entity.registry.properties.DefaultEntityPropertyRegistry;
import com.foreach.across.modules.entity.registry.properties.DefaultEntityPropertyRegistryProvider;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.IterableAssert;
import org.assertj.core.api.MapAssert;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.validation.DataBinder;
import org.springframework.validation.ObjectError;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author Arne Vandamme
 * @since 3.1.0
 */
public abstract class AbstractEntityPropertiesBinderTest
{
	private static final String ENTITY = "someEntity";

	private ConversionService conversionService = new DefaultConversionService();

	private DefaultEntityPropertyRegistry propertyRegistry;
	private DataBinder dataBinder;

	protected EntityPropertiesBinder propertyValues;

	@BeforeEach
	public void resetForBinding() {
		propertyRegistry = new DefaultEntityPropertyRegistry( DefaultEntityPropertyRegistryProvider.newInstance() );
		registeredProperties().forEach( b -> propertyRegistry.register( b.build() ) );

		propertyValues = new EntityPropertiesBinder( propertyRegistry );
		propertyValues.setBinderPrefix( "properties" );
		propertyValues.setConversionService( conversionService );
		propertyValues.setEntity( ENTITY );
		propertyValues.setTarget( ENTITY );

		dataBinder = new DataBinder( propertyValues );
		dataBinder.setConversionService( conversionService );

	}

	protected abstract Collection<EntityPropertyDescriptorBuilder> registeredProperties();

	protected void bind( String... inlinedProperties ) {
		MutablePropertyValues pv = new MutablePropertyValues( TestPropertySourceUtils.convertInlinedPropertiesToMap( inlinedProperties ) );
		dataBinder.bind( pv );

		if ( dataBinder.getBindingResult().hasErrors() ) {
			fail( dataBinder.getBindingResult().getAllErrors().stream().map( ObjectError::getDefaultMessage ).collect( Collectors.joining( "\n" ) ) );
		}
	}

	protected AbstractObjectAssert<?, ?> assertProperty( String property ) {
		return assertThat( propertyValues.get( property ).getValue() );
	}

	@SuppressWarnings("unchecked")
	protected IterableAssert<Object> assertCollection( String property ) {
		return assertThat( (Iterable) propertyValues.get( property ).getValue() );
	}

	@SuppressWarnings("unchecked")
	protected MapAssert<Object, Object> assertMap( String property ) {
		return assertThat( (Map) propertyValues.get( property ).getValue() );
	}

}
