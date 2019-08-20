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

package com.foreach.across.modules.bootstrapui.elements.icons;

import com.foreach.across.modules.web.ui.elements.AbstractNodeViewElement;
import com.foreach.across.test.support.AbstractViewElementTemplateTest;
import org.junit.Before;
import org.junit.Test;

import static com.foreach.across.modules.web.ui.elements.HtmlViewElement.Functions.css;
import static com.foreach.across.modules.web.ui.elements.HtmlViewElements.i;

/**
 * @author Stijn Vanhoof
 */
public class TestIconSet extends AbstractViewElementTemplateTest
{
	private String customFontAwesomeIconSet = "custom-fontawesome-solid";

	@Before
	public void setup() {
		IconSets.add( customFontAwesomeIconSet, new IconSet( ( iconName ) -> i( css( "fas fa-" + iconName ) ) ) );
	}

	@Test
	public void defaultIcon() {
		AbstractNodeViewElement icon = IconSets.iconSet( customFontAwesomeIconSet ).icon( "edit" );
		renderAndExpect( icon, "<i class=\"fas fa-edit\"></i>" );
	}

	@Test
	public void customIcon() {
		AbstractNodeViewElement icon = IconSets.iconSet( customFontAwesomeIconSet ).icon( "save" );
		renderAndExpect( icon, "<i class=\"fas fa-save\"></i>" );

		IconSets.iconSet( customFontAwesomeIconSet ).add( "save", ( iconName ) -> i( css( "fas fa-floppy" ) ) );
		icon = IconSets.iconSet( customFontAwesomeIconSet ).icon( "save" );
		renderAndExpect( icon, "<i class=\"fas fa-floppy\"></i>" );
	}
}
