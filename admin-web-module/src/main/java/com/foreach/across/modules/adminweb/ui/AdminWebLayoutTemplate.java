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

package com.foreach.across.modules.adminweb.ui;

import com.foreach.across.modules.adminweb.AdminWeb;
import com.foreach.across.modules.adminweb.menu.AdminMenu;
import com.foreach.across.modules.adminweb.resource.AdminWebWebResources;
import com.foreach.across.modules.bootstrapui.components.BootstrapUiComponentFactory;
import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.web.menu.MenuFactory;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.template.LayoutTemplateProcessorAdapterBean;
import com.foreach.across.modules.web.template.WebTemplateRegistry;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Represents the admin web layout with top and left navigation.
 * Will also register static paths and admin web root path as javascript data values.
 *
 * @author Arne Vandamme
 * @since 2.1.0
 */
@ConditionalOnBean(SpringTemplateEngine.class)
@Component
public class AdminWebLayoutTemplate extends LayoutTemplateProcessorAdapterBean
{
	/**
	 * Nav positions used in this layout.
	 */
	public static final String NAVBAR = "navbar";
	public static final String NAVBAR_RIGHT = "navbar-right";
	public static final String SIDEBAR = "sidebar";

	/**
	 * Model attributes for different navigational components.
	 */
	public static final String MODEL_ATTR_NAVBAR = "adminWebNavbarNavigation";
	public static final String MODEL_ATTR_NAVBAR_RIGHT = "adminWebNavbarRightNavigation";
	public static final String MODEL_ATTR_SIDEBAR = "adminWebSidebarNavigation";
	public static final String MODEL_ATTR_BREADCRUMB = "adminWebBreadcrumb";

	private BootstrapUiComponentFactory bootstrapUiComponentFactory;

	/**
	 * Create a default template.
	 */
	public AdminWebLayoutTemplate() {
		this( AdminWeb.NAME, AdminWeb.LAYOUT_TEMPLATE );
	}

	/**
	 * Create a new {@link AdminWebLayoutTemplate} implementation with a specific name and layout template.
	 *
	 * @param name           of this layout
	 * @param layoutTemplate view template to use
	 */
	public AdminWebLayoutTemplate( String name, String layoutTemplate ) {
		super( name, layoutTemplate );
	}

	@Autowired
	void registerAdminWebLayoutTemplate( WebTemplateRegistry adminWebTemplateRegistry ) {
		adminWebTemplateRegistry.register( this );
	}

	@Override
	protected void registerWebResources( WebResourceRegistry registry ) {
		registry.addPackage( AdminWebWebResources.NAME );
		registry.addWithKey( WebResource.CSS, AdminWeb.MODULE, AdminWeb.LAYOUT_TEMPLATE_CSS, WebResource.VIEWS );
	}

	@Override
	protected void buildMenus( MenuFactory menuFactory ) {
		menuFactory.buildMenu( AdminMenu.NAME, AdminMenu.class );
	}

	@Override
	public void applyTemplate( HttpServletRequest request,
	                           HttpServletResponse response,
	                           Object handler,
	                           ModelAndView modelAndView ) {
		if ( modelAndView != null ) {
			Map<String, Object> model = modelAndView.getModel();
			AdminMenu adminMenu = (AdminMenu) request.getAttribute( AdminMenu.NAME );

			if ( adminMenu != null ) {
				model.computeIfAbsent(
						MODEL_ATTR_NAVBAR,
						key -> bootstrapUiComponentFactory.nav( adminMenu )
						                                  .navbar()
						                                  .keepGroupsAsGroup( true )
						                                  .replaceGroupBySelectedItem( false )
						                                  .filter( navPosition( NAVBAR, true ) )
						                                  .build()
				);
				model.computeIfAbsent(
						MODEL_ATTR_NAVBAR_RIGHT,
						key -> bootstrapUiComponentFactory.nav( adminMenu )
						                                  .navbar()
						                                  .css( "navbar-right" )
						                                  .keepGroupsAsGroup( true )
						                                  .replaceGroupBySelectedItem( false )
						                                  .filter( navPosition( NAVBAR_RIGHT, false ) )
						                                  .build()
				);
				model.computeIfAbsent(
						MODEL_ATTR_SIDEBAR,
						key -> bootstrapUiComponentFactory.panels( adminMenu )
						                                  .keepGroupsAsGroup( true )
						                                  .filter( navPosition( SIDEBAR, true ) )
						                                  .build()
				);
				model.computeIfAbsent(
						MODEL_ATTR_BREADCRUMB,
						key -> bootstrapUiComponentFactory
								.breadcrumb( adminMenu )
								.filter( item -> !Boolean.FALSE.equals( item.getAttribute( AdminMenu.ATTR_BREADCRUMB ) ) )
								.build()
				);
			}

			super.applyTemplate( request, response, handler, modelAndView );
		}
	}

	@SuppressWarnings("all")
	private Predicate<Menu> navPosition( String position, boolean defaultInclude ) {
		return menu ->
				Optional.ofNullable( menu.getAttribute( AdminMenu.ATTR_NAV_POSITION ) )
				        .map(
						        value -> value instanceof String[]
								        ? ArrayUtils.contains( (String[]) value, position )
								        : position.equals( value )
				        )
				        .orElse( defaultInclude || menu.getLevel() > 1 );
	}

	@Autowired
	void setBootstrapUiComponentFactory( BootstrapUiComponentFactory bootstrapUiComponentFactory ) {
		this.bootstrapUiComponentFactory = bootstrapUiComponentFactory;
	}
}

