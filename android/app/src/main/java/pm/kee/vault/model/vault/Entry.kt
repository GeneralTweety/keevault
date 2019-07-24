package pm.kee.vault.model.vault

data class Entry (
    val uRLs : List<String>? = null,
    val BlockedURLs : List<String>? = null,
    val regExURLs : List<String>? = null,
    val regExBlockedURLs : List<String>? = null,
    val title : String? = null,
    val uniqueID : String,
    val iconImageData : String? = null,
    val hTTPRealm : String? = null,
    val formFieldList : List<FormField>? = null,
    val alwaysAutoFill : Boolean? = null,
    val neverAutoFill : Boolean? = null,
    val alwaysAutoSubmit : Boolean? = null,
    val neverAutoSubmit : Boolean? = null,
    val priority : Int? = null,
    val parent : Group? = null,
    val db : Db? = null,
    val matchAccuracy : Int? = null, // Result of matching so always null or "None" - worthless to us
    val matchAccuracyMethod: String? = null // Domain | Hostname (post-MVP) | Exact (awaiting Android API improvements in R+)
)
