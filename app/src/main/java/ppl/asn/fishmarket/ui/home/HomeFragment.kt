package ppl.asn.fishmarket.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.andremion.counterfab.CounterFab
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.JsonParser
import ppl.asn.fishmarket.R
import ppl.asn.fishmarket.data.PreferencesData
import ppl.asn.fishmarket.response.GetProductResponse
import ppl.asn.fishmarket.response.ResponseClass
import kotlin.math.max
import kotlin.math.min


class HomeFragment : Fragment(), SearchView.OnQueryTextListener
{
    private lateinit var listView : RecyclerView
    private lateinit var fabCart : CounterFab
    private lateinit var search : SearchView
    private lateinit var loadingBar : ProgressBar
    private lateinit var emptyText : TextView

    private var cartData = arrayListOf<ItemDetailClass>()
    private lateinit var adapter : ListAdapterItem

    private lateinit var model : HomeSharedViewModel
    private var firstInitiate = true

    private var buyCount = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        listView = root.findViewById(R.id.home_list)
        fabCart = root.findViewById(R.id.home_fab)
        search = root.findViewById(R.id.home_search)
        loadingBar = root.findViewById(R.id.home_progressBar)
        emptyText = root.findViewById(R.id.home_empty)

        emptyText.visibility = View.GONE

        Log.e("home", "onCreate, cart = ${cartData.size}")
        model = ViewModelProviders.of(activity!!).get(HomeSharedViewModel::class.java)

        cartData = arrayListOf()
        firstInitiate = true
        initiateData()
        return root
    }

    private fun initiateData()
    {
        loadingBar.visibility = View.VISIBLE
        requestProductData()
    }

    private fun requestProductData()
    {
        val token = PreferencesData.getToken(requireContext())
        Log.e("token", token)
        val queue = Volley.newRequestQueue(requireContext())

//        val dialog = ProgressDialog(requireContext())
//        dialog.setMessage("Sedang mempersiapkan . . .")
//        dialog.setCancelable(false)
//        dialog.show()

        var urls = PreferencesData.getURL(requireContext())
        if(urls.isEmpty()) urls = "${ResponseClass.URL_MAIN}/${ResponseClass.URL_GET_PRODUCT}"
        else urls += "/${ResponseClass.URL_GET_PRODUCT}"
        Log.e("this url", urls)

        val request = object : StringRequest(
            Request.Method.GET, urls,
            Response.Listener { response ->
                Log.e("response", response)
                getResponseProduct(response)
            },
            Response.ErrorListener { error ->
                loadingBar.visibility = View.GONE
                initiateView()
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
        queue.add(request)
    }

    private fun getResponseProduct(serverResponse : String)
    {
        val gson = Gson()

        loadingBar.visibility = View.GONE
        try
        {
            val productElemen = JsonParser().parse(serverResponse)
            val productObject = productElemen.asJsonObject
            if(productObject.get("status").asString == ResponseClass.STATUS_SUCCESS){
                val dataObject = productObject.getAsJsonArray("message")
                for(data in dataObject){
                    val item = gson.fromJson(data, GetProductResponse.GetProductData::class.java)
                    cartData.add(
                        ItemDetailClass(
                            item._id,
                            item.name,
                            item.description,
                            item.store,
                            item.stock,
                            item.price,
                            0
                        )
                    )
                }
            }
            else{
                Toast.makeText(requireContext(), "Failed, ${productObject.get("message") ?: "unknown"}", Toast.LENGTH_SHORT).show()
            }
        }
        catch (e : Exception)
        {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
        }

        initiateView()
    }

    private fun initiateView()
    {
        model.sharedList.observe(this, Observer {list ->
            Log.e("home", "view")
            if(firstInitiate){
                firstInitiate = false
                for(item in list){
                    for((ind, value) in cartData.withIndex()){
                        if(value.itemId == item.itemId) cartData[ind] = item
                    }
                }

                buyCount = 0
                for(product in cartData){
                    if(product.qty > 0) buyCount++
                }
                fabCart.count = buyCount

                adapter = ListAdapterItem(
                    cartData,
                    dataFilter = cartData,
                    getDetail = object : IGetDetail {
                        override fun getDetail(itemSelected: ItemDetailClass) {
                            showDetailItem(itemSelected)
                        }
                    })
                listView.layoutManager = LinearLayoutManager(requireContext())
                listView.adapter = adapter

                if(cartData.isEmpty()){
                    emptyText.visibility = View.VISIBLE
                    listView.visibility = View.GONE
                }
                else{
                    emptyText.visibility = View.GONE
                    listView.visibility = View.VISIBLE
                }

                fabCart.setOnClickListener {
                    goToCart(it)
                }

                search.setOnQueryTextListener(this)
            }
        })

    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        adapter.filter.filter(query)
        return false
    }

    override fun onQueryTextChange(query: String?): Boolean {
        adapter.filter.filter(query)
        return false
    }

    private fun showDetailItem(itemSelected : ItemDetailClass)
    {
        val dialog = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.alert_layout_item, null)
        dialog.setView(dialogView)
        dialog.setCancelable(true)

        val nameText = dialogView.findViewById<TextView>(R.id.detail_name)
        val descriptionText = dialogView.findViewById<TextView>(R.id.detail_description)
        val priceText = dialogView.findViewById<TextView>(R.id.detail_price)
        val stockText = dialogView.findViewById<TextView>(R.id.detail_stock)
        val textQty = dialogView.findViewById<TextView>(R.id.detail_qty)
        val addButton = dialogView.findViewById<ImageView>(R.id.detail_add)
        val removeButton = dialogView.findViewById<ImageView>(R.id.detail_remove)

        nameText.text = itemSelected.named
        descriptionText.text = itemSelected.description
        val str = "Rp ${itemSelected.price}"
        priceText.text = str
        val stk = "Stock : ${itemSelected.stock}"
        stockText.text = stk
        var qty = itemSelected.qty
        textQty.text = qty.toString()
        addButton.setOnClickListener {
            qty = min(qty + 1, itemSelected.stock)
            textQty.text = qty.toString()
        }
        removeButton.setOnClickListener {
            qty = max(0, qty - 1)
            textQty.text = qty.toString()
        }

        dialog.setPositiveButton("Simpan") { dialogs, _ ->
            when{
                itemSelected.qty > 0 && qty == 0 -> {
                    buyCount -= 1
                    updateFab()
                }
                itemSelected.qty == 0 && qty > 0 -> {
                    buyCount += 1
                    updateFab()
                }
            }
            itemSelected.qty = qty
            dialogs.dismiss()
        }
        dialog.setNegativeButton("Batal") { dialogs, _ ->
            dialogs.dismiss()
        }

        dialog.show()
    }

    private fun updateFab()
    {
        fabCart.count = buyCount
    }

    private fun goToCart(view : View)
    {
        val itemBuyed = arrayListOf<ItemDetailClass>()
        for(item in adapter.datas){
            if(item.qty > 0) itemBuyed.add(item)
        }
        model.changeList(itemBuyed)
        val bundle = bundleOf("datas" to itemBuyed)
        view.findNavController().navigate(R.id.action_home_to_cart, bundle)
    }
}