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
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

	private Date value;

	public DateTimeFormElement() {
		setAddonAfter( new GlyphIcon( GlyphIcon.CALENDAR ) );
		addCssClass( CSS_JS_CONTROL, CSS_DATE );
		setAttribute( ATTRIBUTE_DATA_DATEPICKER, new DateTimeFormElementConfiguration() );
	}

	public DateTimeFormElementConfiguration getConfiguration() {
		return getAttribute( ATTRIBUTE_DATA_DATEPICKER, DateTimeFormElementConfiguration.class );
	}

	public void setConfiguration( DateTimeFormElementConfiguration configuration ) {
		Assert.notNull( configuration );
		setAttribute( ATTRIBUTE_DATA_DATEPICKER, configuration );
	}

	public Date getValue() {
		return value;
	}

	public void setValue( Date value ) {
		this.value = value;
	}

	@Override
	public List<ViewElement> getChildren() {
		FormControlElement controlElement = getControl( FormControlElement.class );
		String controlName = controlElement.getControlName();

		if ( controlName != null && !StringUtils.equals( "_" + hidden.getControlName(), controlName ) ) {
			hidden.setControlName( controlName );
			controlElement.setHtmlId( controlElement.getHtmlId() );
			controlElement.setControlName( "_" + controlName );
		}

		if ( value != null ) {
			String dateAsString = DateTimeFormElementConfiguration.JAVA_FORMATTER.format( value );
			hidden.setValue( dateAsString );

			if ( controlElement instanceof ConfigurableTextViewElement ) {
				( (ConfigurableTextViewElement) controlElement ).setText( dateAsString );
			}
		}

		List<ViewElement> elements = new ArrayList<>();
		elements.addAll( super.getChildren() );
		elements.add( hidden );

		return elements;
	}
}
