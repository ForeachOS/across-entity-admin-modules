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
package com.foreach.across.modules.bootstrapui.elements;

import com.foreach.across.modules.bootstrapui.utils.BootstrapElementUtils;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Arne Vandamme
 */
public class TestNumericFormElement extends AbstractBootstrapViewElementTest
{
	private static final String DATA_ATTRIBUTE = "data-bootstrapui-numeric='{\"minimumValue\":-9223372036854775808,\"decimalPlaces\":2}'";

	private NumericFormElement numeric;

	@BeforeEach
	public void before() {
		numeric = new NumericFormElement();
	}

	@Test
	public void emptyNumeric() {
		renderAndExpect(
				numeric,
				"<input data-bootstrapui-adapter-type='basic' class='numeric form-control' type='text' />"
		);
	}

	@Test
	public void simpleNumericWithControlNameAndValue() {
		numeric.setControlName( "number" );
		numeric.setValue( 123L );
		assertEquals( "number", numeric.getControlName() );

		renderAndExpect(
				numeric,
				"<input data-bootstrapui-adapter-type='basic' id='number' name='number' class='numeric form-control' type='text' value='123' />"
		);
	}

	@Test
	public void withControlNameAndValue() {
		BigDecimal number = new BigDecimal( "123.9541" );
		numeric.setConfiguration( new NumericFormElementConfiguration() );
		numeric.setControlName( "number" );
		numeric.setValue( number );
		assertEquals( "number", numeric.getControlName() );

		renderAndExpect(
				numeric,
				"<input data-bootstrapui-adapter-type='numeric' id='number' name='_number' class='numeric form-control' " +
						"type='text' " + DATA_ATTRIBUTE + " value='123.9541' />" +
						"<input type='hidden' name='number' value='123.9541' />"
		);
	}

	@Test
	public void numericFormElementInFormGroup() {
		FormGroupElement group = new FormGroupElement();

		numeric.setName( "number" );

		LabelFormElement label = new LabelFormElement();
		label.setTarget( numeric );
		label.setText( "title" );

		group.setLabel( label );
		group.setControl( numeric );

		renderAndExpect(
				group,
				"<div class='form-group'>" +
						"<label for='number'>title</label>" +
						"<input data-bootstrapui-adapter-type='basic' id='number' name='number' class='numeric form-control' type='text' />" +
						"</div>"
		);
	}

	@Test
	public void updateControlName() {
		NumericFormElement control = new NumericFormElement();
		control.setControlName( "one" );
		render( control );
		control.setControlName( "two" );
		renderAndExpect(
				control,
				"<input data-bootstrapui-adapter-type='basic' type='text' class='numeric form-control' id='two' name='two' />"
		);

		assertEquals( "two", control.getControlName() );
	}

	@Test
	public void updateControlNameThroughContainer() {
		ContainerViewElement container = new ContainerViewElement();
		FormInputElement control = new NumericFormElement();
		control.setControlName( "one" );
		render( control );
		container.addChild( control );

		BootstrapElementUtils.prefixControlNames( "prefix.", container );

		renderAndExpect(
				control,
				"<input data-bootstrapui-adapter-type='basic' type='text' class='numeric form-control' id='prefix.one' name='prefix.one' />"
		);

		assertEquals( "prefix.one", control.getControlName() );
	}

	@Test
	public void updateControlNameThroughContainerWithElementConfiguration() {
		ContainerViewElement container = new ContainerViewElement();
		NumericFormElement control = new NumericFormElement();
		control.setConfiguration( new NumericFormElementConfiguration() );
		control.setControlName( "number" );
		control.setValue( 1433 );
		control.setHtmlId( "my-specific-id" );

		control.setControlName( "one" );
		render( control );
		container.addChild( control );
		control.setControlName( "two" );

		BootstrapElementUtils.prefixControlNames( "prefix.", container );

		renderAndExpect(
				control,
				"<input data-bootstrapui-adapter-type='numeric' id='my-specific-id' name='_prefix.two' class='numeric form-control' " +
						"type='text' " + DATA_ATTRIBUTE + " value='1433' />" +
						"<input type='hidden' name='prefix.two' value='1433' />"
		);

		assertEquals( "prefix.two", control.getControlName() );
	}
}
