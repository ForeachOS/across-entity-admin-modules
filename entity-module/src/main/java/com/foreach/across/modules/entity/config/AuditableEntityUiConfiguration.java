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
package com.foreach.across.modules.entity.config;

import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.newviews.ViewElementMode;
import com.foreach.across.modules.entity.newviews.bootstrapui.elements.builder.AuditablePropertyViewElementBuilder;
import com.foreach.across.modules.hibernate.business.Auditable;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;

/**
 * Configures custom {@link ViewElementBuilder} for created and last modified properties
 * of any {@link Auditable} entity.
 *
 * @author Arne Vandamme
 */
@Configuration
public class AuditableEntityUiConfiguration implements EntityConfigurer
{
	@Autowired
	private ConversionService mvcConversionService;

	@Override
	public void configure( EntitiesConfigurationBuilder configuration ) {
		configuration.assignableTo( Auditable.class )
		             .properties()
		             .property( "createdDate" )
		             .viewElementBuilder( ViewElementMode.LIST_VALUE, createdValueBuilder() )
		             .and()
		             .property( "lastModifiedDate" )
		             .viewElementBuilder( ViewElementMode.LIST_VALUE, lastModifiedValueBuilder() );
	}

	@Bean
	public ViewElementBuilder createdValueBuilder() {
		AuditablePropertyViewElementBuilder builder = new AuditablePropertyViewElementBuilder();
		builder.setConversionService( mvcConversionService );

		return builder;
	}

	@Bean
	public ViewElementBuilder lastModifiedValueBuilder() {
		AuditablePropertyViewElementBuilder builder = new AuditablePropertyViewElementBuilder();
		builder.setConversionService( mvcConversionService );
		builder.setForLastModifiedProperty( true );

		return builder;
	}
}
