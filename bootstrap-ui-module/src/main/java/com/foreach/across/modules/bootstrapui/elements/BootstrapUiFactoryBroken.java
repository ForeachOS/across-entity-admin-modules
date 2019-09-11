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

import com.foreach.across.modules.bootstrapui.elements.autosuggest.AutoSuggestFormElementBuilder;
import com.foreach.across.modules.bootstrapui.elements.builder.*;
import com.foreach.across.modules.bootstrapui.ui.factories.BootstrapViewElements;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementBuilderFactory;
import com.foreach.across.modules.web.ui.elements.builder.NodeViewElementBuilder;

/**
 * @deprecated use the static facade {@link BootstrapViewElements#builders} instead
 */
@Deprecated
public interface BootstrapUiFactoryBroken extends ViewElementBuilderFactory
{
	HiddenFormElementBuilder hidden();

	NodeViewElementBuilder div();

	NodeViewElementBuilder span();

	NodeViewElementBuilder paragraph();

	LinkViewElementBuilder link();

	FormViewElementBuilder form();

	LabelFormElementBuilder label();

	LabelFormElementBuilder label( String labelText );

	FormGroupElementBuilder formGroup();

	FormGroupElementBuilder formGroup( ViewElementBuilder label, ViewElementBuilder control );

	ButtonViewElementBuilder button();

	FileUploadFormElementBuilder file();

	TextboxFormElementBuilder textbox();

	TextboxFormElementBuilder textarea();

	TextboxFormElementBuilder password();

	FieldsetFormElementBuilder fieldset();

	FieldsetFormElementBuilder fieldset( String legendText );

	TableViewElementBuilder table();

	TableViewElementBuilder.Header tableHeader();

	TableViewElementBuilder.Body tableBody();

	TableViewElementBuilder.Footer tableFooter();

	TableViewElementBuilder.Caption tableCaption();

	TableViewElementBuilder.Cell tableCell();

	TableViewElementBuilder.Cell tableHeaderCell();

	TableViewElementBuilder.Row tableRow();

	NodeViewElementBuilder row();

	NodeViewElementBuilder helpBlock();

	NodeViewElementBuilder helpBlock( String text );

	OptionsFormElementBuilder options();

	OptionFormElementBuilder option();

	OptionFormElementBuilder<CheckboxFormElement> checkbox();

	OptionFormElementBuilder<RadioFormElement> radio();

	ColumnViewElementBuilder column( Grid.DeviceGridLayout... layouts );

	InputGroupFormElementBuilderSupport inputGroup();

	InputGroupFormElementBuilderSupport inputGroup( ViewElementBuilder control );

	DateTimeFormElementBuilder datetime();

	NumericFormElementBuilder numeric();

	AlertViewElementBuilder alert();

	@Deprecated
	FaIcon faIcon( String icon );

	AutoSuggestFormElementBuilder autosuggest();
}
