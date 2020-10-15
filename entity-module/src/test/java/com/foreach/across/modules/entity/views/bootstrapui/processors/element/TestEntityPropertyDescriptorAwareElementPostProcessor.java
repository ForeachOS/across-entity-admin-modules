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
import com.foreach.across.modules.bootstrapui.elements.TextboxFormElement;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.web.ui.DefaultViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestEntityPropertyDescriptorAwareElementPostProcessor
{
	@Spy
	private AbstractPropertyDescriptorAwarePostProcessor<ViewElement, ConfigurablePlaceholderText> postProcessor =
			new AbstractPropertyDescriptorAwarePostProcessor<ViewElement, ConfigurablePlaceholderText>( ConfigurablePlaceholderText.class )
			{
				@Override
				protected void postProcess( ViewElementBuilderContext builderContext,
				                            ConfigurablePlaceholderText element,
				                            EntityPropertyDescriptor propertyDescriptor ) {

				}
			};

	private ViewElementBuilderContext builderContext = new DefaultViewElementBuilderContext();

	@Test
	public void neverForwardedIfNoPropertyDescriptor() {
		postProcessor.postProcess( builderContext, new TextViewElement() );
		verify( postProcessor, never() ).postProcess( eq( builderContext ), any(), any() );
	}

	@Test
	public void notForwardedIfNotAnInstanceOfType() {
		EntityPropertyDescriptor descriptor = mock( EntityPropertyDescriptor.class );
		builderContext.setAttribute( EntityPropertyDescriptor.class, descriptor );

		postProcessor.postProcess( builderContext, new TextViewElement() );
		verify( postProcessor, never() ).postProcess( eq( builderContext ), any(), any() );
	}

	@Test
	public void forwardedIfThereIsAPropertyDescriptor() {
		EntityPropertyDescriptor descriptor = mock( EntityPropertyDescriptor.class );
		builderContext.setAttribute( EntityPropertyDescriptor.class, descriptor );

		TextboxFormElement textbox = new TextboxFormElement();
		postProcessor.postProcess( builderContext, textbox );
		verify( postProcessor ).postProcess( builderContext, textbox, descriptor );
	}
}
