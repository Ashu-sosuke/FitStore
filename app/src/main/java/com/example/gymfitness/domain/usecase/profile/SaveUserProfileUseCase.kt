package com.example.gymfitness.domain.usecase.profile

import com.example.gymfitness.data.local.entity.UserEntity
import com.example.gymfitness.domain.repository.UserRepository

class SaveUserProfileUseCase(
    private val repository: UserRepository
) {

    suspend operator fun invoke(user: UserEntity) {
        repository.saveUser(user)
    }

}