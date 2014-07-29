package com.foreach.across.modules.it.properties.extendingmodule.registry;

import com.foreach.across.modules.properties.config.EntityPropertiesDescriptor;
import com.foreach.across.modules.properties.registries.EntityPropertiesRegistry;
import org.springframework.stereotype.Service;

/**
 * @author Arne Vandamme
 */
public class ClientPropertyRegistry extends EntityPropertiesRegistry
{
	public ClientPropertyRegistry( EntityPropertiesDescriptor descriptor ) {
		super( descriptor );
	}
}
