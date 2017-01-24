/*
 * Copyright (C) 2016  Irotsoma, LLC
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
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
/*
 * Created by irotsoma on 7/28/2016.
 */
package com.irotsoma.cloudbackenc.cloudbackencui.userinterfaces

import com.irotsoma.cloudbackenc.cloudbackencui.CloudServiceModel
import com.irotsoma.cloudbackenc.cloudbackencui.applicationProperties
import com.irotsoma.cloudbackenc.cloudbackencui.trustSelfSignedSSL
import com.irotsoma.cloudbackenc.common.cloudservicesserviceinterface.CloudServiceException
import com.irotsoma.cloudbackenc.common.cloudservicesserviceinterface.CloudServiceExtension
import com.irotsoma.cloudbackenc.common.cloudservicesserviceinterface.CloudServiceExtensionList
import com.irotsoma.cloudbackenc.common.cloudservicesserviceinterface.CloudServiceUser
import com.irotsoma.cloudbackenc.common.logger
import javafx.collections.ObservableList
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TableView
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.StageStyle
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import tornadofx.*
import java.util.*

/**
 * Cloud Services UI functionality
 *
 * @author Justin Zak
*/
class CloudServicesFragment() : Fragment() {
    companion object { val LOG by logger() }
    override val root: VBox by fxml()
    val availableCloudServicesModel: CloudServiceModel = CloudServiceModel(CloudServiceExtension(UUID.randomUUID(),""))
    val cloudServicesSetupButton : Button by fxid("cloudServicesSetupButton")
    val cloudServicesRefreshButton : Button by fxid("cloudServicesRefreshButton")
    val cloudServicesRemoveButton : Button by fxid("cloudServicesRemoveButton")
    val availableCloudServicesTable : TableView<CloudServiceExtension> by fxid("availableCloudServicesTable")
    val activeCloudServicesTable : TableView<CloudServiceExtension> by fxid("activeCloudServicesTable")

    init {
        title = messages["cloudbackencui.title.application"]
        //val restTemplate = RestTemplate()
        //populate available cloud services list
        with (availableCloudServicesTable) {
            placeholder = Label(messages["cloudbackencui.message.loading.services"])
            //asynchronously access the central controller and get a list of available cloud service extensions
            asyncItems {
                getCloudServices()
            }
            //uuid column
            with(column(messages["cloudbackencui.column.cloud.service.id"], CloudServiceExtension::uuid)){
                prefWidth=230.0
            }
            //name column
            with(column(messages["cloudbackencui.column.cloud.service.name"], CloudServiceExtension::name)){
                prefWidth=270.0
            }
            //bind to list of services through model
            availableCloudServicesModel.rebindOnChange(this){ selectedService -> service = selectedService ?: CloudServiceExtension(UUID.randomUUID(),"") }
            //only enable setup button if something is selected
            selectionModel.selectedItemProperty().onChange{
                cloudServicesSetupButton.isDisable = it == null
            }
        }

        with (cloudServicesSetupButton){
            setOnAction {
                val userInfoPopup = CloudServiceUserInfoFragment(availableCloudServicesModel.service.name)
                LOG.debug("Attempting to open user ID popup.")
                userInfoPopup.openModal(StageStyle.UTILITY, Modality.WINDOW_MODAL, false, this.scene.window,true)
                LOG.debug("User entered: ${userInfoPopup.userId}")
                if (userInfoPopup.userId != null) {
                    setupCloudService(userInfoPopup.userId!!)
                }
            }
        }

        with (cloudServicesRefreshButton){
            setOnAction{
                with (availableCloudServicesTable) {
                    items.clear()
                    asyncItems {
                        getCloudServices()
                    }
                }
            }
        }

    }
    //moved the actual throw to a separate function to appease the compiler (unreachable code error).  This allows for adding a custom message to the throw.
    fun throwError(message: String, e: Exception){
        throw(CloudServiceException(message, e))
    }

    fun getCloudServices() : ObservableList<CloudServiceExtension> {
        try {
            //determine if ssl is enabled in settings
            val protocol = if (applicationProperties["centralcontroller.useSSL"] == "true") "https" else "http"
            //for testing use a hostname verifier that doesn't do any verification
            if (applicationProperties["centralcontroller.disableCertificateValidation"] == "true"){
                trustSelfSignedSSL()
            }
            return RestTemplate().getForObject("$protocol://${applicationProperties["centralcontroller.host"]}:${applicationProperties["centralcontroller.port"]}/cloud-services", CloudServiceExtensionList::class.java).observable()
        }
        catch (e: ResourceAccessException){
            throwError(messages["cloudbackencui.error.getting.cloud.services.list"], e)
            return CloudServiceExtensionList().observable() //this isn't used but is necessary to satisfy the compiler
        }
    }

    fun setupCloudService(userId: String){
        LOG.debug("Attempting to set up cloud service ${availableCloudServicesModel.service.uuid}: ${availableCloudServicesModel.service.name}")
        val protocol = if (applicationProperties["centralcontroller.useSSL"] == "true") "https" else "http"
        val hostname = applicationProperties["server.address"]
        val port = applicationProperties["server.port"]
        //for testing use a hostname verifier that doesn't do any verification
        if ((applicationProperties["centralcontroller.useSSL"] == "true") && (applicationProperties["centralcontroller.disableCertificateValidation"] == "true")) {
            trustSelfSignedSSL()
            LOG.warn("SSL is enabled, but certificate validation is disabled.")
        }
        val requestHeaders = HttpHeaders()
        requestHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

        val callbackURL = "$protocol://$hostname:$port/cloud-service-callback"
        LOG.debug("Calculated callback address: $callbackURL")
        val httpEntity = HttpEntity<CloudServiceUser>(CloudServiceUser(userId, null, availableCloudServicesModel.service.uuid.toString(), callbackURL), requestHeaders)
        LOG.debug("Connecting to central controller cloud service login service at $protocol://${applicationProperties["centralcontroller.host"]}:${applicationProperties["centralcontroller.port"]}/cloud-services/login/${availableCloudServicesModel.service.uuid}")
        runAsync {
            val callResponse = RestTemplate().postForEntity("$protocol://${applicationProperties["centralcontroller.host"]}:${applicationProperties["centralcontroller.port"]}/cloud-service/login/${availableCloudServicesModel.service.uuid}", httpEntity, CloudServiceUser.STATE::class.java)
            LOG.debug("Cloud service setup call response: ${callResponse.statusCode}: ${callResponse.statusCodeValue}")
            LOG.debug("Cloud service user state: ${callResponse.body.name}")
        }
    }
}