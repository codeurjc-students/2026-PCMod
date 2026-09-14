package es.codeurjcstudents.pcmod.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import es.codeurjcstudents.pcmod.model.User;
import es.codeurjcstudents.pcmod.repository.UsersRepository;

@Service
public class RepositoryUserDetailsService implements UserDetailsService {

  @Autowired
  private UsersRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {

    User user = userRepository.findByEmail(identifier)
        .orElseGet(() -> userRepository.findById(Long.parseLong(identifier))
            .orElseThrow(() -> new UsernameNotFoundException("User not found with id or email: " + identifier)));

    List<GrantedAuthority> roles = new ArrayList<>();
    for (String role : user.getRoles()) {
      roles.add(new SimpleGrantedAuthority("ROLE_" + role));
    }

    return new org.springframework.security.core.userdetails.User(user.getId().toString(), user.getEncodedPassword(),
        roles);

  }
}
