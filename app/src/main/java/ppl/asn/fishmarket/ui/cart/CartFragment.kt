package ppl.asn.fishmarket.ui.cart

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ppl.asn.fishmarket.R
import ppl.asn.fishmarket.ui.home.HomeSharedViewModel
import ppl.asn.fishmarket.ui.home.ItemDetailClass
import kotlin.math.max
import kotlin.math.min

class CartFragment : Fragment(){

    private lateinit var cartList : RecyclerView
    private lateinit var cartEmpty : TextView
    private lateinit var cartTotal : TextView
    private lateinit var cartCheckout : Button

    private val bundleKey = "datas"
    private var datas : ArrayList<ItemDetailClass>? = null
    private lateinit var adapter : ListAdapterCart

    private lateinit var model : HomeSharedViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val root = inflater.inflate(R.layout.fragment_cart, container, false)

        model = ViewModelProviders.of(activity!!).get(HomeSharedViewModel::class.java)

        cartList = root.findViewById(R.id.cart_list)
        cartEmpty = root.findViewById(R.id.cart_empty)
        cartTotal = root.findViewById(R.id.cart_total)
        cartCheckout = root.findViewById(R.id.cart_checkout)

        datas = arguments?.getParcelableArrayList<ItemDetailClass>(bundleKey)
        initiateView()
        return root
    }

    private fun initiateView()
    {
        var str = "Subtotal : "
        if(datas.isNullOrEmpty()){
            Log.e("cart", "null or empty")
            cartEmpty.visibility = View.VISIBLE
            cartList.visibility = View.INVISIBLE
            str += "-"
            cartTotal.text = str
            cartCheckout.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light))
            cartCheckout.isClickable = false
            cartCheckout.isEnabled = false
        }
        else{
            Log.e("cart", "exist")
            cartEmpty.visibility = View.INVISIBLE
            cartList.visibility = View.VISIBLE
            adapter = ListAdapterCart(
                datas ?: arrayListOf(),
                object : IGetDetailCart {
                    override fun getDetail(item: ItemDetailClass, position: Int) {
                        showDetailItem(item, position)
                    }

                })
            cartList.layoutManager = LinearLayoutManager(requireContext())
            cartList.adapter = adapter
            str += "Rp ${getTotal()}"
            cartTotal.text = str
            cartCheckout.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark))
            cartCheckout.isClickable = true
            cartCheckout.isEnabled = true
            cartCheckout.setOnClickListener {
                goToPayment(it)
            }
        }
    }

    private fun getTotal() : Int
    {
        var total = 0
        for(item in (datas ?: arrayListOf())){
            total += item.price * item.qty
        }
        return total
    }

    private fun showDetailItem(itemSelected : ItemDetailClass, pos : Int)
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
            itemSelected.qty = qty
            if(qty == 0) adapter.removeAt(pos)
            else adapter.updateAt(pos)
            updateViewModel()
            dialogs.dismiss()
        }
        dialog.setNegativeButton("Batal") { dialogs, _ ->
            dialogs.dismiss()
        }

        dialog.show()
    }

    private fun updateViewModel()
    {
        Log.e("adapter count", adapter.datas.size.toString())
        model.changeList(adapter.datas)
    }

    private fun goToPayment(view : View)
    {
        val itemBuyed = adapter.datas
        val bundle = bundleOf("datas" to itemBuyed)
        view.findNavController().navigate(R.id.action_cart_to_checkout, bundle)
    }
}
