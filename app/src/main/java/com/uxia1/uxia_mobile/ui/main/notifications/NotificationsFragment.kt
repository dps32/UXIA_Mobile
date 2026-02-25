package com.uxia1.uxia_mobile.ui.main.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context.BLUETOOTH_SERVICE
import android.content.Context

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.uxia1.uxia_mobile.data.model.Device
import com.uxia1.uxia_mobile.ui.common.ShareViewModel
import com.uxia1.uxia_mobile.R
import com.uxia1.uxia_mobile.databinding.FragmentNotificationsBinding
import com.uxia1.uxia_mobile.ui.main.MainData
import com.uxia1.uxia_mobile.utils.XmlUtils


class NotificationsFragment : Fragment() {

    lateinit var lv : ListView
    lateinit var btnToken : Button


    private val viewModel: ShareViewModel by activityViewModels()
    val dataset = mutableListOf<BluetoothDevice>()
    lateinit var adapter : ArrayAdapter<BluetoothDevice>


    lateinit var recargar : Button


    private lateinit var device: Device


    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
//aca
//        val textView: TextView = binding.textNotifications
//        notificationsViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }

        // Inicialitzem l'ArrayAdapter amb el layout pertinent









        return root
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = getListAdapter()


        btnToken = binding.btnToken
        btnToken.setOnClickListener {
            XmlUtils.eliminarXML("user",requireContext())
            MainData.Companion.setToken("asdasd")
        }
        recargar = binding.btnRecargar
        recargar.setOnClickListener {
            updatePairedDevices()
            adapter.notifyDataSetChanged()

            XmlUtils.eliminarXML("setting",requireContext())

            Toast.makeText(requireContext(),"Recargado", Toast.LENGTH_SHORT).show()
        }

        lv = binding.listView
        lv.setAdapter(adapter)

        lv.setOnItemClickListener { _, _, position, _ ->

            val selectedDevice = dataset[position]

            selectedDevice.let {
                val nom = it.name
                val address = it.address
//           status = selectedDevice.status

                device = Device(nom,address,"")

                guardarData()

                Toast.makeText(
                    requireContext(),
                    "Seleccionado: $nom",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }
    override fun onResume() {
        super.onResume()

        updatePairedDevices()
        adapter.notifyDataSetChanged()

    }
    fun actualizarDevice(device: Device){
        viewModel.updateDevice(device)
    }
    fun getListAdapter (): ArrayAdapter<BluetoothDevice>{
        adapter = object : ArrayAdapter<BluetoothDevice>(requireContext(), R.layout.list_bluetooth_device, dataset)
        {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun getView(pos: Int, convertView: View?, container: ViewGroup): View {
                // getView ens construeix el layout i hi "pinta" els valors de l'element en la posició pos
                var convertView = convertView
                if (convertView == null) {
                    // inicialitzem l'element la View amb el seu layout
                    convertView = getLayoutInflater().inflate(R.layout.list_bluetooth_device, container, false)
                }
                // "Pintem" valors (quan es refresca)
                convertView.findViewById<TextView>(R.id.nom).text = getItem(pos)?.name
                convertView.findViewById<TextView>(R.id.txtHistory).text = getItem(pos)?.address
                convertView.findViewById<TextView>(R.id.status).text = getItem(pos)?.type.toString()
                return convertView
            }
        }
        return adapter

    }

    @SuppressLint("MissingPermission")
    fun updatePairedDevices() {
        if (!checkBluetoothPermission()) return
        // empty list
        dataset.clear()

        // update list
        val bluetoothManager = requireContext().getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        for( elem in bluetoothAdapter.bondedDevices.filter { device ->
            // Filtrar per dispositius BLE
            device.type == BluetoothDevice.DEVICE_TYPE_LE ||
                    device.type == BluetoothDevice.DEVICE_TYPE_DUAL ||
                    device.type == BluetoothDevice.DEVICE_TYPE_UNKNOWN
        } ) {
            // afegim element al dataset
            dataset.add( elem )
            adapter.notifyDataSetChanged()
        }
    }

    private fun checkBluetoothPermission(): Boolean {
        var context = requireContext()
        return if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                1001
            )
            false
        }
    }

    fun guardarData(){
        actualizarDevice(device)
        XmlUtils.generarDeviceXML(device,requireContext())
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

