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
 * Created by justin on 3/30/17.
 */
package com.irotsoma.cloudbackenc.cloudbackencui

import java.util.prefs.Preferences

object UserPreferences {
    const val ACTIVE_USER_PREFERENCE_KEY = "ACTIVE_USER"
    val systemPreferences: Preferences? = Preferences.userNodeForPackage(this::class.java)
    var activeUser: Long = systemPreferences?.getLong(ACTIVE_USER_PREFERENCE_KEY, -1) ?: -1
    set(value){
        systemPreferences?.putLong(ACTIVE_USER_PREFERENCE_KEY, value)
        field = value
    }
}
