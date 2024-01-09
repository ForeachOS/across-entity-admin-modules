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

package com.foreach.across.modules.entity.config.builders;

import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.processors.SimpleEntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.processors.support.EntityViewProcessorRegistry;
import com.foreach.across.modules.entity.views.processors.support.EntityViewProcessorRegistry.EntityViewProcessorRegistration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 4.0.0
 */
@ExtendWith(MockitoExtension.class)
class TestEntityViewProcessorConfigurer
{
	@Mock
	private EntityViewProcessorRegistry registry;

	@Mock
	private AutowireCapableBeanFactory beanFactory;

	@InjectMocks
	private EntityViewProcessorConfigurer<?> configurer;

	@Test
	void nothingToBeDone() {
		configurer.applyTo( registry );
		verifyNoInteractions( beanFactory );
	}

	@Test
	void nameIsResolvedFromTypeIfNotSet() {
		EntityViewProcessorConfigurer<MyViewProcessor> typed = configurer.withType( MyViewProcessor.class );
		typed.applyTo( registry );

		verifyNoInteractions( beanFactory );
		verify( registry ).getProcessorRegistration( MyViewProcessor.class.getName() );
	}

	@Test
	void registerNewProcessorUsingGetBeanByType( @Mock MyViewProcessor processor ) {
		EntityViewProcessorConfigurer<MyViewProcessor> typed = configurer.getBean( MyViewProcessor.class );

		when( registry.getProcessorRegistration( MyViewProcessor.class.getName() ) ).thenReturn( Optional.empty() );
		when( beanFactory.getBean( MyViewProcessor.class ) ).thenReturn( processor );

		typed.applyTo( registry );

		verify( registry ).addProcessor( MyViewProcessor.class.getName(), processor );
	}

	@Test
	void registerNewProcessorUsingGetBeanByName( @Mock MyViewProcessor processor ) {
		configurer = configurer.getBean( "someBean" );

		when( registry.getProcessorRegistration( "someBean" ) ).thenReturn( Optional.empty() );
		when( beanFactory.getBean( "someBean" ) ).thenReturn( processor );

		configurer.applyTo( registry );

		verify( registry ).addProcessor( "someBean", processor );
	}

	@Test
	void registerNewProcessorUsingCreateBean( @Mock MyViewProcessor processor ) {
		EntityViewProcessorConfigurer<MyViewProcessor> typed = configurer.createBean( MyViewProcessor.class );

		when( registry.getProcessorRegistration( MyViewProcessor.class.getName() ) ).thenReturn( Optional.empty() );
		when( beanFactory.createBean( MyViewProcessor.class ) ).thenReturn( processor );

		typed.applyTo( registry );

		verify( registry ).addProcessor( MyViewProcessor.class.getName(), processor );
	}

	@Test
	void exceptionIsThrownIfProcessorIsAlreadyPresentAndNoUpdate( @Mock EntityViewProcessorRegistration existing ) {
		configurer.createBean( MyViewProcessor.class ).order( 1 );

		when( registry.getProcessorRegistration( MyViewProcessor.class.getName() ) ).thenReturn( Optional.of( existing ) );

		assertThatExceptionOfType( IllegalStateException.class )
				.isThrownBy( () -> configurer.applyTo( registry ) );
	}

	@Test
	void skipIfProcessorIsAlreadyPresentButFlagIsSet( @Mock EntityViewProcessorRegistration existing ) {
		configurer.createBean( MyViewProcessor.class ).order( 1 ).skipIfPresent();

		when( registry.getProcessorRegistration( MyViewProcessor.class.getName() ) ).thenReturn( Optional.of( existing ) );

		configurer.applyTo( registry );

		verify( registry ).getProcessorRegistration( MyViewProcessor.class.getName() );
		verifyNoInteractions( existing );
		verifyNoMoreInteractions( registry );
	}

	@Test
	void updateIfProcessorIsPresentButFlagIsSet( @Mock EntityViewProcessorRegistration existing, @Mock MyViewProcessor processor ) {
		configurer.createBean( MyViewProcessor.class ).order( 1 ).updateIfPresent();

		when( existing.getProcessor() ).thenReturn( processor );
		when( registry.getProcessorRegistration( MyViewProcessor.class.getName() ) ).thenReturn( Optional.of( existing ) );

		configurer.applyTo( registry );

		verify( existing ).setOrder( 1 );
		verifyNoMoreInteractions( existing );
	}

	@Test
	void replaceProcessorIfPresentButFlagIsSet( @Mock EntityViewProcessorRegistration existing, @Mock MyViewProcessor processor ) {
		configurer.provideBean( processor ).replaceIfPresent();

		when( registry.getProcessorRegistration( MyViewProcessor.class.getName() ) ).thenReturn( Optional.of( existing ) );

		configurer.applyTo( registry );

		verify( existing ).setProcessor( processor );
		verifyNoMoreInteractions( existing );
	}

	@Test
	void exceptionIsThrownAttemptingToUpdateProcessorOfDifferentType( @Mock EntityViewProcessorRegistration existing,
	                                                                  @Mock EntityViewProcessorAdapter processor ) {
		when( existing.getProcessor() ).thenReturn( processor );
		when( registry.getProcessorRegistration( MyViewProcessor.class.getName() ) ).thenReturn( Optional.of( existing ) );

		configurer.withType( MyViewProcessor.class ).order( 10 );

		assertThatExceptionOfType( IllegalStateException.class )
				.isThrownBy( () -> configurer.applyTo( registry ) );
	}

	@Test
	void processorUpdatedIfNoTypeSpecified( @Mock EntityViewProcessorRegistration existing,
	                                        @Mock EntityViewProcessorAdapter processor ) {
		when( existing.getProcessor() ).thenReturn( processor );
		when( registry.getProcessorRegistration( MyViewProcessor.class.getName() ) ).thenReturn( Optional.of( existing ) );

		configurer.withName( MyViewProcessor.class.getName() ).order( 10 );

		configurer.applyTo( registry );

		verify( existing ).setOrder( 10 );
		verifyNoMoreInteractions( existing );
	}

	@Test
	void skipWhenUpdatingProcessorOfDifferentTypeIfFlagIsSet( @Mock EntityViewProcessorRegistration existing, @Mock EntityViewProcessorAdapter processor ) {
		when( existing.getProcessor() ).thenReturn( processor );
		when( registry.getProcessorRegistration( MyViewProcessor.class.getName() ) ).thenReturn( Optional.of( existing ) );

		configurer.withType( MyViewProcessor.class ).order( 10 ).skipIfDifferentType();

		configurer.applyTo( registry );

		verify( existing, never() ).setOrder( 10 );
	}

	@Test
	void exceptionIsThrownAttemptingToUpdateMissingProcessor() {
		when( registry.getProcessorRegistration( MyViewProcessor.class.getName() ) ).thenReturn( Optional.empty() );

		configurer.withType( MyViewProcessor.class ).order( 10 );

		assertThatExceptionOfType( IllegalStateException.class )
				.isThrownBy( () -> configurer.applyTo( registry ) );
	}

	@Test
	void skipWhenUpdatingMissingProcessorAndFlagIsSet() {
		when( registry.getProcessorRegistration( MyViewProcessor.class.getName() ) ).thenReturn( Optional.empty() );

		configurer.withType( MyViewProcessor.class ).order( 10 ).skipIfMissing();

		configurer.applyTo( registry );
		verify( registry ).getProcessorRegistration( MyViewProcessor.class.getName() );
		verifyNoMoreInteractions( registry );
	}

	@Test
	void removingProcessorWhichIsNotPresentDoesNotFail() {
		when( registry.getProcessorRegistration( MyViewProcessor.class.getName() ) ).thenReturn( Optional.empty() );

		configurer.withType( MyViewProcessor.class ).remove();

		configurer.applyTo( registry );
		verify( registry ).getProcessorRegistration( MyViewProcessor.class.getName() );
		verifyNoMoreInteractions( registry );
	}

	private static class MyViewProcessor extends SimpleEntityViewProcessorAdapter
	{
	}
}
