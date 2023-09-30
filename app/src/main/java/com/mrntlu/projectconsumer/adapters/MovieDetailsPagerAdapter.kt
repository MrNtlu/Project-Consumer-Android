package com.mrntlu.projectconsumer.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mrntlu.projectconsumer.models.main.movie.MovieDetails
import com.mrntlu.projectconsumer.ui.common.DetailsCastFragment
import com.mrntlu.projectconsumer.ui.movie.MovieDetailsAboutFragment
import com.mrntlu.projectconsumer.ui.movie.MovieDetailsStreamingFragment

class MovieDetailsFragmentFactory(
    private val movieDetails: MovieDetails,
): FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            MovieDetailsAboutFragment::class.java.name -> MovieDetailsAboutFragment(movieDetails)
            DetailsCastFragment::class.java.name -> DetailsCastFragment(movieDetails)
            MovieDetailsStreamingFragment::class.java.name -> MovieDetailsStreamingFragment(movieDetails)
            else -> super.instantiate(classLoader, className)
        }
    }
}

class MovieDetailsPagerAdapter(
    fm: FragmentManager,
    lifecycle: Lifecycle,
    private val fragmentFactory: FragmentFactory,
): FragmentStateAdapter(fm, lifecycle) {

    private val fragmentList = listOf(
        MovieDetailsAboutFragment::class.java.name,
        DetailsCastFragment::class.java.name,
        MovieDetailsStreamingFragment::class.java.name,
    )

    override fun getItemCount() = fragmentList.size

    override fun createFragment(position: Int): Fragment {
        return fragmentFactory.instantiate(
            ClassLoader.getSystemClassLoader(),
            fragmentList[position]
        )
    }
}