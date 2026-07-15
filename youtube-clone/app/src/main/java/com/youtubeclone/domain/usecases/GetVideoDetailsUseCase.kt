package com.youtubeclone.domain.usecases

import com.youtubeclone.data.repository.VideoRepository
import com.youtubeclone.domain.models.Video
import javax.inject.Inject

class GetVideoDetailsUseCase @Inject constructor(
    private val repository: VideoRepository
) {
    suspend operator fun invoke(videoId: String): Result<Video> {
        return repository.getVideoDetails(videoId)
    }
}
