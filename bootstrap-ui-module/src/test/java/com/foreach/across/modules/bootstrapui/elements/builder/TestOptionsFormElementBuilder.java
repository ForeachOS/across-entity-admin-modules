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
package com.foreach.across.modules.bootstrapui.elements.builder;

import com.foreach.across.modules.web.ui.ViewElementBuilderFactory;
import com.foreach.across.modules.web.ui.elements.AbstractNodeViewElement;
import com.foreach.across.test.support.AbstractViewElementBuilderTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 */
public class TestOptionsFormElementBuilder extends AbstractViewElementBuilderTest<OptionsFormElementBuilder, AbstractNodeViewElement>
{
	@Override
	protected OptionsFormElementBuilder createBuilder( ViewElementBuilderFactory builderFactory ) {
		return new OptionsFormElementBuilder();
	}

	@Test
	public void noNestingOfOptionsFormElementBuilders() {
		assertThrows( IllegalStateException.class, () -> {
			when( builderContext.hasAttribute( OptionsFormElementBuilder.class ) ).thenReturn( true );

			build();
		} );
	}
}
