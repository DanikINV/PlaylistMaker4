package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.dto.TracksResponseDto
import com.example.playlistmaker.data.dto.toDomain
import com.example.playlistmaker.data.network.ITunesApi
import com.example.playlistmaker.domain.interactors.SearchConsumer
import com.example.playlistmaker.domain.repository.SearchRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchRepositoryImpl(
    private val service: ITunesApi
) : SearchRepository {

    override fun search(
        query: String,
        consumer: SearchConsumer
    ) {

        service.search(query).enqueue(
            object : Callback<TracksResponseDto> {

                override fun onResponse(
                    call: Call<TracksResponseDto>,
                    response: Response<TracksResponseDto>
                ) {

                    if (!response.isSuccessful) {
                        consumer.consumeError()
                        return
                    }

                    val tracks = response.body()
                        ?.results
                        ?.map { it.toDomain() }
                        .orEmpty()

                    consumer.consume(tracks)
                }

                override fun onFailure(
                    call: Call<TracksResponseDto>,
                    t: Throwable
                ) {
                    if (call.isCanceled) {
                        return
                    }

                    consumer.consumeError()
                }
            }
        )
    }
}