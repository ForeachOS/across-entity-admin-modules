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

package com.foreach.across.modules.entity.config.builders;

import com.foreach.across.modules.entity.registry.EntityFactory;
import com.foreach.across.modules.entity.registry.EntityModel;
import com.foreach.across.modules.entity.registry.EntityModelImpl;
import com.foreach.across.modules.entity.registry.MutableEntityConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.format.Printer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Allows customizing an {@link com.foreach.across.modules.entity.registry.EntityModel}.
 * If the model is an implementation of {@link EntityModelImpl}, properties can be customized, else only the
 * postprocessors will be executed.
 *
 * @author Arne Vandamme
 * @see EntityModel
 * @since 2.0.0
 */
public class EntityModelBuilder<T>
{
	private static final Logger LOG = LoggerFactory.getLogger( EntityModelBuilder.class );

	private final EntityConfigurationBuilder<T> parentBuilder;

	private final Collection<Consumer<EntityModel>> postProcessors = new ArrayList<>();

	private EntityFactory<T> entityFactory;
	private EntityInformation<T, Serializable> entityInformation;
	private Printer<T> labelPrinter;

	private Function<Serializable, T> findOneMethod;
	private UnaryOperator<T> saveMethod;
	private Consumer<T> deleteMethod;
	private Consumer<Serializable> deleteMethodById;

	EntityModelBuilder( EntityConfigurationBuilder<T> parentBuilder ) {
		this.parentBuilder = parentBuilder;
	}

	/**
	 * @return parent builder that owns the EntityConfiguration for this model
	 */
	public EntityConfigurationBuilder<T> and() {
		return parentBuilder;
	}

	/**
	 * Set the {@link EntityFactory} for this model.
	 *
	 * @param entityFactory to set
	 * @return current builder
	 */
	public EntityModelBuilder<T> entityFactory( EntityFactory<T> entityFactory ) {
		this.entityFactory = entityFactory;
		return this;
	}

	/**
	 * Set the {@link EntityInformation} for this model.
	 *
	 * @param entityInformation to set
	 * @return current builder
	 */
	public EntityModelBuilder<T> entityInformation( EntityInformation<T, Serializable> entityInformation ) {
		this.entityInformation = entityInformation;
		return this;
	}

	/**
	 * Set the label printer for this model.
	 *
	 * @param labelPrinter to set
	 * @return current builder
	 */
	public EntityModelBuilder<T> labelPrinter( Printer<T> labelPrinter ) {
		this.labelPrinter = labelPrinter;
		return this;
	}

	/**
	 * Set the method callback for finding a single entity by id.
	 *
	 * @param findOneMethod callback method
	 * @return current builder
	 */
	public EntityModelBuilder<T> findOneMethod( Function<Serializable, T> findOneMethod ) {
		this.findOneMethod = findOneMethod;
		return this;
	}

	/**
	 * Set the method callback for saving an entity.
	 *
	 * @param saveMethod callback method
	 * @return current builder
	 */
	public EntityModelBuilder<T> saveMethod( UnaryOperator<T> saveMethod ) {
		this.saveMethod = saveMethod;
		return this;
	}

	/**
	 * Set the method callback for deleting an entity.
	 *
	 * @param deleteMethod callback method
	 * @return current builder
	 */
	public EntityModelBuilder<T> deleteMethod( Consumer<T> deleteMethod ) {
		this.deleteMethod = deleteMethod;
		return this;
	}

	/**
	 * Set the method callback based on the unique id of the entity.
	 * Will automatically convert into an entity based delete method that uses the {@link EntityModel} itself
	 * to lookup the id of the entity.
	 * <p/>
	 * Will only be used if the builder was not called with {@link #deleteMethod(Consumer)}.
	 *
	 * @param deleteMethodById callback method
	 * @return current builder
	 */
	public EntityModelBuilder<T> deleteMethodById( Consumer<Serializable> deleteMethodById ) {
		this.deleteMethodById = deleteMethodById;
		return this;
	}

	/**
	 * Add a post processor that will be applied after all properties, regardless of the type
	 * of {@link EntityModel} implementation.
	 *
	 * @param postProcessor Post processor instance to add.
	 * @return current builder
	 */
	public EntityModelBuilder<T> postProcessor( Consumer<EntityModel> postProcessor ) {
		postProcessors.add( postProcessor );
		return this;
	}

	void apply( MutableEntityConfiguration<T> configuration ) {
		EntityModel<T, Serializable> currentModel = configuration.getEntityModel();

		if ( currentModel == null ) {
			// create a new model if non exists yet
			currentModel = new EntityModelImpl<>();
			configuration.setEntityModel( currentModel );
		}

		if ( currentModel instanceof EntityModelImpl ) {
			EntityModelImpl<T, Serializable> model = (EntityModelImpl<T, Serializable>) currentModel;

			if ( entityFactory != null ) {
				model.setEntityFactory( entityFactory );
			}
			if ( entityInformation != null ) {
				model.setEntityInformation( entityInformation );
			}
			if ( labelPrinter != null ) {
				model.setLabelPrinter( labelPrinter );
			}
			if ( findOneMethod != null ) {
				model.setFindOneMethod( findOneMethod );
			}
			if ( saveMethod != null ) {
				model.setSaveMethod( saveMethod );
			}
			if ( deleteMethod != null ) {
				model.setDeleteMethod( deleteMethod );
			}
			else if ( deleteMethodById != null ) {
				// wrap convert by id method in a regular delete method
				model.setDeleteMethod( entity -> deleteMethodById.accept( model.getId( entity ) ) );
			}
		}
		else {
			LOG.warn(
					"Skipping EntityModelBuilder properties for entity configuration {}, current implementation is not of type EntityModelImpl",
					configuration.getName() );
		}

		EntityModel actual = currentModel;
		postProcessors.forEach( postProcessor -> postProcessor.accept( actual ) );
	}
}
