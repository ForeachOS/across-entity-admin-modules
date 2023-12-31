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

package com.foreach.across.modules.entity.views.bootstrapui.processors.builder;

import com.foreach.across.modules.entity.views.bootstrapui.TextboxFormElementBuilderFactory;
import org.junit.jupiter.api.Test;

import javax.validation.groups.Default;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestEmailTypeDetectionProcessor
{
	@Test
	public void defaultGroupNotPresent() {
		TextboxFormElementBuilderFactory.EmailTypeDetectionProcessor emailTypeDetectionProcessor =
				new TextboxFormElementBuilderFactory.EmailTypeDetectionProcessor();
		assertFalse( emailTypeDetectionProcessor.hasDefaultGroup( Collections.singletonMap( "groups", new String[] { "foo" } ) ) );
	}

	@Test
	public void defaultGroupIsPresent() {
		TextboxFormElementBuilderFactory.EmailTypeDetectionProcessor emailTypeDetectionProcessor =
				new TextboxFormElementBuilderFactory.EmailTypeDetectionProcessor();
		assertTrue( emailTypeDetectionProcessor.hasDefaultGroup( Collections.emptyMap() ) );
	}

	@Test
	public void defaultGroupIsPresentAndSet() {
		TextboxFormElementBuilderFactory.EmailTypeDetectionProcessor emailTypeDetectionProcessor =
				new TextboxFormElementBuilderFactory.EmailTypeDetectionProcessor();
		assertTrue( emailTypeDetectionProcessor.hasDefaultGroup( Collections.singletonMap( "groups", new Class[] { Default.class } ) ) );
	}
}
