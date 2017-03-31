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

/**
 * Created by irotsoma on 3/31/17.
 */
package com.irotsoma.cloudbackenc.cloudbackencui.users

import javafx.beans.property.Property
import tornadofx.*

/**
 *
 *
 * @author Justin Zak
 */
class UserAccountModel(var userAccountListObject: UserListObject): ViewModel() {
    val userId: Property<Long> = bind{userAccountListObject.observable(UserListObject::userId)}
    val username: Property<String> = bind{userAccountListObject.observable(UserListObject::username)}
    val isLoggedIn: Property<Boolean> = bind{userAccountListObject.observable(UserListObject::isLoggedIn)}
    val isDefault: Property<Boolean> = bind{userAccountListObject.observable(UserListObject::isDefault)}
}