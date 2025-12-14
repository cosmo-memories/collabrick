package nz.ac.canterbury.seng302.homehelper.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration class for Spring Security settings. This class sets up authentication and authorization rules, defines
 * password encoding, and configures login and logout behaviors.
 * Implemented by following the Spring Security Handout on LEARN
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    /**
     * URLs that should bypass security.
     */
    public static final String[] PASSTHROUGH_URLS = {
            "/h2/**", "/webjars/**", "/css/**", "/javascript/**", "/user-photos/**", "/images/**", "/fonts/**", "/ws/**", "/ws"
    };

    /**
     * Publicly accessible URLs.
     */
    public static final String[] ALLOWED_URLS = {
            "/", "/home", "/register", "/login", "/demo", "/logout", "/do_login", "/verification", "/forgotten-password", "/reset-password", "/tagAutoComplete",
            "/browse/**", "/browse", "/autocomplete", "/tasks-calendar/**", "/invitation", "/decline-invitation", "/expired-invitation", "/renovation/**"
    };

    private final CustomAuthenticationProvider authProvider;

    /**
     * Constructs a new SecurityConfiguration.
     *
     * @param authProvider the custom authentication provider that handles user authentication.
     */
    @Autowired
    public SecurityConfiguration(CustomAuthenticationProvider authProvider) {
        this.authProvider = authProvider;
    }

    /**
     * Configures the authentication manager using the custom authentication provider.
     *
     * @param http the HttpSecurity configuration object.
     * @return an AuthenticationManager configured with the custom authentication provider.
     * @throws Exception if there is an error building the AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(authProvider);
        return authenticationManagerBuilder.build();
    }

    /**
     * Defines the security rules for HTTP requests, including login and logout behaviors.
     *
     * @param http The HttpSecurity configuration object.
     * @return The configured SecurityFilterChain.
     * @throws Exception If an error occurs during security configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers(PASSTHROUGH_URLS).permitAll()
                        .requestMatchers(ALLOWED_URLS).permitAll()
                        .anyRequest().authenticated())
                .headers(headers -> headers.frameOptions(Customizer.withDefaults()).disable())
                .csrf(csrf -> csrf.ignoringRequestMatchers(PASSTHROUGH_URLS))

                // Define logging in, a POST "/login" endpoint now exists under the hood after login redirect to user page
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        // The /security_login and failureUrl changes were GPT'd
                        // This basically prevents Spring from intercepting login requests
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/userPage", true)
                        .failureUrl("/login?error=true"))

                // Define logging out, a POST "/logout" endpoint now exists under the hood, redirect to "/login",
                // invalidate session and remove cookies
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"));
        return http.build();
    }

    /**
     * Defines the password encoder to be used throughout the application.
     *
     * @return A {@link BCryptPasswordEncoder} instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
