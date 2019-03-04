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

/**
 * Created by irotsoma on 3/1/17.
 */
package com.irotsoma.cloudbackenc.cloudbackencui

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Settings for connecting to the central controller.
 *
 * @author Justin Zak
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("centralcontroller")
class CentralControllerSettings {
    /**
     * Central Controller REST port number
     */
    var port: Int = 0
    /**
     * Central Controller REST host name
     */
    var host: String = ""
    /**
     * Is Central Controller using SSL?
     */
    var useSSL: Boolean = false
    /**
     * Disables SSL certificate validation.  Should only be true for testing purposes with self signed certificates.
     */
    var disableCertificateValidation: Boolean = false
    /**
     * Path to the cloud services rest service
     */
    var cloudServicesPath: String = ""
    /**
     * Path to the users rest service
     */
    var usersPath: String = ""
    /**
     * Path to the auth rest service
     */
    var authPath: String = ""
    /**
     * Path to the files rest service
     */
    var filesPath: String = ""

}