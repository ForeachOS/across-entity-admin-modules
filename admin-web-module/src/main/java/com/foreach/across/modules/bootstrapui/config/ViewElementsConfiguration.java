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
package com.foreach.across.modules.bootstrapui.config;

import com.foreach.across.modules.bootstrapui.elements.ButtonViewElement;
import com.foreach.across.modules.bootstrapui.elements.IconViewElement;
import com.foreach.across.modules.bootstrapui.elements.thymeleaf.ButtonViewElementNodeBuilder;
import com.foreach.across.modules.bootstrapui.elements.thymeleaf.IconViewElementNodeBuilder;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementNodeBuilderRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author Arne Vandamme
 */
// todo: check Thymeleaf is enabled
@Configuration
public class ViewElementsConfiguration
{
	@Autowired
	private ViewElementNodeBuilderRegistry viewElementNodeBuilderRegistry;

	@PostConstruct
	public void registerViewElements() {
		viewElementNodeBuilderRegistry.registerNodeBuilder( IconViewElement.class, new IconViewElementNodeBuilder() );
		viewElementNodeBuilderRegistry.registerNodeBuilder( ButtonViewElement.class,
		                                                    new ButtonViewElementNodeBuilder() );
	}
}
