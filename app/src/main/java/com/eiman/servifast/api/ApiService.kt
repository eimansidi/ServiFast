package com.eiman.servifast.api

import com.eiman.servifast.api.models.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("login.php")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("register.php")
    fun register(@Body request: RegisterRequest): Call<GenericResponse>

    @POST("reset_password.php")
    fun resetPassword(@Body request: ResetPasswordRequest): Call<GenericResponse>

    @POST("check_user_exists.php")
    fun checkUserExists(@Body request: UserCheckRequest): Call<UserCheckResponse>

    @POST("change_password.php")
    fun changePassword(@Body request: ChangePasswordRequest): Call<GenericResponse>

    @POST("update_user.php")
    fun updateUser(@Body request: UpdateUserRequest): Call<GenericResponse>

    @POST("get_user_rating.php")
    fun getUserRating(@Body request: UserRatingRequest): Call<UserRatingResponse>
}
