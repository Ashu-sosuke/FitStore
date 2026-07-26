package com.example.gymfitness.domain.usecase.profile

import com.example.gymfitness.domain.models.UserProfile
import com.example.gymfitness.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(deviceId: String): Flow<UserProfile?> {
        return repository.getProfileFlow(deviceId)
    }
}
