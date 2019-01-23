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
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import liquibase.util.StringUtils;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Form input control that represents a formatted numeric input field, for example currency or percentage.
 *
 * @author Arne Vandamme
 */
public class NumericFormElement extends FormControlElementSupport implements FormControlElement.Proxy, ConfigurablePlaceholderText
{
	public static final String ATTRIBUTE_DATA_NUMERIC = "data-bootstrapui-numeric";

	public static final String CSS_NUMERIC = "numeric";

	private Number value;
	private boolean htmlIdSpecified = false;

	private final TextboxFormElement textbox = new TextboxFormElement();
	private final HiddenFormElement hidden = new HiddenFormElement();

	public NumericFormElement() {
		super( "input" );
		setElementType( ContainerViewElement.ELEMENT_TYPE );
		addCssClass( CSS_NUMERIC );
		addChild( textbox );
		setAttribute( BootstrapUiAttributes.CONTROL_ADAPTER_TYPE, "numeric" );
	}

	@Override
	public TextboxFormElement getControl() {
		return textbox;
	}

	public NumericFormElementConfiguration getConfiguration() {
		return textbox.getAttribute( ATTRIBUTE_DATA_NUMERIC, NumericFormElementConfiguration.class );
	}

	public void setConfiguration( @NonNull NumericFormElementConfiguration configuration ) {
		textbox.setAttribute( ATTRIBUTE_DATA_NUMERIC, configuration );
	}

	@Override
	public void setPlaceholder( String placeholder ) {
		textbox.setPlaceholder( placeholder );
	}

	@Override
	public String getPlaceholder() {
		return textbox.getPlaceholder();
	}

	@Override
	public boolean isReadonly() {
		return textbox.isReadonly();
	}

	@Override
	public void setReadonly( boolean readonly ) {
		textbox.setReadonly( readonly );
	}

	@Override
	public boolean isRequired() {
		return textbox.isRequired();
	}

	@Override
	public void setRequired( boolean required ) {
		textbox.setRequired( required );
	}

	@Override
	public String getControlName() {
		return hasConfiguration() ? hidden.getControlName() : textbox.getControlName();
	}

	@Override
	public void setControlName( String controlName ) {
		hidden.setControlName( controlName );
		textbox.setControlName( controlName );
	}

	@Override
	public boolean isDisabled() {
		return textbox.isDisabled();
	}

	@Override
	public void setDisabled( boolean disabled ) {
		textbox.setDisabled( disabled );
	}

	@Override
	public String getTagName() {
		return textbox.getTagName();
	}

	@Override
	public void addCssClass( String... cssClass ) {
		textbox.addCssClass( cssClass );
	}

	@Override
	public boolean hasCssClass( String cssClass ) {
		return textbox.hasCssClass( cssClass );
	}

	@Override
	public void removeCssClass( String... cssClass ) {
		textbox.removeCssClass( cssClass );
	}

	@Override
	public void setHtmlId( String id ) {
		htmlIdSpecified = StringUtils.isNotEmpty( id );
		textbox.setHtmlId( id );
	}

	@Override
	public String getHtmlId() {
		return textbox.getHtmlId();
	}

	@Override
	public Map<String, Object> getAttributes() {
		return textbox.getAttributes();
	}

	@Override
	public void setAttributes( Map<String, Object> attributes ) {
		textbox.setAttributes( attributes );
	}

	@Override
	public void setAttribute( String attributeName, Object attributeValue ) {
		textbox.setAttribute( attributeName, attributeValue );
	}

	@Override
	public void addAttributes( Map<String, Object> attributes ) {
		textbox.addAttributes( attributes );
	}

	@Override
	public void removeAttribute( String attributeName ) {
		textbox.removeAttribute( attributeName );
	}

	@Override
	public Object getAttribute( String attributeName ) {
		return textbox.getAttribute( attributeName );
	}

	@Override
	public <V> V getAttribute( String attributeName, Class<V> expectedType ) {
		return textbox.getAttribute( attributeName, expectedType );
	}

	@Override
	public boolean hasAttribute( String attributeName ) {
		return textbox.hasAttribute( attributeName );
	}

	public Number getValue() {
		return value;
	}

	public void setValue( Number value ) {
		this.value = value;

		textbox.setText( value != null ? Objects.toString( value ) : null );
	}

	@Override
	public List<ViewElement> getChildren() {
		List<ViewElement> children = new ArrayList<>( super.getChildren() );

		String controlName = getControlName();

		if ( hasConfiguration() && controlName != null ) {
			if ( !htmlIdSpecified ) {
				textbox.setHtmlId( hidden.getControlName() );
			}
			textbox.setControlName( "_" + hidden.getControlName() );
			hidden.setValue( textbox.getText() );

			children.add( hidden );
		}
		else {
			if ( !htmlIdSpecified ) {
				textbox.setHtmlId( getControlName() );
			}
			textbox.setControlName( getControlName() );
		}

		return children;
	}

	private boolean hasConfiguration() {
		return getConfiguration() != null;
	}
}
