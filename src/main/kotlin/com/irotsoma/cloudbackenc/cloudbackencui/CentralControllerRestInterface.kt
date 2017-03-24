/*
 * Copyright (C) 2017  Irotsoma, LLC
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

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import tornadofx.*
import java.net.InetAddress

/**
 * An autowired class that holds information required for making rest calls to the central controller.
 * This prevents requiring tornadoFX components to become spring components but still allows for autowiring settings.
 *
 * Lazy to prevent it from being initialized during integration tests for callback controller.
 *
 * @author Justin Zak
 * @property localPort Port that the current internal Spring REST server is using.
 * @property useSSL Property that is used to determine if the internal Spring REST server is using SSL by checking for a key store.  Assumes no SSL if null.
 * @property centralControllerSettings Autowired configuration bean that contains information to access the central controller.
 * @property localProtocol Protocol portion of URI for the local REST server (http or https)
 * @property localHostname Hostname of the internal Spring REST server.
 * @property centralControllerProtocol Protocol portion of URI for accessing the Central Controller (http or https)
 */
@Lazy
@Component
class CentralControllerRestInterface {
    @Value("\${server.port}")
    var localPort: Int = 0
    @Value("\${server.ssl.key-store}")
    private var useSSL: String? = null
    @Autowired
    var centralControllerSettings: CentralControllerSettings? = null
    final val localProtocol: String
    final val localHostname: String = InetAddress.getLocalHost().hostName
    final val centralControllerProtocol: String
    init {
        //get the current instance of the spring application and autowire beans in this class
        val fxApplication = FX.application as CloudBackEncUIApp
        fxApplication.applicationContext?.autowireCapableBeanFactory?.autowireBean(this)
        //Calculate protocols and callback url after autowire is complete
        localProtocol = if (useSSL!=null && useSSL!="") "https" else "http"
        centralControllerProtocol = if (centralControllerSettings!!.useSSL) "https" else "http"
    }
}