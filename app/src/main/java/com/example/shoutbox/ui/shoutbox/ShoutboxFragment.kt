package com.example.shoutbox.ui.shoutbox

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.shoutbox.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ShoutboxFragment : Fragment(), CustomAdapter.OnItemClickListener {

    private lateinit var login: String
    private var flag: Int = 0
    private lateinit var content: String
    private lateinit var jsonPlaceholderAPI: JsonPlaceHolderAPI
    private lateinit var recyclerView: RecyclerView
    private lateinit var navController: NavController
    var postList: MutableList<Post>? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_shoutbox, container, false)

        val textContent: EditText = root.findViewById(R.id.editTextContent)
        val sendButton: Button = root.findViewById(R.id.buttonSendData)
        recyclerView = root.findViewById(R.id.recycler_view)
        val pullToRefresh: SwipeRefreshLayout = root.findViewById(R.id.pull_to_refresh)


        flag = requireArguments().getInt("flag")
        login = requireArguments().getString("login").toString()

        if (flag == 0) saveLogin()
        if (flag == 1) {                // flag = 1 gdy powróciło się do fragmentu z edycji
            login = loadLogin().toString()
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("http://tgryl.pl/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        jsonPlaceholderAPI = retrofit.create(JsonPlaceHolderAPI::class.java)

        showPosts()

        sendButton.setOnClickListener {
            content = textContent.text.toString()
            sendData()
            textContent.setText("")
        }

        automaticRefresh()


        pullToRefresh.setOnRefreshListener {
            showPosts()
            Handler().postDelayed({
                pullToRefresh.isRefreshing = false
            }, 500)
        }

        recyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = postList?.let { CustomAdapter(it, this) }

        val swipeHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = recyclerView.adapter as CustomAdapter
                val position = viewHolder.adapterPosition
                val message: Post? = postList?.get(position)
                val id = message?.getId()
                adapter.removeAt(position)
                if(MainActivity().isConnectedToNetwork(requireContext())) {
                    val call: Call<Void> = jsonPlaceholderAPI.deletePost(id.toString())
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
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
    }

    private fun showPosts() {
        if(MainActivity().isConnectedToNetwork(requireContext())) {
            val call: Call<MutableList<Post>> = jsonPlaceholderAPI.getPosts()
            call.enqueue(object : Callback<MutableList<Post>> {
                override fun onResponse(call: Call<MutableList<Post>>, response: Response<MutableList<Post>>) {
                    try {
                        val posts: MutableList<Post> = response.body()!!
                        postList = posts
                        recyclerView.adapter = CustomAdapter(posts, this@ShoutboxFragment)
                        recyclerView.layoutManager = LinearLayoutManager(activity)
                        recyclerView.setHasFixedSize(true)
                    } catch (e: NullPointerException) {
                        println(e.stackTrace)
                    }
                }
                override fun onFailure(call: Call<MutableList<Post>>, t: Throwable) {
                    println(t.message)
                }
            })
        } else {
            Toast.makeText(context, "Brak Internetu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendData() {
        val post = Post(content, login)
        if(MainActivity().isConnectedToNetwork(requireContext())) {
            val call: Call<Post> = jsonPlaceholderAPI.createPost(post)
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

    override fun onItemClick(message: Post) {
        val loginMessage = message.getLogin()
        val date = message.getDate()
        val content = message.getContent()
        val id = message.getId()
        val bundle = bundleOf(
            "login" to loginMessage,
            "date" to date,
            "content" to content,
            "id" to id,
            "loginAuthorisation" to login
        )
        navController.navigate(R.id.action_nav_shoutbox_to_editingFragment, bundle)
    }

    private fun saveLogin() {
        val sharedPreferences =
            requireContext().getSharedPreferences("SavedLogin", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("LOGIN_KEY", login)
        editor.apply()
    }

    private fun loadLogin(): String? {
        val sharedPreferences: SharedPreferences =
            requireContext().getSharedPreferences("SavedLogin", Context.MODE_PRIVATE)
        val loginText = sharedPreferences.getString("LOGIN_KEY", "")
        return loginText
    }

    private fun automaticRefresh() {
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                if(context != null) {       // Po wyjściu z fragmentu odświeżanie się nie odbywa
                    showPosts()
                    handler.postDelayed(this, 60000) // 60s
                }
            }
        }, 6000)
    }

}
