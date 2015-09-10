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

/**
 * @author Arne Vandamme
 */
public interface FormControlElement extends FormInputElement
{
	boolean isReadonly();

	void setReadonly( boolean readonly );

	boolean isRequired();

	void setRequired( boolean required );

	/**
	 * Marks the implementation as being a proxy for the actual control that is
	 * to be one of its children.
	 */
	interface Proxy extends FormControlElement
	{
		/**
		 * @return the actual control
		 */
		ViewElement getControl();
	}
}
