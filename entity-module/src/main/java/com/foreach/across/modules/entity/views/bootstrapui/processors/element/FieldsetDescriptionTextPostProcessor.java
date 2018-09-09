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

package com.foreach.across.modules.entity.views.bootstrapui.processors.element;

import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.views.bootstrapui.elements.ViewElementFieldset;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import static com.foreach.across.modules.bootstrapui.elements.BootstrapUiBuilders.helpBlock;
import static com.foreach.across.modules.bootstrapui.elements.builder.FormGroupElementBuilder.CSS_FORM_TEXT_DESCRIPTION;

/**
 * Post-processor that resolves a description text for a current property and a {@link com.foreach.across.modules.entity.views.bootstrapui.elements.ViewElementFieldset}.
 * By default supports HTML in the resulting message. The description text is added in the header of the fieldset.
 * <p/>
 * This post processor is usually registered automatically when rendering {@link com.foreach.across.modules.entity.views.ViewElementMode#FORM_WRITE}.
 *
 * @author Arne Vandamme
 * @see FormGroupDescriptionTextPostProcessor
 * @since 3.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
public class FieldsetDescriptionTextPostProcessor<T extends ViewElement> extends AbstractPropertyDescriptorAwarePostProcessor<T, ViewElementFieldset>
{
	/**
	 * -- SETTER --
	 * Should HTML be escaped.
	 */
	private boolean escapeHtml;

	@Override
	protected void postProcess( ViewElementBuilderContext builderContext, ViewElementFieldset element, EntityPropertyDescriptor propertyDescriptor ) {
		val text = builderContext.getMessage( "properties." + propertyDescriptor.getName() + "[description]", "" );

		if ( !StringUtils.isEmpty( text ) ) {
			element.getHeader()
			       .addChild(
					       helpBlock()
							       .css( CSS_FORM_TEXT_DESCRIPTION )
							       .add( new TextViewElement( text, escapeHtml ) )
							       .build( builderContext )
			       );
		}
	}
}
