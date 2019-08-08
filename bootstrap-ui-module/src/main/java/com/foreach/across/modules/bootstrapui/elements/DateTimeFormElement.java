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

import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.elements.ConfigurableTextViewElement;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import lombok.NonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Consumer;

/**
 * Extension of an {@link InputGroupFormElement} that represents a date/time picker.
 * By default this is an input group with a calendar icon after the control element,
 * and a {@link TextboxFormElement} as control.
 *
 * @author Arne Vandamme
 */
public class DateTimeFormElement extends InputGroupFormElement
{
	public static final String ATTRIBUTE_DATA_DATEPICKER = "data-bootstrapui-datetimepicker";

	public static final String CSS_JS_CONTROL = "js-form-datetimepicker";
	public static final String CSS_DATE = "date";

	private final HiddenFormElement hidden = new HiddenFormElement();

	private LocalDateTime value;

	public DateTimeFormElement() {
		setAddonAfter( new GlyphIcon( GlyphIcon.CALENDAR ) );
		addCssClass( CSS_JS_CONTROL, CSS_DATE );
		setAttribute( ATTRIBUTE_DATA_DATEPICKER, new DateTimeFormElementConfiguration() );
		setAttribute( BootstrapUiViewElementAttributes.CONTROL_ADAPTER_TYPE, "datetime" );
	}

	public DateTimeFormElementConfiguration getConfiguration() {
		return getAttribute( ATTRIBUTE_DATA_DATEPICKER, DateTimeFormElementConfiguration.class );
	}

	public void setConfiguration( @NonNull DateTimeFormElementConfiguration configuration ) {
		setAttribute( ATTRIBUTE_DATA_DATEPICKER, configuration );
	}

	@Override
	public String getControlName() {
		return hidden.getControlName();
	}

	@Override
	public DateTimeFormElement setControlName( String controlName ) {
		hidden.setControlName( controlName );
		return this;
	}

	@Deprecated
	public Date getValue() {
		return getConfiguration().localDateTimeToDate( value );
	}

	@Deprecated
	public DateTimeFormElement setValue( Date value ) {
		setLocalDateTime( getConfiguration().dateToLocalDateTime( value ) );
		return this;
	}

	public DateTimeFormElement setLocalDate( LocalDate value ) {
		setLocalDateTime( DateTimeFormElementConfiguration.localDateToLocalDateTime( value ) );
		return this;
	}

	public LocalDate getLocalDate() {
		return value.toLocalDate();
	}

	public DateTimeFormElement setLocalTime( LocalTime value ) {
		setLocalDateTime( DateTimeFormElementConfiguration.localTimeToLocalDateTime( value ) );
		return this;
	}

	public LocalTime getLocalTime() {
		return value.toLocalTime();
	}

	public DateTimeFormElement setLocalDateTime( LocalDateTime value ) {
		this.value = value;
		return this;
	}

	public LocalDateTime getLocalDateTime() {
		return value;
	}

	@Override
	public List<ViewElement> getChildren() {
		FormControlElement controlElement = getControl( FormControlElement.class );
		controlElement.removeAttribute( BootstrapUiViewElementAttributes.CONTROL_ADAPTER_TYPE );
		String controlName = hidden.getControlName();

		if ( controlName != null ) {
			controlElement.setControlName( "_" + controlName );
			controlElement.setHtmlId( controlName );
		}
		else {
			controlElement.setControlName( null );
		}

		if ( value != null ) {
			String dateAsString = DateTimeFormElementConfiguration.JAVA_DATE_TIME_FORMATTER.format( value );
			hidden.setValue( dateAsString );

			if ( controlElement instanceof ConfigurableTextViewElement ) {
				( (ConfigurableTextViewElement) controlElement ).setText( dateAsString );
			}
		}

		List<ViewElement> elements = new ArrayList<>( super.getChildren() );
		elements.add( hidden );
		return elements;
	}

	@Override
	public DateTimeFormElement setAddonBefore( ViewElement addonBefore ) {
		super.setAddonBefore( addonBefore );
		return this;
	}

	@Override
	public DateTimeFormElement setAddonAfter( ViewElement addonAfter ) {
		super.setAddonAfter( addonAfter );
		return this;
	}

	@Override
	public DateTimeFormElement setControl( ViewElement control ) {
		super.setControl( control );
		return this;
	}

	@Override
	public DateTimeFormElement setPlaceholder( String placeholder ) {
		super.setPlaceholder( placeholder );
		return this;
	}

	@Override
	public DateTimeFormElement setDisabled( boolean disabled ) {
		super.setDisabled( disabled );
		return this;
	}

	@Override
	public DateTimeFormElement setReadonly( boolean readonly ) {
		super.setReadonly( readonly );
		return this;
	}

	@Override
	public DateTimeFormElement setRequired( boolean required ) {
		super.setRequired( required );
		return this;
	}

	@Override
	public DateTimeFormElement addCssClass( String... cssClass ) {
		super.addCssClass( cssClass );
		return this;
	}

	@Override
	public DateTimeFormElement removeCssClass( String... cssClass ) {
		super.removeCssClass( cssClass );
		return this;
	}

	@Override
	public DateTimeFormElement setAttributes( Map<String, Object> attributes ) {
		super.setAttributes( attributes );
		return this;
	}

	@Override
	public DateTimeFormElement setAttribute( String attributeName, Object attributeValue ) {
		super.setAttribute( attributeName, attributeValue );
		return this;
	}

	@Override
	public DateTimeFormElement addAttributes( Map<String, Object> attributes ) {
		super.addAttributes( attributes );
		return this;
	}

	@Override
	public DateTimeFormElement removeAttribute( String attributeName ) {
		super.removeAttribute( attributeName );
		return this;
	}

	@Override
	public DateTimeFormElement setName( String name ) {
		super.setName( name );
		return this;
	}

	@Override
	public DateTimeFormElement setCustomTemplate( String customTemplate ) {
		super.setCustomTemplate( customTemplate );
		return this;
	}

	@Override
	protected DateTimeFormElement setElementType( String elementType ) {
		super.setElementType( elementType );
		return this;
	}

	@Override
	public DateTimeFormElement addChild( ViewElement element ) {
		super.addChild( element );
		return this;
	}

	@Override
	public DateTimeFormElement addChildren( Collection<? extends ViewElement> elements ) {
		super.addChildren( elements );
		return this;
	}

	@Override
	public DateTimeFormElement addFirstChild( ViewElement element ) {
		super.addFirstChild( element );
		return this;
	}

	@Override
	public DateTimeFormElement clearChildren() {
		super.clearChildren();
		return this;
	}

	@Override
	public DateTimeFormElement apply( Consumer<ContainerViewElement> consumer ) {
		super.apply( consumer );
		return this;
	}

	@Override
	public <U extends ViewElement> DateTimeFormElement applyUnsafe( Consumer<U> consumer ) {
		super.applyUnsafe( consumer );
		return this;
	}

	@Override
	protected DateTimeFormElement setTagName( String tagName ) {
		super.setTagName( tagName );
		return this;
	}

	@Override
	public DateTimeFormElement setHtmlId( String htmlId ) {
		super.setHtmlId( htmlId );
		return this;
	}
}
