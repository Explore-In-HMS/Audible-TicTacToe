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

import android.os.Bundle
import android.util.Pair
import com.huawei.hms.mlsdk.tts.*

// Handling TTS interactions
class TextToSpeechHelper(
    private val isPlayingCallback: (TTSPlayingStatus) -> Unit
) {

    private var mlTtsConfig: MLTtsConfig? = null
    private var mlTtsEngine: MLTtsEngine? = null

    private var ttsPlayingStatus = TTSPlayingStatus.NOT_STARTED

    /**
     * ML Kit TTS has a 500 character limit. We split to reduce the size using punctuations.
     */
    fun startTTS(text: String) {
        mlTtsEngine?.let {

            when (ttsPlayingStatus) {
                TTSPlayingStatus.NOT_STARTED -> {
                    it.stop()
                    val sentences = text.split("\n|\\.(?!\\d)|(?<!\\d)\\.").toTypedArray()
                    for (sentence in sentences)
                        if (sentence.length < 500) {
                            it.speak(sentence, MLTtsEngine.QUEUE_APPEND)
                        }
                }

                TTSPlayingStatus.PLAYING -> {
                    pauseTTS()
                }
                TTSPlayingStatus.PAUSED -> {
                    resumeTTS()
                }
            }
        }
    }

    // Pause playback
    fun pauseTTS() {
        if (ttsPlayingStatus == TTSPlayingStatus.PLAYING) {
            mlTtsEngine?.pause()
        }
    }

    // Resume playback.
    fun resumeTTS() {
        if (ttsPlayingStatus == TTSPlayingStatus.PAUSED) {
            mlTtsEngine?.resume()
        }
    }

    // Stop playback.
    fun stopTTS() {
        mlTtsEngine?.stop()
    }

    // Destroy playback.
    fun destroyTTS() {
        mlTtsEngine?.shutdown()
        mlTtsEngine = null
    }

    // Initialize TTS with config
    init {
        isPlayingCallback.invoke(TTSPlayingStatus.NOT_STARTED)
        mlTtsConfig = MLTtsConfig().apply {
            language = MLTtsConstants.TTS_EN_US
            person = MLTtsConstants.TTS_SPEAKER_FEMALE_EN
            speed = 1.0f
            volume = 1.0f
        }

        mlTtsEngine = MLTtsEngine(mlTtsConfig)

        val callback: MLTtsCallback = object : MLTtsCallback {
            override fun onError(taskId: String?, error: MLTtsError?) {
                ttsPlayingStatus = TTSPlayingStatus.NOT_STARTED
            }

            override fun onWarn(taskId: String?, warn: MLTtsWarn?) {

            }

            override fun onRangeStart(taskId: String?, start: Int, end: Int) {

            }

            override fun onAudioAvailable(
                p0: String?,
                p1: MLTtsAudioFragment?,
                p2: Int,
                p3: Pair<Int, Int>?,
                p4: Bundle?
            ) {

            }

            // Callback method of a TTS event
            override fun onEvent(taskId: String?, eventId: Int, bundle: Bundle?) {

                when (eventId) {
                    MLTtsConstants.EVENT_PLAY_START -> {
                        ttsPlayingStatus = TTSPlayingStatus.PLAYING
                        isPlayingCallback.invoke(ttsPlayingStatus)
                    }
                    MLTtsConstants.EVENT_PLAY_STOP -> {
                        ttsPlayingStatus = TTSPlayingStatus.NOT_STARTED
                        isPlayingCallback.invoke(ttsPlayingStatus)
                    }
                    MLTtsConstants.EVENT_PLAY_RESUME -> {
                        ttsPlayingStatus = TTSPlayingStatus.PLAYING
                        isPlayingCallback.invoke(ttsPlayingStatus)
                    }
                    MLTtsConstants.EVENT_PLAY_PAUSE -> {
                        ttsPlayingStatus = TTSPlayingStatus.PAUSED
                        isPlayingCallback.invoke(ttsPlayingStatus)
                    }
                }
            }

        }

        mlTtsEngine?.setTtsCallback(callback)

    }

}

// TTS Playing Status
enum class TTSPlayingStatus { NOT_STARTED, PLAYING, PAUSED }