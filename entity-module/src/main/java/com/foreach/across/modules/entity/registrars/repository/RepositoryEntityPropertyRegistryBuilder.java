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
package com.foreach.across.modules.entity.registrars.repository;

import com.foreach.across.modules.entity.registry.MutableEntityConfiguration;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyComparators;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyRegistryProvider;
import com.foreach.across.modules.entity.registry.properties.MutableEntityPropertyRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.ValidatorFactory;
import javax.validation.metadata.BeanDescriptor;

/**
 * <p>Creates a {@link com.foreach.across.modules.entity.registry.properties.EntityPropertyRegistry} for a
 * {@link org.springframework.data.repository.core.support.RepositoryFactoryInformation} bean.</p>
 * <p>Puts every EntityPropertyRegistry in the central registry so properties of associated entities
 * can be determined as well.</p>
 */
@Component
@RequiredArgsConstructor
class RepositoryEntityPropertyRegistryBuilder
{
	private final ValidatorFactory validatorFactory;
	private final EntityPropertyRegistryProvider entityPropertyRegistryProvider;

	public <T> void buildEntityPropertyRegistry( MutableEntityConfiguration<T> entityConfiguration ) {
		Class<? extends T> entityType = entityConfiguration.getEntityType();

		MutableEntityPropertyRegistry registry = entityPropertyRegistryProvider.get( entityType );
		registry.setId( entityConfiguration.getName() );
		registry.setDefaultOrder( new EntityPropertyComparators.Ordered() );

		setBeanDescriptor( entityConfiguration );

		configureDefaultFilter( registry );

		entityConfiguration.setPropertyRegistry( registry );
	}

	private void configureDefaultFilter( MutableEntityPropertyRegistry registry ) {
		registry.getProperty( "class" ).setHidden( true );
	}

	private void setBeanDescriptor( MutableEntityConfiguration<?> entityConfiguration ) {
		BeanDescriptor beanDescriptor = validatorFactory.getValidator().getConstraintsForClass(
				entityConfiguration.getEntityType() );

		if ( beanDescriptor != null ) {
			entityConfiguration.setAttribute( BeanDescriptor.class, beanDescriptor );
		}
	}
}
