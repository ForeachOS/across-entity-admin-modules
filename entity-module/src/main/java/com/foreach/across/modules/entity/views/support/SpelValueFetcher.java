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
package com.foreach.across.modules.entity.views.support;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * @author Arne Vandamme
 */
public class SpelValueFetcher<T> implements ValueFetcher<T>
{
	private static final ExpressionParser PARSER;

	static {
		PARSER = new SpelExpressionParser();
	}

	private final String expression;

	public SpelValueFetcher( String expression ) {
		this.expression = expression;
	}

	@Override
	public Object getValue( T entity ) {
		return entity != null ? PARSER.parseExpression( expression ).getValue( entity, Object.class ) : null;
	}
}
