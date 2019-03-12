package ru.nikstep.redink.core.config

import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

/**
 * Security config of the application
 */
@Configuration
@EnableOAuth2Sso
class SecurityConfig : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http.csrf().disable()
            .antMatcher("/**")
            .authorizeRequests()
            .antMatchers("/**")
            .permitAll()
            .and().logout().logoutSuccessUrl("/").permitAll()
            .and().headers().frameOptions().sameOrigin()
    }

}
