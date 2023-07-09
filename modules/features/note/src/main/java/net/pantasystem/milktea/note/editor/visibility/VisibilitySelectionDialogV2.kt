package net.pantasystem.milktea.note.editor.visibility

import android.app.Dialog
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.notes.CanLocalOnly
import net.pantasystem.milktea.model.notes.Visibility
import net.pantasystem.milktea.model.notes.isLocalOnly
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.editor.viewmodel.NoteEditorUiState
import net.pantasystem.milktea.note.editor.viewmodel.NoteEditorViewModel

@AndroidEntryPoint
class VisibilitySelectionDialogV2 : BottomSheetDialogFragment() {

    val viewModel by activityViewModels<NoteEditorViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            val view = ComposeView(requireContext()).apply {
                setContent {
                    MdcTheme {
                        VisibilitySelectionDialogContent(viewModel = viewModel)
                    }
                }
            }
            setContentView(view)
            val behavior = (this as BottomSheetDialog).behavior
            behavior.peekHeight = (resources.displayMetrics.density * 350).toInt()
        }
    }


}

@Composable
fun VisibilitySelectionDialogContent(viewModel: NoteEditorViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val channelsState by viewModel.channels.collectAsState()
    VisibilitySelectionDialogLayout(
        uiState = uiState,
        channelsState = channelsState,
        onVisibilityChanged = viewModel::setVisibility,
        onChannelSelected = {
            viewModel.setChannelId(it.id)
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun VisibilitySelectionDialogLayout(
    uiState: NoteEditorUiState,
    channelsState: ResultState<List<Channel>>,
    onVisibilityChanged: (visibility: Visibility) -> Unit,
    onChannelSelected: (channel: Channel) -> Unit,
) {
    val visibility = uiState.sendToState.visibility
    val channels =
        (channelsState.content as? StateContent.Exist)?.rawContent ?: emptyList()
    val channelId = uiState.sendToState.channelId
    Surface(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        Column(
            Modifier.padding(top = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .width(32.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Gray),
                content = {}
            )
            Spacer(Modifier.height(4.dp))

            LazyColumn(
                Modifier.fillMaxWidth()
            ) {
                item {
                    VisibilitySelectionTile(
                        item = Visibility.Public(visibility.isLocalOnly()),
                        isSelected = visibility is Visibility.Public && channelId == null,
                        onClick = onVisibilityChanged
                    )

                    VisibilitySelectionTile(
                        item = Visibility.Home(visibility.isLocalOnly()),
                        isSelected = visibility is Visibility.Home && channelId == null,
                        onClick = onVisibilityChanged
                    )
                    VisibilitySelectionTile(
                        item = Visibility.Followers(
                            visibility.isLocalOnly()
                        ),
                        isSelected = visibility is Visibility.Followers && channelId == null,
                        onClick = onVisibilityChanged
                    )
                    VisibilitySelectionTile(
                        item = Visibility.Specified(
                            emptyList()
                        ),
                        isSelected = visibility is Visibility.Specified && channelId == null,
                        onClick = onVisibilityChanged
                    )

                    if (uiState.currentAccount?.instanceType == Account.InstanceType.MISSKEY) {
                        VisibilityLocalOnlySwitch(
                            checked = visibility.isLocalOnly(),
                            enabled = visibility is CanLocalOnly && channelId == null,
                            onChanged = { result ->
                                (visibility as? CanLocalOnly)?.changeLocalOnly(
                                    result
                                )?.also {
                                    onVisibilityChanged(it as Visibility)
                                }
                            },
                        )

                        VisibilityChannelTitle()
                    }
                }

                if (uiState.currentAccount?.instanceType == Account.InstanceType.MISSKEY) {
                    items(channels) { channel ->
                        VisibilityChannelSelection(
                            item = channel,
                            isSelected = channel.id == channelId,
                            onClick = {
                                onChannelSelected(it)
                            }
                        )
                    }
                }
            }
        }

    }
}


@Composable
fun VisibilityChannelTitle() {
    Text(
        stringResource(R.string.channel),
        fontSize = 20.sp,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
        fontWeight = FontWeight.ExtraBold
    )
}

