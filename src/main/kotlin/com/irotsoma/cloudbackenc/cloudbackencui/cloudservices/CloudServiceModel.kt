/*
 * Copyright (C) 2016-2018  Irotsoma, LLC
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
 * Created by irotsoma on 7/27/2016.
 */
package com.irotsoma.cloudbackenc.cloudbackencui.cloudservices

import com.irotsoma.cloudbackenc.common.cloudservices.CloudServiceExtension
import tornadofx.*

/**
 * View model class for binding CloudServiceExtension objects to UI components
 *
 * @author Justin Zak
 * @property uuid Binds the cloud service UUID to the table.
 * @property name Binds the cloud service name to the table.
 * @property token Binds a cloud service's authentication token to a row.  Should not be displayed in the table.
 */
class CloudServiceModel : ItemViewModel<CloudServiceExtension>() {
    val uuid = bind { item?.observable(CloudServiceExtension::extensionUuid) }
    val name = bind { item?.observable(CloudServiceExtension::extensionName) }
}