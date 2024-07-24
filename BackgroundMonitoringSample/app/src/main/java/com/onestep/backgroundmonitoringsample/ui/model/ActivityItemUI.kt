package com.onestep.backgroundmonitoringsample.ui.model

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.onestep.android.core.external.models.ParamName

@Composable
fun ActivityItemUI(
    modifier: Modifier = Modifier,
    activityItem: ActivityItem,
) {
    var expanded by remember { mutableStateOf(false) }
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .then(modifier),
        onClick = { expanded = !expanded },
    ) {
        Column {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = activityItem.title,
                    modifier = Modifier
                        .padding(8.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                AnimatedIcon(expanded = expanded)
            }
            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(8.dp)) {
                    if (activityItem.params.getOrDefault(ParamName.WALKING_WALK_SCORE, 0f) == 0f) {
                        Text(
                            modifier = Modifier.padding(4.dp),
                            text = "Walk Score: unable to analyze",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    activityItem.params.forEach {
                        Text(
                            text = "${it.key.columnName}: ${it.value}",
                            modifier = Modifier.padding(4.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedIcon(expanded: Boolean) {
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "")
    val iconColor = LocalContentColor.current
    Icon(
        imageVector = Icons.Default.KeyboardArrowDown,
        contentDescription = null,
        modifier = Modifier
            .padding(start = 8.dp)
            .rotate(rotation),
        tint = iconColor,
    )
}
