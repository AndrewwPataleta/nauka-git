package uddug.com.naukoteka.mvvm.chat

import io.reactivex.Single
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> Single<T>.await(): T = suspendCancellableCoroutine { cont ->
    val disposable = this.subscribe(
        { value -> if (cont.isActive) cont.resume(value) },
        { error -> if (cont.isActive) cont.resumeWithException(error) }
    )
    cont.invokeOnCancellation { disposable.dispose() }
}
