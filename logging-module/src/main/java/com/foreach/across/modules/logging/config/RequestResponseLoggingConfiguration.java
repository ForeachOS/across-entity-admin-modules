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
package com.foreach.across.modules.logging.config;

import com.foreach.across.core.AcrossException;
import com.foreach.across.core.annotations.AcrossCondition;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.modules.logging.LoggingModuleSettings;
import com.foreach.across.modules.logging.controllers.RequestResponseLogController;
import com.foreach.across.modules.logging.requestresponse.RequestResponseLogConfiguration;
import com.foreach.across.modules.logging.requestresponse.RequestResponseLogRegistry;
import com.foreach.across.modules.logging.requestresponse.RequestResponseLoggingFilter;
import com.foreach.across.modules.web.servlet.AcrossWebDynamicServletConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Collection;
import java.util.EnumSet;

/**
 * @author Andy Somers
 */
@Configuration
@AcrossCondition("settings.requestResponseLogEnabled")
public class RequestResponseLoggingConfiguration extends AcrossWebDynamicServletConfigurer
{
	private static final Logger LOG = LoggerFactory.getLogger( RequestResponseLoggingConfiguration.class );

	@Autowired
	private LoggingModuleSettings settings;

	@Bean
	public RequestResponseLogRegistry requestResponseLogRegistry() {
		RequestResponseLogRegistry registry = new RequestResponseLogRegistry();
		registry.setMaxEntries( logConfiguration().getMaxEntries() );

		return registry;
	}

	@Bean
	@AcrossDepends(required = "DebugWebModule")
	public RequestResponseLogController requestResponseLogController() {
		return new RequestResponseLogController();
	}

	@Bean
	@Lazy
	public RequestResponseLoggingFilter requestResponseLoggingFilter() {
		RequestResponseLoggingFilter filter = new RequestResponseLoggingFilter( requestResponseLogRegistry(),
		                                                                        logConfiguration().isPaused() );

		if ( logConfiguration().getIncludedPathPatterns() != null ) {
			filter.setIncludedPathPatterns( logConfiguration().getIncludedPathPatterns() );
		}

		if ( logConfiguration().getExcludedPathPatterns() != null ) {
			filter.setExcludedPathPatterns( logConfiguration().getExcludedPathPatterns() );
		}

		return filter;
	}

	@Override
	protected void dynamicConfigurationAllowed( ServletContext servletContext ) throws ServletException {
		FilterRegistration.Dynamic filter = servletContext.addFilter( "loggingFilter", requestResponseLoggingFilter() );

		Collection<String> urlFilterMappings = logConfiguration().getUrlFilterMappings();
		Collection<String> servletNameFilterMappings = logConfiguration().getServletNameFilterMappings();

		if ( urlFilterMappings.isEmpty() && servletNameFilterMappings.isEmpty() ) {
			throw new AcrossException(
					"At least one filter mapping must be specified when enabling request/response debug logging" );
		}

		if ( !urlFilterMappings.isEmpty() ) {
			filter.addMappingForUrlPatterns( EnumSet.allOf( DispatcherType.class ), false,
			                                 urlFilterMappings.toArray( new String[urlFilterMappings.size()] ) );
		}
		if ( !servletNameFilterMappings.isEmpty() ) {
			filter.addMappingForServletNames( EnumSet.allOf( DispatcherType.class ), false,
			                                  servletNameFilterMappings.toArray(
					                                  new String[servletNameFilterMappings.size()] ) );
		}
	}

	@Override
	protected void dynamicConfigurationDenied( ServletContext servletContext ) throws ServletException {
		LOG.warn(
				"Request/response logging is configured, but I am unable to add RequestResponseLoggingFilter dynamically." );
	}

	private RequestResponseLogConfiguration logConfiguration() {
		return settings.getProperty( LoggingModuleSettings.REQUEST_RESPONSE_LOG_CONFIGURATION,
		                             RequestResponseLogConfiguration.class );
	}

}
