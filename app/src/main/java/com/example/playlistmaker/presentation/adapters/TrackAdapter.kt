package com.example.playlistmaker.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track

class TrackAdapter(
    private val onTrackClick: (Track) -> Unit,
    private val onTrackLongClick: (Track) -> Unit = {}
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    private var tracks = emptyList<Track>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TrackViewHolder {
        return TrackViewHolder(parent)
    }

    override fun onBindViewHolder(
        holder: TrackViewHolder,
        position: Int
    ) {
        val track = tracks[position]

        holder.bind(track)

        holder.itemView.setOnClickListener {
            onTrackClick(track)
        }

        holder.itemView.setOnLongClickListener {
            onTrackLongClick(track)
            true
        }
    }

    override fun getItemCount(): Int =
        tracks.size

    fun setTracks(
        newTracks: List<Track>
    ) {
        tracks = newTracks
        notifyDataSetChanged()
    }

    class TrackViewHolder(
        parent: ViewGroup
    ) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_track,
                parent,
                false
            )
    ) {

        private val artwork: ImageView =
            itemView.findViewById(
                R.id.iv_track_artwork
            )

        private val trackName: TextView =
            itemView.findViewById(
                R.id.tv_track_name
            )

        private val artistTime: TextView =
            itemView.findViewById(
                R.id.tv_track_artist_time
            )

        fun bind(track: Track) {

            trackName.text =
                track.trackName

            val minutes =
                track.trackTime / 60000

            val seconds =
                (track.trackTime % 60000) / 1000

            val formattedTime =
                String.format(
                    "%02d:%02d",
                    minutes,
                    seconds
                )

            artistTime.text =
                "${track.artistName} • $formattedTime"

            val cornerRadiusPx =
                itemView.context.resources
                    .getDimensionPixelSize(
                        R.dimen.track_artwork_corner_radius
                    )

            Glide.with(itemView)
                .load(track.artworkUrl100)
                .placeholder(R.drawable.vector)
                .error(R.drawable.vector)
                .transform(
                    MultiTransformation(
                        CenterCrop(),
                        RoundedCorners(
                            cornerRadiusPx
                        )
                    )
                )
                .into(artwork)
        }
    }
}