package io.github.irishgreencitrus.sourgraves.config

import kotlinx.serialization.Serializable
import net.peanuuutz.tomlkt.TomlComment

enum class PaymentType {
    FLAT,
    PER_ITEM
}

@Serializable
data class EconomyConfig(
    @TomlComment("Whether to enable the economy features of this plugin (Requires VaultUnlocked to be installed)")
    var enable: Boolean = false,
    @TomlComment("The cost to recover one of your own graves")
    var graveRecoverCost: Double = 1.0,
    @TomlComment("The cost either per grave or per item stack in the grave")
    var graveRecoverPaymentType: PaymentType = PaymentType.FLAT,
    @TomlComment("The cost to rob someone else's grave")
    var graveRobCost: Double = 5.0,
    @TomlComment("The cost either per grave or per item stack in the grave")
    var graveRobPaymentType: PaymentType = PaymentType.FLAT,
    // TODO: implement insurance when I can be bothered.
)


