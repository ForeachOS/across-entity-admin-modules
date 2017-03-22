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

package com.foreach.across.modules.entity.views;

import com.foreach.across.modules.bootstrapui.elements.BootstrapUiFactory;
import com.foreach.across.modules.entity.support.EntityMessageCodeResolver;
import com.foreach.across.modules.entity.views.context.ConfigurableEntityViewContext;
import com.foreach.across.modules.entity.views.processors.support.TransactionalEntityViewProcessorRegistry;
import com.foreach.across.modules.entity.views.request.EntityViewCommand;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.entity.web.EntityViewModel;
import com.foreach.across.modules.web.ui.DefaultViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElementBuilderContextHolder;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilder;
import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilderSupport;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.WebDataBinder;

import java.util.Optional;

/**
 * Base implementation for a {@link EntityViewFactory} that supports {@link com.foreach.across.modules.web.ui.ViewElement} rendering,
 * and dispatches its logic to {@link EntityViewProcessor} instances that have more fine-grained hooks for interacting with the view rendering.
 * <p/>
 * Supports a {@link TransactionalEntityViewProcessorRegistry}.  All {@link EntityViewProcessor#doControl(EntityViewRequest, EntityView, EntityViewCommand)}
 * calls will be dispatched in a single transaction if they occur with a state altering {@link HttpMethod} like {@link HttpMethod#POST}.
 *
 * @author Arne Vandamme
 * @see TransactionalEntityViewProcessorRegistry
 * @since 2.0.0
 */
public class DefaultEntityViewFactory implements DispatchingEntityViewFactory
{
	public static final String ATTRIBUTE_CONTAINER_BUILDER = "entityViewContainerBuilder";
	public static final String ATTRIBUTE_CONTAINER_ELEMENT = "entityViewContainer";

	private BootstrapUiFactory bootstrapUiFactory;

	/*
		Advised advised = (Advised) clientRepository;
		Stream.of( advised.getAdvisors() )
		      .map( Advisor::getAdvice )
		      .filter( TransactionInterceptor.class::isInstance )
		      .map( TransactionInterceptor.class::cast )
		      .findFirst()
		      .ifPresent( ti -> System.err.println(ti.getTransactionManager()) );
		*/
	// if repository, get the interceptors - if TransactionInterceptor is present, get the transactionManagerBeanName
	// if set, retrieve the transaction manager from the bean factory, else get the transactionManager from the interceptor,
	// if set, use that one - else use the default transaction manager
	@Getter
	@Setter
	private TransactionalEntityViewProcessorRegistry processorRegistry = new TransactionalEntityViewProcessorRegistry();

	@Override
	public void prepareEntityViewContext( ConfigurableEntityViewContext entityViewContext ) {
		processorRegistry.dispatch( p -> p.prepareEntityViewContext( entityViewContext ) );
	}

	@Override
	public void authorizeRequest( EntityViewRequest entityViewRequest ) {
		processorRegistry.dispatch( p -> p.authorizeRequest( entityViewRequest ) );
	}

	@Override
	public void initializeCommandObject( EntityViewRequest entityViewRequest,
	                                     EntityViewCommand command,
	                                     WebDataBinder dataBinder ) {
		processorRegistry.dispatch( p -> p.initializeCommandObject( entityViewRequest, command, dataBinder ) );
	}

	@Override
	public EntityView createView( EntityViewRequest entityViewRequest ) {
		Optional<com.foreach.across.modules.web.ui.ViewElementBuilderContext> existingBuilderContext
				= ViewElementBuilderContextHolder.setViewElementBuilderContext( createViewElementBuilderContext( entityViewRequest ) );

		final EntityView entityView = new EntityView( entityViewRequest.getModel(), entityViewRequest.getRedirectAttributes() );

		try {
			// pre-process the view
			processorRegistry.dispatch( p -> p.preProcess( entityViewRequest, entityView ) );

			// perform controller logic - optionally do so in a single transaction
			processorRegistry.dispatch(
					p -> p.doControl( entityViewRequest, entityView, entityViewRequest.getCommand() ),
					shouldDispatchInTransaction( entityViewRequest )
			);

			// check if rendering is required
			if ( entityView.shouldRender() ) {
				// prepare for rendering
				processorRegistry.dispatch( p -> p.preRender( entityViewRequest, entityView ) );

				// create a container builder
				ContainerViewElementBuilder containerBuilder = bootstrapUiFactory.container();
				entityView.addAttribute( ATTRIBUTE_CONTAINER_BUILDER, containerBuilder );

				// do the initial render
				processorRegistry.dispatch( p -> p.render( entityViewRequest, entityView ) );

				// build the container - add as first child to the page content
				ContainerViewElementBuilderSupport<ContainerViewElement, ?> actualContainerBuilder
						= entityView.removeAttribute( ATTRIBUTE_CONTAINER_BUILDER, ContainerViewElementBuilderSupport.class );

				ContainerViewElement container = actualContainerBuilder.build();
				entityView.addAttribute( ATTRIBUTE_CONTAINER_ELEMENT, container );

				entityViewRequest.getPageContentStructure().addFirstChild( container );

				// perform render related post-processing
				processorRegistry.dispatch( p -> p.postRender( entityViewRequest, entityView ) );

				entityView.removeAttribute( ATTRIBUTE_CONTAINER_ELEMENT );
			}

			// perform general post-processing
			processorRegistry.dispatch( p -> p.postProcess( entityViewRequest, entityView ) );
		}
		finally {
			// reset to the original builder context
			ViewElementBuilderContextHolder.setViewElementBuilderContext( existingBuilderContext );
		}

		return entityView;
	}

	/**
	 * Create a custom {@link com.foreach.across.modules.web.ui.ViewElementBuilderContext} for the view request.
	 */
	protected com.foreach.across.modules.web.ui.ViewElementBuilderContext createViewElementBuilderContext( EntityViewRequest entityViewRequest ) {
		com.foreach.across.modules.web.ui.ViewElementBuilderContext builderContext = new DefaultViewElementBuilderContext( entityViewRequest.getModel() );
		if ( entityViewRequest.getEntityViewContext().holdsEntity() ) {
			Object entity = entityViewRequest.getEntityViewContext().getEntity();
			entityViewRequest.getModel().addAttribute( EntityViewModel.ENTITY, entity );
		}
		builderContext.setAttribute( EntityMessageCodeResolver.class, entityViewRequest.getEntityViewContext().getMessageCodeResolver() );
		builderContext.setAttribute( EntityViewRequest.class, entityViewRequest );

		return builderContext;
	}

	/**
	 * Should the {@link EntityViewProcessor#doControl(EntityViewRequest, EntityView, EntityViewCommand)} be executed in a wrapping transaction?
	 * By default this is the case for all state modifying {@link HttpMethod}s.
	 * <p/>
	 * Requires a transaction template to be set on the {@link #processorRegistry} to be set as well before a transaction will be used.
	 *
	 * @param entityViewRequest to check
	 * @return true if transaction should be used
	 */
	protected boolean shouldDispatchInTransaction( EntityViewRequest entityViewRequest ) {
		return ArrayUtils.contains(
				new HttpMethod[] { HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE },
				entityViewRequest.getHttpMethod()
		);
	}

	@Autowired
	void setBootstrapUiFactory( BootstrapUiFactory bootstrapUiFactory ) {
		this.bootstrapUiFactory = bootstrapUiFactory;
	}
}
