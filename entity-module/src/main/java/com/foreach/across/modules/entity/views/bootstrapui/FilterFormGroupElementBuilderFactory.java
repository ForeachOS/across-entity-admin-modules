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

import com.foreach.across.modules.bootstrapui.elements.builder.FormGroupElementBuilder;
import com.foreach.across.modules.bootstrapui.elements.builder.OptionFormElementBuilder;
import com.foreach.across.modules.entity.conditionals.ConditionalOnBootstrapUI;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.views.EntityViewElementBuilderFactorySupport;
import com.foreach.across.modules.entity.views.EntityViewElementBuilderService;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.helpers.PropertyViewElementBuilderWrapper;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.foreach.across.modules.bootstrapui.ui.factories.BootstrapViewElements.bootstrap;

/**
 * Builds the simple form group for a filter control in a basic filter form.
 * The form group renders the label of the property and the {@link ViewElementMode#FILTER_CONTROL} as control.
 * Help text, tooltip text etc is all ignored.
 *
 * @author Arne Vandamme
 * @since 3.4.0
 */
@ConditionalOnBootstrapUI
@Component
@RequiredArgsConstructor
public class FilterFormGroupElementBuilderFactory extends EntityViewElementBuilderFactorySupport<FormGroupElementBuilder>
{
	/**
	 * View element type that this factory builds.
	 */
	public static final String VIEW_ELEMENT_TYPE = "filterFormGroup";

	private final EntityViewElementBuilderService entityViewElementBuilderService;

	@Override
	public boolean supports( String viewElementType ) {
		return VIEW_ELEMENT_TYPE.equals( viewElementType );
	}

	@Override
	protected FormGroupElementBuilder createInitialBuilder( EntityPropertyDescriptor propertyDescriptor,
	                                                        ViewElementMode viewElementMode,
	                                                        String viewElementType ) {
		ViewElementBuilder control
				= entityViewElementBuilderService.getElementBuilder( propertyDescriptor, determineControlViewElementMode( viewElementMode ) );

		FormGroupElementBuilder formGroupElementBuilder = bootstrap.builders.formGroup()
		                                                                    .data( "em-property", propertyDescriptor.getName() )
				.name( FormGroupElementBuilderFactory.NAME_PREFIX + propertyDescriptor.getName() )
		                                                                    .control( control );

		if ( !isRadioOrCheckboxControl( control ) ) {
			ViewElementBuilder labelText = entityViewElementBuilderService.getElementBuilder( propertyDescriptor, ViewElementMode.LABEL );
			formGroupElementBuilder.label( bootstrap.builders.label().text( labelText ) );
		}

		return formGroupElementBuilder;
	}

	private ViewElementMode determineControlViewElementMode( ViewElementMode formGroupElementMode ) {
		return formGroupElementMode.isForMultiple() ? ViewElementMode.FILTER_CONTROL.forMultiple() : ViewElementMode.FILTER_CONTROL;
	}

	private boolean isRadioOrCheckboxControl( ViewElementBuilder builder ) {
		return PropertyViewElementBuilderWrapper.retrieveTargetBuilder( builder ) instanceof OptionFormElementBuilder;
	}
}
