package com.grebnev.core.permissions.multiple

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grebnev.core.permissions.R
import com.grebnev.core.permissions.extensions.toReadableName

@Composable
internal fun MultiplePermissionsDeniedDialog(
    context: Context,
    permissionsText: Set<String>,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.access_required)) },
        text = {
            Column {
                Text(stringResource(R.string.functions_not_available))
                Spacer(Modifier.height(8.dp))
                permissionsText.forEach { permissionText ->
                    Text("• ${permissionText.toReadableName(context)}")
                }
            }
        },
        confirmButton = {
            Button(onClick = onOpenSettings) {
                Text(stringResource(R.string.settings))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
    )
}