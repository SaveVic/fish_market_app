package ppl.asn.fishmarket.response

class LoginResponse {
    data class LoginMain(
        val status : String,
        val message : String?,
        val token : String?
    )
}