package com.bragadev.fincheck.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import com.bragadev.fincheck.R
import com.bragadev.fincheck.core.domain.model.MAX_DESCRIPTION_LENGTH

/**
 * Optional "Descrição" of the item and bill forms. As Material Design asks of a field with a
 * limit, it shows a character counter ("31/200") under it, and typing stops at the limit.
 */
@Composable
fun DescriptionField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { text -> onValueChange(text.take(MAX_DESCRIPTION_LENGTH)) },
        label = { Text(stringResource(R.string.description_label)) },
        supportingText = {
            Text(
                text = stringResource(R.string.description_counter, value.length, MAX_DESCRIPTION_LENGTH),
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        minLines = 2,
        maxLines = 4,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        modifier = modifier.fillMaxWidth(),
    )
}
