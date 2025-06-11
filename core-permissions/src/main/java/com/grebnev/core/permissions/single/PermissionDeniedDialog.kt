package com.grebnev.core.permissions.single

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.grebnev.core.permissions.R

@Composable
fun PermissionDeniedDialog(
    permissionText: String,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.access_denied)) },
        text = {
            Text(
                stringResource(R.string.change_permission_in_settings, permissionText),
            )
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