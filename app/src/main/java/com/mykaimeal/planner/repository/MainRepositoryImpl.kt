package com.mykaimeal.planner.repository

import android.util.Log
import com.google.gson.JsonObject
import com.mykaimeal.planner.apiInterface.ApiInterface
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.messageclass.ErrorMessage
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(private val api: ApiInterface) : MainRepository {

    override suspend fun bogyGoal(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.getBogyGoal().apply {
                if (isSuccessful) { body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun getDietaryRestrictions(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.getDietaryRestrictions().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun getFavouriteCuisines(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.getFavouriteCuisines().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun getDislikeIngredients(successCallback: (response: NetworkResult<String>) -> Unit,itemCount:String?) {
        try {
            api.getDislikeIngredients(itemCount).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun getDislikeSearchIngredients(
        successCallback: (response: NetworkResult<String>) -> Unit,
        itemCount: String?,
        type: String
    ) {
        try {
            val response = if (type.equals("Profile", ignoreCase = true)) {
                api.userPreferencesDislikeApi(itemCount, "100")
            } else {
                api.getDislikeSearchIngredients("100", itemCount)
            }

            if (response.isSuccessful) {
                response.body()?.let {
                    successCallback(NetworkResult.Success(it.toString()))
                } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
            } else {
                successCallback(NetworkResult.Error(response.errorBody()?.string() ?: ErrorMessage.apiError))
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(e.localizedMessage ?: "Unknown error occurred"))
        }
    }


    override suspend fun getAllergensSearchIngredients(successCallback: (response: NetworkResult<String>) -> Unit,data:String, itemCount: String?, type: String) {
        try {
            val response = if (type.equals("Profile", ignoreCase = true)) {
                api.userPreferencesAllergiesApi(data, itemCount)
            } else {
                api.getAllergensSearchIngredients(itemCount, data)
            }

            if (response.isSuccessful) {
                response.body()?.let {
                    successCallback(NetworkResult.Success(it.toString()))
                } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
            } else {
                successCallback(NetworkResult.Error(response.errorBody()?.string() ?: ErrorMessage.apiError))
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(e.localizedMessage ?: "Unknown error occurred"))
        }
    }


//    override suspend fun getDislikeSearchIngredients(successCallback: (response: NetworkResult<String>) -> Unit,itemCount:String?,type: String) {
//        try {
//            if (type.equals("Profile",true)){
//                api.userPreferencesDislikeApi(itemCount,"100").apply {
//                    if (isSuccessful) {
//                        body()?.let {
//                            successCallback(NetworkResult.Success(it.toString()))
//                        } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
//                    } else {
//                         successCallback(NetworkResult.Error(ErrorMessage.serverError))
//                    }
//                }
//
//            }else{
//                api.getDislikeSearchIngredients("100",itemCount).apply {
//                    if (isSuccessful) {
//                        body()?.let {
//                            successCallback(NetworkResult.Success(it.toString()))
//                        } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
//                    } else {
//                         successCallback(NetworkResult.Error(ErrorMessage.serverError))
//                    }
//                }
//            }
//
//
//
//
//        } catch (e: Exception) {
//            successCallback(NetworkResult.Error(ErrorMessage.serverError))
//        }
//    }

    override suspend fun getAllergensIngredients(successCallback: (response: NetworkResult<String>) -> Unit,itemCount:String?) {
        try {
            api.getAllergensIngredients(itemCount).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun getMealRoutine(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.getMealRoutine().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun getCookingFrequency(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.getCookingFrequency().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun getEatingOut(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.getEatingOut().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun getTipUrl(
        successCallback: (response: NetworkResult<String>) -> Unit, tip: String?
    ) {
        try {
            api.getTipUrl(tip).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun getTakeAwayReason(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.getTakeAwayReason().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun signUpModel(
        successCallback: (response: NetworkResult<String>) -> Unit,
        emailOrPhone: String,
        password: String
    ) {
        try {
            api.userSignUp(emailOrPhone, password).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun otpVerify(
        successCallback: (response: NetworkResult<String>) -> Unit,
        userid: String?,
        otp: String?,
        userName: String?,
        userGender: String?,
        bodyGoal: String?,
        cookingFrequency: String?,
        eatingOut: String?,
        takeAway: String?,
        takeWayName: String?,
        cookingForType: String?,
        partnerName: String?,
        partnerAge: String?,
        partnerGender: String?,
        familyMemberName: String?,
        familyMemberAge: String?,
        childFriendlyMeals: String?,
        mealRoutineId: List<String>?,
        spendingAmount: String?,
        duration: String?,
        dietaryid: List<String>?,
        favourite: List<String>?,
        allergies: List<String>?,
        dislikeIngredients: List<String>?,
        deviceType: String?,
        fcmToken: String?,
        referralFrom: String?
    ) {
        try {
            api.otpVerify(
                userid,
                otp,
                userName,
                userGender,
                bodyGoal,
                cookingFrequency,
                eatingOut,
                takeAway,
                takeWayName,
                cookingForType,
                partnerName,
                partnerAge,
                partnerGender,
                familyMemberName,
                familyMemberAge,
                childFriendlyMeals,
                mealRoutineId,
                spendingAmount,
                duration,
                dietaryid,
                favourite,
                allergies,
                dislikeIngredients,
                deviceType,
                fcmToken,referralFrom
            ).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun forgotPassword(
        successCallback: (response: NetworkResult<String>) -> Unit,
        emailOrPhone: String
    ) {
        try {
            api.forgotPassword(emailOrPhone).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun resendSignUpModel(
        successCallback: (response: NetworkResult<String>) -> Unit,
        emailOrPhone: String
    ) {
        try {
            api.resendOtp(emailOrPhone).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun forgotOtpVerify(
        successCallback: (response: NetworkResult<String>) -> Unit,
        emailOrPhone: String,
        otp: String
    ) {
        try {
            api.forgotOtpVerify(emailOrPhone, otp).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun resendOtp(
        successCallback: (response: NetworkResult<String>) -> Unit,
        emailOrPhone: String
    ) {
        try {
            api.resendOtp(emailOrPhone).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun resetPassword(
        successCallback: (response: NetworkResult<String>) -> Unit,
        emailOrPhone: String,
        password: String,
        confirmPassword: String
    ) {
        try {
            api.resetPassword(emailOrPhone, password, confirmPassword).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun userLogin(
        successCallback: (response: NetworkResult<String>) -> Unit,
        emailOrPhone: String,
        password: String,
        deviceType: String,
        fcmToken: String
    ) {
        try {
            api.userLogin(emailOrPhone, password, deviceType, fcmToken).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun socialLogin(
        successCallback: (response: NetworkResult<String>) -> Unit,
        emailOrPhone: String?,
        socialID: String?,
        userName: String?,
        userGender: String?,
        bodyGoal: String?,
        cookingFrequency: String?,
        eatingOut: String?,
        takeAway: String?,
        takeWayName: String?,
        cookingForType: String?,
        partnerName: String?,
        partnerAge: String?,
        partnerGender: String?,
        familyMemberName: String?,
        familyMemberAge: String?,
        childFriendlyMeals: String?,
        mealRoutineId: List<String>?,
        spendingAmount: String?,
        duration: String?,
        dietaryid: List<String>?,
        favourite: List<String>?,
        allergies: List<String>?,
        dislikeIngredients: List<String>?,
        deviceType: String?,
        fcmToken: String?,
        referralFrom: String?
    ) {
        try {
            api.socialLogin(
                emailOrPhone,
                socialID,
                userName,
                userGender,
                bodyGoal,
                cookingFrequency,
                eatingOut,
                takeAway,
                takeWayName,
                cookingForType,
                partnerName,
                partnerAge,
                partnerGender,
                familyMemberName,
                familyMemberAge,
                childFriendlyMeals,
                mealRoutineId,
                spendingAmount,
                duration,
                dietaryid,
                favourite,
                allergies,
                dislikeIngredients,
                deviceType,
                fcmToken,referralFrom
            ).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun updateLocation(
        successCallback: (response: NetworkResult<String>) -> Unit,
        locationStatus: String
    ) {
        try {
            api.updateLocation(locationStatus).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun updateNotification(
        successCallback: (response: NetworkResult<String>) -> Unit,
        notificationStatus: String
    ) {
        try {
            api.updateNotification(notificationStatus).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun privacyPolicy(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.getPrivacyPolicy().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun termCondition(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.getTermsCondition().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun saveFeedback(
        successCallback: (response: NetworkResult<String>) -> Unit,
        email: String,
        message: String
    ) {
        try {
            api.saveFeedback(email, message).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun userProfileDataApi(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.userProfileDataApi().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun userLogOutDataApi(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.userLogOutDataApi().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun userDeleteDataApi(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.userDeleteDataApi().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun upDateProfileRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        name: String,
        bio: String,
        genderType: String,
        dob: String,
        height: String,
        heightType: String,
        activityLevel: String,
        heightProtein: String,
        calories: String,
        fat: String,
        carbs: String,
        protien: String,
        weight: String,
        weightType: String,
        targetWeight: String,
        targetWeightType: String,
        macros: String,
        target: String,
        carbsPer: String,
        protinPer: String,
        fatPer: String,
        time: String,
        valuePerWeek: String,
        typeStatus: String,
        old_macro: String
    ) {
        try {
            api.userProfileUpdateApi(
                name, bio, genderType, dob, height, heightType, activityLevel,
                heightProtein, calories, fat, carbs, protien, weight, weightType,targetWeight,targetWeightType,
                macros,target,carbsPer,protinPer,fatPer,time,valuePerWeek,typeStatus,old_macro
            ).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun userProfileUpdateBioApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        bio: String) {
        try {
            api.userProfileUpdateBioApi(bio).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun upDateImageNameRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        Image: MultipartBody.Part?,
        name: RequestBody
    ) {
        try {
            api.upDateImageNameRequestApi(Image, name).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun addCardRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        token: String
    ) {
        try {
            api.addCardRequestApi(token).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun notificationRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        pushNotification: String,
        recipeRecommendations: String,
        productUpdates: String,
        promotionalUpdates: String
    ) {
        try {
            api.notificationRequestApi(
                pushNotification,
                recipeRecommendations,
                productUpdates,
                promotionalUpdates
            ).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun scanImageRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        jsonObject: JsonObject
    ) {
        try {
            api.scanImageRequestApi(jsonObject).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                    successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            Log.d("#Erorr","*****"+e.message)
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun recipeDetailsRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        url: String,
        recipeID: String,
        statusType: String,
        serving: String
    ) {
        try {
            api.recipeDetailsRequestApi(url,recipeID,statusType,serving).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun recipeReviewRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        url: String,
        msg: String,
        ratingBarcount: String
    ) {
        try {
            api.recipeReviewRequestApi(url, msg, ratingBarcount).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun homeDetailsRequestApi(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.homeDetailsRequestApi().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun recipeAddBasketRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        jsonObject: JsonObject
    ) {
        try {
            api.recipeAddBasketRequestApi(jsonObject).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun recipeAddToPlanRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        jsonObject: JsonObject
    ) {
        try {
            api.recipeAddToPlanRequestApi(jsonObject).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun createRecipeRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        jsonObject: JsonObject,
        screenName: String
    ) {
        try {
            val response = if (screenName.equals("Create Recipe", true)) {
                api.createRecipeRequestApi(jsonObject)
            } else {
                api.editRecipeRequestApi(jsonObject)
            }
            if (response.isSuccessful) {
                response.body()?.let {
                    successCallback(NetworkResult.Success(it.toString()))
                } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
            } else {
                successCallback(NetworkResult.Error(ErrorMessage.serverError))
            }

        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun convertUnitRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        jsonObject: JsonObject
    ) {
        api.convertUnitRequestApi(jsonObject).apply {
            if (isSuccessful) {
                body()?.let {
                    successCallback(NetworkResult.Success(it.toString()))
                } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
            } else {
                successCallback(NetworkResult.Error(ErrorMessage.serverError))
            }
        }

    }


    override suspend fun updateMealUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,
        jsonObject: JsonObject
    ) {
        try {
            api.updateMealUrl(jsonObject).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun getCookBookRequestApi(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.getCookBookRequestApi().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun getUnitRequest(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.getUnitRequest().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun getCookBookTypeRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        id: String?
    ) {
        try {
            api.getCookBookTypeRequestApi(id).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun createCookBookApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        name: RequestBody?,
        image: MultipartBody.Part?,
        status: RequestBody?,
        id: RequestBody?
    ) {
        try {
            api.createCookBook(name, image, status, id).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun planRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        q: String
    ) {
        try {
            api.planRequestApi(q).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun planPaginationRequest(
        successCallback: (response: NetworkResult<String>) -> Unit,
        pageType: String
    ) {
        try {
            api.planPaginationRequest(pageType).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun planDateRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        date: String, planType: String
    ) {
        try {
            api.planDateRequestApi(date, planType).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }
    override suspend fun cookPlanDateRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        date: String, planType: String
    ) {
        try {
            api.cookPlanDateRequestApi(date, planType).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun getScheduleApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        date: String, planType: String
    ) {
        try {
            api.getScheduleApi(date, planType).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun likeUnlikeRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        uri: String,
        likeType: String,
        type: String
    ) {
        try {
            api.likeUnlikeRequestApi(uri, likeType, type).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun superMarketSaveRequest(
        successCallback: (response: NetworkResult<String>) -> Unit,
        store: String?,storeName: String?
    ) {
        try {
            api.superMarketSaveRequestApi(store,storeName).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun moveRecipeRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        id: String,
        cookBook: String
    ) {
        try {
            api.moveRecipeRequestApi(id, cookBook).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun deleteCookBookRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        id: String
    ) {
        try {
            api.deleteCookBookRequestApi(id).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun addBasketRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit, uri: String, quantity: String,type: String) {
        try {
            api.addBasketRequestApi(uri, quantity,type).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun getCardAndBankRequestApi(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.getCardAndBankRequestApi().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }




    override suspend fun getWalletRequestApi(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.getWalletRequestApi().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun deleteCardRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        cardId: String,
        customerId: String
    ) {
        try {
            api.deleteCardRequestApi(cardId, customerId).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun deleteBankRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        stripeAccountId: String
    ) {
        try {
            api.deleteBankRequestApi(stripeAccountId).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun countryRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        url: String
    ) {
        try {
            api.countryRequestApi(url).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun transferAmountRequest(
        successCallback: (response: NetworkResult<String>) -> Unit,
        amount: String,
        destination: String
    ) {
        try {
            api.transferAmountRequest(amount, destination).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun addBankRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        filePartFront: MultipartBody.Part?,
        filePartBack: MultipartBody.Part?,
        filePart: MultipartBody.Part?,
        firstNameBody: RequestBody,
        lastNameBody: RequestBody,
        emailBody: RequestBody,
        phoneBody: RequestBody,
        dobBody: RequestBody,
        personalIdentificationNobody: RequestBody,
        idTypeBody: RequestBody,
        ssnBody: RequestBody,
        addressBody: RequestBody,
        countryBody: RequestBody,
        shortStateNameBody: RequestBody,
        cityBody: RequestBody,
        postalCodeBody: RequestBody,
        bankDocumentTypeBody: RequestBody,
        deviceTypeBody: RequestBody,
        tokenTypeBody: RequestBody,
        stripeTokenBody: RequestBody,
        saveCardBody: RequestBody,
        amountBody: RequestBody,
        paymentTypeBody: RequestBody,
        bankIdBody: RequestBody
    ) {
        try {
            api.addBankRequestApi(
                filePartFront,
                filePartBack,
                filePart,
                firstNameBody,
                lastNameBody,
                emailBody,
                phoneBody,
                dobBody,
                personalIdentificationNobody,
                idTypeBody,
                ssnBody,
                addressBody,
                countryBody,
                shortStateNameBody,
                cityBody,
                postalCodeBody,
                bankDocumentTypeBody,
                deviceTypeBody,
                tokenTypeBody,
                stripeTokenBody,
                saveCardBody,
                amountBody,
                paymentTypeBody,
                bankIdBody
            ).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun userPreferencesApi(
        successCallback: (response: NetworkResult<String>) -> Unit
    ) {
        try {
            api.userPreferencesApi().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun userSubscriptionCountApi(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.userSubscriptionCountApi().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun userPreferencesDislikeApi(
        successCallback: (response: NetworkResult<String>) -> Unit,dislike_search:String?,dislike_num:String?
    ) {
        try {
            api.userPreferencesDislikeApi(dislike_search,dislike_num).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun userPreferencesAllergiesApi(
        successCallback: (response: NetworkResult<String>) -> Unit,allergic_search:String?,allergic_num:String?
    ) {
        try {
            api.userPreferencesAllergiesApi(allergic_search,allergic_num).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun updateBodyGoalApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        bodygoal: String?
    ) {
        try {
            api.updateBodyGoalApi(bodygoal).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun updateCookBookApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        cookBookId: String?
    ) {
        try {
            api.updateCookBookApi(cookBookId).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun updateCookingFrequencyApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        cookingFrequency: String?
    ) {
        try {
            api.updateCookingFrequencyApi(cookingFrequency).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun updateAllergiesApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        allergies: List<String>?
    ) {
        try {
            api.updateAllergiesApi(allergies).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun addToCartUrlApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        foodIds: MutableList<String>?,
        schId: String?,
        foodName: MutableList<String>?,
        status: MutableList<String>?,
        recipeUri:String,mealType:String
    ) {
        try {
            api.addToCartUrlApi(foodIds, schId, foodName, status,recipeUri,mealType).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e:  Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun addShoppingCartUrlApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        foodIds: MutableList<String>?,
        schId: MutableList<String>?,
        foodName: MutableList<String>?,
        status: MutableList<String>?,
        uri:String,
        type:String
    ) {
        try {
            api.addShoppingCartUrlApi(foodIds, schId, foodName, status,uri,type).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e:  Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }
    override suspend fun addShoppingCheckApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        ids: MutableList<String>?
    ) {
        try {
            api.addShoppingCheckApi(ids).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e:  Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }
    override suspend fun addPurchasedElemenentsUrlApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        ids: MutableList<String>?
    ) {
        try {
            api.addPurchasedElemenentsUrlApi(ids).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e:  Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun addGraphDataApi(
        successCallback: (response: NetworkResult<String>) -> Unit,storeName: String?,
        cardTotal: String?,
        date: String?
    ) {
        try {
            api.addGraphDataApi(storeName,cardTotal,date).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e:  Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun updateReasonTakeAwayApi(successCallback: (response: NetworkResult<String>) -> Unit, takeway: String?, takeWayName: String?) {
        try {
            api.updateReasonTakeAwayApi(takeway,takeWayName).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun updateEatingOutApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        eatingOut: String?
    ) {
        try {
            api.updateEatingOutApi(eatingOut).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun updatePartnerInfoApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        partnerName: String?, partnerAge: String?, partnerGender: String?
    ) {
        try {
            api.updatePartnerInfoApi(partnerName, partnerAge, partnerGender).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun updateFamilyInfoApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        familyName: String?, familyAge: String?, status: String?
    ) {
        try {
            api.updateFamilyInfoApi(familyName, familyAge, status).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun updateSpendingGroceriesApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        spendingAmount: String?, duration: String?
    ) {
        try {
            api.updateSpendingGroceriesApi(spendingAmount, duration).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun updateMealRoutineApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        mealRoutineId: List<String>?
    ) {
        try {
            api.updateMealRoutineApi(mealRoutineId).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun updateDietaryApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        dietaryId: List<String>?
    ) {
        try {
            api.updateDietaryApi(dietaryId).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun updateFavouriteApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        favouriteId: List<String>?
    ) {
        try {
            api.updateFavouriteApi(favouriteId).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun updateDislikedIngredientsApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        dislikedId: List<String>?
    ) {
        try {
            api.updateDislikedIngredientsApi(dislikedId).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun updatePostCodeApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        postCode: String?, longitude: String?, latitude: String?
    ) {
        try {
            api.updatePostCode(postCode, longitude, latitude).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun userEstimatedApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        weight: String?,
        weightType: String?,
        targetWeight: String?,
        targetWeightType: String?,
        type: String?) {
        try {
            api.userEstimatedApi(weight?.trim()?.toDoubleOrNull(), weightType, targetWeight?.trim()?.toDoubleOrNull(),targetWeightType,type).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                    successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(e.message.toString()))
        }
    }

    override suspend fun recipeSearchApi(successCallback: (response: NetworkResult<String>) -> Unit, itemSearch: JsonObject?) {
        try {
            api.recipeSearchApi(itemSearch).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun recipeSearchFromSearchApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        itemSearch: JsonObject?
    ) {
        try {
            api.recipeSearchFromSearchApi(itemSearch).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun recipeFilterSearchApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        mealType: MutableList<String>?,
        health: MutableList<String>?,
        time: String?,
        cuisine: MutableList<String>?,
        nutrition: String?
    ) {
        try {
            // Convert empty strings to null
            val validtime = if (time.isNullOrBlank()) null else time
            val validNutrition = if (nutrition.isNullOrBlank()) null else nutrition

            api.recipeFilterSearchApi(mealType, health, validtime,cuisine,validNutrition).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun recipeforSearchApi(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.recipeForSearchApi().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun getMissingIngBasketUrl(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.getMissingIngBasketUrl().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun recipePreferencesApi(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.recipePreferencesApi().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun createRecipeUrlApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        itemSearchName: String?
    ) {
        try {
            api.createRecipeUrlApi(itemSearchName).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun recipeSearchFromURLApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        itemSearchName: String?,
        apiCall: String?
    ) {
        try {
            val response = if (apiCall.equals("import")) {
                api.recipeSearchFromURLApi(itemSearchName)
            } else {
                api.createRecipeUrlApi(itemSearchName)
            }
            response.apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                    successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun recipeSearchIngredientApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        itemSearchName: String?,
        type: String?
    ) {
        try {
            api.recipeSearchIngredientApi(itemSearchName,type).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                    successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun removeMealApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        cookedId: String?
    ) {
        try {
            api.removeMealApi(cookedId).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun removeMealCookApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        cookedId: String?
    ) {
        try {
            api.removeMealCookApi(cookedId).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun removeBasketUrlApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        recipeId: String?
    ) {
        try {
            api.removeBasketUrlApi(recipeId).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun getInstaCart(
        successCallback: (response: NetworkResult<String>) -> Unit,
        jsonObject: JsonObject?
    ) {
        try {
            api.getInstaCart(jsonObject).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                    successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun getMissingIngredientsApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        uri: String?, shcId: String?
    ) {
        try {
            api.getMissingIngredientsApi(uri, shcId).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun getMealByUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,
        q: String?
    ) {
        try {
            api.getMealByUrl(q).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }
    override suspend fun getRecipeByUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,
        q: String?
    ) {
        try {
            api.getRecipeByUrl(q).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun updatePreferencesApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        userName: String?,
        cookingForType: String?,
        userGender: String?,
        bodygoal: String?,
        partner_name: String?,
        partner_age: String?,
        partner_gender: String?,
        family_member_name: String?,
        family_member_age: String?,
        child_friendly_meals: String?,
        dietaryId: MutableList<String>?,
        favouriteId: MutableList<String>?,
        dislikeIngId: MutableList<String>?,
        allergensId: MutableList<String>?,
        mealRoutineId: MutableList<String>?,
        cookingFrequency: String?,
        spendingAmount: String?,
        duration: String?,
        eatingOut: String?,
        takeWay: String?,takeWayName: String?
    ) {
        try {
            api.updatePreferencesApi(
                userName,
                cookingForType,
                userGender,
                bodygoal,
                partner_name,
                partner_age,
                partner_gender,
                family_member_name,
                family_member_age,
                child_friendly_meals,
                dietaryId,
                favouriteId,
                dislikeIngId,
                allergensId,
                mealRoutineId,
                cookingFrequency,
                spendingAmount,
                duration,
                eatingOut,
                takeWay,takeWayName
            ).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun getFilterList(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.getFilterList().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun getSuperMarket(
        successCallback: (response: NetworkResult<String>) -> Unit,
        latitude: String?, longitude: String?
    ) {
        try {
            api.getSuperMarket(latitude, longitude).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun getSuperMarketWithPage(
        successCallback: (response: NetworkResult<String>) -> Unit,
        latitude: String?, longitude: String?,pageCount:String?
    ) {
        try {
            api.getSuperMarketWithPage(latitude, longitude,pageCount).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun getcheckAvailablity(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.getcheckAvailablity().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun getScheduleTimeApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        date: String?,
        days: String?,
        type: String?
    ) {
        try {
            api.getScheduleTimeApi(date,days,type).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                    successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun setScheduleTimeApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        date: String?,
        startTime: String?,
        endTime: String?,
        discount: String
    ) {
        try {
            api.setScheduleTimeApi(date,startTime,endTime,discount).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                    successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun subscriptionGoogle(successCallback: (response: NetworkResult<String>) -> Unit,
                                            type: String?, purchaseToken: String?, subscriptionId: String?) {
        try {
            api.subscriptionGoogle(type,purchaseToken, subscriptionId).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun subscriptionPurchaseType(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.subscriptionPurchaseType().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun getBasketUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,storeId:String?,latitude:String?,longitude:String?) {
        try {
            api.getBasketListUrl(storeId,latitude, longitude).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun getYourRecipeUrl(
        successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.getYourRecipeUrl().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun sendOtpUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,phone:String?) {
        try {
            api.sendOtpUrl(phone).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun addPhoneUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,phone:String?,otp:String?,countryCode:String?) {
        try {
            api.addPhoneUrl(phone,otp,countryCode).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun getAddressUrl(
        successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.getAddressUrl().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun addAddressUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,
        latitude: String?, longitude: String?,streetName:String?,streetNum:String?,apartNum:String?,city:String?,state:String?,country:String?,
        zipcode:String?,primary:String?,id:String?,type:String?
    ) {
        try {
            api.addAddressUrl(latitude, longitude,streetName,streetNum,apartNum,city,state,country,zipcode, primary, id, type).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun makeAddressPrimaryUrl(
        successCallback: (response: NetworkResult<String>) -> Unit, id:String?) {
        try {
            api.makeAddressPrimaryUrl(id).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun getShoppingList(
        successCallback: (response: NetworkResult<String>) -> Unit
    ) {
        try {
            api.getShoppingListUrl().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun getNotesUrl(
        successCallback: (response: NetworkResult<String>) -> Unit
    ) {
        try {
            api.getNotesUrl().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun addNotesUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,
        pickup:String?,description:String?
    ) {
        try {
            api.addNotesUrl(pickup,description).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun getCheckoutScreenUrl(
        successCallback: (response: NetworkResult<String>) -> Unit
    ) {
        try {
            api.getCheckoutScreenUrl().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun getOrderProductUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,paymentObject: JsonObject
    ) {
        try {
            api.getOrderProductUrl(paymentObject).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun getOrderProductGooglePayUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,
        tip: String?,
        amount: String?,
        stripeTokenId: String?
    ) {
        try {
            api.getOrderProductGooglePayUrl(tip, amount,stripeTokenId).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun getStoreProductUrl(
        successCallback: (response: NetworkResult<String>) -> Unit
    ) {
        try {
            api.getStoreProductUrl().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun addCardMealMeUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,cardNumber:String?,expMonth:String?,expYear:String?,cvv:String?,status:String?,type:String?
    ) {
        try {
            api.addCardMealMeUrl(cardNumber,expMonth,expYear,cvv,status,type).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun deleteCardMealMeUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,id:String?
    ) {
        try {
            api.deleteCardMealMeUrl(id).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun setPreferredCardMealMeUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,id:String?
    ) {
        try {
            api.setPreferredCardMealMeUrl(id).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun getCardMealMeUrl(
        successCallback: (response: NetworkResult<String>) -> Unit
    ) {
        try {
            api.getCardMealMeUrl().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun addToBasketAllUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,date: String?
    ) {
        try {
            api.addToBasketAllUrl(date).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun updateDietSuggestionUrl(
        successCallback: (response: NetworkResult<String>) -> Unit, gender: String?, dob: String?, height: String?,
        heightType: String?, weight: String?, weightType: String?, activityLevel: String?, targetWeight: String?, targetWeightType: String?
        , goalDate: String?, macros: String?, calories: String?, fatPer: String?, proteinPer: String?, carbsPer: String?, typeStatus: String?
    ) {
        try {
            api.updateDietSuggestionUrl(gender,dob, height, heightType, weight, weightType,
                activityLevel,targetWeight,targetWeightType,goalDate,macros,calories,fatPer,proteinPer,carbsPer,typeStatus).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun getProductsUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,query:String?,foodId:String?,schId:String?
    ) {
        try {
            api.getProductsUrl(query,foodId,schId).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun getProductsDetailsUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,proId:String?,query:String?,foodId:String?,schId:String?
    ) {
        try {
            api.getProductsDetailsUrl(proId,query,foodId, schId).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun getSelectProductsUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,id:String?,productId:String?,schId: String?
    ) {
        try {
            api.getSelectProductsUrl(id,productId,schId).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun getAllIngredientsUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,category:String?,search:String?,number:String?
    ) {
        try {
            api.getAllIngredientsUrl(category, search, number).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun recipeSwapUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,id:String?,uri:String?
    ) {
        try {
            api.recipeSwapUrl(id,uri).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun basketYourRecipeIncDescUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,uri:String?,quantity:String?,type:String?
    ) {
        try {
            api.basketYourRecipeIncDescUrl(uri,quantity,type.toString()).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun basketIngIncDescUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,foodId:String?,quantity:String?
    ) {
        try {
            api.basketIngIncDescUrl(foodId,quantity).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun addMealTypeApiUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,uri:String?,planType:String?,mealType:String?
    ) {
        try {
            api.addMealTypeApiUrl(uri,planType,mealType).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun generateLinkUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,
        link: RequestBody?, image: MultipartBody.Part?,
    ) {
        try {
            api.generateLinkUrl(link, image).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun selectStoreProductUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,
        storeName: String?, storeId:String?,
    ) {
        try {
            api.selectStoreProductUrl(storeName, storeId).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun getGraphScreenUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,
        month: String?,
        year: String?
    ) {
        try {
            api.getGraphScreenUrl(month,year).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }


    override suspend fun orderHistoryUrl(
        successCallback: (response: NetworkResult<String>) -> Unit
    ) {
        try {
            api.orderHistoryUrl().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun orderWeekUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,startDate:String?,endDate:String?,year:String?
    ) {
        try {
            api.orderWeekUrl(startDate,endDate,year).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun referralUrl(
        successCallback: (response: NetworkResult<String>) -> Unit
    ) {
        try {
            api.referralUrl().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

    override suspend fun referralRedeem(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            api.referralRedeem().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(ErrorMessage.apiError))
                } else {
                     successCallback(NetworkResult.Error(ErrorMessage.serverError))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(ErrorMessage.serverError))
        }
    }

}