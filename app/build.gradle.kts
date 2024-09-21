import java.util.Properties

plugins {

    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("androidx.navigation.safeargs.kotlin")

}


android {
    namespace = "com.iyr.ian"
    compileSdk = 34


    packagingOptions {
        pickFirst("META-INF/DEPENDENCIES")
    }

    defaultConfig {
        applicationId = "com.iyr.ian"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Get the API keys from local.properties
        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())

        // Set API keys in BuildConfig
        buildConfigField("String", "MAPS_API_KEY", properties.getProperty("MAPS_API_KEY"))
        buildConfigField("Boolean", "NAVIGATION_HOST_MODE", properties.getProperty("NAVIGATION_HOST_MODE"))


    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
    }

    buildFeatures {
        dataBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }


}

dependencies {


    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("com.android.support:cardview-v7:28.0.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    kapt("com.android.databinding:compiler:3.2.0-alpha10")



    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")


    // COROUTINES
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")


    // MVVM
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.fragment:fragment-ktx:1.8.1")

    implementation("android.arch.lifecycle:extensions:1.1.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")


    // FUNTIONALITIES SUPPORT


    implementation("com.google.firebase:firebase-dynamic-links")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-messaging-directboot:23.2.1")


    //PEOPLE
    implementation("com.google.api-client:google-api-client:1.23.0")
    implementation("com.google.apis:google-api-services-people:v1-rev294-1.25.0")
    implementation("com.google.apis:google-api-services-people:v1-rev20200720-1.30.10")


    // FIREBASE
    implementation(platform("com.google.firebase:firebase-bom:32.0.0"))
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.firebaseui:firebase-ui-storage:6.2.1")
    implementation("com.google.firebase:firebase-appcheck")


    implementation("com.google.android.gms:play-services-auth:19.2.0")
    implementation("com.google.api-client:google-api-client-android:1.20.0")


    // DATABASE MANAGEMENT
    // -- REALTIME DATABASE
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-functions-ktx:20.3.1")


    // AUTHENTICATION
    implementation("com.google.android.gms:play-services-auth:20.6.0")
    implementation("com.google.firebase:firebase-auth")
    implementation("androidx.browser:browser:1.3.0")
    implementation("com.google.firebase:firebase-appcheck-safetynet:16.0.0")
    implementation("androidx.credentials:credentials:1.2.2")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.2")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")


    // SOCIAL NETWORKS / AUTHENTICATION
    implementation("com.facebook.android:facebook-android-sdk:[4,5)")

    // Compresores de imagen y video
    implementation("de.peilicke.sascha:kase64:1.2.0")

    // HTTP
    implementation("com.squareup.okhttp3:okhttp:4.7.2")
    testImplementation("com.squareup.okhttp3:okhttp-urlconnection:4.4.1")

    // Misc
    implementation("com.airbnb.android:lottie:4.1.0")
    implementation("com.github.captain-miao:optroundcardview:1.0.0")
    implementation("com.github.markushi:circlebutton:1.1")
    implementation("com.marlonmafra.android.widget:edittext-password:1.0.0") // EditText para passwords
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.github.marlonlom:timeago:4.0.3")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation ("com.github.alexzhirkevich:custom-qr-generator:2.0.0-alpha01")  // Generador de codigos QR para compartir la aplicacion.
    implementation ("com.github.yuriy-budiyev:code-scanner:2.3.0") //Scanner de codigos QR

    // RecyclerViews Layouts
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("com.yarolegovich:discrete-scrollview:1.5.1")
    implementation("com.chauthai.swipereveallayout:swipe-reveal-layout:1.4.1")

    implementation("com.chenlittleping:recyclercoverflow:1.0.6")
    implementation("com.github.sparrow007:carouselrecyclerview:1.2.6")
    implementation("com.github.jama5262:CarouselView:1.1.0")

    //-- Indicators
    implementation("com.tbuonomo:dotsindicator:4.2")
    implementation("ru.tinkoff.scrollingpagerindicator:scrollingpagerindicator:1.2.5")

    implementation("com.github.martinstamenkovski:ARIndicatorView:2.0.0")
    implementation("com.github.mreram:SeekArc:v1.6")


    implementation("com.github.alirezat775:carousel-view:1.1.1")

    // -- LayoutManagers
    implementation("com.ccy:FocusLayoutManager:1.0.2")


    // OTP
    implementation("com.github.mukeshsolanki:android-otpview-pinview:2.1.1")

    // Imagenes
    /*
        implementation("com.github.bumptech.glide:glide:4.11.0")
        implementation("com.github.bumptech.glide:annotations:4.11.0")
    */
    implementation("com.github.bumptech.glide:glide:4.11.0") {
        exclude(group = "com.android.support")
    }
    annotationProcessor("com.github.bumptech.glide:compiler:4.11.0")
    kapt("com.github.bumptech.glide:compiler:4.11.0")

    // VIDEO
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")

    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.25")
    implementation("com.github.siyamed:android-shape-imageview:0.9.3")


    //-- DIALOGS
    implementation("com.github.f0ris.sweetalert:library:1.5.1")

    //-- Multimedia Related
    implementation("com.github.Mindinventory:Lassi:1.3.0")
    implementation("com.google.android.gms:play-services-cast-framework:21.3.0")


    implementation("com.github.Armen101:AudioRecordView:1.0.5")
    implementation("com.github.3llomi:RecordView:3.1.3")

    implementation("com.iceteck.silicompressorr:silicompressor:2.2.4")
    implementation ("com.googlecode.mp4parser:isoparser:1.1.22")
    // BLUETOOTH
    implementation("com.github.Jasonchenlijian:FastBle:2.4.0")
    // LOCATION
    implementation("io.nlopez.smartlocation:library:3.3.3")

    // MAPS
    implementation("com.google.maps.android:android-maps-utils:0.5")
    implementation("com.utsman.smartmarker:core:1.3.2@aar")
    // extension for google maps
    implementation("com.utsman.smartmarker:ext-googlemaps:1.3.2@aar")
    // ripple animation
//    implementation ("com.github.aarsy.googlemapsrippleeffect:googlemapsrippleeffect:1.0.2")

    implementation("com.github.TecOrb-Developers:HRMarkerAnimation:fe6f64e75b")



    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.gms:play-services-places:17.0.0")
    implementation("com.google.maps:google-maps-services:0.2.5")
    implementation("com.google.maps.android:android-maps-utils:0.5")
    implementation("com.firebase:geofire-java:3.0.0")

    // GEOCODERS
    implementation("com.google.android.libraries.places:places:3.2.0")
    implementation("com.google.android.gms:play-services-places:17.0.0")

    implementation("com.github.MKergall:osmbonuspack:6.9.0")     // OpenStreet Libraries


    // Chat
    implementation("com.github.stfalcon-studio:Chatkit:0.4.1")
}