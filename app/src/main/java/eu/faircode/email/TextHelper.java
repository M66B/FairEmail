package eu.faircode.email;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.textclassifier.TextClassificationManager;
import android.view.textclassifier.TextClassifier;
import android.view.textclassifier.TextLanguage;

import java.util.Locale;

public class TextHelper {

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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

    static Locale detectLanguage(Context context, String text) {
        // Why not ML kit? https://developers.google.com/ml-kit/terms
        if (TextUtils.isEmpty(text))
            return null;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            return null;

        // https://issuetracker.google.com/issues/173337263
        TextClassificationManager tcm =
                (TextClassificationManager) context.getSystemService(Context.TEXT_CLASSIFICATION_SERVICE);
        if (tcm == null)
            return null;

        TextLanguage.Request trequest = new TextLanguage.Request.Builder(text).build();
        TextClassifier tc = tcm.getTextClassifier();
        TextLanguage tlanguage = tc.detectLanguage(trequest);
        if (tlanguage.getLocaleHypothesisCount() > 0)
            return tlanguage.getLocale(0).toLocale();

        return null;
    }
}
