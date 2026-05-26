package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.network.ApiResult
import com.solari.app.data.user.UserRepository
import com.solari.app.ui.models.BlockedUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private const val BlockedAccountsPageSize = 50

class BlockedAccountsViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    var blockedUsers by mutableStateOf<List<BlockedUser>>(emptyList())
        private set

    var sort by mutableStateOf("newest")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isLoadingMoreBlockedUsers by mutableStateOf(false)
        private set

    var canLoadMoreBlockedUsers by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var blockedUsersNextCursor by mutableStateOf<String?>(null)
    private var blockedUsersLoadGeneration = 0

    init {
        loadBlockedUsers()
    }

    fun updateSort(newSort: String) {
        val normalizedSort = newSort.toBlockedAccountsSort()
        if (sort == normalizedSort) return
        sort = normalizedSort
        loadBlockedUsers()
    }

    fun refresh() {
        loadBlockedUsers()
    }

    fun unblockUser(userId: String) {
        viewModelScope.launch {
            when (val result = userRepository.unblockUser(userId)) {
                is ApiResult.Success -> {
                    blockedUsers = blockedUsers.filterNot { it.user.id == userId }
                    errorMessage = null
                    if (canLoadMoreBlockedUsers) {
                        loadMoreBlockedUsers()
                    }
                }

                is ApiResult.Failure -> errorMessage = result.message
            }
        }
    }

    fun loadMoreBlockedUsers() {
        val cursor = blockedUsersNextCursor ?: return
        if (isLoading || isLoadingMoreBlockedUsers || !canLoadMoreBlockedUsers) return
        val requestGeneration = blockedUsersLoadGeneration

        viewModelScope.launch {
            isLoadingMoreBlockedUsers = true
            errorMessage = null

            try {
                when (
                    val result = userRepository.getBlockedUsersPage(
                        limit = BlockedAccountsPageSize,
                        sort = sort,
                        cursor = cursor
                    )
                ) {
                    is ApiResult.Success -> {
                        if (requestGeneration != blockedUsersLoadGeneration) {
                            return@launch
                        }
                        val existingIds = blockedUsers.mapTo(mutableSetOf()) { it.user.id }
                        val newBlockedUsers = result.data.items.filter {
                            existingIds.add(it.user.id)
                        }
                        blockedUsers = blockedUsers + newBlockedUsers
                        blockedUsersNextCursor = result.data.nextCursor
                        canLoadMoreBlockedUsers = blockedUsersNextCursor != null
                    }

                    is ApiResult.Failure -> {
                        if (requestGeneration == blockedUsersLoadGeneration) {
                            errorMessage = result.message
                            canLoadMoreBlockedUsers = false
                        }
                    }
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                if (requestGeneration == blockedUsersLoadGeneration) {
                    errorMessage = throwable.message ?: "Failed to load more blocked accounts"
                    canLoadMoreBlockedUsers = false
                }
            } finally {
                if (requestGeneration == blockedUsersLoadGeneration) {
                    isLoadingMoreBlockedUsers = false
                }
            }
        }
    }

    private fun loadBlockedUsers() {
        blockedUsersLoadGeneration += 1
        val requestGeneration = blockedUsersLoadGeneration

        viewModelScope.launch {
            isLoading = true
            isLoadingMoreBlockedUsers = false
            canLoadMoreBlockedUsers = false
            blockedUsersNextCursor = null
            errorMessage = null

            try {
                when (
                    val result = userRepository.getBlockedUsersPage(
                        limit = BlockedAccountsPageSize,
                        sort = sort,
                        cursor = null
                    )
                ) {
                    is ApiResult.Success -> {
                        if (requestGeneration != blockedUsersLoadGeneration) {
                            return@launch
                        }
                        blockedUsers = result.data.items
                        blockedUsersNextCursor = result.data.nextCursor
                        canLoadMoreBlockedUsers = blockedUsersNextCursor != null
                    }

                    is ApiResult.Failure -> errorMessage = result.message
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to load blocked accounts"
            } finally {
                if (requestGeneration == blockedUsersLoadGeneration) {
                    isLoading = false
                }
            }
        }
    }

    private fun String.toBlockedAccountsSort(): String {
        return when (this) {
            "oldest" -> "oldest"
            else -> "newest"
        }
    }
}
