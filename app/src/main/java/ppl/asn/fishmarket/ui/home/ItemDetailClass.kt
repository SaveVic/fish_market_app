package ppl.asn.fishmarket.ui.home

import android.os.Parcel
import android.os.Parcelable

private const val DEFAULT = "default"

class ItemDetailClass (
    val itemId : String,
    val named : String,
    val description : String,
    val storeId : String,
    val stock : Int,
    val price : Int,
    var qty : Int) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: DEFAULT,
        parcel.readString() ?: DEFAULT,
        parcel.readString() ?: DEFAULT,
        parcel.readString() ?: DEFAULT,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(itemId)
        parcel.writeString(named)
        parcel.writeString(description)
        parcel.writeString(storeId)
        parcel.writeInt(stock)
        parcel.writeInt(price)
        parcel.writeInt(qty)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ItemDetailClass> {
        override fun createFromParcel(parcel: Parcel): ItemDetailClass {
            return ItemDetailClass(parcel)
        }

        override fun newArray(size: Int): Array<ItemDetailClass?> {
            return arrayOfNulls(size)
        }
    }

}