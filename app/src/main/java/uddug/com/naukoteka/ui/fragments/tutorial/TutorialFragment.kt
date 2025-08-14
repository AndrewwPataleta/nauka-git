package uddug.com.naukoteka.ui.fragments.tutorial

import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import uddug.com.domain.entities.TutorialEntity
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentTutorialSearchBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.tutorial.TutorialPresenter
import uddug.com.naukoteka.presentation.tutorial.TutorialView
import uddug.com.naukoteka.utils.BackButtonListener
import uddug.com.naukoteka.utils.viewBinding
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class TutorialFragment : BaseFragment(R.layout.fragment_tutorial_search), TutorialView,
    BackButtonListener {

    override val contentView by viewBinding(FragmentTutorialSearchBinding::bind)

    @InjectPresenter
    lateinit var presenter: TutorialPresenter

    @ProvidePresenter
    fun providePresenter(): TutorialPresenter {
        return getScope().getInstance(TutorialPresenter::class.java)
    }

    override fun showTutorialInformation(resources: List<TutorialEntity>) {
        contentView.run {
            tvSkip.setOnClickListener {
                presenter.finishTutorial()
            }
            viewPager.adapter = TutorialAdapter().apply { setItems(resources) }
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    tvSkip.isVisible = position != 2
                    if (position == 2) {
                        btnTutorialSearchNext.text = getString(R.string.ready)
                        btnTutorialSearchNext.setOnClickListener {
                            presenter.finishTutorial()
                        }
                    } else {
                        btnTutorialSearchNext.text = getString(R.string.next)
                        btnTutorialSearchNext.setOnClickListener {
                            val getItem = viewPager.currentItem
                            viewPager.setCurrentItem(getItem + 1, true)
                        }
                    }
                }
            })
         //   springDotsIndicator.setViewPager2(viewPager)
        }
    }

    override fun onBackPressed(): Boolean {
        presenter.exit()
        return false
    }
}