package com.example.batterymonitorpro

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.math.abs

/**
 * OnboardingActivity introduces the app's features to new users.
 * It uses a ViewPager2 to navigate through different onboarding slides.
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: MaterialButton
    private lateinit var btnSkip: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Skip onboarding if it has already been completed by the user
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        if (sharedPref.getBoolean("onboarding_completed", false)) {
            navigateToMain()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_onboarding)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightNavigationBars = false
        controller.isAppearanceLightStatusBars = false

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottomContainer)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, systemBars.bottom)
            insets
        }

        initViews()
        setupViewPager()
    }

    /**
     * Initializes UI component references and sets up click listeners.
     */
    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btnNext)
        btnSkip = findViewById(R.id.btnSkip)

        btnNext.setOnClickListener {
            val totalItems = viewPager.adapter?.itemCount ?: 0
            if (viewPager.currentItem + 1 < totalItems) {
                // Move to the next page
                viewPager.currentItem += 1
            } else {
                // Last page reached, complete onboarding
                completeOnboarding()
            }
        }

        btnSkip.setOnClickListener {
            // Skip onboarding and navigate to Main without saving preference (it will show again next time)
            navigateToMain()
        }
    }

    /**
     * Prepares the ViewPager2 with its adapter, transformation effects, and indicators.
     */
    private fun setupViewPager() {
        // Data for onboarding slides
        val onboardingItems = listOf(
            OnboardingItem(
                getString(R.string.onboarding_title_1),
                getString(R.string.onboarding_desc_1),
                R.drawable.ic_onboarding_health
            ),
            OnboardingItem(
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_desc_2),
                R.drawable.ic_onboarding_temp
            ),
            OnboardingItem(
                getString(R.string.onboarding_title_3),
                getString(R.string.onboarding_desc_3),
                R.drawable.ic_onboarding_voltage
            )
        )

        // Set adapter and custom page transformer for smooth transitions
        viewPager.adapter = OnboardingAdapter(onboardingItems)
        viewPager.setPageTransformer { page, position ->
            page.apply {
                val absPos = abs(position)
                alpha = 1f - absPos
                scaleX = 0.85f + (1f - absPos) * 0.15f
                scaleY = 0.85f + (1f - absPos) * 0.15f
            }
        }

        // Connect TabLayout to ViewPager2 to show page indicator dots
        TabLayoutMediator(findViewById(R.id.tabLayout), viewPager) { _, _ -> }.attach()

        // Update UI state (buttons visibility and text) when the page changes
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == onboardingItems.size - 1) {
                    btnNext.setText(R.string.btn_get_started)
                    btnSkip.visibility = View.GONE
                } else {
                    btnNext.setText(R.string.btn_next)
                    btnSkip.visibility = View.VISIBLE
                }
            }
        })
    }

    /**
     * Marks onboarding as completed in SharedPreferences and navigates to the main activity.
     */
    private fun completeOnboarding() {
        getSharedPreferences("app_prefs", MODE_PRIVATE).edit {
            putBoolean("onboarding_completed", true)
        }
        navigateToMain()
    }

    /**
     * Navigates to MainActivity and closes this activity.
     */
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /**
     * Data model representing a single onboarding slide.
     */
    data class OnboardingItem(val title: String, val description: String, val imageRes: Int)

    /**
     * RecyclerView Adapter for onboarding ViewPager2.
     */
    class OnboardingAdapter(private val items: List<OnboardingItem>) :
        RecyclerView.Adapter<OnboardingAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivImage: ImageView = view.findViewById(R.id.ivOnboarding)
            val tvTitle: TextView = view.findViewById(R.id.tvTitle)
            val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_onboarding, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvTitle.text = item.title
            holder.tvDescription.text = item.description
            holder.ivImage.setImageResource(item.imageRes)
        }

        override fun getItemCount() = items.size
    }
}
