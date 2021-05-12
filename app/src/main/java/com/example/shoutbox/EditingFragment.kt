package com.example.shoutbox

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class EditingFragment : Fragment(), View.OnClickListener {

    private lateinit var navController: NavController
    private lateinit var jsonPlaceholderAPI: JsonPlaceHolderAPI
    private lateinit var login: String
    private lateinit var loginAuthorisation: String
    private lateinit var content: String
    lateinit var id: String
    private lateinit var textContent: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_editing, container, false)

        loginAuthorisation = requireArguments().getString("loginAuthorisation").toString()
        val date = requireArguments().getString("date").toString()
        login = requireArguments().getString("login").toString()
        content = requireArguments().getString("content").toString()
        id = requireArguments().getString("id").toString()

        val textLogin = root.findViewById<TextView>(R.id.textView_login)
        val textDate = root.findViewById<TextView>(R.id.textView_date)
        textContent = root.findViewById(R.id.editText_content)
        val buttonUpdate = root.findViewById<Button>(R.id.buttonUpdate)


        textLogin.text = login
        textDate.text = (date.replace("T", " ")).substring(0, 19)
        textContent.setText(content)

        if (login != loginAuthorisation) {
            buttonUpdate.isEnabled = false              // autoryzacja użytkownika
            textContent.isEnabled = false
        }


        val retrofit = Retrofit.Builder()
            .baseUrl("http://tgryl.pl/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        jsonPlaceholderAPI = retrofit.create(JsonPlaceHolderAPI::class.java)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (login == loginAuthorisation) {
            setHasOptionsMenu(true)
        }

        navController = Navigation.findNavController(view)
        view.findViewById<Button>(R.id.buttonUpdate).setOnClickListener(this)
        view.findViewById<Button>(R.id.buttonBack).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.buttonUpdate -> {
                content = textContent.text.toString()
                updatePost()
                val flag = 1
                val bundle = bundleOf("flag" to flag)
                navController.navigate(R.id.action_nav_editing_to_nav_shoutbox, bundle)
            }
            R.id.buttonBack -> {
                requireActivity().onBackPressed()
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.shoutbox_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId;
        if (id == R.id.action_delete) {
            deletePost()
            val flag = 1
            val bundle = bundleOf("flag" to flag)
            navController.navigate(R.id.action_nav_editing_to_nav_shoutbox, bundle)
            return true;
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deletePost() {
        if(MainActivity().isConnectedToNetwork(requireContext())) {
            val call: Call<Void> = jsonPlaceholderAPI.deletePost(id)
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (!response.isSuccessful) {
                        println("Code: " + response.code())
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    println(t.message)
                }
            })
        } else {
            Toast.makeText(context, "Brak Internetu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePost() {
        val post: Post = Post(content, login)
        if(MainActivity().isConnectedToNetwork(requireContext())) {
            val call: Call<Post> = jsonPlaceholderAPI.putPost(id, post)
            call.enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    if (!response.isSuccessful) {
                        println("Code: " + response.code())
                    }
                }

                override fun onFailure(call: Call<Post>, t: Throwable) {
                    println(t.message)
                }
            })
        } else {
            Toast.makeText(context, "Brak Internetu", Toast.LENGTH_SHORT).show()
        }
    }

}
