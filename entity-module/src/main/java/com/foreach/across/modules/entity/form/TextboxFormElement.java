package com.foreach.across.modules.entity.form;

import com.foreach.across.modules.entity.business.FormElement;
import com.foreach.across.modules.entity.business.FormPropertyDescriptor;

import java.beans.PropertyDescriptor;

public class TextboxFormElement implements FormElement
{
	private Object entity;

	private String name, label;
	private Object value;

	public TextboxFormElement( PropertyDescriptor propertyDescriptor ) {
		setName( propertyDescriptor.getName() );
		setLabel( propertyDescriptor.getDisplayName() );
	}

	public TextboxFormElement( FormPropertyDescriptor propertyDescriptor ) {
		setName( propertyDescriptor.getName() );
		setLabel( propertyDescriptor.getDisplayName() );
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel( String label ) {
		this.label = label;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public void setValue( Object value ) {
		this.value = value;
	}

	@Override
	public void setEntity( Object entity ) {
		this.entity = entity;
	}

	@Override
	public String getElementType() {
		return "textbox";
	}

}
