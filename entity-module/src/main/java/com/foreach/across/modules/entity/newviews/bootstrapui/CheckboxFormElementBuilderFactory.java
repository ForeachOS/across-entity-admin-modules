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
package com.foreach.across.modules.entity.newviews.bootstrapui;

import com.foreach.across.modules.bootstrapui.elements.BootstrapUiElements;
import com.foreach.across.modules.bootstrapui.elements.BootstrapUiFactory;
import com.foreach.across.modules.bootstrapui.elements.CheckboxFormElement;
import com.foreach.across.modules.bootstrapui.elements.builder.OptionFormElementBuilder;
import com.foreach.across.modules.entity.newviews.EntityViewElementBuilderFactory;
import com.foreach.across.modules.entity.newviews.ViewElementMode;
import com.foreach.across.modules.entity.newviews.bootstrapui.processors.element.EntityPropertyControlPostProcessor;
import com.foreach.across.modules.entity.newviews.bootstrapui.processors.element.EntityPropertyValueCheckboxPostProcessor;
import com.foreach.across.modules.entity.newviews.bootstrapui.processors.element.TextCodeResolverPostProcessor;
import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyRegistry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Builds a {@link OptionFormElementBuilder} in the form of a checkbox for boolean attributes.
 *
 * @author Arne Vandamme
 */
public class CheckboxFormElementBuilderFactory implements EntityViewElementBuilderFactory<OptionFormElementBuilder>
{
	@Autowired
	private BootstrapUiFactory bootstrapUi;

	@Override
	public boolean supports( String viewElementType ) {
		return BootstrapUiElements.CHECKBOX.equals( viewElementType );
	}

	@Override
	public OptionFormElementBuilder createBuilder( EntityPropertyDescriptor descriptor,
	                                               EntityPropertyRegistry entityPropertyRegistry,
	                                               EntityConfiguration entityConfiguration,
	                                               ViewElementMode viewElementMode ) {
		return bootstrapUi.checkbox()
		                  .name( descriptor.getName() )
		                  .text( descriptor.getDisplayName() )
		                  .value( "on" )
		                  .postProcessor( new EntityPropertyControlPostProcessor<CheckboxFormElement>() )
		                  .postProcessor( new EntityPropertyValueCheckboxPostProcessor( descriptor ) )
		                  .postProcessor(
				                  new TextCodeResolverPostProcessor<CheckboxFormElement>(
						                  "properties." + descriptor.getName(),
						                  entityConfiguration.getEntityMessageCodeResolver()
				                  )
		                  );
	}
}
