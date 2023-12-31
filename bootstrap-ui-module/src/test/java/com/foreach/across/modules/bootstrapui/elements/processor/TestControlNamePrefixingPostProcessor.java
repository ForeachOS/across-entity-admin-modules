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
package com.foreach.across.modules.bootstrapui.elements.processor;

import com.foreach.across.modules.bootstrapui.elements.TextboxFormElement;
import com.foreach.across.modules.bootstrapui.elements.builder.TextboxFormElementBuilder;
import com.foreach.across.modules.web.ui.DefaultViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElementBuilderContextHolder;
import com.foreach.across.modules.web.ui.elements.builder.TextViewElementBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Arne Vandamme
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class TestControlNamePrefixingPostProcessor
{
	@BeforeEach
	public void before() {
		ViewElementBuilderContextHolder.setViewElementBuilderContext( new DefaultViewElementBuilderContext() );
	}

	@AfterEach
	public void after() {
		ViewElementBuilderContextHolder.clearViewElementBuilderContext();
	}

	@Test
	public void simplePrefixing() {
		ControlNamePrefixingPostProcessor processor = new ControlNamePrefixingPostProcessor( "test" );

		TextboxFormElement textbox = new TextboxFormElementBuilder()
				.controlName( "textbox" )
				.postProcessor( processor )
				.build();

		assertEquals( "test.textbox", textbox.getControlName() );
	}

	@Test
	public void indexPropertyPrefix() {
		ControlNamePrefixingPostProcessor processor = new ControlNamePrefixingPostProcessor( "test" );

		TextboxFormElement textbox = new TextboxFormElementBuilder()
				.controlName( "[textbox]" )
				.postProcessor( processor )
				.build();

		assertEquals( "test[textbox]", textbox.getControlName() );
	}

	@Test
	public void noControlNameSet() {
		ControlNamePrefixingPostProcessor processor = new ControlNamePrefixingPostProcessor( "test" );

		TextboxFormElement textbox = new TextboxFormElementBuilder()
				.postProcessor( processor )
				.build();

		assertNull( textbox.getControlName() );
	}

	@Test
	public void notAFormInputElement() {
		ControlNamePrefixingPostProcessor processor = new ControlNamePrefixingPostProcessor( "test" );

		new TextViewElementBuilder()
				.postProcessor( processor )
				.build();
	}

	@Test
	public void alreadyPrefixed() {
		ControlNamePrefixingPostProcessor processor = new ControlNamePrefixingPostProcessor( "test" );

		TextboxFormElement textbox = new TextboxFormElementBuilder()
				.controlName( "test.textbox" )
				.postProcessor( processor )
				.build();

		assertEquals( "test.textbox", textbox.getControlName() );
	}

	@Test
	public void alwaysPrefix() {
		ControlNamePrefixingPostProcessor processor = new ControlNamePrefixingPostProcessor( "test", true );

		TextboxFormElement textbox = new TextboxFormElementBuilder()
				.controlName( "test.textbox" )
				.postProcessor( processor )
				.build();

		assertEquals( "test.test.textbox", textbox.getControlName() );
	}

	@Test
	public void exactPrefix() {
		ControlNamePrefixingPostProcessor processor = new ControlNamePrefixingPostProcessor( "test" );
		processor.setExactPrefix( true );

		TextboxFormElement textbox = new TextboxFormElementBuilder()
				.controlName( "textbox" )
				.postProcessor( processor )
				.build();
		assertEquals( "testtextbox", textbox.getControlName() );

		textbox = new TextboxFormElementBuilder()
				.controlName( "[textbox]" )
				.postProcessor( processor )
				.build();
		assertEquals( "test[textbox]", textbox.getControlName() );
	}
}
