package android.os


interface ITextToSpeechService  {

    // Función para hablar un texto
    fun speak(text: String)

    // Función para detener la reproducción de voz
    fun stop()
}