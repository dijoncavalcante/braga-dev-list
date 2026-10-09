package com.bragadev.fincheck.features.about.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bragadev.fincheck.R
import com.bragadev.fincheck.ui.components.adaptiveContentWidth
import com.bragadev.fincheck.ui.theme.FinCheckTheme

/**
 * Public page of this policy, the URL given to Google Play. Its text is docs/privacy-policy.html
 * in this repository, published by GitHub Pages; keep it in sync with the strings shown here.
 */
const val PRIVACY_POLICY_URL = "https://dijoncavalcante.github.io/braga-dev-list/privacy-policy.html"

/**
 * "Política de privacidade", bundled in the app so it is always available, even offline (the app
 * has no internet access). The same text is published online for the Play Store listing.
 */
@Composable
fun PrivacyPolicyScreen(onBackClick: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    PrivacyPolicyContent(
        onBackClick = onBackClick,
        onOpenOnlineClick = { uriHandler.openUri(PRIVACY_POLICY_URL) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrivacyPolicyContent(onBackClick: () -> Unit, onOpenOnlineClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.privacy_title)) },
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
        val titles = stringArrayResource(R.array.privacy_section_titles)
        val bodies = stringArrayResource(R.array.privacy_section_bodies)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .adaptiveContentWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.privacy_updated),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.privacy_summary),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(16.dp),
                )
            }
            Text(text = stringResource(R.string.privacy_intro), style = MaterialTheme.typography.bodyLarge)

            titles.zip(bodies).forEach { (title, body) ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                    Text(text = body, style = MaterialTheme.typography.bodyMedium)
                }
            }

            OutlinedButton(
                onClick = onOpenOnlineClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(stringResource(R.string.privacy_open_online))
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1600)
@Composable
private fun PrivacyPolicyPreview() {
    FinCheckTheme {
        PrivacyPolicyContent(onBackClick = {}, onOpenOnlineClick = {})
    }
}
