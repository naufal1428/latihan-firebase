package com.example.budgetbuddy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.MutableLiveData
import com.example.budgetbuddy.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val firestore = FirebaseFirestore.getInstance()
    private var updateId = ""
    private val budgetListLiveData : MutableLiveData<List<Budget>>
    by lazy {
        MutableLiveData<List<Budget>>()
    }
    private val budgetCollectionRef = firestore.collection("budgets")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            btnAdd.setOnClickListener{
                val noninal = edtNominal.text.toString()
                val desc = edtDesc.text.toString()
                val date = edtDate.text.toString()

                val newBudget = Budget(nominal = noninal, description = desc,
                    date = date)
                addBudget(newBudget)
                setEmptyField()
            }

            btnUpdate.setOnClickListener {
                val noninal = edtNominal.text.toString()
                val desc = edtDesc.text.toString()
                val date = edtDate.text.toString()

                val updateBudget = Budget(nominal = noninal, description = desc,
                    date = date)
                updateBudget(updateBudget)
                updateId = ""
                setEmptyField()
            }

            listView.setOnItemClickListener {
                adapterView,_,i,_ ->
                val item = adapterView.adapter.getItem(i) as Budget
                updateId = item.id
                edtNominal.setText(item.nominal)
                edtDesc.setText(item.description)
                edtDate.setText(item.date)
            }

            listView.onItemLongClickListener = AdapterView.OnItemLongClickListener{
                    adapterView, _, i, _ ->
                val item = adapterView.adapter.getItem(i) as Budget
                updateBudget(item)
                true
            }

            listView.onItemLongClickListener = AdapterView.OnItemLongClickListener{
                adapterView, _, i, _ ->
                val item = adapterView.adapter.getItem(i) as Budget
//                updateId = item.id
                deleteBudget(item)
                true
            }
        }
        observeBudgets()
        getAllBudgets()
    }

    private fun getAllBudgets() {
        budgetCollectionRef.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Log.d("MainActivity", "error listening for budget changes",
                    error)
                return@addSnapshotListener
            }
            val budgets = arrayListOf<Budget>()
            snapshots?.forEach{
                documentReference ->
                budgets.add(
                    Budget(documentReference.id,
                        documentReference.get("nominal").toString(),
                        documentReference.get("description").toString(),
                        documentReference.get("date").toString())
                )
            }
            if (budgets != null) {
                budgetListLiveData.postValue(budgets)
            }
        }
    }

    private fun observeBudgets() {
        budgetListLiveData.observe(this) {
            budgets ->
            val adapter = ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                budgets.toMutableList())
            binding.listView.adapter = adapter
        }
    }

    private fun addBudget(budget: Budget) {
        budgetCollectionRef.add(budget).addOnFailureListener{
            Log.d("MainActivity", "Error adding budget : ",
                it)
        }
    }

    private fun updateBudget(budget: Budget) {
        budgetCollectionRef.document(updateId).set(budget)
            .addOnFailureListener {
                Log.d("MainActivity", "Error deleting budget: ",
                    it)
            }
    }

    private fun deleteBudget(budget: Budget) {
        budgetCollectionRef.document(updateId).delete()
            .addOnFailureListener {
                Log.d("MainActivity", "Error deleting budget: ",
                    it)
            }
    }

    private fun setEmptyField() {
        with(binding){
            edtNominal.setText("")
            edtDesc.setText("")
            edtDate.setText("")
        }
    }
}