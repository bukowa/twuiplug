package com.github.bukowa.twuiplug.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.PROJECT)
@State(name = "MyPluginSettings", storages = [Storage("my_plugin_settings.xml")])
class MyPluginSettings : PersistentStateComponent<MyPluginSettings.State> {

    data class State(
        var exePath: String = "",
        var modsFolderPath: String = "",
        var outPutPath: String = ""
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    fun setExePath(path: String) {
        state.exePath = path
    }

    fun setModsFolderPath(path: String) {
        state.modsFolderPath = path
    }

    fun setOutPutPath(path: String) {
        state.outPutPath = path
    }

    fun getExePath(): String = state.exePath
    fun getModsFolderPath(): String = state.modsFolderPath
    fun getOutPutPath(): String = state.outPutPath
}
