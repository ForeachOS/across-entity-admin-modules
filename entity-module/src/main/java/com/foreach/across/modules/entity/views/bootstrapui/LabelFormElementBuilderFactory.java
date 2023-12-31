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
import com.foreach.across.modules.entity.conditionals.ConditionalOnBootstrapUI;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.views.EntityViewElementBuilderFactorySupport;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.bootstrapui.processors.element.TextCodeResolverPostProcessor;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementPostProcessor;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import com.foreach.across.modules.web.ui.elements.builder.TextViewElementBuilder;
import org.springframework.stereotype.Component;

import static com.foreach.across.modules.bootstrapui.ui.factories.BootstrapViewElements.bootstrap;
import static com.foreach.across.modules.web.ui.elements.HtmlViewElements.html;

/**
 * Reponsible for creating either a {@link com.foreach.across.modules.bootstrapui.elements.builder.LabelFormElementBuilder}
 * or a simple {@link com.foreach.across.modules.web.ui.elements.builder.TextViewElementBuilder} for the label of a property.
 *
 * @author Arne Vandamme
 */
@ConditionalOnBootstrapUI
@Component
public class LabelFormElementBuilderFactory extends EntityViewElementBuilderFactorySupport<ViewElementBuilder>
{
	@Override
	public boolean supports( String viewElementType ) {
		return BootstrapUiElements.LABEL.equals( viewElementType );
	}

	@Override
	protected ViewElementBuilder createInitialBuilder( EntityPropertyDescriptor propertyDescriptor,
	                                                   ViewElementMode viewElementMode, String viewElementType ) {
		boolean labelTextOnly = ViewElementMode.isLabel( viewElementMode );

		TextViewElementBuilder labelText = html.builders
				.text( propertyDescriptor.getDisplayName() )
				.postProcessor( labelCodeResolver( propertyDescriptor ) );

		return labelTextOnly ? labelText : bootstrap.builders.label().add( labelText );
	}

	protected ViewElementPostProcessor<TextViewElement> labelCodeResolver( EntityPropertyDescriptor propertyDescriptor ) {
		return new TextCodeResolverPostProcessor<>( "properties." + propertyDescriptor.getName() );
	}
}
