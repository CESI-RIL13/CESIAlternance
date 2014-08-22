
package fr.cesi.alternance.user;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import com.kolapsis.utils.HttpData;
import com.kolapsis.utils.HttpData.HttpDataException;
import fr.cesi.alternance.user.User;
import fr.cesi.alternance.Constants;
import fr.cesi.alternance.R;
import fr.cesi.alternance.api.Api;
import fr.cesi.alternance.helpers.AccountHelper;
import android.accounts.AuthenticatorException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class PhotoUserDialog extends DialogFragment {
    public static interface UploadListener{
        public void onUpload(String path);
    }
    private static final int FILE_SELECT_CODE = 56;
    private EditText mFile;
    private Button mBrowse;
    private String mPath;
    private User mUser;
    private UploadListener mListener;

    public PhotoUserDialog(User user, UploadListener listener) {
        mUser = user;
        mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.photo_upload, null, false);
        mFile = (EditText) view.findViewById(R.id.file);
        mBrowse = (Button) view.findViewById(R.id.browse);
        mBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });
        return new AlertDialog.Builder(getActivity())
                .setTitle("Upload")
                .setView(view)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        new UploadPhoto().execute();
                    }
                })
                .create();
    }

    public String getPath(Context context, Uri uri)
            throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection,
                        null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent,
                    "Selectionner un fichier a rajouter"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(getActivity(), "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    try {
                        String path = getPath(getActivity(), uri);
                        onSelectFile(path);
                        //new UploadDoc(path).execute();
                    } catch (URISyntaxException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onSelectFile(String path) {
        mPath = path;
        int index = path.lastIndexOf("/");
        String name = path.substring(index + 1);
        mFile.setText(name);
    }

    private class UploadPhoto extends AsyncTask<Void, Void, String> {

        private static final String TAG = "UploadPhoto";
		private ProgressDialog progress;

        public UploadPhoto() {

        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(
                    getActivity(),
                    getString(R.string.progress_envoi),
                    getString(R.string.progress_envoi_infos));
        }

        @Override
        protected String doInBackground(Void... params) {
            String picture = null;
            Log.v(TAG, mPath);
            File file = new File(mPath);
            Log.v(TAG, "existe "+file.exists());
            try {
                final String token = AccountHelper.blockingGetAuthToken(
                        AccountHelper.getAccount(), Constants.ACCOUNT_TOKEN_TYPE, true);
                final String url = Constants.BASE_API_URL + "/user/picture";
                Log.v(TAG, url);
                HttpData p = new HttpData(url).header(Api.APP_AUTH_TOKEN, token).file("file", file)
                        .data("path", "picture/")
                        .data("user_id", String.valueOf(mUser.getId()))
                        .post();
                Log.v(TAG,p.asString());
                JSONObject json = p.asJSONObject();
                if(json.getBoolean("success")){
                    JSONObject result = json.getJSONObject("result");
                    picture = result.getString("picture_path");
                }

            } catch (AuthenticatorException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (HttpDataException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return picture;
        }

        @Override
        protected void onPostExecute(String result) {
            progress.dismiss();
            if(mListener != null) mListener.onUpload(result);
        }
    }
}