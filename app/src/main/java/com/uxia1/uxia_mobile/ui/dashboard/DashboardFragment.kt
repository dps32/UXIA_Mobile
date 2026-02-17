package com.uxia1.uxia_mobile.ui.dashboard

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.uxia1.uxia_mobile.History
import com.uxia1.uxia_mobile.R
import com.uxia1.uxia_mobile.databinding.FragmentDashboardBinding
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.fragment.app.activityViewModels
import com.uxia1.uxia_mobile.ShareViewModel
import org.json.JSONObject
import java.io.File
import kotlin.getValue

class DashboardFragment : Fragment() {

    private val viewModel: ShareViewModel by activityViewModels()
    lateinit var lv : ListView
    val dataset = mutableListOf<History>()
    lateinit var adapter : ArrayAdapter<History>

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.response.observe(viewLifecycleOwner){ res ->
            addHistory(res,)
        }

        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root



        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter= getListAdapter()
        lv = binding.listView
        lv.setAdapter(adapter)


    }
    fun addHistory(response: String,image : String){
        val jsonObject = JSONObject(response)
        if(jsonObject.getString("status")=="OK"){
            val data = jsonObject.getJSONObject("data")
            val descriptor = data.getString("description")
            val tagArray = data.getJSONArray("tags")


            val list = mutableListOf<String>()
            for (i in 0 until tagArray.length()) {
                list.add(tagArray.getString(i))
            }

            dataset.add(History(image, list,descriptor))
            adapter.notifyDataSetChanged()

        }


    }

    fun addHistory(history : History){

            val descriptor = history.txtInfo
            val list = history.tags

            dataset.add(history)
            adapter.notifyDataSetChanged()




    }
    fun updateHistory(){
        var response = "test"
        //dataset.clear()
        val jsonObject = JSONObject(response)
        if(jsonObject.getString("status")=="OK"){
            val data = jsonObject.getJSONObject("data")
            val descriptor = data.getString("description")
            val tagArray = data.getJSONArray("tags")

//            val list = mutableListOf<String>()
//            for (i in 0 until tagArray.length()) {
//                list.add(tagArray.getString(i))
//            }




        }

//        for (elem in serverHistory){
//            dataset.add(History("","",""))
//            adapter.notifyDataSetChanged()
//        }
    }

    fun getListAdapter (): ArrayAdapter<History>{
        adapter = object : ArrayAdapter<History>(requireContext(), R.layout.list_historial, dataset)
        {
            override fun getView(pos: Int, convertView: View?, container: ViewGroup): View {
                // getView ens construeix el layout i hi "pinta" els valors de l'element en la posició pos
                var convertView = convertView
                if (convertView == null) {
                    // inicialitzem l'element la View amb el seu layout
                    convertView = getLayoutInflater().inflate(R.layout.list_historial, container, false)
                }
                // "Pintem" valors (quan es refresca)
                convertView.findViewById<ImageView>(R.id.img).setImageBitmap(base64ToBitMap(getItem(pos)?.base4Image))
                convertView.findViewById<TextView>(R.id.txtHistory).text = getItem(pos)?.txtInfo
                var tag = ""
                val tags = getItem(pos)?.tags
                for (i in 0 until tags!!.size) {
                    tag += tags[i]+" "
                    Log.d("TAG", tag)
                }

                convertView.findViewById<TextView>(R.id.tag).text = tag



                return convertView
            }
        }
        return adapter

    }
    fun base64ToBitMap(base64String: String?) : Bitmap? {
        try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)

            val decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            return decodedBitmap
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}