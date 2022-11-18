/*
 *
 *  * Copyright 2021. Huawei Technologies Co., Ltd. All rights reserved.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *  *
 *
 */
package com.hms.tictactoe.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.huawei.hms.mlsdk.asr.MLAsrConstants
import com.huawei.hms.mlsdk.asr.MLAsrListener
import com.huawei.hms.mlsdk.asr.MLAsrRecognizer
import javax.inject.Inject
import javax.inject.Singleton

// Handling ASR interactions
@Singleton
class ASRHelper @Inject constructor(
    val context: Context
) {

    companion object {
        const val TAG = "ASRHelper"
    }

    var lastProcessTime: Long = 0

    private var mSpeechRecognizer: MLAsrRecognizer? = null
    var listener: OnSpeechEventListener? = null


    private val speechRecognizerIntent = Intent(MLAsrConstants.ACTION_HMS_ASR_SPEECH).apply {
        // Set the language that can be recognized to English. If this parameter is not set, English is recognized by default. Example: "zh-CN": Chinese; "en-US": English; "fr-FR": French; "es-ES": Spanish; "de-DE": German; "it-IT": Italian; "ar": Arabic; "th=TH": Thai; "ms-MY": Malay; "fil-PH": Filipino; "tr-TR": Turkish.
        putExtra(
            MLAsrConstants.LANGUAGE,
            "en-US"
        ) // Set to return the recognition result along with the speech. If you ignore the setting, this mode is used by default. Options are as follows:
            // MLAsrConstants.FEATURE_WORDFLUX: Recognizes and returns texts through onRecognizingResults.
            // MLAsrConstants.FEATURE_ALLINONE: After the recognition is complete, texts are returned through onResults.
            .putExtra(
                MLAsrConstants.FEATURE,
                MLAsrConstants.FEATURE_ALLINONE
            )
    }


    fun startRecognizing() {
        try {
            mSpeechRecognizer = MLAsrRecognizer.createAsrRecognizer(context)
            mSpeechRecognizer?.setAsrListener(mlAsrListener)
            mSpeechRecognizer?.startRecognizing(speechRecognizerIntent)
        } catch (exception: Exception) {
            mlAsrListener.onError(1, exception.message)
        }
    }

    fun destroyRecognizing() {
        mSpeechRecognizer?.destroy()
    }

    interface OnSpeechEventListener {
        fun onStartListening()
        fun onStartCapturing()
        fun onResults(finalResults: String)
        fun onError(errorMessage: String)
    }

    private val mlAsrListener = object : MLAsrListener {

        override fun onResults(bundle: Bundle?) {
            val now = System.currentTimeMillis()
            if (lastProcessTime == 0L || (now - lastProcessTime) >= 1000) {
                lastProcessTime = now
                Log.d(TAG, "onResults:${bundle?.getString(MLAsrRecognizer.RESULTS_RECOGNIZED)}")
                val text = bundle?.getString(MLAsrRecognizer.RESULTS_RECOGNIZED) ?: ""
                listener?.onResults(text)
            }
        }

        override fun onRecognizingResults(partialResults: Bundle?) {

        }

        override fun onError(error: Int, errorMessage: String?) {
            Log.e(TAG, "ErrorId:$error, errorMessage:$errorMessage")
            listener?.onError(errorMessage ?: "")
        }

        override fun onStartListening() {
            listener?.onStartListening()
        }

        override fun onStartingOfSpeech() {
            listener?.onStartCapturing()
        }

        override fun onVoiceDataReceived(data: ByteArray?, energy: Float, bundle: Bundle?) {

        }

        override fun onState(state: Int, params: Bundle?) {
            val message = when (state) {
                MLAsrConstants.STATE_LISTENING -> "Listening..."
                MLAsrConstants.STATE_NO_NETWORK -> "No network"
                MLAsrConstants.STATE_NO_SOUND -> "No sound"
                MLAsrConstants.STATE_NO_SOUND_TIMES_EXCEED -> "Silence"
                MLAsrConstants.STATE_NO_UNDERSTAND -> "Not recognized"
                MLAsrConstants.STATE_WAITING -> "Waiting"
                else -> "Else"
            }
            Log.d(TAG, "onState:$message")
        }

    }

}