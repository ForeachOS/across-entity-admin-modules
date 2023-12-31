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

package com.foreach.across.modules.entity.icons;

import com.foreach.across.modules.bootstrapui.elements.icons.IconSet;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.web.ui.elements.HtmlViewElement;

public class EntityModuleAutoSuggestIcons
{
	public final static String REMOVE_ITEM = "control-autosuggest-remove-item";

	public HtmlViewElement removeItem() {
		return IconSet.iconSet( EntityModule.NAME ).icon( REMOVE_ITEM );
	}
}
