package net.pantasystem.milktea.note.detail.viewmodel

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import net.pantasystem.milktea.app_store.notes.NoteTranslationStore
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.note.NoteDataSource
import net.pantasystem.milktea.model.note.NoteRelation
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData

//viewはRecyclerView
class NoteConversationViewData(
    noteRelation: NoteRelation,
    account: Account,
    translationStore: NoteTranslationStore,
    coroutineScope: CoroutineScope,
    noteDataSource: NoteDataSource,
    configRepository: LocalConfigRepository,
) : PlaneNoteViewData(
    noteRelation,
    account,
    translationStore,
    noteDataSource,
    configRepository,
    coroutineScope
) {
    val conversation = MutableLiveData<List<PlaneNoteViewData>>()
    val hasConversation = MutableLiveData<Boolean>()

}