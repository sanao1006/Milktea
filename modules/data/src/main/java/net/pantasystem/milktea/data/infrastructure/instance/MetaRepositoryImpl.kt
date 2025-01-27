package net.pantasystem.milktea.data.infrastructure.instance

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.instance.HostWithVersion
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.instance.MetaDataSource
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.api.misskey.instance.RequestMeta
import java.net.URL
import javax.inject.Inject

class MetaRepositoryImpl @Inject constructor(
    private val metaDataSource: MetaDataSource,
    private val metaCache: MetaCache,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
): MetaRepository {

    override suspend fun sync(instanceDomain: String): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val meta = fetch(instanceDomain)
            metaDataSource.add(meta)
            metaCache.put(meta.uri, meta)
            HostWithVersion.put(URL(meta.uri).host, meta.getVersion())
        }
    }

    override suspend fun find(instanceDomain: String): Result<Meta> = runCancellableCatching {
        withContext(ioDispatcher) {
            val cacheMeta = metaCache.get(instanceDomain)
            if (cacheMeta != null) {
                return@withContext cacheMeta
            }
            val localMeta = metaDataSource.get(instanceDomain)
            if (localMeta == null) {
                val meta = fetch(instanceDomain)
                metaDataSource.add(meta)
            } else {
                localMeta
            }.also { meta ->
                metaCache.put(meta.uri, meta)
                HostWithVersion.put(URL(meta.uri).host, meta.getVersion())
            }
        }

    }


    override fun observe(instanceDomain: String): Flow<Meta?> {
        return metaDataSource.observe(instanceDomain).onEach { meta ->
            if (meta != null) {
                metaCache.put(instanceDomain, meta)
                HostWithVersion.put(URL(meta.uri).host, meta.getVersion())
            }
        }
    }

    private suspend fun fetch(instanceDomain: String): Meta {
        val res = misskeyAPIProvider.get(instanceDomain).getMeta(RequestMeta(detail = true))
            .throwIfHasError()
        return res.body()!!.toModel()
    }

}