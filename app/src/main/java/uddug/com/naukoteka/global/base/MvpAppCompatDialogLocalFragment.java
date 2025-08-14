package uddug.com.naukoteka.global.base;

import static android.app.PendingIntent.getActivity;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import moxy.MvpAppCompatDialogFragment;
import moxy.MvpDelegate;
import moxy.MvpDelegateHolder;

public class MvpAppCompatDialogLocalFragment extends FragmentActivity implements MvpDelegateHolder {

    private boolean isStateSaved;

    private MvpDelegate<? extends MvpAppCompatDialogFragment> mvpDelegate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getMvpDelegate().onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        isStateSaved = false;

        getMvpDelegate().onAttach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        isStateSaved = true;

        getMvpDelegate().onSaveInstanceState(outState);
        getMvpDelegate().onDetach();
    }

    @Override
    public void onStop() {
        super.onStop();

        getMvpDelegate().onDetach();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        //We leave the screen and respectively all fragments will be destroyed
        if (getParent().isFinishing()) {
            getMvpDelegate().onDestroy();
            return;
        }

        // When we rotate device isRemoving() return true for fragment placed in backstack
        // http://stackoverflow.com/questions/34649126/fragment-back-stack-and-isremoving
        if (isStateSaved) {
            isStateSaved = false;
            return;
        }

        boolean anyParentIsRemoving = false;

    }

    /**
     * @return The {@link MvpDelegate} being used by this Fragment.
     */
    @Override
    public MvpDelegate getMvpDelegate() {
        return mvpDelegate;
    }
}
