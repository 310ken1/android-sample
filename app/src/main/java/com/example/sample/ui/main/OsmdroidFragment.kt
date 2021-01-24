package com.example.sample.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sample.R
import com.example.sample.databinding.OsmdroidFragmentBinding
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class OsmdroidFragment : Fragment() {

    companion object {
        private const val TAG = "OsmdroidFragment"
        fun newInstance() = OsmdroidFragment()
    }

    private var _binding: OsmdroidFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: OsmdroidViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = OsmdroidFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(OsmdroidViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        binding.map.setMultiTouchControls(true)
        binding.map.controller.setZoom(18.0)

        var location = MyLocationNewOverlay(
                GpsMyLocationProvider(context),binding.map)
        location.enableFollowLocation()
        location.enableMyLocation()
        binding.map.overlays.add(location)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }
}