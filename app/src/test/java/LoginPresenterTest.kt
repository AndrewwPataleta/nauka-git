import android.app.Application
import androidx.test.core.app.ApplicationProvider
import io.reactivex.Completable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import org.robolectric.RobolectricTestRunner
import toothpick.Toothpick
import uddug.com.data.validator.FieldsValidator
import uddug.com.domain.interactors.auth.AuthInteractor
import uddug.com.domain.utils.logging.ILogger
import uddug.com.naukoteka.di.DI
import uddug.com.naukoteka.di.modules.AppModule
import uddug.com.naukoteka.navigation.AppRouter
import uddug.com.naukoteka.presentation.login.LoginPresenter
import uddug.com.naukoteka.presentation.login.LoginView
import uddug.com.naukoteka.presentation.login.`LoginView$$State`

@RunWith(MockitoJUnitRunner::class)
class LoginPresenterTest {

    private lateinit var loginPresenter: LoginPresenter

    private val getAppScope = Toothpick.openRootScope()
        .openSubScope(DI.APP_SCOPE)

    @get:Rule
    val mockitoRule get() = MockitoJUnit.rule()!!

    @Mock
    var authInteractor: AuthInteractor = Mockito.mock(AuthInteractor::class.java)

    @Mock
    var fieldsValidator: FieldsValidator = Mockito.mock(FieldsValidator::class.java)

    @Mock
    var router: AppRouter = Mockito.mock(AppRouter::class.java)

    @Mock
    var logger: ILogger = Mockito.mock(ILogger::class.java)

    @Mock
    var view: LoginView = Mockito.mock(LoginView::class.java)

    var application: Application= ApplicationProvider.getApplicationContext()
    val appScope = getAppScope.installTestModules(AppModule(application))
    val interactor = appScope.getInstance(AuthInteractor::class.java)


    @Mock
    lateinit var viewState: `LoginView$$State`

    private fun initPresenter() =
        LoginPresenter(authInteractor, fieldsValidator, router, logger).apply {
            attachView(this@LoginPresenterTest.view)
            setViewState(this@LoginPresenterTest.viewState)
        }

    @Before
    fun setup() {
        `when`(interactor.login("romanakamoran@gmail.com", "123QWEasdZXC$"))
            .thenReturn(Completable.complete())
        
    }

    @Test
    fun `Pass data to login with correct credentials`() {
        verify(interactor).login("romanakamoran@gmail.com", "123QWEasdZXC$")

    }

}
