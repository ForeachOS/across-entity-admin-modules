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

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
public class TestEntityQueryTokenConverter
{
	private EntityQueryTokenConverter converter;

	@Before
	public void before() {
		converter = new EntityQueryTokenConverter();
	}

	@Test
	public void emptyTokensReturnsAllQuery() {
		assertEquals( EntityQuery.all(), convert() );
	}

	@Test
	public void simpleQueryWithValue() {
		assertEquals(
				EntityQuery.and( new EntityQueryCondition( "value", EntityQueryOps.EQ, new EQValue( "123" ) ) ),
				convert( "value", "=", "123" )
		);
	}

	@Test
	public void simpleQueryWithMultiplePredicates() {
		assertEquals(
				EntityQuery.or(
						new EntityQueryCondition( "value", EntityQueryOps.EQ, new EQValue( "123" ) ),
						new EntityQueryCondition( "name", EntityQueryOps.CONTAINS, new EQValue( "abc" ) )
				),
				convert( "value", "=", "123", "or", "name", "contains", "abc" )
		);
		assertEquals(
				EntityQuery.and(
						new EntityQueryCondition( "value", EntityQueryOps.EQ, new EQValue( "123" ) ),
						new EntityQueryCondition( "name", EntityQueryOps.CONTAINS, new EQValue( "abc" ) ),
						new EntityQueryCondition( "time", EntityQueryOps.NEQ, new EQValue( "2" ) )
				),
				convert( "value", "=", "123", "and", "name", "contains", "abc", "and", "time", "!=", "2" )
		);
	}

	@Test
	public void unGroupedDifferentPredicateOperatorsAreNotAllowed() {
		expectError(
				"Illegal keyword or - cannot combine and/or on the same level without explicit grouping", 70,
				"value", "=", "123", "and", "name", "contains", "abc", "or", "time", "!=", "2"
		);
		expectError(
				"Illegal keyword and - cannot combine and/or on the same level without explicit grouping", 70,
				"value", "=", "123", "or", "name", "contains", "abc", "and", "time", "!=", "2"
		);
	}

	@Test
	public void simpleQueryWithGroupedPredicates() {
		assertEquals(
				EntityQuery.or(
						new EntityQueryCondition( "value", EntityQueryOps.EQ, new EQValue( "123" ) ),
						EntityQuery.and(
								new EntityQueryCondition( "name", EntityQueryOps.CONTAINS, new EQValue( "abc" ) ),
								new EntityQueryCondition( "time", EntityQueryOps.NEQ, new EQValue( "2" ) )
						)

				),
				convert( "value", "=", "123", "or", "(", "name", "contains", "abc", "and", "time", "!=", "2", ")" )
		);
	}

	@Test
	public void casingOfKeywordsIsIgnored() {
		assertEquals(
				EntityQuery.or(
						new EntityQueryCondition( "value", EntityQueryOps.EQ, new EQValue( "123" ) ),
						EntityQuery.and(
								new EntityQueryCondition( "name", EntityQueryOps.CONTAINS, new EQValue( "abc" ) ),
								new EntityQueryCondition( "time", EntityQueryOps.NEQ, new EQValue( "2" ) )
						),
						EntityQuery.or(
								new EntityQueryCondition( "name", EntityQueryOps.CONTAINS, new EQValue( "abc" ) ),
								new EntityQueryCondition( "time", EntityQueryOps.NEQ, new EQValue( "2" ) )
						)

				),
				convert( "value", "=", "123", "OR", "(", "name", "contains", "abc", "AND", "time", "!=", "2", ")", "or",
				         "(", "name", "CONTAINS", "abc", "OR", "time", "!=", "2", ")" )
		);
	}

	@Test
	public void illegalField() {
		expectError(
				"Illegal field: and", 0,
				"and"
		);
		expectError(
				"Illegal field: !=", 0,
				"!="
		);
	}

	@Test
	public void missingField() {
		expectError(
				"Missing expected field at position 33", 33,
				"a", "=", "b", "and"
		);

		expectError(
				"Missing expected field at position 41", 41,
				"a", "=", "b", "and", "("
		);
	}

	@Test
	public void operatorMissing() {
		expectError(
				"Missing operator for: value", 5,
				"value"
		);
	}

	@Test
	public void illegalOperator() {
		expectError(
				"Illegal operator: 123", 10,
				"value", "123"
		);
	}

	@Test
	public void illegalValue() {
		expectError(
				"Missing keyword and/or before: 456", 30,
				"value", "=", "123", "456"
		);
	}

	@Test
	public void valueMissing() {
		expectError(
				"Missing value after: name = ", 11,
				"name", "="
		);
	}

	@Test
	public void simpleQueryWithStringLiteralValue() {
		assertEquals(
				EntityQuery.and( new EntityQueryCondition( "value", EntityQueryOps.EQ, new EQString( "123 456" ) ) ),
				convert( "value", "=", "'123 456'" )
		);

		assertEquals(
				EntityQuery.and( new EntityQueryCondition( "value", EntityQueryOps.EQ, new EQString( "123 456" ) ) ),
				convert( "value", "=", "\"123 456\"" )
		);

		assertEquals(
				EntityQuery.and( new EntityQueryCondition( "value", EntityQueryOps.EQ, new EQString( "" ) ) ),
				convert( "value", "=", "''" )
		);
	}

	@Test
	public void unbalancedStringLiteral() {
		expectError(
				"Missing token ' after: '123", 24,
				"value", "=", "'123"
		);
	}

	@Test
	public void operatorWithMultipleKeywords() {
		assertEquals(
				EntityQuery.and(
						new EntityQueryCondition( "value", EntityQueryOps.NOT_CONTAINS, new EQString( "test" ) )
				),
				convert( "value", "not", "contains", "'test'" )
		);
	}

	@Test
	public void emptyGroupValueIsNotAllowed() {
		expectError(
				"Illegal token: )", 30,
				"value", "contains", "(", ")"
		);
	}

	@Test
	public void singleElementGroupValue() {
		assertEquals(
				EntityQuery.and(
						new EntityQueryCondition( "value", EntityQueryOps.CONTAINS,
						                          new EQGroup( Collections.singleton( new EQValue( "123" ) ) ) )
				),
				convert( "value", "contains", "(", "123", ")" )
		);
	}

	@Test
	public void multiElementGroupValues() {
		assertEquals(
				EntityQuery.and(
						new EntityQueryCondition(
								"value",
								EntityQueryOps.CONTAINS,
								new EQGroup( Arrays.asList( new EQValue( "123" ), new EQString( "test" ) ) )
						)
				),
				convert( "value", "contains", "(", "123", ",", "'test'", ")" )
		);
	}

	@Test
	public void unbalancedGroupValues() {
		expectError(
				"Missing token ) after: 123", 33,
				"value", "contains", "(", "123"
		);
		expectError(
				"Missing token ) after: 'test'", 56,
				"value", "contains", "(", "123", ",", "'test'"
		);
	}

	@Test
	public void missingGroupValue() {
		expectError(
				"Missing value after: (", 21,
				"value", "contains", "("
		);
		expectError(
				"Illegal token: )", 70,
				"value", "contains", "(", "123", ",", "'test me'", ",", ")"
		);
		expectError(
				"Illegal token: ,", 30,
				"value", "contains", "(", ",", "123", ")"
		);
	}

	@Test
	public void illegalGroupValue() {
		expectError(
				"Illegal token: 456", 40,
				"value", "contains", "(", "123", "456", ")"
		);
		expectError(
				"Illegal token: !=", 30,
				"value", "contains", "(", "!=", ")"
		);
	}

	@Test
	public void groupContainingOtherGroupIsNotAllowed() {
		expectError(
				"Illegal token: (", 50,
				"value", "contains", "(", "123", ",", "(", "456", ")", ")"
		);
	}

	@Test
	public void functionValueWithoutParameters() {
		assertEquals(
				EntityQuery.and(
						new EntityQueryCondition( "user", EntityQueryOps.EQ, new EQFunction( "currentUser" ) )
				),
				convert( "user", "=", "currentUser", "(", ")" )
		);
	}

	@Test
	public void unclosedFunctionValue() {
		expectError(
				"Missing value after: (", 31,
				"user", "=", "currentUser", "("
		);
	}

	@Test
	public void functionValueWithParameters() {
		assertEquals(
				EntityQuery.and(
						new EntityQueryCondition(
								"user", EntityQueryOps.EQ, new EQFunction(
								"currentUser", Arrays.asList( new EQString( "test" ), new EQValue( "123" ) )
						) )
				),
				convert( "user", "=", "currentUser", "(", "'test'", ",", "123", ")" )
		);
	}

	@Test
	public void groupContainingFunctionWithoutParameters() {
		assertEquals(
				EntityQuery.and(
						new EntityQueryCondition(
								"value",
								EntityQueryOps.CONTAINS,
								new EQGroup( Arrays.asList( new EQFunction( "currentUser" ), new EQString( "test" ) ) )
						)
				),
				convert( "value", "contains", "(", "currentUser", "(", ")", ",", "'test'", ")" )
		);
	}

	@Test
	public void parameterizedFunctionContainingParameterizedFunctionAsParameter() {
		EQFunction parameter = new EQFunction( "timestamp", Collections.singleton( new EQValue( "today" ) ) );

		assertEquals(
				EntityQuery.and(
						new EntityQueryCondition( "user", EntityQueryOps.EQ,
						                          new EQFunction( "currentUser",
						                                          Arrays.asList( new EQString( "456" ), parameter ) )
						)
				),
				convert( "user", "=", "currentUser", "(", "'456'", ",", "timestamp", "(", "today", ")", ")" )
		);
	}

	@Test
	public void groupContainingFunctionWithParameters() {
		EQFunction parameter = new EQFunction( "timestamp", Collections.singleton( new EQValue( "today" ) ) );
		assertEquals(
				EntityQuery.and(
						new EntityQueryCondition(
								"value",
								EntityQueryOps.CONTAINS,
								new EQGroup( Arrays.asList(
										new EQFunction( "currentUser",
										                Arrays.asList( new EQString( "456" ), parameter ) ),
										new EQString( "test" )
								) )
						)
				),
				convert( "value", "contains", "(", "currentUser", "(", "'456'", ",", "timestamp", "(", "today", ")",
				         ")", ",", "'test'", ")" )
		);
	}

	@Test
	public void groupedPredicatesWithGroupAndFunctionValues() {
		assertEquals(
				EntityQuery.or(
						new EntityQueryCondition( "value", EntityQueryOps.EQ, new EQValue( "123" ) ),
						EntityQuery.and(
								new EntityQueryCondition(
										"name", EntityQueryOps.EQ,
										new EQFunction( "currentUser", Collections.singleton( new EQString( "test" ) ) )
								),
								new EntityQueryCondition(
										"time", EntityQueryOps.CONTAINS,
										new EQGroup( Arrays.asList( new EQValue( "2" ), new EQValue( "3" ) ) )
								)
						)
				),
				convert( "value", "=", "123", "or", "(", "name", "=", "currentUser", "(", "'test'", ")", "and",
				         "time", "contains", "(", "2", ",", "3", ")", ")" )

		);
	}

	@Test
	public void verifyContextOnMissingValue() {
		try {
			convert( "a", "=", "b", "and", "c", "=" );
		}
		catch ( EntityQueryParsingException iae ) {
			assertEquals( "Missing value after: c = ", iae.getMessage() );
			assertEquals( 51, iae.getErrorExpressionPosition() );
			assertEquals( 40, iae.getContextExpressionStart() );
			assertEquals( "c =", iae.getContextExpression() );
			return;
		}

		fail( "The expected exception was not thrown" );
	}

	@Test
	public void verifyContextOnUnclosedLiteral() {
		try {
			convert( "a", "=", "b", "and", "c", "contains", "(", "'test", ")" );
		}
		catch ( EntityQueryParsingException iae ) {
			assertEquals( "Missing token ' after: 'test", iae.getMessage() );
			assertEquals( 75, iae.getErrorExpressionPosition() );
			assertEquals( 40, iae.getContextExpressionStart() );
			assertEquals( "c contains ( 'test", iae.getContextExpression() );
			return;
		}

		fail( "The expected exception was not thrown" );
	}

	@Test
	public void verifyContextOnStartOfExpression() {
		try {
			convert( "a", "=" );
		}
		catch ( EntityQueryParsingException iae ) {
			assertEquals( "Missing value after: a = ", iae.getMessage() );
			assertEquals( 11, iae.getErrorExpressionPosition() );
			assertEquals( 0, iae.getContextExpressionStart() );
			assertEquals( "a =", iae.getContextExpression() );
			return;
		}

		fail( "The expected exception was not thrown" );
	}

	@Test
	public void verifyContextOnGroupStart() {
		try {
			convert( "a", "=", "b", "and" );
		}
		catch ( EntityQueryParsingException iae ) {
			assertEquals( "Missing expected field at position 33", iae.getMessage() );
			assertEquals( 33, iae.getErrorExpressionPosition() );
			assertEquals( 30, iae.getContextExpressionStart() );
			assertEquals( "and", iae.getContextExpression() );
			return;
		}

		fail( "The expected exception was not thrown" );
	}

	private void expectError( String message, int position, String... tokens ) {
		try {
			convert( tokens );
		}
		catch ( EntityQueryParsingException iae ) {
			assertEquals( message, iae.getMessage() );
			assertEquals( position, iae.getErrorExpressionPosition() );
			return;
		}

		fail( "The expected exception was not thrown" );
	}

	private EntityQuery convert( String... tokens ) {
		List<EntityQueryTokenizer.TokenMetadata> metadata = new ArrayList<>();
		for ( int i = 0; i < tokens.length; i++ ) {
			metadata.add( new EntityQueryTokenizer.TokenMetadata( tokens[i], i * 10 ) );
		}

		return converter.convertTokens( metadata );
	}
}
