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

package com.foreach.across.modules.entity.autosuggest;

import lombok.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for attaching a {@link SimpleAutoSuggestDataSet} to a particular endpoint.
 * This usually makes the dataset callable over a remote API.
 *
 * @author Arne Vandamme
 * @see AutoSuggestDataController
 * @since 3.0.0
 */
public final class AutoSuggestDataEndpoint
{
	private final UriComponentsBuilder urlBuilder;
	private final Map<String, MappedDataSet> dataSetMap = new ConcurrentHashMap<>();

	public AutoSuggestDataEndpoint( String baseUrl ) {
		//Uses .fromPath instead of .fromUriString because of a host() part check
		String[] baseUrlParts = baseUrl.split( ":" );
		urlBuilder = UriComponentsBuilder.fromPath( baseUrlParts[1] )
		                                 .scheme( baseUrlParts[0] )
		                                 .queryParam( "query", "{{query}}" )
		                                 .queryParam( "controlName", "{{controlName}}" );

	}

	/**
	 * Attach a dataset to this endpoint. This will assign a random unique name to this dataset,
	 * and make it available on the controller attached to this endpoint.
	 *
	 * @param dataSet to attach
	 * @return attached dataset context
	 * @deprecated since 3.4.0 - manually specify an id
	 */
	@Deprecated
	public MappedDataSet registerDataSet( AutoSuggestDataSet dataSet ) {
		return registerDataSet( UUID.randomUUID().toString(), dataSet );
	}

	/**
	 * Attach a dataset with a specific id to this endpoint. Will replace any previously registered
	 * dataset for that endpoint.
	 *
	 * @param id      of the dataset
	 * @param dataSet to attach
	 * @return attached dataset context
	 */
	public MappedDataSet registerDataSet( @NonNull String id, @NonNull AutoSuggestDataSet dataSet ) {
		val mapped = new MappedDataSet( id, dataSet );
		dataSetMap.put( mapped.getId(), mapped );
		return mapped;
	}

	/**
	 * Remove the dataset with that id.
	 *
	 * @param id of the dataset
	 * @return the underlying dataset that has been removed
	 */
	public AutoSuggestDataSet removeDataSet( String id ) {
		return Optional.ofNullable( dataSetMap.remove( id ) ).map( MappedDataSet::getDataSet ).orElse( null );
	}

	/**
	 * Get the dataset with that id.
	 *
	 * @param dataSetId id of the dataset
	 * @return dataset of {@code null} if not found
	 */
	public MappedDataSet getDataSet( String dataSetId ) {
		return dataSetMap.get( dataSetId );
	}

	/**
	 * Create the suggestions url components for a named dataset.
	 * Usually the resulting url should not be encoded as it contains {@code {{XX}}} parameters.
	 *
	 * @param dataSetId id of the dataset
	 * @return url
	 */
	public UriComponentsBuilder suggestionsUriComponents( String dataSetId ) {
		return urlBuilder.cloneBuilder().path( "/query" ).queryParam( "dataset", dataSetId );
	}

	/**
	 * Create the prefetch url components for a named dataset.
	 * Usually the resulting url should not be encoded as it contains {@code {{XX}}} parameters.
	 *
	 * @param dataSetId id of the dataset
	 * @return url
	 */
	public UriComponentsBuilder prefetchUriComponents( String dataSetId ) {
		return urlBuilder.cloneBuilder().path( "/prefetch" ).queryParam( "dataset", dataSetId );
	}

	/**
	 * Wraps a regular {@link SimpleAutoSuggestDataSet} with endpoint identification data.
	 */
	@Getter
	@EqualsAndHashCode
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public class MappedDataSet
	{
		private final String id;
		private final AutoSuggestDataSet dataSet;

		public String suggestionsUrl() {
			return suggestionsUriComponents().build().toUriString();
		}

		public UriComponentsBuilder suggestionsUriComponents() {
			return AutoSuggestDataEndpoint.this.suggestionsUriComponents( id );
		}

		public String prefetchUrl() {
			return prefetchUriComponents().build().toUriString();
		}

		public UriComponentsBuilder prefetchUriComponents() {
			return AutoSuggestDataEndpoint.this.prefetchUriComponents( id );
		}

		public boolean isPrefetchSupported() {
			return dataSet.isPrefetchSupported();
		}
	}
}
