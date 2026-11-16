package com.hopa.engine.execution

import com.hopa.contracts.wire.Action

/** An action plus the path the selector chose for it. */
data class PlannedAction(
    val action: Action,
    val tier: PathTier,
)
