package jp.panta.misskeyandroidclient.model.channel

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.api.misskey.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.api.misskey.throwIfHasError
import jp.panta.misskeyandroidclient.api.misskey.v12.MisskeyAPIV12
import jp.panta.misskeyandroidclient.api.misskey.v12.channel.ChannelDTO
import jp.panta.misskeyandroidclient.api.misskey.v12.channel.FindPageable
import jp.panta.misskeyandroidclient.model.*
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.util.PageableState
import jp.panta.misskeyandroidclient.util.StateContent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response

enum class ChannelListType {
    OWNED, FOLLOWED, FEATURED
}

class ChannelPagingModel @AssistedInject constructor(
    val encryption: Encryption,
    private val channelStateModel: ChannelStateModel,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val accountRepository: AccountRepository,
    val loggerFactory: Logger.Factory,
    @Assisted val accountId: Long,
    @Assisted val type: ChannelListType,
) : EntityConverter<ChannelDTO, Channel.Id>, PreviousLoader<ChannelDTO>,
    IdGetter<Channel.Id>, StateLocker, PaginationState<Channel.Id> {

    val logger: Logger by lazy {
        loggerFactory.create("ChannelPagingModel")
    }

    @AssistedFactory
    interface ModelAssistedFactory {
        fun create(accountId: Long, type: ChannelListType): ChannelPagingModel
    }

    override val mutex: Mutex = Mutex()


    private val _state = MutableStateFlow<PageableState<List<Channel.Id>>>(
        PageableState.Fixed(
            StateContent.NotExist()
        )
    )
    override val state: Flow<PageableState<List<Channel.Id>>>
        get() = _state


    override fun getState(): PageableState<List<Channel.Id>> {
        return _state.value
    }

    override suspend fun getSinceId(): Channel.Id? {
        if (type == ChannelListType.FEATURED) {
            return null
        }
        return (getState().content as? StateContent.Exist)?.rawContent?.firstOrNull()
    }

    override suspend fun getUntilId(): Channel.Id? {
        if (type == ChannelListType.FEATURED) {
            return null
        }
        return (getState().content as? StateContent.Exist)?.rawContent?.lastOrNull()
    }

    override fun setState(state: PageableState<List<Channel.Id>>) {
        _state.value = state
    }

    override suspend fun convertAll(list: List<ChannelDTO>): List<Channel.Id> {
        val account = accountRepository.get(accountId)
        return channelStateModel.addAll(list.map { it.toModel(account) }).map { it.id }
    }
//    NOTE: MisskeyのAPIがバグってるのか正常に動かない（Postmanからもチェック済み）
//    override suspend fun loadFuture(): Response<List<ChannelDTO>> {
//        val sinceId = getSinceId()?.channelId
//        logger.debug("loadFuture type:$type, sinceId:$sinceId")
//        val account = accountRepository.get(accountId)
//        val api = (misskeyAPIProvider.get(account) as MisskeyAPIV12)
//        val i = account.getI(encryption)
//        val res = when (type) {
//            ChannelListType.FOLLOWED -> api.followedChannels(
//                FindPageable(
//                    i = i,
//                    sinceId = sinceId,
//                    untilId = null,
//                )
//            )
//            ChannelListType.OWNED -> api.ownedChannels(
//                FindPageable(
//                    i = i,
//                    sinceId = sinceId,
//                    untilId = null
//                )
//            )
//            ChannelListType.FEATURED -> {
//                throw IllegalStateException("featuredはサポートしていません。")
//            }
//        }
//        return res.throwIfHasError()
//    }

    override suspend fun loadPrevious(): Response<List<ChannelDTO>> {
        val account = accountRepository.get(accountId)
        val api = (misskeyAPIProvider.get(account) as MisskeyAPIV12)
        val i = account.getI(encryption)
        val res = when (type) {
            ChannelListType.FOLLOWED -> {
                if (getUntilId() != null) {
                    // TODO: APIのページネーションが修正されたら修正する
                    throw IllegalStateException()
                }
                api.followedChannels(
                    FindPageable(
                        i = i,
                        sinceId = null,
                        untilId = null,
//                    untilId = getUntilId()?.channelId,
                        limit = 99,
                    )
                )
            }
            ChannelListType.OWNED -> {
                if (getUntilId() != null) {
                    // TODO: APIのページネーションが修正されたら修正する
                    throw IllegalStateException()
                }
                api.ownedChannels(
                    FindPageable(
                        i = i,
                        sinceId = null,
                        untilId = null,
//                    untilId = getUntilId()?.channelId,
                        limit = 99,
                    )
                )
            }
            ChannelListType.FEATURED -> {
                if (getUntilId() != null) {
                    // NOTE: featuredはページネーションできないので
                    throw IllegalStateException()
                }
                api.featuredChannels(I(i))
            }
        }
        return res.throwIfHasError()
    }


    suspend fun clear() {
        mutex.withLock {
            _state.value = PageableState.Fixed(StateContent.NotExist())
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeChannels(): Flow<PageableState<List<Channel>>> {
        return channelStateModel.state.flatMapLatest { globalState ->
            state.map { state ->
                state.convert { list ->
                    list.mapNotNull { id ->
                        globalState.get(id)
                    }
                }
            }
        }.distinctUntilChanged()
    }
}