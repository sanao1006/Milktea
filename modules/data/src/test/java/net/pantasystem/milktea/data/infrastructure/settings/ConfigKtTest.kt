package net.pantasystem.milktea.data.infrastructure.settings

import net.pantasystem.milktea.model.setting.Config
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.Keys
import net.pantasystem.milktea.model.setting.PrefType
import net.pantasystem.milktea.model.setting.Theme
import net.pantasystem.milktea.model.setting.allKeys
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ConfigKtTest {


    @Test
    fun allKeys() {
        Assertions.assertEquals(
            Keys::class.nestedClasses.map { it.simpleName }.filterNot { it == "Companion" }.toSet(),
            Keys.allKeys.map { it::class }.map { it.simpleName }.toSet()
        )
    }

    @Test
    fun prefs() {
        val config = DefaultConfig.config.copy(
            theme = Theme.Bread,
            surfaceColorOpacity = 250,
            isClassicUI = true,
            isPostButtonAtTheBottom = false,
            isUserNameDefault = false,
            isSimpleEditorEnabled = true,
            isIncludeMyRenotes = false,
            isIncludeRenotedMyNotes = false,
            isIncludeLocalRenotes = false,
        )
        Assertions.assertNotNull(config.prefs()[Keys.ThemeType])
        config.prefs().forEach { (k, u) ->
            when (k) {
                Keys.BackgroundImage -> Assertions.assertEquals(
                    config.backgroundImagePath,
                    (u as PrefType.StrPref).value
                )
                Keys.ClassicUI -> Assertions.assertEquals(
                    config.isClassicUI,
                    (u as PrefType.BoolPref).value
                )
                Keys.IsPostButtonToBottom -> Assertions.assertEquals(
                    config.isPostButtonAtTheBottom,
                    (u as PrefType.BoolPref).value
                )
                Keys.IsSimpleEditorEnabled -> Assertions.assertEquals(
                    config.isSimpleEditorEnabled,
                    (u as PrefType.BoolPref).value
                )
                Keys.IsUserNameDefault -> Assertions.assertEquals(
                    config.isUserNameDefault,
                    (u as PrefType.BoolPref).value
                )
                Keys.NoteLimitHeight -> Assertions.assertEquals(
                    config.noteExpandedHeightSize,
                    (u as PrefType.IntPref).value
                )
                Keys.ReactionPickerType -> Assertions.assertEquals(
                    config.reactionPickerType.ordinal,
                    (u as PrefType.IntPref).value
                )
                Keys.ThemeType -> Assertions.assertEquals(
                    (config.theme.toInt()),
                    (u as PrefType.IntPref).value
                )
                Keys.IsIncludeLocalRenotes -> Assertions.assertEquals(
                    config.isIncludeLocalRenotes,
                    (u as PrefType.BoolPref).value
                )
                Keys.IsIncludeMyRenotes -> Assertions.assertEquals(
                    config.isIncludeMyRenotes,
                    (u as PrefType.BoolPref).value
                )
                Keys.IsIncludeRenotedMyNotes -> Assertions.assertEquals(
                    config.isIncludeRenotedMyNotes,
                    (u as PrefType.BoolPref).value
                )
                Keys.SurfaceColorOpacity -> Assertions.assertEquals(
                    config.surfaceColorOpacity,
                    (u as PrefType.IntPref).value
                )
                Keys.IsEnableTimelineScrollAnimation -> Assertions.assertEquals(
                    config.isEnableTimelineScrollAnimation,
                    (u as PrefType.BoolPref).value
                )
                Keys.IsCrashlyticsCollectionEnabled -> Assertions.assertEquals(
                    config.isCrashlyticsCollectionEnabled.isEnable,
                    (u as PrefType.BoolPref).value
                )
                Keys.IsConfirmedCrashlyticsCollection -> Assertions.assertEquals(
                    config.isCrashlyticsCollectionEnabled.isConfirmed,
                    (u as PrefType.BoolPref).value
                )
                Keys.IsAnalyticsCollectionEnabled -> Assertions.assertEquals(
                    config.isAnalyticsCollectionEnabled.isEnabled,
                    (u as PrefType.BoolPref).value
                )
                Keys.IsConfirmedAnalyticsCollection -> Assertions.assertEquals(
                    config.isCrashlyticsCollectionEnabled.isConfirmed,
                    (u as PrefType.BoolPref).value
                )
                Keys.IsConfirmedPostNotification -> Assertions.assertEquals(
                    config.isConfirmedPostNotification,
                    (u as PrefType.BoolPref).value
                )
                Keys.IsEnableInstanceTicker -> Assertions.assertEquals(
                    config.isEnableInstanceTicker,
                    (u as PrefType.BoolPref).value
                )
                Keys.IsDriveUsingGridView -> Assertions.assertEquals(
                    config.isDriveUsingGridView,
                    (u as PrefType.BoolPref).value
                )
                Keys.IsEnableNotificationSound -> Assertions.assertEquals(
                    config.isEnableNotificationSound,
                    (u as PrefType.BoolPref).value
                )
                Keys.IsStopNoteCaptureWhenBackground -> Assertions.assertEquals(
                    config.isStopNoteCaptureWhenBackground,
                    (u as PrefType.BoolPref).value
                )
                Keys.IsStopStreamingApiWhenBackground -> Assertions.assertEquals(
                    config.isStopStreamingApiWhenBackground,
                    (u as PrefType.BoolPref).value
                )
                Keys.IsEnableStreamingAPIAndNoteCapture -> Assertions.assertEquals(
                    config.isEnableStreamingAPIAndNoteCapture,
                    (u as PrefType.BoolPref).value
                )
                Keys.IsEnableNoteDivider -> Assertions.assertEquals(
                    config.isEnableNoteDivider,
                    (u as PrefType.BoolPref).value
                )
                Keys.IsVisibleInstanceUrlInToolbar -> Assertions.assertEquals(
                    config.isVisibleInstanceUrlInToolbar,
                    (u as PrefType.BoolPref).value
                )
                Keys.NoteContentFontSize -> Assertions.assertEquals(
                    config.noteContentFontSize,
                    (u as PrefType.FloatPref).value
                )
                Keys.NoteHeaderFontSize -> Assertions.assertEquals(
                    config.noteHeaderFontSize,
                    (u as PrefType.FloatPref).value
                )
                Keys.IsDisplayTimestampsAsAbsoluteDates -> Assertions.assertEquals(
                    config.isDisplayTimestampsAsAbsoluteDates,
                    (u as PrefType.BoolPref).value
                )
                Keys.NoteReactionCounterFontSize -> Assertions.assertEquals(
                    config.noteReactionCounterFontSize,
                    (u as PrefType.FloatPref).value
                )
                Keys.NoteCustomEmojiScaleSizeInText -> Assertions.assertEquals(
                    config.noteCustomEmojiScaleSizeInText,
                    (u as PrefType.FloatPref).value
                )
                Keys.EmojiPickerEmojiDisplaySize -> Assertions.assertEquals(
                    config.emojiPickerEmojiDisplaySize,
                    (u as PrefType.IntPref).value
                )
                Keys.AvatarIconShapeType -> Assertions.assertEquals(
                    config.avatarIconShapeType.value,
                    (u as PrefType.IntPref).value
                )
                Keys.MediaDisplayMode -> Assertions.assertEquals(
                    config.mediaDisplayMode.value,
                    (u as PrefType.IntPref).value
                )
                Keys.IsSafeSearchEnabled -> Assertions.assertEquals(
                    config.isEnableSafeSearch.isEnabled,
                    (u as PrefType.BoolPref).value
                )

                Keys.IsConfirmedSafeSearchEnabled -> Assertions.assertEquals(
                    config.isEnableSafeSearch.isConfirmed,
                    (u as PrefType.BoolPref).value
                )
                Keys.IsShowWarningDisplayingSensitiveMedia -> Assertions.assertEquals(
                    config.isShowWarningDisplayingSensitiveMedia,
                    (u as PrefType.BoolPref).value
                )
                Keys.IsEnableHapticFeedbackOnNewPost -> Assertions.assertEquals(
                    config.isEnableHapticFeedbackOnNewPost,
                    (u as PrefType.BoolPref).value
                )
            }
        }
    }

    @Test
    fun from() {
        Assertions.assertEquals(
            Theme.Bread, Config.from(
                mapOf(
                    Keys.ThemeType to PrefType.IntPref(Theme.Bread.toInt())
                )
            ).theme
        )

    }
}