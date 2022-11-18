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
package com.hms.tictactoe.ui.result

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hms.tictactoe.R
import com.hms.tictactoe.base.BaseFragment
import com.hms.tictactoe.data.model.GameAreaStatus
import com.hms.tictactoe.data.model.Player
import com.hms.tictactoe.data.model.VoiceCommand
import com.hms.tictactoe.databinding.FragmentResultBinding
import com.hms.tictactoe.utils.ASRHelper
import com.hms.tictactoe.utils.PermissionUtil
import com.hms.tictactoe.utils.PermissionUtil.launchSinglePermission
import com.hms.tictactoe.utils.PermissionUtil.registerPermission
import com.hms.tictactoe.utils.Utils
import com.hms.tictactoe.utils.VoiceCommandManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// Players see Game Result
@AndroidEntryPoint
class ResultFragment : BaseFragment<ResultViewModel, FragmentResultBinding>() {

    private val viewModel: ResultViewModel by viewModels()

    override fun getFragmentViewModel(): ResultViewModel = viewModel

    @Inject
    lateinit var asrHelper: ASRHelper

    private val resultFragmentArgs by navArgs<ResultFragmentArgs>()

    private val recordAudioPermission = registerPermission {
        onRecordAudioPermission(it)
    }


    override fun getFragmentViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentResultBinding {
        return FragmentResultBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resultFragmentArgs.gameResult?.let { gameResult ->
            if (gameResult.isDraw) {
                fragmentViewBinding.textViewWinner.text = getString(R.string.game_result_draw)
                fragmentViewBinding.congratulations.text =
                    getString(R.string.game_result_draw_desc)
                fragmentViewBinding.textViewWinner.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorDraw
                    )
                )
            }

            when (gameResult.winner) {
                Player.PLAYER_BLACK -> {
                    fragmentViewBinding.textViewWinner.text =
                        getString(R.string.game_result_black_won)
                    fragmentViewBinding.textViewWinner.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black
                        )
                    )
                }
                Player.PLAYER_RED -> {
                    fragmentViewBinding.textViewWinner.text =
                        getString(R.string.game_result_red_won)
                    fragmentViewBinding.textViewWinner.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorTurnRedText
                        )
                    )
                }
                else -> {}
            }

            for (i in gameResult.gameAreaStatus.indices) {
                if (gameResult.gameAreaStatus[i] != GameAreaStatus.EMPTY) {
                    changeImage(i, gameResult.gameAreaStatus[i])
                }
            }

        }
    }

    override fun setupUi() {
        super.setupUi()
        recordAudioPermission.launchSinglePermission(Manifest.permission.RECORD_AUDIO)
    }

    override fun setupListeners() {
        super.setupListeners()

        fragmentViewBinding.buttonPlayAgain.setOnClickListener {
            navigateToGameScreen()
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

    // If mic permission is granted, it starts to listen.
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

    // Check voice command play or quit
    private fun checkVoiceCommand(voiceCommand: VoiceCommand) {
        when (voiceCommand) {
            VoiceCommand.QUIT -> Utils.closeApp(requireActivity())
            VoiceCommand.PLAY_AGAIN -> navigateToGameScreen()
            else -> startListening()
        }
    }

    //Change image in  result screen
    private fun changeImage(index: Int, gameAreaStatus: GameAreaStatus) {
        val images = arrayOf(
            fragmentViewBinding.imageViewFieldOne,
            fragmentViewBinding.imageViewFieldTwo,
            fragmentViewBinding.imageViewFieldThree,
            fragmentViewBinding.imageViewFieldFour,
            fragmentViewBinding.imageViewFieldFive,
            fragmentViewBinding.imageViewFieldSix,
            fragmentViewBinding.imageViewFieldSeven,
            fragmentViewBinding.imageViewFieldEight,
            fragmentViewBinding.imageViewFieldNine
        )
        when (gameAreaStatus) {
            GameAreaStatus.PLAYED_X -> {
                images[index].setImageResource(R.drawable.ic_field_x)
            }
            GameAreaStatus.PLAYED_O -> {
                images[index].setImageResource(R.drawable.ic_field_o)
            }
            else -> {}
        }
    }

    // Play game again
    private fun navigateToGameScreen() {
        findNavController().navigate(R.id.action_resultFragment_to_gameFragment)
    }

}