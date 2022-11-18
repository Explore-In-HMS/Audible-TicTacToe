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

import com.hms.tictactoe.data.model.VoiceCommand
import java.util.*

// Voice commands manager according to text
object VoiceCommandManager {

    fun getCommandFromText(text: String): VoiceCommand {
        return when (text.lowercase(Locale.ENGLISH)) {
            "start game" -> VoiceCommand.START_GAME
            "how to play" -> VoiceCommand.HOW_TO_PLAY
            "play again" -> VoiceCommand.PLAY_AGAIN
            "back" -> VoiceCommand.BACK
            "quit" -> VoiceCommand.QUIT
            "one", "1" -> VoiceCommand.NUMBER_1
            "two", "2", "to" -> VoiceCommand.NUMBER_2
            "three", "3", "tree" -> VoiceCommand.NUMBER_3
            "four", "4", "4th", "for" -> VoiceCommand.NUMBER_4
            "five", "5", "Y5" -> VoiceCommand.NUMBER_5
            "six", "6" -> VoiceCommand.NUMBER_6
            "seven", "7" -> VoiceCommand.NUMBER_7
            "eight", "8" -> VoiceCommand.NUMBER_8
            "nine", "9" -> VoiceCommand.NUMBER_9
            else -> VoiceCommand.UNKNOWN

        }
    }
}