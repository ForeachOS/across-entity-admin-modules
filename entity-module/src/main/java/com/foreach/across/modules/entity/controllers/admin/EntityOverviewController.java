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

package com.foreach.across.modules.entity.controllers.admin;

import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.modules.adminweb.annotations.AdminWebController;
import com.foreach.across.modules.adminweb.ui.PageContentStructure;
import com.foreach.across.modules.bootstrapui.elements.BootstrapUiFactory;
import com.foreach.across.modules.bootstrapui.elements.Grid;
import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.registry.EntityRegistry;
import com.foreach.across.modules.entity.support.EntityMessageCodeResolver;
import com.foreach.across.modules.entity.web.EntityLinkBuilder;
import com.foreach.across.modules.spring.security.actions.AllowableAction;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import com.foreach.across.modules.web.ui.elements.builder.NodeViewElementBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Lists all entity types registered in the context.
 *
 * @author Arne Vandamme
 */
@AdminWebController
@RequiredArgsConstructor
public class EntityOverviewController
{
	private final EntityRegistry entityRegistry;
	private final BootstrapUiFactory bootstrapUiFactory;
	private final PageContentStructure pageContentStructure;

	@RequestMapping(GenericEntityViewController.ROOT_PATH)
	public String listAllEntityTypes() {
		Map<String, List<EntityConfiguration>> entitiesByGroup = entityRegistry
				.getEntities()
				.stream()
				.sorted( Comparator.comparing( EntityConfiguration::getDisplayName ) )
				.filter( c -> !c.isHidden() && c.getAllowableActions().contains( AllowableAction.READ ) )
				.collect( Collectors.groupingBy( this::determineGroupName ) );

		NodeViewElementBuilder row = bootstrapUiFactory.row();

		entitiesByGroup.forEach( ( groupName, entities ) -> {
			NodeViewElementBuilder body = bootstrapUiFactory.div().css( "panel-body" );

			entities.forEach( entityConfiguration -> {
				EntityLinkBuilder linkBuilder = entityConfiguration.getAttribute( EntityLinkBuilder.class );
				EntityMessageCodeResolver codeResolver = entityConfiguration.getEntityMessageCodeResolver();

				body.add(
						bootstrapUiFactory.paragraph().add(
								bootstrapUiFactory.link()
								                  .text( codeResolver.getNameSingular() )
								                  .url( linkBuilder.overview() )
						)
				);
			} );

			row.add(
					bootstrapUiFactory.column( Grid.Device.MD.width( 3 ) )
					                  .add(
							                  bootstrapUiFactory.div()
							                                    .css( "panel", "panel-primary" )
							                                    .add(
									                                    bootstrapUiFactory
											                                    .div()
											                                    .css( "panel-heading" )
											                                    .add(
													                                    bootstrapUiFactory.node( "h3" )
													                                                      .css( "panel-title" )
													                                                      .add( TextViewElement.text( groupName ) )
											                                    )
							                                    )
							                                    .add( body )
					                  )
			);
		} );

		pageContentStructure.addChild( row.build() );

		return PageContentStructure.TEMPLATE;
	}

	private String determineGroupName( EntityConfiguration entityConfiguration ) {
		return entityConfiguration.hasAttribute( AcrossModuleInfo.class )
				? entityConfiguration.getAttribute( AcrossModuleInfo.class ).getName()
				: "Other";
	}
}
