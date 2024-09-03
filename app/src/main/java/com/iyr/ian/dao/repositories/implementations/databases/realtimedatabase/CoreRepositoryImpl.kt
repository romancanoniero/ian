package com.iyr.ian.repository.implementations.databases.realtimedatabase

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import com.iyr.ian.dao.models._LatLng
import com.iyr.ian.dao.repositories.CoreRepository
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.tasks.await

class CoreRepositoryImpl : CoreRepository() {


    override suspend fun postCurrentLocation(userKey : String, latLng: LatLng, batteryLevel: Float): Resource<Boolean?> {
        return try {
            var tokenResult = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (tokenResult != null) {
                val data: MutableMap<String, Any> = HashMap()
                data["user_key"] = userKey
                data["latitude"] = latLng.latitude
                data["longitude"] = latLng.longitude
                data["battery_percentage"] = (batteryLevel*100).toInt()
                data["auth_token"] = tokenResult.token.toString()
                try {
                    var call = FirebaseFunctions.getInstance()
                        .getHttpsCallable("updateUserLocation")
                        .call(data).await()

                    var result = call.data as HashMap<String, Any?>
                    if (result["status"] ?: -1 == 0) {
                        Resource.Success<Boolean?>(true)
                    }
                    else
                        Resource.Error<Boolean?>(result["message"].toString())


                } catch (exception: Exception) {
                    Resource.Error<Boolean?>(exception.message.toString())
                }
            } else {
                Resource.Error<Boolean?>("error_getting_token")
            }
        } catch (exception: Exception) {
            Resource.Error<Boolean?>(exception.message.toString())
        }
    }


    override suspend fun postUserLocationsBatch(userKey : String, map: HashMap<String, _LatLng>,batteryLevel: Float ): Resource<Boolean?> {
        return try {
            var tokenResult = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (tokenResult != null) {
                val hashMapLocations = Gson().toJson(map)
                val data: MutableMap<String, Any> = HashMap()
                data["user_key"] = userKey
                data["locations"] = hashMapLocations
                data["batery_level"] =batteryLevel
                data["auth_token"] = tokenResult.token.toString()
                try {
                    var call = FirebaseFunctions.getInstance()
                        .getHttpsCallable("updateUserLocationsBatch")
                        .call(data).await()

                    var result = call.data as HashMap<String, Any?>
                    if (result["status"] ?: -1 == 0) {
                        Resource.Success<Boolean?>(true)
                    }
                    else
                        Resource.Error<Boolean?>(result["message"].toString())


                } catch (exception: Exception) {
                    Resource.Error<Boolean?>(exception.message.toString())
                }
            } else {
                Resource.Error<Boolean?>("error_getting_token")
            }
        } catch (exception: Exception) {
            Resource.Error<Boolean?>(exception.message.toString())
        }
    }
}