package com.example.airadvise.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.airadvise.R
import com.example.airadvise.databinding.FragmentHomeBinding
import com.example.airadvise.utils.SessionManager

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get authenticated user data
        val userData = SessionManager.getUserData(requireContext())

        // Display user data
        userData?.let {
            binding.tvUsername.text = "Welcome, ${it.name}"
        }

        // TODO: Implement other UI components
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}