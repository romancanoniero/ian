package com.iyr.ian.core

/*
fun Context.scheduleLocationUpdateJob() {
    val jobScheduler = this.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    val componentName = ComponentName(this, NewLocationJobService::class.java)

    val jobInfo = JobInfo.Builder(1, componentName)
        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        .setRequiresCharging(false)
        .setPersisted(true)
       // .setPeriodic(10 * 1000)
       // .setPeriodic(30 * 1000)
        //  .setExtras(extras)
        .build()

    val resultCode = jobScheduler.schedule(jobInfo)
    if (resultCode == JobScheduler.RESULT_SUCCESS) {
        // El trabajo se programó con éxito
        Log.d("LOCATION_JOB_SERVICE", "Voy a iniciar el trackeo")
        this.speak("Iniciando servicio de Tracking")

    } else {
        // Error al programar el trabajo
        Log.d("LOCATION_JOB_SERVICE", "No pude iniciar el trackeo")

        this.speak("Error al iniciar servicio de Tracking")
    }
}
*/