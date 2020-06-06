package ppl.asn.fishmarket.ui.checkout

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.JsonParser
import org.json.JSONArray
import org.json.JSONObject
import ppl.asn.fishmarket.MainActivity
import ppl.asn.fishmarket.R
import ppl.asn.fishmarket.data.PreferencesData
import ppl.asn.fishmarket.response.GetUserResponse
import ppl.asn.fishmarket.response.ResponseClass
import ppl.asn.fishmarket.ui.home.ItemDetailClass

class CheckoutFragment : Fragment(){

    private lateinit var subtotalText : TextView
    private lateinit var taxText : TextView
    private lateinit var taxRow : TableRow
    private lateinit var discountText : TextView
    private lateinit var discountRow : TableRow
    private lateinit var deliveryText : TextView
    private lateinit var totalText : TextView
    private lateinit var saldoText : TextView
    private lateinit var warningText : TextView
    private lateinit var payButton : Button

    private var subtotal = 0
    private var tax = 0
    private var discount = 0
    private var delivery = 5
    private var saldo = 0

    private val bundleKey = "datas"
    private var datas : ArrayList<ItemDetailClass>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val root = inflater.inflate(R.layout.fragment_checkout, container, false)
        subtotalText = root.findViewById(R.id.checkout_subtotal)
        taxText = root.findViewById(R.id.checkout_tax)
        taxRow = root.findViewById(R.id.row_tax)
        discountText = root.findViewById(R.id.checkout_diskon)
        discountRow = root.findViewById(R.id.row_discount)
        deliveryText = root.findViewById(R.id.checkout_delivery)
        totalText = root.findViewById(R.id.checkout_total)
        saldoText = root.findViewById(R.id.checkout_saldo)
        warningText = root.findViewById(R.id.checkout_warning)
        payButton = root.findViewById(R.id.checkout_pay)

        saldo = PreferencesData.getMoney(requireContext())
        datas = arguments?.getParcelableArrayList<ItemDetailClass>(bundleKey)
        subtotal = getTotal()

        initiateView()
        return root
    }

    private fun getTotal() : Int
    {
        var total = 0
        for(item in (datas ?: arrayListOf())){
            total += item.price * item.qty
        }
        return total
    }

    private fun initiateView()
    {
        var str = "Rp $subtotal"
        subtotalText.text = str
        if(tax > 0){
            str = "${subtotal * tax / 100}"
            taxRow.visibility = View.VISIBLE
            taxText.text = str
            tax = subtotal * tax / 100
        }
        else taxRow.visibility = View.GONE
        if(discount > 0){
            str = "-${subtotal * discount / 100}"
            discountRow.visibility = View.VISIBLE
            discountText.text = str
            discount = subtotal * discount / 100
        }
        else discountRow.visibility = View.GONE
        str = "$delivery"
        deliveryText.text = str

        val total = (subtotal + tax - discount + delivery)
        str = "Rp $total"
        totalText.text = str
        str = "Rp $saldo"
        saldoText.text = str

        if(total <= saldo){
            warningText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_light))
            warningText.text = "Saldo mencukupi"
            payButton.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark))
            payButton.isClickable = true
            payButton.isEnabled = true
            payButton.setOnClickListener {
                actionPayment()
            }
        }
        else{
            warningText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
            warningText.text = "Saldo tidak mencukupi"
            payButton.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light))
            payButton.isClickable = false
            payButton.isEnabled = false
        }
    }

    private fun actionPayment()
    {
        val jsonArray = JSONArray()
        val jsonObject = JSONObject()

        val listItem = JSONArray()
        val listQty = JSONArray()
        for(item in datas ?: arrayListOf()){
            listItem.put(item.itemId)
            listQty.put(item.qty)
        }
        try{
            jsonObject.put("productID", listItem)
            jsonObject.put("qty", listQty)
            jsonArray.put(jsonObject)
        }
        catch (e : Exception){ }

        requestTransactionBuy(jsonObject)
    }

    private fun requestTransactionBuy(jsonObject : JSONObject)
    {
        val dialog = ProgressDialog(requireContext())
        dialog.setMessage("Sedang mempersiapkan . . .")
        dialog.setCancelable(false)
        dialog.show()

        val requestQueue = Volley.newRequestQueue(requireContext())

        val token = PreferencesData.getToken(requireContext())
        var urls = PreferencesData.getURL(requireContext())
        if(urls.isEmpty()) urls = "${ResponseClass.URL_MAIN}/${ResponseClass.URL_TRANSACTION_BUY}"
        else urls += "/${ResponseClass.URL_TRANSACTION_BUY}"
        Log.e("this url", urls)
        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, urls, jsonObject,
            Response.Listener { response ->
                Log.e("response", response.toString())
                onGetResponseTransaction(response)
                dialog.dismiss()
            },
            Response.ErrorListener { error ->
                dialog.dismiss()
                ResponseClass.showErrorResponse(requireContext(), error)
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

        requestQueue.add(jsonObjectRequest)
    }

    private fun onGetResponseTransaction(response : JSONObject)
    {
        try
        {
            val status = response.get("status") as String
            val message = response.get("message") as String
            if(status == ResponseClass.STATUS_SUCCESS) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                requestGetUser()
            }
            else Toast.makeText(requireContext(), "Failed, $message", Toast.LENGTH_SHORT).show()
        }
        catch (e : Exception)
        {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Gagal bertransaksi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestGetUser()
    {
        val token = PreferencesData.getToken(requireContext())
        Log.e("token", token)
        val queue = Volley.newRequestQueue(requireContext())

        val dialog = ProgressDialog(requireContext())
        dialog.setMessage("Sedang mempersiapkan . . .")
        dialog.setCancelable(false)
        dialog.show()

        var urls = PreferencesData.getURL(requireContext())
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
                ResponseClass.showErrorResponse(requireContext(), error)
                Toast.makeText(requireContext(), "Data not updated", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
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
                Toast.makeText(requireContext(), "Data updated", Toast.LENGTH_SHORT).show()
                val data = gson.fromJson(userObject.get("message"), GetUserResponse.GetUserData::class.java)
                PreferencesData.setRole(requireContext(), data.role)
                PreferencesData.setMoney(requireContext(), data.money)
                PreferencesData.setName(requireContext(), data.name)
                PreferencesData.setUsername(requireContext(), data.username)
                PreferencesData.setEmail(requireContext(), data.email)
                PreferencesData.setAddress(requireContext(), data.address)
                PreferencesData.setPhone(requireContext(), data.phone)
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            else{
                Toast.makeText(requireContext(), "Data not updated", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
        catch (e : Exception)
        {
            globalDialog.dismiss()
            Toast.makeText(requireContext(), "Data not updated", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
