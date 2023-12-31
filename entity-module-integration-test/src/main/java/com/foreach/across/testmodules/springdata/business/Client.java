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

package com.foreach.across.testmodules.springdata.business;

import com.foreach.across.modules.hibernate.business.SettableIdBasedEntity;
import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Set;

@Entity
@Table(name = "cl")
public class Client extends SettableIdBasedEntity<Client>
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_client_id")
	@GenericGenerator(
			name = "seq_client_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@org.hibernate.annotations.Parameter(name = "sequenceName", value = "seq_client_id"),
					@org.hibernate.annotations.Parameter(name = "allocationSize", value = "1")
			}
	)
	private Long id;

	@NotBlank
	@Column(unique = true)
	private String name;

	@ManyToOne
	private Company company;

	@OneToMany(mappedBy = "id.client")
	private Set<ClientGroup> groups;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable
	private Set<String> aliases;

	@ElementCollection
	@CollectionTable
	private Set<Phone> phones;

	public Client() {
	}

	public Client( String name ) {
		this.name = name;
	}

	public Client( String name, Company company ) {
		this.name = name;
		this.company = company;
	}

	public Long getId() {
		return id;
	}

	public void setId( Long id ) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public String getNameWithId() {
		return String.format( "%s (%s)", getName(), getId() );
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany( Company company ) {
		this.company = company;
	}

	public Set<ClientGroup> getGroups() {
		return groups;
	}

	public void setGroups( Set<ClientGroup> groups ) {
		this.groups = groups;
	}

	public Set<String> getAliases() {
		return aliases;
	}

	public void setAliases( Set<String> aliases ) {
		this.aliases = aliases;
	}

	public Set<Phone> getPhones() {
		return phones;
	}

	public void setPhones( Set<Phone> phones ) {
		this.phones = phones;
	}
}
