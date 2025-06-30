package com.grebnev.core.common.delegates

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class StateFlowDelegate<State : Any>(
    private val scope: CoroutineScope,
    private val stateFlow: StateFlow<State>,
) : ReadOnlyProperty<Any, Value<State>> {
    private val model = MutableValue(stateFlow.value)

    init {
        scope.launch {
            stateFlow.collect { newState ->
                model.value = newState
            }
        }
    }

    override fun getValue(
        thisRef: Any,
        property: KProperty<*>,
    ): Value<State> = model
}