package ppl.asn.fishmarket.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeSharedViewModel : ViewModel() {

    val sharedList = MutableLiveData<ArrayList<ItemDetailClass>>()

    fun changeList(item: ArrayList<ItemDetailClass>) {
        sharedList.value = item
    }
}