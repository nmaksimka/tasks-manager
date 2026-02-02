package com.tasks_manager.taskmanager.security;

import com.tasks_manager.taskmanager.entities.User;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record AppUserPrincipal(Long id, String username, String password, List<SimpleGrantedAuthority> authorities)
        implements UserDetails {

    public static AppUserPrincipal from(User user) {
        var authorities = user.getRoles().stream().sorted().map(SimpleGrantedAuthority::new).toList();
        return new AppUserPrincipal(user.getId(), user.getUsername(), user.getPassword(), authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
