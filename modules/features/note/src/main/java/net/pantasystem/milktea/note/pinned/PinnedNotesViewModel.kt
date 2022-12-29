package net.pantasystem.milktea.note.pinned

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.app_store.notes.NoteTranslationStore
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.asLoadingStateFlow
import net.pantasystem.milktea.common_navigation.EXTRA_ACCOUNT_ID
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.CurrentAccountWatcher
import net.pantasystem.milktea.model.notes.FindPinnedNoteUseCase
import net.pantasystem.milktea.model.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.model.notes.NoteRelationGetter
import net.pantasystem.milktea.model.url.UrlPreviewStoreProvider
import net.pantasystem.milktea.model.user.Acct
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import net.pantasystem.milktea.note.timeline.viewmodel.TimelineListItem
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewDataCache
import javax.inject.Inject

@HiltViewModel
class PinnedNotesViewModel @Inject constructor(
    val findPinnedNoteUseCase: FindPinnedNoteUseCase,
    val userRepository: UserRepository,
    val accountRepository: AccountRepository,
    val userDataSource: UserDataSource,
    val noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    val urlPreviewStoreProvider: UrlPreviewStoreProvider,
    val noteTranslationStore: NoteTranslationStore,
    val noteRelationGetter: NoteRelationGetter,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        const val EXTRA_ACCT = "PinnedNotesViewModel.EXTRA_ACCT"
        const val EXTRA_USER_ID = "PinnedNotesViewModel.EXTRA_USER_ID"
    }

    private val userId: User.Id? by lazy {
        savedStateHandle[EXTRA_USER_ID]
    }

    private val accountId: Long? by lazy {
        userId?.accountId
            ?: savedStateHandle.get<Long>(EXTRA_ACCOUNT_ID).let {
                if (it == -1L) null else it
            }
    }

    private val acct: Acct? by lazy {
        savedStateHandle.get<String>(EXTRA_ACCT)?.let {
            Acct(it)
        }
    }

    private val accountWatcher = CurrentAccountWatcher(currentAccountId = accountId, accountRepository)

    private val cache = PlaneNoteViewDataCache({ accountWatcher.getAccount() },
        noteCaptureAPIAdapter,
        noteTranslationStore,
        {
            urlPreviewStoreProvider.getUrlPreviewStore(accountWatcher.getAccount())
        },
        viewModelScope,
        noteRelationGetter
    )

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val notes = suspend {
        getUserId()
    }.asFlow().flatMapLatest {
        userDataSource.observe(it).distinctUntilChanged()
    }.flatMapLatest {
        suspend {
            findPinnedNoteUseCase.invoke(getUserId()).getOrThrow()
        }.asLoadingStateFlow()
    }.map { resultState ->
        resultState.suspendConvert { notes ->
            cache.getByIds(notes.map { it.id })
        }
    }.map { resultState ->
        when(val content = resultState.content) {
            is StateContent.Exist -> {
                content.rawContent.map {
                    TimelineListItem.Note(it)
                }
            }
            is StateContent.NotExist -> {
                listOf(when(resultState) {
                    is ResultState.Error -> TimelineListItem.Error(resultState.throwable)
                    is ResultState.Fixed -> TimelineListItem.Empty
                    is ResultState.Loading -> TimelineListItem.Loading
                })
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimelineListItem.Loading)


    private suspend fun getUserId(): User.Id {
        if (userId != null) {
            return requireNotNull(userId)
        }

        val account = accountWatcher.getAccount()
        if (acct != null) {
            val (userName, host) = requireNotNull(acct).let {
                it.userName to it.host
            }
            return userRepository.findByUserName(account.accountId, userName, host).id
        }
        throw IllegalStateException()
    }
}