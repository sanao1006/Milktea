package jp.panta.misskeyandroidclient.viewmodel.drive.file

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import java.lang.IllegalArgumentException


class FileViewModelFactory(
    private val connectionInstance: ConnectionInstance,
    private val miApplication: MiApplication,
    private val isSelectable: Boolean = false,
    private val maxSelectableItemSize: Int = 0,
    private val folderId: String? = null
) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == FileViewModel::class.java){
            val misskeyAPI = miApplication.misskeyAPIService!!
            return FileViewModel(connectionInstance, misskeyAPI, isSelectable, maxSelectableItemSize, folderId) as T
        }
        throw IllegalArgumentException("クラスが一致しない")
    }
}