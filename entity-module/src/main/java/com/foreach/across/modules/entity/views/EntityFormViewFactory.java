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
package com.foreach.across.modules.entity.views;

import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.registry.EntityModel;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.services.EntityFormService;
import com.foreach.across.modules.entity.support.EntityMessageCodeResolver;
import com.foreach.across.modules.entity.views.elements.ViewElement;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Arne Vandamme
 */
public class EntityFormViewFactory extends ConfigurablePropertiesEntityViewFactorySupport<EntityFormView>
{
	@Autowired
	private EntityFormService formFactory;

	@SuppressWarnings("unchecked")
	@Override
	protected void extendViewModel( EntityConfiguration entityConfiguration, EntityFormView view ) {
		EntityModel entityModel = entityConfiguration.getEntityModel();

		Object entity = retrieveOrCreateEntity( entityModel, view );
		view.setEntity( entity );

		Object original = view.getOriginalEntity();

		if ( original == null ) {
			original = entity;
		}

		boolean newEntity = entityModel.isNew( entity );
		view.addObject( "existing", !newEntity );
		view.setFormAction( newEntity
				                    ? view.getEntityLinkBuilder().create()
				                    : view.getEntityLinkBuilder().update( original )
		);
	}

	private Object retrieveOrCreateEntity( EntityModel entityModel, EntityFormView view ) {
		Object entity = view.getEntity();

		if ( entity == null ) {
			entity = entityModel.createNew();
		}

		return entity;
	}

	@Override
	protected ViewElement createPropertyView( EntityConfiguration entityConfiguration,
	                                                    EntityPropertyDescriptor descriptor,
	                                                    EntityMessageCodeResolver messageCodeResolver ) {
		return formFactory.createFormElement( entityConfiguration, getPropertyRegistry(), descriptor,
		                                      messageCodeResolver );
	}

	@Override
	protected EntityFormView createEntityView() {
		return new EntityFormView();
	}
}
