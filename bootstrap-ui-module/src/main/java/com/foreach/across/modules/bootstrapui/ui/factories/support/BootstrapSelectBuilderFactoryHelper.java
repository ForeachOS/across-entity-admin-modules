/*
 * Copyright 2019 the original author or authors
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

package com.foreach.across.modules.bootstrapui.ui.factories.support;

import com.foreach.across.modules.bootstrapui.ui.factories.BootstrapViewElementBuilders;
import com.foreach.across.modules.bootstrapui.elements.builder.OptionFormElementBuilder;
import com.foreach.across.modules.web.ui.ViewElement;

/**
 * Support class used by {@link BootstrapViewElementBuilders}
 *
 * @author Stijn Vanhoof
 * @since 3.0.0
 */
public class BootstrapSelectBuilderFactoryHelper
{
	/**
	 * Support class used by {@link BootstrapViewElementBuilders}
	 * <p>
	 * Build a {@link com.foreach.across.modules.web.ui.ViewElementBuilder} that will build a single select option.
	 */
	public OptionFormElementBuilder option() {
		return new OptionFormElementBuilder();
	}

	public OptionFormElementBuilder option( ViewElement.WitherSetter... setters ) {
		return option().with( setters );
	}
}
