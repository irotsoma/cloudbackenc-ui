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
    var port: Int = 0
    var host: String = ""
    var useSSL: Boolean = false
    var disableCertificateValidation: Boolean = false
}