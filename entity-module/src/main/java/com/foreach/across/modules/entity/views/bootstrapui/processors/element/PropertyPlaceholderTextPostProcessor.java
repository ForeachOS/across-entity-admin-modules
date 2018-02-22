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

import com.foreach.across.modules.bootstrapui.elements.ConfigurablePlaceholderText;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;

/**
 * If the target element is a {@link com.foreach.across.modules.bootstrapui.elements.ConfigurablePlaceholderText}
 * element and there is no placeholder yet, this processor resolves the property specific placeholder message and sets it.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
public class PropertyPlaceholderTextPostProcessor<T extends ViewElement> extends AbstractPropertyDescriptorAwarePostProcessor<T, ConfigurablePlaceholderText>
{
	@Override
	protected void postProcess( ViewElementBuilderContext builderContext, ConfigurablePlaceholderText element, EntityPropertyDescriptor propertyDescriptor ) {
		if ( element.getPlaceholder() == null ) {
			String messageCode = "properties." + propertyDescriptor.getName() + "[placeholder]";
			element.setPlaceholder( builderContext.getMessage( messageCode, "" ) );
		}
	}
}
