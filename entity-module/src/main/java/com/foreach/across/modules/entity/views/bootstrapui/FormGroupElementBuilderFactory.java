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

import com.foreach.across.modules.bootstrapui.elements.BootstrapUiBuilders;
import com.foreach.across.modules.bootstrapui.elements.BootstrapUiElements;
import com.foreach.across.modules.bootstrapui.elements.FormControlElement;
import com.foreach.across.modules.bootstrapui.elements.StaticFormElement;
import com.foreach.across.modules.bootstrapui.elements.builder.FormGroupElementBuilder;
import com.foreach.across.modules.bootstrapui.elements.builder.LabelFormElementBuilder;
import com.foreach.across.modules.bootstrapui.elements.builder.OptionFormElementBuilder;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.views.EntityViewElementBuilderFactorySupport;
import com.foreach.across.modules.entity.views.EntityViewElementBuilderService;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.bootstrapui.processors.element.FormGroupDescriptionTextPostProcessor;
import com.foreach.across.modules.entity.views.bootstrapui.processors.element.FormGroupHelpTextPostProcessor;
import com.foreach.across.modules.entity.views.bootstrapui.processors.element.FormGroupTooltipTextPostProcessor;
import com.foreach.across.modules.entity.views.helpers.PropertyViewElementBuilderWrapper;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.foreach.across.modules.bootstrapui.elements.BootstrapUiBuilders.formGroup;

/**
 * @author Arne Vandamme
 */
@Component
@RequiredArgsConstructor
public class FormGroupElementBuilderFactory extends EntityViewElementBuilderFactorySupport<FormGroupElementBuilder>
{
	public static final String NAME_PREFIX = "formGroup-";

	private final EntityViewElementBuilderService entityViewElementBuilderService;

	@Override
	public boolean supports( String viewElementType ) {
		return BootstrapUiElements.FORM_GROUP.equals( viewElementType );
	}

	@Override
	protected FormGroupElementBuilder createInitialBuilder( EntityPropertyDescriptor propertyDescriptor,
	                                                        ViewElementMode viewElementMode,
	                                                        String viewElementType ) {
		ViewElementMode controlMode = ViewElementMode.CONTROL;

		if ( !propertyDescriptor.isWritable() || ViewElementMode.FORM_READ.equals( viewElementMode ) ) {
			controlMode = ViewElementMode.VALUE;
		}

		ViewElementBuilder controlBuilder = entityViewElementBuilderService.getElementBuilder( propertyDescriptor, controlMode );

		FormGroupElementBuilder formGroup = formGroup()
				.name( NAME_PREFIX + propertyDescriptor.getName() )
				.control( controlBuilder )
				.postProcessor( ( builderContext, element ) -> {
					FormControlElement control = element.getControl( FormControlElement.class );

					if ( control != null && control.isRequired() ) {
						element.setRequired( true );
					}
				} );

		// add form write post processors
		if ( ViewElementMode.FORM_WRITE.equals( viewElementMode.forSingle() ) ) {
			formGroup.postProcessor( new FormGroupDescriptionTextPostProcessor<>() )
			         .postProcessor( new FormGroupTooltipTextPostProcessor<>() )
			         .postProcessor( new FormGroupHelpTextPostProcessor<>() );
		}

		// todo: clean this up, work with separate control (?) allow list value to be without link, but other to be with
		if ( controlMode.equals( ViewElementMode.VALUE ) ) {
			formGroup.postProcessor( ( builderContext, element ) -> {
				StaticFormElement staticFormElement = new StaticFormElement();
				staticFormElement.addChild( element.getControl() );
				element.setControl( staticFormElement );
			} );
		}

		if ( !isRadioOrCheckboxControl( controlBuilder ) ) {
			ViewElementBuilder labelText = entityViewElementBuilderService.getElementBuilder( propertyDescriptor, ViewElementMode.LABEL );
			LabelFormElementBuilder labelBuilder = BootstrapUiBuilders.label().text( labelText );
			formGroup.label( labelBuilder );
		}

		return formGroup;
	}

	private boolean isRadioOrCheckboxControl( ViewElementBuilder builder ) {
		return PropertyViewElementBuilderWrapper.retrieveTargetBuilder( builder ) instanceof OptionFormElementBuilder;
	}
}
