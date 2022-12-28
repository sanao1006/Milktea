package net.pantasystem.milktea.data.infrastructure.instance

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.instance.FeatureEnables
import net.pantasystem.milktea.model.instance.FeatureType
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.instance.Version
import javax.inject.Inject

class FeatureEnablesImpl @Inject constructor(
    val misskeyAPIProvider: MisskeyAPIProvider,
    val metaRepository: MetaRepository,
): FeatureEnables {
    override suspend fun isEnable(instanceDomain: String, type: FeatureType, default: Boolean): Boolean {
        return withContext(Dispatchers.IO) {
            val meta = metaRepository.find(instanceDomain).getOrNull()?: return@withContext default
            when(type) {
                FeatureType.Gallery -> meta.getVersion() >= Version("12.75.0")
                FeatureType.Channel -> meta.getVersion() >= Version("12")
                FeatureType.Group -> meta.getVersion() >= Version("11")
                FeatureType.Antenna -> meta.getVersion() >= Version("12.75.0")
                FeatureType.UserReactionHistory -> meta.getVersion() >= Version("12")
            }
        }
    }
}