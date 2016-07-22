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

import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.modules.bootstrapui.elements.BootstrapUiFactory;
import com.foreach.across.modules.bootstrapui.elements.Grid;
import com.foreach.across.modules.bootstrapui.elements.Style;
import com.foreach.across.modules.entity.controllers.EntityControllerAttributes;
import com.foreach.across.modules.entity.query.AssociatedEntityQueryExecutor;
import com.foreach.across.modules.entity.query.EntityQuery;
import com.foreach.across.modules.entity.registry.EntityAssociation;
import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.support.EntityMessageCodeResolver;
import com.foreach.across.modules.entity.views.events.BuildEntityDeleteViewEvent;
import com.foreach.across.modules.entity.views.support.EntityMessages;
import com.foreach.across.modules.entity.web.EntityLinkBuilder;
import com.foreach.across.modules.web.resource.WebResourceUtils;
import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilder;
import com.foreach.across.modules.web.ui.elements.builder.NodeViewElementBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;

/**
 * Creates the delete form for an entity.
 *
 * @author Arne Vandamme
 * @since 1.2.0
 */
public class EntityDeleteViewFactory<V extends ViewCreationContext>
		extends SimpleEntityViewFactorySupport<V, EntityView>
{
	public static final String FORM_NAME = "entityDeleteForm";

	private BootstrapUiFactory bootstrapUi;
	private AcrossEventPublisher eventPublisher;

	@Autowired
	public void setBootstrapUiFactory( BootstrapUiFactory bootstrapUiFactory ) {
		this.bootstrapUi = bootstrapUiFactory;
	}

	@Autowired
	public void setEventPublisher( AcrossEventPublisher eventPublisher ) {
		this.eventPublisher = eventPublisher;
	}

	@Override
	protected EntityView createEntityView( ModelMap model ) {
		return new EntityView( model );
	}

	@Override
	protected void buildViewModel( V viewCreationContext,
	                               EntityConfiguration entityConfiguration,
	                               EntityMessageCodeResolver codeResolver,
	                               EntityView view ) {
		EntityViewElementBuilderContext<EntityView> builderContext = new EntityViewElementBuilderContext<>( view );
		builderContext.setWebResourceRegistry( WebResourceUtils.currentRegistry() );

		EntityLinkBuilder linkBuilder = view.getEntityLinkBuilder();
		EntityMessages messages = view.getEntityMessages();

		BuildEntityDeleteViewEvent viewConfiguration = buildViewConfiguration( view );

		NodeViewElementBuilder list = bootstrapUi.node( "ul" );

		for ( EntityAssociation association : ( (EntityConfiguration<?>) entityConfiguration ).getAssociations() ) {
			dod( list, association, linkBuilder, view.getEntity() );
		}

		ContainerViewElementBuilder buttons = buildButtons( linkBuilder, messages, viewConfiguration );

		view.setViewElements(
				bootstrapUi.form()
				           .name( EntityFormViewFactory.FORM_NAME )
				           .commandAttribute( EntityControllerAttributes.VIEW_REQUEST )
				           .post()
				           .noValidate()
				           .action( linkBuilder.delete( view.getEntity() ) )
				           .add(
						           bootstrapUi.row()
						                      .add(
								                      bootstrapUi.column( Grid.Device.MD.width( Grid.Width.HALF ) )
								                                 .name( EntityFormViewFactory.FORM_LEFT )
								                                 .add(
										                                 bootstrapUi
												                                 .text( messages.withNameSingular(
														                                 "views.deleteView.confirmation" ) )
								                                 )
								                                 .add( list )
						                      )
						                      .add(
								                      bootstrapUi.column( Grid.Device.MD.width( Grid.Width.HALF ) )
								                                 .name( EntityFormViewFactory.FORM_RIGHT )
						                      )
				           )
				           .add( buttons )
				           .build( builderContext )
		);
	}

	private ContainerViewElementBuilder buildButtons( EntityLinkBuilder linkBuilder,
	                                                  EntityMessages messages,
	                                                  BuildEntityDeleteViewEvent viewConfiguration ) {
		ContainerViewElementBuilder buttons = bootstrapUi.container().name( "buttons" );
		if ( !viewConfiguration.isDeleteDisabled() ) {
			buttons.add(
					bootstrapUi.button()
					           .name( "btn-delete" )
					           .style( Style.DANGER )
					           .submit()
					           .text( messages.messageWithFallback( "buttons.delete" ) )
			);
		}
		buttons.add(
				bootstrapUi.button()
				           .name( "btn-cancel" )
				           .link( linkBuilder.overview() )
				           .text( messages.messageWithFallback( "actions.cancel" ) )
		);
		return buttons;
	}

	private BuildEntityDeleteViewEvent buildViewConfiguration( EntityView view ) {
		BuildEntityDeleteViewEvent<?> event = new BuildEntityDeleteViewEvent<>( view.getEntity() );
		event.setDeleteDisabled( false );

		eventPublisher.publish( event );

		return event;
	}

	private void dod( NodeViewElementBuilder list,
	                  EntityAssociation association,
	                  EntityLinkBuilder parentLinkBuilder,
	                  Object parent ) {
		AssociatedEntityQueryExecutor executor = association.getAttribute( AssociatedEntityQueryExecutor.class );
		int count = executor.findAll( parent, EntityQuery.all() ).size();

		if ( count > 0 ) {
			EntityMessages messages = new EntityMessages(
					association.getTargetEntityConfiguration().getEntityMessageCodeResolver()
			);

			EntityLinkBuilder linkBuilder
					= association.getAttribute( EntityLinkBuilder.class ).asAssociationFor( parentLinkBuilder, parent );

			list.add( bootstrapUi.node( "li" )
			                     .add( bootstrapUi.button().link().url( linkBuilder.overview() )
			                                      .text( messages.withNamePlural( "views.deleteView.associatedResults",
			                                                                      count ) ) ) );
		}

		//list.add( bootstrapUi.node( "li" ).add( bootstrapUi.textarea() ) )
	}
}
