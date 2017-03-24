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
/*
 * Created by irotsoma on 8/12/16.
 */
package com.irotsoma.cloudbackenc.cloudbackencui

import com.irotsoma.cloudbackenc.cloudbackencui.userinterfaces.CloudServiceWebView
import com.irotsoma.cloudbackenc.common.cloudservicesserviceinterface.CloudServiceCallbackURL
import javafx.application.Platform
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.awt.Desktop
import java.net.URI

/**
 * Callback Controller for Cloud Services
 *
 * Rest controller that receives a callback from the central controller if a cloud service provider requests user input.
 *
 * @author Justin Zak
 */
@RestController
class CloudServiceCallbackController {
    /** kotlin-logging implementation*/
    companion object: KLogging()

    //TODO: secure this to allow only calls from central controller

    /**
     * Receives the POST calls for OAuth cloud service authentication callbacks.
     */
    @RequestMapping("cloud-service-callback", method = arrayOf(RequestMethod.POST))
    fun authenticate(@RequestBody url: CloudServiceCallbackURL) : ResponseEntity<Void> {

        //for controller tests
        val testUUID = "f8bed9c2-c68b-4ab4-a66a-f16a6b46b768"
        if (url.uuid == testUUID){
            logger.info{"Sending test response."}
            return(ResponseEntity(HttpStatus.ACCEPTED))
        }

        Platform.runLater({
            //try to open the browser to the authentication URL using awt Desktop.  If not possible, use custom webview instead
            var browserSuccess = false
            try {
                if (Desktop.isDesktopSupported()) {
                    val desktop = Desktop.getDesktop()
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        desktop.browse(URI(url.authorizationURL))
                        browserSuccess = true
                    }
                }
            } catch (e: Exception) {
                logger.debug{"Error getting desktop to display authorization URL to user.  Will try custom view"}
            }
            if (!browserSuccess) {
                CloudServiceWebView(url.authorizationURL).openModal()
            }
        })
        return(ResponseEntity(HttpStatus.ACCEPTED))
    }
}