package com.mrntlu.projectconsumer.ui.anime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.mrntlu.projectconsumer.databinding.FragmentAnimeDetailsBinding
import com.mrntlu.projectconsumer.interfaces.BottomSheetOperation
import com.mrntlu.projectconsumer.interfaces.OnBottomSheetClosed
import com.mrntlu.projectconsumer.interfaces.UserListModel
import com.mrntlu.projectconsumer.interfaces.toAnimeWatchList
import com.mrntlu.projectconsumer.models.main.anime.AnimeDetails
import com.mrntlu.projectconsumer.ui.BaseDetailsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnimeDetailsFragment : BaseDetailsFragment<FragmentAnimeDetailsBinding>() {

    companion object {
        const val TYPE = "anime"
    }

    private val args: AnimeDetailsFragmentArgs by navArgs()

    private var animeDetails: AnimeDetails? = null

    private val onBottomSheetClosedCallback = object: OnBottomSheetClosed {
        override fun onSuccess(data: UserListModel?, operation: BottomSheetOperation) {
            animeDetails?.watchList = if (operation == BottomSheetOperation.DELETE)
                null
            else
                data?.toAnimeWatchList()

            if (operation != BottomSheetOperation.UPDATE)
                handleWatchListLottie(
                    binding.detailsInclude,
                    animeDetails?.watchList == null
                )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnimeDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setObservers()
    }

    private fun setObservers() {

    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            detailsConsumeLaterViewModel.consumeLater.removeObservers(this)
        }

        super.onDestroyView()
    }
}