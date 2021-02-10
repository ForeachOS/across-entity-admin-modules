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

package com.foreach.across.testmodules.elastic.domain.elastic.customer;

import com.foreach.across.testmodules.elastic.domain.elastic.country.ElasticCountry;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Arne Vandamme
 * @since 2.2.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "countrycustomeridx")
@EqualsAndHashCode(of = "id")
@Getter
@Setter
public class ElasticCustomer implements Persistable<Long>
{
	@Id
	@Field(type = FieldType.Long)
	private Long id;

	@NotBlank
	@Length(max = 250)
	@Field(type = FieldType.Keyword)

	private String firstName;

	@Length(max = 250)
	@Field(type = FieldType.Keyword)
	private String lastName;

	@Field(type = FieldType.Nested, includeInParent = true)
	private ElasticCountry country;

	@Field(type = FieldType.Date, format = DateFormat.date_optional_time)
	private Date createdDate;

	@Field(type = FieldType.Date, format = DateFormat.date_optional_time)
	private LocalDateTime updatedDate;

	@Field(type = FieldType.Nested, includeInParent = true)
	private List<ElasticContact> primaryContacts;

	@Version
	private Long version;

	@Override
	public String toString() {
		return String.format(
				"Customer[id=%s, firstName='%s', lastName='%s']",
				id, firstName, lastName );
	}

	@Override
	public boolean isNew() {
		return Objects.isNull( getId() ) || getId() == 0;
	}
}


