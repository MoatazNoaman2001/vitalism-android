package com.example.livenativerppg.models.pagerStartInstruction.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.livenativerppg.R
import com.example.livenativerppg.commons.ErrorLoginHandler
import com.example.livenativerppg.commons.startingAccountFirstTime
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.FragmentPagerStartInstructionBinding
import com.example.livenativerppg.models.mainAppPage.ui.MainAppPageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class PagerStartInstructionFragment : Fragment() {
    lateinit var binding: FragmentPagerStartInstructionBinding
    lateinit var controller: NavController
    lateinit var user:FirebaseUser

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    lateinit var adapter: ViewPagerAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPagerStartInstructionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = Navigation.findNavController(requireView())
        user = FirebaseAuth.getInstance().currentUser!!

        sharedPreferences.edit().putBoolean(Variables.Start_Pager_Visited, true).apply();

        adapter = ViewPagerAdapter()

        binding.viewPager.adapter = adapter

        binding.dotIndecator.attachTo(binding.viewPager)


        binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 3) {
                    binding.next.text = "Finish"
                    binding.next.setOnClickListener {
                        requireActivity().startActivity(Intent(requireActivity(),
                            MainAppPageActivity::class.java))
                        requireActivity().finishAffinity()
                    }
                } else {
                    binding.next.text = "Next"
                    binding.next.setOnClickListener {
                        binding.viewPager.currentItem = position + 1
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })

    }




}