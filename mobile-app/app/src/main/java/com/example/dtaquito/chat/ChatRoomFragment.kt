package com.example.dtaquito.chat

import Beans.chat.ChatMessage
import Beans.chat.MessageRecieve
import Beans.playerList.Player
import Interface.PlaceHolder
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import com.example.dtaquito.utils.showToast
import com.google.gson.GsonBuilder
import network.RetrofitClient
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatRoomFragment : Fragment() {

    private lateinit var service: PlaceHolder
    private lateinit var chatView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private var userId: Int = -1
    private var webSocket: WebSocket? = null
    private var gameRoomId: Int = -1
    private lateinit var prefs: SharedPreferences
    private var shouldAutoScroll = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            gameRoomId = it.getInt("GAME_ROOM_ID", -1)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        RetrofitClient.initialize(context.applicationContext)

        prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        service = RetrofitClient.instance.create(PlaceHolder::class.java)

        userId = prefs.getInt("user_id", -1)
        Log.d("ChatRoomFragment", "User ID: $userId")
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_chat_room, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatView = view.findViewById(R.id.chatView)
        chatAdapter = ChatAdapter(userId)

        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        chatView.layoutManager = layoutManager
        chatView.adapter = chatAdapter

        // Detectar cuando el usuario hace scroll manual
        chatView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
                val totalItems = chatAdapter.itemCount

                // Si el usuario no está al final, no hacer auto-scroll
                shouldAutoScroll = lastVisibleItem >= totalItems - 1
            }
        })

        val sendButton = view.findViewById<ImageButton>(R.id.sendButton)
        val messageInput = view.findViewById<EditText>(R.id.messageInput)

        if (gameRoomId != -1) {
            fetchMessages(gameRoomId)
            fetchPlayerListByRoomId(gameRoomId)
        } else {
            requireContext().showToast("Invalid game room ID")
        }

        sendButton.setOnClickListener {
            val message = messageInput.text.toString()
            if (message.isNotEmpty()) {
                sendMessage(gameRoomId, userId, message)
                messageInput.text.clear()
                shouldAutoScroll = true // Permitir auto-scroll cuando el usuario envía mensaje
            }
        }

        messageInput.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                val message = messageInput.text.toString()
                if (message.isNotEmpty()) {
                    sendMessage(gameRoomId, userId, message)
                    messageInput.text.clear()
                    shouldAutoScroll = true
                }
                return@OnEditorActionListener true
            }
            false
        })
    }

    override fun onStart() {
        super.onStart()
        if (gameRoomId != -1 && webSocket == null) {
            setupWebSocket(gameRoomId)
        }
    }

    override fun onStop() {
        super.onStop()
        webSocket?.close(1000, null)
        webSocket = null
    }


    private fun fetchPlayerListByRoomId(roomId: Int) {
        service.getPlayerListByRoomId(roomId).enqueue(object : Callback<List<Player>> {
            override fun onResponse(call: Call<List<Player>>, response: Response<List<Player>>) {
                if (response.isSuccessful) {
                    response.body()?.let { playerList ->
                        val isUserInList = playerList.any { it.id == userId }
                        Log.d("ChatRoomFragment", "Player list for room $isUserInList")
                        if (isUserInList) {
                            setupWebSocket(roomId)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<Player>>, t: Throwable) {
                logAndShowError("Error: ${t.message}")
            }
        })
    }

    private fun fetchMessages(roomId: Int) {
        Log.d("ChatRoomFragment", "Fetching messages for room $roomId")
        service.getMessages(roomId).enqueue(object : Callback<List<ChatMessage>> {
            override fun onResponse(call: Call<List<ChatMessage>>, response: Response<List<ChatMessage>>) {
                Log.d("ChatRoomFragment", "Response received: ${response.code()}")
                if (response.isSuccessful) {
                    response.body()?.let { messages ->
                        Log.d("ChatRoomFragment", "Messages for room $roomId: $messages")
                        chatAdapter.setMessages(messages)
                        // Solo hacer scroll al cargar mensajes iniciales
                        if (messages.isNotEmpty()) {
                            chatView.scrollToPosition(chatAdapter.itemCount - 1)
                        }
                    } ?: run {
                        Log.e("ChatRoomFragment", "Response body is null")
                        context?.showToast("Failed to fetch messages")
                    }
                } else {
                    Log.e("ChatRoomFragment", "Failed to fetch messages: ${response.errorBody()?.string()}")
                    context?.showToast("Failed to fetch messages")
                }
            }

            override fun onFailure(call: Call<List<ChatMessage>>, t: Throwable) {
                Log.e("ChatRoomFragment", "Error fetching messages: ${t.message}")
                logAndShowError("Error: ${t.message}")
            }
        })
    }


    private fun sendMessage(roomId: Int, userId: Int, message: String) {
        service.sendMessage(roomId, userId, MessageRecieve(content = message)).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {

            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                logAndShowError("Error: ${t.message}")
            }
        })
        val chatMessage = ChatMessage(
            content = message,
            userId = userId,
            userName = "unname",
            createdAt = "",
            roomId = roomId
        )
        val gson = GsonBuilder().create()
        webSocket?.send(gson.toJson(chatMessage))
    }
    private fun setupWebSocket(roomId: Int) {
        if (webSocket != null) return

        val request = Request.Builder()
            .url(RetrofitClient.CHAT_URL)
            .build()

        val client = OkHttpClient()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                activity?.runOnUiThread {
                    Log.d("WebSocket", "Connected — suscribiéndome a la sala $roomId")
                }
                val subscribeJson = """
                {
                  "action": "subscribe",
                  "roomId": $roomId
                }
            """.trimIndent()
                webSocket.send(subscribeJson)
            }

            val gson = GsonBuilder()
                .registerTypeAdapter(ChatMessage::class.java, ChatMessageDeserializer())
                .create()

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Message received: $text")
                try {
                    val chatMessage = gson.fromJson(text, ChatMessage::class.java)

                    activity?.runOnUiThread {
                        if (isAdded) {
                            chatAdapter.addMessage(chatMessage)
                            // Solo hacer auto-scroll si el usuario está al final del chat
                            if (shouldAutoScroll) {
                                chatView.scrollToPosition(chatAdapter.itemCount - 1)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Parsing Error", "Error parsing message: ${e.message}")
                }
            }


            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                activity?.runOnUiThread {
                    Log.e("WebSocket", "Error: ${t.message}")
                }
            }
        })
    }

    private fun logAndShowError(message: String) {
        Log.e("ChatRoomFragment", message)
        context?.showToast(message)
    }
}