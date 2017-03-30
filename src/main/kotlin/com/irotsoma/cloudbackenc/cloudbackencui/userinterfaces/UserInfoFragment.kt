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
 * Created by irotsoma on 9/28/2016.
 */
package com.irotsoma.cloudbackenc.cloudbackencui.userinterfaces

import javafx.scene.control.Button
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import tornadofx.Fragment
import tornadofx.get

/**
 * User Login Screen
 *
 * @author Justin Zak
 */
class UserInfoFragment(serviceName:String) : Fragment() {
    override val root: VBox by fxml()

    var username: String? = null
    var password: String? = null

    val userInfoUsernameField: TextField by fxid("userInfoUsernameField")
    val userInfoPasswordField: PasswordField by fxid("userInfoPasswordField")
    val userInfoOkButton: Button by fxid("userInfoOkButton")
    val userInfoCancelButton: Button by fxid("userInfoCancelButton")

    init {
        title = "${messages["cloudbackencui.title.enter.user.info"]} $serviceName"
        with(userInfoOkButton){
            setOnAction{
                username = userInfoUsernameField.text
                password = userInfoPasswordField.text
                close()
            }
        }
        with(userInfoCancelButton){
            setOnAction {
                username = null
                password = null
                close()
            }
        }
    }
}
