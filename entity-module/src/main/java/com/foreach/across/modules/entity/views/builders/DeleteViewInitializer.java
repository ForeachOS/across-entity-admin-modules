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

package com.foreach.across.modules.entity.views.builders;

import com.foreach.across.modules.entity.config.builders.EntityViewFactoryBuilder;
import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyRegistryProvider;
import com.foreach.across.modules.entity.views.DefaultEntityViewFactory;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.processors.DeleteEntityViewProcessor;
import com.foreach.across.modules.entity.views.processors.GlobalPageFeedbackViewProcessor;
import com.foreach.across.modules.entity.views.processors.SingleEntityFormViewProcessor;
import com.foreach.across.modules.entity.views.processors.SingleEntityPageStructureViewProcessor;
import com.foreach.across.modules.entity.views.support.EntityMessages;
import com.foreach.across.modules.spring.security.actions.AllowableAction;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

/**
 * Configures a blank {@link EntityViewFactoryBuilder} for the {@link EntityView#DELETE_VIEW_NAME}.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
@Component
final class DeleteViewInitializer extends AbstractViewInitializer<EntityViewFactoryBuilder>
{
	public DeleteViewInitializer( AutowireCapableBeanFactory beanFactory,
	                              EntityPropertyRegistryProvider propertyRegistryProvider ) {
		super( beanFactory, propertyRegistryProvider );
	}

	@Override
	protected String templateName() {
		return EntityView.DELETE_VIEW_NAME;
	}

	@Override
	protected BiConsumer<EntityConfiguration<?>, EntityViewFactoryBuilder> createConfigurationInitializer() {
		return ( entityConfiguration, builder ) -> {
			builder.factoryType( DefaultEntityViewFactory.class )
			       .messagePrefix( "entityViews." + EntityView.DELETE_VIEW_NAME, "entityViews" )
			       .requiredAllowableAction( AllowableAction.DELETE )
			       .propertyRegistry( propertyRegistryProvider.createForParentRegistry( entityConfiguration.getPropertyRegistry() ) )
			       .viewProcessor( beanFactory.getBean( GlobalPageFeedbackViewProcessor.class ) );

			SingleEntityPageStructureViewProcessor pageStructureViewProcessor = beanFactory.createBean( SingleEntityPageStructureViewProcessor.class );
			pageStructureViewProcessor.setAddEntityMenu( true );
			pageStructureViewProcessor.setTitleMessageCode( EntityMessages.PAGE_TITLE_DELETE );
			builder.viewProcessor( pageStructureViewProcessor );

			SingleEntityFormViewProcessor formViewProcessor = beanFactory.createBean( SingleEntityFormViewProcessor.class );
			formViewProcessor.setAddDefaultButtons( true );
			formViewProcessor.setAddGlobalBindingErrors( true );
			builder.viewProcessor( formViewProcessor );

			DeleteEntityViewProcessor deleteEntityViewProcessor = beanFactory.createBean( DeleteEntityViewProcessor.class );
			builder.viewProcessor( deleteEntityViewProcessor );
		};
	}
}
