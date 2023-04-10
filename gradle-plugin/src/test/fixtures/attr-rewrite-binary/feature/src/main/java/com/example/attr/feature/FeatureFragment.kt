package com.example.attr.feature

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.attr.feature.databinding.FragmentFeatureBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FeatureFragment : Fragment() {

  private var _binding: FragmentFeatureBinding? = null

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {

    _binding = FragmentFeatureBinding.inflate(inflater, container, false)
    return binding.root

  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.buttonFirst.setOnClickListener {
      requireActivity().finish()
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}