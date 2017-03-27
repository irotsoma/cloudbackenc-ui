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
package com.irotsoma.cloudbackenc.cloudbackencui

import com.fasterxml.jackson.annotation.JsonIgnore
import com.irotsoma.cloudbackenc.common.CloudBackEncUser
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import javax.persistence.*

/**
 * User Account Object
 */
@Entity
@Table(name = "user_account")
class UserAccount(@Column(name = "username", nullable = false) var username: String,
                  password: String) {
    companion object {
        val PASSWORD_ENCODER: PasswordEncoder = BCryptPasswordEncoder()
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @JsonIgnore
    @Column(name="password", nullable=false)
    var password: String? = password
        set(value) {
            field = PASSWORD_ENCODER.encode(value)
        }
    fun cloudBackEncUser(): CloudBackEncUser {
        return CloudBackEncUser(username, CloudBackEncUser.Companion.PASSWORD_MASKED, "", true, emptyList())
    }
}