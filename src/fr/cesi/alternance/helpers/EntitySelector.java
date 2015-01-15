package fr.cesi.alternance.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import fr.cesi.alternance.R;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

public class EntitySelector extends DialogFragment {
	public static final String TAG = "UserSelectDialog";
	private SelectUserListener mListener;
	private ArrayList<Entity> mList;
	private EditText search;
	private ListView list;
	private ListAdapter adapter;
	
	public static EntitySelector newInstance(Bundle args, SelectUserListener mListener) {
		EntitySelector instance = new EntitySelector();
		instance.setArguments(args);
		instance.mListener = mListener;
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		mList = (ArrayList<Entity>) args.get("list");
		adapter = new ListAdapter(getActivity(), mList);
	}
	
	@SuppressLint("InflateParams")
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.select_entity, null, false);
		search = (EditText) view.findViewById(R.id.search);
		search.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			@Override
			public void afterTextChanged(Editable edit) {
				adapter.getFilter().filter(edit.toString());
			}
		});
		list = (ListView) view.findViewById(android.R.id.list);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapt, View list, int position, long id) {
				if (mListener != null) mListener.onSelect(adapter.getItem(position));
				dismiss();
			}
		});
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Sélectionnez");
		builder.setView(view);
		return builder.create();
	}
	
	public static interface SelectUserListener{
		public void onSelect(Entity selected);
	}
	
	public class ListAdapter extends BaseAdapter implements Filterable {

	    private List<Entity>originalData = null;
	    private List<Entity>filteredData = null;
	    private LayoutInflater mInflater;
	    private ItemFilter mFilter = new ItemFilter();

	    public ListAdapter(Context context, List<Entity> data) {
	        this.filteredData = data ;
	        this.originalData = data ;
	        mInflater = LayoutInflater.from(context);
	    }

	    public int getCount() {
	        return filteredData.size();
	    }

	    public Entity getItem(int position) {
	        return filteredData.get(position);
	    }

	    public long getItemId(int position) {
	        return position;
	    }

	    @SuppressLint({ "ViewHolder", "InflateParams" })
		public View getView(int position, View convertView, ViewGroup parent) {
	    	convertView = mInflater.inflate(android.R.layout.simple_list_item_1, null, false);
	    	TextView text = (TextView) convertView.findViewById(android.R.id.text1);
	    	text.setText(filteredData.get(position).getName());
	        return convertView;
	    }
	    
	    public Filter getFilter() {
	        return mFilter;
	    }

	    private class ItemFilter extends Filter {
	        @Override
	        protected FilterResults performFiltering(CharSequence constraint) {
	            String filterString = constraint.toString().toLowerCase(Locale.FRANCE);
	            FilterResults results = new FilterResults();
	            final List<Entity> list = originalData;
	            int count = list.size();
	            final ArrayList<Entity> nlist = new ArrayList<Entity>(count);
	            String filterableString ;
	            for (int i = 0; i < count; i++) {
	                filterableString = list.get(i).getName();
	                if (filterableString.toLowerCase(Locale.FRANCE).contains(filterString)) {
	                    nlist.add(list.get(i));
	                }
	            }
	            results.values = nlist;
	            results.count = nlist.size();
	            return results;
	        }

	        @SuppressWarnings("unchecked")
	        @Override
	        protected void publishResults(CharSequence constraint, FilterResults results) {
	            filteredData = (ArrayList<Entity>) results.values;
	            notifyDataSetChanged();
	        }

	    }
	}
	
}
