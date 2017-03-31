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
 * Created by irotsoma on 8/15/2016.
 */
package com.irotsoma.cloudbackenc.cloudbackencui.users

import java.util.*
import javax.persistence.*

/**
 * User Account Object
 *
 * @author Justin Zak
 * @property id Unique database id for user
 * @property username Username of user.
 * @property token Login token for Central Controller.
 * @property tokenExpiration Expiration date/time of the token.
 */
@Entity
@Table(name = "user_account")
class UserAccount(@Column(name = "username", nullable = false) var username: String,
                  @Column(name = "token", nullable = false) var token: String,
                  @Column(name="token_expiration",nullable = false) var tokenExpiration: Date) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}