package app

import kotlinx.coroutines.CancellationException

/** Like [runCatching], but a cancellation still cancels the caller. */
inline fun <T> runCatchingNonCancel(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    Result.failure(e)
}
