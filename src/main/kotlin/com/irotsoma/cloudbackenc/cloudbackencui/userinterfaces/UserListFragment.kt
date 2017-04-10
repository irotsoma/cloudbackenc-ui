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
 * Created by justin on 3/30/17.
 */
package com.irotsoma.cloudbackenc.cloudbackencui.userinterfaces

import com.irotsoma.cloudbackenc.cloudbackencui.UserPreferences
import com.irotsoma.cloudbackenc.cloudbackencui.users.UserAccountManager
import com.irotsoma.cloudbackenc.cloudbackencui.users.UserAccountModel
import com.irotsoma.cloudbackenc.cloudbackencui.users.UserListObject
import javafx.collections.ObservableList
import javafx.scene.control.Button
import javafx.scene.control.TableView
import javafx.scene.layout.VBox
import tornadofx.*
import java.util.*


class UserListFragment : Fragment() {
    override val root: VBox by fxml()
    val userAccountModel: UserAccountModel = UserAccountModel(UserListObject(-1,"",false,false))
    val listUsersSetDefaultButton: Button by fxid("listUsersSetDefaultButton")
    val listUsersUserTable: TableView<UserListObject> by fxid("listUsersUserTable")
    init {
        title = messages["cloudbackencui.title.user.list"]
        with(listUsersUserTable){
            asyncItems {
                getUsers()
            }
            with(column(messages["cloudbackencui.column.user.loggedin"], UserListObject::isLoggedIn)){
                prefWidth=75.0
            }
            with(column(messages["cloudbackencui.column.user.username"], UserListObject::username)){
                prefWidth=200.0
            }
            with(column(messages["cloudbackencui.column.user.default"], UserListObject::isDefault)){
                prefWidth=75.0
            }
            userAccountModel.rebindOnChange(this){selectedAccount -> userAccountListObject = selectedAccount ?: UserListObject(-1,"",false,false)}
            selectionModel.selectedItemProperty().onChange{
                listUsersSetDefaultButton.isDisable = it == null
            }
        }
        with(listUsersSetDefaultButton){
            setOnAction{
                UserPreferences.activeUser = userAccountModel.userAccountListObject.userId
                with (listUsersUserTable) {
                    items.clear()
                    asyncItems {
                        getUsers()
                    }
                }
            }
        }
    }
    fun getUsers() : ObservableList<UserListObject>{
        val userAccountManager = UserAccountManager()
        val userAccounts= userAccountManager.userAccountRepository.findAll()
        val userList = ArrayList<UserListObject>()

        userAccounts.mapTo(userList) { UserListObject(it.id, it.username, (it.tokenExpiration ?: Date(Long.MAX_VALUE) > Date()) , it.id == UserPreferences.activeUser) }
        return userList.observable()
    }

}
