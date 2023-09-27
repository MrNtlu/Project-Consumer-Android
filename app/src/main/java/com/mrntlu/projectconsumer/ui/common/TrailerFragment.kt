package com.mrntlu.projectconsumer.ui.common

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import androidx.navigation.fragment.navArgs
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.mrntlu.projectconsumer.databinding.FragmentTrailerBinding
import com.mrntlu.projectconsumer.ui.BaseFragment

class TrailerFragment : BaseFragment<FragmentTrailerBinding>() {

    private val args: TrailerFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrailerBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(binding.trailerWebview.settings, WebSettingsCompat.FORCE_DARK_ON)
        }

        binding.trailerToolbar.setNavigationOnClickListener { navController.popBackStack() }

        try {
            binding.trailerWebview.apply {
                val htmlData = """
                    <html>
                    
                        <body style="margin:0px;padding:0px;">
                           <div id="player"></div>
                            <script>
                                var player;
                                function onYouTubeIframeAPIReady() {
                                    player = new YT.Player('player', {
                                        height: '100%',
                                        width: '100%',
                                        videoId: '${args.youtubeID}',
                                        playerVars: {
                                            'playsinline': 1
                                        },
                                        events: {
                                            'onReady': onPlayerReady
                                        }
                                    });
                                }
                                function onPlayerReady(event) {
                                 player.playVideo();
                                }
                                function seekTo(time) {
                                    player.seekTo(time, true);
                                }
                            </script>
                            <script src="https://www.youtube.com/iframe_api"></script>
                        </body>
                    </html>
                """

                loadDataWithBaseURL(
                    "https://www.youtube.com",
                    htmlData,
                    "text/html",
                    "UTF-8",
                    null
                )
                settings.javaScriptEnabled = true
                webChromeClient = WebChromeClient()
            }
        } catch (_: Exception) {
            navController.popBackStack()
        }
    }

    override fun onDestroyView() {
        binding.trailerWebview.webChromeClient = null
        binding.trailerWebview.destroy()

        super.onDestroyView()
    }
}