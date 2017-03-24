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
 * Created by irotsoma on 8/12/16.
 */
package com.irotsoma.cloudbackenc.cloudbackencui.userinterfaces

import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.scene.web.WebView
import tornadofx.*


class CloudServiceWebView(url: String) : View() {
    override val root: VBox by fxml()
    val cloudServiceWebView: WebView by fxid("cloudServiceWebView")
    val cloudServiceWebViewLabel: Label by fxid("cloudServiceWebViewLabel")
    val cloudServiceWebViewHyperlink: Hyperlink by fxid("cloudServiceWebViewHyperlink")
    init {
        cloudServiceWebView.engine.load(url)
        cloudServiceWebViewHyperlink.text = url
        cloudServiceWebViewLabel.text = messages["cloudbackencui.webview.header"]
    }
}