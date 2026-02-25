package com.johnsamte.pathianlaisiangthou

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.johnsamte.pathianlaisiangthou.databinding.FragmentColorPickerBinding

class ColorPickerFragment : Fragment() {

    private var _binding: FragmentColorPickerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentColorPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Prevent memory leaks by clearing the binding reference
        _binding = null
    }
}
