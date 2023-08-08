package com.example.twitterclone.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.twitterclone.databinding.ActivityLoginBinding
import com.example.twitterclone.util.DATABASE_URL
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid
        user?.let {
            startActivity(HomeActivity.newIntent(this))
            finish()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnLogin.setOnClickListener { onLogin() }
        binding.tvSignup.setOnClickListener { goToSignup() }

        setTextChangeListener(binding.etEmail, binding.tilEmail)
        setTextChangeListener(binding.etPassword, binding.tilPassword)
        binding.loginProgressLayout.setOnTouchListener { view, motionEvent ->
            true
        }

    }

    private fun setTextChangeListener(et: EditText, til: TextInputLayout) {
        et.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                til.isErrorEnabled = false
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    private fun onLogin() = with(binding) {
        var proceed = true
        if (etEmail.text.isNullOrEmpty()) {
            tilEmail.error = "Email is required"
            tilEmail.isErrorEnabled = true
            proceed = false
        }
        if (etPassword.text.isNullOrEmpty()) {
            tilPassword.error = "Password is required"
            tilPassword.isErrorEnabled = true
            proceed = false
        }
        if (proceed) {
            loginProgressLayout.visibility = View.VISIBLE
            firebaseAuth.signInWithEmailAndPassword(
                etEmail.text.toString(), etPassword.text.toString()
            )
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        loginProgressLayout.visibility = View.GONE
                        Toast.makeText(
                            this@LoginActivity,
                            "Login error: ${task.exception?.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    loginProgressLayout.visibility = View.GONE
                }
        }
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(firebaseAuthListener)
    }

    private fun goToSignup() {
        startActivity(SignupActivity.newIntent(this))
        finish()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, LoginActivity::class.java)
    }
}