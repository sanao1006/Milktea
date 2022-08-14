package net.pantasystem.milktea.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.asLoadingStateFlow
import net.pantasystem.milktea.model.group.*
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class GroupDetailViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userDataSource: UserDataSource,
    private val groupRepository: GroupRepository,
    private val groupDataSource: GroupDataSource,
    private val accountStore: AccountStore,
) : ViewModel() {
    private val uiStateType =
        MutableStateFlow<GroupDetailUiStateType>(GroupDetailUiStateType.Editing(null))

    private val groupSyncState = uiStateType.mapNotNull { type ->
        type.groupId
    }.distinctUntilChanged().flatMapLatest {
        suspend {
            groupRepository.syncOne(it)
        }.asLoadingStateFlow()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ResultState.Loading(StateContent.NotExist()))

    private val membersSyncState = uiStateType.mapNotNull { type ->
        type.groupId
    }.distinctUntilChanged().flatMapLatest {
        groupDataSource.observeOne(it)
    }.flatMapLatest {
        suspend {
            userRepository.syncIn(it.group.userIds).getOrThrow()
        }.asLoadingStateFlow()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ResultState.Loading(StateContent.NotExist()))

    private val members = uiStateType.mapNotNull { type ->
        type.groupId
    }.distinctUntilChanged().flatMapLatest {
        groupDataSource.observeOne(it)
    }.flatMapLatest { groupWithMember ->
        userDataSource.observeIn(
            groupWithMember.group.id.accountId,
            groupWithMember.group.userIds.map { it.id })
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val group = uiStateType.mapNotNull { type ->
        type.groupId
    }.distinctUntilChanged().flatMapLatest {
        groupDataSource.observeOne(it)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val uiState = combine(
        uiStateType,
        group,
        groupSyncState,
        membersSyncState,
        members
    ) { type, g, gss, mss, m ->
        // NOTE: groupIdがnullの状態の時はグループ関連のデータを空かnullにしたい
        // NOTE: 源流となるflowがgroupIdがnullの時はfilterするようにしてしまっているので、ここでempty, nullを割り当てている
        GroupDetailUiState(
            type,
            if (type.groupId == null) null else g?.group,
            if (type.groupId == null) emptyList() else m,
            if (type.groupId == null) ResultState.Fixed(StateContent.NotExist()) else mss,
            if (type.groupId == null) ResultState.Fixed(StateContent.NotExist()) else gss,
            when (type) {
                is GroupDetailUiStateType.Editing -> {
                    type.name
                }
                is GroupDetailUiStateType.Show -> {
                    g?.group?.name ?: ""
                }
            }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(500),
        GroupDetailUiState(GroupDetailUiStateType.Editing(null), null)
    )


    fun setState(stateType: GroupDetailUiStateType) {
        uiStateType.update {
            val group = this.group.value
            if (
                stateType is GroupDetailUiStateType.Editing
                && stateType.groupId == group?.group?.id
            ) {
                stateType.copy(name = group?.group?.name ?: "")
            } else {
                stateType
            }
        }
    }


    fun setName(name: String) {
        uiStateType.update {
            when (it) {
                is GroupDetailUiStateType.Editing -> {
                    it.copy(name = name)
                }
                is GroupDetailUiStateType.Show -> {
                    // NOTE: 表示モードの時に編集しようとした時は、編集モードに移行するようにしている。
                    GroupDetailUiStateType.Editing(it.groupId, name = name)
                }
            }
        }
    }

    fun cancelEditing() {
        uiStateType.update { type ->
            if (type.groupId == null) {
                type
            } else {
                GroupDetailUiStateType.Show(type.groupId!!)
            }
        }
    }

    fun save() {
        val type = uiStateType.value
        if (type is GroupDetailUiStateType.Editing) {
            viewModelScope.launch(Dispatchers.IO) {
                runCatching {
                    if (type.groupId == null) {
                        groupRepository.create(
                            CreateGroup(accountStore.currentAccountId!!, type.name)
                        )
                    } else {
                        groupRepository.update(UpdateGroup(type.groupId!!, type.name))
                    }
                }.onFailure {

                }.onSuccess { group ->
                    uiStateType.update {
                        GroupDetailUiStateType.Show(group.id)
                    }
                }
            }
        }
    }
}

data class GroupDetailUiState(
    val type: GroupDetailUiStateType,
    val group: Group?,
    val members: List<User> = emptyList(),
    val syncMembersState: ResultState<List<User.Id>> = ResultState.Fixed(StateContent.NotExist()),
    val syncGroupState: ResultState<Group> = ResultState.Fixed(StateContent.NotExist()),
    val title: String = "",
)

sealed interface GroupDetailUiStateType {
    val groupId: Group.Id?

    data class Editing(override val groupId: Group.Id?, val name: String = "") : GroupDetailUiStateType

    data class Show(override val groupId: Group.Id) : GroupDetailUiStateType
}


