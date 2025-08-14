package uddug.com.naukoteka.utils.rx

import android.annotation.SuppressLint
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView

typealias OnComplete = () -> Unit


@SuppressLint("CheckResult")
fun <V : MvpView> Completable.subscribe(
    withProgress: Boolean,
    presenter: BasePresenterImpl<V>,
    onError: (throwable: Throwable) -> Unit,
    onComplete: OnComplete
): Disposable {

    if (withProgress) {
        (presenter.viewState as? LoadingView)?.showLoading(true)
    }
    return doOnComplete { if (withProgress) (presenter.viewState as? LoadingView)?.showLoading(false) }
        .doOnError { if (withProgress) (presenter.viewState as? LoadingView)?.showLoading(false) }
        .subscribe(onComplete, onError)

}

