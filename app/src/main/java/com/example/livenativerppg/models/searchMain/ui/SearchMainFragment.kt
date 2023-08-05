package com.example.livenativerppg.models.searchMain.ui

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.transition.TransitionInflater
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.livenativerppg.R
import com.example.livenativerppg.component.base.BaseFragment
import com.example.livenativerppg.databinding.FragmentSearchMainBinding
import com.example.livenativerppg.models.searchMain.data.viewmodel.SearchVM
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope

private const val TAG = "SearchMainFragment"

@AndroidEntryPoint
class SearchMainFragment : BaseFragment<FragmentSearchMainBinding>(R.layout.fragment_search_main) {
    lateinit var controller: NavController
    lateinit var voiceRecognition: ActivityResultLauncher<Intent>
    lateinit var searchViewPagerAdpater: SearchViewPagerAdapter
    lateinit var searchVM: SearchVM


    override fun onInitialized() {
        super.onInitialized()
        controller = Navigation.findNavController(requireView())
        searchVM= ViewModelProvider(requireActivity())[SearchVM::class.java]
        searchViewPagerAdpater = SearchViewPagerAdapter(this, listOf(SearchUserFragment()))
        binding.searchVM = searchVM
        searchVM.isSearching.postValue(false)
        binding.TabLayout.isVisible = false


        val transaction =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = transaction
        binding.SearchViewPager.adapter  = searchViewPagerAdpater

        binding.searchInputLayout.post {
            binding.searchInputLayout.requestFocus()
            val imgr: InputMethodManager =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imgr.toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            );
        }



        TabLayoutMediator(binding.TabLayout, binding.SearchViewPager) { tab, position ->
            tab.text = if (position == 0) "Users" else "EMR"
        }.attach()

    }

    override fun addObservers() {
        super.addObservers()

        voiceRecognition =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    val matches: ArrayList<String> =
                        it.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)!!
                    binding.searchInputLayout.editText?.setText(matches[0])
                }
            }
        binding.searchInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.txtCancel.isVisible = count != 0
//                Log.d(TAG, "onTextChanged: ${s.toString()}")
                searchVM.isSearching.postValue(s?.length != 0)
                searchVM.searchUserData.postValue(s.toString())
//                when (pos) {
//                    0 -> {
//                (searchViewPagerAdpater.fragments[binding.SearchViewPager.currentItem] as SearchInterface).searchListener(s.toString())
//                    }
//                    1 -> {
//
//                    }
//                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
    }

    override fun setUpClicks() {
        binding.searchInputLayout.setEndIconOnClickListener {

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Speech recognition demo"
            )
            voiceRecognition.launch(intent)
        }
        binding.searchInputLayout.setStartIconOnClickListener {

        }
        binding.frameArrow.setOnClickListener {
            controller.popBackStack()
        }
        binding.txtCancel.setOnClickListener {
            binding.searchInputLayout.editText?.setText("")
        }
    }

    inner class SearchViewPagerAdapter(val fragment: Fragment, val fragments: List<Fragment>) :
        FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int {
            return fragments.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }

    }
}