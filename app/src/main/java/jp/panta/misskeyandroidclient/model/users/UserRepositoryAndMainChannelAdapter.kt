package jp.panta.misskeyandroidclient.model.users

import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.streaming.ChannelBody
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPI
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPIWithAccountProvider
import kotlinx.coroutines.flow.*


/**
 * ChannelAPIのMainのイベントをUserRepositoryへ適応するAdapter
 */
class UserRepositoryAndMainChannelAdapter(
    private val userDataSource: UserDataSource,
    private val channelAPIWithAccountProvider: ChannelAPIWithAccountProvider,
) {


    fun listen(account: Account): Flow<ChannelBody.Main.HavingUserBody> {
        return suspend {
            channelAPIWithAccountProvider.get(account)
        }.asFlow().flatMapLatest {
            it.connect(ChannelAPI.Type.MAIN)
        }.map {
            it as? ChannelBody.Main.HavingUserBody
        }.filterNotNull().onEach {
            userDataSource.add(it.body.toUser(account, true))
        }
    }

}