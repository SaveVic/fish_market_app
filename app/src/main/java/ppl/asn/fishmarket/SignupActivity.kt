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
import com.google.gson.Gson
import com.google.gson.JsonParser
import ppl.asn.fishmarket.data.PreferencesData
import ppl.asn.fishmarket.response.GetUserResponse
import ppl.asn.fishmarket.response.RegisterResponse
import ppl.asn.fishmarket.response.ResponseClass
import java.util.regex.Pattern

class SignupActivity : AppCompatActivity() {

    private lateinit var nameText : EditText
    private lateinit var usernameText : EditText
    private lateinit var passwordText : EditText
    private lateinit var passwordConfirm : EditText
    private lateinit var emailText : EditText
    private lateinit var addressText : EditText
    private lateinit var phoneText : EditText
    private lateinit var signupButton : Button
    private lateinit var loginLink : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        nameText = findViewById(R.id.signup_name)
        usernameText = findViewById(R.id.signup_username)
        passwordText = findViewById(R.id.signup_password)
        passwordConfirm = findViewById(R.id.signup_password_confirm)
        emailText = findViewById(R.id.signup_email)
        addressText = findViewById(R.id.signup_address)
        phoneText = findViewById(R.id.signup_phone)
        signupButton = findViewById(R.id.signup_button)
        loginLink = findViewById(R.id.signup_link_login)

        initiateView()
    }

    private fun initiateView()
    {
        signupButton.setOnClickListener {
            if(!validateLogin()){
                Toast.makeText(this, "Invalid data", Toast.LENGTH_SHORT).show()
            }
            else {
                val name = nameText.text.toString()
                val username = usernameText.text.toString()
                val password =  passwordText.text.toString()
                val email = emailText.text.toString()
                val address = addressText.text.toString()
                val phone = phoneText.text.toString()
                requestRegister(name, username, password, email, address, phone)
            }
        }

        loginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validateLogin() : Boolean
    {
        var valid = true

        val name = nameText.text.toString()
        val username = usernameText.text.toString()
        val password =  passwordText.text.toString()
        val passConfirm = passwordConfirm.text.toString()
        val email = emailText.text.toString().trim()
        val address = addressText.text.toString()
        val phone = phoneText.text.toString()

        if(name.isEmpty() || name.length < 3){
            nameText.error = "At least 3 character"
            valid = false
        }
        else{
            nameText.error = null
        }

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

        if(passConfirm != password){
            passwordConfirm.error = "Confirmation password must same"
            valid = false
        }
        else{
            passwordConfirm.error = null
        }

        if(email.isEmpty() || !isValidEmail(email)){
            emailText.error = "Enter a valid email address"
            valid = false
        }
        else{
            emailText.error = null
        }

        if(address.isEmpty()){
            addressText.error = "Enter a valid address"
            valid = false
        }
        else{
            addressText.error = null
        }

        if(phone.isEmpty()){
            phoneText.error = "Enter a valid phone number"
            valid = false
        }
        else{
            phoneText.error = null
        }

        return valid
    }

    private fun isValidEmail(str : String) : Boolean
    {
        return true
//        return Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
//                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
//                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
//                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
//                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
//                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(str).matches()
    }

    private fun requestRegister(name : String, username : String, password : String, email : String, address : String, phone : String)
    {
        val queue = Volley.newRequestQueue(this)

        val dialog = ProgressDialog(this)
        dialog.setMessage("Sedang memverifikasi . . .")
        dialog.setCancelable(false)
        dialog.show()

        var urls = PreferencesData.getURL(baseContext)
        if(urls.isEmpty()) urls = "${ResponseClass.URL_MAIN}/${ResponseClass.URL_SIGN_UP}"
        else urls += "/${ResponseClass.URL_SIGN_UP}"
        Log.e("this url", urls)

        val request = object : StringRequest(
            Request.Method.POST, urls,
            Response.Listener { response ->
                Log.e("response", response)
                getRegisterResponse(response, dialog)
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
                return myHeader
            }
            override fun getParams(): Map<String, String>
            {
                val myParam = HashMap<String, String>()
                myParam["name"] = name
                myParam["username"] = username
                myParam["password"] = password
                myParam["email"] = email
                myParam["address"] = address
                myParam["phone"] = phone
                return myParam
            }
        }
        queue.add(request)
    }

    private fun getRegisterResponse(serverResponse : String, globalDialog : ProgressDialog)
    {
        val gson = Gson()

        try
        {
            val registerResponse = gson.fromJson(serverResponse, RegisterResponse.RegisterMain::class.java)
            if(registerResponse.status == ResponseClass.STATUS_SUCCESS) {
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                PreferencesData.setToken(baseContext, registerResponse.token ?: "")
                requestGetUser()
            }
            else Toast.makeText(this, "Failed, ${registerResponse.message ?: "unknown"}", Toast.LENGTH_SHORT).show()
            globalDialog.dismiss()
        }
        catch (e : Exception)
        {
            e.printStackTrace()
            Toast.makeText(this, "Gagal register. Ulangi lagi", Toast.LENGTH_SHORT).show()
            globalDialog.dismiss()
        }
    }

    private fun requestGetUser()
    {
        val token = PreferencesData.getToken(baseContext)
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
            globalDialog.dismiss()
        }
        catch (e : Exception)
        {
            e.printStackTrace()
            Toast.makeText(this, "Terjadi kesalahan. Ulangi lagi", Toast.LENGTH_SHORT).show()
            globalDialog.dismiss()
        }
    }
}