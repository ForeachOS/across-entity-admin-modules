package com.foreach.across.modules.entity.views.forms.elements.textbox;

import com.foreach.across.modules.entity.views.forms.elements.CommonFormElements;
import com.foreach.across.modules.entity.views.forms.elements.FormElementSupport;

/**
 * Represents a HTML "text" input type as well as "textarea" types.
 */
public class TextboxFormElement extends FormElementSupport
{
	public static final String TYPE = CommonFormElements.TEXTBOX;

	private Integer maxLength;
	private boolean multiLine = true;

	public TextboxFormElement() {
		super( TYPE );
	}

	public Integer getMaxLength() {
		return maxLength;
	}

	public void setMaxLength( Integer maxLength ) {
		this.maxLength = maxLength;
	}

	public boolean isMultiLine() {
		return multiLine;
	}

	public void setMultiLine( boolean multiLine ) {
		this.multiLine = multiLine;
	}
}
