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

package com.foreach.across.samples.entity.application.config;

import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.core.annotations.Module;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.config.builders.EntityConfigurationBuilder;
import com.foreach.across.modules.entity.registry.EntityAssociation;
import com.foreach.across.modules.entity.registry.EntityRegistry;
import com.foreach.across.modules.entity.registry.MutableEntityConfiguration;
import com.foreach.across.modules.entity.registry.properties.MutableEntityPropertyDescriptor;
import com.foreach.across.modules.entity.views.DispatchingEntityViewFactory;
import com.foreach.across.modules.entity.views.context.ConfigurableEntityViewContext;
import com.foreach.across.modules.entity.views.context.EntityViewContext;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.processors.support.EntityViewProcessorRegistry;
import com.foreach.across.modules.entity.web.EntityLinkBuilder;
import com.foreach.across.modules.entity.web.links.EntityViewLinkBuilder;
import com.foreach.across.modules.entity.web.links.EntityViewLinkBuilderSupport;
import com.foreach.across.modules.entity.web.links.SingleEntityViewLinkBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.Repository;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

// todo see {@link ElasticsearchMenuconfiguration for menu configuration
@Configuration
@RequiredArgsConstructor
@Order()
@ConditionalOnAcrossModule("AcrossHibernateJpaModule")
public class EntityElasticsearchConfiguration implements EntityConfigurer
{
	@Module(EntityModule.NAME)
	@Autowired
	private final AcrossModuleInfo moduleInfo;

	public static final String ATTR_ELASTIC_PROXY_REFERENCE = "elasticsearch-reference-entity";

	private final EntityRegistry entityRegistry;

	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.matching( this::isElasticsearchEntity )
		        .properties( props -> props.processRegistry( pr -> pr.getRegisteredDescriptors()
		                                                             .stream()
		                                                             .filter( pd -> MutableEntityPropertyDescriptor.class.isAssignableFrom( pd.getClass() ) )
		                                                             .map( MutableEntityPropertyDescriptor.class::cast )
		                                                             .forEach( pd -> pd.setWritable( false ) ) ) )
		        .postProcessor( this::configureElasticSearchEntity );
	}

	public <E, L> Consumer<MutableEntityConfiguration<E>> configureAssociationQuery( String targetAssociation,
	                                                                                 String searchAssociation,
	                                                                                 Class<L> searchEntityType,
	                                                                                 String targetAssociationProperty,
	                                                                                 String matchingSearchEntityProperty ) {
		return mec -> {
			Collection<EntityAssociation> associations = mec.getAssociations();
			associations.stream()
			            .filter( ass -> StringUtils.equalsIgnoreCase( targetAssociation, ass.getName() ) )
			            .findFirst()
			            .ifPresent(
					            ass -> {
						            Arrays.stream( mec.getViewNames() )
						                  .filter( vn -> StringUtils.startsWith( vn, targetAssociation ) )
						                  .map( mec::getViewFactory )
						                  .filter( Objects::nonNull )
						                  .filter( vf -> DispatchingEntityViewFactory.class.isAssignableFrom( vf.getClass() ) )
						                  .map( DispatchingEntityViewFactory.class::cast )
						                  .forEach(
								                  dvf -> {
									                  EntityViewProcessorRegistry processorRegistry = dvf.getProcessorRegistry();
									                  if ( !processorRegistry.contains( ElasticAssociationViewContextLinkBuilderProcessor.class.getName() ) ) {
										                  processorRegistry.addProcessor(
												                  new ElasticAssociationViewContextLinkBuilderProcessor( searchAssociation, targetAssociation )
										                  );
									                  }
								                  }
						                  );
					            }
			            );

			new EntityConfigurationBuilder<E>( moduleInfo.getApplicationContext().getAutowireCapableBeanFactory() )
					.association(
							ab -> ab.name( searchAssociation )
							        .targetEntityType( searchEntityType )
							        .sourceProperty( targetAssociationProperty )
							        .targetProperty( matchingSearchEntityProperty )
							        .associationType( EntityAssociation.Type.EMBEDDED )
							        .listView( lvb -> lvb.viewProcessor(
									        vp -> vp.provideBean(
											        new ElasticAssociationViewContextLinkBuilderProcessor( searchAssociation, targetAssociation ) )
							        ) ).show()
					)
					.apply( mec );
		};
	}

	private void configureElasticSearchEntity( MutableEntityConfiguration<?> entityConfiguration ) {
		// custom entityviewlinkbuilder
		if ( isElasticsearchProxy( entityConfiguration ) ) {
			Class<?> referencedEntity = entityConfiguration.getAttribute( ATTR_ELASTIC_PROXY_REFERENCE, Class.class );
			if ( entityRegistry.contains( referencedEntity ) ) {
				EntityViewLinkBuilder searchLinkBuilder = entityConfiguration.getAttribute( EntityViewLinkBuilder.class );
				MutableEntityConfiguration<?> referencedEntityConfiguration =
						(MutableEntityConfiguration<?>) entityRegistry.getEntityConfiguration( referencedEntity );
				EntityViewLinkBuilder instanceLinkBuilder = referencedEntityConfiguration.getAttribute( EntityViewLinkBuilder.class );
				if ( Objects.nonNull( searchLinkBuilder ) && Objects.nonNull( instanceLinkBuilder ) ) {
					ProxyEntityViewLinkBuilder newInstanceLinkBuilder = new ProxyEntityViewLinkBuilder( searchLinkBuilder, instanceLinkBuilder );
					referencedEntityConfiguration.setAttribute( EntityViewLinkBuilder.class, newInstanceLinkBuilder );

					EntityViewLinkBuilder proxyEntityViewLinkBuilder = new ProxyEntityViewLinkBuilder( searchLinkBuilder, newInstanceLinkBuilder );
					entityConfiguration.setAttribute( EntityViewLinkBuilder.class, proxyEntityViewLinkBuilder );
				}
			}
		}
	}

	private boolean isElasticsearchEntity( MutableEntityConfiguration<?> ec ) {
		return ec.hasAttribute( Repository.class ) && ElasticsearchRepository.class.isAssignableFrom( ec.getAttribute( Repository.class ).getClass() );
	}

	private boolean isElasticsearchProxy( MutableEntityConfiguration<?> ec ) {
		return ec.hasAttribute( ATTR_ELASTIC_PROXY_REFERENCE );
	}

	public static class ProxyEntityViewLinkBuilder extends EntityViewLinkBuilder
	{
		private final EntityViewLinkBuilder searchLinkBuilder;
		private final EntityViewLinkBuilder entityLinkBuilder;

		public ProxyEntityViewLinkBuilder( EntityViewLinkBuilder searchLinkBuilder,
		                                   EntityViewLinkBuilder entityLinkBuilder ) {
			super( entityLinkBuilder.toUriComponentsBuilder(), null );
			this.searchLinkBuilder = searchLinkBuilder;
			this.entityLinkBuilder = entityLinkBuilder;
		}

		@Override
		public EntityLinkBuilder asAssociationFor( EntityLinkBuilder sourceLinkBuilder, Object sourceEntity ) {
			return entityLinkBuilder.asAssociationFor( sourceLinkBuilder, sourceEntity );
		}

		@Override
		public EntityViewLinkBuilderSupport listView() {
			return searchLinkBuilder.listView();
		}

		@Override
		public EntityViewLinkBuilderSupport createView() {
			return entityLinkBuilder.createView();
		}

		@Override
		public SingleEntityViewLinkBuilder forInstance( @NonNull Object entity ) {
			return entityLinkBuilder.forInstance( entity );
		}

		@Override
		public SingleEntityViewLinkBuilder withId( @NonNull Object id ) {
			return entityLinkBuilder.withId( id );
		}

		// override to prevent local componentsbuilder being used

		@Override
		public UriComponentsBuilder toUriComponentsBuilder() {
			return entityLinkBuilder.toUriComponentsBuilder();
		}

		@Override
		public UriComponents toUriComponents() {
			return toUriComponentsBuilder().build();
		}

		@Override
		public URI toUri() {
			return searchLinkBuilder.toUri();
		}

		@Override
		public String toUriString() {
			return searchLinkBuilder.toUriString();
		}
	}

	@RequiredArgsConstructor
	static class ElasticAssociationViewContextLinkBuilderProcessor extends EntityViewProcessorAdapter
	{
		private final String searchAssociation;
		private final String targetAssociation;

		@Override
		public void prepareEntityViewContext( ConfigurableEntityViewContext entityViewContext ) {
			if ( entityViewContext.isForAssociation() ) {
				EntityViewLinkBuilder instanceLinkBuilder = resolveAssociationLinkBuilder( entityViewContext.getParentContext(), targetAssociation );
				EntityViewLinkBuilder linkBuilder = resolveAssociationLinkBuilder( entityViewContext.getParentContext(), searchAssociation );

				EntityViewLinkBuilder proxyInstanceLinkBuilder = new ProxyEntityViewLinkBuilder( linkBuilder, instanceLinkBuilder );
				EntityViewLinkBuilder proxyLinkBuilder = new ProxyEntityViewLinkBuilder( linkBuilder, proxyInstanceLinkBuilder );
				entityViewContext.setLinkBuilder( proxyLinkBuilder );
			}
		}
	}

	static EntityViewLinkBuilder resolveAssociationLinkBuilder( EntityViewContext associationParentContext, String targetAssociation ) {
		return associationParentContext.getLinkBuilder().forInstance( associationParentContext.getEntity() )
		                               .association( targetAssociation );
	}
}
