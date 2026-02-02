package com.uxia1.uxia_mobile.ui.home

import android.os.Bundle
import android.util.Xml
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.uxia1.uxia_mobile.BLEconnDialog
import com.uxia1.uxia_mobile.DeviceViewModel
import com.uxia1.uxia_mobile.databinding.FragmentHomeBinding
import org.xmlpull.v1.XmlPullParser

class HomeFragment : Fragment() {
    lateinit var homeViewModel : HomeViewModel
    lateinit var txtInfo : TextView
    private val viewModel: DeviceViewModel by activityViewModels()
    var device : Device? = null

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewModel.device.observe(viewLifecycleOwner){ device ->
            actualizarDevice(device)
        }

        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root




        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        txtInfo = binding.txtInfo

        cagarXML()

//        homeViewModel.text.observe(viewLifecycleOwner) {
//            txtInfo.text = it
//        }
    }
    fun cagarXML(){
        val filename = "setting.xml"
        try {
            requireContext().openFileInput(filename).use { inputStream ->
                val parser: XmlPullParser = Xml.newPullParser()
                parser.setInput(inputStream, null)

                var eventType = parser.eventType
                var name: String? = null
                var address: String? = null

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            when (parser.name) {
                                "name" -> name = parser.nextText()
                                "address" -> address = parser.nextText()

                            }
                        }
                    }
                    eventType = parser.next()
                }

                if (name != null && address != null) {
                    device = Device(name, address,"")
                    actualizarDevice(device!!)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun actualizarDevice(device: Device){
        txtInfo.text = "${device.nom} ${device.address}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}

data class Device(
    val nom: String,
    val address : String,
    val status : String
)