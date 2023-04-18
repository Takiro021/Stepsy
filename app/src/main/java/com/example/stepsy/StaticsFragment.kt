package com.example.stepsy

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.stepsy.databinding.FragmentStaticsBinding

class StaticsFragment : Fragment() {

    private lateinit var binding: FragmentStaticsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using view binding
        binding = FragmentStaticsBinding.inflate(inflater, container, false)
        loadProgressFromSharedPreferences()
        return binding.root
    }

    private fun loadProgressFromSharedPreferences() {
        val fileMonday = "stepsyDataMONDAY"
        val fileTuesday = "stepsyDataTUEDAY"
        val fileWednesday = "stepsyDataWEDNESDAY"
        val fileThursday = "stepsyDataTHURDAY"
        val fileFriday = "stepsyDataFRIDAY"
        val fileSaturday = "stepsyDataSATURDAY"
        val fileSunday = "stepsyDataSUNDAY"

        val fileNames = arrayOf(
            fileMonday,
            fileTuesday,
            fileWednesday,
            fileThursday,
            fileFriday,
            fileSaturday,
            fileSunday,
        )

        fileNames.forEach { day ->
            val sharedPreferences = requireContext().getSharedPreferences(day,
                Context.MODE_PRIVATE
            )
            val savedSteps = sharedPreferences.getFloat("stepsToday", 0f).toInt().toString()

            when(day) {
                fileMonday -> binding.statsMonday.text = savedSteps
                fileTuesday -> binding.statTuesday.text = savedSteps
                fileWednesday -> binding.statsWednesday.text = savedSteps
                fileThursday -> binding.statsThursday.text = savedSteps
                fileFriday -> binding.statsFriday.text = savedSteps
                fileSaturday -> binding.statsSaturday.text = savedSteps
                fileSunday -> binding.statsSunday.text = savedSteps
            }
        }
    }
}