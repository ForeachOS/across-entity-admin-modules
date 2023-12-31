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

package com.foreach.across.modules.entity.query.elastic;

import com.foreach.across.modules.entity.query.EntityQueryCondition;
import com.foreach.across.modules.entity.query.EntityQueryConditionFunctionHandler;
import org.springframework.data.elasticsearch.core.query.Criteria;

/***
 * A {@link EntityQueryConditionFunctionHandler} for Elasticsearch.
 *
 * @author Marc Vanbrabant
 * @since 4.2.0
 */
@FunctionalInterface
public interface EntityQueryConditionElasticFunctionHandler extends EntityQueryConditionFunctionHandler<Criteria>
{
	@Override
	Criteria apply( EntityQueryCondition entityQueryCondition );
}
