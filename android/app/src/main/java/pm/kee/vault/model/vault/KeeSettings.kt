package pm.kee.vault.model.vault

data class KeeSettings (
	val autoFillForms : Boolean? = null,
	val autoFillFormsWithMultipleMatches : Boolean? = null,
	val autoSubmitForms : Boolean? = null,
	val autoSubmitMatchedForms : Boolean? = null,
	val listAllOpenDBs : Boolean? = null,
	val logLevel : Int? = null,
	val mruGroup : Map<String, String>? = null,
	val notifyWhenEntryUpdated : Boolean? = null,
	val overWriteFieldsAutomatically : Boolean? = null,
	val rememberMRUDB : Boolean? = null,
	val rememberMRUGroup : Boolean? = null,
	val saveFavicons : Boolean? = null,
	val searchAllOpenDBs : Boolean? = null,
	val siteConfig : SiteConfigIndex? = null,
	val autoSubmitNetworkAuthWithSingleMatch : Boolean? = null,
	val notificationCountGeneric : Int? = null,
	val notificationCountSavePassword : Int? = null,
	val currentSearchTermTimeout : Int? = null,
	val animateWhenOfferingSave : Boolean? = null,
	val manualSubmitOverrideProhibited : Boolean? = null
)