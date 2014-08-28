package com.foreach.across.modules.spring.security.acl.support;

import com.foreach.across.modules.hibernate.business.IdBasedEntity;
import com.foreach.across.modules.spring.security.acl.services.AclSecurityService;
import com.foreach.across.modules.spring.security.infrastructure.business.SecurityPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

/**
 * Base class for an interceptor hooked to {@link com.foreach.across.modules.hibernate.repositories.BasicRepository}
 * persistence methods.  Useful for creating ACLs when saving or deleting instances.
 *
 * Implementations will be picked up automatically by the
 * {@link com.foreach.across.modules.spring.security.acl.aop.BasicRepositoryAclInterceptor} if it is active.
 *
 * @author Arne Vandamme
 */
public abstract class IdBasedEntityAclInterceptor<T extends IdBasedEntity>
{
	private final Class<T> entityClass;

	private AclSecurityService aclSecurityService;

	public IdBasedEntityAclInterceptor( Class<T> entityClass ) {
		Assert.notNull( entityClass );

		this.entityClass = entityClass;
	}

	public void setAclSecurityService( AclSecurityService aclSecurityService ) {
		this.aclSecurityService = aclSecurityService;
	}

	protected AclSecurityService aclSecurityService() {
		return aclSecurityService;
	}

	/**
	 * @return The current SecurityPrincipal or null in case there is no instance of SecurityPrincipal attached.
	 */
	protected SecurityPrincipal currentSecurityPrincipal() {
		if ( isAuthenticated() ) {
			Object principal = currentAuthentication().getPrincipal();

			if ( principal instanceof SecurityPrincipal ) {
				return (SecurityPrincipal) principal;
			}
		}

		return null;
	}

	protected boolean isAuthenticated() {
		return currentAuthentication().isAuthenticated();
	}

	protected Authentication currentAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	public Class<T> getEntityClass() {
		return entityClass;
	}

	public void beforeCreate( T entity ) {
	}

	public abstract void afterCreate( T entity );

	public void beforeUpdate( T Entity ) {
	}

	public abstract void afterUpdate( T entity );

	public abstract void beforeDelete( T entity, boolean isSoftDelete );

	public void afterDelete( T entity, boolean isSoftDelete ) {
	}
}
