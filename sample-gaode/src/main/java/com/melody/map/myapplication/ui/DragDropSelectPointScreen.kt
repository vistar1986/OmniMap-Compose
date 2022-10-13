package com.melody.map.myapplication.ui

import android.Manifest
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.melody.map.gd_compose.GDMap
import com.melody.map.gd_compose.poperties.MapUiSettings
import com.melody.map.gd_compose.overlay.Marker
import com.melody.map.gd_compose.overlay.rememberMarkerState
import com.melody.map.gd_compose.position.rememberCameraPositionState
import com.melody.map.myapplication.R
import com.melody.map.myapplication.contract.DragDropSelectPointContract
import com.melody.map.myapplication.dialog.ShowOpenGPSDialog
import com.melody.map.myapplication.viewmodel.DragDropSelectPointViewModel
import com.melody.sample.common.launcher.handlerGPSLauncher
import com.melody.sample.common.utils.requestMultiplePermission
import com.melody.sample.common.utils.showToast
import com.melody.ui.components.ForceStartLocationButton
import com.melody.ui.components.UIMarkerInScreenCenter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

/**
 * DragDropSelectPointContract
 * @author TheMelody
 * email developer_melody@163.com
 * created 2022/10/09 17:36
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun DragDropSelectPointScreen() {
    var isMapLoaded by rememberSaveable{ mutableStateOf(false) }
    val dragDropAnimatable = remember { Animatable(Size.Zero,Size.VectorConverter) }
    val cameraPositionState = rememberCameraPositionState()
    val locationState = rememberMarkerState()
    val viewModel: DragDropSelectPointViewModel = viewModel()
    val currentState by viewModel.uiState.collectAsState()

    val openGpsLauncher = handlerGPSLauncher(viewModel::checkGpsStatus)

    val reqGPSPermission = requestMultiplePermission(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ),
        onNoGrantPermission = viewModel::handleNoGrantLocationPermission,
        onGrantAllPermission = viewModel::checkGpsStatus
    )

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.onEach {
            if(it is DragDropSelectPointContract.Effect.Toast) {
                showToast(it.msg)
            }
        }.collect()
    }

    LaunchedEffect(Unit) {
        snapshotFlow { reqGPSPermission.allPermissionsGranted }.collect {
            viewModel.checkGpsStatus()
        }
    }

    LaunchedEffect(currentState.isOpenGps, reqGPSPermission.allPermissionsGranted) {
        if(currentState.isOpenGps == true) {
            if (!reqGPSPermission.allPermissionsGranted) {
                reqGPSPermission.launchMultiplePermissionRequest()
            } else {
                viewModel.startMapLocation()
            }
        }
    }

    // 地图移动，中心的Marker需要动画跳动
    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving) {
            dragDropAnimatable.animateTo(Size(45F,20F))
        } else {
            dragDropAnimatable.animateTo(Size(25F,11F))
            // 查询附近1000米地址数据
            viewModel.doSearchQueryPoi(cameraPositionState.position.target)
        }
    }

    LaunchedEffect(currentState.isClickForceStartLocation, currentState.currentLocation) {
        val curLocation = currentState.currentLocation
        if(null == curLocation || cameraPositionState.position.target == curLocation) return@LaunchedEffect
        locationState.position = curLocation
        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(currentState.currentLocation, 17F))
    }

    if(currentState.isShowOpenGPSDialog) {
        ShowOpenGPSDialog(
            onDismiss = viewModel::hideOpenGPSDialog,
            onPositiveClick = {
                viewModel.openGPSPermission(openGpsLauncher)
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5F)) {
            GDMap(
                modifier = Modifier.matchParentSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    showMapLogo = false,
                    isZoomGesturesEnabled = true,
                    isScrollGesturesEnabled = true,
                ),
                onMapLoaded = {
                    isMapLoaded = true
                }
            ){
                Marker(
                    state = locationState,
                    anchor = Offset(0.5F,0.5F),
                    rotation = currentState.currentRotation,
                    icon = BitmapDescriptorFactory.fromResource(com.melody.ui.components.R.drawable.ic_map_location_self),
                    onClick = { true }
                )
            }
            if(isMapLoaded) {
                // 地图加载出来之后，再显示出来选点的图标
                UIMarkerInScreenCenter(R.drawable.purple_pin) {
                    dragDropAnimatable.value
                }
            }
            // 强制触发单次定位的按钮
            ForceStartLocationButton(viewModel::startMapLocation)
        }

        //选点获取到的结果
        DragDropPoiResultList(
            poiItemList = currentState.poiItems,
            onItemClick = viewModel::showSelectAddressInfo
        )
    }
}