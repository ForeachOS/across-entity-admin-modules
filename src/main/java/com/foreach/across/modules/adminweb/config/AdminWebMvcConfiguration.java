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
package com.foreach.across.modules.adminweb.config;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.*;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.development.AcrossDevelopmentMode;
import com.foreach.across.core.events.AcrossContextBootstrappedEvent;
import com.foreach.across.modules.adminweb.AdminWeb;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.adminweb.AdminWebModuleSettings;
import com.foreach.across.modules.adminweb.annotations.AdminWebController;
import com.foreach.across.modules.adminweb.controllers.AuthenticationController;
import com.foreach.across.modules.adminweb.menu.AdminMenu;
import com.foreach.across.modules.adminweb.menu.AdminMenuBuilder;
import com.foreach.across.modules.adminweb.menu.EntityAdminMenu;
import com.foreach.across.modules.adminweb.menu.EntityAdminMenuBuilder;
import com.foreach.across.modules.adminweb.resource.AdminBootstrapWebResourcePackage;
import com.foreach.across.modules.adminweb.resource.JQueryWebResourcePackage;
import com.foreach.across.modules.web.config.support.PrefixingHandlerMappingConfigurer;
import com.foreach.across.modules.web.context.PrefixingPathRegistry;
import com.foreach.across.modules.web.menu.MenuFactory;
import com.foreach.across.modules.web.mvc.InterceptorRegistry;
import com.foreach.across.modules.web.mvc.PrefixingRequestMappingHandlerMapping;
import com.foreach.across.modules.web.mvc.WebAppPathResolverExposingInterceptor;
import com.foreach.across.modules.web.resource.*;
import com.foreach.across.modules.web.template.LayoutTemplateProcessorAdapterBean;
import com.foreach.across.modules.web.template.WebTemplateInterceptor;
import com.foreach.across.modules.web.template.WebTemplateRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.annotation.AnnotationClassFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate4.support.OpenSessionInViewInterceptor;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Locale;

@Configuration
@AcrossEventHandler
public class AdminWebMvcConfiguration implements PrefixingHandlerMappingConfigurer
{
	private static final Logger LOG = LoggerFactory.getLogger( AdminWebModule.class );

	@Autowired(required = false)
	private WebResourceTranslator viewsWebResourceTranslator;

	@Autowired
	private AcrossContextInfo contextInfo;

	@Autowired
	@Module(AcrossModule.CURRENT_MODULE)
	private AdminWebModule adminWebModule;

	@Autowired
	@Module(AcrossModule.CURRENT_MODULE)
	private AdminWebModuleSettings settings;

	@Autowired
	private AcrossDevelopmentMode developmentMode;

	@Autowired
	private MenuFactory menuFactory;

	@Autowired
	private PrefixingPathRegistry prefixingPathRegistry;

	@RefreshableCollection(includeModuleInternals = true)
	private Collection<PrefixingHandlerMappingConfigurer> prefixingHandlerMappingConfigurers;

	@SuppressWarnings("unchecked")
	@PostConstruct
	public void initialize() {
		menuFactory.addMenuBuilder( adminMenuBuilder(), AdminMenu.class );
		menuFactory.addMenuBuilder( entityAdminMenuBuilder(), EntityAdminMenu.class );
	}

	@AcrossCondition("#{not getBeanFactory().containsBean('" + DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME + "')}")
	@Bean(name = DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME)
	@Exposed
	public CookieLocaleResolver localeResolver() {
		CookieLocaleResolver cookieLocaleResolver = new CookieLocaleResolver();
		Locale defaultLocale = settings.getDefaultLocale();

		if ( defaultLocale != null ) {
			cookieLocaleResolver.setDefaultLocale( defaultLocale );
		}

		return new CookieLocaleResolver();
	}

	@Bean
	public AdminMenuBuilder adminMenuBuilder() {
		return new AdminMenuBuilder();
	}

	@Bean
	public EntityAdminMenuBuilder entityAdminMenuBuilder() {
		return new EntityAdminMenuBuilder();
	}

	@Bean(name = AdminWeb.NAME)
	@Exposed
	public AdminWeb adminWeb() {
		AdminWeb adminWeb = new AdminWeb( adminWebModule.getRootPath() );
		prefixingPathRegistry.add( AdminWeb.NAME, adminWeb );

		return adminWeb;
	}

	@Bean
	@Exposed
	public WebTemplateRegistry adminWebTemplateRegistry() {
		WebTemplateRegistry webTemplateRegistry = new WebTemplateRegistry();

		webTemplateRegistry.register( AdminWeb.LAYOUT_TEMPLATE, adminLayoutTemplateProcessor() );
		webTemplateRegistry.setDefaultTemplateName( AdminWeb.LAYOUT_TEMPLATE );

		return webTemplateRegistry;
	}

	// todo: verify thymeleaf support is enabled
	@Bean
	public LayoutTemplateProcessorAdapterBean adminLayoutTemplateProcessor() {
		return new LayoutTemplateProcessorAdapterBean( AdminWeb.LAYOUT_TEMPLATE )
		{
			@Override
			protected void registerWebResources( WebResourceRegistry registry ) {
				registry.addPackage( AdminBootstrapWebResourcePackage.NAME );
				registry.addWithKey( WebResource.CSS, AdminWeb.MODULE, AdminWeb.LAYOUT_TEMPLATE_CSS,
				                     WebResource.VIEWS );
			}

			@Override
			protected void buildMenus( MenuFactory menuFactory ) {
				menuFactory.buildMenu( AdminMenu.NAME, AdminMenu.class );
			}
		};
	}

	@Bean
	public WebTemplateInterceptor adminWebTemplateInterceptor() {
		return new WebTemplateInterceptor( adminWebTemplateRegistry() );
	}

	@Bean
	@Exposed
	public WebResourcePackageManager adminWebResourcePackageManager() {
		WebResourcePackageManager webResourcePackageManager = new WebResourcePackageManager();
		webResourcePackageManager.register( JQueryWebResourcePackage.NAME,
		                                    new JQueryWebResourcePackage( !developmentMode.isActive() ) );
		webResourcePackageManager.register( AdminBootstrapWebResourcePackage.NAME,
		                                    new AdminBootstrapWebResourcePackage( !developmentMode.isActive() ) );

		return webResourcePackageManager;
	}

	@Bean
	@Exposed
	public WebResourceRegistryInterceptor adminWebResourceRegistryInterceptor() {
		WebResourceRegistryInterceptor interceptor =
				new WebResourceRegistryInterceptor( adminWebResourcePackageManager() );

		if ( viewsWebResourceTranslator != null ) {
			interceptor.addWebResourceTranslator( viewsWebResourceTranslator );
		}
		else {
			LOG.warn( "No default viewsWebResourceTranslator configured - manual translators will be required." );
		}

		return interceptor;
	}

	@Bean
	public AuthenticationController authenticationController() {
		return new AuthenticationController();
	}

	@Bean
	@Exposed
	public PrefixingRequestMappingHandlerMapping adminRequestMappingHandlerMapping() {
		PrefixingRequestMappingHandlerMapping mappingHandlerMapping = new PrefixingRequestMappingHandlerMapping(
				adminWebModule.getRootPath(), new AnnotationClassFilter( AdminWebController.class, true )
		);
		mappingHandlerMapping.setOrder( contextInfo.getModuleIndex( adminWebModule ) );

		return mappingHandlerMapping;
	}

	@Event
	protected void updateInterceptors( AcrossContextBootstrappedEvent contextBootstrappedEvent ) {
		InterceptorRegistry interceptorRegistry = new InterceptorRegistry();

		for ( PrefixingHandlerMappingConfigurer configurer : prefixingHandlerMappingConfigurers ) {
			if ( configurer.supports( AdminWebModule.NAME ) ) {
				configurer.addInterceptors( interceptorRegistry );
			}
		}

		adminRequestMappingHandlerMapping().setInterceptors( interceptorRegistry.getInterceptors().toArray() );
		adminRequestMappingHandlerMapping().reload();
	}

	@Override
	public boolean supports( String mapperName ) {
		return AdminWebModule.NAME.equals( mapperName );
	}

	@Override
	public void addInterceptors( InterceptorRegistry interceptorRegistry ) {
		// todo: unify web registration approach and move this to a different configuration
		if ( contextInfo.hasModule( "AcrossHibernateModule" ) ) {
			ApplicationContext moduleCtx = contextInfo.getModuleInfo( "AcrossHibernateModule" ).getApplicationContext();

			if ( moduleCtx.containsLocalBean( "openSessionInViewInterceptor" ) ) {
				interceptorRegistry.addWebRequestInterceptor(
						moduleCtx.getBean( "openSessionInViewInterceptor", OpenSessionInViewInterceptor.class )
				);
			}
		}

		interceptorRegistry.addInterceptor( new WebAppPathResolverExposingInterceptor( adminWeb() ) );
		interceptorRegistry.addInterceptor( adminWebResourceRegistryInterceptor() );
		interceptorRegistry.addInterceptor( adminWebTemplateInterceptor() );
	}
}
