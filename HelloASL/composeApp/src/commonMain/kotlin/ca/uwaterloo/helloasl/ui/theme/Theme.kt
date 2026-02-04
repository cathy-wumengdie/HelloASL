package ca.uwaterloo.helloasl.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = HelloASLOrange,
    onPrimary = HelloASLOnLight,
    primaryContainer = HelloASLOrangeContainer,
    onPrimaryContainer = HelloASLOnDark,

    secondary = HelloASLBlue,
    onSecondary = HelloASLOnLight,
    secondaryContainer = HelloASLBlueContainer,
    onSecondaryContainer = HelloASLOnDark,

    tertiary = HelloASLTeal,
    onTertiary = HelloASLOnDark,
    tertiaryContainer = HelloASLTealContainer,
    onTertiaryContainer = HelloASLOnDark,

    background = HelloASLBackground,
    onBackground = HelloASLOnDark,

    surface = HelloASLYellow,
    onSurface = HelloASLOnDark,
    surfaceVariant = HelloASLYellowContainer,
    onSurfaceVariant = HelloASLOnDark,

    surfaceContainer = HelloASLSurfaceVariant,

    outline = HelloASLOutline
)


@Composable
fun HelloASLTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}



