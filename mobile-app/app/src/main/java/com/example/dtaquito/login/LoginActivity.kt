package com.example.dtaquito.login

import Beans.auth.login.LoginRequest
import Interface.PlaceHolder
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.dtaquito.MainActivity
import com.example.dtaquito.R
import com.example.dtaquito.forgotPassword.ForgotPasswordActivity
import com.example.dtaquito.register.RegisterActivity
import com.example.dtaquito.utils.showToast
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import network.RetrofitClient

class LoginActivity : AppCompatActivity() {

    // Constantes
    companion object {
        private const val SHARED_PREFS = "user_prefs"
        private const val JWT_TOKEN_KEY = "jwt_token"
        private const val ROLE_TYPE_KEY = "role_type"
        private const val USER_CREDITS = "credits"
        private const val NETWORK_TIMEOUT_MS = 30000L // 30 segundos
        private const val REMEMBER_ME_KEY = "remember_me"
        private const val SAVED_EMAIL_KEY = "saved_email"
        private const val SAVED_PASSWORD_KEY = "saved_password"
        private const val MIN_PASSWORD_LENGTH = 6
    }

    // Data class para encapsular credenciales
    data class LoginCredentials(val email: String, val password: String) {
        fun isValid(): Boolean =
                email.isNotEmpty() &&
                        password.isNotEmpty() &&
                        Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Referencias a vistas principales
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var passwordToggle: ImageView
    private lateinit var rememberMeCheckbox: CheckBox
    private lateinit var loginBtn: Button
    private lateinit var signUpBtn: TextView
    private lateinit var forgotPass: TextView
    private lateinit var selectLanguageBtn: Button

    // Referencias a elementos de error y loading
    private lateinit var loginError: TextView
    private lateinit var loadingProgress: ProgressBar
    private lateinit var loadingText: TextView

    // Variables de estado
    private var userId: Int = -1
    private var isPasswordVisible = false

    // Servicio de red
    private val apiService = RetrofitClient.instance.create(PlaceHolder::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Verificar y limpiar sesiones temporales al inicio
        checkAndClearTemporarySession()
        
        initializeLanguage()
        setContentView(R.layout.activity_login)

        initializeViews()
        setupHyperlinks()
        setupInputValidation()
        setupPasswordToggle()
        setupLoginButton()
        setupLanguageButton()
        checkRememberedCredentials()
        
        // Verificar si hay una sesión activa válida
        checkExistingSession()
    }
    
    private fun checkAndClearTemporarySession() {
        val prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val isTemporarySession = prefs.getBoolean("is_temporary_session", false)
        val appWasClosed = prefs.getBoolean("app_was_closed", false)
        
        // Si había una sesión temporal y la app se cerró, limpiar todo
        if (isTemporarySession && appWasClosed) {
            prefs.edit().apply {
                remove("user_id")
                remove("user_name")
                remove("user_email")
                remove("role_type")
                remove("credits")
                remove("jwt_token")
                remove("is_temporary_session")
                remove("app_was_closed")
                remove("remember_me")
                apply()
            }
        }
    }
    
    private fun checkExistingSession() {
        val prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val roleType = prefs.getString(ROLE_TYPE_KEY, null)
        val userId = prefs.getInt("user_id", -1)
        val isTemporarySession = prefs.getBoolean("is_temporary_session", false)
        val appWasClosed = prefs.getBoolean("app_was_closed", false)
        
        // Si hay una sesión válida y no es temporal que se cerró
        if (roleType != null && userId != -1 && !(isTemporarySession && appWasClosed)) {
            // Hay una sesión activa válida, ir directo a MainActivity
            redirectToMainActivity(roleType)
        }
    }

    private fun initializeLanguage() {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val lang = prefs.getString("app_lang", Locale.getDefault().language) ?: "es"
        setLocale(this, lang)
    }

    private fun setupLanguageButton() {
        selectLanguageBtn.setOnClickListener { showLanguageDialog() }
    }

    private fun showLanguageDialog() {
        val idiomas = arrayOf(getString(R.string.spanish), getString(R.string.english))
        val codigos = arrayOf("es", "en")
        val adapter =
                object : ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, idiomas) {
                    override fun getView(
                            position: Int,
                            convertView: View?,
                            parent: ViewGroup
                    ): View {
                        val view = super.getView(position, convertView, parent)
                        (view as TextView).setTextColor(
                                ContextCompat.getColor(context, R.color.white)
                        )
                        return view
                    }
                }

        AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setTitle(getString(R.string.language))
                .setAdapter(adapter) { _, which ->
                    setLocale(this, codigos[which])
                    recreate()
                }
                .show()
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm =
                    getSystemService(INPUT_METHOD_SERVICE) as
                            android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun setLocale(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.createConfigurationContext(config)
        val prefs = context.getSharedPreferences("settings", MODE_PRIVATE)
        prefs.edit { putString("app_lang", language) }
    }

    private fun initializeViews() {
        // Vistas principales
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input_field)
        passwordToggle = findViewById(R.id.password_toggle)
        rememberMeCheckbox = findViewById(R.id.remember_me_checkbox)
        loginBtn = findViewById(R.id.login_btn)
        signUpBtn = findViewById(R.id.newUser)
        forgotPass = findViewById(R.id.forgotPassword)
        selectLanguageBtn = findViewById(R.id.btn_select_language)

        // Elementos de error y loading
        loginError = findViewById(R.id.login_error)
        loadingProgress = findViewById(R.id.loading_progress)
        loadingText = findViewById(R.id.loading_text)
    }

    private fun setupInputValidation() {
        // Validación en tiempo real para email
        emailInput.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                    ) {}
                    override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                    ) {}
                    override fun afterTextChanged(s: Editable?) {
                        validateEmailInput(s.toString())
                    }
                }
        )

        emailInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                hideLoginError()
            } else {
                validateEmailInput(emailInput.text.toString())
            }
        }

        // Validación en tiempo real para contraseña
        passwordInput.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                    ) {}
                    override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                    ) {}
                    override fun afterTextChanged(s: Editable?) {
                        validatePasswordInput(s.toString())
                    }
                }
        )

        passwordInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                hideLoginError()
            } else {
                validatePasswordInput(passwordInput.text.toString())
            }
        }
    }
    
    private fun validateEmailInput(email: String) {
        when {
            email.isEmpty() -> {
                hideLoginError()
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showLoginError(getString(R.string.error_email_invalid))
            }
            else -> {
                hideLoginError()
            }
        }
    }
    
    private fun validatePasswordInput(password: String) {
        when {
            password.isEmpty() -> {
                hideLoginError()
            }
            password.length < MIN_PASSWORD_LENGTH -> {
                showLoginError(getString(R.string.error_password_too_short))
            }
            else -> {
                hideLoginError()
            }
        }
    }

    private fun setupPasswordToggle() {
        passwordToggle.setOnClickListener { togglePasswordVisibility() }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Ocultar contraseña
            passwordInput.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordToggle.setImageResource(android.R.drawable.ic_menu_view)
            passwordToggle.contentDescription = getString(R.string.show_password)
            isPasswordVisible = false
        } else {
            // Mostrar contraseña
            passwordInput.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordToggle.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            passwordToggle.contentDescription = getString(R.string.hide_password)
            isPasswordVisible = true
        }
        // Mantener el cursor al final del texto
        passwordInput.setSelection(passwordInput.text.length)
        
        // Aplicar el estilo de fuente nuevamente para mantener consistencia
        passwordInput.typeface = resources.getFont(R.font.righteous)
    }
    
    private fun checkRememberedCredentials() {
        val prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val rememberMe = prefs.getBoolean(REMEMBER_ME_KEY, false)
        
        rememberMeCheckbox.isChecked = rememberMe
        
        if (rememberMe) {
            val savedEmail = prefs.getString(SAVED_EMAIL_KEY, "")
            val savedPassword = prefs.getString(SAVED_PASSWORD_KEY, "")
            
            if (!savedEmail.isNullOrEmpty()) {
                emailInput.setText(savedEmail)
            }
            if (!savedPassword.isNullOrEmpty()) {
                passwordInput.setText(savedPassword)
            }
            
            // Si tenemos ambas credenciales, mostrar un hint visual
            if (!savedEmail.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
                showToast("Credenciales recordadas cargadas")
            }
        }
    }

    private fun saveCredentialsIfNeeded(credentials: LoginCredentials) {
        val prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val editor = prefs.edit()

        if (rememberMeCheckbox.isChecked) {
            editor.putBoolean(REMEMBER_ME_KEY, true)
            editor.putString(SAVED_EMAIL_KEY, credentials.email)
            editor.putString(SAVED_PASSWORD_KEY, credentials.password)
        } else {
            editor.putBoolean(REMEMBER_ME_KEY, false)
            editor.remove(SAVED_EMAIL_KEY)
            editor.remove(SAVED_PASSWORD_KEY)
            
            // También limpiar cualquier sesión previa si no se quiere recordar
            clearPreviousSession()
        }
        editor.apply()
    }
    
    private fun clearPreviousSession() {
        val prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        prefs.edit().apply {
            // Solo limpiar credenciales guardadas, NO los datos de sesión activa
            remove(SAVED_EMAIL_KEY)
            remove(SAVED_PASSWORD_KEY)
            putBoolean(REMEMBER_ME_KEY, false)
            apply()
        }
    }

    private fun setupLoginButton() {
        loginBtn.setOnClickListener { handleLoginButtonClick() }
    }

    private fun handleLoginButtonClick() {
        hideKeyboard()

        val credentials = getLoginCredentials()
        if (!validateCredentials(credentials)) {
            return
        }

        // Guardar credenciales si está marcado "Recordar sesión"
        saveCredentialsIfNeeded(credentials)

        showLoadingState()
        loginUser(credentials)
    }

    private fun getLoginCredentials(): LoginCredentials {
        return LoginCredentials(
                email = emailInput.text.toString().trim(),
                password = passwordInput.text.toString().trim()
        )
    }

    private fun validateCredentials(credentials: LoginCredentials): Boolean {
        when {
            credentials.email.isEmpty() -> {
                showLoginError(getString(R.string.error_email_empty))
                emailInput.requestFocus()
                return false
            }
            credentials.password.isEmpty() -> {
                showLoginError(getString(R.string.error_password_empty))
                passwordInput.requestFocus()
                return false
            }
            !Patterns.EMAIL_ADDRESS.matcher(credentials.email).matches() -> {
                showLoginError(getString(R.string.error_email_invalid))
                emailInput.requestFocus()
                return false
            }
            credentials.password.length < MIN_PASSWORD_LENGTH -> {
                showLoginError(getString(R.string.error_password_too_short))
                passwordInput.requestFocus()
                return false
            }
            else -> {
                hideLoginError()
                return true
            }
        }
    }

    // Métodos para manejar el estado de loading
    private fun showLoadingState() {
        disableLoginButton()
        loadingProgress.visibility = View.VISIBLE
        loadingText.visibility = View.VISIBLE
        loadingText.text = getString(R.string.loading_signing_in)
    }

    private fun hideLoadingState() {
        enableLoginButton()
        loadingProgress.visibility = View.GONE
        loadingText.visibility = View.GONE
    }

    private fun disableLoginButton() {
        loginBtn.isEnabled = false
    }

    private fun enableLoginButton() {
        loginBtn.isEnabled = true
    }

    // Métodos para mostrar/ocultar errores
    private fun showLoginError(message: String) {
        if (loginError.visibility != View.VISIBLE) {
            loginError.alpha = 0f
            loginError.visibility = View.VISIBLE
            loginError.animate()
                .alpha(1f)
                .setDuration(200)
                .start()
        }
        loginError.text = message
    }
    
    private fun hideLoginError() {
        if (loginError.visibility == View.VISIBLE) {
            loginError.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    loginError.visibility = View.GONE
                    loginError.text = ""
                }
                .start()
        }
    }

    private fun setupHyperlinks() {
        val signUpText = signUpBtn.text.toString()
        val signUpClickable = getString(R.string.sign_up_clickable)
        val signUpStart = signUpText.indexOf(signUpClickable)
        val signUpEnd = signUpStart + signUpClickable.length
        if (signUpStart >= 0 && signUpEnd <= signUpText.length) {
            val signUpSpannable = SpannableString(signUpText)
            val signUpClickableSpan =
                    object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            navigateToRegister()
                        }
                    }
            val colorSpan = ForegroundColorSpan(ContextCompat.getColor(this, R.color.green))
            val underlineSpan = UnderlineSpan()

            signUpSpannable.setSpan(
                    signUpClickableSpan,
                    signUpStart,
                    signUpEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            signUpSpannable.setSpan(
                    colorSpan,
                    signUpStart,
                    signUpEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            signUpSpannable.setSpan(
                    underlineSpan,
                    signUpStart,
                    signUpEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            signUpBtn.text = signUpSpannable
            signUpBtn.movementMethod = LinkMovementMethod.getInstance()
        }

        val forgotPassText = forgotPass.text.toString()
        val forgotPassClickable = getString(R.string.forgot_password_clickable)
        val forgotPassStart = forgotPassText.indexOf(forgotPassClickable)
        val forgotPassEnd = forgotPassStart + forgotPassClickable.length
        if (forgotPassStart >= 0 && forgotPassEnd <= forgotPassText.length) {
            val forgotPassSpannable = SpannableString(forgotPassText)
            val forgotPassClickableSpan =
                    object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            val intent =
                                    Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
                            startActivity(intent)
                        }
                    }
            forgotPassSpannable.setSpan(
                    forgotPassClickableSpan,
                    forgotPassStart,
                    forgotPassEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            forgotPass.text = forgotPassSpannable
            forgotPass.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun loginUser(credentials: LoginCredentials) {
        val loginRequest = LoginRequest(credentials.email, credentials.password)

        lifecycleScope.launch {
            try {
                val response =
                        withTimeout(NETWORK_TIMEOUT_MS) {
                            withContext(Dispatchers.IO) { apiService.loginUser(loginRequest) }
                        }
                handleLoginResponse(response.isSuccessful, response.body())
            } catch (e: Exception) {
                handleLoginError(e)
            }
        }
    }

    private fun handleLoginResponse(isSuccessful: Boolean, userResponse: Any?) {
        hideLoadingState()

        if (isSuccessful && userResponse != null) {
            handleSuccessfulLogin(userResponse)
        } else {
            showLoginError(getString(R.string.error_login_invalid_credentials))
        }
    }

    private fun handleSuccessfulLogin(userResponse: Any) {
        try {
            // Asumiendo que userResponse tiene una propiedad id
            val userResponseReflection = userResponse.javaClass
            val idField = userResponseReflection.getDeclaredField("id")
            idField.isAccessible = true
            userId = idField.getInt(userResponse)

            // SIEMPRE guardar el user_id durante la sesión activa
            val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
            prefs.edit { putInt("user_id", userId) }

            showToast(getString(R.string.success_login))
            getUserInfo()
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error processing user response", e)
            showLoginError(getString(R.string.error_network_general, e.message))
        }
    }

    private fun handleLoginError(exception: Exception) {
        hideLoadingState()

        val errorMessage =
                when (exception) {
                    is SocketTimeoutException,
                    is kotlinx.coroutines.TimeoutCancellationException -> {
                        getString(R.string.error_network_timeout)
                    }
                    is UnknownHostException -> {
                        getString(R.string.error_network_timeout)
                    }
                    else -> {
                        Log.e("LoginActivity", "Login error: ${exception.message}", exception)
                        getString(
                                R.string.error_network_general,
                                exception.message ?: "Unknown error"
                        )
                    }
                }

        showLoginError(errorMessage)
    }

    private fun getUserInfo() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { apiService.getUserId() }

                if (response.isSuccessful) {
                    response.body()?.let { user -> handleUserInfoSuccess(response, user) }
                } else {
                    Log.e("LoginActivity", "Error al obtener usuario: ${response.code()}")
                    showLoginError(getString(R.string.error_network_general, "User info error"))
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Error de red al obtener usuario", e)
                showLoginError(getString(R.string.error_network_general, e.message))
            }
        }
    }

    private fun handleUserInfoSuccess(response: retrofit2.Response<*>, user: Any) {
        try {
            val jwtToken = response.headers()["Set-Cookie"]?.let { extractJwtToken(it) }
            
            // Usar reflexión para acceder a las propiedades del usuario
            val userClass = user.javaClass
            val nameField = userClass.getDeclaredField("name")
            val emailField = userClass.getDeclaredField("email")
            val roleTypeField = userClass.getDeclaredField("roleType")
            val creditsField = userClass.getDeclaredField("credits")

            nameField.isAccessible = true
            emailField.isAccessible = true
            roleTypeField.isAccessible = true
            creditsField.isAccessible = true

            val userName = nameField.get(user) as String
            val userEmail = emailField.get(user) as String
            val roleType = roleTypeField.get(user) as String
            val credits = creditsField.get(user)

            // SIEMPRE guardar todos los datos durante la sesión activa
            jwtToken?.let { saveToSharedPreferences(JWT_TOKEN_KEY, it) }
            saveUserDataToPreferences(userName, userEmail, roleType, credits?.toString() ?: "0")
            
            // Guardar estado de sesión: si es temporal y si el usuario quiere recordar
            val prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
            prefs.edit().apply {
                putBoolean("is_temporary_session", !rememberMeCheckbox.isChecked)
                putBoolean(REMEMBER_ME_KEY, rememberMeCheckbox.isChecked)
                apply()
            }
            
            // Siempre redirigir con toda la información disponible
            redirectToMainActivity(roleType)
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error processing user info", e)
            showLoginError(getString(R.string.error_network_general, e.message))
        }
    }

    private fun saveUserDataToPreferences(name: String,email: String,roleType: String,credits: String) {
        saveToSharedPreferences("user_name", name)
        saveToSharedPreferences("user_email", email)
        saveToSharedPreferences(ROLE_TYPE_KEY, roleType)
        saveToSharedPreferences(USER_CREDITS, credits)
        Log.d("PasoDeCreditos", "LoginActivity - Créditos obtenidos: $credits")
    }

    private fun extractJwtToken(cookieHeader: String): String? {
        val jwtRegex = "JWT_TOKEN=([^;]+)".toRegex()
        return jwtRegex.find(cookieHeader)?.groupValues?.get(1)
    }

    private fun saveToSharedPreferences(key: String, value: String) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString(key, value)
            apply()
        }
    }

    private fun redirectToMainActivity(roleType: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("ROLE_TYPE", roleType)
        startActivity(intent)
        finish()
    }

    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}
