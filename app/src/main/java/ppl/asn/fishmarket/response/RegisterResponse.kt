package ppl.asn.fishmarket.response

class RegisterResponse {
    data class RegisterMain(
        val status : String,
        val message : String?,
        val token : String?
    )
}