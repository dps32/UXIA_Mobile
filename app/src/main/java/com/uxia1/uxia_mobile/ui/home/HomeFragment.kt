package com.uxia1.uxia_mobile.ui.home

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.util.Xml
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.uxia1.uxia_mobile.Device
import com.uxia1.uxia_mobile.ShareViewModel
import com.uxia1.uxia_mobile.MainActivity
import com.uxia1.uxia_mobile.databinding.FragmentHomeBinding
import org.xmlpull.v1.XmlPullParser
import java.io.File

class HomeFragment : Fragment() {
    lateinit var homeViewModel : HomeViewModel
    lateinit var txtInfo : TextView

    lateinit var fotoView : ImageView

    lateinit var btnShowDialog : Button
    private val viewModel: ShareViewModel by activityViewModels()
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
        viewModel.device.observe(viewLifecycleOwner){ newDevice ->
            device=newDevice
            actualizarDevice(newDevice)
        }

        viewModel.imgFile.observe(viewLifecycleOwner){ file ->
            cambiarImagen(file)
        }

        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root




        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnShowDialog = binding.btnShowDialog
        txtInfo = binding.txtInfo
        fotoView = binding.fotoView
        cagarXML()
        if(device==null){
            btnShowDialog.isEnabled=false
        }

        btnShowDialog.setOnClickListener {
            val btAdapter = BluetoothAdapter.getDefaultAdapter()
            val btdevice = btAdapter.getRemoteDevice(device!!.address)
            (requireContext() as MainActivity).showBLEDialog(btdevice)
        }

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

        txtInfo.text = "${device.nom}\n${device.address}"
        btnShowDialog.isEnabled=true
    }

    fun cambiarImagen(imgFile : File){
        fotoView.setImageDrawable(null)
        fotoView.setImageURI(imgFile.toUri())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}

