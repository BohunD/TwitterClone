package com.example.twitterclone.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.twitterclone.R
import com.example.twitterclone.databinding.ActivityHomeBinding
import com.example.twitterclone.fragments.HomeFragment
import com.example.twitterclone.fragments.MyActivityFragment
import com.example.twitterclone.fragments.SearchFragment
import com.example.twitterclone.util.DATA_USERS
import com.example.twitterclone.util.DATA_USER_IMAGE_URL
import com.example.twitterclone.util.User
import com.example.twitterclone.util.loadUrl
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var binding: ActivityHomeBinding
    private val homeFragment = HomeFragment()
    private val searchFragment = SearchFragment()
    private val myActivityFragment = MyActivityFragment()
    private var sectionPageAdapter: SectionPageAdapter?=null
    private var userId = FirebaseAuth.getInstance().currentUser?.uid
    private val firebaseDB = FirebaseFirestore.getInstance()
    private var imageUrl : String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sectionPageAdapter = SectionPageAdapter(supportFragmentManager)
        with(binding) {
            container.adapter = sectionPageAdapter
            container.addOnPageChangeListener(
                TabLayout.TabLayoutOnPageChangeListener(
                    tabs
                )
            )
            initLogo()
            tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
            tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
                override fun onTabSelected(tab: TabLayout.Tab?) {

                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }

            })
            logo.setOnClickListener {
                startActivity(ProfileActivity.newIntent(this@HomeActivity))
            }
        }
    }

    private fun initLogo(){
        binding.homeProgressLayout.visibility = View.VISIBLE
        firebaseDB.collection(DATA_USERS).document(userId!!).get()
            .addOnSuccessListener { it ->
                val user = it.toObject(User::class.java)
                imageUrl = user?.imageUrl
                binding.logo.loadUrl(imageUrl, R.drawable.logo)
                binding.homeProgressLayout.visibility = View.GONE
            }
    }

    override fun onResume() {
        super.onResume()
        userId = FirebaseAuth.getInstance().currentUser?.uid
        if(userId==null){
            startActivity(LoginActivity.newIntent(this))
            finish()
        }
        initLogo()
    }

    fun onLogout(){
        firebaseAuth.signOut()
        startActivity(LoginActivity.newIntent(this))
    }

    inner class  SectionPageAdapter(fa: FragmentManager) : FragmentPagerAdapter(fa){
        override fun getCount(): Int {
            return 3
        }

        override fun getItem(position: Int): Fragment {
            return when(position){
                0 -> homeFragment
                1 -> searchFragment
                else -> myActivityFragment
            }
        }


    }

    companion object{
        fun newIntent(context: Context) = Intent(context, HomeActivity::class.java)
    }
}