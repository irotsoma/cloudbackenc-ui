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
 * Created by irotsoma on 7/28/2016.
 */
package com.irotsoma.cloudbackenc.cloudbackencui.userinterfaces

import com.irotsoma.cloudbackenc.cloudbackencui.CentralControllerRestInterface
import com.irotsoma.cloudbackenc.cloudbackencui.UserPreferences
import com.irotsoma.cloudbackenc.cloudbackencui.cloudservices.CloudServiceModel
import com.irotsoma.cloudbackenc.cloudbackencui.trustSelfSignedSSL
import com.irotsoma.cloudbackenc.cloudbackencui.users.UserAccountManager
import com.irotsoma.cloudbackenc.common.cloudservices.CloudServiceException
import com.irotsoma.cloudbackenc.common.cloudservices.CloudServiceExtension
import com.irotsoma.cloudbackenc.common.cloudservices.CloudServiceExtensionList
import com.irotsoma.cloudbackenc.common.cloudservices.CloudServiceUser
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
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import tornadofx.*

/**
 * Cloud Services UI functionality
 *
 * @author Justin Zak
*/

class CloudServicesFragment : Fragment() {
    private data class CloudServicesLists(val availableCloudServices:ObservableList<CloudServiceExtension>, val activeCloudServices:ObservableList<CloudServiceExtension>)
    /** kotlin-logging implementation*/
    companion object: KLogging()
    override val root: VBox by fxml()
    val availableCloudServicesModel: CloudServiceModel by inject()
    val cloudServicesSetupButton : Button by fxid("cloudServicesSetupButton")
    val cloudServicesRefreshButton : Button by fxid("cloudServicesRefreshButton")
    val cloudServicesRemoveButton : Button by fxid("cloudServicesRemoveButton")
    val availableCloudServicesTable : TableView<CloudServiceExtension> by fxid("availableCloudServicesTable")
    val activeCloudServiceModel: CloudServiceModel by inject()
    val activeCloudServicesTable : TableView<CloudServiceExtension> by fxid("activeCloudServicesTable")
    val userAccountRepository = UserAccountManager().userAccountRepository

    init {
        title = messages["cloudbackencui.title.application"]
        //val restTemplate = RestTemplate()
        //populate available cloud services list
        with (availableCloudServicesTable) {
            placeholder = Label(messages["cloudbackencui.message.loading.services"])
            //uuid column
            column(messages["cloudbackencui.column.cloud.service.id"], CloudServiceExtension::extensionUuid)
            //name column
            column(messages["cloudbackencui.column.cloud.service.name"], CloudServiceExtension::extensionName)
            //bind to list of services through model
            bindSelected(availableCloudServicesModel)
            //only enable setup button if something is selected
            selectionModel.selectedItemProperty().onChange{
                cloudServicesSetupButton.isDisable = it == null
            }
            columnResizePolicy = SmartResize.POLICY
        }
        with (activeCloudServicesTable){
            placeholder = Label(messages["cloudbackencui.message.loading.services"])
            //uuid column
            column(messages["cloudbackencui.column.cloud.service.id"], CloudServiceExtension::extensionUuid)
            //name column
            column(messages["cloudbackencui.column.cloud.service.name"], CloudServiceExtension::extensionName).remainingWidth()
            //bind to list of services through model
            bindSelected(activeCloudServiceModel)
            //only enable remove button if something is selected
            selectionModel.selectedItemProperty().onChange{
                cloudServicesRemoveButton.isDisable = it == null
            }
            columnResizePolicy = SmartResize.POLICY
        }
        cloudServicesSetupButton.setOnAction {
            if (availableCloudServicesModel.item.requiresPassword || availableCloudServicesModel.item.requiresUsername) {
                val userInfoPopup = UserInfoFragment(availableCloudServicesModel.item.extensionName)
                logger.trace{"Attempting to open user info popup."}
                if (!availableCloudServicesModel.item.requiresUsername) {
                    userInfoPopup.userInfoUsernameField.isDisable = true
                }
                if (!availableCloudServicesModel.item.requiresPassword) {
                    userInfoPopup.userInfoPasswordField.isDisable = true
                }
                userInfoPopup.openModal(StageStyle.UTILITY, Modality.WINDOW_MODAL, false, cloudServicesSetupButton.scene.window, true)
                logger.trace{"User entered: ${userInfoPopup.username ?: ""} : ${if (userInfoPopup.password.isNullOrBlank()) "" else "Password Masked"}"}
                if (userInfoPopup.username != null || userInfoPopup.password != null) {
                    setupCloudService(userInfoPopup.username, userInfoPopup.password)
                }
            } else {
                setupCloudService (null, null)
            }
        }

        cloudServicesRemoveButton.setOnAction {
            removeCloudService(null)
        }

        cloudServicesRefreshButton.setOnAction{
            refreshTables()
        }
        refreshTables()
    }
    private fun refreshTables(){
        with (availableCloudServicesTable) {
            placeholder = Label(messages["cloudbackencui.message.loading.services"])
            items.clear()
        }
        with (activeCloudServicesTable){
            placeholder = Label(messages["cloudbackencui.message.loading.services"])
            items.clear()
        }
        runAsync {
            val username = userAccountRepository.findById(UserPreferences.activeUser).get().username
            val (availableCloudServices, activeCloudServices) = getCloudServices(username)
            availableCloudServicesTable.items = availableCloudServices
            activeCloudServicesTable.items = activeCloudServices
        } ui {
            availableCloudServicesTable.placeholder = null
            activeCloudServicesTable.placeholder = null
        }
    }
    private fun getLoggedInCloudServices(username: String): CloudServiceExtensionList{
        try {
            val restInterface = CentralControllerRestInterface()
            if ((restInterface.centralControllerSettings!!.useSSL) && (restInterface.centralControllerSettings!!.disableCertificateValidation)) {
                trustSelfSignedSSL()
                logger.warn{"SSL is enabled, but certificate validation is disabled.  This should only be used in test environments!"}
            }
            val requestHeaders = HttpHeaders()
            //TODO: Check token for expiration or null and prompt for login
            val userToken = userAccountRepository.findById(UserPreferences.activeUser).get().token
            requestHeaders.add(HttpHeaders.AUTHORIZATION,"Bearer $userToken")
            val httpEntity = HttpEntity<Any>(requestHeaders)
            return RestTemplate().exchange("${restInterface.centralControllerProtocol}://${restInterface.centralControllerSettings!!.host}:${restInterface.centralControllerSettings!!.port}${restInterface.centralControllerSettings!!.cloudServicesPath}$username",HttpMethod.GET,httpEntity, CloudServiceExtensionList::class.java).body ?: CloudServiceExtensionList()
        }
        catch (e: ResourceAccessException){
            throw(CloudServiceException(messages["cloudbackencui.error.getting.cloud.services.list"], e))
        }
    }
    private fun getCloudServices(username: String?) : CloudServicesLists {
        try {
            val restInterface = CentralControllerRestInterface()
            if ((restInterface.centralControllerSettings!!.useSSL) && (restInterface.centralControllerSettings!!.disableCertificateValidation)) {
                trustSelfSignedSSL()
                logger.warn{"SSL is enabled, but certificate validation is disabled.  This should only be used in test environments!"}
            }
            var availableCloudServices =  RestTemplate().getForObject("${restInterface.centralControllerProtocol}://${restInterface.centralControllerSettings!!.host}:${restInterface.centralControllerSettings!!.port}${restInterface.centralControllerSettings!!.cloudServicesPath}", CloudServiceExtensionList()::class.java)
            if (availableCloudServices.size < 1){
                throw(CloudServiceException(messages["cloudbackencui.error.cloud.services.list.empty"]))
            }
            val activeCloudServices =
            if (username != null){
                 try {
                    getLoggedInCloudServices(username)
                } catch (e: Exception){
                     CloudServiceExtensionList()
                }
            } else {
                CloudServiceExtensionList()
            }
            if(activeCloudServices.isNotEmpty()) {
                availableCloudServices = CloudServiceExtensionList(availableCloudServices.filter{ it !in activeCloudServices })
            }
            return CloudServicesLists(availableCloudServices.observable(),activeCloudServices.observable())
        }
        catch (e: ResourceAccessException){
            throw(CloudServiceException(messages["cloudbackencui.error.getting.cloud.services.list"], e))
        }
    }
    fun setupCloudService(userId: String?, password: String?){
        if (availableCloudServicesModel.isNotEmpty) {
            logger.debug { "Attempting to set up cloud service ${availableCloudServicesModel.item.extensionUuid}: ${availableCloudServicesModel.item.extensionName}" }
            val restInterface = CentralControllerRestInterface()
            //for testing use a hostname verifier that doesn't do any verification
            if ((restInterface.centralControllerSettings!!.useSSL) && (restInterface.centralControllerSettings!!.disableCertificateValidation)) {
                trustSelfSignedSSL()
                logger.warn { "SSL is enabled, but certificate validation is disabled.  This should only be used in test environments!" }
            }
            val requestHeaders = HttpHeaders()
            requestHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            //TODO: Check token for expiration or null and prompt for login

            val userToken = userAccountRepository.findById(UserPreferences.activeUser).get().token
            requestHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer $userToken")

            val callbackURL = "${restInterface.localProtocol}://${restInterface.localHostname}:${restInterface.localPort}/cloud-service-callback"
            logger.debug { "Calculated callback address: $callbackURL" }
            val httpEntity = HttpEntity<CloudServiceUser>(CloudServiceUser(userId ?: "", password, availableCloudServicesModel.item.extensionUuid.toString(), callbackURL), requestHeaders)
            val centralControllerURL = "${restInterface.centralControllerProtocol}://${restInterface.centralControllerSettings!!.host}:${restInterface.centralControllerSettings!!.port}${restInterface.centralControllerSettings!!.cloudServicesPath}/login/${availableCloudServicesModel.item.extensionUuid}"
            logger.debug { "Connecting to central controller cloud service login service at $centralControllerURL" }
            runAsync {
                val callResponse = RestTemplate().postForEntity(centralControllerURL, httpEntity, CloudServiceUser.STATE::class.java)
                logger.debug { "Cloud service setup call response: ${callResponse.statusCode}: ${callResponse.statusCode?.name}" }
                logger.debug { "Cloud service user state: ${callResponse.body?.name}" }
            } ui {
                refreshTables()
            }
        }
    }
    fun removeCloudService(userId: String?){
        if (activeCloudServiceModel.isNotEmpty) {
            logger.debug { "Attempting to log out of cloud service ${activeCloudServiceModel.item.extensionUuid}: ${activeCloudServiceModel.item.extensionName}" }
            val restInterface = CentralControllerRestInterface()
            //for testing use a hostname verifier that doesn't do any verification
            if ((restInterface.centralControllerSettings!!.useSSL) && (restInterface.centralControllerSettings!!.disableCertificateValidation)) {
                trustSelfSignedSSL()
                logger.warn { "SSL is enabled, but certificate validation is disabled.  This should only be used in test environments!" }
            }
            val requestHeaders = HttpHeaders()
            requestHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            val userToken = userAccountRepository.findById(UserPreferences.activeUser).get().token
            requestHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer $userToken")
            val httpEntity = HttpEntity<CloudServiceUser>(CloudServiceUser(userId ?: "", null, availableCloudServicesModel.item.extensionUuid.toString(), null),requestHeaders)
            val centralControllerURL = "${restInterface.centralControllerProtocol}://${restInterface.centralControllerSettings!!.host}:${restInterface.centralControllerSettings!!.port}${restInterface.centralControllerSettings!!.cloudServicesPath}/logout/${activeCloudServiceModel.item.extensionUuid}"
            runAsync {
                val callResponse = RestTemplate().exchange(centralControllerURL, HttpMethod.GET, httpEntity, CloudServiceUser.STATE::class.java)
                logger.debug { "Cloud service logout call response: ${callResponse?.statusCode}: ${callResponse?.statusCode?.name}" }
                logger.debug { "Cloud service user state: ${callResponse?.body?.name}" }
            } ui {
                refreshTables()
            }
        }
    }
}