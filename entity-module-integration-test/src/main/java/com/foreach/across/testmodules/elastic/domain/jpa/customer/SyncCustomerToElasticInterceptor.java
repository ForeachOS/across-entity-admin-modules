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

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.hibernate.aop.EntityInterceptorAdapter;
import com.foreach.across.testmodules.elastic.domain.elastic.country.ElasticCountryRepository;
import com.foreach.across.testmodules.elastic.domain.elastic.customer.ElasticCustomer;
import com.foreach.across.testmodules.elastic.domain.elastic.customer.ElasticCustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Exposed
public class SyncCustomerToElasticInterceptor extends EntityInterceptorAdapter<Customer>
{
	private final ElasticCustomerRepository elasticCustomerRepository;
	private final ElasticCountryRepository elasticCountryRepository;

	@Override
	public boolean handles( Class<?> entityClass ) {
		return Customer.class.isAssignableFrom( entityClass );
	}

	@Override
	public void afterCreate( Customer entity ) {
		ElasticCustomer elasticCustomer = convert( entity );
		elasticCustomerRepository.save( elasticCustomer );
	}

	private ElasticCustomer convert( Customer entity ) {
		ElasticCustomer elasticCustomer = new ElasticCustomer();
		copyOver( entity, elasticCustomer );
		return elasticCustomer;
	}

	private void copyOver( Customer entity, ElasticCustomer elasticCustomer ) {
		elasticCustomer.setId( entity.getId() );
		if ( Objects.nonNull( entity.getCountry() ) ) {
			elasticCountryRepository.findById( entity.getCountry().getId() )
			                        .ifPresent( elasticCustomer::setCountry );
		}
		elasticCustomer.setCreatedDate( entity.getCreatedDate() );
		elasticCustomer.setFirstName( entity.getFirstName() );
		elasticCustomer.setLastName( entity.getLastName() );
		elasticCustomer.setUpdatedDate( entity.getUpdatedDate() );
	}

	@Override
	public void afterUpdate( Customer entity ) {
		ElasticCustomer elasticCustomer = elasticCustomerRepository.findById( entity.getId() )
		                                                           .orElseGet( ElasticCustomer::new );
		copyOver( entity, elasticCustomer );
		elasticCustomerRepository.save( elasticCustomer );
	}

	@Override
	public void afterDelete( Customer entity ) {
		elasticCustomerRepository.findById( entity.getId() )
		                         .ifPresent( elasticCustomerRepository::delete );
	}
}
