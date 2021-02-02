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

package com.foreach.across.testmodules.elastic.domain.jpa.customer;

import com.foreach.across.testmodules.elastic.domain.jpa.country.Country;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "tbl_es_customer")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Customer implements Persistable<Long>
{
	@Id
	@GeneratedValue
	private Long id;

	@NotBlank
	@Length(max = 250)
	@Column(name = "first_name")
	private String firstName;

	@Length(max = 250)
	@Column(name = "last_name")
	private String lastName;

	@JoinColumn(name = "country_id")
	@ManyToOne
	private Country country;

	@Column(name = "created_date")
	private Date createdDate;

	@Column(name = "updated_date")
	private LocalDateTime updatedDate;

	@Version
	@Column(name = "version")
	private Long version;

	@Override
	public boolean isNew() {
		return Objects.isNull( getId() ) || getId() == 0;
	}
}
