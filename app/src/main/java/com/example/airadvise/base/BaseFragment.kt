package com.example.airadvise.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * Base fragment class that handles ViewBinding lifecycle automatically.
 * Fragments extending this class should implement createBinding() to provide their ViewBinding.
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    
    private var _binding: VB? = null
    protected val binding get() = _binding!!
    
    /**
     * Abstract method to be implemented by child fragments to create their ViewBinding.
     * @param inflater The LayoutInflater to use for inflating the binding
     * @param container The parent ViewGroup
     * @return The ViewBinding instance
     */
    protected abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?): VB
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = createBinding(inflater, container)
        return binding.root
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
