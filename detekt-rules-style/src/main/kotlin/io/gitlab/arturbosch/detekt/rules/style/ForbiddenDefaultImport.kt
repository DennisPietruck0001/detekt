package io.gitlab.arturbosch.detekt.rules.style

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.internal.RequiresTypeResolution

@RequiresTypeResolution
class ForbiddenDefaultImport(config: Config = Config.empty) : Rule(config) {
    override val issue: Issue
        get() = TODO("Not yet implemented")
}
