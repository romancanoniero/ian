package com.iyr.ian.dao.repositories


import com.google.android.gms.maps.model.LatLng
import com.iyr.ian.dao.models._LatLng
import com.iyr.ian.utils.coroutines.Resource

interface CoreRepositoryInterface {

    /**
     * actualiza la ubicacion actual del usuario
     *
     */
    suspend fun postCurrentLocation(userKey : String, latLng: LatLng, batteryLevel: Float) : Resource<Boolean?>
    suspend fun postUserLocationsBatch(userKey : String, map: HashMap<String, _LatLng>,batteryLevel: Float ) : Resource<Boolean?>

}


abstract class CoreRepository : CoreRepositoryInterface {
    private var authManager: Any? = null
    private val tableReference: Any? = null


}