package net.pantasystem.milktea.model.user

import net.pantasystem.milktea.model.user.query.FindUsersQuery

interface UserRepository {

    suspend fun find(userId: User.Id, detail: Boolean = true): User

    suspend fun findByUserName(accountId: Long, userName: String, host: String?, detail: Boolean = true): User

    suspend fun syncByUserName(accountId: Long, userName: String, host: String?): Result<Unit>

    suspend fun searchByNameOrUserName(accountId: Long, keyword: String, limit: Int = 100, nextId: String? = null, host: String? = null): List<User>


    suspend fun findUsers(accountId: Long, query: FindUsersQuery): List<User>

    suspend fun sync(userId: User.Id): Result<Unit>

    suspend fun syncIn(userIds: List<User.Id>): Result<List<User.Id>>
}