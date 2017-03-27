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
import tornadofx.*

/**
 * Cloud User Login ID Screen
 *
 * @author Justin Zak
 */
class CloudServiceUserInfoFragment(serviceName:String) : Fragment() {
    override val root: VBox by fxml()

    var userId: String? = null
    var password: String? = null

    val cloudServiceUserInfoUserIDField : TextField by fxid("cloudServiceUserInfoUserIDField")
    val cloudServiceUserInfoPasswordField : PasswordField by fxid("cloudServiceUserInfoPasswordField")
    val cloudServiceUserInfoOkButton: Button by fxid("cloudServiceUserInfoOkButton")
    val cloudServiceUserInfoCancelButton: Button by fxid("cloudServiceUserInfoCancelButton")

    init {
        title = "${messages["cloudbackencui.title.enter.user.id"]} $serviceName"
        with(cloudServiceUserInfoOkButton){
            setOnAction{
                userId = cloudServiceUserInfoUserIDField.text
                password = cloudServiceUserInfoPasswordField.text
                close()
            }
        }
        with(cloudServiceUserInfoCancelButton){
            setOnAction {
                userId = null
                password = null
                close()
            }
        }
    }
}
