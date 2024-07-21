package com.onestep.backgroundmonitoringsample.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CollectionDataValue(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = Modifier
            .padding(8.dp)
            .then(modifier),
        color = MaterialTheme.colorScheme.onBackground,
    )
}