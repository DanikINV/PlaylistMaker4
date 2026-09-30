package com.example.playlistmaker.presentation.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ItemPlaylistSheetBinding
import com.example.playlistmaker.domain.model.Playlist

class PlaylistSheetAdapter(
    private val onPlaylistClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistSheetAdapter.PlaylistViewHolder>() {

    private var playlists = emptyList<Playlist>()

    fun setPlaylists(newPlaylists: List<Playlist>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlaylistViewHolder {

        val binding = ItemPlaylistSheetBinding.inflate(
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
        holder.bind(playlists[position])
    }

    override fun getItemCount(): Int {
        return playlists.size
    }

    inner class PlaylistViewHolder(
        private val binding: ItemPlaylistSheetBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {

            binding.tvPlaylistName.text =
                playlist.name

            binding.tvTracksCount.text =
                binding.root.context.resources.getQuantityString(
                    R.plurals.tracks_count,
                    playlist.tracksCount,
                    playlist.tracksCount
                )

            binding.ivPlaylistCover.setImageResource(
                R.drawable.vector
            )

            val coverPath = playlist.coverPath

            if (!coverPath.isNullOrEmpty()) {

                Glide.with(binding.root)
                    .load(Uri.parse(coverPath))
                    .transform(
                        RoundedCorners(
                            (
                                    12 *
                                            binding.root.resources
                                                .displayMetrics
                                                .density
                                    ).toInt()
                        )
                    )
                    .error(R.drawable.vector)
                    .into(binding.ivPlaylistCover)
            }

            binding.root.setOnClickListener {
                onPlaylistClick(playlist)
            }
        }
    }
}