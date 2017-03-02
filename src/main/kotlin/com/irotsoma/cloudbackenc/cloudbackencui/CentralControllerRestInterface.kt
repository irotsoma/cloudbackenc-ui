/**
 * Created by irotsoma on 3/1/17.
 */
package com.irotsoma.cloudbackenc.cloudbackencui

import com.irotsoma.cloudbackenc.common.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import tornadofx.FX
import java.net.InetAddress

/**
 * An autowired class that holds information required for making rest calls to the central controller.
 * This prevents requiring tornadoFX components to become spring components but still allows for autowiring settings.
 *
 * @author Justin Zak
 */
@Lazy
@Component
class CentralControllerRestInterface {
    companion object { val LOG by logger() }
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