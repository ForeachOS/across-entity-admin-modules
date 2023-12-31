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
package com.foreach.across.modules.entity.views.bootstrapui;

import com.foreach.across.modules.bootstrapui.elements.BootstrapUiElements;
import com.foreach.across.modules.bootstrapui.elements.builder.OptionFormElementBuilder;
import com.foreach.across.modules.entity.conditionals.ConditionalOnBootstrapUI;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.views.EntityViewElementBuilderFactorySupport;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.bootstrapui.processors.element.EntityPropertyValueCheckboxPostProcessor;
import com.foreach.across.modules.entity.views.bootstrapui.processors.element.TextCodeResolverPostProcessor;
import com.foreach.across.modules.entity.views.processors.EntityQueryFilterProcessor;
import com.foreach.across.modules.entity.views.processors.query.EntityQueryFilterControlUtils;
import com.foreach.across.modules.entity.views.util.EntityViewElementUtils;
import com.foreach.across.modules.web.ui.ViewElementBuilderSupport;
import org.springframework.stereotype.Component;

import static com.foreach.across.modules.bootstrapui.ui.factories.BootstrapViewElements.bootstrap;

/**
 * Builds a {@link OptionFormElementBuilder} in the form of a checkbox for boolean attributes.
 *
 * @author Arne Vandamme
 */
@ConditionalOnBootstrapUI
@Component
public class CheckboxFormElementBuilderFactory extends EntityViewElementBuilderFactorySupport<OptionFormElementBuilder>
{
	@Override
	public boolean supports( String viewElementType ) {
		return BootstrapUiElements.CHECKBOX.equals( viewElementType );
	}

	@Override
	public OptionFormElementBuilder createInitialBuilder( EntityPropertyDescriptor descriptor, ViewElementMode viewElementMode, String viewElementType ) {
		return bootstrap.builders
				.checkbox()
				.name( descriptor.getName() )
				.controlName( descriptor.getName() )
				.text( descriptor.getDisplayName() )
				.value( "on" )
				.postProcessor( EntityViewElementUtils.controlNamePostProcessor( descriptor ) )
				.postProcessor( new EntityPropertyValueCheckboxPostProcessor( descriptor ) )
				.postProcessor( new TextCodeResolverPostProcessor<>( "properties." + descriptor.getName() ) )
				.postProcessor(
						( ( builderContext, element ) -> {
							if ( ViewElementMode.FILTER_CONTROL.equals( viewElementMode.forSingle() ) ) {
								element.setValue( true );
								element.addCssClass( EntityQueryFilterProcessor.ENTITY_QUERY_CONTROL_MARKER );
								EntityQueryFilterControlUtils.configureControlSettings(
										ViewElementBuilderSupport.ElementOrBuilder.wrap( element ), descriptor );
							}
						} )
				);
	}
}
