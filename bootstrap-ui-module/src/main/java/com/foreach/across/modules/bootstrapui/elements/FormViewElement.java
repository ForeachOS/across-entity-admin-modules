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

import com.foreach.across.modules.web.ui.elements.AbstractNodeViewElement;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;

import static com.foreach.across.modules.bootstrapui.elements.FormLayout.Type.HORIZONTAL;
import static com.foreach.across.modules.bootstrapui.elements.FormLayout.Type.INLINE;

/**
 * Represents a HTML form element, supporting a Bootstrap {@link FormLayout} that will automatically
 * be applied to all controls of the form that do not have a separate layout specified.
 *
 * @author Arne Vandamme
 */
public class FormViewElement extends AbstractNodeViewElement
{
	public static final String ELEMENT_TYPE = BootstrapUiElements.FORM;

	public static final String ENCTYPE_PLAIN = "text/plain";
	public static final String ENCTYPE_MULTIPART = "multipart/form-data";
	public static final String ENCTYPE_URLENCODED = "application/x-www-form-urlencoded";

	private FormLayout formLayout;
	private String commandAttribute;

	public FormViewElement() {
		super( "form" );
		setElementType( ELEMENT_TYPE );

		setAttribute( "role", "form" );

		setMethod( HttpMethod.POST );
	}

	public FormLayout getFormLayout() {
		return formLayout;
	}

	/**
	 * Set the {@link FormLayout} that should be applied to all {@link FormGroupElement} members
	 * of this form.  Also set the corresponding class.
	 *
	 * @param formLayout instance
	 */
	public void setFormLayout( FormLayout formLayout ) {
		this.formLayout = formLayout;

		removeCssClass( "form-horizontal", "form-inline" );
		if ( formLayout != null ) {
			if ( HORIZONTAL.equals( formLayout.getType() ) ) {
				addCssClass( "form-horizontal" );
			}
			else if ( INLINE.equals( formLayout.getType() ) ) {
				addCssClass( "form-inline" );
			}
		}
	}

	public String getCommandAttribute() {
		return commandAttribute;
	}

	/**
	 * Set the attribute name of the command object that this form is for.  All form groups
	 * within this form will have the control names prefixed and field errors bound.
	 * This is basically the equivalent of putting a {@code th:object} attribute on an element in Thymeleaf.
	 *
	 * @param commandAttribute instance
	 */
	public void setCommandAttribute( String commandAttribute ) {
		this.commandAttribute = commandAttribute;
	}

	public HttpMethod getMethod() {
		return HttpMethod.valueOf( StringUtils.upperCase( (String) getAttribute( "method" ) ) );
	}

	public void setMethod( HttpMethod httpMethod ) {
		Assert.isTrue( httpMethod == HttpMethod.GET || httpMethod == HttpMethod.POST,
		               "Method POST or GET is required." );
		setAttribute( "method", StringUtils.lowerCase( httpMethod.toString() ) );
	}

	public String getAction() {
		return getAttribute( "action", String.class );
	}

	public void setAction( String url ) {
		setAttribute( "action", url );
	}

	@Override
	public void setName( String name ) {
		super.setName( name );

		if ( getFormName() == null ) {
			setFormName( name );
		}
	}

	/**
	 * @return name attribute of the form
	 */
	public String getFormName() {
		return getAttribute( "name", String.class );
	}

	/**
	 * Set the HTML name attribute of this form.  Defaults to {@link #getName()}.
	 *
	 * @param name attribute to use
	 */
	public void setFormName( String name ) {
		setAttribute( "name", name );
	}

	public String getEncType() {
		return getAttribute( "enctype", String.class );
	}

	/**
	 * Set encoding type (enctype attribute) to use for this form.
	 *
	 * @param encType value
	 */
	public void setEncType( String encType ) {
		setAttribute( "enctype", encType );
	}

	public String getAcceptCharSet() {
		return getAttribute( "accept-charset", String.class );
	}

	public void setAcceptCharSet( String charSet ) {
		setAttribute( "accept-charset", charSet );
	}

	public boolean isAutoComplete() {
		return !hasAttribute( "autocomplete" ) || StringUtils.equals( (String) getAttribute( "autocomplete" ), "on" );
	}

	public void setAutoComplete( boolean autoComplete ) {
		setAttribute( "autocomplete", autoComplete ? "on" : "off" );
	}

	public boolean isNoValidate() {
		return hasAttribute( "novalidate" );
	}

	public void setNoValidate( boolean noValidate ) {
		if ( noValidate ) {
			setAttribute( "novalidate", "novalidate" );
		}
		else {
			removeAttribute( "novalidate" );
		}
	}

	//form-inline
	//form-horizontal
}
