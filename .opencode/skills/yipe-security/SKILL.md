---
name: yipe-security
description: Spring Security form login configuration for YIPE Personal Finances. Single-user setup, CSRF protection, password encoding.
---

## YIPE Security Conventions

### SecurityConfig Structure
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/error").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        return http.build();
    }
}
```

### Key Points
- CSRF protection enabled by default — forms need `th:action` or hidden `_csrf` input
- Static resources (`/css/**`, `/js/**`) are public
- Login page at `/login` — custom Thymeleaf template
- Default success redirect to `/dashboard`

### User Details (Dev)
```java
@Bean
public UserDetailsService users() {
    UserDetails admin = User.builder()
        .username("admin")
        .password(passwordEncoder().encode("admin"))
        .roles("ADMIN")
        .build();
    return new InMemoryUserDetailsManager(admin);
}

@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### Production Checklist
- Replace hardcoded credentials with environment variables
- Replace `InMemoryUserDetailsManager` with JDBC/JPA persistence
- Add HTTPS via reverse proxy or Spring Boot SSL config
- Consider `Spring Session JDBC` for persistent sessions across restarts
