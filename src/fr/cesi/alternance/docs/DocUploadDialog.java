package fr.cesi.alternance.docs;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.AuthenticatorException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kolapsis.utils.HttpData;
import com.kolapsis.utils.HttpData.HttpDataException;

import fr.cesi.alternance.Constants;
import fr.cesi.alternance.R;
import fr.cesi.alternance.api.Api;
import fr.cesi.alternance.helpers.AccountHelper;

@SuppressLint("NewApi")
public class DocUploadDialog extends DialogFragment {
	private static final int FILE_SELECT_CODE = 56;
	public static final String TAG = "DocUploadDialog";

	public static DocUploadDialog newInstance(Bundle args, UploadListener listener) {
		DocUploadDialog instance = new DocUploadDialog();
		instance.setArguments(args);
		instance.mListener = listener;
		return instance;
	}

	public static interface UploadListener{
		public void onUpload(Doc newDoc);

	}

	private EditText mTitle,mDesc,mFile;
	private Button mBrowse;
	private String mPath;
	private long mEstablishment, mTraining, mPromo, mUser;
	private UploadListener mListener;
	public DocUploadDialog(){

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		mEstablishment = args.getLong("id_establishment", 1);
		mTraining = args.getLong("id_training", 0);
		mPromo = args.getLong("id_promo", 0);
		mUser = args.getLong("id_user", 0);
		Log.v(TAG, "Id après envoi :" + mUser);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View view = getActivity().getLayoutInflater().inflate(R.layout.doc_upload, null,false);
		mTitle = (EditText) view.findViewById(R.id.title);
		mDesc = (EditText) view.findViewById(R.id.description);
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
				new UploadDoc().execute();
			}
		})
		.create();
	}

	public String getPath(Uri uri)
			throws URISyntaxException {
		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
		
		// DocumentProvider
	    if (isKitKat && DocumentsContract.isDocumentUri(getActivity(), uri)) {
	        // ExternalStorageProvider
	        if (isExternalStorageDocument(uri)) {
	            final String docId = DocumentsContract.getDocumentId(uri);
	            final String[] split = docId.split(":");
	            final String type = split[0];

	            if ("primary".equalsIgnoreCase(type)) {
	                return Environment.getExternalStorageDirectory() + "/" + split[1];
	            }

	            // TODO handle non-primary volumes
	        }
	        // DownloadsProvider
	        else if (isDownloadsDocument(uri)) {

	            final String id = DocumentsContract.getDocumentId(uri);
	            final Uri contentUri = ContentUris.withAppendedId(
	                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

	            return getDataColumn(getActivity(), contentUri, null, null);
	        }
	        // MediaProvider
	        else if (isMediaDocument(uri)) {
	            final String docId = DocumentsContract.getDocumentId(uri);
	            final String[] split = docId.split(":");
	            final String type = split[0];

	            Uri contentUri = null;
	            if ("image".equals(type)) {
	                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	            } else if ("video".equals(type)) {
	                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	            } else if ("audio".equals(type)) {
	                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	            }

	            final String selection = "_id=?";
	            final String[] selectionArgs = new String[] {
	                    split[1]
	            };

	            return getDataColumn(getActivity(), contentUri, selection, selectionArgs);
	        }
	    }
	    // MediaStore (and general)
	    else if ("content".equalsIgnoreCase(uri.getScheme())) {

	        // Return the remote address
	        if (isGooglePhotosUri(uri))
	            return uri.getLastPathSegment();

	        return getDataColumn(getActivity(), uri, null, null);
	    }
	    // File
	    else if ("file".equalsIgnoreCase(uri.getScheme())) {
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
					String path = getPath(uri);
					onSelectFile(path);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void onSelectFile(String path) {
		if(path != null){
			int indexMime = path.lastIndexOf(".");
			String nameMimeUp = path.substring(indexMime+1);
			String nameMimeDown = nameMimeUp.toLowerCase();
			path = path.replace(nameMimeUp, nameMimeDown);
			int index = path.lastIndexOf("/");
			String name = path.substring(index+1);
			mPath = path;
			mFile.setText(name);
			if(TextUtils.isEmpty(mTitle.getText().toString())){
				int i = name.lastIndexOf(".");
				if (i>-1) name = name.substring(0,i);
				mTitle.setText(name);
			}
		}
	}
	
	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @param selection (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection,
	        String[] selectionArgs) {

	    Cursor cursor = null;
	    final String column = "_data";
	    final String[] projection = {
	            column
	    };

	    try {
	        cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
	                null);
	        if (cursor != null && cursor.moveToFirst()) {
	            final int index = cursor.getColumnIndexOrThrow(column);
	            return cursor.getString(index);
	        }
	    } finally {
	        if (cursor != null)
	            cursor.close();
	    }
	    return null;
	}
	
	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
	    return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
	    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
	    return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
	    return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}

	private class UploadDoc extends AsyncTask<Void, Void, Doc> {

		private ProgressDialog progress;

		public UploadDoc() {

		}

		@Override
		protected void onPreExecute() {
			progress = ProgressDialog.show(
					getActivity(),
					getString(R.string.progress_envoi),
					getString(R.string.progress_envoi_infos));
		}

		@Override
		protected Doc doInBackground(Void... params) {
			boolean success = false;
			File file = new File(mPath);
			Doc newDoc = new Doc();
			try {
				final String token = AccountHelper.blockingGetAuthToken(
						AccountHelper.getAccount(), Constants.ACCOUNT_TOKEN_TYPE, true);
				final String url = Constants.BASE_API_URL + "/document/upload";
				HttpData p = new HttpData(url).header(Api.APP_AUTH_TOKEN, token).file("file", file)
						.data("titre", mTitle.getText().toString())
						.data("description", mDesc.getText().toString())
						.data("id_establishment", String.valueOf(mEstablishment))
						.data("id_training", String.valueOf(mTraining))
						.data("id_promo", String.valueOf(mPromo))
						.data("id_user", String.valueOf(mUser))
						.post();
				Log.v(TAG, p.asString());
				JSONObject json =  p.asJSONObject();
				success = json.has("success") && json.getBoolean("success");
				Log.v(TAG, "success : " + success);
				if (success) {
					newDoc.setName(mTitle.getText().toString());
					newDoc.setDescription(mDesc.getText().toString());
					newDoc.setId(json.getJSONObject("result").getLong("id"));
					newDoc.setPath(json.getJSONObject("result").getString("path_doc"));
				}
			} catch (AuthenticatorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (HttpDataException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return success ? newDoc : null;
		}

		@Override
		protected void onPostExecute(Doc result) {
			progress.dismiss();
			if(mListener != null){
				mListener.onUpload(result);
			}
		}

	}


}
