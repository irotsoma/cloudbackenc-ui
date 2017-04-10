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
 * Created by irotsoma on 7/19/2016.
 */

package com.irotsoma.cloudbackenc.cloudbackencui.userinterfaces

import com.irotsoma.cloudbackenc.cloudbackencui.CentralControllerRestInterface
import com.irotsoma.cloudbackenc.cloudbackencui.trustSelfSignedSSL
import com.irotsoma.cloudbackenc.cloudbackencui.users.UserAccount
import com.irotsoma.cloudbackenc.cloudbackencui.users.UserAccountManager
import com.irotsoma.cloudbackenc.common.AuthenticationToken
import javafx.scene.control.MenuItem
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.StageStyle
import mu.KLogging
import org.apache.tomcat.util.codec.binary.Base64
import org.springframework.context.annotation.Lazy
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import tornadofx.*


/**
 * Startup view
 *
 * Using @Lazy to allow for tests to run on the Spring services without launching the UI
 *
 * @author Justin Zak
*/
@Lazy
@Component
class MainView : View() {
    /** kotlin-logging implementation*/
    companion object: KLogging()
    override val root: VBox by fxml()
    final val menuCloudServicesSetup : MenuItem by fxid("menuCloudServicesSetup")
    final val menuUsersCreateUser : MenuItem by fxid("menuUsersCreateUser")
    final val menuUsersLogin: MenuItem by fxid("menuUsersLogin")
    final val menuUsersList: MenuItem by fxid("menuUsersList")
    init{
        title = messages["cloudbackencui.title.application"]
        menuCloudServicesSetup.setOnAction{
            CloudServicesFragment().openModal(StageStyle.DECORATED, Modality.APPLICATION_MODAL, false)
        }
        menuUsersCreateUser.setOnAction {
            CreateUserFragment().openModal(StageStyle.UTILITY, Modality.APPLICATION_MODAL, false)
        }
        menuUsersList.setOnAction{
            UserListFragment().openModal(StageStyle.DECORATED,Modality.APPLICATION_MODAL, false)
        }
        menuUsersLogin.setOnAction {
            val userInfoPopup = UserInfoFragment(messages["cloudbackencui.title.product"]).apply{openModal(StageStyle.UTILITY,Modality.APPLICATION_MODAL, false, this.currentWindow, true)}
            if (userInfoPopup.username != null && userInfoPopup.password != null) {
                val user = userInfoPopup.username!!
                val plainUserCredentials = "$user:${userInfoPopup.password}".toByteArray()
                val base64UserCredentials = String(Base64.encodeBase64(plainUserCredentials))
                val tokenRequestHeaders = HttpHeaders()
                tokenRequestHeaders.add(HttpHeaders.AUTHORIZATION, "Basic $base64UserCredentials")
                val httpTokenEntity = HttpEntity<Any>(tokenRequestHeaders)
                runAsync {
                    val restInterface = CentralControllerRestInterface()
                    //for testing use a hostname verifier that doesn't do any verification
                    if ((restInterface.centralControllerSettings!!.useSSL) && (restInterface.centralControllerSettings!!.disableCertificateValidation)) {
                        trustSelfSignedSSL()
                        logger.warn { "SSL is enabled, but certificate validation is disabled.  This should only be used in test environments!" }
                    }
                    val tokenResponse = RestTemplate().exchange("${restInterface.centralControllerProtocol}://${restInterface.centralControllerSettings!!.host}:${restInterface.centralControllerSettings!!.port}/auth", HttpMethod.GET, httpTokenEntity, AuthenticationToken::class.java)
                    if (tokenResponse.statusCode == HttpStatus.OK) {
                        //update or insert user in database
                        val userAccountRepository = UserAccountManager().userAccountRepository
                        val userAccount = userAccountRepository.findByUsername(user)
                        if (userAccount == null){
                            UserAccount(user, tokenResponse.body.token,tokenResponse.body.tokenExpiration)
                        } else {
                            userAccount.token = tokenResponse.body.token
                            userAccount.tokenExpiration = tokenResponse.body.tokenExpiration
                        }
                        userAccountRepository.save(userAccount)
                        //TODO: add success message popup
                    }
                }
            }
        }
    }

}
