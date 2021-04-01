package htw.ai.kbe.authservice.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.time.Duration
import javax.ws.rs.HttpMethod.POST


const val AUTH_PATH = "/auth"

@Configuration
class SecurityConfig(
    private val userDetailsService: JwtUserDetailsService,
    private val jwtService: JwtService
) : WebSecurityConfigurerAdapter() {

    @Value("\${server.servlet.context-path}")
    private lateinit var contextPath: String

    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    @Bean
    fun jwtAuthenticationSuccessHandler() = JwtAuthenticationSuccessHandler(jwtService)

    @Bean
    fun jwtAuthenticationFailureHandler() = JwtAuthenticationFailureHandler()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()
        config.addAllowedOriginPattern("*")
        config.addAllowedMethod("*")
        config.addAllowedHeader("*")
        config.setMaxAge(Duration.ofHours(24))
        config.exposedHeaders = listOf(HttpHeaders.AUTHORIZATION, HttpHeaders.LOCATION)
        config.applyPermitDefaultValues()
        source.registerCorsConfiguration("/**", config)
        return source
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder())
    }

    override fun configure(http: HttpSecurity) {
        http.cors()
            .and()
            .csrf().disable()
            .authorizeRequests()
            .antMatchers(POST, contextPath + AUTH_PATH).permitAll()
            .anyRequest().authenticated().and()
            .addFilter(jwtAuthenticationFilter())
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }

    fun jwtAuthenticationFilter(): JwtAuthenticationFilter {
        val jwtAuthenticationFilter = JwtAuthenticationFilter(authenticationManager())
        jwtAuthenticationFilter.setAuthenticationSuccessHandler(jwtAuthenticationSuccessHandler())
        jwtAuthenticationFilter.setAuthenticationFailureHandler(jwtAuthenticationFailureHandler())
        jwtAuthenticationFilter.setFilterProcessesUrl(AUTH_PATH)
        return jwtAuthenticationFilter
    }
}
