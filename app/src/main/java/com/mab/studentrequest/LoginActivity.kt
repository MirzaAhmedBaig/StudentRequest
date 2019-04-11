package com.mab.studentrequest

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val TAG = LoginActivity::class.java.simpleName
    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setListeners()
    }

    public override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null)
            gotoMainActivity()
    }

    private fun setListeners() {
        login_button.setOnClickListener {
            if (performValidation()) {
                loginUser()
            }
        }

        register_button.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun performValidation(): Boolean {
        if (!email.text.toString().isEmailValid()) {
            email.requestFocus()
            email.error = "Enter valid email"
            return false
        }
        if (password.text.isBlank() || password.text.length < 6) {
            password.requestFocus()
            password.error = "Enter valid password min 6 char"
            return false
        }
        return true
    }


    private fun gotoMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
    }


    private fun loginUser() {
        loader.visibility = View.VISIBLE
        firebaseAuth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
            .addOnCompleteListener(this) { task ->
                loader.visibility = View.GONE
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    val user = firebaseAuth.currentUser
                    gotoMainActivity()
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }
}
