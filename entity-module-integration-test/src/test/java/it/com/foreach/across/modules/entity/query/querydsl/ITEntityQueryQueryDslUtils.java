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

package it.com.foreach.across.modules.entity.query.querydsl;

import com.foreach.across.modules.entity.query.EntityQuery;
import com.foreach.across.modules.entity.query.querydsl.EntityQueryQueryDslUtils;
import com.foreach.across.testmodules.springdata.business.Company;
import com.foreach.across.testmodules.springdata.business.Representative;
import it.com.foreach.across.modules.entity.query.jpa.ITEntityQueryJpaUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Arne Vandamme
 */
public class ITEntityQueryQueryDslUtils extends ITEntityQueryJpaUtils
{
	@Override
	protected boolean assertCompanyResults( EntityQuery query, Company... companies ) {
		List<Company> found = (List<Company>) companyRepository.findAll(
				EntityQueryQueryDslUtils.toPredicate( query, Company.class, "company" )
		);
		assertEquals( companies.length, found.size() );
		assertTrue( found.containsAll( Arrays.asList( companies ) ) );
		return true;
	}

	@Override
	protected void assertRepresentativeResults( EntityQuery query, Representative... representatives ) {
		List<Representative> found = (List<Representative>) representativeRepository.findAll(
				EntityQueryQueryDslUtils.toPredicate( query, Representative.class, "representative" )
		);
		assertEquals( representatives.length, found.size() );
		assertTrue( found.containsAll( Arrays.asList( representatives ) ) );
	}
}
