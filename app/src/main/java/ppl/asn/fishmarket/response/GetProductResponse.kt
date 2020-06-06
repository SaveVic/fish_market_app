package ppl.asn.fishmarket.response

class GetProductResponse {
    data class GetProductMain(
        val status : String,
        val message1 : String?,
        val message2 : Array<GetProductData>?
    )

    data class GetProductData(
        val _id : String,
        val name : String,
        val description : String,
        val store : String,
        val stock : Int,
        val price : Int,
        val __v : Int
    )
}