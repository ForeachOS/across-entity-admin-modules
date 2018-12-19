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

package com.foreach.across.samples.entity.application.config;

import com.foreach.across.modules.bootstrapui.elements.NumericFormElementConfiguration;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.query.EntityQueryExecutor;
import com.foreach.across.modules.entity.query.collections.CollectionEntityQueryExecutor;
import com.foreach.across.modules.entity.registry.EntityFactory;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.format.Printer;
import org.springframework.format.annotation.NumberFormat;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Test configuration for an entity with embedded collections.
 *
 * @author Arne Vandamme
 * @since 3.2.0
 */
@Configuration
public class LibraryConfiguration implements EntityConfigurer
{
	private final Map<Serializable, Library> libraries = new HashMap<>();

	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		Library example = createExampleLibrary();
		libraries.put( example.id, example );

		entities.create()
		        .as( Library.class )
		        .entityType( Library.class, true )
		        .displayName( "Library" )
		        .entityModel(
				        model -> model.entityFactory( LIBRARY_FACTORY )
				                      .entityInformation( LIBRARY_INFORMATION )
				                      .findOneMethod( libraries::get )
				                      .labelPrinter( ( library, locale ) -> library.getName() )
				                      .saveMethod( library -> {
					                      if ( library.id == null ) {
						                      library.id = UUID.randomUUID().toString();
					                      }
					                      libraries.put( library.id, library );
					                      return library;
				                      } )
				                      .deleteByIdMethod( libraries::remove )
		        )
		        .properties(
				        props -> props.property( "books[]" )
				                      .attribute( Printer.class, ( book, locale ) -> ( (Book) book ).getTitle() )
				                      .and()
				                      .property( "discounts[]" )
				                      .attribute( NumericFormElementConfiguration.class, NumericFormElementConfiguration.percent( 2, true ) )
		        )
		        .attribute(
				        ( configuration, attributes ) ->
						        attributes.setAttribute(
								        EntityQueryExecutor.class,
								        new CollectionEntityQueryExecutor<>( libraries::values, configuration.getPropertyRegistry() )
						        )
		        )
		        .detailView()
		        .listView()
		        .createFormView()
		        .updateFormView()
		        .deleteFormView()
		        .show();

	}

	private Library createExampleLibrary() {
		Library library = new Library();
		library.setId( "example" );
		library.setName( "Some library" );
		library.setTypeOfBooks( EnumSet.of( TypeOfBooks.FICTION, TypeOfBooks.NON_FICTION ) );
		library.setDiscounts( Arrays.asList( new BigDecimal( "15.00" ), new BigDecimal( "12.5" ) ) );

		Book book = new Book();
		book.setTitle( "Lord of the Rings" );
		book.setGenres( Arrays.asList( EnumSet.of( Genre.FANTASY ), EnumSet.of( Genre.CRIME, Genre.ROMANCE ) ) );

		Author author = new Author();
		author.setName( "JRR Tolkien" );
		book.setAuthors( Collections.singletonList( author ) );

		Publication publication = new Publication();
		publication.setNumber( 1 );
		publication.setListPrice( new BigDecimal( "22" ) );
		publication.setPublicationDate( ZonedDateTime.now() );
		publication.setWritingDuration( Duration.ofDays( 100 ) );
		book.setPublications( Collections.singletonList( publication ) );

		library.setBooks( Collections.singletonList( book ) );

		return library;
	}

	private static final EntityFactory<Library> LIBRARY_FACTORY = new EntityFactory<Library>()
	{
		@Override
		public Library createNew( Object... args ) {
			return new Library();
		}

		@Override
		public Library createDto( Library entity ) {
			return entity.copy();
		}
	};

	private static final EntityInformation<Library, String> LIBRARY_INFORMATION = new EntityInformation<Library, String>()
	{
		@Override
		public boolean isNew( Library library ) {
			return library.id != null;
		}

		@Override
		public String getId( Library library ) {
			return library.id;
		}

		@Override
		public Class<String> getIdType() {
			return String.class;
		}

		@Override
		public Class<Library> getJavaType() {
			return Library.class;
		}
	};

	@Data
	static class Library
	{
		@Setter(AccessLevel.PRIVATE)
		@Getter(AccessLevel.PRIVATE)
		private String id;

		@NotBlank
		@Length(max = 100)
		private String name;

		private List<BigDecimal> discounts = Collections.emptyList();

		@NotNull
		private Set<TypeOfBooks> typeOfBooks = Collections.emptySet();

		@NotEmpty
		public List<Book> books = Collections.emptyList();

		public Library copy() {
			Library l = new Library();
			l.id = id;
			l.name = name;
			l.setDiscounts( new ArrayList<>( discounts ) );
			l.setBooks( books.stream().map( Book::copy ).collect( Collectors.toList() ) );
			l.setTypeOfBooks( new HashSet<>( typeOfBooks ) );
			return l;
		}
	}

	@Data
	public static class Book
	{
		@NotBlank
		@Length(max = 100)
		private String title;

		private List<Set<Genre>> genres = Collections.emptyList();

		private List<Author> authors = Collections.emptyList();

		@NotEmpty
		private List<Publication> publications = Collections.emptyList();

		public Book copy() {
			Book b = new Book();
			b.title = title;

			b.genres = new ArrayList<>( genres.size() );
			genres.stream()
			      .map( HashSet::new )
			      .forEach( b.genres::add );
			b.authors = authors.stream().map( Author::copy ).collect( Collectors.toList() );
			b.publications = publications.stream().map( Publication::copy ).collect( Collectors.toList() );
			return b;
		}
	}

	@Data
	public static class Publication
	{
		@NotNull
		private Integer number;

		@NotNull
		private ZonedDateTime publicationDate;

		private Duration writingDuration;

		@NumberFormat(style = NumberFormat.Style.CURRENCY)
		private BigDecimal listPrice;

		public Publication copy() {
			Publication p = new Publication();
			p.number = number;
			p.publicationDate = publicationDate;
			p.listPrice = listPrice;
			p.writingDuration = writingDuration;
			return p;
		}
	}

	@Data
	public static class Author
	{
		@NotBlank
		@Length(max = 100)
		private String name;

		public Author copy() {
			Author author = new Author();
			author.name = name;
			return author;
		}
	}

	enum Genre implements Serializable
	{
		FANTASY,
		CRIME,
		DRAMA,
		ROMANCE
	}

	enum TypeOfBooks implements Serializable
	{
		FICTION,
		NON_FICTION
	}
}
