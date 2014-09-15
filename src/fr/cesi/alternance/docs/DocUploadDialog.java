package fr.cesi.alternance.docs;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import com.kolapsis.utils.HttpData;
import com.kolapsis.utils.HttpData.HttpDataException;

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
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
	private long mEstablishment, mTraining, mPromo;
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

	public String getPath(Context context, Uri uri)
			throws URISyntaxException {
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			String[] projection = { "_data" };
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
			Log.v(TAG, "path"+path);
			int index = path.lastIndexOf("/");
			String name = path.substring(index+1);
			mPath = path;
			Log.v(TAG, "path mine down"+mPath);
			mFile.setText(name);
			if(TextUtils.isEmpty(mTitle.getText().toString())){
				int i = name.lastIndexOf(".");
				if (i>-1) name = name.substring(0,i);
				mTitle.setText(name);
			}
		}
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
			File file = new File(mPath);
			Doc newDoc = new Doc();
			Log.v(TAG, file.getPath());
			try {
				final String token = AccountHelper.blockingGetAuthToken(
						AccountHelper.getAccount(), Constants.ACCOUNT_TOKEN_TYPE, true);
				final String url = Constants.BASE_API_URL + "/document/upload";
				Log.v(TAG, "establishment : "+mEstablishment);
				HttpData p = new HttpData(url).header(Api.APP_AUTH_TOKEN, token).file("file", file)
						.data("path", "document/")
						.data("titre", mTitle.getText().toString())
						.data("description", mDesc.getText().toString())
						.data("id_establishment", String.valueOf(mEstablishment))
						.data("id_training", String.valueOf(mTraining))
						.data("id_promo", String.valueOf(mPromo))
						.post();
				JSONObject json =  p.asJSONObject();
				if(json.getBoolean("success")){
					
					newDoc.setName(mTitle.getText().toString());
					newDoc.setDescription(mDesc.getText().toString());
					newDoc.setId(json.getJSONObject("result").getLong("id"));
					newDoc.setPath(json.getJSONObject("result").getString("path_doc"));
				}
				Log.v(TAG, p.asString());
			} catch (AuthenticatorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (HttpDataException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return newDoc;
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
