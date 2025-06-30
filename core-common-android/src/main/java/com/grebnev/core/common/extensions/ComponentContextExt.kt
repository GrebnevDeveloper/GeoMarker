package com.grebnev.core.common.extensions

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

val ComponentContext.scope: CoroutineScope
    get() =
        CoroutineScope(
            Dispatchers.Main.immediate + SupervisorJob(),
        ).apply {
            lifecycle.doOnDestroy { cancel() }
        }