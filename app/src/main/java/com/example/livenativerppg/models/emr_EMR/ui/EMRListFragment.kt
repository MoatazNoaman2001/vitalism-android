package com.example.livenativerppg.models.emr_EMR.ui

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.transition.TransitionInflater
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
//import com.chaquo.python.Python
//import com.chaquo.python.android.AndroidPlatform
import com.example.livenativerppg.R
import com.example.livenativerppg.component.base.BaseFragment
import com.example.livenativerppg.databinding.FragmentEMRListBinding
import com.example.livenativerppg.models.emr_EMR.data.viewModel.EMRViewModel
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint


private const val TAG = "EMRListFragment"


@AndroidEntryPoint
class EMRListFragment : BaseFragment<FragmentEMRListBinding>(R.layout.fragment_e_m_r_list) {
    private var controller: NavController? = null
    private var user: FirebaseUser? = null
    private var adapter: DateStringEMRRecycleAdapter? = null
    private val viewModel: EMRViewModel by viewModels()


    @Composable
    fun DisplayStatics(bitmap: Bitmap) {
        Column(
            modifier = Modifier.border(
                width = 2.dp,
                Color.Blue,
                shape = RoundedCornerShape(8.dp)
            )
        ) {
            Image(bitmap = bitmap.asImageBitmap(), contentDescription = null)
            Icon(painter = painterResource(R.drawable.ic_round_check_24), contentDescription = null)
        }
    }

    override fun onInitialized() {
        super.onInitialized()
        controller = findNavController(requireView())
//
//        if (!Python.isStarted()) {
//            Python.start(AndroidPlatform(requireActivity()))
//        }
//        val py = Python.getInstance()
//        val module = py.getModule("plot")
//
//        CoroutineScope(lifecycleScope.coroutineContext).launch {
//            val thread = Thread(object : Runnable {
//                override fun run() {
//                    val bytes = module.callAttr("plot" , listOf(1,2,3,4).toIntArray() , listOf(45,87,65,74).toIntArray())
//                        .toJava(ByteArray::class.java)
//                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
//
//                    binding.simpleImage.setContent {
//                        MaterialTheme {
//                            Surface {
//                                DisplayStatics(bitmap)
//                            }
//                        }
//                    }
//                }
//            }, "thread plot")
//            thread.start()
//        }

        val transition =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = transition

        binding.viewPager.adapter = EMRViewPagerAdapter(this,
            ArrayList<Fragment>().apply {
                add(ChooseVitalSignToShowFragment())
//                add(EMRFirstScreenFragment.getInstance("hr"))
            })

        binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
            }
        })
    }

    override fun addObservers() {
        super.addObservers()
    }

    override fun setUpClicks() {
        binding.toolbar.setNavigationOnClickListener { controller!!.popBackStack() }
    }
}

class EMRViewPagerAdapter(fragment: Fragment, val fragmentList: ArrayList<Fragment>) :
    FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return fragmentList.size;
    }

    override fun createFragment(position: Int): Fragment {
        Log.d(TAG, "createFragment: ${fragmentList[position].toString()}")
        Log.d(TAG, "createFragment: ${fragmentList.map { it.toString() }.joinToString { it }}")
        return fragmentList[position];
    }

    fun isContain(fragment: Fragment)= fragmentList.map { it.javaClass::getName }.any { fragment.javaClass::getName == it }
    fun removeFragment(fragment: Fragment) = fragmentList.map { it.javaClass::getName }.toList().dropWhile { fragment.javaClass::getName == it }

    fun addFragment(fragment: Fragment) {
        fragmentList.apply {
            add(fragment)
        }
        notifyItemInserted(0)
    }
}
