package com.example.dtaquito.register

import Beans.auth.register.RegisterRequest
import Interface.PlaceHolder
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.example.dtaquito.R
import com.example.dtaquito.login.LoginActivity
import com.example.dtaquito.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import network.RetrofitClient

class RegisterActivity : AppCompatActivity() {

    // Constantes
    companion object {
        // Validación
        private const val MIN_PASSWORD_LENGTH = 16
        private const val MIN_NAME_LENGTH = 2
        private const val PLACEHOLDER_ROLE_POSITION = 0
        
        // UI
        private const val PLACEHOLDER_COLOR = "#4D4D4D"
    }
    
    // Data class para encapsular datos del usuario
    data class UserInput(
        val name: String,
        val email: String,
        val password: String,
        val role: String
    ) {
        fun isValid(): Boolean = name.isNotEmpty() && email.isNotEmpty() && 
                               password.isNotEmpty() && role.isNotEmpty()
    }

    // Variables de estado
    private var isRoleSelected = false
    private var selectedRolePosition = 0
    private lateinit var selectedRole: String

    // Referencias a vistas
    private lateinit var signInTextView: TextView
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var roleSpinner: Spinner
    private lateinit var registerButton: Button

    // Referencias a TextViews de error
    private lateinit var nameError: TextView
    private lateinit var emailError: TextView
    private lateinit var passwordError: TextView
    private lateinit var roleError: TextView
    
    // Referencias a elementos de loading
    private lateinit var loadingProgress: ProgressBar
    private lateinit var loadingText: TextView

    // Servicio de red
    private val apiService by lazy { RetrofitClient.instance.create(PlaceHolder::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        initializeViews()
        setupClickableSignInText()
        setupRoleSpinner()
        setupInputValidation()
        setupRegisterButton()
    }

    private fun initializeViews() {
        // Inicializar vistas principales
        signInTextView = findViewById(R.id.signIn)
        nameInput = findViewById(R.id.name_input)
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        roleSpinner = findViewById(R.id.rol_input)
        registerButton = findViewById(R.id.register_btn)

        // Inicializar TextViews de error
        nameError = findViewById(R.id.name_error)
        emailError = findViewById(R.id.email_error)
        passwordError = findViewById(R.id.password_error)
        roleError = findViewById(R.id.role_error)
        
        // Inicializar elementos de loading
        loadingProgress = findViewById(R.id.loading_progress)
        loadingText = findViewById(R.id.loading_text)
    }

    private fun setupClickableSignInText() {
        val signInText = signInTextView.text.toString()
        val clickableText = getString(R.string.sign_in_clickable)
        val startIndex = signInText.indexOf(clickableText)
        val endIndex = startIndex + clickableText.length

        if (startIndex < 0 || endIndex > signInText.length) return

        val spannableString = SpannableString(signInText)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                navigateToLogin()
            }
        }

        spannableString.apply {
            setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this@RegisterActivity, R.color.green)),
                startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        signInTextView.apply {
            text = spannableString
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun setupRoleSpinner() {
        val roleItems = listOf(
            getString(R.string.role_choose_text), 
            getString(R.string.role_player), 
            getString(R.string.role_owner)
        )
        val adapter = createSpinnerAdapter(roleItems)
        
        roleSpinner.adapter = adapter
        roleSpinner.onItemSelectedListener = createSpinnerItemListener(roleItems)
    }

    private fun createSpinnerAdapter(items: List<String>): ArrayAdapter<String> {
        return object : ArrayAdapter<String>(this, R.layout.spinner_items, items) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                return super.getView(position, convertView, parent).apply {
                    setTextColor(position)
                }
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                return super.getDropDownView(position, convertView, parent).apply {
                    setTextColor(position)
                }
            }

            private fun View.setTextColor(position: Int) {
                findViewById<TextView>(android.R.id.text1).setTextColor(
                    if (position == PLACEHOLDER_ROLE_POSITION) {
                        PLACEHOLDER_COLOR.toColorInt()
                    } else {
                        Color.WHITE
                    }
                )
            }
        }
    }

    private fun createSpinnerItemListener(items: List<String>): AdapterView.OnItemSelectedListener {
        return object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                handlePasswordValidationBeforeSpinner()
                updateRoleSelection(position, items)
                validateRole()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                resetRoleSelection()
                validateRole()
            }
        }
    }

    private fun handlePasswordValidationBeforeSpinner() {
        if (passwordInput.hasFocus()) {
            passwordInput.clearFocus()
            validatePassword(passwordInput.text.toString().trim())
        }
    }

    private fun updateRoleSelection(position: Int, items: List<String>) {
        if (position == PLACEHOLDER_ROLE_POSITION) {
            resetRoleSelection()
        } else {
            isRoleSelected = true
            selectedRole = items[position]
            selectedRolePosition = position
        }
    }

    private fun resetRoleSelection() {
        isRoleSelected = false
        selectedRole = ""
        selectedRolePosition = PLACEHOLDER_ROLE_POSITION
    }

    private fun setupRegisterButton() {
        registerButton.setOnClickListener {
            handleRegisterButtonClick()
        }
    }

    private fun setupInputValidation() {
        setupNameInputValidation()
        setupEmailInputValidation()
        setupPasswordInputValidation()
    }

    private fun setupNameInputValidation() {
        // Validación en tiempo real
        nameInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (s != null && s.isNotEmpty()) {
                    validateName(s.toString().trim())
                } else {
                    hideError(nameError)
                }
            }
        })

        // Mantener también el focus listener
        nameInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                hideError(nameError)
            } else {
                validateName(nameInput.text.toString().trim())
            }
        }
    }

    private fun setupEmailInputValidation() {
        // Validación en tiempo real
        emailInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (s != null && s.isNotEmpty()) {
                    validateEmail(s.toString().trim())
                } else {
                    hideError(emailError)
                }
            }
        })

        // Mantener también el focus listener
        emailInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                hideError(emailError)
            } else {
                validateEmail(emailInput.text.toString().trim())
                validatePreviousFieldIfNotEmpty(nameInput) { validateName(it) }
            }
        }
    }

    private fun setupPasswordInputValidation() {
        // Validación en tiempo real
        passwordInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (s != null && s.isNotEmpty()) {
                    validatePassword(s.toString().trim())
                } else {
                    hideError(passwordError)
                }
            }
        })

        // Mantener también el focus listener
        passwordInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                hideError(passwordError)
            } else {
                validatePassword(passwordInput.text.toString().trim())
                validatePreviousFieldIfNotEmpty(emailInput) { validateEmail(it) }
                validatePreviousFieldIfNotEmpty(nameInput) { validateName(it) }
            }
        }
    }

    private fun validatePreviousFieldIfNotEmpty(input: EditText, validator: (String) -> Boolean) {
        if (input.text.isNotEmpty()) {
            validator(input.text.toString().trim())
        }
    }

    private fun handleRegisterButtonClick() {
        val userData = getUserInputData()
        if (!validateAllInputs(userData)) {
            return
        }

        showLoadingState()
        val registerRequest = createRegisterRequest(userData)
        registerUser(registerRequest)
    }

    private fun getUserInputData(): UserInput {
        return UserInput(
            name = nameInput.text.toString().trim(),
            email = emailInput.text.toString().trim(),
            password = passwordInput.text.toString().trim(),
            role = selectedRole
        )
    }

    private fun createRegisterRequest(userData: UserInput): RegisterRequest {
        return RegisterRequest(
            name = userData.name,
            email = userData.email,
            password = userData.password,
            role = userData.role.uppercase()
        )
    }

    private fun disableRegisterButton() {
        registerButton.isEnabled = false
    }

    private fun enableRegisterButton() {
        registerButton.isEnabled = true
    }
    
    // Métodos para manejar el estado de loading
    private fun showLoadingState() {
        disableRegisterButton()
        loadingProgress.visibility = View.VISIBLE
        loadingText.visibility = View.VISIBLE
        loadingText.text = getString(R.string.loading_registering_user)
    }
    
    private fun hideLoadingState() {
        enableRegisterButton()
        loadingProgress.visibility = View.GONE
        loadingText.visibility = View.GONE
    }

    // Validación de entradas
    private fun validateAllInputs(userData: UserInput): Boolean {
        clearAllErrors()
        
        val validationResults = listOf(
            validateName(userData.name),
            validateEmail(userData.email),
            validatePassword(userData.password),
            validateRole()
        )
        
        return validationResults.all { it }
    }

    // Validaciones individuales
    private fun validateName(name: String): Boolean {
        val errorMessage = when {
            name.isEmpty() -> getString(R.string.error_name_empty)
            name.any { it.isDigit() } -> getString(R.string.error_name_numbers)
            name.length < MIN_NAME_LENGTH -> getString(R.string.error_name_too_short, MIN_NAME_LENGTH)
            !name.all { it.isLetter() || it.isWhitespace() } -> getString(R.string.error_name_invalid_chars)
            else -> return showValidField(nameError)
        }
        return showErrorField(nameError, errorMessage)
    }

    private fun validateEmail(email: String): Boolean {
        val errorMessage = when {
            email.isEmpty() -> getString(R.string.error_email_empty)
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> getString(R.string.error_email_invalid)
            else -> return showValidField(emailError)
        }
        return showErrorField(emailError, errorMessage)
    }

    private fun validatePassword(password: String): Boolean {
        if (password.isEmpty()) {
            return showErrorField(passwordError, getString(R.string.error_password_empty))
        }

        val errors = mutableListOf<String>()
        
        if (password.length < MIN_PASSWORD_LENGTH) {
            errors.add(getString(R.string.error_password_length, MIN_PASSWORD_LENGTH))
        }
        
        if (!password.any { it.isUpperCase() }) {
            errors.add(getString(R.string.error_password_uppercase))
        }
        
        if (!password.any { !it.isLetterOrDigit() }) {
            errors.add(getString(R.string.error_password_special))
        }
        
        return if (errors.isNotEmpty()) {
            showErrorField(passwordError, errors.joinToString("\n"))
        } else {
            showValidField(passwordError)
        }
    }

    private fun validateRole(): Boolean {
        return if (!isRoleSelected || selectedRolePosition == PLACEHOLDER_ROLE_POSITION) {
            showErrorField(roleError, getString(R.string.error_role_empty))
        } else {
            showValidField(roleError)
        }
    }

    // Métodos de utilidad para mostrar errores
    private fun showErrorField(errorTextView: TextView, message: String): Boolean {
        showError(errorTextView, message)
        return false
    }

    private fun showValidField(errorTextView: TextView): Boolean {
        hideError(errorTextView)
        return true
    }

    // Mostrar error en un TextView específico
    private fun showError(errorTextView: TextView, message: String) {
        errorTextView.text = message
        errorTextView.visibility = View.VISIBLE
    }

    // Ocultar error de un TextView específico
    private fun hideError(errorTextView: TextView) {
        errorTextView.text = ""
        errorTextView.visibility = View.GONE
    }

    // Limpiar todos los errores
    private fun clearAllErrors() {
        hideError(nameError)
        hideError(emailError)
        hideError(passwordError)
        hideError(roleError)
    }

    // Registro del usuario
    private fun registerUser(registerRequest: RegisterRequest) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { 
                    apiService.createUser(registerRequest) 
                }
                handleRegistrationResponse(response.isSuccessful)
            } catch (_: Exception) {
                handleRegistrationError(getString(R.string.error_network))
            }
        }
    }
    
    private fun handleRegistrationResponse(isSuccessful: Boolean) {
        hideLoadingState()
        if (isSuccessful) {
            showToast(getString(R.string.success_register))
            navigateToLogin()
        } else {
            showToast(getString(R.string.error_register))
        }
    }
    
    private fun handleRegistrationError(message: String) {
        hideLoadingState()
        showToast(message)
    }

    // Navegación a la pantalla de inicio de sesión
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
