package ppl.asn.fishmarket

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.JsonParser
import ppl.asn.fishmarket.data.PreferencesData
import ppl.asn.fishmarket.response.GetUserResponse
import ppl.asn.fishmarket.response.LoginResponse
import ppl.asn.fishmarket.response.ResponseClass


class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"

    private lateinit var usernameText : EditText
    private lateinit var passwordText : EditText
    private lateinit var loginButton : Button
    private lateinit var signupLink : TextView

    private lateinit var database : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        PreferencesData.setToken(baseContext, "")
        database = FirebaseDatabase.getInstance().reference.child("data")
        checkUpdateURL()

        usernameText = findViewById(R.id.login_username)
        passwordText = findViewById(R.id.login_password)
        loginButton = findViewById(R.id.login_button)
        signupLink = findViewById(R.id.login_link_signup)

        initiateView()
    }

    private fun checkUpdateURL()
    {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Preparing...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        database.addListenerForSingleValueEvent(object : ValueEventListener
        {
            override fun onCancelled(p0: DatabaseError)
            {
                progressDialog.dismiss()
                Toast.makeText(this@LoginActivity, "Failed to prepare", Toast.LENGTH_SHORT).show()
            }
            override fun onDataChange(dataSnapshot: DataSnapshot)
            {
                progressDialog.dismiss()
                try{
                    for(item in dataSnapshot.children){
                        if((item.key ?: "") != "url_main") continue
                        val urlNew = item.value as String
                        Log.e("url from firebase", urlNew)
                        PreferencesData.setURL(this@LoginActivity, urlNew)
                        break
                    }
                    Toast.makeText(this@LoginActivity, "Preparation done", Toast.LENGTH_SHORT).show()
                }
                catch(e : Exception){
                    e.printStackTrace()
                    Toast.makeText(this@LoginActivity, "Exception", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun initiateView()
    {
        loginButton.setOnClickListener {
            if(!validateLogin()){
                Toast.makeText(this, "Username atau password tidak valid", Toast.LENGTH_SHORT).show()
            }
            else{
                val username = usernameText.text.toString()
                val password =  passwordText.text.toString()
                requestLogin(username, password)
            }
        }

        signupLink.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validateLogin() : Boolean
    {
        var valid = true

        val username = usernameText.text.toString()
        val password =  passwordText.text.toString()

        if(username.isEmpty() || username.contains(" ")){
            usernameText.error = "Enter a valid username without space"
            valid = false
        }
        else{
            usernameText.error = null
        }

        if(password.isEmpty() || password.length < 4){
            passwordText.error = "Minimum length 4 alphanumeric character"
            valid = false
        }
        else{
            passwordText.error = null
        }

        return valid
    }

    private fun requestLogin(username : String, password : String)
    {
        val queue = Volley.newRequestQueue(this)

        val dialog = ProgressDialog(this)
        dialog.setMessage("Sedang memverifikasi . . .")
        dialog.setCancelable(false)
        dialog.show()

        var urls = PreferencesData.getURL(baseContext)
        if(urls.isEmpty()) urls = "${ResponseClass.URL_MAIN}/${ResponseClass.URL_LOGIN}"
        else urls += "/${ResponseClass.URL_LOGIN}"
        Log.e("this url", urls)

        val request = object : StringRequest(
            Request.Method.POST, urls,
            Response.Listener { response ->
                Log.e("response", response)
                saveAuthData(response, dialog)
            },
            Response.ErrorListener { error ->
                dialog.dismiss()
                ResponseClass.showErrorResponse(this, error)
            })
        {
            override fun getHeaders(): MutableMap<String, String>
            {
                val myHeader = HashMap<String, String>()
                myHeader["Accept"] = "application/json"
                return myHeader
            }
            override fun getParams(): Map<String, String>
            {
                val myParam = HashMap<String, String>()
                myParam["username"] = username
                myParam["password"] = password
                return myParam
            }
        }
        queue.add(request)
    }

    private fun saveAuthData(serverResponse : String, globalDialog : ProgressDialog)
    {
        val gson = Gson()

        try
        {
            val loginResponse = gson.fromJson(serverResponse, LoginResponse.LoginMain::class.java)
            if(loginResponse.status == ResponseClass.STATUS_SUCCESS) {
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                Log.e("Token", loginResponse.token ?: "empty")
                PreferencesData.setToken(baseContext, loginResponse.token ?: "")
                requestGetUser()
            }
            else Toast.makeText(this, "Failed, ${loginResponse.message ?: "unknown"}", Toast.LENGTH_SHORT).show()
            globalDialog.dismiss()
        }
        catch (e : Exception)
        {
            e.printStackTrace()
            Toast.makeText(this, "Gagal login. Ulangi lagi", Toast.LENGTH_SHORT).show()
            globalDialog.dismiss()
        }
    }

    private fun requestGetUser()
    {
        val token = PreferencesData.getToken(baseContext)
        Log.e("token", token)
        val queue = Volley.newRequestQueue(this)

        val dialog = ProgressDialog(this)
        dialog.setMessage("Sedang mempersiapkan . . .")
        dialog.setCancelable(false)
        dialog.show()

        var urls = PreferencesData.getURL(baseContext)
        if(urls.isEmpty()) urls = "${ResponseClass.URL_MAIN}/${ResponseClass.URL_GET_USER}"
        else urls += "/${ResponseClass.URL_GET_USER}"
        Log.e("this url", urls)

        val request = object : StringRequest(
            Request.Method.GET, urls,
            Response.Listener { response ->
                Log.e("response", response)
                saveDataUser(response, dialog)
                dialog.dismiss()
            },
            Response.ErrorListener { error ->
                dialog.dismiss()
                ResponseClass.showErrorResponse(this, error)
            })
        {
            override fun getHeaders(): MutableMap<String, String>
            {
                val myHeader = HashMap<String, String>()
                myHeader["Accept"] = "application/json"
                myHeader["token"] = token
                return myHeader
            }
        }
        queue.add(request)
    }

    private fun saveDataUser(serverResponse: String, globalDialog : ProgressDialog)
    {
        val gson = Gson()

        try
        {
            globalDialog.dismiss()
            val userElemen = JsonParser().parse(serverResponse)
            val userObject = userElemen.asJsonObject
            if(userObject.get("status").asString == ResponseClass.STATUS_SUCCESS){
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                val data = gson.fromJson(userObject.get("message"), GetUserResponse.GetUserData::class.java)
                PreferencesData.setRole(baseContext, data.role)
                PreferencesData.setMoney(baseContext, data.money)
                PreferencesData.setName(baseContext, data.name)
                PreferencesData.setUsername(baseContext, data.username)
                PreferencesData.setEmail(baseContext, data.email)
                PreferencesData.setAddress(baseContext, data.address)
                PreferencesData.setPhone(baseContext, data.phone)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            else{
                Toast.makeText(this, "Failed, ${userObject.get("message") ?: "unknown"}", Toast.LENGTH_SHORT).show()
                PreferencesData.setToken(baseContext, "")
            }
        }
        catch (e : Exception)
        {
            e.printStackTrace()
            Toast.makeText(this, "Terjadi kesalahan. Ulangi lagi", Toast.LENGTH_SHORT).show()
            globalDialog.dismiss()
        }
    }
}
