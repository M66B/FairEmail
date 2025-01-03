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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ArrowKeyMovementMethod;
import android.text.style.URLSpan;
import android.util.Base64;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.textfield.TextInputLayout;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

public class ProtectedContent {
    static final int MAX_PROTECTED_TEXT = 1500;
    private static final int DECRYPT_ITERATIONS = 120000;
    private static final int DECRYPT_KEYLEN = 256;
    private static final String DECRYPT_DERIVATION = "PBKDF2WithHmacSHA512";
    private static final int DECRYPT_TAGLEN = 128;
    private static final String DECRYPT_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String DECRYPT_URL = "https://fairemail.net/decrypt/";

    static Uri toUri(Context context, String html, String password)
            throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (html.length() > MAX_PROTECTED_TEXT)
            throw new IllegalArgumentException(context.getString(R.string.title_style_protect_size));

        SecureRandom random = new SecureRandom();

        byte[] salt = new byte[16]; // 128 bits
        random.nextBytes(salt);

        byte[] iv = new byte[12]; // 96 bites
        random.nextBytes(iv);

        // Iterations = 120,000; Keylength = 256 bits = 32 bytes
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, DECRYPT_ITERATIONS, DECRYPT_KEYLEN);

        SecretKeyFactory skf = SecretKeyFactory.getInstance(DECRYPT_DERIVATION);
        SecretKey key = skf.generateSecret(spec);

        // Authentication tag length = 128 bits = 16 bytes
        GCMParameterSpec parameterSpec = new GCMParameterSpec(DECRYPT_TAGLEN, iv);

        final Cipher cipher = Cipher.getInstance(DECRYPT_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

        byte[] cipherText = cipher.doFinal(html.getBytes(StandardCharsets.UTF_8));

        ByteBuffer out = ByteBuffer.allocate(1 + salt.length + iv.length + cipherText.length);
        out.put((byte) 1); // version
        out.put(salt);
        out.put(iv);
        out.put(cipherText);

        String fragment = Base64.encodeToString(out.array(), Base64.URL_SAFE | Base64.NO_WRAP);
        return Uri.parse(DECRYPT_URL + "#" + fragment);
    }

    static String fromUri(Context context, Uri uri, String password) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] msg = Base64.decode(uri.getFragment(), Base64.URL_SAFE | Base64.NO_WRAP);

        int version = msg[0];
        if (version > 1)
            throw new IllegalArgumentException("Please update the app");

        byte[] salt = new byte[16]; // 128 bits
        System.arraycopy(msg, 1, salt, 0, salt.length);

        byte[] iv = new byte[12]; // 96 bites
        System.arraycopy(msg, 1 + salt.length, iv, 0, iv.length);

        byte[] encrypted = new byte[msg.length - 1 - salt.length - iv.length];
        System.arraycopy(msg, 1 + salt.length + iv.length, encrypted, 0, encrypted.length);

        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DECRYPT_DERIVATION);
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, DECRYPT_ITERATIONS, DECRYPT_KEYLEN);
        SecretKey secret = keyFactory.generateSecret(keySpec);
        Cipher cipher = Cipher.getInstance(DECRYPT_TRANSFORMATION);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, secret, ivSpec);
        byte[] decrypted = cipher.doFinal(encrypted);

        return new String(decrypted, StandardCharsets.UTF_8);
    }

    static boolean isProtectedContent(Uri uri) {
        return uri.toString().startsWith(DECRYPT_URL);
    }

    public static final class FragmentDialogDecrypt extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            Bundle args = getArguments();

            final Context context = getContext();
            final View view = LayoutInflater.from(context).inflate(R.layout.dialog_decrypt, null);
            final TextInputLayout tilPassword = view.findViewById(R.id.tilPassword);
            final Button btnDecrypt = view.findViewById(R.id.btnDecrypt);
            final TextView tvContent = view.findViewById(R.id.tvContent);
            final TextView tvError = view.findViewById(R.id.tvError);
            final TextView tvErrorDetail = view.findViewById(R.id.tvErrorDetail);

            tilPassword.setVisibility(View.VISIBLE);
            btnDecrypt.setVisibility(View.VISIBLE);
            tvContent.setVisibility(View.GONE);
            tvError.setVisibility(View.GONE);
            tvErrorDetail.setVisibility(View.GONE);

            String password = args.getString("password");
            tilPassword.getEditText().setText(password);
            btnDecrypt.setEnabled(!TextUtils.isEmpty(password));

            tilPassword.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Do nothing
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Do nothing
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String password = s.toString();
                    btnDecrypt.setEnabled(!TextUtils.isEmpty(password));
                    args.putString("password", password);
                }
            });

            btnDecrypt.setEnabled(false);

            tvContent.setMovementMethod(new ArrowKeyMovementMethod() {
                private GestureDetector gestureDetector = new GestureDetector(context,
                        new GestureDetector.SimpleOnGestureListener() {
                            @Override
                            public boolean onSingleTapUp(MotionEvent event) {
                                return onClick(event);
                            }

                            private boolean onClick(MotionEvent event) {
                                Spannable buffer = (Spannable) tvContent.getText();
                                int off = Helper.getOffset(tvContent, buffer, event);

                                URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);
                                if (link.length > 0) {
                                    String url = link[0].getURL();
                                    Uri uri = Uri.parse(url);

                                    int start = buffer.getSpanStart(link[0]);
                                    int end = buffer.getSpanEnd(link[0]);
                                    String title = (start < 0 || end < 0 || end <= start
                                            ? null : buffer.subSequence(start, end).toString());
                                    if (url.equals(title))
                                        title = null;

                                    Bundle args = new Bundle();
                                    args.putParcelable("uri", uri);
                                    args.putString("title", title);
                                    args.putBoolean("always_confirm", true);

                                    FragmentDialogOpenLink fragment = new FragmentDialogOpenLink();
                                    fragment.setArguments(args);
                                    fragment.show(getParentFragmentManager(), "open:link");

                                    return true;
                                }

                                return false;
                            }
                        });

                @Override
                public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });

            btnDecrypt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new SimpleTask<Spanned>() {
                        @Override
                        protected void onPreExecute(Bundle args) {
                            tilPassword.setEnabled(false);
                            btnDecrypt.setEnabled(false);
                            tvError.setVisibility(View.GONE);
                            tvErrorDetail.setVisibility(View.GONE);
                        }

                        @Override
                        protected void onPostExecute(Bundle args) {
                            tilPassword.setEnabled(true);
                            btnDecrypt.setEnabled(true);
                        }

                        @Override
                        protected Spanned onExecute(Context context, Bundle args) throws Throwable {
                            Uri uri = args.getParcelable("uri");
                            String password = args.getString("password");

                            String html = ProtectedContent.fromUri(context, uri, password);

                            Document d = JsoupEx.parse(html);
                            d = HtmlHelper.sanitizeView(context, d, true);
                            return HtmlHelper.fromDocument(context, d, new HtmlHelper.ImageGetterEx() {
                                @Override
                                public Drawable getDrawable(Element element) {
                                    return ImageHelper.decodeImage(context,
                                            -1, element, true, 0, 1.0f, tvContent);
                                }
                            }, null);
                        }

                        @Override
                        protected void onExecuted(Bundle args, Spanned content) {
                            tilPassword.setVisibility(View.GONE);
                            btnDecrypt.setVisibility(View.GONE);
                            tvContent.setText(content);
                            tvContent.setVisibility(View.VISIBLE);
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            tvError.setText(new ThrowableWrapper(ex).getSafeMessage());
                            tvErrorDetail.setText(new ThrowableWrapper(ex).toSafeString());
                            tvError.setVisibility(View.VISIBLE);
                            tvErrorDetail.setVisibility(View.VISIBLE);
                        }
                    }.execute(FragmentDialogDecrypt.this, args, "decypt");
                }
            });

            tilPassword.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        btnDecrypt.performClick();
                        return true;
                    } else
                        return false;
                }
            });

            if (!TextUtils.isEmpty(password))
                btnDecrypt.post(new Runnable() {
                    @Override
                    public void run() {
                        btnDecrypt.performClick();
                    }
                });

            Dialog dialog = new AlertDialog.Builder(context)
                    .setView(view)
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();

            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

            return dialog;
        }
    }

    public static void showDialogEncrypt(Context context, LifecycleOwner owner, EditText etBody) {
        View dview = LayoutInflater.from(context).inflate(R.layout.dialog_password_protect, null);
        TextInputLayout tilPassword1 = dview.findViewById(R.id.tilPassword1);
        TextInputLayout tilPassword2 = dview.findViewById(R.id.tilPassword2);
        Button btnInfo = dview.findViewById(R.id.btnInfo);

        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 184);
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int start = etBody.getSelectionStart();
                        int end = etBody.getSelectionEnd();
                        boolean selection = (start >= 0 && start < end);
                        if (selection) {
                            Editable edit = etBody.getText();

                            Bundle args = new Bundle();
                            args.putCharSequence("text", edit.subSequence(start, end));
                            args.putString("password", tilPassword1.getEditText().getText().toString());
                            args.putInt("start", start);
                            args.putInt("end", end);

                            new SimpleTask<Uri>() {
                                @Override
                                protected Uri onExecute(Context context, Bundle args) throws Throwable {
                                    Spanned text = (Spanned) args.getCharSequence("text");
                                    String password = args.getString("password");

                                    String html = getContent(context, text);
                                    return ProtectedContent.toUri(context, html, password);
                                }

                                @Override
                                protected void onExecuted(Bundle args, Uri uri) {
                                    if (etBody.getSelectionStart() != start ||
                                            etBody.getSelectionEnd() != end)
                                        return;

                                    String title = context.getString(R.string.title_decrypt);
                                    edit.delete(start, end);
                                    edit.insert(start, title);
                                    edit.setSpan(new URLSpan(uri.toString()), start, start + title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    etBody.setSelection(start + title.length());
                                }

                                @Override
                                protected void onException(Bundle args, Throwable ex) {
                                    Log.e(ex);
                                }
                            }.execute(context, owner, args, "protect");
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        // WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        dialog.show();

        Button btnOk = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        TextWatcher w = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String p1 = tilPassword1.getEditText().getText().toString();
                String p2 = tilPassword2.getEditText().getText().toString();
                btnOk.setEnabled(!TextUtils.isEmpty(p1) && p1.equals(p2));
                tilPassword2.setHint(!TextUtils.isEmpty(p2) && !p2.equals(p1)
                        ? R.string.title_setup_password_different
                        : R.string.title_setup_password_repeat);
            }
        };

        tilPassword1.getEditText().addTextChangedListener(w);
        tilPassword2.getEditText().addTextChangedListener(w);
        w.afterTextChanged(null);

        tilPassword2.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    btnOk.performClick();
                    return true;
                } else
                    return false;
            }
        });
    }

    static String getContent(Context context, Spanned text) {
        Drawable d = ContextCompat.getDrawable(context, R.drawable.twotone_image_24);
        d.setTint(Color.GRAY);

        Bitmap bm = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        d.setBounds(0, 0, c.getWidth(), c.getHeight());
        d.draw(c);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, bos);

        StringBuilder sb = new StringBuilder();
        sb.append("data:image/png;base64,");
        sb.append(Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP));

        String html = HtmlHelper.toHtml(text, context);
        Document doc = JsoupEx.parse(html);
        for (Element img : doc.select("img"))
            img.attr("src", sb.toString());

        return doc.body().html();
    }
}
