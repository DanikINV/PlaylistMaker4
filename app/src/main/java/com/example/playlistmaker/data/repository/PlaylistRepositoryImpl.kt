package com.example.playlistmaker.data.repository


import com.example.playlistmaker.data.db.PlaylistDao
import com.example.playlistmaker.data.db.PlaylistEntity
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.repository.PlaylistRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao
) : PlaylistRepository {



    private val gson =
        Gson()




    override suspend fun createPlaylist(
        playlist: Playlist
    ) {


        playlistDao.insertPlaylist(

            PlaylistEntity(

                name = playlist.name,

                description = playlist.description,

                coverPath = playlist.coverPath,

                trackIds =
                    gson.toJson(
                        playlist.trackIds
                    ),

                tracksCount =
                    playlist.tracksCount
            )
        )
    }





    override fun getPlaylists():
            Flow<List<Playlist>> {


        return playlistDao
            .getPlaylists()
            .map { list ->


                list.map { entity ->


                    Playlist(


                        playlistId =
                            entity.playlistId,


                        name =
                            entity.name,


                        description =
                            entity.description,


                        coverPath =
                            entity.coverPath,


                        trackIds =
                            gson.fromJson(

                                entity.trackIds,

                                object :
                                    TypeToken<List<Long>>() {}.type
                            ),


                        tracksCount =
                            entity.tracksCount
                    )
                }
            }
    }





    override suspend fun addTrackToPlaylist(
        playlistId: Long,
        trackId: Long
    ): Boolean {


        val playlist =

            playlistDao.getPlaylist(
                playlistId
            )
                ?: return false




        val trackIds =

            gson.fromJson<List<Long>>(

                playlist.trackIds,

                object :
                    TypeToken<List<Long>>() {}.type

            ).toMutableList()




        if (
            trackIds.contains(trackId)
        ) {

            return false
        }




        trackIds.add(trackId)




        playlistDao.updatePlaylist(

            playlist.copy(

                trackIds =
                    gson.toJson(trackIds),


                tracksCount =
                    trackIds.size
            )
        )



        return true
    }
}