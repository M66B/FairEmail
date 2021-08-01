package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2021 by Marcel Bokhorst (M66B)
*/

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

public class ActivitySearch extends ActivityBase {
    private static final String SEARCH_SCHEME = "eu.faircode.email.search";

    @Override
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CharSequence query;

        Uri uri = getIntent().getData();
        if (uri == null || !SEARCH_SCHEME.equals(uri.getScheme()))
            query = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        else
            query = Uri.decode(uri.toString().substring(SEARCH_SCHEME.length() + 1));

        Log.i("External search query=" + query);
        Intent view = new Intent(this, ActivityView.class);
        view.putExtra(Intent.EXTRA_PROCESS_TEXT, query);
        startActivity(view);

        finish();
    }
}
