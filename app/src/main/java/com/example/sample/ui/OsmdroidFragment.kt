package com.example.sample.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sample.R
import com.example.sample.databinding.OsmdroidFragmentBinding
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class OsmdroidFragment : Fragment() {

    companion object {
        val TAG: String = OsmdroidFragment::class.java.simpleName
    }

    private val binding get() = _binding!!
    private var _binding: OsmdroidFragmentBinding? = null
    private lateinit var viewModel: OsmdroidViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate savedInstanceState=$savedInstanceState")
        viewModel = ViewModelProvider(this).get(OsmdroidViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        _binding = _binding ?: OsmdroidFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        binding.map.setMultiTouchControls(true)
        binding.map.controller.setZoom(18.0)

        binding.map.overlays.add(
            MyLocationNewOverlay(
                GpsMyLocationProvider(context),
                binding.map
            ).apply {
                enableFollowLocation()
                enableMyLocation()
            })

//        binding.button.setOnClickListener(
//            Navigation.createNavigateOnClickListener(R.id.blankFragment, null)
//        )

        binding.button.setOnClickListener {
            var next =
                parentFragmentManager.findFragmentByTag(BlankFragment.TAG) ?: BlankFragment().let {
                    parentFragmentManager.beginTransaction().apply {
                        add(R.id.nav_host_fragment, it, BlankFragment.TAG)
                        commit()
                    }
                    it
                }
            parentFragmentManager.beginTransaction().also {
                it.show(next)
                it.hide(this)
                it.addToBackStack(null)
                it.commit()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        binding.map.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach")
    }
}