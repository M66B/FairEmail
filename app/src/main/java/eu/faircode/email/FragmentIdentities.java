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

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import static android.app.Activity.RESULT_OK;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FragmentIdentities extends FragmentBase {
    private boolean cards;
    private boolean dividers;

    private RecyclerView rvIdentity;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;
    private FloatingActionButton fab;
    private ObjectAnimator animator;

    private AdapterIdentity adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        cards = prefs.getBoolean("cards", true);
        dividers = prefs.getBoolean("dividers", true);
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_list_identities);

        View view = inflater.inflate(R.layout.fragment_identities, container, false);

        // Get controls
        rvIdentity = view.findViewById(R.id.rvIdentity);
        pbWait = view.findViewById(R.id.pbWait);
        grpReady = view.findViewById(R.id.grpReady);
        fab = view.findViewById(R.id.fab);

        // Wire controls

        rvIdentity.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvIdentity.setLayoutManager(llm);

        if (!cards && dividers) {
            DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), llm.getOrientation());
            itemDecorator.setDrawable(getContext().getDrawable(R.drawable.divider));
            rvIdentity.addItemDecoration(itemDecorator);
        }

        DividerItemDecoration categoryDecorator = new DividerItemDecoration(getContext(), llm.getOrientation()) {
            @Override
            public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int count = parent.getChildCount();
                for (int i = 0; i < count; i++) {
                    View view = parent.getChildAt(i);
                    int pos = parent.getChildAdapterPosition(view);

                    View header = getView(view, parent, pos);
                    if (header != null) {
                        canvas.save();
                        canvas.translate(0, parent.getChildAt(i).getTop() - header.getMeasuredHeight());
                        header.draw(canvas);
                        canvas.restore();
                    }
                }
            }

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int pos = parent.getChildAdapterPosition(view);
                View header = getView(view, parent, pos);
                if (header == null)
                    outRect.setEmpty();
                else
                    outRect.top = header.getMeasuredHeight();
            }

            private View getView(View view, RecyclerView parent, int pos) {
                if (pos == NO_POSITION)
                    return null;

                if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    return null;

                TupleIdentityEx prev = adapter.getItemAtPosition(pos - 1);
                TupleIdentityEx identity = adapter.getItemAtPosition(pos);
                if (pos > 0 && prev == null)
                    return null;
                if (identity == null)
                    return null;

                if (pos > 0) {
                    if (Objects.equals(prev.accountCategory, identity.accountCategory))
                        return null;
                } else {
                    if (identity.accountCategory == null)
                        return null;
                }

                View header = inflater.inflate(R.layout.item_group, parent, false);
                TextView tvCategory = header.findViewById(R.id.tvCategory);
                TextView tvDate = header.findViewById(R.id.tvDate);

                if (cards || !dividers) {
                    View vSeparator = header.findViewById(R.id.vSeparator);
                    vSeparator.setVisibility(View.GONE);
                }

                tvCategory.setText(identity.accountCategory);
                tvDate.setVisibility(View.GONE);

                header.measure(View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());

                return header;
            }
        };
        rvIdentity.addItemDecoration(categoryDecorator);

        adapter = new AdapterIdentity(this);
        rvIdentity.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentIdentity fragment = new FragmentIdentity();
                fragment.setArguments(new Bundle());
                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("identity");
                fragmentTransaction.commit();
            }
        });

        animator = Helper.getFabAnimator(fab, getViewLifecycleOwner());

        // Initialize
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Context context = getContext();
        final DB db = DB.getInstance(context);

        // Observe identities
        db.identity().liveIdentities().observe(getViewLifecycleOwner(), new Observer<List<TupleIdentityEx>>() {
            @Override
            public void onChanged(@Nullable List<TupleIdentityEx> identities) {
                if (identities == null)
                    identities = new ArrayList<>();

                adapter.set(identities);

                pbWait.setVisibility(View.GONE);
                grpReady.setVisibility(View.VISIBLE);

                if (identities.size() == 0) {
                    fab.setCustomSize(Helper.dp2pixels(context, 3 * 56 / 2));
                    if (animator != null && !animator.isStarted())
                        animator.start();
                } else {
                    fab.clearCustomSize();
                    if (animator != null && animator.isStarted())
                        animator.end();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case ActivitySetup.REQUEST_EDIT_IDENITY_COLOR:
                    if (resultCode == RESULT_OK && data != null)
                        onEditIdentityColor(data.getBundleExtra("args"));
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onEditIdentityColor(Bundle args) {
        if (!ActivityBilling.isPro(getContext())) {
            startActivity(new Intent(getContext(), ActivityBilling.class));
            return;
        }

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                Integer color = args.getInt("color");

                if (color == Color.TRANSPARENT)
                    color = null;

                DB db = DB.getInstance(context);
                db.identity().setIdentityColor(id, color);
                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "edit:color");
    }
}
