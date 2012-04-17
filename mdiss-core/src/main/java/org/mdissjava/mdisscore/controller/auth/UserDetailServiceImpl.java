package org.mdissjava.mdisscore.controller.auth;

import java.util.ArrayList;
import java.util.Collection;

import org.mdissjava.mdisscore.model.dao.UserDao;
import org.mdissjava.mdisscore.model.dao.impl.UserDaoImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom implementation for getting users from database for spring security
 * This way using our DAO with Hibernate we could create a custom user and 
 * Spring will compare both 
 * 
 * @author MdissJava
 *
 */
@Service("userDetailsService") 
public class UserDetailServiceImpl implements UserDetailsService {

	private UserDao dao = new UserDaoImpl();
	
	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
	
		//When we need to store passwords, the way is to encode in SHA256 this way:
		//PasswordEncoder sha256Encoder = new ShaPasswordEncoder(256);
		//String password = sha256Encoder.encodePassword(plainPassword, salt);
		
		//Get the user from the DAO
		org.mdissjava.mdisscore.model.pojo.User user = dao.getUserByName(username);
		
	    String password = user.getPass();
	    String role = user.getRole();
	    boolean enabled = user.isActive();
	    boolean accountNonExpired = user.isActive();
	    boolean credentialsNonExpired = user.isActive();
	    boolean accountNonLocked = user.isActive();
	    Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
	    
	    //Check the user's role in order to assign the authorities.
	    if (role.equals("ADMIN"))
	    {
	    	authorities.add(new GrantedAuthorityImpl("ROLE_ADMIN"));
	    }
	    else if (role.equals("USER"))
	    {
	    	authorities.add(new GrantedAuthorityImpl("ROLE_USER"));
	    }
	    
	    User spUser = new User(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
		return spUser;
		
		/*
		PasswordEncoder sha256Encoder = new ShaPasswordEncoder(256);
		String salt = null;
		String plainPassword = "slok";
		String password = sha256Encoder.encodePassword(plainPassword, salt);
	    String username = "slok";
	    boolean enabled = true;
	    boolean accountNonExpired = true;
	    boolean credentialsNonExpired = true;
	    boolean accountNonLocked = true;

	   // if (arg0 != username)
	    //	throw new UsernameNotFoundException("user not found");
	    
	    
	    Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
	    authorities.add(new GrantedAuthorityImpl("ROLE_USER"));
		User user = new User(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
		return user;
		*/
	}

}