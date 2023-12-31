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
package com.foreach.across.modules.entity.views.menu;

import com.foreach.across.modules.adminweb.AdminWeb;
import com.foreach.across.modules.entity.conditionals.ConditionalOnAdminWeb;
import com.foreach.across.modules.web.events.BuildMenuEvent;
import com.foreach.across.modules.web.menu.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Builder responsible for the {@link EntityAdminMenu}. Uses deprecated types from AdminWebModule,
 * in order to keep code backwards compatible. Ensures that any default url also gets an admin web
 * prefixed variant as it is expected most registered items are admin web relative paths.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
@ConditionalOnAdminWeb
@Component
@RequiredArgsConstructor
class EntityAdminMenuBuilder extends RequestMenuBuilder
{
	private final AdminWeb adminWeb;

	@Autowired
	@SuppressWarnings("all")
	void registerMenuBuilder( MenuFactory menuFactory ) {
		menuFactory.addMenuBuilder( this, EntityAdminMenu.class );
	}

	@Override
	public Menu build() {
		throw new UnsupportedOperationException( "Unable to auto build a generic menu." );
	}

	@Override
	@SuppressWarnings("unchecked")
	public BuildMenuEvent createEvent( Menu menu ) {
		PathBasedMenuBuilder menuBuilder = new PathBasedMenuBuilder( new PrefixContextMenuItemBuilderProcessor( adminWeb ) );
		return new EntityAdminMenuEvent( (EntityAdminMenu) menu, menuBuilder );
	}
}
