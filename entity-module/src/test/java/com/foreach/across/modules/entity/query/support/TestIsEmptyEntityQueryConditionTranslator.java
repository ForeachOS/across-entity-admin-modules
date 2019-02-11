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

package com.foreach.across.modules.entity.query.support;

import com.foreach.across.modules.entity.query.EntityQuery;
import com.foreach.across.modules.entity.query.EntityQueryCondition;
import com.foreach.across.modules.entity.query.EntityQueryConditionTranslator;
import com.foreach.across.modules.entity.query.EntityQueryOps;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Steven Gentens
 * @since 3.3.0
 */
public class TestIsEmptyEntityQueryConditionTranslator
{
	private EntityQueryConditionTranslator translator = EmptyStringEntityQueryConditionTranslator.INSTANCE;

	@Test
	public void nonInIsLeftUnmodified() {
		EntityQueryCondition condition = new EntityQueryCondition( "test", EntityQueryOps.CONTAINS, 1 );
		assertSame( condition, translator.translate( condition ) );
	}

	@Test
	public void isEmptyIsConverted() {
		assertEquals(
				EntityQuery.or(
						new EntityQueryCondition( "name", EntityQueryOps.IS_NULL ),
						new EntityQueryCondition( "name", EntityQueryOps.EQ, "" )
				),
				translator.translate( new EntityQueryCondition( "name", EntityQueryOps.IS_EMPTY ) )
		);
		assertEquals(
				EntityQuery.and(
						new EntityQueryCondition( "name", EntityQueryOps.IS_NOT_NULL ),
						new EntityQueryCondition( "name", EntityQueryOps.NEQ, "" )
				),
				translator.translate( new EntityQueryCondition( "name", EntityQueryOps.IS_NOT_EMPTY ) )
		);
	}
}
