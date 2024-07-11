package com.iyr.ian.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.UiModeManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.BatteryManager
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.people.v1.PeopleService
import com.google.api.services.people.v1.model.ListConnectionsResponse
import com.google.api.services.people.v1.model.Person
import com.google.gson.Gson
import com.iyr.ian.ui.signup.phone_contacts.PhoneContactListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException
import java.util.Objects


class PhoneContact {

    private var contact_id: String? = null
    var image_uri: String? = null
    var display_name: String? = null
    var telephone_number: String? = null
    var phone_type: String? = null
    var email: String? = null
    var selected: Boolean = false
    var have_phone: Boolean = false
    var add_to_speed_dial: Boolean = false

    override fun equals(o: Any?): Boolean {

        val that = o as PhoneContact
        return display_name == that.display_name &&
        telephone_number == that.telephone_number
    }

    override fun hashCode(): Int {
        return Objects.hash(contact_id)
    }

    fun copy(): PhoneContact { //Get another instance of YourClass with the values like this!
        val json = Gson().toJson(this)
        return Gson().fromJson(json, PhoneContact::class.java)
    }
}


class DeviceExtension
{
    fun getGoogleContacts() {

        val HTTP_TRANSPORT = NetHttpTransport()
        val JSON_FACTORY = JacksonFactory.getDefaultInstance()

        val credential = GoogleCredential().setAccessToken("84941778245-en7p3pv56pf3glmv7am8l9inrq45osh5.apps.googleusercontent.com")
        val peopleService = PeopleService.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).build()
        val connectionsResponse: ListConnectionsResponse = peopleService.people().connections()
            .list("people/me")
            .setPersonFields("names,emailAddresses")
            .execute()
        val connections: List<Person> = connectionsResponse.connections
        for (person in connections) {
            println("Name: ${person.names[0].displayName}")
            println("Email: ${person.emailAddresses[0].value}")
        }


    }
}

fun Activity.openNavigatorTo(latLng: LatLng) {
    val stringBuffer = StringBuffer()
    stringBuffer.append("google.navigation:q=")
    stringBuffer.append(latLng.latitude)
    stringBuffer.append(",")
    stringBuffer.append(latLng.longitude)

    val gmmIntentUri: Uri = Uri.parse(stringBuffer.toString())
    val mapIntent: Intent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    startActivity(mapIntent)
}


fun Context.getBatteryPercentage(): Float {
    val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val batteryStatus: Intent = registerReceiver(null, ifilter)!!

    val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

    val batLevel = level.toFloat() / scale.toFloat()
    return batLevel
}


suspend fun Activity.getContactList2(listener: PhoneContactListener) {
    var recordsRetrieved = 0
    val resolver: ContentResolver = getContentResolver()
    val cursorWithContacts: Cursor? =
        resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)

/*
    val extIntance =  DeviceExtension()
    extIntance.getGoogleContacts()
*/
    cursorWithContacts?.let { cursor ->
        while (cursor.moveToNext()) {
            try {
                val id: String =
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val name: String =
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                val hasPhoneNumber: Int =
                    cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                if (hasPhoneNumber > 0) {
                    val contact: PhoneContact = PhoneContact()

                    val cursorPhone = resolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )

                    cursorPhone?.let { cursorP ->
                        while (cursorP.moveToNext()) {
                            val phoneType: String =
                                cursorP.getString(cursorP.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE))
                            val phoneNumber: String =
                                cursorP.getString(cursorP.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))

                            var image: String = ""
                            image =
                                cursorP.getString(cursorP.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Photo.PHOTO_URI))

                            contact.image_uri = image
                            contact.phone_type = phoneType
                            contact.telephone_number = phoneNumber

                            val cursorEmail: Cursor? = resolver.query(
                                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                                arrayOf(id),
                                null
                            )

                            if (cursorEmail != null && cursorEmail.moveToFirst()) {
                                val email = cursorEmail.getString(cursorEmail.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.DATA))
                                contact.email = email
                            }

                            cursorEmail?.close()

                            contact.display_name = name
                            withContext(Dispatchers.Main) {
                                listener.onContactAdded(contact)
                            }
                        }
                        cursorP.close()
                    }
                }
            } catch (e: Exception) {
                // Handle exception
            }

            recordsRetrieved++
            listener.onPercentChanged((recordsRetrieved * 100) / cursorWithContacts.count)
        }
        cursor.close()
    }



}

/*
suspend fun Activity.getContactList2(listener: PhoneContactListener) {

    var recordsRetrieved = 0
    val resolver: ContentResolver = getContentResolver()
    val cursorWithContacts: Cursor? =
        resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)

    cursorWithContacts?.let { cursor ->
        while (cursor.moveToNext()) {

            try {
                val id: String =
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val name: String =
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                val hasPhoneNumber: Int =
                    cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))



                if (hasPhoneNumber > 0) {

                    val contact: PhoneContact = PhoneContact()



                    val cursorPhone = resolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )

                    cursorPhone?.let { cursorP ->
                        if (cursorP.moveToFirst()) {
                            val phoneType: String =
                                cursorP.getString(cursorP.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE))
                            val phoneNumber: String =
                                cursorP.getString(cursorP.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                      /*
                            val email: String =
                                cursorP.getString(cursorP.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.DATA))
                        */
                            var image: String = ""

                            image =
                                cursorP.getString(cursorP.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Photo.PHOTO_URI))

                            contact.image_uri = image
                            contact.phone_type = phoneType
                            contact.telephone_number = phoneNumber

                            //---------
                            val cursorEmail: Cursor? = resolver.query(
                                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                                arrayOf(id), // id del contacto
                                null
                            )

                            if (cursorEmail != null && cursorEmail.moveToFirst()) {
                                val email = cursorEmail.getString(cursorEmail.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.DATA))
                                // Aquí tienes el correo electrónico del contacto
                                println("Email: $email")
                                contact.email = email

                            } else {
                                // El contacto no tiene correo electrónico
                                println("El contacto no tiene correo electrónico")
                            }

                            cursorEmail?.close()

                            contact.display_name = name
                            withContext(Dispatchers.Main) {
                                listener.onContactAdded(contact)
                            }


                            //----------

                            // Aquí tienes el nombre, el tipo de teléfono, el número de teléfono, el correo electrónico y la imagen del contacto
                        //    println("Nombre: $name, Tipo de teléfono: $phoneType, Número de teléfono: $phoneNumber, Email: $email, Imagen: $image")

                        }
                        cursorP.close()
                    }



                }


            } catch (e: Exception) {
                var ppp = 3
            }

            recordsRetrieved++
            listener.onPercentChanged((recordsRetrieved * 100) / cursorWithContacts.count)

        }
        cursor.close()
    }

}
*/

@SuppressLint("Range")
suspend fun Activity.getContactList(listener: PhoneContactListener): ArrayList<PhoneContact> {
    val contacts = ArrayList<PhoneContact>()

    /*
        var sb: StringBuffer = StringBuffer()
        sb.append("......Contact Details.....")
     */
    val cr: ContentResolver = contentResolver
    val cur: Cursor? = cr.query(
        ContactsContract.Contacts.CONTENT_URI, null,
        null, null, null
    )

    var phone: String? = null
    var phoneType: String? = null
    var emailContact: String? = null
    var emailType: String? = null
    var image_uri: String? = ""
    var bitmap: Bitmap? = null
    var recordsRetrieved = 0
    if (cur?.count!! > 0) {

        phone = null
        phoneType = null
        emailContact = null
        emailType = null
        image_uri = ""
        bitmap = null


        while (cur.moveToNext()) {
            val id: String? = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
            val name: String? =
                cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
            image_uri =
                cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
            if (Integer.parseInt(
                    cur.getString(
                        cur
                            .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                    )
                ) > 0
            ) {
                println("name : $name, ID : $id")
                //   sb.append("\n Contact Name:" + name);
                val pCur: Cursor? = cr.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                            + " = ?", arrayOf(id), null
                )

                if (pCur != null) {
                    while (pCur.moveToNext()) {
                        phone = pCur
                            .getString(
                                pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            )



                        phoneType =
                            pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))
                                ?: ""
                    }
                }
                pCur?.close()

                val emailCur: Cursor? = cr.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID
                            + " = ?", arrayOf(id), null
                )
                if (emailCur != null) {
                    while (emailCur.moveToNext()) {
                        emailContact = emailCur
                            .getString(
                                emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)
                            )
                        emailType = emailCur.getString(
                            emailCur
                                .getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE)
                        )
                    }
                }

                emailCur?.close()
            }

            if (image_uri != null) {
                //              System.out.println(Uri.parse(image_uri));
                try {
                    bitmap = MediaStore.Images.Media
                        .getBitmap(
                            this.contentResolver,
                            Uri.parse(image_uri)
                        )
//                    sb.append("\n Image in Bitmap:" + bitmap);
//                    System.out.println(bitmap);

                } catch (e: FileNotFoundException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                } catch (e: IOException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }

            }

            if (phoneType.toString() == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE.toString() &&
                phone.toString().replace(" ", "").replace("-", "").length > 8
            ) {
                val contact: PhoneContact = PhoneContact()
                contact.image_uri = image_uri
                contact.display_name = name
                contact.phone_type = phoneType
                contact.telephone_number = phone
                contact.email = emailContact

                withContext(Dispatchers.Main) {
                    listener.onContactAdded(contact)
                }
            }
            recordsRetrieved++
            listener.onPercentChanged((recordsRetrieved * 100) / cur.count)
        }

    }
    listener.onFinishedRetrieve()
    return contacts

}

fun Context.isAndroidTV(): Boolean {
    val uiModeManager: UiModeManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
    return if (uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION) {
        Log.d("TV", "Running on a TV Device")
        true
    } else {
        Log.d("TV", "Running on a non-TV Device")
        false
    }
}