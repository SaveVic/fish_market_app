package ppl.asn.fishmarket.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import ppl.asn.fishmarket.R

private const val URL_IMAGE = "https://source.unsplash.com/random/featured/?fish"

class ListAdapterItem(val datas : ArrayList<ItemDetailClass>, private var dataFilter: ArrayList<ItemDetailClass> = datas, private val getDetail : IGetDetail)
    : RecyclerView.Adapter<ListAdapterItem.ItemViewHolder>(), Filterable
{
    override fun getFilter(): Filter {
        return object : Filter(){
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString = charSequence.toString()
                dataFilter = if (charString.isEmpty()) {
                    datas
                } else {
                    val filtered = arrayListOf<ItemDetailClass>()
                    for (row in datas) {
                        if (row.named.toLowerCase().contains(charString.toLowerCase())) {
                            filtered.add(row)
                        }
                    }
                    filtered
                }
                val filterResult = FilterResults()
                filterResult.values = dataFilter
                return filterResult
            }

            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults?) {
                dataFilter = filterResults?.values as ArrayList<ItemDetailClass>? ?: arrayListOf()
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_home_list, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataFilter.count()
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val items = dataFilter[position]
        Glide.with(holder.image.context)
            .load(URL_IMAGE)
            .centerCrop()
            .signature(ObjectKey(System.currentTimeMillis().toString()))
            .into(holder.image)
        holder.names.text = items.named
        val str = "Rp ${items.price}"
        holder.price.text = str
        holder.detail.setOnClickListener {
            getDetail.getDetail(dataFilter[position])
        }
    }

    class ItemViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)
    {
        var image = itemView.findViewById(R.id.item_image) as ImageView
        var names = itemView.findViewById(R.id.item_name) as TextView
        var price = itemView.findViewById(R.id.item_price) as TextView
        var detail = itemView.findViewById(R.id.item_add_button) as ImageView
    }
}

interface IGetDetail
{
    fun getDetail(itemSelected : ItemDetailClass)
}