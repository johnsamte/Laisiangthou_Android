package com.johnsamte.pathianlaisiangthou

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ImageShareFragAdapter (fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 4 // Define how many tabs/fragments you have

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ImagesFragment()        // Fragment for the Images tab
            1 -> FontsFragment()         // Fragment for the Fonts tab
            2 -> ColorPickerFragment()   // Fragment for the Color Picker tab
            3 -> TextCustomisationFragment()  // Fragment for the Text Customisation tab
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}