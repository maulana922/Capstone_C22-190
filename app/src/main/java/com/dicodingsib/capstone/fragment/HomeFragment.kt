package com.dicodingsib.capstone.fragment

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.dicodingsib.capstone.R
import com.dicodingsib.capstone.article.RecycleActivity
import com.dicodingsib.capstone.article.ReduceActivity
import com.dicodingsib.capstone.article.ReuseActivity
import com.dicodingsib.capstone.databinding.FragmentHomeBinding
import com.dicodingsib.capstone.model.Tabungan
import com.dicodingsib.capstone.utility.Extensions.animateVisibility
import com.dicodingsib.capstone.utility.Extensions.rupiahFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null
    private lateinit var db_Tabungan : FirebaseDatabase
    private lateinit var dbRef: DatabaseReference
    lateinit var id: String
    lateinit var kategoriSelected: String
    lateinit var hargaSelected: String
    lateinit var kategori: Array<String>
    lateinit var harga: Array<String>
    lateinit var tanggal: String
    var countBerat = 0
    var countHarga = 0
    var countTotal = 0



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        user = auth.currentUser

        db_Tabungan = Firebase.database
        dbRef = db_Tabungan.getReference(TABUNGAN_CHILD)
        setInitLayout()


        clickReuse()
        clickReduce()
        clickRecycle()


        binding.btnTabung.setOnClickListener{
            tanggal = binding.inputTanggal.text.toString()

            if ((kategori.isEmpty()) || (countBerat == 0) || (countHarga == 0 || tanggal.isEmpty() )){
                Toast.makeText(
                    activity,
                    "Data tidak boleh ada yang kosong!",
                    Toast.LENGTH_SHORT
                ).show()
                setLoadingState(false)
            }
            else {
                val tabungan = Tabungan(
                    kategori = kategoriSelected,
                    berat = countBerat,
                    harga = countHarga,
                    total = countTotal,
                    tanggal = tanggal
                )
                id = user?.uid.toString()
                dbRef.child(id).push().setValue(tabungan)
                Toast.makeText(
                    activity,
                    "Tabungan anda sedang diproses, cek di menu riwayat",
                    Toast.LENGTH_LONG
                ).show()
                binding.inputBerat.text?.clear()
                binding.inputTanggal.text?.clear()
            }
        }
    }

    private fun clickReduce() {
        binding.cvReduce.setOnClickListener {
            val intent = Intent(activity, ReduceActivity::class.java)
            startActivity(intent)
        }

    }private fun clickRecycle() {
        binding.cvRecycle.setOnClickListener {
            val intent = Intent(activity, RecycleActivity::class.java)
            startActivity(intent)
        }
    }

    private fun clickReuse() {
        binding.cvReuse.setOnClickListener {
            val intent = Intent(activity, ReuseActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setInitLayout() {
        kategori = resources.getStringArray(R.array.kategori_sampah)
        harga = resources.getStringArray(R.array.harga_perkilo)

        val arrayBahasa =
            activity?.let { ArrayAdapter(it, android.R.layout.simple_list_item_1, kategori) }
        arrayBahasa?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spKategori.adapter = arrayBahasa

        binding.spKategori.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                kategoriSelected = parent.getItemAtPosition(position).toString()
                hargaSelected = harga[position]
                binding.spKategori.isEnabled = true
                countHarga = hargaSelected.toInt()
                if (binding.inputBerat.text.toString() != "") {
                    countBerat = binding.inputBerat.text.toString().toInt()
                    setTotalPrice(countBerat)
                } else {
                    binding.inputHarga.setText(rupiahFormat(countHarga))
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        binding.inputBerat.addTextChangedListener { editable ->
            if (editable?.isNotEmpty() == true) {
                countBerat = editable.toString().toInt()
                setTotalPrice(countBerat)
            }
            else {
                binding.inputHarga.setText(rupiahFormat(countHarga))
            }
        }

        binding.inputTanggal.setOnClickListener { view: View? ->
            val tanggalSetor = Calendar.getInstance()
            val date =
                DatePickerDialog.OnDateSetListener { view1: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    tanggalSetor[Calendar.YEAR] = year
                    tanggalSetor[Calendar.MONTH] = monthOfYear
                    tanggalSetor[Calendar.DAY_OF_MONTH] = dayOfMonth
                    val strFormatDefault = "d MMMM yyyy"
                    val simpleDateFormat = SimpleDateFormat(strFormatDefault, Locale.getDefault())
                    binding.inputTanggal.setText(simpleDateFormat.format(tanggalSetor.time))
                }
            activity?.let {
                DatePickerDialog(
                    it, date,
                    tanggalSetor[Calendar.YEAR],
                    tanggalSetor[Calendar.MONTH],
                    tanggalSetor[Calendar.DAY_OF_MONTH]
                ).show()
            }
        }
    }

    private fun setTotalPrice(berat: Int) {
        countTotal = countHarga * berat
        binding.inputHarga.setText(rupiahFormat(countTotal))
    }

        private fun setLoadingState(isLoading: Boolean) {
            binding.apply {

                if (isLoading) {
                    viewLoading.animateVisibility(true, 20)
                } else {
                    viewLoading.animateVisibility(false)
                }
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }

        companion object {
            const val TABUNGAN_CHILD = "Tabungan"
        }


}