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

import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.support.EntityMessageCodeResolver;
import com.foreach.across.modules.entity.views.elements.*;
import com.foreach.across.modules.entity.views.elements.button.ButtonViewElement;
import com.foreach.across.modules.entity.views.elements.container.ContainerViewElement;
import com.foreach.across.modules.entity.views.elements.table.TableViewElement;
import com.foreach.across.modules.entity.views.support.EntityMessages;
import com.foreach.across.modules.entity.views.support.ListViewEntityMessages;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.ui.ModelMap;

import java.util.Collection;

/**
 * Handles a list of items (entities) with support for the properties to show,
 * paging, sorting and configuring the sortable properties.
 *
 * @author Arne Vandamme
 */
public class EntityListViewFactory<V extends ViewCreationContext> extends ConfigurablePropertiesEntityViewFactorySupport<V, EntityListView>
{
	private int pageSize = 50;
	private boolean showResultNumber = true;

	private Sort defaultSort;
	private Collection<String> sortableProperties;
	private EntityListViewPageFetcher pageFetcher;

	public EntityListViewPageFetcher getPageFetcher() {
		return pageFetcher;
	}

	/**
	 * @param pageFetcher The ListViewPageFetcher to use for retrieving the actual items.
	 */
	public void setPageFetcher( EntityListViewPageFetcher pageFetcher ) {
		this.pageFetcher = pageFetcher;
	}

	public int getPageSize() {
		return pageSize;
	}

	/**
	 * @param pageSize The default page size to use if no custom Pageable is passed.
	 */
	public void setPageSize( int pageSize ) {
		this.pageSize = pageSize;
	}

	public Sort getDefaultSort() {
		return defaultSort;
	}

	/**
	 * @param defaultSort The default sort to use if no custom Pageable or Sort is passed.
	 */
	public void setDefaultSort( Sort defaultSort ) {
		this.defaultSort = defaultSort;
	}

	public Collection<String> getSortableProperties() {
		return sortableProperties;
	}

	/**
	 * @param sortableProperties Names of the properties that should be sortable in the UI.
	 */
	public void setSortableProperties( Collection<String> sortableProperties ) {
		this.sortableProperties = sortableProperties;
	}

	public boolean isShowResultNumber() {
		return showResultNumber;
	}

	/**
	 * @param showResultNumber True if the index of an entity in the total results should be displayed.
	 */
	public void setShowResultNumber( boolean showResultNumber ) {
		this.showResultNumber = showResultNumber;
	}

	@Override
	protected EntityListView createEntityView( ModelMap model ) {
		return new EntityListView( model );
	}

	@Override
	protected void extendViewModel( V viewCreationContext, final EntityListView view ) {
		Pageable pageable = buildPageable( view );
		Page page = getPageFetcher().fetchPage( viewCreationContext, pageable, view );

		view.setPageable( pageable );
		view.setPage( page );
		view.setShowResultNumber( isShowResultNumber() );

		TableViewElement table = new TableViewElement();
		table.setName( "resultsTable" );
		table.setPage( page );
		table.setColumns( (Iterable<ViewElement>) view.getEntityProperties().remove( "table" ) );

		ContainerViewElement buttons = new ContainerViewElement( "buttons" );

		EntityMessages messages = view.getEntityMessages();

		ButtonViewElement create = new ButtonViewElement();
		create.setName( "btn-create" );
		create.setElementType( CommonViewElements.LINK_BUTTON );
		create.setLink( view.getEntityLinkBuilder().create() );
		create.setLabel( messages.createAction() );
		buttons.add( create );

		ButtonViewElement edit = new ButtonViewElement() {
			@Override
			public String print( Object entity ) {
				return view.getEntityLinkBuilder().update( entity );
			}
		};
		edit.setName( "btn-edit" );
		edit.setElementType( CommonViewElements.LINK_BUTTON );
		edit.setLabel( messages.updateAction() );

		((ViewElements) table.getColumns()).add( edit );

		view.getEntityProperties().addFirst( table );
		view.getEntityProperties().addFirst( buttons );
	}

	private Pageable buildPageable( EntityListView view ) {
		Pageable existing = view.getPageable();

		if ( existing == null ) {
			existing = new PageRequest( 0, getPageSize(), getDefaultSort() );
		}

		return existing;
	}

	@Override
	protected EntityMessages createEntityMessages( EntityMessageCodeResolver codeResolver ) {
		return new ListViewEntityMessages( codeResolver );
	}

	@Override
	protected ViewElement createPropertyView( ViewElementBuilderContext builderContext,
	                                          EntityPropertyDescriptor descriptor ) {
		SortablePropertyViewElement sortablePropertyView = new SortablePropertyViewElement(
				super.createPropertyView( builderContext, descriptor )
		);
		sortablePropertyView.setSortableProperty( determineSortableProperty( descriptor ) );

		return sortablePropertyView;
	}

	@Override
	protected ViewElements customizeViewElements( ViewElements elements ) {
		ContainerViewElement root = new ContainerViewElement( "root" );

		// Props are in fact the table members
		ContainerViewElement table = new ContainerViewElement( "table" );
		table.addAll( elements );

		root.add( table );

		return root;
	}

	private String determineSortableProperty( EntityPropertyDescriptor descriptor ) {
		String sortableProperty = descriptor.getAttribute( EntityAttributes.SORTABLE_PROPERTY, String.class );

		if ( sortableProperties != null && !sortableProperties.contains( descriptor.getName() ) ) {
			sortableProperty = null;
		}

		return sortableProperty;
	}

	@Override
	protected ViewElementMode getMode() {
		return ViewElementMode.FOR_READING;
	}

	@Deprecated
	public static class SortablePropertyViewElement implements ViewElement
	{
		private final ViewElement wrapped;
		private String sortableProperty;

		public SortablePropertyViewElement( ViewElement wrapped ) {
			this.wrapped = wrapped;
		}

		@Override
		public String getElementType() {
			return "sortable-property";
		}

		@Override
		public String getName() {
			return wrapped.getName();
		}

		@Override
		public String getLabel() {
			return wrapped.getLabel();
		}

		@Override
		public String getCustomTemplate() {
			return wrapped.getCustomTemplate();
		}

		@Override
		public Object value( Object entity ) {
			return wrapped.value( entity );
		}

		@Override
		public String print( Object entity ) {
			return wrapped.print( entity );
		}

		public boolean isSortable() {
			return sortableProperty != null;
		}

		public String getSortableProperty() {
			return sortableProperty;
		}

		public void setSortableProperty( String sortableProperty ) {
			this.sortableProperty = sortableProperty;
		}

		@Override
		public boolean isField() {
			return false;
		}
	}
}
