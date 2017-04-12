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
 * Created by irotsoma on 3/30/17.
 */
package com.irotsoma.cloudbackenc.cloudbackencui.users

import tornadofx.*

class UserListObject(userId:Long, username:String, isLoggedIn: Boolean, isDefault:Boolean = false){
    var userId: Long by property(userId)
    fun userIdProperty() = getProperty(UserListObject::userId)

    var username by property(username)
    fun usernameProperty() = getProperty(UserListObject::username)

    var isLoggedIn by property(isLoggedIn)
    fun isLoggedInProperty() = getProperty(UserListObject::isLoggedIn)

    var isDefault by property(isDefault)
    fun isDefaultProperty() = getProperty(UserListObject::isDefault)
}