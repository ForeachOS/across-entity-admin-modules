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
import com.foreach.across.modules.entity.views.request.EntityViewCommand;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.web.ui.DefaultViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElementBuilderContextHolder;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilder;
import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilderSupport;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.WebDataBinder;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Base implementation for a {@link EntityViewFactory} that supports {@link com.foreach.across.modules.web.ui.ViewElement} rendering,
 * and dispatches its logic to {@link EntityViewProcessor} instances that have more fine-grained hooks for interacting with the view rendering.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
public class DispatchingEntityViewFactory extends ToBeRemovedEntityViewFactory
{
	public static final String ATTRIBUTE_CONTAINER_BUILDER = "entityViewContainerBuilder";
	public static final String ATTRIBUTE_CONTAINER_ELEMENT = "entityViewContainer";

	private final Collection<EntityViewProcessor> processors = new ArrayDeque<>();

	private TransactionTemplate transactionTemplate;

	private BootstrapUiFactory bootstrapUiFactory;

	/**
	 * Set the transaction template that should be used for methods that require executing in a transaction.
	 * If set this will execute all {@link EntityViewProcessor#doControl(EntityViewRequest, EntityView, EntityViewCommand)} in a single transaction
	 * if they occur with a state altering {@link HttpMethod} like {@link HttpMethod#POST}.
	 *
	 * @param transactionTemplate to use - can be null if no transactions are every required
	 * @see #shouldDispatchInTransaction(EntityViewRequest)
	 */
	public void setTransactionTemplate( TransactionTemplate transactionTemplate ) {
		this.transactionTemplate = transactionTemplate;
	}

	/**
	 * Set the collection of {@link EntityViewProcessor}s that should be called when building the {@link EntityView}.
	 *
	 * @param processors to execute
	 */
	public void setProcessors( Collection<EntityViewProcessor> processors ) {
		this.processors.clear();
		this.processors.addAll( processors );
	}

	/**
	 * Add a single processor to the back of the list.
	 *
	 * @param processor to add
	 */
	public void addProcessor( EntityViewProcessor processor ) {
		processors.add( processor );
	}

	@Override
	public void prepareEntityViewContext( ConfigurableEntityViewContext entityViewContext ) {
		dispatchToProcessors( EntityViewProcessor.class, p -> p.prepareEntityViewContext( entityViewContext ) );
	}

	@Override
	public void authorizeRequest( EntityViewRequest entityViewRequest ) {
		dispatchToProcessors( EntityViewProcessor.class, p -> p.authorizeRequest( entityViewRequest ) );
	}

	@Override
	public void initializeCommandObject( EntityViewRequest entityViewRequest,
	                                     EntityViewCommand command,
	                                     WebDataBinder dataBinder ) {
		dispatchToProcessors( EntityViewProcessor.class, p -> p.initializeCommandObject( entityViewRequest, command, dataBinder ) );
	}

	@Override
	public EntityView createView( EntityViewRequest entityViewRequest ) {
		Optional<com.foreach.across.modules.web.ui.ViewElementBuilderContext> existingBuilderContext
				= ViewElementBuilderContextHolder.setViewElementBuilderContext( createViewElementBuilderContext( entityViewRequest ) );

		final EntityView entityView = new EntityView( entityViewRequest.getModel(), entityViewRequest.getRedirectAttributes() );

		try {
			// pre-process the view
			dispatchToProcessors( EntityViewProcessor.class, p -> p.preProcess( entityViewRequest, entityView ) );

			// perform controller logic - optionally do so in a single transaction
			dispatchInTransaction(
					entityViewRequest,
					( r ) -> dispatchToProcessors( EntityViewProcessor.class, p -> p.doControl( r, entityView, entityViewRequest.getCommand() ) )
			);

			// check if rendering is required
			if ( entityView.shouldRender() ) {
				// prepare for rendering
				dispatchToProcessors( EntityViewProcessor.class, p -> p.preRender( entityViewRequest, entityView ) );

				// create a container builder
				ContainerViewElementBuilder containerBuilder = bootstrapUiFactory.container();
				entityView.addAttribute( ATTRIBUTE_CONTAINER_BUILDER, containerBuilder );

				// do the initial render
				dispatchToProcessors( EntityViewProcessor.class, p -> p.render( entityViewRequest, entityView ) );

				// build the container - add as first child to the page content
				ContainerViewElementBuilderSupport<ContainerViewElement, ?> actualContainerBuilder
						= entityView.removeAttribute( ATTRIBUTE_CONTAINER_BUILDER, ContainerViewElementBuilderSupport.class );

				ContainerViewElement container = actualContainerBuilder.build();
				entityView.addAttribute( ATTRIBUTE_CONTAINER_ELEMENT, container );

				entityViewRequest.getPageContentStructure().addFirstChild( container );

				// perform render related post-processing
				dispatchToProcessors( EntityViewProcessor.class, p -> p.postRender( entityViewRequest, entityView ) );

				entityView.removeAttribute( ATTRIBUTE_CONTAINER_ELEMENT );
			}

			// perform general post-processing
			dispatchToProcessors( EntityViewProcessor.class, p -> p.postProcess( entityViewRequest, entityView ) );
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
		builderContext.setAttribute( ViewElementBuilderContext.ENTITY, entityViewRequest.getEntityViewContext().getEntity( Object.class ) );
		builderContext.setAttribute( EntityMessageCodeResolver.class, entityViewRequest.getEntityViewContext().getMessageCodeResolver() );
		builderContext.setAttribute( EntityViewRequest.class, entityViewRequest );

		return builderContext;
	}

	protected final void dispatchInTransaction( EntityViewRequest entityViewRequest, Consumer<EntityViewRequest> invocation ) {

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

		if ( transactionTemplate != null && shouldDispatchInTransaction( entityViewRequest ) ) {
			transactionTemplate.execute( ( status ) -> {
				invocation.accept( entityViewRequest );
				return null;
			} );
		}
		else {
			invocation.accept( entityViewRequest );
		}
	}

	/**
	 * Call a method on all processors of a given type.
	 *
	 * @param processorType to call the method on
	 * @param consumer      method to execute
	 */
	protected final <U> void dispatchToProcessors( Class<U> processorType, Consumer<U> consumer ) {
		processors.stream()
		          .filter( processorType::isInstance )
		          .forEach( p -> consumer.accept( processorType.cast( p ) ) );
	}

	/**
	 * Should the {@link EntityViewProcessor#doControl(EntityViewRequest, EntityView, EntityViewCommand)} be executed in a wrapping transaction?
	 * By default this is the case for all state modifying {@link HttpMethod}s.
	 * <p/>
	 * Requires a {@link #transactionTemplate} to be set as well before a transaction will be used.
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
