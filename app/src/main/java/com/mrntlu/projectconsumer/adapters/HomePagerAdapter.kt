package com.mrntlu.projectconsumer.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mrntlu.projectconsumer.ui.anime.AnimeFragment
import com.mrntlu.projectconsumer.ui.game.GameFragment
import com.mrntlu.projectconsumer.ui.movie.MovieFragment
import com.mrntlu.projectconsumer.ui.tv.TVSeriesFragment

class HomePagerAdapter(
    fm: FragmentManager,
    lifecycle: Lifecycle
): FragmentStateAdapter(fm, lifecycle) {

    private val fragmentList = listOf(
        MovieFragment(),
        TVSeriesFragment(),
        AnimeFragment(),
        GameFragment()
    )

    override fun getItemCount() = fragmentList.size

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}