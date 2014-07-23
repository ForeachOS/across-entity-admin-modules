package com.foreach.across.modules.user.services;

import com.foreach.across.modules.user.UserModuleSettings;
import com.foreach.across.modules.user.business.User;
import com.foreach.across.modules.user.dto.UserDto;
import com.foreach.across.modules.user.repositories.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import javax.annotation.PostConstruct;
import java.util.Collection;

@Service
public class UserServiceImpl implements UserService
{
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserValidator userValidator;

	private final PasswordEncoder passwordEncoder;
	private final boolean useEmailAsUsername;
	private final boolean requireEmailUnique;

	public UserServiceImpl( PasswordEncoder passwordEncoder, boolean useEmailAsUsername, boolean requireEmailUnique ) {
		Assert.notNull( passwordEncoder, "A UserService must be configured with a valid PasswordEncoder" );
		this.passwordEncoder = passwordEncoder;
		this.useEmailAsUsername = useEmailAsUsername;
		this.requireEmailUnique = requireEmailUnique;
	}

	@PostConstruct
	public void validateProperties() {
		if( useEmailAsUsername && !requireEmailUnique ) {
			throw new RuntimeException( UserModuleSettings.REQUIRE_EMAIL_UNIQUE + " must be TRUE if " + UserModuleSettings.USE_EMAIL_AS_USERNAME + " is TRUE" );
		}
	}

	public boolean isUseEmailAsUsername() {
		return useEmailAsUsername;
	}

	public boolean isRequireEmailUnique() {
		return requireEmailUnique;
	}

	@Override
	public Collection<User> getUsers() {
		return userRepository.getUsers();
	}

	@Override
	public User getUserById( long id ) {
		return userRepository.getUserById( id );
	}

	@Override
	public User getUserByEmail( String email ) {
		return userRepository.getUserByEmail( email );
	}

	@Override
	public User getUserByUsername( String username ) {
		return userRepository.getUserByUsername( username );
	}

	@Override
	public UserDto createUserDto( User user ) {
		return new UserDto( user );
	}

	@Override
	public void save( UserDto userDto ) {
		User user;

		if ( userDto.isNewUser() ) {
			user = new User();

			if ( StringUtils.isBlank( userDto.getPassword() ) ) {
				throw new UserModuleException( "A new user always requires a non-blank password to be set." );
			}
		}
		else {
			long existingUserId = userDto.getId();

			if ( existingUserId == 0 ) {
				throw new UserModuleException(
						"Impossible to update a user with id 0, 0 is a special id that should never be used for persisted entities." );
			}

			user = getUserById( existingUserId );

			if ( user == null ) {
				throw new UserModuleException(
						"Attempt to update user with id " + existingUserId + " but that user does not exist" );
			}
		}

		Errors errors = new BeanPropertyBindingResult( userDto, "user" );
		userValidator.validate( userDto, errors );
		if( errors.hasErrors() ) {
			throw new UserValidationException( "Failed to validate User, [" + errors.getErrorCount() + "] validation errors", errors.getAllErrors() );
		}

		BeanUtils.copyProperties( userDto, user, "password" );
		if( useEmailAsUsername ) {
			user.setUsername( userDto.getEmail() );
		}

		// Only modify password if password on the dto is not blank
		if ( !StringUtils.isBlank( userDto.getPassword() ) ) {
			user.setPassword( passwordEncoder.encode( userDto.getPassword() ) );
		}

		if ( StringUtils.isBlank( user.getDisplayName() ) ) {
			user.setDisplayName( String.format( "%s %s", user.getFirstName(), user.getLastName() ).trim() );
		}

		if ( userDto.isNewUser() ) {
			userRepository.create( user );
		}
		else {
			userRepository.update( user );
		}

		userDto.setFromUser( user );
	}

	@Override
	public void delete( long id ) {
		User user = userRepository.getUserById( id );
		userRepository.delete( user );
	}
}
