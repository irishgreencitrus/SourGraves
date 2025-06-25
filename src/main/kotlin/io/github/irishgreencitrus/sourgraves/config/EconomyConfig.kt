package io.github.irishgreencitrus.sourgraves.config

import kotlinx.serialization.Serializable
import net.peanuuutz.tomlkt.TomlComment

enum class PaymentType {
    FLAT,
    PER_ITEM
}

@Serializable
data class EconomyConfig(
    @TomlComment("Whether to enable the economy features of this plugin [Requires VaultUnlocked]")
    var enable: Boolean = false,
    @TomlComment("The cost to recover one of your own graves")
    var graveRecoverCost: Double = 1.0,
    @TomlComment("The cost either per grave or per item stack in the grave")
    var graveRecoverPaymentType: PaymentType = PaymentType.FLAT,
    @TomlComment("The cost to rob someone else's grave")
    var graveRobCost: Double = 5.0,
    @TomlComment("The cost either per grave or per item stack in the grave")
    var graveRobPaymentType: PaymentType = PaymentType.FLAT,
    @TomlComment("Whether to enable the insurance feature. If a player does not have grave insurance, they will not drop a grave.")
    var enableInsurance: Boolean = true,
    @TomlComment("The cost of grave insurance. Only active if insurance is enabled.")
    var insuranceCost: Double = 20.0
)


