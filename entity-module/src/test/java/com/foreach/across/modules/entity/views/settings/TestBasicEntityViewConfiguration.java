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

package com.foreach.across.modules.entity.views.settings;

import com.foreach.across.modules.entity.config.AttributeRegistrar;
import com.foreach.across.modules.entity.config.builders.EntityViewFactoryBuilder;
import com.foreach.across.modules.entity.views.EntityViewFactoryAttributes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.BiPredicate;

import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
@ExtendWith(MockitoExtension.class)
public class TestBasicEntityViewConfiguration
{
	@Mock(answer = Answers.RETURNS_SELF)
	private EntityViewFactoryBuilder builder;

	private BasicEntityViewSettings configuration = new BasicEntityViewSettings();

	@Test
	public void emptyConfigurationDoesNothing() {
		configuration.accept( builder );
		verifyNoInteractions( builder );
	}

	@Test
	@SuppressWarnings("unchecked")
	public void adminMenu() {
		configuration.adminMenu( "/test" ).accept( builder );
		verify( builder ).attribute( any( AttributeRegistrar.class ) );
		verifyNoMoreInteractions( builder );
	}

	@Test
	public void accessValidator() {
		configuration.accessValidator( ( viewFactory, viewContext ) -> true ).accept( builder );
		verify( builder ).attribute( eq( EntityViewFactoryAttributes.ACCESS_VALIDATOR ), any( BiPredicate.class ) );
		verifyNoMoreInteractions( builder );
	}
}
