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
 * Created by irotsoma on 10/4/2016.
 */
package com.irotsoma.cloudbackenc.cloudbackencui.userinterfaces

import com.irotsoma.cloudbackenc.cloudbackencui.applicationProperties
import com.irotsoma.cloudbackenc.cloudbackencui.trustSelfSignedSSL
import com.irotsoma.cloudbackenc.common.CloudBackEncRoles
import com.irotsoma.cloudbackenc.common.CloudBackEncUser
import com.irotsoma.cloudbackenc.common.logger
import javafx.collections.FXCollections
import javafx.scene.control.*
import javafx.scene.layout.VBox
import org.apache.tomcat.util.codec.binary.Base64
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import tornadofx.Fragment
import tornadofx.get

class CreateUserFragment : Fragment() {
    companion object { val LOG by logger() }
    override val root: VBox by fxml()

    val cloudServiceCreateUserIDField : TextField by fxid("cloudServiceCreateUserIDField")
    val cloudServiceCreateUserPasswordField: PasswordField by fxid("cloudServiceCreateUserPasswordField")
    val cloudServiceCreateUserConfirmPasswordField : PasswordField by fxid("cloudServiceCreateUserConfirmPasswordField")
    val cloudServiceCreateUserOkButton : Button by fxid("cloudServiceCreateUserOkButton")
    val cloudServiceCreateUserCancelButton : Button by fxid("cloudServiceCreateUserCancelButton")
    val cloudServiceCreateUserErrorLabel : Label by fxid("cloudServiceCreateUserErrorLabel")
    val cloudServiceCreateUserEmailField : TextField by fxid("cloudServiceCreateUserEmailField")
    val cloudServiceCreateUserRoleList : ListView<String> by fxid("cloudServiceCreateUserRoleList")

    init {
        title = messages["cloudbackencui.title.create.user"]
        with (cloudServiceCreateUserCancelButton){
            setOnAction{
                closeModal()
            }
        }
        with(cloudServiceCreateUserRoleList) {
            items = FXCollections.observableArrayList(CloudBackEncRoles.values().map { it.name })
            selectionModel.selectionMode = SelectionMode.MULTIPLE
        }
        with (cloudServiceCreateUserOkButton){
            setOnAction{
                cloudServiceCreateUserErrorLabel.text = ""
                cloudServiceCreateUserConfirmPasswordField.styleClass.removeAll("error")
                if (cloudServiceCreateUserPasswordField.text != cloudServiceCreateUserConfirmPasswordField.text) {
                    cloudServiceCreateUserErrorLabel.text = messages["cloudbackencui.message.password.mismatch.error"]
                    with(cloudServiceCreateUserConfirmPasswordField) {
                        if (!styleClass.contains("error")) {
                            styleClass.add("error")
                        }
                    }
                    it.consume()
                }
                else if (cloudServiceCreateUserRoleList.selectionModel.selectedItems.size == 0){
                    cloudServiceCreateUserErrorLabel.text = messages["cloudbackencui.message.user.role.required.error"]
                } else {
                    SetupUser()
                    closeModal()
                }
            }
        }
    }

    private fun SetupUser() {
        LOG.debug("Attempting to create user: ${cloudServiceCreateUserIDField.text}.")
        val protocol = if (applicationProperties["centralcontroller.useSSL"] == "true") "https" else "http"
        //for testing use a hostname verifier that doesn't do any verification
        if ((applicationProperties["centralcontroller.useSSL"] == "true") && (applicationProperties["centralcontroller.disableCertificateValidation"] == "true")) {
            trustSelfSignedSSL()
            LOG.warn("SSL is enabled, but certificate validation is disabled.")
        }


        //TODO: remove hard coded username/password

        val plainCreds = "admin:password".toByteArray()
        val base64CredsBytes = Base64.encodeBase64(plainCreds)
        val base64Creds = String(base64CredsBytes)
        val requestHeaders = HttpHeaders()
        requestHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        requestHeaders.add("Authorization", "Basic " + base64Creds)
        val httpEntity = HttpEntity<CloudBackEncUser>(CloudBackEncUser(cloudServiceCreateUserIDField.text,cloudServiceCreateUserPasswordField.text,cloudServiceCreateUserEmailField.text,true, cloudServiceCreateUserRoleList.selectionModel.selectedItems.map{ it -> CloudBackEncRoles.valueOf(it)}), requestHeaders)
        runAsync {
            val callResponse = RestTemplate().postForEntity("$protocol://${applicationProperties["centralcontroller.host"]}:${applicationProperties["centralcontroller.port"]}/users", httpEntity, CloudBackEncUser::class.java)
            LOG.debug("Create User call response: ${callResponse.statusCode}: ${callResponse.statusCodeValue}")
        }
    }
}
