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
import com.foreach.across.modules.web.ui.elements.AbstractNodeViewElement;

/**
 * @author Arne Vandamme
 */
public class FormGroupElement extends AbstractNodeViewElement
{
	public static final String ELEMENT_TYPE = BootstrapUiElements.FORM_GROUP;

	private ViewElement label, control, helpBlock;
	private FormLayout formLayout;
	private boolean required, renderHelpBlockBeforeControl;

	public FormGroupElement() {
		super( "div" );
		setElementType( ELEMENT_TYPE );
	}

	public ViewElement getLabel() {
		return label;
	}

	public void setLabel( ViewElement label ) {
		this.label = label;
	}

	public <V extends ViewElement> V getLabel( Class<V> elementType ) {
		return returnIfType( label, elementType );
	}

	public ViewElement getControl() {
		return getControl( ViewElement.class );
	}

	public void setControl( ViewElement control ) {
		this.control = control;
	}

	public <V extends ViewElement> V getControl( Class<V> elementType ) {
		return returnIfType( control, elementType );
	}

	public ViewElement getHelpBlock() {
		return getHelpBlock( ViewElement.class );
	}

	/**
	 * @param helpBlock view element
	 */
	public void setHelpBlock( ViewElement helpBlock ) {
		this.helpBlock = helpBlock;
	}

	public <V extends ViewElement> V getHelpBlock( Class<V> elementType ) {
		return returnIfType( helpBlock, elementType );
	}

	/**
	 * @return true if helpBlock should be rendered before the control (default: false)
	 */
	public boolean isRenderHelpBlockBeforeControl() {
		return renderHelpBlockBeforeControl;
	}

	public void setRenderHelpBlockBeforeControl( boolean renderHelpBlockBeforeControl ) {
		this.renderHelpBlockBeforeControl = renderHelpBlockBeforeControl;
	}

	public FormLayout getFormLayout() {
		return formLayout;
	}

	public void setFormLayout( FormLayout formLayout ) {
		this.formLayout = formLayout;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired( boolean required ) {
		this.required = required;
	}
}
