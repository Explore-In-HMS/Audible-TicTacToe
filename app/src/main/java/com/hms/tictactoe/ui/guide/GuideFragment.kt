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

package com.hms.tictactoe.ui.guide

import android.Manifest
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hms.tictactoe.R
import com.hms.tictactoe.base.BaseFragment
import com.hms.tictactoe.data.model.VoiceCommand
import com.hms.tictactoe.databinding.FragmentGuideBinding
import com.hms.tictactoe.utils.*
import com.hms.tictactoe.utils.PermissionUtil.launchSinglePermission
import com.hms.tictactoe.utils.PermissionUtil.registerPermission
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
/*
Here Players see how to play game
 */
@AndroidEntryPoint
class GuideFragment : BaseFragment<GuideViewModel, FragmentGuideBinding>() {

    private val viewModel: GuideViewModel by viewModels()

    override fun getFragmentViewModel(): GuideViewModel = viewModel

    private var textToSpeechHelper: TextToSpeechHelper? = null

    @Inject
    lateinit var asrHelper: ASRHelper

    private val recordAudioPermission = registerPermission {
        onRecordAudioPermission(it)
    }

    override fun getFragmentViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGuideBinding {
        return FragmentGuideBinding.inflate(inflater, container, false)
    }

    override fun setupUi() {
        super.setupUi()
        initTTS()
        recordAudioPermission.launchSinglePermission(Manifest.permission.RECORD_AUDIO)
    }

    override fun setupListeners() {
        super.setupListeners()

        fragmentViewBinding.textViewBackArrow.setOnClickListener {
            findNavController().navigateUp()
        }

        fragmentViewBinding.buttonPlay.setOnClickListener {
            val text = fragmentViewBinding.textViewContent.text.toString()
            textToSpeechHelper?.startTTS(text)
        }

    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.isListening.observe(viewLifecycleOwner) { isListening ->
            if (isListening) {
                fragmentViewBinding.linearLayoutListening.visibility = View.VISIBLE
            } else {
                fragmentViewBinding.linearLayoutListening.visibility = View.INVISIBLE
            }
        }
    }

    private fun onRecordAudioPermission(state: PermissionUtil.PermissionState) {
        when (state) {
            PermissionUtil.PermissionState.Denied -> {

            }
            PermissionUtil.PermissionState.Granted -> {
                if (!viewModel.isASRActive) {
                    viewModel.isASRActive = true
                    startListening()
                }
            }
            PermissionUtil.PermissionState.PermanentlyDenied -> {

            }
        }
    }

    private fun startListening() {
        asrHelper.listener = onSpeechEventListener
        asrHelper.startRecognizing()
    }

    private val onSpeechEventListener = object : ASRHelper.OnSpeechEventListener {

        override fun onStartListening() {
            viewModel.setListeningVisibility(true)
        }

        override fun onStartCapturing() {}

        override fun onResults(finalResults: String) {
            viewModel.setListeningVisibility(false)
            val voiceCommand = VoiceCommandManager.getCommandFromText(finalResults)
            checkVoiceCommand(voiceCommand)
        }

        override fun onError(errorMessage: String) {
            viewModel.setListeningVisibility(false)
            startListening()
        }
    }
    // Check voice commands
    private fun checkVoiceCommand(voiceCommand: VoiceCommand) {
        when (voiceCommand) {
            VoiceCommand.QUIT -> Utils.closeApp(requireActivity())
            VoiceCommand.BACK -> findNavController().navigateUp()
            else -> startListening()
        }
    }

    // Initialize Text to speech
    private fun initTTS() {

        textToSpeechHelper = TextToSpeechHelper { ttsPlayingStatus ->
            when (ttsPlayingStatus) {
                TTSPlayingStatus.PLAYING -> {
                    fragmentViewBinding.buttonPlay.setImageResource(R.drawable.pause_arrow)
                }

                TTSPlayingStatus.PAUSED -> {
                    fragmentViewBinding.buttonPlay.setImageResource(R.drawable.play_arrow)
                }

                TTSPlayingStatus.NOT_STARTED -> {
                    fragmentViewBinding.buttonPlay.setImageResource(R.drawable.play_arrow)
                }
            }
        }

    }

    //Destroy text to speech
    override fun onDestroy() {
        textToSpeechHelper?.destroyTTS()
        super.onDestroy()
    }


}