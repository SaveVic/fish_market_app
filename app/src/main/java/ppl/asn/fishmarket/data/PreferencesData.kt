package ppl.asn.fishmarket.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class PreferencesData
{
    companion object{
        private const val KEY_ALTERNATIVE_URL = "url_main"

        private const val KEY_TOKEN = "token"
        private const val KEY_USERNAME = "username"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_ADDRESS = "address"
        private const val KEY_PHONE = "phone"

        private const val KEY_ROLE = "role"
        private const val KEY_MONEY = "money"

        private fun getSharedPreferences(context : Context) : SharedPreferences
        {
            return PreferenceManager.getDefaultSharedPreferences(context)
        }

        fun setURL(context: Context, url: String)
        {
            val editor = getSharedPreferences(
                context
            ).edit()
            editor.putString(KEY_ALTERNATIVE_URL, url).apply()
        }

        fun getURL(context: Context) : String
        {
            return getSharedPreferences(context).getString(
                KEY_ALTERNATIVE_URL, "") ?: ""
        }

        fun setToken(context: Context, token: String)
        {
            val editor = getSharedPreferences(
                context
            ).edit()
            editor.putString(KEY_TOKEN, token).apply()
        }

        fun getToken(context: Context) : String
        {
            return getSharedPreferences(context).getString(
                KEY_TOKEN, "") ?: ""
        }

        fun setUsername(context: Context, username: String)
        {
            val editor = getSharedPreferences(
                context
            ).edit()
            editor.putString(KEY_USERNAME, username).apply()
        }

        fun getUsername(context: Context) : String
        {
            return getSharedPreferences(context).getString(
                KEY_USERNAME, "") ?: ""
        }

        fun setName(context: Context, name: String)
        {
            val editor = getSharedPreferences(
                context
            ).edit()
            editor.putString(KEY_NAME, name).apply()
        }

        fun getName(context: Context) : String
        {
            return getSharedPreferences(context).getString(
                KEY_NAME, "") ?: ""
        }

        fun setEmail(context: Context, email: String)
        {
            val editor = getSharedPreferences(
                context
            ).edit()
            editor.putString(KEY_EMAIL, email).apply()
        }

        fun getEmail(context: Context) : String
        {
            return getSharedPreferences(context).getString(
                KEY_EMAIL, "") ?: ""
        }

        fun setAddress(context: Context, address: String)
        {
            val editor = getSharedPreferences(
                context
            ).edit()
            editor.putString(KEY_ADDRESS, address).apply()
        }

        fun getAddress(context: Context) : String
        {
            return getSharedPreferences(context).getString(
                KEY_ADDRESS, "") ?: ""
        }

        fun setPhone(context: Context, phone: String)
        {
            val editor = getSharedPreferences(
                context
            ).edit()
            editor.putString(KEY_PHONE, phone).apply()
        }

        fun getPhone(context: Context) : String
        {
            return getSharedPreferences(context).getString(
                KEY_PHONE, "") ?: ""
        }

        fun setRole(context: Context, role: String)
        {
            val editor = getSharedPreferences(
                context
            ).edit()
            editor.putString(KEY_ROLE, role).apply()
        }

        fun getRole(context: Context) : String
        {
            return getSharedPreferences(context).getString(
                KEY_ROLE, "") ?: ""
        }

        fun setMoney(context: Context, money: Int)
        {
            val editor = getSharedPreferences(
                context
            ).edit()
            editor.putInt(KEY_MONEY, money).apply()
        }

        fun getMoney(context: Context) : Int
        {
            return getSharedPreferences(context)
                .getInt(KEY_MONEY, 0)
        }
    }
}

