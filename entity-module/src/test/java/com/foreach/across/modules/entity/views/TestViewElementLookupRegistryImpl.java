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

import com.foreach.across.modules.bootstrapui.elements.BootstrapUiElements;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementPostProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.foreach.across.modules.entity.views.ViewElementMode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * @author Arne Vandamme
 */
public class TestViewElementLookupRegistryImpl
{
	private ViewElementLookupRegistryImpl registry;

	@BeforeEach
	public void before() {
		registry = new ViewElementLookupRegistryImpl();
		registry.setDefaultCacheable( false );
	}

	@Test
	public void lookupRegistryIsCacheableByDefault() {
		assertTrue( new ViewElementLookupRegistryImpl().isDefaultCacheable() );
	}

	@Test
	public void typeIsAlwaysStored() {
		assertFalse( registry.isCacheable( CONTROL ) );
		assertNull( registry.getViewElementType( CONTROL ) );

		registry.setViewElementType( CONTROL, BootstrapUiElements.BUTTON );
		assertEquals( BootstrapUiElements.BUTTON, registry.getViewElementType( CONTROL ) );

		assertNull( registry.getViewElementType( LIST_LABEL ) );

		registry.reset( CONTROL );
		assertEquals( BootstrapUiElements.BUTTON, registry.getViewElementType( CONTROL ) );

		registry.setViewElementType( CONTROL, null );
		assertNull( registry.getViewElementType( CONTROL ) );
	}

	@Test
	public void builderIsOnlyStoredIfCacheable() {
		ViewElementBuilder builder = mock( ViewElementBuilder.class );
		assertFalse( registry.isCacheable( CONTROL ) );
		assertFalse( registry.isCacheable( LABEL ) );
		assertNull( registry.getViewElementBuilder( CONTROL ) );
		assertNull( registry.getViewElementBuilder( LABEL ) );

		assertFalse( registry.cacheViewElementBuilder( CONTROL, builder ) );
		assertFalse( registry.cacheViewElementBuilder( LABEL, builder ) );
		assertNull( registry.getViewElementBuilder( CONTROL ) );
		assertNull( registry.getViewElementBuilder( LABEL ) );

		registry.setCacheable( CONTROL, true );
		assertTrue( registry.isCacheable( CONTROL ) );
		assertFalse( registry.isCacheable( LABEL ) );
		assertTrue( registry.cacheViewElementBuilder( CONTROL, builder ) );
		assertFalse( registry.cacheViewElementBuilder( LABEL, builder ) );
		assertSame( builder, registry.getCachedViewElementBuilder( CONTROL ) );
		assertNull( registry.getViewElementBuilder( LABEL ) );
	}

	@Test
	public void defaultCacheable() {
		registry.setDefaultCacheable( true );

		ViewElementBuilder builder = mock( ViewElementBuilder.class );
		assertTrue( registry.isCacheable( CONTROL ) );
		assertTrue( registry.isCacheable( LABEL ) );
		assertNull( registry.getViewElementBuilder( CONTROL ) );
		assertNull( registry.getViewElementBuilder( LABEL ) );

		assertTrue( registry.cacheViewElementBuilder( CONTROL, builder ) );
		assertTrue( registry.cacheViewElementBuilder( LABEL, builder ) );
		assertNull( registry.getViewElementBuilder( CONTROL ) );
		assertNull( registry.getViewElementBuilder( LABEL ) );
		assertSame( builder, registry.getCachedViewElementBuilder( CONTROL ) );
		assertSame( builder, registry.getCachedViewElementBuilder( LABEL ) );

		registry.setCacheable( CONTROL, false );
		assertFalse( registry.isCacheable( CONTROL ) );
		assertTrue( registry.isCacheable( LABEL ) );

		assertNull( registry.getCachedViewElementBuilder( CONTROL ) );
		assertSame( builder, registry.getCachedViewElementBuilder( LABEL ) );

		assertFalse( registry.cacheViewElementBuilder( CONTROL, builder ) );
		assertTrue( registry.cacheViewElementBuilder( LABEL, builder ) );

		assertNull( registry.getCachedViewElementBuilder( CONTROL ) );
		assertSame( builder, registry.getCachedViewElementBuilder( LABEL ) );
	}

	@Test
	public void resetAndRemovingCacheableStatusRemoveStoredBuilder() {
		ViewElementBuilder builder = mock( ViewElementBuilder.class );
		registry.setCacheable( CONTROL, true );
		registry.setViewElementType( CONTROL, BootstrapUiElements.BUTTON );
		registry.cacheViewElementBuilder( CONTROL, builder );

		assertEquals( BootstrapUiElements.BUTTON, registry.getViewElementType( CONTROL ) );
		assertSame( builder, registry.getCachedViewElementBuilder( CONTROL ) );

		registry.reset( CONTROL );

		assertEquals( BootstrapUiElements.BUTTON, registry.getViewElementType( CONTROL ) );
		assertNull( registry.getCachedViewElementBuilder( CONTROL ) );

		registry.cacheViewElementBuilder( CONTROL, builder );
		assertEquals( BootstrapUiElements.BUTTON, registry.getViewElementType( CONTROL ) );
		assertSame( builder, registry.getCachedViewElementBuilder( CONTROL ) );

		registry.setCacheable( CONTROL, false );
		assertEquals( BootstrapUiElements.BUTTON, registry.getViewElementType( CONTROL ) );
		assertNull( registry.getCachedViewElementBuilder( CONTROL ) );

		assertFalse( registry.cacheViewElementBuilder( CONTROL, builder ) );
	}

	@Test
	public void fixedViewElementBuilder() {
		ViewElementBuilder builder = mock( ViewElementBuilder.class );
		registry.setViewElementBuilder( CONTROL, builder );

		assertSame( builder, registry.getViewElementBuilder( CONTROL ) );
		registry.reset( CONTROL );
		assertSame( builder, registry.getViewElementBuilder( CONTROL ) );
	}

	@Test
	public void mergeInto() {
		registry.setDefaultCacheable( true );

		ViewElementPostProcessor ppOne = mock( ViewElementPostProcessor.class );
		ViewElementPostProcessor ppTwo = mock( ViewElementPostProcessor.class );

		ViewElementLookupRegistryImpl other = new ViewElementLookupRegistryImpl();
		other.setDefaultCacheable( true );
		other.setCacheable( CONTROL, true );
		other.setCacheable( LABEL, false );
		other.setViewElementType( LIST_LABEL, "button" );
		other.setViewElementType( LIST_VALUE, "button" );
		other.cacheViewElementBuilder( ViewElementMode.VALUE, mock( ViewElementBuilder.class ) );
		other.addViewElementPostProcessor( CONTROL, ppOne );

		assertNull( other.getViewElementBuilder( ViewElementMode.VALUE ) );
		assertNotNull( other.getCachedViewElementBuilder( ViewElementMode.VALUE ) );

		registry.setCacheable( LABEL, true );
		registry.setCacheable( FORM_WRITE, false );
		registry.setViewElementType( LIST_CONTROL, "boem" );
		registry.setViewElementType( LIST_VALUE, "bla" );
		registry.cacheViewElementBuilder( FORM_READ, mock( ViewElementBuilder.class ) );
		registry.addViewElementPostProcessor( LABEL, mock( ViewElementPostProcessor.class ) );
		registry.addViewElementPostProcessor( CONTROL, ppTwo );

		registry.mergeInto( other );

		assertTrue( other.isCacheable( CONTROL ) );
		assertTrue( other.isCacheable( LABEL ) );
		assertFalse( other.isCacheable( FORM_WRITE ) );

		assertEquals( "button", other.getViewElementType( LIST_LABEL ) );
		assertEquals( "bla", other.getViewElementType( LIST_VALUE ) );
		assertEquals( "boem", other.getViewElementType( LIST_CONTROL ) );

		assertNull( other.getViewElementBuilder( FORM_READ ) );
		assertNull( other.getCachedViewElementBuilder( VALUE ) );
		assertNull( other.getViewElementBuilder( LABEL ) );

		assertTrue( other.getViewElementPostProcessors( LIST_LABEL ).isEmpty() );
		assertEquals( 1, other.getViewElementPostProcessors( LABEL ).size() );
		assertEquals( Arrays.asList( ppOne, ppTwo ), other.getViewElementPostProcessors( CONTROL ) );
	}
}

