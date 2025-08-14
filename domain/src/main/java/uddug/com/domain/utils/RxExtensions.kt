package uddug.com.domain.utils

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single


fun <T> T.asObservable() = Observable.just(this)

fun <T> T.asSingle() = Single.just(this)

fun <T> T?.asMaybe() = this?.let { Maybe.just(it) } ?: Maybe.empty()

fun asCompletable(block: () -> Unit) = Completable.fromAction { block() }