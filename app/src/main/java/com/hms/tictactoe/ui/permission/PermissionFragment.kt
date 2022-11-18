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
package com.hms.tictactoe.ui.permission

import android.Manifest
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hms.tictactoe.R
import com.hms.tictactoe.base.BaseFragment
import com.hms.tictactoe.databinding.FragmentPermissionBinding
import com.hms.tictactoe.utils.PermissionUtil
import com.hms.tictactoe.utils.PermissionUtil.launchSinglePermission
import com.hms.tictactoe.utils.PermissionUtil.registerPermission
import com.hms.tictactoe.utils.extensions.openAppSystemSettings
import dagger.hilt.android.AndroidEntryPoint

/*
If a user denies the permission request, the user is navigated to this fragment
 */
@AndroidEntryPoint
class PermissionFragment : BaseFragment<PermissionViewModel, FragmentPermissionBinding>() {

    private val viewModel: PermissionViewModel by viewModels()

    override fun getFragmentViewModel(): PermissionViewModel = viewModel

    private val recordAudioPermission = registerPermission {
        onRecordAudioPermission(it)
    }

    override fun getFragmentViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPermissionBinding {
        return FragmentPermissionBinding.inflate(inflater, container, false)
    }

    override fun setupListeners() {
        super.setupListeners()

        fragmentViewBinding.buttonAskForPermission.setOnClickListener {
            recordAudioPermission.launchSinglePermission(Manifest.permission.RECORD_AUDIO)
        }

    }

    //Check permission for mic
    private fun onRecordAudioPermission(state: PermissionUtil.PermissionState) {
        when (state) {
            PermissionUtil.PermissionState.Denied -> {
                requireContext().openAppSystemSettings()
            }
            PermissionUtil.PermissionState.Granted -> {
                findNavController().navigate(R.id.action_permissionFragment_to_startFragment)
            }
            PermissionUtil.PermissionState.PermanentlyDenied -> {
                requireContext().openAppSystemSettings()
            }
        }
    }

}