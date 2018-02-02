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

package com.foreach.across.modules.bootstrapui.components;

import com.foreach.across.modules.bootstrapui.components.builder.BreadcrumbNavComponentBuilder;
import com.foreach.across.modules.bootstrapui.components.builder.DefaultNavComponentBuilder;
import com.foreach.across.modules.bootstrapui.components.builder.PanelsNavComponentBuilder;
import com.foreach.across.modules.web.menu.Menu;
import org.springframework.stereotype.Service;

/**
 * @author Arne Vandamme
 * @since 1.0.0
 */
@Service
public class BootstrapUiComponentFactoryImpl implements BootstrapUiComponentFactory
{
	@Override
	public DefaultNavComponentBuilder nav( Menu menu ) {
		return new DefaultNavComponentBuilder().menu( menu );
	}

	@Override
	public PanelsNavComponentBuilder panels( Menu menu ) {
		return new PanelsNavComponentBuilder().menu( menu );
	}

	@Override
	public BreadcrumbNavComponentBuilder breadcrumb( Menu menu ) {
		return new BreadcrumbNavComponentBuilder().menu( menu );
	}
}
