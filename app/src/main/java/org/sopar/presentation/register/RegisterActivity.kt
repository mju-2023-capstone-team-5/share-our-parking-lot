package org.sopar.presentation.register

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.sopar.R
import org.sopar.databinding.ActivityRegisterBinding

@AndroidEntryPoint
class RegisterActivity :AppCompatActivity() {

    private val binding: ActivityRegisterBinding by lazy {
        ActivityRegisterBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setToolBar()
        setViewPager()
    }

    private fun setViewPager() {

        val pagerAdapter = RegisterRecyclerViewAdapter(this)
        pagerAdapter.addFragment(RegisterFragment1())
        pagerAdapter.addFragment(RegisterFragment2())
        pagerAdapter.addFragment(RegisterFragment3())
        pagerAdapter.addFragment(RegisterFragment4())
        pagerAdapter.addFragment(RegisterFragment5())
        pagerAdapter.addFragment(RegisterFragment6())
        pagerAdapter.addFragment(RegisterParkingLotImageFragment())
        pagerAdapter.addFragment(RegisterPermissionImageFragment())

        binding.registerViewPager.apply {
            adapter = pagerAdapter
            isUserInputEnabled = false
        }

    }

    private fun setToolBar() {
        setSupportActionBar(binding.noticeToolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }
}