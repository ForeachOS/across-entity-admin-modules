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

package com.foreach.across.modules.bootstrapui.elements;

import org.junit.Test;
import org.springframework.util.SerializationUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestStyle
{
	@Test
	public void shouldBeSerializable() {
		byte[] serialized = SerializationUtils.serialize( new Style( "custom-style" ) );
		assertNotNull( serialized );
		assertEquals( new Style( "custom-style" ), SerializationUtils.deserialize( serialized ) );

		assertEquals(
				Style.Button.LINK,
				SerializationUtils.deserialize( SerializationUtils.serialize( Style.Button.LINK ) )
		);
	}
}