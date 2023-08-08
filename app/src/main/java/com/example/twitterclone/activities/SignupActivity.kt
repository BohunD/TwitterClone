package com.example.twitterclone.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import com.example.twitterclone.databinding.ActivitySignupBinding
import com.example.twitterclone.util.DATA_USERS
import com.example.twitterclone.util.User
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid
        user?.let{
            startActivity(HomeActivity.newIntent(this))
            finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTextChangeListener(binding.etUsername, binding.tilUsername)
        setTextChangeListener(binding.etEmail, binding.tilEmail)
        setTextChangeListener(binding.etPassword, binding.tilPassword)
        binding.btnSignup.setOnClickListener {
            onSignUp()
        }
        binding.tvSignin.setOnClickListener {
            goToLogin()
        }

    }

    private fun setTextChangeListener(et: EditText, til: TextInputLayout) {
        et.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                til.isErrorEnabled = false
                binding.tvError.visibility = View.INVISIBLE
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    private fun onSignUp() = with(binding) {
        var proceed = true
        if (etUsername.text.isNullOrEmpty()) {
            tilUsername.error = "Username is required"
            tilUsername.isErrorEnabled = true
            proceed = false
        }
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
            signupProgressLayout.visibility = View.VISIBLE
            firebaseAuth.createUserWithEmailAndPassword(
                etEmail.text.toString(), etPassword.text.toString()
            )
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        binding.tvError.text = "Signup error: ${task.exception?.localizedMessage}"
                        binding.tvError.visibility = View.VISIBLE
                    }
                    else{
                        val email = etEmail.text.toString()
                        val name = etUsername.text.toString()
                        val user = User(
                            email,
                            name,
                            "",
                            arrayListOf(),
                            arrayListOf()
                        )
                        firebaseDB.collection(DATA_USERS).document(firebaseAuth.uid!!).set(user)
                    }
                    signupProgressLayout.visibility = View.GONE
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    signupProgressLayout.visibility = View.GONE
                }
        }
    }

    private fun goToLogin() {
        startActivity(LoginActivity.newIntent(this))
        finish()
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(firebaseAuthListener)
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, SignupActivity::class.java)
    }
}