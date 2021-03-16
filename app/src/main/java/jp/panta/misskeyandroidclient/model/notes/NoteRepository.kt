package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.model.notes.reaction.CreateReaction

interface NoteRepository {

    suspend fun delete(noteId: Note.Id): Boolean

    suspend fun create(createNote: CreateNote): Note

    suspend fun find(noteId: Note.Id): Note

    suspend fun reaction(createReaction: CreateReaction): Boolean

    suspend fun unreaction(noteId: Note.Id): Boolean
}