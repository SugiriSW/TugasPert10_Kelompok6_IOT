package com.vvadu.iot_project


import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.blogspot.atifsoftwares.animatoolib.R.anim as Anim
import com.vvadu.iot_project.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var toolName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        enableEdgeToEdge()
        toolName = "Control"
        replaceFragment(ControlFragment(), R.anim.slide_left, R.anim.slide_right, toolName)

        binding.menuNav.setOnItemSelectedListener {
            val currentFragment = getCurrentFragment()

            when (it.itemId) {
                R.id.nav_control -> {
                    toolName = "Control"
                    if (currentFragment is ChartFragment2 || currentFragment is NotificationFragment) {
                        replaceFragment(ControlFragment(), Anim.animate_swipe_left_enter, Anim.abc_fade_out, toolName)
                    }
                }
                R.id.nav_notif -> {
                    toolName = "Notification"
                    if (currentFragment is ChartFragment2 || currentFragment is ControlFragment) {
                        replaceFragment(
                            NotificationFragment(),
                            Anim.animate_swipe_right_enter,
                            Anim.abc_fade_out,
                            toolName
                        )
                    }
                }
                R.id.nav_chart -> {
                    toolName = "Chart"
                    if (currentFragment is ControlFragment) {
                        replaceFragment(ChartFragment2(), Anim.animate_swipe_right_enter, Anim.abc_fade_out, toolName)
                    }else if(currentFragment is NotificationFragment){
                        replaceFragment(ChartFragment2(), Anim.animate_swipe_left_enter, Anim.abc_fade_out, toolName)
                    }
                }
                else -> { }
            }
            true
        }

    }
    private fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.frame_layout)
    }

    // Fungsi untuk mengganti fragment dengan animasi
    private fun replaceFragment(fragment: Fragment, enterAnim: Int, exitAnim: Int, toolName:String) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        // Atur animasi berdasarkan parameter
        fragmentTransaction.setCustomAnimations(enterAnim, exitAnim)

        // Ganti fragment
        supportActionBar?.title = toolName
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}
