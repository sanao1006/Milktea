package net.pantasystem.milktea.model.group

import net.pantasystem.milktea.model.user.User

data class Pull (
    val groupId: Group.Id,
    val userId: User.Id,
)