package androidx.appcompat.widget;

import android.content.Context;
import android.util.AttributeSet;

public class PersistentSearchView extends SearchView {

    public CharSequence lastQuery;

    public PersistentSearchView(Context context) {
        super(context);
    }

    public PersistentSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PersistentSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onActionViewCollapsed() {
        lastQuery = getQuery();
        super.onActionViewCollapsed();
    }

    @Override
    public void onActionViewExpanded() {
        super.onActionViewExpanded();
        setQuery(lastQuery, false);
    }
}
