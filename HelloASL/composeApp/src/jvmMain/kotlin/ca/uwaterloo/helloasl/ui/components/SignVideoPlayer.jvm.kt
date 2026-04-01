package ca.uwaterloo.helloasl.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.media.Media
import javafx.scene.media.MediaException
import javafx.scene.media.MediaPlayer
import javafx.scene.media.MediaView
import javafx.util.Duration
import java.awt.EventQueue
import java.io.File
import java.util.concurrent.atomic.AtomicLong

private object DesktopVideoDebugIds {
    val composableId = AtomicLong(0L)
    val playerId = AtomicLong(0L)
}

private fun logDesktopVideo(message: String) {
    println("[DesktopSignVideoPlayer] $message")
}

@Composable
actual fun SignVideoPlayer(
    resourcePath: String,
    modifier: Modifier
) {
    val ownerId = remember(resourcePath) {
        DesktopVideoDebugIds.composableId.incrementAndGet()
    }

    var isReady by remember(resourcePath) { mutableStateOf(false) }
    var isPlaying by remember(resourcePath) { mutableStateOf(false) }
    var loadFailed by remember(resourcePath) { mutableStateOf(false) }

    val jfxPanel = remember(resourcePath) { JFXPanel() }
    val mediaView = remember(resourcePath) {
        MediaView().apply {
            isPreserveRatio = true
        }
    }

    DisposableEffect(resourcePath) {
        logDesktopVideo("Composable start owner=$ownerId resourcePath=$resourcePath")

        var mediaPlayer: MediaPlayer? = null

        Platform.runLater {
            val playButton = Button("Play")
            val pauseButton = Button("Pause")

            fun updateButtons() {
                val status = mediaPlayer?.status
                val ready = mediaPlayer != null &&
                        status != null &&
                        status != MediaPlayer.Status.UNKNOWN &&
                        status != MediaPlayer.Status.DISPOSED
                val playing = ready && status == MediaPlayer.Status.PLAYING

                playButton.isDisable = !ready || playing
                pauseButton.isDisable = !ready || !playing

                logDesktopVideo(
                    "updateButtons owner=$ownerId status=$status ready=$ready playing=$playing " +
                            "playDisabled=${playButton.isDisable} pauseDisabled=${pauseButton.isDisable}"
                )
            }

            playButton.setOnAction {
                logDesktopVideo("Play clicked owner=$ownerId status=${mediaPlayer?.status}")
                mediaPlayer?.play()
                updateButtons()
            }

            pauseButton.setOnAction {
                logDesktopVideo("Pause clicked owner=$ownerId status=${mediaPlayer?.status}")
                mediaPlayer?.pause()
                updateButtons()
            }

            val controls = HBox(12.0, playButton, pauseButton).apply {
                alignment = Pos.CENTER
                style = "-fx-padding: 8 0 0 0;"
            }

            val videoContainer = StackPane(mediaView).apply {
                alignment = Pos.CENTER
                style = "-fx-background-color: transparent;"
                minHeight = 180.0
            }

            mediaView.fitWidthProperty().bind(videoContainer.widthProperty())
            mediaView.fitHeightProperty().bind(videoContainer.heightProperty())

            val root = VBox(12.0, videoContainer, controls).apply {
                alignment = Pos.CENTER
                style = "-fx-background-color: transparent;"
                VBox.setVgrow(videoContainer, Priority.ALWAYS)
            }

            jfxPanel.scene = Scene(root)

            try {
                val mediaUri = when {
                    resourcePath.startsWith("http://") ||
                            resourcePath.startsWith("https://") ||
                            resourcePath.startsWith("file:/") -> resourcePath
                    else -> File(resourcePath).toURI().toString()
                }

                val playerId = DesktopVideoDebugIds.playerId.incrementAndGet()
                logDesktopVideo("creating player playerId=$playerId owner=$ownerId uri=$mediaUri")

                mediaPlayer = MediaPlayer(Media(mediaUri)).apply {
                    cycleCount = 1
                    isAutoPlay = false
                }

                mediaView.mediaPlayer = mediaPlayer

                mediaPlayer?.setOnReady {
                    logDesktopVideo("onReady playerId=$playerId owner=$ownerId status=${mediaPlayer?.status}")
                    updateButtons()
                    EventQueue.invokeLater {
                        isReady = true
                        isPlaying = false
                        loadFailed = false
                    }
                }

                mediaPlayer?.setOnPlaying {
                    logDesktopVideo("onPlaying playerId=$playerId owner=$ownerId status=${mediaPlayer?.status}")
                    updateButtons()
                    EventQueue.invokeLater {
                        isReady = true
                        isPlaying = true
                        loadFailed = false
                    }
                }

                mediaPlayer?.setOnPaused {
                    logDesktopVideo("onPaused playerId=$playerId owner=$ownerId status=${mediaPlayer?.status}")
                    updateButtons()
                    EventQueue.invokeLater {
                        isReady = true
                        isPlaying = false
                        loadFailed = false
                    }
                }

                mediaPlayer?.setOnStopped {
                    logDesktopVideo("onStopped playerId=$playerId owner=$ownerId status=${mediaPlayer?.status}")
                    updateButtons()
                    EventQueue.invokeLater {
                        isReady = true
                        isPlaying = false
                        loadFailed = false
                    }
                }

                mediaPlayer?.setOnEndOfMedia {
                    logDesktopVideo("onEndOfMedia playerId=$playerId owner=$ownerId status=${mediaPlayer?.status}")
                    mediaPlayer?.pause()
                    mediaPlayer?.seek(Duration.ZERO)
                    updateButtons()
                    EventQueue.invokeLater {
                        isReady = true
                        isPlaying = false
                        loadFailed = false
                    }
                }

                mediaPlayer?.setOnError {
                    val errorMessage = mediaPlayer?.error?.message
                    logDesktopVideo("onError playerId=$playerId owner=$ownerId error=$errorMessage")
                    updateButtons()
                    EventQueue.invokeLater {
                        isReady = false
                        isPlaying = false
                        loadFailed = true
                    }
                }

                updateButtons()
            } catch (e: MediaException) {
                logDesktopVideo("MediaException owner=$ownerId error=${e.message}")
                updateButtons()
                EventQueue.invokeLater {
                    isReady = false
                    isPlaying = false
                    loadFailed = true
                }
            } catch (e: Exception) {
                logDesktopVideo("Exception owner=$ownerId error=${e.message}")
                updateButtons()
                EventQueue.invokeLater {
                    isReady = false
                    isPlaying = false
                    loadFailed = true
                }
            }
        }

        onDispose {
            logDesktopVideo(
                "Composable dispose owner=$ownerId resourcePath=$resourcePath " +
                        "isReady=$isReady isPlaying=$isPlaying loadFailed=$loadFailed"
            )

            Platform.runLater {
                runCatching {
                    mediaPlayer?.stop()
                    mediaPlayer?.dispose()
                }.onFailure {
                    logDesktopVideo("dispose failed owner=$ownerId error=${it.message}")
                }

                mediaView.mediaPlayer = null
                jfxPanel.scene = null
                mediaPlayer = null
            }
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (!loadFailed) {
            SwingPanel(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    logDesktopVideo("SwingPanel factory owner=$ownerId resourcePath=$resourcePath")
                    jfxPanel
                }
            )
        }

        when {
            loadFailed -> {
                Text("Video unavailable", style = MaterialTheme.typography.bodyMedium)
            }
            !isReady -> {
                Text("Loading video…", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}