package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.db.FavoriteTracksDatabase
import com.example.playlistmaker.data.dto.TracksResponseDto
import com.example.playlistmaker.data.dto.toDomain
import com.example.playlistmaker.data.network.ITunesApi
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchRepositoryImpl(
    private val service: ITunesApi,
    private val database: FavoriteTracksDatabase
) : SearchRepository {

    override fun search(
        query: String
    ): Flow<Result<List<Track>>> = flow {

        try {
            val response = service.search(query)

            if (!response.isSuccessful) {
                emit(
                    Result.failure(
                        Exception("HTTP ${response.code()}")
                    )
                )
                return@flow
            }

            val favoriteTrackIds =
                database.favoriteTrackDao()
                    .getFavoriteTrackIds()

            val tracks = response.body()
                ?.results
                ?.map { dto ->
                    dto.toDomain().apply {
                        isFavorite = trackId in favoriteTrackIds
                    }
                }
                .orEmpty()

            emit(
                Result.success(tracks)
            )

        } catch (e: Exception) {
            emit(
                Result.failure(e)
            )
        }
    }
}