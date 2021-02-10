package com.example.sample.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.sample.R
import com.example.sample.databinding.BlankFragmentBinding

class BlankFragment : Fragment() {

    companion object {
        val TAG: String = BlankFragment::class.java.simpleName
    }

    private var _binding: BlankFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: BlankViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(OsmdroidFragment.TAG, "onCreate savedInstanceState=$savedInstanceState")
        viewModel = ViewModelProvider(this).get(BlankViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        _binding = _binding ?: BlankFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")

//        binding.button.setOnClickListener(
//            Navigation.createNavigateOnClickListener(R.id.osmdroidFragment, null)
//        )
        binding.button.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
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