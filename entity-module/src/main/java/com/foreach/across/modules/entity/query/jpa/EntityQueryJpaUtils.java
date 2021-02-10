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
package com.foreach.across.modules.entity.query.jpa;

import com.foreach.across.modules.entity.query.EntityQuery;
import com.foreach.across.modules.entity.query.EntityQueryCondition;
import com.foreach.across.modules.entity.query.EntityQueryExpression;
import com.foreach.across.modules.entity.query.EntityQueryOps;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyRegistry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author Arne Vandamme
 */
public abstract class EntityQueryJpaUtils
{
	private EntityQueryJpaUtils() {
	}

	public static <V> Specification<V> toSpecification( final EntityQuery query ) {
		return ( root, criteriaQuery, cb ) -> EntityQueryJpaUtils.buildPredicate( query, root, criteriaQuery, cb );
	}

	private static <V> Predicate buildPredicate( EntityQueryExpression expression, Root<V> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb ) {
		if ( expression instanceof EntityQueryCondition ) {
			return buildConditionPredicate( (EntityQueryCondition) expression, root, criteriaQuery, cb );
		}
		else {
			return buildQueryPredicate( (EntityQuery) expression, root, criteriaQuery, cb );
		}
	}

	@SuppressWarnings("unchecked")
	private static <V> Predicate buildConditionPredicate( EntityQueryCondition condition,
	                                                      Root<V> root,
	                                                      CriteriaQuery<?> criteriaQuery,
	                                                      CriteriaBuilder cb ) {
		if ( condition.getFirstArgument() instanceof EntityQueryConditionJpaFunctionHandler ) {
			return ( (EntityQueryConditionJpaFunctionHandler) condition.getFirstArgument() ).apply( condition ).toPredicate( root, criteriaQuery, cb );
		}
		switch ( condition.getOperand() ) {
			case IS_NULL:
				return cb.isNull( resolveProperty( root, condition.getProperty() ) );
			case IS_NOT_NULL:
				return cb.isNotNull( resolveProperty( root, condition.getProperty() ) );
			case EQ:
				return cb.equal( resolveProperty( root, condition.getProperty() ), condition.getFirstArgument() );
			case NEQ:
				return cb.notEqual( resolveProperty( root, condition.getProperty() ), condition.getFirstArgument() );
			case GT: {
				Expression<Comparable> p = (Expression<Comparable>) resolveProperty( root, condition.getProperty() );
				return cb.greaterThan( p, (Comparable) condition.getFirstArgument() );
			}
			case GE: {
				Expression<Comparable> p = (Expression<Comparable>) resolveProperty( root, condition.getProperty() );
				return cb.greaterThanOrEqualTo( p, (Comparable) condition.getFirstArgument() );
			}
			case LT: {
				Expression<Comparable> p = (Expression<Comparable>) resolveProperty( root, condition.getProperty() );
				return cb.lessThan( p, (Comparable) condition.getFirstArgument() );
			}
			case LE: {
				Expression<Comparable> p = (Expression<Comparable>) resolveProperty( root, condition.getProperty() );
				return cb.lessThanOrEqualTo( p, (Comparable) condition.getFirstArgument() );
			}
			case CONTAINS: {
				Expression<Collection> collection
						= (Expression<Collection>) resolveProperty( root, condition.getProperty() );
				return cb.isMember( condition.getFirstArgument(), collection );
			}
			case NOT_CONTAINS: {
				Expression<Collection> collection
						= (Expression<Collection>) resolveProperty( root, condition.getProperty() );
				return cb.isNotMember( condition.getFirstArgument(), collection );
			}
			case IS_EMPTY: {
				Path<?> path = resolveProperty( root, condition.getProperty() );

				if ( path.getModel() instanceof SingularAttribute ) {
					throw new IllegalArgumentException( "Unable to perform 'IS EMPTY' on single value property - use 'IS NULL' instead" );
				}

				Expression<Collection<?>> collection = (Expression<Collection<?>>) path;
				return cb.isEmpty( collection );
			}
			case IS_NOT_EMPTY: {
				Path<?> path = resolveProperty( root, condition.getProperty() );

				if ( path.getModel() instanceof SingularAttribute ) {
					throw new IllegalArgumentException( "Unable to perform 'IS NOT EMPTY' on single value property - use 'IS NOT NULL' instead" );
				}

				Expression<Collection<?>> collection = (Expression<Collection<?>>) path;
				return cb.isNotEmpty( collection );
			}
			case IN:
				return resolveProperty( root, condition.getProperty() ).in( condition.getArguments() );
			case NOT_IN:
				return cb.not( resolveProperty( root, condition.getProperty() ).in( condition.getArguments() ) );
			case LIKE: {
				Expression<String> p = (Expression<String>) resolveProperty( root, condition.getProperty() );
				return cb.like( p, toEscapedString( condition.getFirstArgument() ), ';' );
			}
			case LIKE_IC: {
				Expression<String> p = (Expression<String>) resolveProperty( root, condition.getProperty() );
				return cb.like( cb.lower( p ), StringUtils.lowerCase( toEscapedString( condition.getFirstArgument() ) ), ';' );
			}
			case NOT_LIKE: {
				Expression<String> p = (Expression<String>) resolveProperty( root, condition.getProperty() );
				return cb.notLike( p, toEscapedString( condition.getFirstArgument() ), ';' );
			}
			case NOT_LIKE_IC: {
				Expression<String> p = (Expression<String>) resolveProperty( root, condition.getProperty() );
				return cb.notLike( cb.lower( p ), StringUtils.lowerCase( toEscapedString( condition.getFirstArgument() ) ), ';' );
			}
		}

		throw new IllegalArgumentException( "Unsupported operand for JPA query: " + condition.getOperand() );
	}

	/**
	 * Convert object to string value, automatically escape underscores, and replace
	 * the EQ escape character backslash (<strong>\</strong>) by semi-colon (<strong>;</strong>).
	 */
	public static String toEscapedString( Object argument ) {
		String escaped = Objects.toString( argument );
		escaped = StringUtils.replace( escaped, "\\_", "_" );
		escaped = StringUtils.replace( escaped, "_", "\\_" );
		escaped = StringUtils.replace( escaped, ";", "\\;" );
		escaped = StringUtils.replace( escaped, "\\\\", "\\ESCAPE\\" );
		escaped = StringUtils.replace( escaped, "\\", ";" );
		escaped = StringUtils.replace( escaped, ";ESCAPE;", "\\" );
		return escaped;
	}

	private static Path<?> resolveProperty( Path<?> path, String propertyName ) {
		int ix = propertyName.indexOf( "." );
		if ( ix >= 0 ) {
			String name = StringUtils.left( propertyName, ix );
			String remainder = StringUtils.substring( propertyName, ix + 1 );

			Path<?> nestedPath;
			if ( StringUtils.endsWith( name, EntityPropertyRegistry.INDEXER ) ) {
				name = StringUtils.removeEnd( name, EntityPropertyRegistry.INDEXER );
				nestedPath = path.get( name );
				if ( Collection.class.isAssignableFrom( nestedPath.getJavaType() ) ) {
					nestedPath = ( (From<?, ?>) path ).join( name );
				}
			}
			else {
				nestedPath = path.get( name );
			}

			return resolveProperty( nestedPath, remainder );
		}

		return path.get( propertyName );
	}

	private static <V> Predicate buildQueryPredicate( EntityQuery query, Root<V> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb ) {
		List<Predicate> predicates = new ArrayList<>();

		for ( EntityQueryExpression expression : query.getExpressions() ) {
			predicates.add( buildPredicate( expression, root, criteriaQuery, cb ) );
		}

		return query.getOperand() == EntityQueryOps.AND
				? cb.and( predicates.toArray( new Predicate[0] ) )
				: cb.or( predicates.toArray( new Predicate[0] ) );
	}
}
