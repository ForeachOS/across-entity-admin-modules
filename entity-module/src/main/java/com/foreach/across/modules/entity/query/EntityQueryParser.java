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

package com.foreach.across.modules.entity.query;

import lombok.NonNull;
import org.springframework.util.Assert;

/**
 * Reverse parsing of a {@link String} into an {@link EntityQuery}.
 * Uses a {@link DefaultEntityQueryMetadataProvider} if none is set.
 *
 * @author Arne Vandamme
 * @see DefaultEntityQueryMetadataProvider
 * @since 2.0.0
 */
public class EntityQueryParser
{
	private static final EntityQueryTokenizer tokenizer = new EntityQueryTokenizer();
	private static final EntityQueryTokenConverter converter = new EntityQueryTokenConverter();

	private EntityQueryMetadataProvider metadataProvider;
	private EntityQueryTranslator queryTranslator;

	/**
	 * Set the actual {@link EntityQueryMetadataProvider} that should be used for validating and typing query strings.
	 *
	 * @param metadataProvider instance
	 */
	public void setMetadataProvider( @NonNull EntityQueryMetadataProvider metadataProvider ) {
		this.metadataProvider = metadataProvider;
	}

	/**
	 * Set the {@link DefaultEntityQueryTranslator} to use for translating raw queries that passed validation.
	 *
	 * @param queryTranslator instance
	 */
	public void setQueryTranslator( @NonNull EntityQueryTranslator queryTranslator ) {
		this.queryTranslator = queryTranslator;
	}

	public void validateProperties() {
		Assert.notNull( metadataProvider, "metadataProvider cannot be null" );
		Assert.notNull( queryTranslator, "queryTranslator cannot be null" );
	}

	/**
	 * Convert a query string into a typed and validated {@link EntityQuery}.
	 * The {@link #setMetadataProvider(EntityQueryMetadataProvider)}
	 * value will be used for validating the query, and - when passing validation - query will be translated
	 * by the {@link EntityQueryTranslator}.
	 * <p>
	 * An exception will be thrown if the query cannot be converted.
	 *
	 * @param queryString string representation of the query
	 * @return query instance
	 * @throws IllegalArgumentException if parsing fails
	 */
	public EntityQuery parse( String queryString ) {
		validateProperties();

		EntityQuery rawQuery = parseRawQuery( queryString );
		return prepare( rawQuery );
	}

	/**
	 * Convert a raw {@link EntityQuery} instance into a validated and executable
	 * query instance that can be passed to the {@link EntityQueryExecutor} of the corresponding entity.
	 * <p/>
	 * Query translation will be done by the configured {@link EntityQueryTranslator}?
	 *
	 * @param rawQuery to convert
	 * @return executable query instance
	 */
	public EntityQuery prepare( EntityQuery rawQuery ) {
		validatePropertiesAndOperators( rawQuery );

		return queryTranslator.translate( rawQuery );
	}

	private void validatePropertiesAndOperators( EntityQuery query ) {
		for ( EntityQueryExpression expression : query.getExpressions() ) {
			if ( expression instanceof EntityQueryCondition ) {
				EntityQueryCondition condition = (EntityQueryCondition) expression;
				if ( !metadataProvider.isValidProperty( condition.getProperty() ) ) {
					throw new EntityQueryParsingException.IllegalField( condition.getProperty() );
				}
				if ( !metadataProvider.isValidOperatorForProperty( condition.getOperand(),
				                                                   condition.getProperty() ) ) {
					throw new EntityQueryParsingException.IllegalOperator( condition.getOperand().getToken(),
					                                                       condition.getProperty() );
				}

				if ( condition.hasArguments() ) {
					if ( !metadataProvider.isValidValueForPropertyAndOperator( condition.getFirstArgument(),
					                                                           condition.getProperty(),
					                                                           condition.getOperand() ) ) {
						throw new EntityQueryParsingException.IllegalValue( condition.getOperand().getToken(),
						                                                    condition.getProperty() );
					}
				}
			}
			else {
				validatePropertiesAndOperators( (EntityQuery) expression );
			}
		}
	}

	/**
	 * Parse an EQL statement into a raw {@link EntityQuery}.
	 * Exceptions will be thrown in case parsing fails.
	 *
	 * @param eql query
	 * @return query
	 */
	public static EntityQuery parseRawQuery( String eql ) {
		return converter.convertTokens( tokenizer.tokenize( eql ) );
	}
}
