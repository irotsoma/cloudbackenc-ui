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
 * Created by irotsoma on 8/15/2016.
 */
package com.irotsoma.cloudbackenc.cloudbackencui.users

import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repository object for storing user accounts
 *
 * @author Justin Zak
 */
interface UserAccountRepository : JpaRepository<UserAccount, Long> {
    /**
     * Given a username returns an instance of [UserAccount] or null if the user does not exist
     *
     * @param username Username to lookup in database.
     * @return Instance of [UserAccount] for the given username or null if the user does not exist.
     */
    fun findByUsername(username: String): UserAccount?
    /**
     * Given a user database ID returns an instance of [UserAccount] or null if the user does not exist
     *
     * @param userId User database ID to lookup in database.
     * @return Instance of [UserAccount] for the given user ID or null if the user does not exist.
     */
    //fun findById(userId:Long): UserAccount?
}