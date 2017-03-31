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
 * Created by irotsoma on 10/4/2016.
 */
package com.irotsoma.cloudbackenc.cloudbackencui.userinterfaces

import com.irotsoma.cloudbackenc.cloudbackencui.CentralControllerRestInterface
import com.irotsoma.cloudbackenc.cloudbackencui.trustSelfSignedSSL
import com.irotsoma.cloudbackenc.cloudbackencui.users.UserAccount
import com.irotsoma.cloudbackenc.cloudbackencui.users.UserAccountManager
import com.irotsoma.cloudbackenc.common.AuthenticationToken
import com.irotsoma.cloudbackenc.common.CloudBackEncRoles
import com.irotsoma.cloudbackenc.common.CloudBackEncUser
import javafx.collections.FXCollections
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.StageStyle
import mu.KLogging
import org.apache.tomcat.util.codec.binary.Base64
import org.springframework.http.*
import org.springframework.web.client.RestTemplate
import tornadofx.*

class CreateUserFragment : Fragment() {
    /** kotlin-logging implementation*/
    companion object: KLogging()
    override val root: VBox by fxml()

    val createUserUsernameField: TextField by fxid("createUserUsernameField")
    val createUserPasswordField: PasswordField by fxid("createUserPasswordField")
    val createUserConfirmPasswordField: PasswordField by fxid("createUserConfirmPasswordField")
    val createUserOkButton: Button by fxid("createUserOkButton")
    val createUserCancelButton: Button by fxid("createUserCancelButton")
    val createUserErrorLabel: Label by fxid("createUserErrorLabel")
    val createUserEmailField: TextField by fxid("createUserEmailField")
    val createUserRoleList: ListView<String> by fxid("createUserRoleList")

    init {
        title = messages["cloudbackencui.title.create.user"]
        with (createUserCancelButton){
            setOnAction{
                close()
            }
        }
        with(createUserRoleList) {
            items = FXCollections.observableArrayList(CloudBackEncRoles.values().map { it.name })
            selectionModel.selectionMode = SelectionMode.MULTIPLE
        }
        with (createUserOkButton){
            setOnAction{
                createUserErrorLabel.text = ""
                createUserConfirmPasswordField.styleClass.removeAll("error")
                if (createUserPasswordField.text != createUserConfirmPasswordField.text) {
                    createUserErrorLabel.text = messages["cloudbackencui.message.password.mismatch.error"]
                    with(createUserConfirmPasswordField) {
                        if (!styleClass.contains("error")) {
                            styleClass.add("error")
                        }
                    }
                    it.consume()
                }
                else if (createUserRoleList.selectionModel.selectedItems.size == 0){
                    createUserErrorLabel.text = messages["cloudbackencui.message.user.role.required.error"]
                } else {
                    SetupUser()
                    close()
                }
            }
        }
    }

    private fun SetupUser() {
        logger.debug{"Attempting to create user: ${createUserUsernameField.text}."}
        val restInterface = CentralControllerRestInterface()
        //for testing use a hostname verifier that doesn't do any verification
        if ((restInterface.centralControllerSettings!!.useSSL) && (restInterface.centralControllerSettings!!.disableCertificateValidation)) {
            trustSelfSignedSSL()
            logger.warn{"SSL is enabled, but certificate validation is disabled.  This should only be used in test environments!"}
        }


        //TODO: remove hard coded username/password and add popup to prompt for admin user login
        val userInfoPopup = UserInfoFragment("Administrator")
        logger.trace{"Attempting to open user info popup."}
        userInfoPopup.openModal(StageStyle.UTILITY, Modality.WINDOW_MODAL, false, this.currentWindow, true)
        logger.trace{"User entered: ${userInfoPopup.username ?: ""} : ${if (userInfoPopup.password.isNullOrBlank()) "" else "Password Masked"}"}
        if (userInfoPopup.username == null || userInfoPopup.password == null){
            return
        }
        val plainCredentials = "${userInfoPopup.username?.trim()}:${userInfoPopup.password}".toByteArray()
        val base64Credentials = String(Base64.encodeBase64(plainCredentials))
        val requestHeaders = HttpHeaders()
        requestHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        requestHeaders.add(HttpHeaders.AUTHORIZATION, "Basic $base64Credentials")
        val newUsername = createUserUsernameField.text.trim()
        val httpEntity = HttpEntity<CloudBackEncUser>(CloudBackEncUser(newUsername, createUserPasswordField.text, createUserEmailField.text,true, createUserRoleList.selectionModel.selectedItems.map{ it -> CloudBackEncRoles.valueOf(it)}), requestHeaders)
        val plainUserCredentials = "$newUsername:${createUserPasswordField.text}".toByteArray()
        val base64UserCredentials = String(Base64.encodeBase64(plainUserCredentials))
        val tokenRequestHeaders = HttpHeaders()
        tokenRequestHeaders.add(HttpHeaders.AUTHORIZATION, "Basic $base64UserCredentials")
        val httpTokenEntity = HttpEntity<Any>(tokenRequestHeaders)
        //TODO: add progress spinner
        runAsync {
            //make call to add user
            val callResponse = RestTemplate().postForEntity("${restInterface.centralControllerProtocol}://${restInterface.centralControllerSettings!!.host}:${restInterface.centralControllerSettings!!.port}/users", httpEntity, CloudBackEncUser::class.java)
            logger.debug{"Create User call response: ${callResponse.statusCode}: ${callResponse.statusCodeValue}"}
            if (callResponse.statusCode == HttpStatus.CREATED) {
                //make call to create a token
                val tokenResponse = RestTemplate().exchange("${restInterface.centralControllerProtocol}://${restInterface.centralControllerSettings!!.host}:${restInterface.centralControllerSettings!!.port}/auth", HttpMethod.GET, httpTokenEntity, AuthenticationToken::class.java)
                if (tokenResponse.statusCode == HttpStatus.OK) {
                    //update or insert user in database
                    val userAccountRepository = UserAccountManager().userAccountRepository
                    //Note: sets expiration to 100 years in the future if it's null as a workaround for no expiration date
                    val userAccount = userAccountRepository.findByUsername(newUsername) ?: UserAccount(newUsername,tokenResponse.body.token,tokenResponse.body.tokenExpiration)
                    userAccountRepository.save(userAccount)
                    //TODO: add success message popup
                }
            }
        }
    }
}
