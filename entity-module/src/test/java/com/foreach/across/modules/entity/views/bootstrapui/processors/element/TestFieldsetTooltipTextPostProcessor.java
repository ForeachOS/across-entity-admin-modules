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

import com.foreach.across.modules.bootstrapui.elements.FieldsetFormElement;
import com.foreach.across.modules.bootstrapui.elements.FormGroupElement;
import com.foreach.across.modules.bootstrapui.elements.tooltip.TooltipViewElement;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.web.ui.DefaultViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.elements.NodeViewElement;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestFieldsetTooltipTextPostProcessor
{
	@Mock
	private EntityPropertyDescriptor descriptor;

	@Spy
	private DefaultViewElementBuilderContext builderContext = new DefaultViewElementBuilderContext();

	private FieldsetFormElement fieldset = new FieldsetFormElement();
	private FieldsetTooltipTextPostProcessor<ViewElement> postProcessor = new FieldsetTooltipTextPostProcessor<>();
	
	@Before
	public void before() {
		when( descriptor.getName() ).thenReturn( "myprop" );
		builderContext.setAttribute( EntityPropertyDescriptor.class, descriptor );
	}

	@Test
	public void textNotSetIfEmpty() {
		when( builderContext.getMessage( "properties.myprop[tooltip]", "" ) ).thenReturn( "" );
		postProcessor.postProcess( builderContext, fieldset );
		assertThat( fieldset.getLegend().getChildren() ).isEmpty();
	}

	@Test
	public void resolvedMessageSupportsHtmlByDefault() {
		when( builderContext.getMessage( "properties.myprop[tooltip]", "" ) ).thenReturn( "description" );
		postProcessor.postProcess( builderContext, fieldset );
		assertThat( (TooltipViewElement) fieldset.getLegend().getChildren().get( 0 ) )
				.satisfies( text -> {
					assertThat( text.getText() ).isEqualTo( "description" );
					assertThat( text.isEscapeHtml() ).isFalse();
				} );
	}

	@Test
	public void resolvedMessageIsEscapedIfConfigured() {
		postProcessor = new FieldsetTooltipTextPostProcessor<>( true );
		when( builderContext.getMessage( "properties.myprop[tooltip]", "" ) ).thenReturn( "description" );
		postProcessor.postProcess( builderContext, fieldset );
		assertThat( (TooltipViewElement) fieldset.getLegend().getChildren().get( 0 ) )
				.satisfies( text -> {
					assertThat( text.getText() ).isEqualTo( "description" );
					assertThat( text.isEscapeHtml() ).isTrue();
				} );
	}
}
