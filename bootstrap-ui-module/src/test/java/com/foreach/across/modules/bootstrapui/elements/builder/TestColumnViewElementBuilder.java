/*
 * Copyright 2019 the original author or authors
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

import com.foreach.across.modules.bootstrapui.elements.ColumnViewElement;
import com.foreach.across.modules.bootstrapui.elements.Grid;
import com.foreach.across.modules.web.ui.ViewElementBuilderFactory;
import com.foreach.across.test.support.AbstractViewElementBuilderTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Arne Vandamme
 */
public class TestColumnViewElementBuilder extends AbstractViewElementBuilderTest<ColumnViewElementBuilder, ColumnViewElement>
{
	@Override
	protected ColumnViewElementBuilder createBuilder( ViewElementBuilderFactory builderFactory ) {
		return new ColumnViewElementBuilder();
	}

	@Test
	public void addingCssApartFromLayouts() {
		builder.css( "my-css" )
		       .layout( Grid.Device.MD.width( 2 ) )
		       .css( "other-css" );

		build();

		assertEquals(
				"col-md-2 my-css other-css",
				element.getAttribute( "class" )
		);
	}
}
