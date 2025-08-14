package uddug.com.naukoteka.global.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import moxy.MvpAppCompatDialogFragment
import uddug.com.domain.utils.logging.ILogger
import uddug.com.naukoteka.di.DI
import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import uddug.com.naukoteka.ui.custom.square_toast.ToastInfo
import toothpick.Scope
import toothpick.ktp.KTP
import toothpick.ktp.delegate.inject
import uddug.com.naukoteka.ui.fragments.profile.MvpAppCompatBottomDialogFragment

abstract class BaseBottomSheetDialogFragment : MvpAppCompatBottomDialogFragment(), LoadingView,
    InformativeView {

    val logger : ILogger by inject()

    abstract val contentView: ViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getScope()
            .inject(this)
    }

    open fun getScope(): Scope {
        return KTP.openRootScope()
            .openSubScope(DI.APP_SCOPE)
            .openSubScope(DI.MAIN_ACTIVITY_SCOPE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        logger.debug("onCreateView from ${javaClass.canonicalName}")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroy() {
        logger.debug("onDestroy from ${javaClass.canonicalName}")
        super.onDestroy()
    }

    open fun onFragmentResume() {
        childFragmentManager.fragments
            .filterIsInstance(BaseBottomSheetDialogFragment::class.java)
            .forEach { it.onFragmentResume() }
    }

    override fun showLoading(show: Boolean) {
        (activity as? LoadingView)?.showLoading(show)
    }

    override fun showRefreshLoading(show: Boolean) {
    }

    override fun showInfoMessage(message: Int?) {
        (activity as? InformativeView)?.showInfoMessage(message)
    }

    override fun showInfoMessage(message: String?) {
        (activity as? InformativeView)?.showInfoMessage(message)
    }

    override fun showActionMessage(message: String?, action: String, invoker: () -> Unit) {
        (activity as? InformativeView)?.showActionMessage(message, action, invoker)
    }

    override fun showActionMessage(message: Int?, action: Int, invoker: () -> Unit) {
        (activity as? InformativeView)?.showActionMessage(message, action, invoker)
    }

    override fun showMessage(data: ToastInfo) {
        (activity as? InformativeView)?.showMessage(data)
    }

    fun setBackpressedDispatcherActionListener(actionListener: (() -> Unit)) {

    }

}
