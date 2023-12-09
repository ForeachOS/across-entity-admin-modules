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

package com.foreach.across.samples.entity.application.business;

import lombok.Data;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Entity
@Data
@Setter
public class Server implements Persistable<UUID>
{
	@Id
	@Type(type = "uuid-char")
	@GeneratedValue
	private UUID id;

	@NotBlank
	@Length(max = 255)
	private String hostname;

	@ManyToOne(fetch = FetchType.LAZY)
	private Application application;

	@Override
	@Transient
	public boolean isNew() {
		return id == null;
	}
}
