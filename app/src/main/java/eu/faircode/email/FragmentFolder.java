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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Lifecycle;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class FragmentFolder extends FragmentBase {
    private ViewGroup view;
    private ScrollView scroll;

    private TextView tvParent;
    private EditText etName;
    private EditText etDisplay;
    private ViewButtonColor btnColor;
    private CheckBox cbHide;
    private CheckBox cbUnified;
    private CheckBox cbNavigation;
    private CheckBox cbNotify;
    private CheckBox cbSynchronize;
    private CheckBox cbPoll;
    private EditText etPoll;
    private TextView tvPoll;
    private CheckBox cbDownload;
    private Button btnInfo;
    private EditText etSyncDays;
    private EditText etKeepDays;
    private CheckBox cbKeepAll;
    private CheckBox cbAutoDelete;
    private Button btnSave;
    private ContentLoadingProgressBar pbSave;
    private TextView tvInboxRootHint;
    private ContentLoadingProgressBar pbWait;
    private Group grpParent;
    private Group grpPoll;
    private Group grpAutoDelete;

    private long id = -1;
    private long account = -1;
    private String parent = null;
    private boolean saving = false;
    private boolean deletable = false;

    private static final int REQUEST_COLOR = 1;
    private static final int REQUEST_SAVE_CHANGES = 101;
    private static final int REQUEST_DELETE_FOLDER = 102;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        id = args.getLong("id", -1);
        account = args.getLong("account", -1);
        parent = args.getString("parent");
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_edit_folder);
        setHasOptionsMenu(true);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_folder, container, false);
        scroll = view.findViewById(R.id.scroll);

        // Get controls
        etName = view.findViewById(R.id.etName);
        tvParent = view.findViewById(R.id.tvParent);
        etDisplay = view.findViewById(R.id.etDisplay);
        btnColor = view.findViewById(R.id.btnColor);
        cbHide = view.findViewById(R.id.cbHide);
        cbUnified = view.findViewById(R.id.cbUnified);
        cbNavigation = view.findViewById(R.id.cbNavigation);
        cbNotify = view.findViewById(R.id.cbNotify);
        cbSynchronize = view.findViewById(R.id.cbSynchronize);
        cbPoll = view.findViewById(R.id.cbPoll);
        etPoll = view.findViewById(R.id.etPoll);
        tvPoll = view.findViewById(R.id.tvPoll);
        cbDownload = view.findViewById(R.id.cbDownload);
        btnInfo = view.findViewById(R.id.btnInfo);
        etSyncDays = view.findViewById(R.id.etSyncDays);
        etKeepDays = view.findViewById(R.id.etKeepDays);
        cbKeepAll = view.findViewById(R.id.cbKeepAll);
        cbAutoDelete = view.findViewById(R.id.cbAutoDelete);
        btnSave = view.findViewById(R.id.btnSave);
        pbSave = view.findViewById(R.id.pbSave);
        tvInboxRootHint = view.findViewById(R.id.tvInboxRootHint);
        pbWait = view.findViewById(R.id.pbWait);
        grpParent = view.findViewById(R.id.grpParent);
        grpPoll = view.findViewById(R.id.grpPoll);
        grpAutoDelete = view.findViewById(R.id.grpAutoDelete);

        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putInt("color", btnColor.getColor());
                args.putString("title", getString(R.string.title_color));
                args.putBoolean("reset", true);

                FragmentDialogColor fragment = new FragmentDialogColor();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentFolder.this, REQUEST_COLOR);
                fragment.show(getParentFragmentManager(), "account:color");
            }
        });

        cbSynchronize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cbPoll.setEnabled(isChecked);
                etPoll.setEnabled(isChecked);
                tvPoll.setEnabled(isChecked);
                grpPoll.setVisibility(isChecked && cbPoll.isChecked() ? View.VISIBLE : View.GONE);
            }
        });

        cbPoll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                grpPoll.setVisibility(cbPoll.isEnabled() && isChecked ? View.VISIBLE : View.GONE);
            }
        });

        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.viewFAQ(getContext(), 39);
            }
        });

        cbKeepAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                etKeepDays.setEnabled(!isChecked);
                cbAutoDelete.setEnabled(!isChecked);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave(false);
            }
        });

        addKeyPressedListener(new ActivityBase.IKeyPressedListener() {
            @Override
            public boolean onKeyPressed(KeyEvent event) {
                return false;
            }

            @Override
            public boolean onBackPressed() {
                onSave(true);
                return true;
            }
        });

        // Initialize
        tvParent.setText(parent);
        grpParent.setVisibility(parent == null ? View.GONE : View.VISIBLE);
        Helper.setViewsEnabled(view, false);
        grpAutoDelete.setVisibility(View.GONE);
        btnSave.setEnabled(false);
        pbSave.setVisibility(View.GONE);
        tvInboxRootHint.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = new Bundle();
        args.putLong("id", id);

        new SimpleTask<EntityFolder>() {
            @Override
            protected EntityFolder onExecute(Context context, Bundle args) {
                long id = args.getLong("id");

                DB db = DB.getInstance(context);
                EntityFolder folder = db.folder().getFolder(id);

                if (folder != null) {
                    EntityAccount account = db.account().getAccount(folder.account);
                    if (account != null)
                        args.putInt("interval", account.poll_interval);
                }

                return folder;
            }

            @Override
            protected void onExecuted(Bundle args, EntityFolder folder) {
                if (savedInstanceState == null) {
                    int interval = args.getInt("interval", EntityAccount.DEFAULT_KEEP_ALIVE_INTERVAL);
                    etName.setText(folder == null ? null : folder.name);
                    etDisplay.setText(folder == null ? null : folder.display);
                    etDisplay.setHint(folder == null ? null : EntityFolder.localizeName(getContext(), folder.name));
                    btnColor.setColor(folder == null ? null : folder.color);
                    cbHide.setChecked(folder == null ? false : folder.hide);
                    cbUnified.setChecked(folder == null ? false : folder.unified);
                    cbNavigation.setChecked(folder == null ? false : folder.navigation);
                    cbNotify.setChecked(folder == null ? false : folder.notify);
                    cbSynchronize.setChecked(folder == null || folder.synchronize);
                    cbPoll.setChecked(folder == null ? false : folder.poll);
                    etPoll.setText(folder == null ? null : Integer.toString(folder.poll_factor));
                    tvPoll.setText(getString(R.string.title_factor_minutes, interval));
                    cbDownload.setChecked(folder == null ? true : folder.download);
                    etSyncDays.setText(Integer.toString(folder == null ? EntityFolder.DEFAULT_SYNC : folder.sync_days));
                    if (folder != null && folder.keep_days == Integer.MAX_VALUE)
                        cbKeepAll.setChecked(true);
                    else
                        etKeepDays.setText(Integer.toString(folder == null ? EntityFolder.DEFAULT_KEEP : folder.keep_days));

                    if (folder != null && folder.read_only)
                        grpAutoDelete.setVisibility(View.GONE);
                    else {
                        cbAutoDelete.setText(folder != null && EntityFolder.TRASH.equals(folder.type)
                                ? R.string.title_auto_delete : R.string.title_auto_trash);
                        cbAutoDelete.setChecked(folder != null && folder.auto_delete);
                        grpAutoDelete.setVisibility(View.VISIBLE);
                    }

                    tvInboxRootHint.setVisibility(folder == null && parent == null ? View.VISIBLE : View.GONE);
                }

                // Consider previous save as cancelled
                pbWait.setVisibility(View.GONE);

                Helper.setViewsEnabled(view, true);

                etName.setEnabled(folder == null || EntityFolder.USER.equals(folder.type));
                cbPoll.setEnabled(cbSynchronize.isChecked());
                etPoll.setEnabled(cbSynchronize.isChecked());
                tvPoll.setEnabled(cbSynchronize.isChecked());
                grpPoll.setVisibility(cbPoll.isEnabled() && cbPoll.isChecked() ? View.VISIBLE : View.GONE);
                etKeepDays.setEnabled(!cbKeepAll.isChecked());
                cbAutoDelete.setEnabled(!cbKeepAll.isChecked());
                btnSave.setEnabled(true);

                deletable = (folder != null && EntityFolder.USER.equals(folder.type));
                getActivity().invalidateOptionsMenu();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "folder:get");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_COLOR:
                    if (resultCode == RESULT_OK && data != null) {
                        if (ActivityBilling.isPro(getContext())) {
                            Bundle args = data.getBundleExtra("args");
                            btnColor.setColor(args.getInt("color"));
                        } else
                            startActivity(new Intent(getContext(), ActivityBilling.class));
                    }
                    break;
                case REQUEST_SAVE_CHANGES:
                    if (resultCode == RESULT_OK) {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                scroll.smoothScrollTo(0, btnSave.getBottom());
                            }
                        });
                        onSave(false);
                    } else if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                        getParentFragmentManager().popBackStack();
                    break;

                case REQUEST_DELETE_FOLDER:
                    if (resultCode == RESULT_OK)
                        onDelete();
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_folder, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_delete).setVisible(id > 0 && !saving && deletable);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                onMenuDelete();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuDelete() {
        Bundle aargs = new Bundle();
        aargs.putString("question", getString(R.string.title_folder_delete));

        FragmentDialogAsk ask = new FragmentDialogAsk();
        ask.setArguments(aargs);
        ask.setTargetFragment(FragmentFolder.this, REQUEST_DELETE_FOLDER);
        ask.show(getParentFragmentManager(), "folder:delete");
    }

    private void onSave(boolean should) {
        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putLong("account", account);
        args.putString("parent", parent);
        args.putString("name", etName.getText().toString());
        args.putString("display", etDisplay.getText().toString());
        args.putInt("color", btnColor.getColor());
        args.putBoolean("hide", cbHide.isChecked());
        args.putBoolean("unified", cbUnified.isChecked());
        args.putBoolean("navigation", cbNavigation.isChecked());
        args.putBoolean("notify", cbNotify.isChecked());
        args.putBoolean("synchronize", cbSynchronize.isChecked());
        args.putBoolean("poll", cbPoll.isChecked());
        args.putString("factor", etPoll.getText().toString());
        args.putBoolean("download", cbDownload.isChecked());
        args.putString("sync", etSyncDays.getText().toString());
        args.putString("keep", cbKeepAll.isChecked()
                ? Integer.toString(Integer.MAX_VALUE)
                : etKeepDays.getText().toString());
        args.putBoolean("auto_delete", cbAutoDelete.isChecked());

        args.putBoolean("should", should);

        new SimpleTask<Boolean>() {
            @Override
            protected void onPreExecute(Bundle args) {
                saving = true;
                getActivity().invalidateOptionsMenu();
                Helper.setViewsEnabled(view, false);
                pbSave.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                saving = false;
                getActivity().invalidateOptionsMenu();
                Helper.setViewsEnabled(view, true);
                pbSave.setVisibility(View.GONE);
            }

            @Override
            protected Boolean onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                long aid = args.getLong("account");
                String parent = args.getString("parent");
                String name = args.getString("name");
                String display = args.getString("display");
                Integer color = args.getInt("color");
                boolean hide = args.getBoolean("hide");
                boolean unified = args.getBoolean("unified");
                boolean navigation = args.getBoolean("navigation");
                boolean notify = args.getBoolean("notify");
                boolean synchronize = args.getBoolean("synchronize");
                boolean poll = args.getBoolean("poll");
                String factor = args.getString("factor");
                boolean download = args.getBoolean("download");
                String sync = args.getString("sync");
                String keep = args.getString("keep");
                boolean auto_delete = args.getBoolean("auto_delete");

                boolean pro = ActivityBilling.isPro(context);
                boolean should = args.getBoolean("should");

                if (color == Color.TRANSPARENT || !pro)
                    color = null;
                if (TextUtils.isEmpty(display) || display.equals(name))
                    display = null;

                int sync_days = (TextUtils.isEmpty(sync) ? EntityFolder.DEFAULT_SYNC : Integer.parseInt(sync));
                int keep_days = (TextUtils.isEmpty(keep) ? EntityFolder.DEFAULT_KEEP : Integer.parseInt(keep));
                if (keep_days < sync_days)
                    keep_days = sync_days;
                int poll_factor = (TextUtils.isEmpty(factor) ? 1 : Integer.parseInt(factor));
                if (poll_factor < 1)
                    poll_factor = 1;

                boolean reload;
                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityFolder folder = db.folder().getFolder(id);

                    if (should) {
                        if (folder == null)
                            return !TextUtils.isEmpty(name);

                        if (!Objects.equals(folder.name, name))
                            return true;
                        if (!Objects.equals(folder.display, display))
                            return true;
                        if (!Objects.equals(folder.color, color))
                            return true;
                        if (!Objects.equals(folder.unified, unified))
                            return true;
                        if (!Objects.equals(folder.navigation, navigation))
                            return true;
                        if (!Objects.equals(folder.notify, notify))
                            return true;
                        if (!Objects.equals(folder.hide, hide))
                            return true;
                        if (!Objects.equals(folder.synchronize, synchronize))
                            return true;
                        if (!Objects.equals(folder.poll, poll))
                            return true;
                        if (!Objects.equals(folder.poll_factor, poll_factor))
                            return true;
                        if (!Objects.equals(folder.download, download))
                            return true;
                        if (!Objects.equals(folder.sync_days, sync_days))
                            return true;
                        if (!Objects.equals(folder.keep_days, keep_days))
                            return true;
                        if (!Objects.equals(folder.auto_delete, auto_delete))
                            return true;

                        return false;
                    }

                    if (folder == null) {
                        reload = true;
                        Log.i("Creating folder=" + name + " parent=" + parent);

                        if (parent != null) {
                            EntityAccount account = db.account().getAccount(aid);
                            if (account == null)
                                return false;

                            name = parent + account.separator + name;
                        }

                        if (TextUtils.isEmpty(name))
                            throw new IllegalArgumentException(context.getString(R.string.title_folder_name_missing));
                        EntityFolder target = db.folder().getFolderByName(aid, name);
                        if (target != null)
                            throw new IllegalArgumentException(context.getString(R.string.title_folder_exists, name));

                        EntityFolder create = new EntityFolder();
                        create.account = aid;
                        create.name = name;
                        create.display = display;
                        create.color = color;
                        create.type = EntityFolder.USER;
                        create.unified = unified;
                        create.navigation = navigation;
                        create.notify = notify;
                        create.hide = hide;
                        create.synchronize = synchronize;
                        create.poll = poll;
                        create.poll_factor = poll_factor;
                        create.download = download;
                        create.sync_days = sync_days;
                        create.keep_days = keep_days;
                        create.auto_delete = auto_delete;
                        create.tbc = true;
                        db.folder().insertFolder(create);
                    } else {
                        if (!folder.name.equals(name)) {
                            EntityFolder target = db.folder().getFolderByName(folder.account, name);
                            if (target != null)
                                throw new IllegalArgumentException(context.getString(R.string.title_folder_exists, name));
                        }

                        reload = (!folder.name.equals(name) ||
                                !folder.poll.equals(poll));

                        Log.i("Updating folder=" + folder.name);
                        db.folder().setFolderProperties(id,
                                folder.name.equals(name) ? null : name,
                                display, color, unified, navigation, notify, hide,
                                synchronize, poll, poll_factor, download,
                                sync_days, keep_days, auto_delete);
                        db.folder().setFolderError(id, null);

                        if (!reload && synchronize)
                            EntityOperation.sync(context, folder.id, true);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (reload)
                    ServiceSynchronize.reload(context, aid, false, "save folder");
                else
                    ServiceSynchronize.eval(context, "save folder");

                return false;
            }

            @Override
            protected void onExecuted(Bundle args, Boolean dirty) {
                if (dirty) {
                    Bundle aargs = new Bundle();
                    aargs.putString("question", getString(R.string.title_ask_save));

                    FragmentDialogAsk ask = new FragmentDialogAsk();
                    ask.setArguments(aargs);
                    ask.setTargetFragment(FragmentFolder.this, REQUEST_SAVE_CHANGES);
                    ask.show(getParentFragmentManager(), "folder:save");
                } else if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    getParentFragmentManager().popBackStack();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "folder:save");
    }

    private void onDelete() {
        Helper.setViewsEnabled(view, false);
        pbSave.setVisibility(View.VISIBLE);

        Bundle args = new Bundle();
        args.putLong("id", id);

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long id = args.getLong("id");

                EntityFolder folder;

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    folder = db.folder().getFolder(id);
                    if (folder == null)
                        return null;

                    int count = db.operation().getOperationCount(folder.id, null);
                    if (count > 0)
                        throw new IllegalArgumentException(
                                context.getResources().getQuantityString(
                                        R.plurals.title_notification_operations, count, count));

                    db.folder().setFolderTbd(folder.id);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                ServiceSynchronize.reload(context, folder.account, false, "delete folder");

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    getParentFragmentManager().popBackStack();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.setViewsEnabled(view, true);
                pbSave.setVisibility(View.GONE);

                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "folder:delete");
    }
}
