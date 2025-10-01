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

        
        if (getParent().isFinishing()) {
            getMvpDelegate().onDestroy();
            return;
        }

        
        
        if (isStateSaved) {
            isStateSaved = false;
            return;
        }

        boolean anyParentIsRemoving = false;

    }

    


    @Override
    public MvpDelegate getMvpDelegate() {
        return mvpDelegate;
    }
}
