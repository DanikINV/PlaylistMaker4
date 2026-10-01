package com.example.playlistmaker.presentation.adapter

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ItemPlaylistBinding
import com.example.playlistmaker.domain.model.Playlist
import java.io.File

class PlaylistAdapter(
    private val onPlaylistClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    private var playlists =
        emptyList<Playlist>()

    fun setPlaylists(
        newPlaylists: List<Playlist>
    ) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlaylistViewHolder {

        val binding =
            ItemPlaylistBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: PlaylistViewHolder,
        position: Int
    ) {
        holder.bind(
            playlists[position]
        )
    }

    override fun getItemCount(): Int {
        return playlists.size
    }

    inner class PlaylistViewHolder(
        private val binding: ItemPlaylistBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            playlist: Playlist
        ) {

            binding.tvPlaylistName.text =
                playlist.name

            binding.tvTracksCount.text =
                binding.root.context.resources.getQuantityString(
                    R.plurals.tracks_count,
                    playlist.tracksCount,
                    playlist.tracksCount
                )

            val isNightMode =
                binding.root.context.resources.configuration.uiMode and
                        Configuration.UI_MODE_NIGHT_MASK ==
                        Configuration.UI_MODE_NIGHT_YES

            if (isNightMode) {
                binding.tvPlaylistName.setTextColor(
                    binding.root.context.getColor(
                        android.R.color.white
                    )
                )
            } else {
                binding.tvPlaylistName.setTextColor(
                    binding.root.context.getColor(
                        R.color.text_primary
                    )
                )
            }

            binding.tvTracksCount.setTextColor(
                binding.root.context.getColor(
                    R.color.text_secondary
                )
            )

            binding.ivPlaylistCover.setImageResource(
                R.drawable.vector
            )

            val coverPath =
                playlist.coverPath

            if (!coverPath.isNullOrEmpty()) {

                val coverFile =
                    File(coverPath)

                Glide.with(binding.root)
                    .load(coverFile)
                    .transform(
                        RoundedCorners(
                            (
                                    16 *
                                            binding.root
                                                .resources
                                                .displayMetrics
                                                .density
                                    ).toInt()
                        )
                    )
                    .placeholder(
                        R.drawable.vector
                    )
                    .error(
                        R.drawable.vector
                    )
                    .into(
                        binding.ivPlaylistCover
                    )
            }

            binding.root.setOnClickListener {
                onPlaylistClick(
                    playlist
                )
            }
        }
    }
}