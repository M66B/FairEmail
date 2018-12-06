package eu.faircode.email;

import android.content.Intent;
import android.os.Bundle;

public class ActivitySearch extends ActivityBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent view = new Intent(this, ActivityView.class);
        view.putExtra(Intent.EXTRA_PROCESS_TEXT, getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT));
        startActivity(view);

        finish();
    }
}
