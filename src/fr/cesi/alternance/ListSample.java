package fr.cesi.alternance;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ListSample extends ListActivity {
	
	private static final String TAG = Constants.APP_NAME + ".ListSample";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ArrayList<String> data = new ArrayList<String>();
		for (int i=0; i<100; i++) data.add("Value " + (i+1));
		
		MyAdapter adapter = new MyAdapter(this, android.R.layout.simple_list_item_multiple_choice, data);
		setListAdapter(adapter);
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		//getListView().setOnItemClickListener(mClickListener);
	}
	
	/*private AdapterView.OnItemClickListener mClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> list, View view, int position, long id) {
			SparseBooleanArray checked = getListView().getCheckedItemPositions();
			Log.v(TAG, "onItemClick: " + position + " > " + checked.get(position));
		}
	};*/
	
	private class MyAdapter extends ArrayAdapter<String> {
		
		private final Context mContext;
		private final int mResource;
		private final ArrayList<String> mItems;
		
		public MyAdapter(Context ctx, int res, ArrayList<String> items) {
			super(ctx, res, items);
			mContext = ctx;
			mResource = res;
			mItems = items;
		}
		
		@Override
		public View getView(int position, View view, ViewGroup parent) {
			if (view == null) view = LayoutInflater.from(mContext).inflate(mResource, parent, false);
			TextView tv = (TextView) view.findViewById(android.R.id.text1);
			tv.setText(mItems.get(position));
			return view;
		}
	}
	
}
