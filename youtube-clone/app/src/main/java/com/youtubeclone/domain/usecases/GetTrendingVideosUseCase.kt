package com.youtubeclone.domain.usecases

import com.youtubeclone.data.repository.VideoRepository
import com.youtubeclone.domain.models.Video
import javax.inject.Inject

class GetTrendingVideosUseCase @Inject constructor(
    private val repository: VideoRepository
) {
    suspend operator fun invoke(
        pageToken: String? = null,
        categoryId: String? = null
    ): Result<Pair<List<Video>, String?>> {
        return repository.getTrendingVideos(pageToken, categoryId).map { result ->
            result.videos to result.nextPageToken
        }
    }
}
