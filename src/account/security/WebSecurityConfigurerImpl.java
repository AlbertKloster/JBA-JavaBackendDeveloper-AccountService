package account.security;

import account.constant.Entrypoint;
import account.enums.Role;
import account.service.EventRepositoryService;
import account.service.UserRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;

@EnableWebSecurity
public class WebSecurityConfigurerImpl extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;

    @Autowired
    public WebSecurityConfigurerImpl(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(getEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic().authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                .and()
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler())
                .and()
                .authorizeRequests()
                .mvcMatchers(HttpMethod.POST, Entrypoint.AUTH_CHANGEPASS).hasAnyRole(
                        Role.USER.getRole(),
                        Role.ACCOUNTANT.getRole(),
                        Role.ADMINISTRATOR.getRole()
                )
                .mvcMatchers(HttpMethod.GET, Entrypoint.EMPL_PAYMENT).hasAnyRole(
                        Role.USER.getRole(),
                        Role.ACCOUNTANT.getRole()
                )
                .mvcMatchers(HttpMethod.POST, Entrypoint.ACCT_PAYMENTS).hasAnyRole(
                        Role.ACCOUNTANT.getRole()
                )
                .mvcMatchers(HttpMethod.PUT, Entrypoint.ACCT_PAYMENTS).hasAnyRole(
                        Role.ACCOUNTANT.getRole()
                )
                .mvcMatchers(Entrypoint.ADMIN_ALL).hasRole(
                        Role.ADMINISTRATOR.getRole()
                )
                .mvcMatchers(HttpMethod.GET, Entrypoint.SECURITY_EVENTS).hasRole(
                        Role.AUDITOR.getRole()
                )
                .mvcMatchers(HttpMethod.POST, Entrypoint.AUTH_SIGNUP).permitAll()
                .mvcMatchers(HttpMethod.POST, Entrypoint.ACTUATOR_SHUTDOWN).permitAll()
                .anyRequest().fullyAuthenticated()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no session
                .and()
                .csrf().disable(); // allow sending external POST requests
    }

    @Bean
    public PasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler(){
        return new CustomAccessDeniedHandler();
    }
}
