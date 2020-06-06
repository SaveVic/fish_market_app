package ppl.asn.fishmarket.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import ppl.asn.fishmarket.R
import ppl.asn.fishmarket.data.PreferencesData

class ProfileFragment : Fragment() {

    private lateinit var nameText : TextView
    private lateinit var usernameText : TextView
    private lateinit var emailText : TextView
    private lateinit var addressText : TextView
    private lateinit var phoneText : TextView
    private lateinit var moneyText : TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        nameText = root.findViewById(R.id.profile_name)
        usernameText = root.findViewById(R.id.profile_username)
        emailText = root.findViewById(R.id.profile_email)
        addressText = root.findViewById(R.id.profile_address)
        phoneText = root.findViewById(R.id.profile_phone)
        moneyText = root.findViewById(R.id.profile_money)

        initiateView()
        return root
    }

    private fun initiateView()
    {
        nameText.text = PreferencesData.getName(requireContext())
        usernameText.text = PreferencesData.getUsername(requireContext())
        emailText.text = PreferencesData.getEmail(requireContext())
        addressText.text = PreferencesData.getAddress(requireContext())
        phoneText.text = PreferencesData.getPhone(requireContext())
        moneyText.text = PreferencesData.getMoney(requireContext()).toString()
    }
}