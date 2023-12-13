package uz.androdev.milliytanlov.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * Created by: androdev
 * Date: 08-12-2023
 * Time: 3:40 PM
 * Email: Khudoyshukur.Juraev.001@mail.ru
 */

typealias Inflate<VB> = (LayoutInflater, ViewGroup?, Boolean) -> VB

abstract class BaseFragment<VB : ViewBinding>(private val inflate: Inflate<VB>) : Fragment() {
    private var _binding: VB? = null
    protected val binding get() = _binding!!

    private val onDestroyRunnableSet = mutableSetOf<Runnable>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = inflate.invoke(inflater, container, false)
        return binding.root
    }

    fun addOnDestroyAction(runnable: Runnable) {
        onDestroyRunnableSet.add(runnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onDestroyRunnableSet.forEach { it.run() }
        _binding = null
    }
}