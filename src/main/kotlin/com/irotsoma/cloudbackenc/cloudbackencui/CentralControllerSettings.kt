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
}