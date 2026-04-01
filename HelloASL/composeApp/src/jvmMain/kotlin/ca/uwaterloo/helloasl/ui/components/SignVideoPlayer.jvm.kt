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
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.scene.media.MediaView
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JPanel
import androidx.compose.foundation.background

private fun logFx(message: String) {
    println("[DesktopJavaFxPlayer] $message")
}

private class DesktopFxVideoState {
    var swingRoot: JPanel? = null
    var jfxPanel: JFXPanel? = null
    var mediaPlayer: MediaPlayer? = null
    var initializedScene: Boolean = false
    var currentUrl: String? = null
    var isReady: Boolean = false
    var isPlaying: Boolean = false
    var playButton: JButton? = null
    var pauseButton: JButton? = null
}

private fun updateButtons(state: DesktopFxVideoState) {
    val playButton = state.playButton ?: return
    val pauseButton = state.pauseButton ?: return

    playButton.isEnabled = state.isReady && !state.isPlaying
    pauseButton.isEnabled = state.isReady && state.isPlaying
}

@Composable
actual fun SignVideoPlayer(
    resourcePath: String,
    modifier: Modifier,
    dimmed: Boolean
) {
    var loadFailed by remember { mutableStateOf(false) }
    val state = remember { DesktopFxVideoState() }

    DisposableEffect(Unit) {
        Platform.setImplicitExit(false)

        onDispose {
            logFx("dispose whole SignVideoPlayer")
            Platform.runLater {
                state.mediaPlayer?.stop()
                state.mediaPlayer?.dispose()
                state.mediaPlayer = null
                state.isReady = false
                state.isPlaying = false
                updateButtons(state)

                state.jfxPanel?.scene = Scene(StackPane())
            }
        }
    }

    LaunchedEffect(resourcePath) {
        loadFailed = false
        state.currentUrl = resourcePath
        state.isReady = false
        state.isPlaying = false
        updateButtons(state)

        logFx("resourcePath changed -> $resourcePath")

        val panel = state.jfxPanel
        if (panel == null) {
            logFx("skip load because jfxPanel not ready yet")
            return@LaunchedEffect
        }

        Platform.runLater {
            try {
                if (!state.initializedScene) {
                    panel.scene = Scene(StackPane())
                    state.initializedScene = true
                    logFx("initialized empty scene")
                }

                state.mediaPlayer?.stop()
                state.mediaPlayer?.dispose()
                state.mediaPlayer = null

                val media = Media(resourcePath)
                val player = MediaPlayer(media)
                val mediaView = MediaView(player).apply {
                    isPreserveRatio = true
                    fitWidth = 720.0
                    fitHeight = 405.0
                }

                media.setOnError {
                    logFx("media error -> ${media.error?.message}")
                    loadFailed = true
                    state.isReady = false
                    state.isPlaying = false
                    updateButtons(state)
                }

                player.setOnError {
                    logFx("media player error -> ${player.error?.message}")
                    loadFailed = true
                    state.isReady = false
                    state.isPlaying = false
                    updateButtons(state)
                }

                player.setOnReady {
                    logFx("media ready -> $resourcePath")
                    state.isReady = true
                    state.isPlaying = false
                    updateButtons(state)
                }

                player.setOnPlaying {
                    logFx("playing -> $resourcePath")
                    state.isPlaying = true
                    updateButtons(state)
                }

                player.setOnPaused {
                    logFx("paused -> $resourcePath")
                    state.isPlaying = false
                    updateButtons(state)
                }

                player.setOnEndOfMedia {
                    logFx("ended -> $resourcePath")
                    state.isPlaying = false
                    player.pause()
                    player.seek(javafx.util.Duration.ZERO)
                    updateButtons(state)
                }

                panel.scene = Scene(StackPane(mediaView))
                state.mediaPlayer = player
                updateButtons(state)
            } catch (e: Exception) {
                logFx("failed to load media -> ${e.message}")
                loadFailed = true
                state.isReady = false
                state.isPlaying = false
                updateButtons(state)
            }
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (loadFailed) {
            Text(
                text = "Video unavailable",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            SwingPanel(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    val fxPanel = JFXPanel()

                    val playButton = JButton("Play")
                    val pauseButton = JButton("Pause")

                    state.playButton = playButton
                    state.pauseButton = pauseButton

                    playButton.addActionListener {
                        val player = state.mediaPlayer ?: return@addActionListener
                        Platform.runLater {
                            if (state.isReady && !state.isPlaying) {
                                logFx("play clicked -> ${state.currentUrl}")
                                player.play()
                            }
                        }
                    }

                    pauseButton.addActionListener {
                        val player = state.mediaPlayer ?: return@addActionListener
                        Platform.runLater {
                            if (state.isReady && state.isPlaying) {
                                logFx("pause clicked -> ${state.currentUrl}")
                                player.pause()
                            }
                        }
                    }

                    pauseButton.isEnabled = false
                    playButton.isEnabled = false

                    val controls = JPanel(FlowLayout(FlowLayout.CENTER)).apply {
                        add(playButton)
                        add(pauseButton)
                    }

                    JPanel(BorderLayout()).apply {
                        state.swingRoot = this
                        state.jfxPanel = fxPanel
                        add(fxPanel, BorderLayout.CENTER)
                        add(controls, BorderLayout.SOUTH)
                    }
                },
                update = {
                    updateButtons(state)
                }
            )

            if (dimmed) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f))
                )
            }
        }
    }
}