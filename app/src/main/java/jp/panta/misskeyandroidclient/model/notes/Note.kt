package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.model.auth.custom.App
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.notes.poll.Poll
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionCount
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.model.users.UserRepository
import jp.panta.misskeyandroidclient.model.users.UserState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.Serializable
import java.lang.Exception
import java.util.*

data class Note(
    val id: Id,
    val createdAt: Date,
    val text: String?,
    val cw: String?,
    val userId: User.Id,

    val replyId: Id?,

    val renoteId: Id?,

    val viaMobile: Boolean?,
    val visibility: String?,
    val localOnly: Boolean?,

    val visibleUserIds: List<User.Id>?,

    val url: String?,
    val uri: String?,
    val renoteCount: Int,
    val reactionCounts: List<ReactionCount>,
    val emojis: List<Emoji>?,
    val repliesCount: Int,
    val files: List<FileProperty>?,
    val poll: Poll?,
    val myReaction: String?,


    val app: App?,
    var instanceUpdatedAt: Date = Date()
) {

    data class Id(
        val accountId: Long,
        val noteId: String
    ) : Serializable

    fun updated(){
        this.instanceUpdatedAt = Date()
    }
}

class NoteRelation(
    val note: Note,
    val user: User,
    val reply: NoteRelation?,
    val renote: NoteRelation?,
)
