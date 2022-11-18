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

package com.hms.tictactoe.ui.game

import android.Manifest
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hms.tictactoe.R
import com.hms.tictactoe.base.BaseFragment
import com.hms.tictactoe.data.model.*
import com.hms.tictactoe.databinding.FragmentGameBinding
import com.hms.tictactoe.utils.ASRHelper
import com.hms.tictactoe.utils.PermissionUtil
import com.hms.tictactoe.utils.PermissionUtil.launchSinglePermission
import com.hms.tictactoe.utils.PermissionUtil.registerPermission
import com.hms.tictactoe.utils.Utils
import com.hms.tictactoe.utils.VoiceCommandManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/*
Here Players see game area and play game
 */
@AndroidEntryPoint
class GameFragment : BaseFragment<GameViewModel, FragmentGameBinding>() {

    private val viewModel: GameViewModel by viewModels()

    override fun getFragmentViewModel(): GameViewModel = viewModel

    //ASR helper is injected with Hilt
    @Inject
    lateinit var asrHelper: ASRHelper

    private val recordAudioPermission = registerPermission {
        onRecordAudioPermission(it)
    }

    override fun getFragmentViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGameBinding {
        return FragmentGameBinding.inflate(inflater, container, false)
    }

    override fun setupUi() {
        super.setupUi()
        recordAudioPermission.launchSinglePermission(Manifest.permission.RECORD_AUDIO)
    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.whoseTurn.observe(viewLifecycleOwner) { player ->
            focusOnPlayer(player)
        }

        viewModel.isListening.observe(viewLifecycleOwner) { isListening ->
            if (isListening) {
                fragmentViewBinding.linearLayoutListening.visibility = View.VISIBLE
            } else {
                fragmentViewBinding.linearLayoutListening.visibility = View.INVISIBLE
            }
        }
    }

    override fun setupListeners() {
        super.setupListeners()

        fragmentViewBinding.imageViewFieldOne.setOnClickListener {
            makeMove(1)
        }
        fragmentViewBinding.imageViewFieldTwo.setOnClickListener {
            makeMove(2)
        }
        fragmentViewBinding.imageViewFieldThree.setOnClickListener {
            makeMove(3)
        }
        fragmentViewBinding.imageViewFieldFour.setOnClickListener {
            makeMove(4)
        }
        fragmentViewBinding.imageViewFieldFive.setOnClickListener {
            makeMove(5)
        }
        fragmentViewBinding.imageViewFieldSix.setOnClickListener {
            makeMove(6)
        }
        fragmentViewBinding.imageViewFieldSeven.setOnClickListener {
            makeMove(7)
        }
        fragmentViewBinding.imageViewFieldEight.setOnClickListener {
            makeMove(8)
        }
        fragmentViewBinding.imageViewFieldNine.setOnClickListener {
            makeMove(9)
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
    // Start listener for asr
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

    // Checks who wins the game
    private fun checkGameStatus(
        completedMove: Int
    ): GameStatus {
        return if (viewModel.gameArea[0] == GameAreaStatus.PLAYED_X && viewModel.gameArea[1] == GameAreaStatus.PLAYED_X && viewModel.gameArea[2] == GameAreaStatus.PLAYED_X) {
            GameStatus.WINNER_BLACK
        } else if (viewModel.gameArea[0] == GameAreaStatus.PLAYED_O && viewModel.gameArea[1] == GameAreaStatus.PLAYED_O && viewModel.gameArea[2] == GameAreaStatus.PLAYED_O) {
            GameStatus.WINNER_RED
        } else if (viewModel.gameArea[0] == GameAreaStatus.PLAYED_X && viewModel.gameArea[3] == GameAreaStatus.PLAYED_X && viewModel.gameArea[6] == GameAreaStatus.PLAYED_X) {
            GameStatus.WINNER_BLACK
        } else if (viewModel.gameArea[0] == GameAreaStatus.PLAYED_O && viewModel.gameArea[3] == GameAreaStatus.PLAYED_O && viewModel.gameArea[6] == GameAreaStatus.PLAYED_O) {
            GameStatus.WINNER_RED
        } else if (viewModel.gameArea[0] == GameAreaStatus.PLAYED_X && viewModel.gameArea[4] == GameAreaStatus.PLAYED_X && viewModel.gameArea[8] == GameAreaStatus.PLAYED_X) {
            GameStatus.WINNER_BLACK
        } else if (viewModel.gameArea[0] == GameAreaStatus.PLAYED_O && viewModel.gameArea[4] == GameAreaStatus.PLAYED_O && viewModel.gameArea[8] == GameAreaStatus.PLAYED_O) {
            GameStatus.WINNER_RED
        } else if (viewModel.gameArea[1] == GameAreaStatus.PLAYED_X && viewModel.gameArea[4] == GameAreaStatus.PLAYED_X && viewModel.gameArea[7] == GameAreaStatus.PLAYED_X) {
            GameStatus.WINNER_BLACK
        } else if (viewModel.gameArea[1] == GameAreaStatus.PLAYED_O && viewModel.gameArea[4] == GameAreaStatus.PLAYED_O && viewModel.gameArea[7] == GameAreaStatus.PLAYED_O) {
            GameStatus.WINNER_RED
        } else if (viewModel.gameArea[2] == GameAreaStatus.PLAYED_X && viewModel.gameArea[5] == GameAreaStatus.PLAYED_X && viewModel.gameArea[8] == GameAreaStatus.PLAYED_X) {
            GameStatus.WINNER_BLACK
        } else if (viewModel.gameArea[2] == GameAreaStatus.PLAYED_O && viewModel.gameArea[5] == GameAreaStatus.PLAYED_O && viewModel.gameArea[8] == GameAreaStatus.PLAYED_O) {
            GameStatus.WINNER_RED
        } else if (viewModel.gameArea[2] == GameAreaStatus.PLAYED_X && viewModel.gameArea[4] == GameAreaStatus.PLAYED_X && viewModel.gameArea[6] == GameAreaStatus.PLAYED_X) {
            GameStatus.WINNER_BLACK
        } else if (viewModel.gameArea[2] == GameAreaStatus.PLAYED_O && viewModel.gameArea[4] == GameAreaStatus.PLAYED_O && viewModel.gameArea[6] == GameAreaStatus.PLAYED_O) {
            GameStatus.WINNER_RED
        } else if (viewModel.gameArea[3] == GameAreaStatus.PLAYED_X && viewModel.gameArea[4] == GameAreaStatus.PLAYED_X && viewModel.gameArea[5] == GameAreaStatus.PLAYED_X) {
            GameStatus.WINNER_BLACK
        } else if (viewModel.gameArea[3] == GameAreaStatus.PLAYED_O && viewModel.gameArea[4] == GameAreaStatus.PLAYED_O && viewModel.gameArea[5] == GameAreaStatus.PLAYED_O) {
            GameStatus.WINNER_RED
        } else if (viewModel.gameArea[6] == GameAreaStatus.PLAYED_X && viewModel.gameArea[7] == GameAreaStatus.PLAYED_X && viewModel.gameArea[8] == GameAreaStatus.PLAYED_X) {
            GameStatus.WINNER_BLACK
        } else if (viewModel.gameArea[6] == GameAreaStatus.PLAYED_O && viewModel.gameArea[7] == GameAreaStatus.PLAYED_O && viewModel.gameArea[8] == GameAreaStatus.PLAYED_O) {
            GameStatus.WINNER_RED
        } else if (completedMove == 9) {
            GameStatus.DRAW
        } else {
            GameStatus.CONTINUE
        }
    }

    // Check voice commands
    fun checkVoiceCommand(voiceCommand: VoiceCommand) {
        when (voiceCommand) {
            VoiceCommand.QUIT -> Utils.closeApp(requireActivity())
            VoiceCommand.NUMBER_1 -> makeMove(1)
            VoiceCommand.NUMBER_2 -> makeMove(2)
            VoiceCommand.NUMBER_3 -> makeMove(3)
            VoiceCommand.NUMBER_4 -> makeMove(4)
            VoiceCommand.NUMBER_5 -> makeMove(5)
            VoiceCommand.NUMBER_6 -> makeMove(6)
            VoiceCommand.NUMBER_7 -> makeMove(7)
            VoiceCommand.NUMBER_8 -> makeMove(8)
            VoiceCommand.NUMBER_9 -> makeMove(9)
            else -> startListening()
        }
    }

    // Here player makes its move
    private fun makeMove(numberOfField: Int) {

        val currentPlayer = viewModel.whoseTurn.value

        if (viewModel.gameArea[numberOfField - 1] != GameAreaStatus.EMPTY) {
            Toast.makeText(
                requireContext(),
                getString(R.string.game_invalid_move),
                Toast.LENGTH_SHORT
            ).show()
            if (viewModel.isASRActive) {
                startListening()
            }
            return
        }

        when (currentPlayer) {
            Player.PLAYER_BLACK -> {
                getImageViewFieldFromNumber(numberOfField).setImageResource(R.drawable.ic_field_x)
                viewModel.gameArea[numberOfField - 1] = GameAreaStatus.PLAYED_X
                viewModel.setWhosePlayer(Player.PLAYER_RED)
            }
            Player.PLAYER_RED -> {
                getImageViewFieldFromNumber(numberOfField).setImageResource(R.drawable.ic_field_o)
                viewModel.gameArea[numberOfField - 1] = GameAreaStatus.PLAYED_O
                viewModel.setWhosePlayer(Player.PLAYER_BLACK)
            }
            else -> {}
        }
        viewModel.completedMove += 1
        checkGameIsFinished()
    }

    //checks if the game is over
    private fun checkGameIsFinished() {
        when (checkGameStatus(viewModel.completedMove)) {
            GameStatus.WINNER_BLACK -> {
                val gameResult = GameResult(false, Player.PLAYER_BLACK, viewModel.gameArea)
                navigateToResultScreen(gameResult)
            }
            GameStatus.WINNER_RED -> {
                val gameResult = GameResult(false, Player.PLAYER_RED, viewModel.gameArea)
                navigateToResultScreen(gameResult)
            }
            GameStatus.DRAW -> {
                val gameResult = GameResult(true, null, viewModel.gameArea)
                navigateToResultScreen(gameResult)
            }
            GameStatus.CONTINUE -> {
                if (viewModel.isASRActive) {
                    startListening()
                }
            }
        }
    }

    // Go game result fragment with arguments
    private fun navigateToResultScreen(gameResult: GameResult) {
        val action =
            GameFragmentDirections.actionGameFragmentToResultFragment(
                gameResult
            )
        findNavController().navigate(action)
    }

    // Change image according to parameter
    private fun getImageViewFieldFromNumber(numberOfField: Int): ImageView {
        return when (numberOfField) {
            1 -> fragmentViewBinding.imageViewFieldOne
            2 -> fragmentViewBinding.imageViewFieldTwo
            3 -> fragmentViewBinding.imageViewFieldThree
            4 -> fragmentViewBinding.imageViewFieldFour
            5 -> fragmentViewBinding.imageViewFieldFive
            6 -> fragmentViewBinding.imageViewFieldSix
            7 -> fragmentViewBinding.imageViewFieldSeven
            8 -> fragmentViewBinding.imageViewFieldEight
            9 -> fragmentViewBinding.imageViewFieldNine
            else -> {
                fragmentViewBinding.imageViewFieldOne
            }
        }
    }

    // Change player turn
    private fun focusOnPlayer(player: Player) {
        when (player) {
            Player.PLAYER_BLACK -> {
                fragmentViewBinding.imageViewPlayerBlack.setImageResource(R.drawable.ic_user_black_active)
                fragmentViewBinding.imageViewPlayerBlackMic.visibility = View.VISIBLE
                fragmentViewBinding.imageViewPlayerRedMic.visibility = View.INVISIBLE
                fragmentViewBinding.imageViewPlayerRed.setImageResource(R.drawable.ic_user_red_passive)
                fragmentViewBinding.textViewPlayerTurn.text =
                    getString(R.string.game_turn_black)
                fragmentViewBinding.textViewPlayerTurn.visibility = View.VISIBLE
                fragmentViewBinding.textViewPlayerTurn.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            }
            Player.PLAYER_RED -> {
                fragmentViewBinding.imageViewPlayerRed.setImageResource(R.drawable.ic_user_red_active)
                fragmentViewBinding.imageViewPlayerRedMic.visibility = View.VISIBLE
                fragmentViewBinding.imageViewPlayerBlackMic.visibility = View.INVISIBLE
                fragmentViewBinding.imageViewPlayerBlack.setImageResource(R.drawable.ic_user_black_passive)
                fragmentViewBinding.textViewPlayerTurn.text = getString(R.string.game_turn_red)
                fragmentViewBinding.textViewPlayerTurn.visibility = View.VISIBLE
                fragmentViewBinding.textViewPlayerTurn.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorTurnRedText
                    )
                )
            }
        }
    }
}













