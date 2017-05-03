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

package it.com.foreach.across.modules.entity.views.bootstrapui.options;

import com.foreach.across.modules.bootstrapui.elements.SelectFormElement;
import com.foreach.across.modules.bootstrapui.elements.builder.OptionFormElementBuilder;
import com.foreach.across.modules.bootstrapui.elements.builder.OptionsFormElementBuilder;
import com.foreach.across.modules.entity.views.bootstrapui.options.FixedOptionIterableBuilder;
import com.foreach.across.modules.entity.views.bootstrapui.options.OptionGenerator;
import com.foreach.across.modules.entity.views.bootstrapui.options.OptionIterableBuilder;
import com.foreach.across.modules.entity.views.support.ValueFetcher;
import com.foreach.across.modules.entity.web.EntityViewModel;
import com.foreach.across.modules.web.ui.DefaultViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 */
@RunWith(MockitoJUnitRunner.class)
public class TestOptionGenerator
{
	private final OptionIterableBuilder singleOption
			= new FixedOptionIterableBuilder( new OptionFormElementBuilder().label( "bbb" ).rawValue( 1L ) );

	private final OptionIterableBuilder noneSelected
			= new FixedOptionIterableBuilder( new OptionFormElementBuilder().label( "bbb" ).rawValue( 1L ),
			                                  new OptionFormElementBuilder().label( "aaa" ).rawValue( "2" )
	);

	private final OptionIterableBuilder withSelected
			= new FixedOptionIterableBuilder( new OptionFormElementBuilder().label( "bbb" ).rawValue( 1L ),
			                                  new OptionFormElementBuilder().label( "aaa" ).rawValue( "2" ).selected()
	);

	private OptionGenerator generator;
	private OptionsFormElementBuilder options;
	private ViewElementBuilderContext builderContext;

	@Mock
	private ValueFetcher<String> valueFetcher;

	@Before
	public void before() {
		generator = new OptionGenerator();
		options = new OptionsFormElementBuilder();

		builderContext = new DefaultViewElementBuilderContext();
		builderContext.setAttribute( OptionsFormElementBuilder.class, options );
	}

	@Test
	public void emptyOptionAddedIfRequiredAndNoneSelected() {
		options.select().required();

		generator.setOptions( noneSelected );

		List<SelectFormElement.Option> generated = build();
		assertEquals( 3, generated.size() );
		assertEquals( "", generated.get( 0 ).getLabel() );
		assertEquals( "bbb", generated.get( 1 ).getLabel() );
		assertEquals( "aaa", generated.get( 2 ).getLabel() );
	}

	@Test
	public void emptyOptionNotAddedAndOptionSelectedIfRequiredAndOnlyOneOption() {
		options.select().required();

		generator.setOptions( singleOption );

		List<SelectFormElement.Option> generated = build();
		assertEquals( 1, generated.size() );
		assertEquals( "bbb", generated.get( 0 ).getLabel() );
		assertTrue( generated.get( 0 ).isSelected() );
	}

	@Test
	public void emptyOptionNotAddedIfRequiredAndOneSelected() {
		options.select().required();
		generator.setOptions( withSelected );

		List<SelectFormElement.Option> generated = build();
		assertEquals( 2, generated.size() );
		assertEquals( "bbb", generated.get( 0 ).getLabel() );
		assertEquals( "aaa", generated.get( 1 ).getLabel() );
	}

	@Test
	public void emptyOptionAlwaysAddedIfNotRequired() {
		options.select();

		generator.setOptions( noneSelected );

		List<SelectFormElement.Option> generated = build();
		assertEquals( 3, generated.size() );
		assertEquals( "", generated.get( 0 ).getLabel() );
		assertEquals( "bbb", generated.get( 1 ).getLabel() );
		assertEquals( "aaa", generated.get( 2 ).getLabel() );

		generator.setOptions( withSelected );
		generated = build();

		assertEquals( 3, generated.size() );
		assertEquals( "", generated.get( 0 ).getLabel() );
		assertEquals( "bbb", generated.get( 1 ).getLabel() );
		assertEquals( "aaa", generated.get( 2 ).getLabel() );

		generator.setOptions( singleOption );
		generated = build();
		assertEquals( 2, generated.size() );
		assertEquals( "", generated.get( 0 ).getLabel() );
		assertEquals( "bbb", generated.get( 1 ).getLabel() );
	}

	@Test
	public void customEmptyOptionIsUsed() {
		options.select();

		generator.setEmptyOption( new OptionFormElementBuilder().label( "myemptyoption" ) );
		generator.setOptions( noneSelected );

		List<SelectFormElement.Option> generated = build();
		assertEquals( 3, generated.size() );
		assertEquals( "myemptyoption", generated.get( 0 ).getLabel() );
		assertEquals( "bbb", generated.get( 1 ).getLabel() );
		assertEquals( "aaa", generated.get( 2 ).getLabel() );
	}

	@Test
	public void nullEmptyOptionIsNeverAdded() {
		options.select();

		generator.setEmptyOption( null );
		generator.setOptions( noneSelected );

		List<SelectFormElement.Option> generated = build();
		assertEquals( 2, generated.size() );
		assertEquals( "bbb", generated.get( 0 ).getLabel() );
		assertEquals( "aaa", generated.get( 1 ).getLabel() );

		generator.setOptions( withSelected );
		generated = build();

		assertEquals( 2, generated.size() );
		assertEquals( "bbb", generated.get( 0 ).getLabel() );
		assertEquals( "aaa", generated.get( 1 ).getLabel() );
	}

	@Test
	public void emptyOptionIsNeverAddedInCaseOfCheckbox() {
		options.checkbox();
		generator.setOptions( noneSelected );

		List<SelectFormElement.Option> generated = build();
		assertEquals( 2, generated.size() );

		generator.setOptions( withSelected );
		generated = build();

		assertEquals( 2, generated.size() );
	}

	@Test
	public void emptyOptionsIsNeverAddedInCaseOfRadio() {
		options.radio();
		generator.setOptions( noneSelected );

		List<SelectFormElement.Option> generated = build();
		assertEquals( 2, generated.size() );

		generator.setOptions( withSelected );
		generated = build();

		assertEquals( 2, generated.size() );
	}

	@Test
	public void membersAreSortedByLabelAndTextIfEnabled() {
		options.select().required();

		generator.setOptions( noneSelected );
		generator.setSorted( true );

		List<SelectFormElement.Option> generated = build();
		assertEquals( 3, generated.size() );
		assertEquals( "", generated.get( 0 ).getLabel() );
		assertEquals( "aaa", generated.get( 1 ).getLabel() );
		assertEquals( "bbb", generated.get( 2 ).getLabel() );

		generator.setEmptyOption( null );
		generated = build();
		assertEquals( 2, generated.size() );
		assertEquals( "aaa", generated.get( 0 ).getLabel() );
		assertEquals( "bbb", generated.get( 1 ).getLabel() );
	}

	@Test
	public void noOptionSelected() {
		builderContext.setAttribute( EntityViewModel.ENTITY, "entity" );
		when( valueFetcher.getValue( "entity" ) ).thenReturn( null );

		generator.setValueFetcher( valueFetcher );
		generator.setSorted( true );
		generator.setEmptyOption( null );
		generator.setOptions( withSelected );
		List<SelectFormElement.Option> generated = build();

		assertEquals( 2, generated.size() );
		assertFalse( generated.get( 0 ).isSelected() );
		assertFalse( generated.get( 1 ).isSelected() );
	}

	@Test
	public void singleOptionSelected() {
		builderContext.setAttribute( EntityViewModel.ENTITY, "entity" );
		when( valueFetcher.getValue( "entity" ) ).thenReturn( 1L );

		generator.setValueFetcher( valueFetcher );
		generator.setSorted( true );
		generator.setEmptyOption( null );
		generator.setOptions( withSelected );
		List<SelectFormElement.Option> generated = build();

		assertEquals( 2, generated.size() );
		assertFalse( generated.get( 0 ).isSelected() );
		assertTrue( generated.get( 1 ).isSelected() );
	}

	@Test
	public void multipleOptionsSelectedAsCollection() {
		builderContext.setAttribute( EntityViewModel.ENTITY, "entity" );
		when( valueFetcher.getValue( "entity" ) ).thenReturn( Arrays.asList( "2", 1L ) );

		generator.setValueFetcher( valueFetcher );
		generator.setSorted( true );
		generator.setEmptyOption( null );
		generator.setOptions( noneSelected );
		List<SelectFormElement.Option> generated = build();

		assertEquals( 2, generated.size() );
		assertTrue( generated.get( 0 ).isSelected() );
		assertTrue( generated.get( 1 ).isSelected() );
	}

	@Test
	public void multipleOptionsSelectedAsArray() {
		builderContext.setAttribute( EntityViewModel.ENTITY, "entity" );
		when( valueFetcher.getValue( "entity" ) ).thenReturn( new Object[] { "2", 1L } );

		generator.setValueFetcher( valueFetcher );
		generator.setSorted( true );
		generator.setEmptyOption( null );
		generator.setOptions( noneSelected );
		List<SelectFormElement.Option> generated = build();

		assertEquals( 2, generated.size() );
		assertTrue( generated.get( 0 ).isSelected() );
		assertTrue( generated.get( 1 ).isSelected() );
	}

	@Test
	public void selfOptionNotIncludedIsDefault() {
		builderContext.setAttribute( EntityViewModel.ENTITY, "2" );

		generator.setSorted( true );
		generator.setEmptyOption( null );
		generator.setOptions( noneSelected );
		List<SelectFormElement.Option> generated = build();

		assertEquals( 1, generated.size() );
		assertEquals( "bbb", generated.get( 0 ).getLabel() );
	}

	@Test
	public void selfOptionIncluded() {
		builderContext.setAttribute( EntityViewModel.ENTITY, "2" );

		generator.setSorted( true );
		generator.setEmptyOption( null );
		generator.setOptions( noneSelected );
		generator.setSelfOptionIncluded( true );
		List<SelectFormElement.Option> generated = build();

		assertEquals( 2, generated.size() );
		assertEquals( "aaa", generated.get( 0 ).getLabel() );
		assertEquals( "bbb", generated.get( 1 ).getLabel() );
	}

	@SuppressWarnings("unchecked")
	private <U> List<U> build() {
		ContainerViewElement container = generator.build( builderContext );
		List<U> members = new ArrayList<>( container.getChildren().size() );

		for ( ViewElement element : container.getChildren() ) {
			members.add( (U) element );
		}

		return members;
	}
}
