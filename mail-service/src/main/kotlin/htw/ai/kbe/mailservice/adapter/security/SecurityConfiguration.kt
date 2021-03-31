package htw.ai.kbe.mailservice.adapter.security

import htw.ai.kbe.mailservice.adapter.security.JwtAuthenticationEntryPoint
import htw.ai.kbe.mailservice.adapter.security.JwtAuthorizationFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
@EnableWebSecurity
class SecurityConfiguration(
    @Value("\${auth.jwt.key.public}") private val jwtPublicKey: String
) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http.cors()
            .and()
            .csrf().disable()
            .authorizeRequests()
            .anyRequest().authenticated()
            .and()
            .addFilter(JwtAuthorizationFilter(authenticationManager(), jwtPublicKey))
            .exceptionHandling()
            .authenticationEntryPoint(JwtAuthenticationEntryPoint())
    }
}
