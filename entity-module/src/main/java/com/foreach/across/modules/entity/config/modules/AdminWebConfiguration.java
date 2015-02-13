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
package com.foreach.across.modules.entity.config.modules;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.PostProcessor;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.controllers.EntityControllerAttributes;
import com.foreach.across.modules.entity.controllers.association.AssociatedEntityCreateController;
import com.foreach.across.modules.entity.controllers.association.AssociatedEntityListController;
import com.foreach.across.modules.entity.controllers.association.AssociatedEntityUpdateController;
import com.foreach.across.modules.entity.controllers.entity.EntityCreateController;
import com.foreach.across.modules.entity.controllers.entity.EntityListController;
import com.foreach.across.modules.entity.controllers.entity.EntityUpdateController;
import com.foreach.across.modules.entity.handlers.MenuEventsHandler;
import com.foreach.across.modules.entity.registry.EntityAssociation;
import com.foreach.across.modules.entity.registry.MutableEntityAssociation;
import com.foreach.across.modules.entity.registry.MutableEntityConfiguration;
import com.foreach.across.modules.entity.web.AssociatedEntityLinkBuilder;
import com.foreach.across.modules.entity.web.EntityLinkBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AcrossDepends(required = "AdminWebModule")
@Configuration
public class AdminWebConfiguration implements EntityConfigurer
{
	@Bean
	public MenuEventsHandler menuEventsHandler() {
		return new MenuEventsHandler();
	}

	@Bean
	public EntityListController entityController() {
		return new EntityListController();
	}

	@Bean
	public EntityCreateController entityCreateController() {
		return new EntityCreateController();
	}

	@Bean
	public EntityUpdateController entitySaveController() {
		return new EntityUpdateController();
	}

	@Bean
	public AssociatedEntityListController associatedEntityController() {
		return new AssociatedEntityListController();
	}

	@Bean
	public AssociatedEntityCreateController associatedEntityCreateController() {
		return new AssociatedEntityCreateController();
	}

	@Bean
	public AssociatedEntityUpdateController associatedEntityUpdateController() {
		return new AssociatedEntityUpdateController();
	}

	@Override
	public void configure( EntitiesConfigurationBuilder configuration ) {
		configuration.addPostProcessor( new PostProcessor<MutableEntityConfiguration<?>>()
		{
			@Override
			public MutableEntityConfiguration process( MutableEntityConfiguration<?> configuration ) {
				configuration.addAttribute( EntityLinkBuilder.class,
				                            new EntityLinkBuilder( EntityControllerAttributes.ROOT_PATH, configuration ) );
				return configuration;
			}
		} );

		// Create associations
		configuration.addPostProcessor( new PostProcessor<MutableEntityConfiguration<?>>()
		{
			@Override
			public MutableEntityConfiguration process( MutableEntityConfiguration<?> configuration ) {
				EntityLinkBuilder parentLinkBuilder = configuration.getAttribute( EntityLinkBuilder.class );
				for ( EntityAssociation association : configuration.getAssociations() ) {
					EntityLinkBuilder originalLinkBuilder = association.getAssociatedEntityConfiguration()
					                                                   .getAttribute( EntityLinkBuilder.class );

					AssociatedEntityLinkBuilder associationLinkBuilder
							= new AssociatedEntityLinkBuilder( parentLinkBuilder, originalLinkBuilder );

					MutableEntityAssociation mutable = configuration.association( association.getEntityType() );
					mutable.addAttribute( AssociatedEntityLinkBuilder.class, associationLinkBuilder );
				}

				return configuration;
			}
		} );
	}
}
