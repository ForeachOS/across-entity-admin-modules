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

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

/**
 * @author Andy Somers
 */
@Entity
@Table(name = "repr")
public class Representative implements Persistable<String>
{
	@Transient
	private boolean isNew;

	@Id
	@NotBlank
	@Length(max = 20)
	@Column(name = "repr_id", length = 20)
	private String id;

	@Length(max = 200)
	private String name;

	@Getter
	@Setter
	@Column(name = "rep_number")
	private Long number;

	public Representative() {
	}

	public Representative( String id, String name ) {
		this.id = id;
		this.name = name;
		setNew( true );
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public boolean isNew() {
		return isNew;
	}

	public void setNew( boolean isNew ) {
		this.isNew = isNew;
	}

	public void setId( String id ) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		Representative that = (Representative) o;

		if ( id != null ? !id.equals( that.id ) : that.id != null ) {
			return false;
		}
		if ( name != null ? !name.equals( that.name ) : that.name != null ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + ( name != null ? name.hashCode() : 0 );
		return result;
	}

	@Override
	public String toString() {
		return "Representative{" +
				"isNew=" + isNew +
				", id='" + id + '\'' +
				", name='" + name + '\'' +
				", number=" + number +
				'}';
	}
}
