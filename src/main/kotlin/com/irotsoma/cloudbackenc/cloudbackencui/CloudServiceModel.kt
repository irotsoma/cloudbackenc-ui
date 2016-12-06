/*
 * Copyright (C) 2016  Irotsoma, LLC
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
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
/*
 * Created by irotsoma on 7/27/2016.
 */
package com.irotsoma.cloudbackenc.cloudbackencui

import com.irotsoma.cloudbackenc.common.cloudservicesserviceinterface.CloudServiceExtension
import javafx.beans.property.Property
import tornadofx.ViewModel
import tornadofx.observable
import java.util.*

/**
 * View model class for binding CloudServiceExtension objects to UI components
 *
 * @author Justin Zak
 */
class CloudServiceModel(var service: CloudServiceExtension) : ViewModel() {
    val uuid: Property<UUID> = bind { service.observable(CloudServiceExtension::uuid)  }
    val name: Property<String> = bind { service.observable(CloudServiceExtension::name) }
    val token: Property<String> = bind { service.observable(CloudServiceExtension::token) }
}