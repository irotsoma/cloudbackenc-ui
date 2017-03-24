/*
 * Copyright (C) 2017  Irotsoma, LLC
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
package com.irotsoma.cloudbackenc.cloudbackencui

import tornadofx.*

/**
 * Stylesheet class for TornadoFX
 *
 * @author Justin Zak
 */
class CloudBackEncUIStyles : Stylesheet(){
    /**
     * Companion object for style delegates
     */
    companion object style{
        /**
         * Css style for errors on text fields
         */
        val error by cssclass()
    }
    init {
        textField{
            and(error){
                borderColor += box(c("red"))
                borderWidth += box(3.px)
            }
        }
    }
}