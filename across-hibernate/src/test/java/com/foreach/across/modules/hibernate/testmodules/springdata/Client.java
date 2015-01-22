package com.foreach.across.modules.hibernate.testmodules.springdata;

import com.foreach.across.modules.hibernate.business.SettableIdBasedEntity;
import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;

@Entity
public class Client extends SettableIdBasedEntity<Client>
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_test_client_id")
	@GenericGenerator(
			name = "seq_test_client_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@org.hibernate.annotations.Parameter(name = "sequenceName", value = "seq_test_client_id"),
					@org.hibernate.annotations.Parameter(name = "allocationSize", value = "1")
			}
	)
	private Long id;

	@Column(unique = true)
	private String name;

	public Client() {
	}

	public Client( String name ) {
		this.name = name;
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

	@Override
	public Client toDto() {
		Client client = new Client();
		BeanUtils.copyProperties( this, client );

		return client;
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		Client client = (Client) o;

		if ( name != null ? !name.equals( client.name ) : client.name != null ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : 0;
	}
}
