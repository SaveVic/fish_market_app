package ppl.asn.fishmarket.ui.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import ppl.asn.fishmarket.R
import ppl.asn.fishmarket.ui.home.ItemDetailClass

class ListAdapterCart(val datas : ArrayList<ItemDetailClass>, private val getDetail : IGetDetailCart) : RecyclerView.Adapter<ListAdapterCart.ItemViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_cart_list, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return datas.count()
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val items = datas[position]
        holder.names.text = items.named
        val temp = "Rp ${items.price}"
        holder.price.text = temp
        val str = "x${items.qty}"
        holder.qty.text = str
        holder.cartLayout.setOnClickListener {
            getDetail.getDetail(datas[position], position)
        }
    }

    fun removeAt(position : Int)
    {
        datas.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, datas.size)
    }

    fun updateAt(position: Int)
    {
        notifyItemChanged(position)
    }

    class ItemViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)
    {
        var cartLayout = itemView.findViewById(R.id.cart_card_layout) as CardView
        var image = itemView.findViewById(R.id.item_cart_image) as ImageView
        var names = itemView.findViewById(R.id.item_cart_name) as TextView
        var price = itemView.findViewById(R.id.item_cart_price) as TextView
        var qty = itemView.findViewById(R.id.item_cart_qty) as TextView
    }
}

interface IGetDetailCart{
    fun getDetail(item : ItemDetailClass, position : Int)
}