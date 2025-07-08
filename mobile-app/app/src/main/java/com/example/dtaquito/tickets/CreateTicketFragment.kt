package com.example.dtaquito.tickets

import Beans.tickets.CreateTicketRequest
import Interface.PlaceHolder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.dtaquito.R
import com.example.dtaquito.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import network.RetrofitClient

class CreateTicketFragment : Fragment() {

    private val service by lazy { RetrofitClient.instance.create(PlaceHolder::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_create_ticket, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        val bankNameSpinner = view.findViewById<Spinner>(R.id.bankNameTextView_spinner)
        val bankNameEditText = view.findViewById<EditText>(R.id.bankNameTextView_input)
        val accountNumberInput = view.findViewById<EditText>(R.id.account_number_input)
        val nameInput = view.findViewById<EditText>(R.id.name_input)
        val submitButton = view.findViewById<Button>(R.id.submit_button)
        val amountInput = view.findViewById<TextView>(R.id.amount_input)

        setupAmountInput(amountInput)
        setupRadioGroup(radioGroup, bankNameSpinner, bankNameEditText, accountNumberInput)
        setupBankNameSpinner(bankNameSpinner)
        setupSubmitButton(submitButton, nameInput, accountNumberInput, radioGroup, bankNameSpinner, bankNameEditText)
    }

    private fun setupAmountInput(amountInput: TextView) {
        val creditsString = arguments?.getString("credits", "0.0") ?: "0.0"
        val userCredits = creditsString.toDoubleOrNull() ?: 0.0
        Log.d("PasoDeCreditos", "CreateTicketFragment - Créditos recibidos: $userCredits")
        amountInput.text = userCredits.toString()
    }

    private fun setupRadioGroup(
        radioGroup: RadioGroup,
        bankNameSpinner: Spinner,
        bankNameEditText: EditText,
        accountNumberInput: EditText
    ) {
        radioGroup.setOnCheckedChangeListener { _, _ ->
            accountNumberInput.text.clear()
            bankNameEditText.text.clear()
            when (radioGroup.checkedRadioButtonId) {
                R.id.option1 -> {
                    bankNameSpinner.visibility = View.VISIBLE
                    bankNameEditText.visibility = View.GONE
                    bankNameSpinner.setSelection(0)
                }
                R.id.option2 -> {
                    bankNameSpinner.visibility = View.GONE
                    bankNameEditText.visibility = View.VISIBLE
                }
            }
            updateAccountNumberLimit(bankNameSpinner, radioGroup, accountNumberInput)
        }
    }

    private fun setupBankNameSpinner(bankNameSpinner: Spinner) {
        val items = listOf(
            getString(R.string.choose_bank),
            getString(R.string.interbank),
            getString(R.string.bcp),
            getString(R.string.bbva)
        )
        val adapter = object : ArrayAdapter<String>(requireContext(), R.layout.spinner_items, items) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(if (position == 0) "#4D4D4D".toColorInt() else android.graphics.Color.WHITE)
                return view
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(if (position == 0) "#4D4D4D".toColorInt() else android.graphics.Color.WHITE)
                return view
            }
        }
        bankNameSpinner.adapter = adapter

        bankNameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val radioGroup = requireView().findViewById<RadioGroup>(R.id.radioGroup)
                val accountNumberInput = requireView().findViewById<EditText>(R.id.account_number_input)
                updateAccountNumberLimit(bankNameSpinner, radioGroup, accountNumberInput)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSubmitButton(
        submitButton: Button,
        nameInput: EditText,
        accountNumberInput: EditText,
        radioGroup: RadioGroup,
        bankNameSpinner: Spinner,
        bankNameEditText: EditText
    ) {
        submitButton.setOnClickListener {
            val fullName = nameInput.text.toString()
            val accountNumber = accountNumberInput.text.toString()
            val transferType = when (radioGroup.checkedRadioButtonId) {
                R.id.option1 -> "CC"
                R.id.option2 -> "CCI"
                else -> null
            }
            val bankName = if (transferType == "CC") {
                bankNameSpinner.selectedItem.toString()
            } else {
                bankNameEditText.text.toString()
            }

            if (fullName.isEmpty() || accountNumber.isEmpty() || transferType == null || bankName.isEmpty()) {
                requireContext().showToast("Por favor, completa todos los campos.")
                return@setOnClickListener
            }

            val ticket = CreateTicketRequest(fullName, transferType, bankName, accountNumber)
            Log.d("CreateTicketFragment", "Datos recopilados: $ticket")

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val response = service.createBankTransfer(ticket)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            requireContext().showToast("Transferencia creada exitosamente.")
                            // Aquí puedes navegar al fragmento de tickets o cerrar el fragmento
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        } else {
                            requireContext().showToast("Error al crear la transferencia: ${response.code()}")
                            Log.e("CreateTicketFragment", "Error: ${response.errorBody()?.string()}")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        requireContext().showToast("Error de red: ${e.message}")
                        Log.e("CreateTicketFragment", "Fallo en la llamada: ${e.message}")
                    }
                }
            }
        }
    }

    private fun updateAccountNumberLimit(
        bankNameSpinner: Spinner,
        radioGroup: RadioGroup,
        accountNumberInput: EditText
    ) {
        val selectedBank = bankNameSpinner.selectedItem.toString()
        val transferType = when (radioGroup.checkedRadioButtonId) {
            R.id.option1 -> "CC"
            R.id.option2 -> "CCI"
            else -> null
        }

        val maxLength = when {
            transferType == "CC" && selectedBank == "Interbank" -> 13
            transferType == "CC" && selectedBank == "BCP" -> 14
            transferType == "CC" && selectedBank == "BBVA" -> 18
            transferType == "CCI" -> 18
            else -> 0
        }

        Log.d("CreateTicketFragment", "Banco: $selectedBank, Tipo: $transferType, maxLength: $maxLength")
        setAccountNumberLimit(accountNumberInput, maxLength)
    }

    private fun setAccountNumberLimit(editText: EditText, maxLength: Int) {
        Log.d("CreateTicketFragment", "setAccountNumberLimit llamado con maxLength: $maxLength")
        editText.filters = arrayOf(android.text.InputFilter.LengthFilter(maxLength))
    }
}