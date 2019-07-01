package eu.faircode.email;

import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_CANCELED;

public class DialogFragmentEx extends DialogFragment {
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        sendResult(RESULT_CANCELED);
    }

    protected void sendResult(int result) {
        Fragment target = getTargetFragment();
        if (target != null) {
            Intent data = new Intent();
            data.putExtra("args", getArguments());
            target.onActivityResult(getTargetRequestCode(), result, null);
        }
    }
}
