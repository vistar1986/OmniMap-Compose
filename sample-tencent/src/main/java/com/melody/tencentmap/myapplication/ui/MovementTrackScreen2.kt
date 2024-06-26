// MIT License
//
// Copyright (c) 2023 被风吹过的夏天
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package com.melody.tencentmap.myapplication.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.melody.map.tencent_compose.TXMap
import com.melody.map.tencent_compose.overlay.MovingPointOverlay
import com.melody.map.tencent_compose.overlay.PolylineRainbow
import com.melody.map.tencent_compose.position.rememberCameraPositionState
import com.melody.tencentmap.myapplication.R
import com.melody.tencentmap.myapplication.viewmodel.MovementTrackViewModel
import com.melody.ui.components.RedCenterLoading
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.flow.filterNotNull

/**
 * MovementTrackScreen
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/02/16 16:33
 */
@Composable
internal fun MovementTrackScreen2() {
    val viewModel: MovementTrackViewModel = viewModel()
    val currentState by viewModel.uiState.collectAsState()
    val cameraPositionState = rememberCameraPositionState()
    LaunchedEffect(Unit) {
        snapshotFlow { currentState.trackLatLng }.filterNotNull().collect {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(it, 200))
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        TXMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = currentState.uiSettings,
            onMapLoaded = viewModel::loadMovementTrackData
        ){
            PolylineRainbow(
                points = currentState.latLngList,
                width = 15F,
                isLineCap = true,
                useGradient = true,
                rainbow = currentState.polylineRainbow,
                animation = currentState.polylineAnimation
            )
            if(currentState.latLngList.isNotEmpty()) {
                MovingPointOverlay(
                    points = currentState.latLngList,
                    icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher),
                    isStartSmoothMove = true,
                    totalDuration = currentState.polylineAnimDuration
                )
            }
        }
        if(currentState.isLoading) {
            RedCenterLoading()
        }
    }
}