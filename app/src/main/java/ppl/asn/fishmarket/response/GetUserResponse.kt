package ppl.asn.fishmarket.response

class GetUserResponse {
    data class GetUserMain(
        val status : String,
        val message1 : String?,
        val message2 : GetUserData?
    )

    data class GetUserData(
        val store : Array<String>,
        val role : String,
        val money : Int,
        val id : String,
        val name : String,
        val username : String,
        val password : String,
        val email : String,
        val address : String,
        val phone : String,
        val __v : Int
    )
}