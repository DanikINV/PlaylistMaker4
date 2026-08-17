package com.example.playlistmaker

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.text.SimpleDateFormat
import java.util.Locale

class TrackAdapter(
    private val tracks: List<Track>,
    private val onTrackClick: (Track) -> Unit
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        return TrackViewHolder(parent)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track)
        holder.itemView.setOnClickListener { onTrackClick(track) }
    }

    override fun getItemCount(): Int = tracks.size

    class TrackViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
    ) {
        private val artwork: ImageView = itemView.findViewById(R.id.iv_track_artwork)
        private val trackName: TextView = itemView.findViewById(R.id.tv_track_name)
        private val artistTime: TextView = itemView.findViewById(R.id.tv_track_artist_time)
        private val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())

        fun bind(track: Track) {
            trackName.text = track.trackName
            val formattedTime = timeFormat.format(track.trackTime)
            artistTime.text = "${track.artistName} \u2022 $formattedTime"

            val cornerRadiusPx = itemView.context.resources
                .getDimensionPixelSize(R.dimen.track_artwork_corner_radius)

            Glide.with(itemView)
                .load(track.artworkUrl100)
                .placeholder(R.drawable.vector)
                .error(R.drawable.vector)
                .transform(MultiTransformation(CenterCrop(), RoundedCorners(cornerRadiusPx)))
                .into(artwork)
        }
    }
}