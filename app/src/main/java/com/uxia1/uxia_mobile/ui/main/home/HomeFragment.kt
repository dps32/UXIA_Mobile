package com.uxia1.uxia_mobile.ui.main.home


import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.uxia1.uxia_mobile.data.model.Device
import com.uxia1.uxia_mobile.ui.common.ShareViewModel
import com.uxia1.uxia_mobile.ui.main.MainActivity
import com.uxia1.uxia_mobile.core.tts.TTS
import com.uxia1.uxia_mobile.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import java.io.File

class HomeFragment : Fragment() {
    lateinit var homeViewModel : HomeViewModel
    lateinit var txtInfo : TextView
    lateinit var redCircle : ImageView
    private var pulseAnimator: AnimatorSet? = null
    lateinit var fotoView : ImageView

    lateinit var btnShowDialog : Button
    private val viewModel: ShareViewModel by activityViewModels()
//    var device : Device? = null

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        //detecta si cambia el dispositivo en sharedViewModel
//        viewModel.device.observe(viewLifecycleOwner){ newDevice ->
//            device=newDevice
////            actualizarDevice(newDevice)
//        }

        viewModel.imgFile.observe(viewLifecycleOwner){ file ->
            cambiarImagen(file)
        }






        //detecta si esta hablando el tts
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Escuchamos al objeto Singleton directamente
                TTS.isSpeaking.collect { talking ->
                    toggleSpeakingAnimation(talking)
                    Log.d("TTS","estado: $talking")
                }
            }
        }


        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root




        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        redCircle = binding.redCircle
        btnShowDialog = binding.btnShowDialog
        txtInfo = binding.txtInfo
        fotoView = binding.fotoView
        cagarXML()



        btnShowDialog.setOnClickListener {
            val btAdapter = BluetoothAdapter.getDefaultAdapter()
            val btdevice = btAdapter.getRemoteDevice(viewModel.device.value.address)
            Log.d("Test","usado ")

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

//                    device = Device(name, address,"")
//                    actualizarDevice(device!!)

                    viewModel.updateDevice(Device(name,address,""))
                    txtInfo.text = "${name}\n${address}"
                    btnShowDialog.isEnabled=true
                }
            }
        } catch (e: Exception) {

            btnShowDialog.isEnabled=false
            txtInfo.text= "Selecciona un dispositivo bluetooth en Setting"

            e.printStackTrace()
        }
    }


    fun cambiarImagen(imgFile : File){
        fotoView.setImageDrawable(null)
        fotoView.setImageURI(imgFile.toUri())
    }
    private fun toggleSpeakingAnimation(isSpeaking: Boolean) {

        if (isSpeaking) {

            btnShowDialog.text = "talking..."
            btnShowDialog.isEnabled=false


            // Configuramos la animación de pulso
            val scaleX = ObjectAnimator.ofFloat(redCircle, "scaleX", 1f, 1.3f)
            val scaleY = ObjectAnimator.ofFloat(redCircle, "scaleY", 1f, 1.3f)

            scaleX.repeatCount = ObjectAnimator.INFINITE
            scaleX.repeatMode = ObjectAnimator.REVERSE
            scaleY.repeatCount = ObjectAnimator.INFINITE
            scaleY.repeatMode = ObjectAnimator.REVERSE

            pulseAnimator = AnimatorSet().apply {
                playTogether(scaleX, scaleY)

                duration = 300 // Velocidad del pulso
                start()
            }
        } else {
            // Detenemos la animación y volvemos al tamaño
            btnShowDialog.text="Connect"
            btnShowDialog.isEnabled=true
            pulseAnimator?.cancel()
            redCircle.animate().scaleX(1f).scaleY(1f).setDuration(200).start()





        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}

