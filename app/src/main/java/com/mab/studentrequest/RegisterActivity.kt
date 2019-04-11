package com.mab.studentrequest

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private val TAG = RegisterActivity::class.java.simpleName
    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setListeners()
    }

    private fun setListeners() {
        login_button.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        register_button.setOnClickListener {
            if (performValidation()) {
                registerUser()
            }
        }
    }

    private fun performValidation(): Boolean {
        if (!email.text.toString().isEmailValid()) {
            email.requestFocus()
            email.error = "Enter valid email"
            return false
        }
        if (password.text.isBlank()|| password.text.length < 6) {
            password.requestFocus()
            password.error = "Enter valid password min 6 char"
            return false
        }
        return true
    }


    private fun registerUser() {
        loader.visibility = View.VISIBLE
        firebaseAuth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    loader.visibility = View.GONE
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = firebaseAuth.currentUser
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                } else {
                    loader.visibility = View.GONE
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }
}
