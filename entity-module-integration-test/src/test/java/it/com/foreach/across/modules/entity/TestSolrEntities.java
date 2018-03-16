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

package it.com.foreach.across.modules.entity;

import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.entity.registry.EntityRegistry;
import com.foreach.across.testmodules.solr.SolrTestModule;
import com.foreach.across.testmodules.solr.business.Product;
import com.foreach.across.testmodules.solr.repositories.ProductRepository;
import com.foreach.across.testmodules.springdata.SpringDataJpaModule;
import com.foreach.across.testmodules.springdata.repositories.ClientRepository;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.spring.security.SpringSecurityModule;
import com.foreach.across.test.AcrossTestConfiguration;
import com.foreach.across.test.AcrossWebAppConfiguration;
import it.com.foreach.across.modules.entity.utils.EntityVerifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.util.TxUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@AcrossWebAppConfiguration
public class TestSolrEntities
{
	@Autowired
	private EntityRegistry entityRegistry;

	@Autowired(required = false)
	private ClientRepository clientRepository;

	@Autowired(required = false)
	private ProductRepository productRepository;

	@Test
	public void contextShouldBootstrap() {
		assertNotNull( clientRepository );
		assertNotNull( productRepository );
	}

	@Test
	public void solrEntityShouldBeRegistered() {
		verify( Product.class )
				.isVisible( true )
				.hasRepository()
				.hasAttribute( EntityAttributes.TRANSACTION_MANAGER_NAME, TxUtils.DEFAULT_TRANSACTION_MANAGER );
	}

	private EntityVerifier verify( Class<?> entityType ) {
		return new EntityVerifier( entityRegistry, entityType );
	}

	@Configuration
	@AcrossTestConfiguration(modules = { SpringSecurityModule.NAME, AdminWebModule.NAME, EntityModule.NAME })
	protected static class Config
	{
		@Bean
		public AcrossHibernateJpaModule acrossHibernateJpaModule() {
			AcrossHibernateJpaModule hibernateModule = new AcrossHibernateJpaModule();
			hibernateModule.setHibernateProperty( "hibernate.hbm2ddl.auto", "create" );
			return hibernateModule;
		}

		@Bean
		public SpringDataJpaModule springDataJpaModule() {
			SpringDataJpaModule springDataJpaModule = new SpringDataJpaModule();
			springDataJpaModule.expose( ClientRepository.class );
			return springDataJpaModule;
		}

		@Bean
		public SolrTestModule solrTestModule() {
			SolrTestModule solrTestModule = new SolrTestModule();
			solrTestModule.expose( ProductRepository.class );
			return solrTestModule;
		}
	}
}
