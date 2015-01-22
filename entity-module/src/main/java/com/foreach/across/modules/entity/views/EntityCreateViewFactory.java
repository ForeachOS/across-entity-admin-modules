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

import com.foreach.across.modules.entity.business.EntityForm;
import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.registry.EntityModel;
import com.foreach.across.modules.entity.services.EntityFormFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Arne Vandamme
 */
public class EntityCreateViewFactory extends ConfigurablePropertiesEntityViewFactorySupport
{
	@Autowired
	private EntityFormFactory formFactory;

	@SuppressWarnings("unchecked")
	@Override
	protected void extendViewModel( EntityConfiguration entityConfiguration, EntityView view ) {
		EntityModel entityModel = entityConfiguration.getEntityModel();

		Object entity = retrieveOrCreateEntity( entityModel, view );
		view.setEntity( entity );

		EntityForm entityForm = formFactory.create( entityConfiguration );
		entityForm.setEntity( entity );

		view.addObject( "entityForm", entityForm );
		view.addObject( "existing", !entityModel.isNew( entity ) );
	}

	private Object retrieveOrCreateEntity( EntityModel entityModel, EntityView view ) {
		Object entity = view.getEntity();

		if ( entity == null ) {
			entity = entityModel.createNew();
		}

		return entity;
	}

	@Override
	protected EntityView createEntityView() {
		return new EntityCreateView();
	}
}
