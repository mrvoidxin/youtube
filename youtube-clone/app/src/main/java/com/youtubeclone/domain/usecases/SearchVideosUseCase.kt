package com.youtubeclone.domain.usecases

import com.youtubeclone.data.repository.SearchRepository
import com.youtubeclone.domain.models.Video
import javax.inject.Inject

class SearchVideosUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(
        query: String,
        pageToken: String? = null
    ): Result<Pair<List<Video>, String?>> {
        return repository.searchVideos(query, pageToken).map { result ->
            result.videos to result.nextPageToken
        }
    }
}
