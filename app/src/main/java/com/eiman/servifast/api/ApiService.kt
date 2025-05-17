package com.eiman.servifast.api

import com.eiman.servifast.api.items.*
import com.eiman.servifast.api.models.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
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

    @POST("get_user.php")
    fun getUserProfile(@Body request: UserProfileRequest): Call<UserProfileResponse>

    @POST("get_user_ratings.php")
    fun getUserRatings(@Body request: UserRatingListRequest): Call<UserRatingsResponse>

    @POST("create_service.php")
    fun createService(@Body req: CreateServiceRequest): Call<GenericResponse>

    @GET("get_categories.php")
    fun getCategories(): Call<List<CategoryResponse>>

    @POST("get_services.php")
    fun getServices(): Call<List<ServicePostItem>>

    @POST("get_latest_services.php")
    fun getLatestServices(): Call<List<ServicePostItem>>

    @GET("get_small_services.php")
    fun getSmallServices(): Call<List<SmallPostItem>>

    @POST("add_favorite.php")
    fun addFavorite(@Body req: FavoriteRequest): Call<GenericResponse>

    @POST("remove_favorite.php")
    fun removeFavorite(@Body req: FavoriteRequest): Call<GenericResponse>

    @POST("get_users.php")
    fun getUsers(): Call<List<UserItem>>

    @POST("filter_services.php")
    fun filterServices(@Body criteria: FilterRequest): Call<List<ServicePostItem>>

    @POST("search_services.php")
    fun searchServices(@Body query: SearchRequest): Call<List<ServicePostItem>>

    @POST("get_users_ratings.php")
    fun getUsersRatings(@Body request: UserRatingListRequest): Call<UserRatingsResponse>
}
