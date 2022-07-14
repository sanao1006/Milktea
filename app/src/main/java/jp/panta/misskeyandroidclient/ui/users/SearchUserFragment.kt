package jp.panta.misskeyandroidclient.ui.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.Activities
import jp.panta.misskeyandroidclient.FollowFollowerActivity
import jp.panta.misskeyandroidclient.UserDetailActivity
import jp.panta.misskeyandroidclient.putActivity
import jp.panta.misskeyandroidclient.ui.users.viewmodel.ToggleFollowViewModel
import jp.panta.misskeyandroidclient.ui.users.viewmodel.search.SearchUserViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SearchUserFragment : Fragment() {

    companion object {
        const val EXTRA_USER_NAME = "jp.panta.misskeyandroidclient.ui.users.SearchUserFragment"

        @JvmStatic
        fun newInstance(userName: String): SearchUserFragment {
            return SearchUserFragment().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_USER_NAME, userName)
                }
            }
        }
    }

//    val mBinding: FragmentSearchUserBinding by dataBinding()

    val viewModel: SearchUserViewModel by viewModels()

    private val toggleFollowViewModel: ToggleFollowViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                MdcTheme {
                    val users by viewModel.users.collectAsState()
                    val state by viewModel.searchState.collectAsState()

                    UserDetailCardList(
                        pageableState = state,
                        users = users,
                        isUserNameMain = false,
                        onAction = ::onAction
                    )
                }
            }
        }.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.search()

    }

    fun onAction(it: UserDetailCardListAction) {
        when (it) {
            is UserDetailCardListAction.CardAction -> {
                when (it.cardAction) {
                    is UserDetailCardAction.FollowersCountClicked -> {
                        startActivity(
                            FollowFollowerActivity.newIntent(
                                requireActivity(),
                                userId = it.cardAction.userId,
                                isFollowing = false,
                            )
                        )
                    }
                    is UserDetailCardAction.FollowingsCountClicked -> {
                        startActivity(
                            FollowFollowerActivity.newIntent(
                                requireActivity(),
                                userId = it.cardAction.userId,
                                isFollowing = true,
                            )
                        )
                    }
                    is UserDetailCardAction.NotesCountClicked -> {
                        val intent = UserDetailActivity.newInstance(
                            requireActivity(),
                            userId = it.cardAction.userId
                        )
                        intent.putActivity(Activities.ACTIVITY_IN_APP)

                        requireActivity().startActivity(intent)
                    }
                    is UserDetailCardAction.OnCardClicked -> {
                        val intent = UserDetailActivity.newInstance(
                            requireActivity(),
                            userId = it.cardAction.userId
                        )
                        intent.putActivity(Activities.ACTIVITY_IN_APP)

                        requireActivity().startActivity(intent)
                    }
                    is UserDetailCardAction.ToggleFollow -> {
                        toggleFollowViewModel.toggleFollow(it.cardAction.userId)
                    }
                }
            }

            UserDetailCardListAction.Refresh -> {
                viewModel.search()
            }
        }
    }




}