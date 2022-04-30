package account.security;

import account.constant.Constant;
import account.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class UserDetailsImpl implements UserDetails {

    private final String username;
    private final String password;
    private final boolean accountNonLocked;

    private final Set<GrantedAuthority> grantedAuthorities;

    public UserDetailsImpl(User user) {
        this.username = user.getEmail();
        this.password = user.getPassword();
        this.accountNonLocked = user.getFailedAttempts() < Constant.MAX_FAILED_ATTEMPTS;
        this.grantedAuthorities = user.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
