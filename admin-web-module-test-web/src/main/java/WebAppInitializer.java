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

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.spring.security.SpringSecurityModule;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.context.AcrossWebApplicationContext;
import com.foreach.across.modules.web.servlet.AbstractAcrossServletInitializer;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author Arne Vandamme
 */
public class WebAppInitializer extends AbstractAcrossServletInitializer
{
	@Override
	protected void configure( AcrossWebApplicationContext applicationContext ) {
		applicationContext.register( WebAppConfiguration.class );
	}

	@Configuration
	@EnableAcrossContext
	protected static class WebAppConfiguration implements AcrossContextConfigurer
	{
		@Bean
		public DataSource acrossDataSource() {
			BasicDataSource dataSource = new BasicDataSource();
			dataSource.setDriverClassName( "org.hsqldb.jdbc.JDBCDriver" );
			dataSource.setUrl( "jdbc:hsqldb:mem:/hsql/testWeb" );
			dataSource.setUsername( "sa" );
			dataSource.setPassword( "" );

			return dataSource;
		}

		@Override
		public void configure( AcrossContext context ) {
			context.addModule( acrossWebModule() );
			context.addModule( adminWebModule() );
			context.addModule( new SpringSecurityModule() );
		}

		private AcrossWebModule acrossWebModule() {
			return new AcrossWebModule();
		}

		private AdminWebModule adminWebModule() {
			AdminWebModule adminWebModule = new AdminWebModule();
			adminWebModule.setRootPath( "/secure" );

			return adminWebModule;
		}
	}
}
