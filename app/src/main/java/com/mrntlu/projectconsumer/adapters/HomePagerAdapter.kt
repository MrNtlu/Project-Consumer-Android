package com.mrntlu.projectconsumer.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mrntlu.projectconsumer.ui.anime.AnimeFragment
import com.mrntlu.projectconsumer.ui.game.GameFragment
import com.mrntlu.projectconsumer.ui.movie.MovieFragment
import com.mrntlu.projectconsumer.ui.tv.TVSeriesFragment

class HomeFragmentFactory : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            MovieFragment::class.java.name -> MovieFragment()
            TVSeriesFragment::class.java.name -> TVSeriesFragment()
            AnimeFragment::class.java.name -> AnimeFragment()
            GameFragment::class.java.name -> GameFragment()
            else -> super.instantiate(classLoader, className)
        }
    }
}

class HomePagerAdapter(
    fm: FragmentManager,
    lifecycle: Lifecycle,
    private val fragmentFactory: FragmentFactory,
): FragmentStateAdapter(fm, lifecycle) {

    private val fragmentList = listOf(
        MovieFragment::class.java.name,
        TVSeriesFragment::class.java.name,
        AnimeFragment::class.java.name,
        GameFragment::class.java.name,
    )

    override fun getItemCount() = fragmentList.size

    override fun createFragment(position: Int): Fragment {
        return fragmentFactory.instantiate(
            ClassLoader.getSystemClassLoader(),
            fragmentList[position]
        )
    }
}