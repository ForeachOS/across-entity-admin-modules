package com.foreach.across.module.applicationinfo.config.modules;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.core.annotations.Event;
import com.foreach.across.core.events.AcrossModuleBootstrappedEvent;
import com.foreach.across.module.applicationinfo.controllers.ApplicationInfoController;
import com.foreach.across.modules.debugweb.DebugWebModuleSettings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Replace the default controller for debugweb.
 */
@AcrossDepends(required = "DebugWebModule")
@Configuration
@AcrossEventHandler
public class DebugWebConfiguration
{
	private static Logger LOG = LoggerFactory.getLogger( DebugWebConfiguration.class );

	@Autowired(required = false)
	private DebugWebModuleSettings debugWebModuleSettings;

	@Event
	protected void registerDebugDashboard( AcrossModuleBootstrappedEvent moduleBootstrappedEvent ) {
		if ( StringUtils.equals( "DebugWebModule", moduleBootstrappedEvent.getModule().getName() )
				&& debugWebModuleSettings != null
				&& StringUtils.equals( debugWebModuleSettings.getDashboardPath(), "/" ) ) {
			LOG.trace( "Registering debug dashboard to application info controller" );
			debugWebModuleSettings.setDashboardPath( ApplicationInfoController.PATH );
		}
	}

	@Bean
	public ApplicationInfoController applicationInfoController() {
		return new ApplicationInfoController();
	}
}
