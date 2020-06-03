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

import com.foreach.across.modules.entity.registry.DefaultEntityModel;
import com.foreach.across.modules.entity.registry.DtoAwarePersistentEntityFactory;
import com.foreach.across.modules.entity.registry.MutableEntityConfiguration;
import com.foreach.across.modules.entity.registry.PersistentEntityFactory;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyRegistry;
import com.foreach.across.modules.entity.views.support.ConvertedValuePrinter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryInformation;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.format.Printer;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.io.Serializable;

/**
 * Builds an {@link com.foreach.across.modules.entity.registry.EntityModel} for a Spring data repository.
 */
@Component
@RequiredArgsConstructor
class RepositoryEntityModelBuilder implements BeanClassLoaderAware
{
	private final ConversionService mvcConversionService;

	private boolean entityWithDtoClassPresent;

	@Override
	public void setBeanClassLoader( ClassLoader classLoader ) {
		entityWithDtoClassPresent = ClassUtils.isPresent( "com.foreach.across.modules.hibernate.business.EntityWithDto", classLoader );
	}

	@SuppressWarnings("unchecked")
	public <T> void buildEntityModel( MutableEntityConfiguration<T> entityConfiguration ) {
		RepositoryFactoryInformation<T, Serializable> repositoryFactoryInformation
				= entityConfiguration.getAttribute( RepositoryFactoryInformation.class );
		Repository<T, ?> repository = entityConfiguration.getAttribute( Repository.class );

		DefaultEntityModel<T, Serializable> entityModel = new DefaultEntityModel<>();
		RepositoryInvoker repositoryInvoker = entityConfiguration.getAttribute( RepositoryInvoker.class );
		// todo: rework signature to optional
		entityModel.setFindOneMethod( id -> (T) repositoryInvoker.invokeFindById( id ).orElse( null ) );
		entityModel.setSaveMethod( repositoryInvoker::invokeSave );

		if ( repository instanceof CrudRepository ) {
			entityModel.setDeleteMethod( ( (CrudRepository) repository )::delete );
		}
		else {
			entityModel.setDeleteMethod( entity -> repositoryInvoker.invokeDeleteById( entityModel.getId( entity ) ) );
		}

		entityModel.setEntityFactory( createEntityFactory( repositoryFactoryInformation.getPersistentEntity() ) );
		entityModel.setEntityInformation( repositoryFactoryInformation.getEntityInformation() );
		entityModel.setLabelPrinter( createLabelPrinter( entityConfiguration.getPropertyRegistry() ) );

		entityConfiguration.setEntityModel( entityModel );
	}

	private Printer createLabelPrinter( EntityPropertyRegistry propertyRegistry ) {
		return new ConvertedValuePrinter(
				mvcConversionService, propertyRegistry.getProperty( EntityPropertyRegistry.LABEL )
		);
	}

	@SuppressWarnings("unchecked")
	private PersistentEntityFactory createEntityFactory( PersistentEntity persistentEntity ) {
		if ( entityWithDtoClassPresent ) {
			return new DtoAwarePersistentEntityFactory( persistentEntity );
		}
		return new PersistentEntityFactory( persistentEntity );
	}

}
