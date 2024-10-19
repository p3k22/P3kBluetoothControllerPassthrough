package com.p3kIndustries.P3kBT2PC.services

import com.p3kIndustries.P3kBT2PC.R
import com.p3kIndustries.P3kBT2PC.events.Device
import com.p3kIndustries.P3kBT2PC.ui.MainUiReferences
import com.p3kIndustries.P3kBT2PC.models.Input
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Locale
import java.util.concurrent.TimeUnit

class Server(
    private val logger: Logger,
    private val ui: MainUiReferences,
    private val device: Device)
{

    var connectedPassThrough = false
    var isSendingData = false

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .writeTimeout(4, TimeUnit.SECONDS)
        .build()

    private var connectionState = 0
    private var ipAddress = ""
    private var port = 8080
    private var sendDataJob: Job? = null


    private suspend fun sendControllerInput(input: Input): Int
    {
        return withContext(Dispatchers.IO) {
            try
            {
                val isLogging = false
                val json = Json.encodeToString(input)
                val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = json.toRequestBody(jsonMediaType)
                val request = Request.Builder()
                    .url("http://$ipAddress:$port")
                    .post(requestBody)
                    .header("Connection", "close")
                    .build()

                withTimeout(1000L) {  // Set a 1-second timeout for the HTTP request
                    httpClient.newCall(request).execute().use { response ->
                        if (isLogging)
                        {
                            if (response.isSuccessful)
                            {
                                val responseBody = response.body?.string() ?: "No Response Body"
                                logger.addLog("Success: HTTP ${response.code} - $responseBody")
                            }
                            else
                            {
                                logger.addLog("Error: HTTP ${response.code} - ${response.message}")
                            }
                        }
                    }
                }

                if (connectionState == 0)
                {
                    logger.addLog("Sending controller input to $ipAddress:$port")
                    connectionState = 1
                }
                return@withContext 1  // Return success code 1
            }
            catch (e: TimeoutCancellationException)
            {
                logger.addLog("Send request timed out after 1000ms. ${e.message}")
                connectionState = 0
                return@withContext -1  // Return timeout error code -1
            }
            catch (e: Exception)
            {
                logger.addLog("Network error: ${e.localizedMessage}")
                connectionState = 0
                return@withContext -2  // Return general network error code -2
            }
        }
    }

    fun startSendingData(thisIPAddress: String, coroutineScope: CoroutineScope)
    {
        logger.addLog("Starting GetInput() service")

        setNetworkIP(thisIPAddress)
        isSendingData = true
        sendDataJob = coroutineScope.launch {
            while (isActive && isSendingData)
            {
                delay(100)
                val response = sendControllerInput(device.getControllerInput())
                connectedPassThrough = response == 1
                //logger.addLog("Resp:" + response)
                when (response)
                {
                    1 ->
                    {
                        ui.statusTextView.text = "Status: Connected"
                        ui.connectButton.text = "Disconnect"
                        ui.statusDot.setBackgroundResource(R.drawable.greencircle)
                        ui.autoDetectButton.isEnabled = false
                    }

                    -1 ->
                    {
                        logger.addLog("Retrying connection")
                    }

                    -2, 0 ->
                    {
                        logger.addLog("Failed completely.")
                        stopSendingData(coroutineScope)
                        break
                    }
                }
                delay(10)
            }
        }
    }

    fun stopSendingData(coroutineScope: CoroutineScope)
    {
        isSendingData = false

        ui.statusTextView.text = "Status: Disconnected"
        ui.connectButton.text = "Connect"
        ui.statusDot.setBackgroundResource(R.drawable.redcircle)
        ui.autoDetectButton.isEnabled = true

        sendDataJob?.cancel()
        sendDataJob = null

        coroutineScope.launch {
            delay(100)
            logger.addLog("Stopped GetInput() service")
        }
    }

    private fun setNetworkIP(thisIPAddress: String, thisPort: Int=8080)
    {
        // Assign the values first
        ipAddress = thisIPAddress
        port = thisPort

        // Update UI fields
        ui.ipEditText.setText(ipAddress)
        ui.portEditText.setText(String.format(Locale.getDefault(), "%d", port))
    }

}