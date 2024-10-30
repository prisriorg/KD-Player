package com.prisri.kdplayer.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.prisri.kdplayer.MainActivity
import com.prisri.kdplayer.R
import com.prisri.kdplayer.activity.PlayerActivity
import com.prisri.kdplayer.adapter.VideoAdapter
import com.prisri.kdplayer.databinding.FragmentVideosBinding
import com.prisri.kdplayer.helper.getAllVideos

class VideosFragment : Fragment() {

    lateinit var adapter: VideoAdapter
    private lateinit var binding: FragmentVideosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        requireContext().theme.applyStyle(MainActivity.themesList[MainActivity.themeIndex], true)
        val view = inflater.inflate(R.layout.fragment_videos, container, false)
        binding = FragmentVideosBinding.bind(view)
        binding.VideoRV.setHasFixedSize(true)
        binding.VideoRV.setItemViewCacheSize(10)
        binding.VideoRV.layoutManager = LinearLayoutManager(requireContext())
        adapter = VideoAdapter(requireContext(), MainActivity.videoList)
        binding.VideoRV.adapter = adapter
        binding.totalVideos.text = "Total Videos: ${MainActivity.videoList.size}"

        //for refreshing layout
        binding.root.setOnRefreshListener {
            MainActivity.videoList = getAllVideos(requireContext())
            adapter.updateList(MainActivity.videoList)
            binding.totalVideos.text = "Total Videos: ${MainActivity.videoList.size}"

            binding.root.isRefreshing = false
        }


        binding.nowPlayingBtn.setOnClickListener{
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtra("class", "NowPlaying")
            startActivity(intent)
        }
        return view
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_view, menu)
        val searchItem = menu.findItem(R.id.searchView)
        searchItem.setIcon(R.drawable.search)
        val searchIcon = searchItem.icon
        val iconColor = ContextCompat.getColor(requireContext(), R.color.white)
        searchIcon?.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN)

        searchItem.setOnActionExpandListener(object: MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                requireActivity().actionBar?.setDisplayHomeAsUpEnabled(false)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                requireActivity().actionBar?.setDisplayHomeAsUpEnabled(false)
                return true
            }
        })
        val searchView = searchItem?.actionView as SearchView
        searchView.queryHint="Search Videos"
        val homeButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_button)

        // Set the color of the home button icon
        homeButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white), PorterDuff.Mode.SRC_ATOP)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText != null){
                    MainActivity.searchList = ArrayList()
                    for(video in MainActivity.videoList){
                        if(video.title.lowercase().contains(newText.lowercase()))
                            MainActivity.searchList.add(video)
                    }
                    MainActivity.search = true
                    adapter.updateList(searchList = MainActivity.searchList)
                }
                return true
            }
        })

        // Customize text color and hint color
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(Color.WHITE)
        searchEditText.setHintTextColor(Color.WHITE)

        // Customize search view background
        val searchPlate = searchView.findViewById<View>(androidx.appcompat.R.id.search_plate)
        searchPlate.setBackgroundColor(Color.TRANSPARENT)

        // Customize close button
        val closeButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        closeButton.setImageDrawable(ContextCompat.getDrawable(requireContext(), com.google.android.material.R.drawable.ic_m3_chip_close))
        closeButton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)


        val backButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_button)
        backButton.setImageDrawable(ContextCompat.getDrawable(requireContext(), com.google.android.material.R.drawable.ic_clear_black_24))
        backButton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        backButton.visibility = View.GONE


        super.onCreateOptionsMenu(menu, inflater)
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
//        if(PlayerActivity.position != -1) binding.nowPlayingBtn.visibility = View.VISIBLE
//        if(MainActivity.adapterChanged) adapter.notifyDataSetChanged()
//        MainActivity.adapterChanged = false
    }
}