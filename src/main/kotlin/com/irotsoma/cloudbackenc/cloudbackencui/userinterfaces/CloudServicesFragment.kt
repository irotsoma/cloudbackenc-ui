/*
 * Copyright (C) 2016-2017  Irotsoma, LLC
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
 * Created by irotsoma on 7/28/2016.
 */
package com.irotsoma.cloudbackenc.cloudbackencui.userinterfaces

import com.irotsoma.cloudbackenc.cloudbackencui.CentralControllerRestInterface
import com.irotsoma.cloudbackenc.cloudbackencui.CloudServiceModel
import com.irotsoma.cloudbackenc.cloudbackencui.trustSelfSignedSSL
import com.irotsoma.cloudbackenc.common.cloudservicesserviceinterface.CloudServiceException
import com.irotsoma.cloudbackenc.common.cloudservicesserviceinterface.CloudServiceExtension
import com.irotsoma.cloudbackenc.common.cloudservicesserviceinterface.CloudServiceExtensionList
import com.irotsoma.cloudbackenc.common.cloudservicesserviceinterface.CloudServiceUser
import javafx.collections.ObservableList
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TableView
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.StageStyle
import mu.KLogging
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

class CloudServicesFragment : Fragment() {
    /** kotlin-logging implementation*/
    companion object: KLogging()
    override val root: VBox by fxml()
    val cloudServiceModel: CloudServiceModel = CloudServiceModel(CloudServiceExtension(UUID.randomUUID(),""))
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
                prefWidth=30.0
            }
            //name column
            with(column(messages["cloudbackencui.column.cloud.service.name"], CloudServiceExtension::name)){
                prefWidth=270.0
            }
            //bind to list of services through model
            cloudServiceModel.rebindOnChange(this){ selectedService -> service = selectedService ?: CloudServiceExtension(UUID.randomUUID(),"") }
            //only enable setup button if something is selected
            selectionModel.selectedItemProperty().onChange{
                cloudServicesSetupButton.isDisable = it == null
            }
        }
        with (cloudServicesSetupButton){
            setOnAction {
                if (cloudServiceModel.service.requiresPassword || cloudServiceModel.service.requiresUsername) {
                    val userInfoPopup = CloudServiceUserInfoFragment(cloudServiceModel.service.name)
                    logger.trace{"Attempting to open user ID popup."}
                    if (!cloudServiceModel.service.requiresUsername) {
                        userInfoPopup.cloudServiceUserInfoUserIDField.isDisable = true
                    }
                    if (!cloudServiceModel.service.requiresPassword) {
                        userInfoPopup.cloudServiceUserInfoPasswordField.isDisable = true
                    }
                    userInfoPopup.openModal(StageStyle.UTILITY, Modality.WINDOW_MODAL, false, this.scene.window, true)
                    logger.trace{"User entered: ${userInfoPopup.userId ?: ""} : ${if (userInfoPopup.password.isNullOrBlank()) "" else "Password Masked"}"}
                    setupCloudService(userInfoPopup.userId, userInfoPopup.password)
                } else {
                    setupCloudService (null, null)
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
    fun getCloudServices() : ObservableList<CloudServiceExtension> {
        try {
            val restInterface = CentralControllerRestInterface()
            if ((restInterface.centralControllerSettings!!.useSSL) && (restInterface.centralControllerSettings!!.disableCertificateValidation)) {
                trustSelfSignedSSL()
                logger.warn{"SSL is enabled, but certificate validation is disabled.  This should only be used in test environments!"}
            }
            val extensions =  RestTemplate().getForObject("${restInterface.centralControllerProtocol}://${restInterface.centralControllerSettings!!.host}:${restInterface.centralControllerSettings!!.port}/cloud-services", CloudServiceExtensionList::class.java).observable()
            if (extensions.size < 1){
                throw(CloudServiceException(messages["cloudbackencui.error.cloud.services.list.empty"]))
            }
            return extensions
        }
        catch (e: ResourceAccessException){
            throw(CloudServiceException(messages["cloudbackencui.error.getting.cloud.services.list"], e))
        }
    }
    fun setupCloudService(userId: String?, password: String?){
        logger.debug{"Attempting to set up cloud service ${cloudServiceModel.service.uuid}: ${cloudServiceModel.service.name}"}
        val restInterface = CentralControllerRestInterface()
        //for testing use a hostname verifier that doesn't do any verification
        if ((restInterface.centralControllerSettings!!.useSSL) && (restInterface.centralControllerSettings!!.disableCertificateValidation)) {
            trustSelfSignedSSL()
            logger.warn{"SSL is enabled, but certificate validation is disabled.  This should only be used in test environments!"}
        }
        val requestHeaders = HttpHeaders()
        requestHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        //TODO: add central controller authentication here

        val callbackURL = "${restInterface.localProtocol}://${restInterface.localHostname}:${restInterface.localPort}/cloud-service-callback"
        logger.debug{"Calculated callback address: $callbackURL"}
        val httpEntity = HttpEntity<CloudServiceUser>(CloudServiceUser(userId ?: "", password, cloudServiceModel.service.uuid.toString(), callbackURL), requestHeaders)
        val centralControllerURL = "${restInterface.centralControllerProtocol}://${restInterface.centralControllerSettings!!.host}:${restInterface.centralControllerSettings!!.port}/cloud-services/login/${cloudServiceModel.service.uuid}"
        logger.debug{"Connecting to central controller cloud service login service at $centralControllerURL"}
        runAsync {
            val callResponse = RestTemplate().postForEntity(centralControllerURL, httpEntity, CloudServiceUser.STATE::class.java)
            logger.debug{"Cloud service setup call response: ${callResponse.statusCode}: ${callResponse.statusCodeValue}"}
            logger.debug{"Cloud service user state: ${callResponse.body.name}"}
        }
    }
}