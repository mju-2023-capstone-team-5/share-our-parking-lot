package org.sopar.presentation.map

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import net.daum.mf.map.api.MapView.MapViewEventListener
import org.sopar.R
import org.sopar.data.remote.response.ParkingLot
import org.sopar.data.remote.response.Place
import org.sopar.databinding.FragmentMapBinding
import org.sopar.domain.entity.NetworkState
import org.sopar.presentation.base.BaseErrorDialog
import org.sopar.presentation.base.BaseFragment
import org.sopar.presentation.main.MainVIewModel
import kotlin.math.log

@AndroidEntryPoint
class MapFragment: BaseFragment<FragmentMapBinding>(R.layout.fragment_map) {
    private val args: MapFragmentArgs by navArgs()
    private val parkingLotAdapter: ParkingLotAdapter = ParkingLotAdapter()
    private var isHourly: Boolean? = null
    private val mapViewModel by viewModels<MapViewModel>()
    private var mapView: MapView? = null
    private lateinit var customMapViewEventListener: MapViewEventListener
    private val mainViewModel by activityViewModels<MainVIewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isAllPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }

        isHourly = requireActivity().intent.extras?.getBoolean("isHourly")

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val customBalloonAdapter = CustomBalloonAdapter(layoutInflater)
        customBalloonAdapter.setOnItemClickListener{
            val action = MapFragmentDirections.actionFragmentMapToReservationFragment2(null, isHourly!!, it)
            findNavController().navigate(action)
        }
        val poiItemEventListener = MarkerEventClickListener()

        poiItemEventListener.setOnItemClickListener {
            val action = MapFragmentDirections.actionFragmentMapToReservationFragment2(null, isHourly!!, it)
            findNavController().navigate(action)
        }

        customMapViewEventListener = object: MapViewEventListener {
            override fun onMapViewInitialized(p0: MapView?) {
                val place = args.place
                if (place == null) {
                    mapView!!.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
                } else {
                    //검색 결과가 있을 경우, 해당 위치로 지도 셋팅
                    mapView!!.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff
                    setSearchResult(place)
                }
            }

            override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {}

            override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {}

            override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {}

            override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {}

            override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {}

            override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {}

            override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {}

            override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {
                Log.e("current moved update", p1.toString())
                mapViewModel.updateMapCenter(p1)
            }
        }

        mapView = MapView(requireActivity())
        mapView?.apply {
            setMapViewEventListener(customMapViewEventListener)
            setCalloutBalloonAdapter(customBalloonAdapter)
            setPOIItemEventListener(poiItemEventListener)
        }
        binding.mapView.addView(mapView)
        setupRecyclerView()
        setObserve()
        setSearchFocusListener()
    }



    private fun setObserve() {
        mapViewModel.getParkingLotState.observe(viewLifecycleOwner) { state ->
            if (state == NetworkState.FAIL) {
                Log.d("mapFragment", "getParkingLotState Error")
                val dialog = BaseErrorDialog(R.string.base_error)
                dialog.show(requireActivity().supportFragmentManager, "BaseErrorDialog")
            }
        }

        mapViewModel.parkingLots.observe(viewLifecycleOwner) { parkingLots ->
            if (parkingLots.isNotEmpty()) {
                val temp = getCategoryList(parkingLots)
                parkingLotAdapter.submitList(temp.toMutableList())

                for (item in temp) {
                    setCustomPicker(item)
                }

                binding.textNoticeParkingLot.visibility = View.GONE
                binding.listParkingLot.visibility = View.VISIBLE
            } else {
                binding.textNoticeParkingLot.visibility = View.VISIBLE
                binding.listParkingLot.visibility = View.GONE
            }
        }

        mapViewModel.mapCenter.observe(viewLifecycleOwner) { center ->
            if (center?.mapPointGeoCoord?.latitude != -1.0E7) {
                Log.d("center check", center.mapPointGeoCoord.latitude.toString())
                mapView!!.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff
                getBound()
            }
        }
    }

    private fun getCategoryList(list: List<ParkingLot>): List<ParkingLot> {
        val temp = ArrayList<ParkingLot>()
        for (item in list) {
            if (isHourly!!) {
                if (item.hourly != null) {
                    temp.add(item)
                }
            } else {
                if (item.monthly != null) {
                    temp.add(item)
                }
            }
        }
        return temp
    }

    //화면에 표시할 위치 범위 구하기
    private fun getBound() {
        val x1 = mapView!!.mapPointBounds.bottomLeft.mapPointGeoCoord.latitude
        val x2 = mapView!!.mapPointBounds.topRight.mapPointGeoCoord.latitude
        val y1 =  mapView!!.mapPointBounds.bottomLeft.mapPointGeoCoord.longitude
        val y2 = mapView!!.mapPointBounds.topRight.mapPointGeoCoord.longitude
        mapViewModel.getParkingLots(y1, y2, x1, x2)
    }

    private fun setupRecyclerView() {
        parkingLotAdapter.setOnItemClickListener { parkingLot ->
            val action = MapFragmentDirections.actionFragmentMapToReservationFragment2(parkingLot, isHourly!!)
            findNavController().navigate(action)
        }

        binding.listParkingLot.apply {
            setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = parkingLotAdapter
        }

    }

    private fun setSearchResult(place: Place) {
        binding.textMapSearch.setText(place.place_name)
        setPicker(place.place_name, place.x.toDouble(), place.y.toDouble())

        mapView!!.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(place.y.toDouble(), place.x.toDouble()), true)
    }

    private fun setCustomPicker(parkingLot: ParkingLot) {
        val marker = MapPOIItem()
        val point = MapPoint.mapPointWithGeoCoord(parkingLot.longitude, parkingLot.latitude)
        marker.apply {
            itemName = parkingLot.name
            tag = parkingLot.id!!
            mapPoint = point
            markerType = MapPOIItem.MarkerType.CustomImage
            //추후 평점 직접 등록
            customImageResourceId = getImageByScore(parkingLot.ratingAvg!!.toFloat())
            setCustomImageAnchor(0.5f, 1.0f)
            showAnimationType = MapPOIItem.ShowAnimationType.SpringFromGround
            isDraggable = false
        }

        mapView!!.addPOIItem(marker)
    }

    private fun getImageByScore(score: Float): Int {
        return if (score >= 4.0F) {
            R.drawable.icon_picker_great
        } else if (score >= 2.5F) {
            R.drawable.icon_picker_good
        } else if (score == 0.0F) {
            R.drawable.icon_picker_new
        } else {
            return R.drawable.icon_picker_bad
        }
    }

    private fun setPicker(name: String, x: Double, y:Double) {
        val marker = MapPOIItem()
        val point = MapPoint.mapPointWithGeoCoord(y, x)
        marker.apply {
            itemName = name
            tag = 0
            mapPoint = point
            markerType = MapPOIItem.MarkerType.BluePin
            selectedMarkerType = MapPOIItem.MarkerType.RedPin
        }

        mapView!!.addPOIItem(marker)
    }


    private fun setSearchFocusListener() {
        binding.textMapSearch.setOnFocusChangeListener { _, hasFocus ->
            if(hasFocus) {
                findNavController().navigate(R.id.action_fragment_map_to_searchFragment)
            }
        }
    }

    private fun isAllPermissionsGranted(): Boolean = REQUIRED_PERMISSIONS.all { permission ->
        ContextCompat.checkSelfPermission(requireContext(), permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach { permission ->
                when {
                    //권한을 승인하였을 떄
                    permission.value -> {
                        Toast.makeText(requireContext(), "필요한 권한이 승인되었습니다!", Toast.LENGTH_SHORT).show()
                    }
                    //권한을 완전히 거부했을 경우
                    shouldShowRequestPermissionRationale(permission.key) -> {
                        Toast.makeText(requireContext(), "설정에서 권한을 승인해주세요!", Toast.LENGTH_SHORT).show()
                    }
                    //권한을 승인하지 않은 경우
                    else -> {
                        Toast.makeText(requireContext(), "설정에서 권한을 승인해주세요!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMapBinding {
        return FragmentMapBinding.inflate(inflater, container, false)
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.getUserInfoById()
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.INTERNET
        )
    }

}