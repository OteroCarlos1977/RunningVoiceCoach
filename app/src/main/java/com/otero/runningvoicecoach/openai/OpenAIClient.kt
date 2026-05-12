package com.otero.runningvoicecoach.openai

import com.otero.runningvoicecoach.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class OpenAIClient(
    private val apiKey: String = BuildConfig.OPENAI_API_KEY,
    private val model: String = BuildConfig.OPENAI_MODEL,
    private val httpClient: OkHttpClient = defaultHttpClient()
) {
    suspend fun generateRunningMessage(context: RunningAlertContext): String {
        if (apiKey.isBlank()) {
            throw OpenAIClientException("OpenAI API key no configurada.")
        }

        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(RESPONSES_URL)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", JSON_MEDIA_TYPE.toString())
                .post(buildRequestBody(context).toRequestBody(JSON_MEDIA_TYPE))
                .build()

            try {
                httpClient.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string().orEmpty()

                    if (!response.isSuccessful) {
                        throw OpenAIClientException("OpenAI respondio con HTTP ${response.code}.")
                    }

                    parseOutputText(responseBody)
                        ?: throw OpenAIClientException("OpenAI no devolvio texto utilizable.")
                }
            } catch (exception: IOException) {
                throw OpenAIClientException("No se pudo conectar con OpenAI.", exception)
            }
        }
    }

    private fun buildRequestBody(context: RunningAlertContext): String {
        return JSONObject()
            .put("model", model)
            .put("instructions", SYSTEM_PROMPT)
            .put("input", buildUserInput(context))
            .put("max_output_tokens", 80)
            .toString()
    }

    private fun buildUserInput(context: RunningAlertContext): String {
        return JSONObject()
            .put("tipo_alerta", context.alertType.name)
            .put("bloque_actual", context.currentStepName ?: JSONObject.NULL)
            .put("ritmo_objetivo_segundos_por_km", context.targetPaceSecondsPerKm ?: JSONObject.NULL)
            .put("ritmo_actual_segundos_por_km", context.currentPaceSecondsPerKm ?: JSONObject.NULL)
            .put("diferencia_ritmo_segundos", context.paceDifferenceSeconds ?: JSONObject.NULL)
            .put("distancia_restante_metros", context.remainingDistanceMeters ?: JSONObject.NULL)
            .put("tiempo_restante_segundos", context.remainingTimeSeconds ?: JSONObject.NULL)
            .toString()
    }

    private fun parseOutputText(responseBody: String): String? {
        val root = JSONObject(responseBody)
        val directOutputText = root.optString("output_text")
            .trim()
            .takeIf { it.isNotBlank() }

        if (directOutputText != null) {
            return directOutputText
        }

        val output = root.optJSONArray("output") ?: return null
        for (outputIndex in 0 until output.length()) {
            val item = output.optJSONObject(outputIndex) ?: continue
            val content = item.optJSONArray("content") ?: continue
            val text = firstOutputText(content)
            if (text != null) {
                return text
            }
        }

        return null
    }

    private fun firstOutputText(content: JSONArray): String? {
        for (contentIndex in 0 until content.length()) {
            val contentItem = content.optJSONObject(contentIndex) ?: continue
            val text = contentItem.optString("text")
                .trim()
                .takeIf { contentItem.optString("type") == "output_text" && it.isNotBlank() }

            if (text != null) {
                return text
            }
        }

        return null
    }

    companion object {
        private const val RESPONSES_URL = "https://api.openai.com/v1/responses"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        private const val SYSTEM_PROMPT =
            "Sos un asistente de running. Genera mensajes breves, claros y utiles para un corredor durante un entrenamiento. " +
                "No inventes datos. Usa solamente los datos recibidos. Responde en espanol rioplatense. " +
                "No des consejos medicos. Maximo 18 palabras."

        private fun defaultHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS)
                .callTimeout(4, TimeUnit.SECONDS)
                .build()
        }
    }
}
