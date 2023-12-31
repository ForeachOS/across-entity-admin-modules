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
import com.foreach.across.modules.bootstrapui.elements.StaticFormElement;
import com.foreach.across.modules.bootstrapui.elements.builder.FormGroupElementBuilder;
import com.foreach.across.modules.bootstrapui.elements.builder.LabelFormElementBuilder;
import com.foreach.across.modules.bootstrapui.elements.builder.OptionFormElementBuilder;
import com.foreach.across.modules.entity.conditionals.ConditionalOnBootstrapUI;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.views.EntityViewElementBuilderFactorySupport;
import com.foreach.across.modules.entity.views.EntityViewElementBuilderService;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.bootstrapui.processors.element.*;
import com.foreach.across.modules.entity.views.helpers.PropertyViewElementBuilderWrapper;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.elements.HtmlViewElement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.foreach.across.modules.bootstrapui.ui.factories.BootstrapViewElements.bootstrap;

/**
 * @author Arne Vandamme
 */
@ConditionalOnBootstrapUI
@Component
@RequiredArgsConstructor
public class FormGroupElementBuilderFactory extends EntityViewElementBuilderFactorySupport<FormGroupElementBuilder>
{
	public static final String LABEL_CHILD_MODE = "label";
	public static final String CONTROL_CHILD_MODE = "control";

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
		ViewElementMode controlMode = resolveControlMode( propertyDescriptor, viewElementMode );
		ViewElementMode labelMode = resolveLabelMode( propertyDescriptor, viewElementMode );

		ViewElementBuilder controlBuilder = entityViewElementBuilderService.getElementBuilder( propertyDescriptor, controlMode );

		FormGroupElementBuilder formGroup = bootstrap.builders
				.formGroup()
				.name( NAME_PREFIX + propertyDescriptor.getName() )
				.data( "em-property", propertyDescriptor.getName() )
				.control( controlBuilder );

		// add form write post processors
		if ( ViewElementMode.FORM_WRITE.matchesSingleTypeOf( viewElementMode ) ) {
			formGroup.postProcessor( new FormGroupRequiredPostProcessor<>() )
			         .postProcessor( new FormGroupDescriptionTextPostProcessor<>() )
			         .postProcessor( new FormGroupTooltipTextPostProcessor<>() )
			         .postProcessor( new FormGroupHelpTextPostProcessor<>() )
			         .postProcessor( new FormGroupFieldErrorsPostProcessor<>() );
		}

		// todo: clean this up, work with separate control (?) allow list value to be without link, but other to be with
		if ( controlMode.matchesSingleTypeOf( ViewElementMode.VALUE ) ) {
			formGroup.postProcessor( ( builderContext, element ) -> {
				ViewElement control = element.getControl();
				if ( !( control instanceof HtmlViewElement ) ) {
					StaticFormElement staticFormElement = new StaticFormElement();
					staticFormElement.addChild( control );
					element.setControl( staticFormElement );
				}
			} );
		}

		if ( !isRadioOrCheckboxControl( controlBuilder ) ) {
			ViewElementBuilder labelText = entityViewElementBuilderService.getElementBuilder( propertyDescriptor, labelMode );
			LabelFormElementBuilder labelBuilder = bootstrap.builders.label().text( labelText );
			formGroup.label( labelBuilder );
		}

		return formGroup;
	}

	protected ViewElementMode resolveControlMode( EntityPropertyDescriptor propertyDescriptor, ViewElementMode viewElementMode ) {
		ViewElementMode controlMode = ViewElementMode.CONTROL;

		if ( !propertyDescriptor.isWritable() || ViewElementMode.FORM_READ.equals( viewElementMode ) ) {
			controlMode = ViewElementMode.VALUE;
		}

		return viewElementMode.getChildMode( CONTROL_CHILD_MODE, controlMode );
	}

	@SuppressWarnings( "unused" )
	protected ViewElementMode resolveLabelMode( EntityPropertyDescriptor propertyDescriptor, ViewElementMode viewElementMode ) {
		return viewElementMode.getChildMode( LABEL_CHILD_MODE, ViewElementMode.LABEL );
	}

	private boolean isRadioOrCheckboxControl( ViewElementBuilder builder ) {
		return PropertyViewElementBuilderWrapper.retrieveTargetBuilder( builder ) instanceof OptionFormElementBuilder;
	}
}
