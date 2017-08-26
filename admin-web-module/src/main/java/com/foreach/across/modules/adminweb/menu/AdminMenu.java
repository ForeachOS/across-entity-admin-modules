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

package com.foreach.across.modules.adminweb.menu;

import com.foreach.across.modules.web.menu.Menu;

public class AdminMenu extends Menu
{
	public static final String NAME = "adminMenu";

	/**
	 * Attribute that will be used to determine the position for the menu item.
	 * Actual interpretation depends on the layout template being used, but the default
	 * {@link com.foreach.across.modules.adminweb.ui.AdminWebLayoutTemplate} supports either
	 * a string or an array of strings as value.
	 */
	public static final String ATTR_NAV_POSITION = "adminMenu:position";

	/**
	 * Attribute that will be used to determine if a menu item should be included in breadcrumb generation.
	 * Items with an explicit value of {@code false} will not be rendered.
	 */
	public static final String ATTR_BREADCRUMB = "adminMenu:breadcrumb";

	public AdminMenu() {
		super( NAME );
	}

	/**
	 * Shortcut to specify the leaf point of the current breadcrumb by adding an additional
	 * item to the path.
	 *
	 * @param title text for the leaf.
	 */
	public void breadcrumbLeaf( String title ) {
		getLowestSelectedItem().addItem( "/breadcrumbLeaf", title ).setSelected( true );
	}
}
