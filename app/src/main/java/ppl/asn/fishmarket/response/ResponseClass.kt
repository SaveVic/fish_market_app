package ppl.asn.fishmarket.response

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.*

class ResponseClass {

    companion object
    {
        const val STATUS_SUCCESS = "success"
        const val STATUS_FAILED = "failed"

        const val URL_MAIN = "localhost:8080"
        const val URL_LOGIN = "auth/login"
        const val URL_SIGN_UP = "auth/register"
        const val URL_GET_USER = "user"
        const val URL_GET_PRODUCT = "product"
        const val URL_TRANSACTION_BUY = "transaction/buy"

        fun showErrorResponse(context : Context, loginError : VolleyError)
        {
            val response = loginError.javaClass
            val networkResponse = loginError.networkResponse
            var result = "null"
            if(networkResponse != null)
                result = String(networkResponse.data)
            Log.e("NetworkResponseResult", result)
            Log.e("LoginErrorString", loginError.toString())
            val errorMessage = when(response)
            {
                TimeoutError::class.java -> "Waktu habis"
                NoConnectionError::class.java -> "Tidak ada koneksi"
                AuthFailureError::class.java -> "Username atau password salah"
                ServerError::class.java -> "Kesalahan server"
                NetworkError::class.java -> "Tidak ada koneksi internet"
                ParseError::class.java -> "Kesalahan parsing"
                else -> "Terjadi kesalahan"
            }
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }

    }
}