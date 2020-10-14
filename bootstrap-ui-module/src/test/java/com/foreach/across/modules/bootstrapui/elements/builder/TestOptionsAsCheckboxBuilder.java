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

import com.foreach.across.modules.bootstrapui.elements.AbstractBootstrapViewElementTest;
import com.foreach.across.modules.web.ui.DefaultViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Arne Vandamme
 */
public class TestOptionsAsCheckboxBuilder extends AbstractBootstrapViewElementTest
{
	protected OptionsFormElementBuilder builder;

	protected ViewElementBuilderContext builderContext;

	@BeforeEach
	public void reset() {
		builderContext = new DefaultViewElementBuilderContext();

		builder = new OptionsFormElementBuilder().checkbox();
	}

	@Test
	public void simple() {
		builder.htmlId( "mybox" ).controlName( "boxName" );

		expect(
				"<div data-bootstrapui-adapter-type='container' id='mybox' class='checkbox-list'/>"
		);
	}

//	@Test
//	public void multiple() {
//		builder.multiple();
//
//		expect(
//				"<select class='form-control' multiple='multiple' />"
//		);
//	}
//
//	@Test
//	public void disabledAndReadonly() {
//		builder.disabled();
//
//		expect(
//				"<select class='form-control' disabled='disabled' />"
//		);
//
//		builder.disabled( false ).readonly();
//
//		expect(
//				"<select class='form-control' readonly='readonly' />"
//		);
//	}

	@Test
	public void options() {
		builder
				.controlName( "mybox" )
				.add(
						new OptionFormElementBuilder().text( "Inner text" ).value( "one" ).css( "one", "two" ).attribute( "data-role", "item" )
				)
				.add(
						new OptionFormElementBuilder().label( "Short two" ).text( "Some text" ).value( 2 ).selected().disabled()
				);

		expect(
				"<div id='options-mybox' data-bootstrapui-adapter-type='container' class='checkbox-list'>" +
						"<div class='one two custom-control custom-checkbox' data-role='item' data-bootstrapui-adapter-type='checkbox'>" +
						"<input type='checkbox' value='one' id='mybox' name='mybox' class='custom-control-input'></input>" +
						"<label for='mybox' class='custom-control-label'>Inner text</label>" +
						"<input type='hidden' name='_mybox' value='on' ></input></div>" +
						"<div class='custom-control custom-checkbox' data-bootstrapui-adapter-type='checkbox'>" +
						"<input type='checkbox' value='2' checked='checked' disabled='disabled' name='mybox' id='mybox1' class='custom-control-input' />" +
						"<label for='mybox1' class='custom-control-label'>Short two</label>" +
						"<input type='hidden' name='_mybox' value='on' disabled='disabled' ></input></div>" +
						"</div>"
		);
	}

	private void expect( String output ) {
		renderAndExpect( builder.build( builderContext ), output );
	}
}
