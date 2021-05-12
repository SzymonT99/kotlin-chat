package com.example.shoutbox.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.shoutbox.MainActivity
import com.example.shoutbox.R
import kotlinx.android.synthetic.main.fragment_settings.*


class SettingsFragment : Fragment(), View.OnClickListener {

    private lateinit var navController: NavController
    private lateinit var textLogin: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root: View = inflater.inflate(R.layout.fragment_settings, container, false)
        textLogin = root.findViewById(R.id.editTextLogin)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textLogin.setText(loadData())
        navController = Navigation.findNavController(view)

        view.findViewById<Button>(R.id.buttonSetLogin).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.buttonSetLogin -> {
                if(!TextUtils.isEmpty(editTextLogin.text.toString())) {
                    if(MainActivity().isConnectedToNetwork(requireContext())) {
                        val login = editTextLogin.text.toString()
                        saveData(login)
                        val bundle = bundleOf("login" to login)
                        navController.navigate(R.id.action_nav_settings_to_nav_shoutbox, bundle)
                    }
                        else{
                        Toast.makeText(context, "Brak Internetu", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun saveData(output: String) {
        val sharedPreferences = requireContext().getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("LOGIN_KEY", output)
        editor.apply()
    }

    private fun loadData(): String? {
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val loginText = sharedPreferences.getString("LOGIN_KEY", "")
        return loginText
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
    }

}
