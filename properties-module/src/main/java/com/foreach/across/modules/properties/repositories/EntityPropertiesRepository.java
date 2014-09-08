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
package com.foreach.across.modules.properties.repositories;

import com.foreach.across.modules.properties.business.StringPropertiesSource;
import com.foreach.across.modules.properties.config.EntityPropertiesDescriptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository interface for persitence of StringTypedPropertyMap instances.
 *
 * @author Arne Vandamme
 */
public class EntityPropertiesRepository<T>
{
	private final String SQL_INSERT_PROPERTY;
	private final String SQL_SELECT_PROPERTIES;
	private final String SQL_DROP_PROPERTIES;

	private final JdbcTemplate jdbcTemplate;

	public EntityPropertiesRepository( EntityPropertiesDescriptor configuration ) {
		jdbcTemplate = new JdbcTemplate( configuration.dataSource() );

		String table = configuration.tableName();
		String keyColumn = configuration.keyColumnName();

		SQL_INSERT_PROPERTY = String.format( "INSERT INTO %s (%s,property_name,property_value) VALUES (?,?,?)", table,
		                                     keyColumn );
		SQL_SELECT_PROPERTIES = String.format( "SELECT property_name, property_value FROM %s WHERE %s = ?", table,
		                                       keyColumn );
		SQL_DROP_PROPERTIES = String.format( "DELETE FROM %s WHERE %s = ?", table, keyColumn );
	}

	@Transactional(readOnly = true)
	public StringPropertiesSource loadProperties( T entityId ) {
		List<Map<String, Object>> properties = jdbcTemplate.queryForList( SQL_SELECT_PROPERTIES, entityId );

		Map<String, String> sourceMap = new HashMap<>();

		for ( Map<String, Object> entry : properties ) {
			sourceMap.put( (String) entry.get( "property_name" ), (String) entry.get( "property_value" ) );
		}

		return new StringPropertiesSource( sourceMap );
	}

	@Transactional
	public void saveProperties( T entityId, StringPropertiesSource properties ) {
		deleteProperties( entityId );

		for ( Map.Entry<String, ?> entry : properties.getProperties().entrySet() ) {
			jdbcTemplate.update( SQL_INSERT_PROPERTY, entityId, entry.getKey(), entry.getValue() );
		}
	}

	@Transactional
	public void deleteProperties( T entityId ) {
		jdbcTemplate.update( SQL_DROP_PROPERTIES, entityId );
	}
}
