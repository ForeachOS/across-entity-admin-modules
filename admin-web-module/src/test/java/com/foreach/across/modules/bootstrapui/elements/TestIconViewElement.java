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

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.bootstrapui.BootstrapUiModule;
import com.foreach.across.test.support.AbstractViewElementTemplateTest;
import org.junit.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Arne Vandamme
 */
@ContextConfiguration
public class TestIconViewElement extends AbstractViewElementTemplateTest
{
	@Test
	public void simpleIcon() {
		renderAndExpect(
				new IconViewElement( GlyphIcon.ARROW_DOWN ),
				"<span class='glyphicon glyphicon-arrow-down' aria-hidden='true'></span>"
		);
	}

	@Test
	public void customTagName() {
		IconViewElement icon = new IconViewElement( GlyphIcon.SEARCH );
		icon.setTagName( "div" );

		renderAndExpect(
				icon,
				"<div class='glyphicon glyphicon-search' aria-hidden='true'></div>"
		);
	}

	@Test
	public void noGlyphResultsInNoElement() {
		renderAndExpect(
				new IconViewElement(),
				""
		);
	}

	@Test
	public void emptyGlyphGeneratesElement() {
		renderAndExpect(
				new IconViewElement( "" ),
				"<span class='glyphicon ' aria-hidden='true'></span>"
		);
	}

	@Configuration
	protected static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( new BootstrapUiModule() );
		}
	}
}
