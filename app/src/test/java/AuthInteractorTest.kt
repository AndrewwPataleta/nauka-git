import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import uddug.com.domain.SchedulersProvider
import uddug.com.domain.interactors.auth.AuthInteractor
import uddug.com.domain.repositories.auth.AuthRepository
import uddug.com.naukoteka.di.providers.AppSchedulersProvider

@RunWith(MockitoJUnitRunner::class)
class AuthInteractorTest {

    @Mock
    lateinit var repository: AuthRepository

    @Mock
    lateinit var schedulers: SchedulersProvider 

    private lateinit var interactor: AuthInteractor

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        interactor = spy(AuthInteractor(repository, schedulers))
    }

    @Test
    fun testLogin() {
        `when`(repository.loginEmail("", "")).then { Completable.complete() }
        `when`(schedulers.io()).then { Schedulers.io() }
        

        var loginemitter = interactor.login("","").test()
        verify(repository).loginEmail("","")
        loginemitter.awaitTerminalEvent()
        loginemitter.assertNoErrors()
        loginemitter.assertValueCount(1)

    }

}
