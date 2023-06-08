package com.mrntlu.projectconsumer.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.navArgs
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.FragmentDiscoverListBinding
import com.mrntlu.projectconsumer.ui.BaseFragment

class DiscoverListFragment : BaseFragment<FragmentDiscoverListBinding>() {
    private companion object {
        private const val GENRE_KEY = "discover.genre"
        private const val STATUS_KEY = "discover.status"
        private const val SORT_KEY = "discover.sort"
        private const val FROM_KEY = "discover.from"
        private const val TO_KEY = "discover.to"
    }

    private val args: DiscoverListFragmentArgs by navArgs()

    private var genre: String? = null
    private var status: String? = null
    private var sort: String? = null
    private var from: Int? = null
    private var to: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (args.genre != null) {
            genre = args.genre
        }

        savedInstanceState?.let {
            genre = it.getString(GENRE_KEY, args.genre)
        }

        setMenu()
    }

    private fun setMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object: MenuProvider {
            override fun onPrepareMenu(menu: Menu) {}

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.sort_toolbar_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId) {
                    R.id.sortMenu -> {

                    }
                }
                return true
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.apply {
            putString(GENRE_KEY, genre)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }
}