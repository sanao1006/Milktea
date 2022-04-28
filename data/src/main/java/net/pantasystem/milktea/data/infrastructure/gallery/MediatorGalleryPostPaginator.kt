package net.pantasystem.milktea.data.infrastructure.gallery

import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.common.Encryption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.FuturePagingController
import net.pantasystem.milktea.common.paginator.PreviousPagingController
import net.pantasystem.milktea.common.paginator.StateLocker
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.gallery.GalleryDataSource
import net.pantasystem.milktea.model.gallery.GalleryPost
import net.pantasystem.milktea.model.user.UserDataSource


interface GalleryPostsStore : StateLocker {
    val state: Flow<PageableState<List<GalleryPost.Id>>>

    suspend fun loadPrevious()
    suspend fun loadFuture()
    suspend fun clear()
}


class GalleryPostsStoreImpl(
    pageable: Pageable.Gallery,
    getAccount: suspend () -> Account,
    misskeyAPIProvider: MisskeyAPIProvider,
    filePropertyDataSource: FilePropertyDataSource,
    userDataSource: UserDataSource,
    galleryDataSource: GalleryDataSource,
    encryption: Encryption
) : GalleryPostsStore {

    override val mutex: Mutex = Mutex()

    private val galleryPostState = GalleryPostsState()
    private val entityAdder =
        GalleryPostsConverter(getAccount, filePropertyDataSource, userDataSource, galleryDataSource)
    private val loader =
        GalleryPostsLoader(pageable, galleryPostState, misskeyAPIProvider, getAccount, encryption)
    private val previousPagingController =
        PreviousPagingController(entityAdder, this, galleryPostState, loader)
    private val futurePaginatorController =
        FuturePagingController(entityAdder, this, galleryPostState, loader)

    override suspend fun loadPrevious() {
        return previousPagingController.loadPrevious()
    }

    override suspend fun loadFuture() {
        futurePaginatorController.loadFuture()
    }

    override suspend fun clear() {
        mutex.withLock {
            galleryPostState.setState(PageableState.Fixed(StateContent.NotExist()))
        }
    }

    override val state: Flow<PageableState<List<GalleryPost.Id>>> = galleryPostState.getFlow()
}

class LikedGalleryPostStoreImpl(
    getAccount: suspend () -> Account,
    misskeyAPIProvider: MisskeyAPIProvider,
    filePropertyDataSource: FilePropertyDataSource,
    userDataSource: UserDataSource,
    galleryDataSource: GalleryDataSource,
    encryption: Encryption
) : GalleryPostsStore {

    override val mutex: Mutex = Mutex()

    private val galleryPostState = LikedGalleryPostsState()
    private val entityAdder = LikedGalleryPostsConverter(
        getAccount,
        filePropertyDataSource,
        userDataSource,
        galleryDataSource
    )
    private val loader =
        LikedGalleryPostsLoader(galleryPostState, misskeyAPIProvider, getAccount, encryption)
    private val previousPagingController =
        PreviousPagingController(entityAdder, this, galleryPostState, loader)
    private val futurePaginatorController =
        FuturePagingController(entityAdder, this, galleryPostState, loader)

    override suspend fun loadPrevious() {
        return previousPagingController.loadPrevious()
    }

    override suspend fun loadFuture() {
        futurePaginatorController.loadFuture()
    }

    override suspend fun clear() {
        mutex.withLock {
            galleryPostState.setState(PageableState.Fixed(StateContent.NotExist()))
        }
    }

    override val state: Flow<PageableState<List<GalleryPost.Id>>> = galleryPostState.getFlow()
}