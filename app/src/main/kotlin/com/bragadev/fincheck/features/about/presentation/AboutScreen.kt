package com.bragadev.fincheck.features.about.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bragadev.fincheck.BuildConfig
import com.bragadev.fincheck.R
import com.bragadev.fincheck.ui.components.adaptiveContentWidth
import com.bragadev.fincheck.ui.theme.FinCheckTheme
import java.util.Calendar

/** Name of the company that develops the app; also used in the copyright line. */
private const val DEVELOPER = "BragaDev"

/**
 * "Sobre o app": what FinCheck is for, its version (from the build, so it never goes stale)
 * and who develops it.
 */
@Composable
fun AboutScreen(onBackClick: () -> Unit, onPrivacyPolicyClick: () -> Unit) {
    AboutContent(
        versionName = BuildConfig.VERSION_NAME,
        versionCode = BuildConfig.VERSION_CODE,
        year = Calendar.getInstance().get(Calendar.YEAR),
        onBackClick = onBackClick,
        onPrivacyPolicyClick = onPrivacyPolicyClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutContent(
    versionName: String,
    versionCode: Int,
    year: Int,
    onBackClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .adaptiveContentWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppIcon()
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.about_tagline),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Text(
                text = stringResource(R.string.about_description),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    InfoRow(
                        label = stringResource(R.string.about_version_label),
                        value = stringResource(R.string.about_version_value, versionName, versionCode),
                    )
                    HorizontalDivider()
                    InfoRow(label = stringResource(R.string.about_developer_label), value = DEVELOPER)
                }
            }

            TextButton(onClick = onPrivacyPolicyClick) {
                Text(stringResource(R.string.privacy_title))
            }

            Text(
                text = stringResource(R.string.about_developer_message, DEVELOPER),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.about_copyright, year, DEVELOPER),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** The launcher icon drawn from its own layers (an adaptive icon can't be painted directly). */
@Composable
private fun AppIcon() {
    Box(
        modifier = Modifier
            .padding(top = 8.dp)
            .size(96.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(colorResource(R.color.fincheck_navy)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            // Adaptive icons show the middle 72dp of 108dp, so the layer is 1.5× the visible tile.
            modifier = Modifier.size(144.dp),
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterStart),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterEnd),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutPreview() {
    FinCheckTheme {
        AboutContent(versionName = "1.0", versionCode = 1, year = 2026, onBackClick = {}, onPrivacyPolicyClick = {})
    }
}
