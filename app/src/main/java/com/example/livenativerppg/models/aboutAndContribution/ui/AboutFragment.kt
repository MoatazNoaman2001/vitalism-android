package com.example.livenativerppg.models.aboutAndContribution.ui

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.viewpager2.widget.CompositePageTransformer
import com.bumptech.glide.RequestManager
import com.example.livenativerppg.R
import com.example.livenativerppg.component.base.BaseFragment
import com.example.livenativerppg.databinding.FragmentAboutBinding
import com.example.livenativerppg.databinding.TeamContribRecycleItemBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import kotlin.math.abs


@AndroidEntryPoint
class AboutFragment : BaseFragment<FragmentAboutBinding>(R.layout.fragment_about) {

    @Inject
    lateinit var imageLoader: RequestManager

    lateinit var controller: NavController
    lateinit var adapter: TeamViewPagerAdapter

    override fun onInitialized() {
        super.onInitialized()
        controller = Navigation.findNavController(requireView())

        CoroutineScope(lifecycleScope.coroutineContext).apply {
            adapter = TeamViewPagerAdapter(team = createTeam(), imageLoader = imageLoader)
            binding.TeamViewPager.adapter = adapter
            binding.TeamViewPager.clipToPadding = false
            binding.TeamViewPager.clipChildren = false
            binding.TeamViewPager.offscreenPageLimit = 5
            binding.TeamViewPager.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            val compositePageTransformer = CompositePageTransformer()
//            compositePageTransformer.addTransformer(MarginPageTransformer(20))
            compositePageTransformer.addTransformer { page, position ->
                page.scaleY = 0.85f + (1 - abs(position)) * 0.15f
            }
            binding.TeamViewPager.setPageTransformer(compositePageTransformer)
        }
    }

    private fun createTeam(): List<Team> {
        return listOf(
            Team(
                "Dr Ali",
                "",
                "Header of the project",
                "",
                img_uri = "https://firebasestorage.googleapis.com/v0/b/vitalism-solution.appspot.com/o/app%2FContrib%2FDr%20Ali%20.png?alt=media&token=3a399ed3-e0cb-4bd8-8f86-70d35f3152fd",
                "",
                ""
            ),
            Team(
                "Mahmoud",
                "mahmoudalyosify.email@gmail.com",
                "Vitalism Team Leader",
                "The Vitalism project extracts vital signs using low-cost portable cameras and advanced signal processing. It enables non-invasive monitoring of patients, improving diagnoses and treatment plans. This has the potential to revolutionize healthcare and improve lives globally. As the project leader, I'm dedicated to advancing this technology and making it accessible to everyone.",
                "https://firebasestorage.googleapis.com/v0/b/vitalism-solution.appspot.com/o/app%2FContrib%2FMahmoud1.png?alt=media&token=9a96761e-b549-4ce0-9d1f-b580b85de115",
                "https://m.facebook.com/MahmoudAlyosify",
                "https://m.facebook.com/MahmoudAlyosify"
            ),
            Team(
                "Mina",
                "menanashat4321@gmail.com",
                "Coordinator of the Vitalism project and a QT Desktop developer using Python",
                "Leading and coordinating the project team: As the Coordinator,  i am responsible for coordinating the project team. This may involve setting project goals, assigning tasks, and ensuring that team members are working together effectively, Developing QT desktop applications: As a QT Desktop developer,iam responsible for developing desktop applications using the QT framework and Python. responsible for designing and implementing user interfaces, writing code, and testing applications to ensure they meet project requirements, Ensuring quality control: responsible for ensuring that the desktop applications development meet quality standards. This may involve testing applications, debugging code, and implementing quality control procedures.",
                "https://firebasestorage.googleapis.com/v0/b/vitalism-solution.appspot.com/o/app%2FContrib%2FMina.png?alt=media&token=72cf661d-dc00-4fa8-9249-68f766b4b32f",
                "https://www.facebook.com/menanashaat.mena",
                "https://www.linkedin.com/in/mina-nashat"
            ),
            Team(
                "moataz",
                "Moataz.noaman12@gmail.com",
                "Native android and algorithm developer in Vitalism",
                "vitalism is a massive service that will help in solve of a lot of issues and problem in health care just from home, it will not only make it easy to monitor patients health data from home but also help them to recognize how to keep there health well, iam native android developer attended team from the beginning and worked in enhancing neural network algorithms and building new way to measuring vital sign",
                img_uri = "https://firebasestorage.googleapis.com/v0/b/vitalism-solution.appspot.com/o/app%2Futilis%2Fcontributed%20team%2Fphoto_2023-04-05_16-05-09.jpg?alt=media&token=a2f1d01e-5f5d-4185-84b4-fd10458ac74e",
                "https://www.facebook.com/moataz.noaman.3?mibextid=ZbWKwL",
                "https://www.linkedin.com/in/moataz-noaman-02196222a"
            ),
            Team(
                "Nada",
                "nadaessamnadaelnagdy@gmail.com",
                "Deep learning development and algorithms of vitalism.",
                "Vitalism is a pioneering project aims to simplify the process of measuring a wide range of vital signs periodically by offering a fast, efficient, affordable, and user-friendly method suitable for people of all ages. As a member of the project team, my role is to develop deep learning methods and algorithms used in the project to enhance the accuracy and efficiency of vital sign extraction process.",
                "https://firebasestorage.googleapis.com/v0/b/vitalism-solution.appspot.com/o/app%2FContrib%2FNada.png?alt=media&token=4fd25024-c15d-47e0-9810-90645d445ee9",
                "https://www.facebook.com/nadaessamnada12345678?mibextid=LQQJ4d",
                "https://www.linkedin.com/in/nada-essam-7018211b7"
            ),
            Team(
                "Nourhan",
                "nour417924@gmail.com",
                "BackEnd , Database developer and Algorisms development for the project",
                "I started working on the project as a backend and developing of the database,  developed some algorithms for measuring vital signs. Vitalism is considered a very big progress in the medical sector because it provides many services that people need and solves many problems that some people face in measuring their vital signs for that Its presence is considered a very great benefit for all groups of society to follow up  their health condition.",
                "https://firebasestorage.googleapis.com/v0/b/vitalism-solution.appspot.com/o/app%2FContrib%2FNourhan.png?alt=media&token=e8a7dd85-2686-454a-ade6-053a5954a2d8",
                "https://www.facebook.com/profile.php?id=100038767134374&mibextid=ZbWKwL",
                "https://www.linkedin.com/in/nourhan-ahmed-54aba0218"
            ),
            Team(
                "Menna Allah Mahmoud Abdelrauf",
                "Monmonmahmoud2006@gmail.com",
                "Ui/UX and Front end developer.",
                "Vitalism is a cutting-edge project that utilizes video data analysis to extract vital signs from \n" +
                        "humans. The project utilizes low-cost, high-quality portable cameras and employs advanced \n" +
                        "image and signal processing techniques to extract features from videos that are invisible to \n" +
                        "the human eye. my role is Ui/UX and Front end developer",
                "https://firebasestorage.googleapis.com/v0/b/vitalism-solution.appspot.com/o/app%2FContrib%2Fmenn0a.png?alt=media&token=f80e2c0d-6d43-4005-9bfb-cfddba9bd4a7",
                "https://www.facebook.com/profile.php?id=100042233687092",
                "https://www.linkedin.com/in/menna-mahmoud-18810a204"
            )
        )
    }

    override fun setUpClicks() {

    }

    override fun addObservers() {
        super.addObservers()
    }

    inner class TeamViewPagerAdapter(val team: List<Team>, val imageLoader: RequestManager) :
        RecyclerView.Adapter<TeamViewPagerAdapter.ViewHolder>() {


        inner class ViewHolder(var binding: TeamContribRecycleItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): TeamViewPagerAdapter.ViewHolder {
            return ViewHolder(
                TeamContribRecycleItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: TeamViewPagerAdapter.ViewHolder, position: Int) {
//            holder.binding.contribName.text = team[position].name
//            holder.binding.contribDisc.text = team[position].disc
            imageLoader.asBitmap().load(team[position].img_uri).centerCrop().into(holder.binding.img)
            holder.binding.Name.text = team[position].name
            holder.binding.email.text = team[position].email
            holder.binding.brif.text = team[position].brif
            holder.binding.disc.text = team[position].disc

            holder.binding.fb.setOnClickListener {
                val uri: Uri = Uri.parse(team[position].fbLink) // missing 'http://' will cause crashed
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
            holder.binding.linkedin.setOnClickListener {
                if (team[position].linkedInLink.isEmpty()) return@setOnClickListener
                val uri: Uri = Uri.parse(team[position].linkedInLink) // missing 'http://' will cause crashed
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }

            holder.binding.img.setOnClickListener {

                if (!holder.binding.additionalLayout.isVisible) {
                    holder.binding.framelayout.layoutParams.width =
                        resources.getDimension(com.intuit.sdp.R.dimen._170sdp).toInt()
                    holder.binding.imglayout.layoutParams.width =
                        resources.getDimension(com.intuit.sdp.R.dimen._170sdp).toInt()
                    holder.binding.imglayout.layoutParams.height =
                        resources.getDimension(com.intuit.sdp.R.dimen._170sdp).toInt()
                }else{
                    holder.binding.framelayout.layoutParams.width =
                        resources.getDimension(com.intuit.sdp.R.dimen._110sdp).toInt()
                    holder.binding.imglayout.layoutParams.width =
                        resources.getDimension(com.intuit.sdp.R.dimen._110sdp).toInt()
                    holder.binding.imglayout.layoutParams.height =
                        resources.getDimension(com.intuit.sdp.R.dimen._110sdp).toInt()
                }
                TransitionManager.beginDelayedTransition(
                    holder.binding.root,
                    AutoTransition()
                )
                holder.binding.additionalLayout.isVisible = !holder.binding.additionalLayout.isVisible
            }
        }

        override fun onViewDetachedFromWindow(holder: ViewHolder) {
            super.onViewDetachedFromWindow(holder)
            holder.binding.additionalLayout.isVisible = false
        }

        override fun getItemCount(): Int {
            return team.size
        }

    }

    data class Team(
        var name: String, var email: String, var brif: String,
        var disc: String, var img_uri: String,
        var fbLink: String, var linkedInLink: String,
    ) {
        constructor() : this(name = "", disc = "", img_uri = "" , email = "" , brif="", fbLink="", linkedInLink="")
    }
}