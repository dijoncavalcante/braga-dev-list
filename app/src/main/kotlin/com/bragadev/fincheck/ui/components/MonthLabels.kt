package com.bragadev.fincheck.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.bragadev.fincheck.R
import com.bragadev.fincheck.core.domain.model.YearMonth

/** "Outubro 2026". */
@Composable
@ReadOnlyComposable
fun YearMonth.label(): String = stringResource(R.string.month_label, stringArrayResource(R.array.month_names)[month - 1], year)

/** "out/2026", for periods such as "out/2026 → mar/2027". */
@Composable
@ReadOnlyComposable
fun YearMonth.shortLabel(): String =
    stringResource(R.string.month_short_label, stringArrayResource(R.array.month_short_names)[month - 1], year)

/** "Out", for the months strip; the year goes below it. */
@Composable
@ReadOnlyComposable
fun YearMonth.monthAbbreviation(): String =
    stringArrayResource(R.array.month_short_names)[month - 1]
        .replaceFirstChar { it.uppercase() }
