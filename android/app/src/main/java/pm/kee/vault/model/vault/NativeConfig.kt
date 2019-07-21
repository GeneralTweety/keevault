package pm.kee.vault.model.vault

data class NativeConfig (
	val expiry : Int,
	val requireFullKey : Boolean? = null
)