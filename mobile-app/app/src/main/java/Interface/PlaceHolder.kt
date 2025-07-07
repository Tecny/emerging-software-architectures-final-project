package Interface

import Beans.auth.forgotPassword.ForgotPasswordRequest
import Beans.auth.login.LoginRequest
import Beans.auth.login.LoginResponse
import Beans.auth.register.RegisterRequest
import Beans.availability.AvailabilityResponse
import Beans.chat.ChatMessage
import Beans.chat.MessageRecieve
import Beans.playerList.Player
import Beans.playerList.PlayerList
import Beans.reservations.Reservation
import Beans.rooms.GameRoom
import Beans.sportspaces.SportSpace
import Beans.suscription.Suscriptions
import Beans.tickets.CreateTicketRequest
import Beans.tickets.Tickets
import Beans.update.UpdateEmailRequest
import Beans.update.UpdateNameRequest
import Beans.update.UpdatePasswordRequest
import Beans.userProfile.UserProfile
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface PlaceHolder {
    // Authentication
    @POST("api/v1/authentication/sign-in")
    suspend fun loginUser(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("api/v1/authentication/log-out")
    fun logOutUser(): Call<Void>

    @POST("api/v1/users/sign-up")
    suspend fun createUser(@Body registerRequest: RegisterRequest): Response<UserProfile>

    @GET("api/v1/users/me")
    suspend fun getUserId(): Response<UserProfile>

    @PUT("api/v1/users/email")
    fun updateEmail(@Body emailRequest: UpdateEmailRequest): Call<ResponseBody>

    @PUT("api/v1/users/password")
    fun updatePassword(@Body passwordRequest: UpdatePasswordRequest): Call<ResponseBody>

    @PUT("api/v1/users/name")
    fun updateName(@Body nameRequest: UpdateNameRequest): Call<ResponseBody>

    // Sport Spaces
    @GET("api/v1/sport-spaces/all")
    fun getAllSportSpaces(): Call<List<SportSpace>>

    @GET("api/v1/sport-spaces/my-space")
    fun getSportSpacesByUserId(): Call<List<SportSpace>>

    @GET("api/v1/sport-spaces/{id}")
    fun getSportSpaceById(@Path("id") id: Int): Call<SportSpace>

    @Multipart
    @POST("api/v1/sport-spaces/create")
    fun createSportSpace(
        @Part("name") name: RequestBody,
        @Part("sportId") sportId: RequestBody,
        @Part image: MultipartBody.Part,
        @Part("price") price: RequestBody,
        @Part("address") address: RequestBody,
        @Part("description") description: RequestBody,
        @Part("openTime") openTime: RequestBody,
        @Part("closeTime") closeTime: RequestBody,
        @Part("gamemodeId") gamemodeId: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody
    ): Call<ResponseBody>

    @GET("api/v1/sport-spaces/{id}/availability")
    fun getSportSpaceAvailability(@Path("id") id: Int): Call<AvailabilityResponse>
    
    // Game Rooms
    @GET("api/v1/rooms/all")
    fun getAllRooms(): Call<List<GameRoom>>

    @GET("api/v1/rooms/{id}")
    fun getRoomById(@Path("id") id: Int): Call<GameRoom>

    //Reservations
    @POST("/api/v1/reservations/create")
    fun createReservation(
        @Body reservationRequest: Reservation
    ): Call<ResponseBody>

    @GET("/api/v1/reservations/my-reservations")
    fun getMyReservations(): Call<List<Reservation>>

    //player-list
    @POST("/api/v1/player-lists/join/{roomId}")
    fun joinRoom(
        @Path("roomId") roomId: Int
    ): Call<Void>

    @GET("/api/v1/player-lists/room/{roomId}")
    fun getPlayerListByRoomId(@Path("roomId") roomId: Int): Call<List<Player>>

    @GET("/api/v1/player-lists/{roomId}/user-room-status")
    fun getUserRoomStatus(
        @Path("roomId") roomId: Int
    ): Call<PlayerList>

    @DELETE("/api/v1/player-lists/leave/{roomId}")
    fun leaveRoom(
        @Path("roomId") roomId: Int
    ): Call<Void>

    //subscriptions
    @GET("api/v1/subscriptions")
    fun getCurrentSubscription(): Call<Suscriptions>

    @PUT("api/v1/subscriptions/upgrade")
    fun upgradeSubscription(@Query("newPlanType") newPlanType: String): Call<ResponseBody>

    //chat
    @GET("/api/v1/chat/rooms/{chatRoomId}/messages")
    fun getMessages(@Path("chatRoomId") chatRoomId: Int): Call<List<ChatMessage>>

    @POST("api/v1/chat/rooms/{roomId}/messages")
    fun sendMessage(
        @Path("roomId") roomId: Int,
        @Query("userId") userId: Int,
        @Body chatMessage: MessageRecieve
    ): Call<Void>

    //password recovery
    @POST("/api/v1/recover-password/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ResponseBody>

    //bank transfer
    @POST("/api/v1/bank-transfer/create")
    suspend fun createBankTransfer(@Body ticket: CreateTicketRequest): Response<Void>

    @GET("api/v1/bank-transfer/owner")
    suspend fun getBankTransfers(): List<Tickets>

    //deposits
    @POST("/api/v1/deposit/create-deposit")
    fun createDeposit(
        @Query("amount") amount: Int
    ): Call<ResponseBody>

    //qr
    @GET("api/v1/reservations/generate-qr-session")
    fun generateQrToken(@Query("reservationId") reservationId: Int): Call<Map<String, String>>

    @GET("api/v1/reservations/verify-qr-image")
    fun generateQrImage(@Query("token") token: String): Call<ResponseBody>
}