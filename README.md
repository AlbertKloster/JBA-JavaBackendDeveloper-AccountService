# Account Service
## JetBrains Academy Java Backend Developer Project

### 1 Develop and implement the API structure.
### 2 Add the user authentication functionality to the service.
### 3 Add  requirements of security standards for the authentication procedure. 
### 4 Implement the business logic of our service.
### 5 Finalize the role model using the Spring Security functionality and add the administrative functions.
### 6 Log security events, detect attacks, and monitor user activities.
### 7 Implement the HTTPS protocol to fortify the service. 

Using WebSecurityConfigurerAdapter implementation with
    .exceptionHandling() + CustomAuthenticationEntryPoint
    .httpBasic().authenticationEntryPoint() + CustomAccessDeniedHandler
    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) for no session
Using @Transactional in UserDetailsServiceImpl to avoid org.hibernate.LazyInitializationException

Using @ElementCollection(fetch = FetchType.EAGER) for OneToMany collection