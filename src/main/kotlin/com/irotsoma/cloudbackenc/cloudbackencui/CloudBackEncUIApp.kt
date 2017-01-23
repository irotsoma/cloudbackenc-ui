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
 * Created by irotsoma on 7/19/2016.
 */
package com.irotsoma.cloudbackenc.cloudbackencui

import com.irotsoma.cloudbackenc.cloudbackencui.userinterfaces.MainView
import javafx.stage.Stage
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Lazy
import tornadofx.App
import tornadofx.FX
import java.util.*

/**
 * TornadoFX app class launched by spring boot
 *
 * Based on JavaFx example:  https://github.com/thomasdarimont/spring-labs/tree/master/spring-boot-javafx
 * App param = startup view class, style class
 *
 * @author Justin Zak
*/

@Lazy
@SpringBootApplication
class CloudBackEncUIApp : App(MainView::class, CloudBackEncUIStyles::class){
    companion object{
        @JvmStatic private var savedArgs: Array<String> = emptyArray()
        //launches the tornadoFX application
        @JvmStatic fun launchApp(args: Array<String>){
            savedArgs = args
            launch(CloudBackEncUIApp::class.java, *args)
        }
    }
    private var applicationContext: ConfigurableApplicationContext? = null

    override fun init() {
        //set the TornadoFX messages object to use the lowercase version of the resource bundle to match spring boot default (by default TornadoFX uses "Messages")
        FX.messages = ResourceBundle.getBundle("messages")
        //disable default stream handler to prevent factory already defined error when running as jar
        TomcatURLStreamHandlerFactory.disable()
        //run the spring boot app in the background
        applicationContext = SpringApplication.run(arrayOf(CloudBackEncUIApp::class.java), savedArgs)
        applicationContext!!.autowireCapableBeanFactory.autowireBean(this)
    }

    override fun start(stage: Stage) {
        super.start(stage)
    }

    override fun stop() {
        super.stop()
        applicationContext!!.close()
    }

}

