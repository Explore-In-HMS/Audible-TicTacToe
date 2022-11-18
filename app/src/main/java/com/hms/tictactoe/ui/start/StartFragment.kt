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
package com.hms.tictactoe.ui.start

import android.Manifest
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hms.tictactoe.R
import com.hms.tictactoe.base.BaseFragment
import com.hms.tictactoe.data.model.VoiceCommand
import com.hms.tictactoe.databinding.FragmentStartBinding
import com.hms.tictactoe.utils.ASRHelper
import com.hms.tictactoe.utils.PermissionUtil
import com.hms.tictactoe.utils.PermissionUtil.launchSinglePermission
import com.hms.tictactoe.utils.PermissionUtil.registerPermission
import com.hms.tictactoe.utils.Utils
import com.hms.tictactoe.utils.VoiceCommandManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/*
Players see Start Screen
 */
@AndroidEntryPoint
class StartFragment : BaseFragment<StartViewModel, FragmentStartBinding>() {

    private val viewModel: StartViewModel by viewModels()

    override fun getFragmentViewModel(): StartViewModel = viewModel

    @Inject
    lateinit var asrHelper: ASRHelper

    private val recordAudioPermission = registerPermission {
        onRecordAudioPermission(it)
    }

    override fun getFragmentViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentStartBinding {
        return FragmentStartBinding.inflate(inflater, container, false)
    }

    override fun setupUi() {
        super.setupUi()
        recordAudioPermission.launchSinglePermission(Manifest.permission.RECORD_AUDIO)
    }

    override fun setupListeners() {
        super.setupListeners()

        // Go to game screen
        fragmentViewBinding.buttonStartGame.setOnClickListener {
            findNavController().navigate(R.id.action_startFragment_to_gameFragment)
        }

        // Go to guide screen
        fragmentViewBinding.buttonGuide.setOnClickListener {
            findNavController().navigate(R.id.action_startFragment_to_guideFragment)
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

    // Start listener for ASR
    private fun startListening() {
        asrHelper.listener = onSpeechEventListener
        asrHelper.startRecognizing()
    }

    // Create ASR helper object for listener
    private val onSpeechEventListener = object : ASRHelper.OnSpeechEventListener {

        override fun onStartListening() {
            viewModel.setListeningVisibility(true)
        }

        override fun onStartCapturing() {}

        override fun onResults(finalResults: String) {
            viewModel.setListeningVisibility(false)
            val voiceCommand = VoiceCommandManager.getCommandFromText(finalResults)
            when (voiceCommand) {
                VoiceCommand.START_GAME -> navigateToGameScreen()
                VoiceCommand.HOW_TO_PLAY -> navigateToGuideScreen()
                VoiceCommand.QUIT -> Utils.closeApp(requireActivity())
                else -> startListening()
            }

        }

        override fun onError(errorMessage: String) {
            viewModel.setListeningVisibility(false)
            startListening()
        }
    }

    private fun onRecordAudioPermission(state: PermissionUtil.PermissionState) {
        when (state) {
            PermissionUtil.PermissionState.Denied -> {
                findNavController().navigate(R.id.action_startFragment_to_permissionFragment)
            }
            PermissionUtil.PermissionState.Granted -> {
                if (!viewModel.shouldWeStartListening) {
                    viewModel.shouldWeStartListening = true
                    startListening()
                }
            }
            PermissionUtil.PermissionState.PermanentlyDenied -> {
                findNavController().navigate(R.id.action_startFragment_to_permissionFragment)
            }
        }
    }

    // Navigate to Game Fragment
    private fun navigateToGameScreen() {
        findNavController().navigate(R.id.action_startFragment_to_gameFragment)
    }

    // Navigate to Guide Fragment
    private fun navigateToGuideScreen() {
        findNavController().navigate(R.id.action_startFragment_to_guideFragment)
    }

    // Starting ASR Helper
    override fun onStart() {
        super.onStart()
        if (viewModel.shouldWeStartListening) {
            startListening()
        }
    }

    //Stop ASR Helper
    override fun onStop() {
        super.onStop()
        if (viewModel.shouldWeStartListening) {
            asrHelper.destroyRecognizing()
        }
    }

    // Destroying ASR Helper
    override fun onDestroy() {
        asrHelper.destroyRecognizing()
        super.onDestroy()
    }
}