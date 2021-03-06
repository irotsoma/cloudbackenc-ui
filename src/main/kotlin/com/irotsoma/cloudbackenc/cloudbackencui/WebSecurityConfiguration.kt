/*
 * Copyright (C) 2016-2019  Irotsoma, LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
/*
 * Created by irotsoma on 8/15/2016.
 */
package com.irotsoma.cloudbackenc.cloudbackencui

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

/**
 * Security configuration for REST controllers
 *
 * @author Justin Zak
 */

@Configuration
@EnableWebSecurity
class WebSecurityConfiguration : WebSecurityConfigurerAdapter() {
    /**
     * Security configuration settings for REST controllers
     */
    override fun configure(http: HttpSecurity){
        http
            .authorizeRequests()
                .antMatchers("/h2-console/**","/cloud-service-callback").permitAll() //TODO: turn off access to H2 console and enable auth on callback
                .anyRequest().authenticated() //but anything else requires authentication
                .and()
            .httpBasic()
                .and()
            .headers()
                .frameOptions().disable() //needed to get h2 console working
                .and()
            .csrf().disable()
    }
}