package fr.cesi.alternance.user; 
  
import java.util.ArrayList; 
import java.util.List; 
import fr.cesi.alternance.Constants; 
import fr.cesi.alternance.R; 
import fr.cesi.alternance.user.User;   
import android.app.ListActivity; 
import android.content.Context; 
import android.content.Intent; 
import android.os.Bundle; 
import android.util.Log;
import android.view.LayoutInflater; 
import android.view.Menu; 
import android.view.MenuItem; 
import android.view.View; 
import android.view.ViewGroup; 
import android.widget.AdapterView; 
import android.widget.ArrayAdapter; 
import android.widget.ListView; 
import android.widget.ProgressBar;
import android.widget.TextView; 
import fr.cesi.alternance.helpers.AccountHelper;
import fr.cesi.alternance.promo.Promo; 
  
public class UserListActivity extends ListActivity { 
  
    public static final String TAG = Constants.APP_NAME + ".UserListActivity"; 
    private String role; 
    private Promo promo;
    private ProgressBar loader;
  
    protected void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 

        setContentView(R.layout.activity_home);
        loader = (ProgressBar) findViewById(android.R.id.progress);

        role = getIntent().getExtras().getString("role"); 
        promo = getIntent().getExtras().getParcelable("promo"); 
        Log.v("UserListActivity", promo.toString());
		TextView name = (TextView) findViewById(R.id.name);
		name.setText(getIntent().getExtras().getString("users_list_name"));

        ListView lv = getListView(); 
          
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() { 
            public void onItemClick(AdapterView<?> list, View view, int position, long id) { 
                User user = (User) getListAdapter().getItem(position); 

                Intent intent = new Intent(UserListActivity.this, UserActivity.class); 
                intent.putExtra("user", user); 
                intent.putExtra("id_promo", promo.getId()); 
                startActivity(intent); 
            } 
        }); 
    } 
    
    @Override
    protected void onResume() {
    	loadUser();
    	super.onResume();
    }

    private void loadUser() {
    	
    	getListView().setVisibility(View.GONE);
    	loader.setVisibility(View.VISIBLE);
    	
    	new Thread(new Runnable() { 
            
            @Override
            public void run() { 
                ArrayList<User> user_list = promo.getListUser(role); 
                  
                final UsersAdapter adapter = new UsersAdapter(UserListActivity.this, android.R.layout.simple_list_item_1, user_list); 
                runOnUiThread(new Runnable() { 
                      
                    @Override
                    public void run() { 
                        setListAdapter(adapter);
                    	getListView().setVisibility(View.VISIBLE);
                    	loader.setVisibility(View.GONE);
                    } 
                }); 
            } 
        }).start(); 
	}

	public boolean onCreateOptionsMenu (Menu menu) {
    	if(!"IF".equals(AccountHelper.getRole()))
    		return false;
        getMenuInflater().inflate(R.menu.add_action, menu);
        return true; 
    } 

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.add_doc_action).setVisible(false);
		menu.findItem(R.id.view_doc_action).setVisible(false);
		return super.onPrepareOptionsMenu(menu);
	}

    public boolean onOptionsItemSelected(MenuItem item) { 
        switch (item.getItemId()) { 
        case R.id.add_list_action:
        	
            User testUser = new User(); 
            testUser.setName("");
            testUser.setMail("");
            testUser.setPhone("");
            testUser.setRole(role);

            Intent intent = new Intent(UserListActivity.this, UserActivity.class); 
            intent.putExtra("user", testUser); 
            intent.putExtra("id_promo", promo.getId()); 
            startActivity(intent); 
            return true; 
        default: 
            return super.onOptionsItemSelected(item); 
        } 
    } 
  
    public class UsersAdapter extends ArrayAdapter<User> { 
          
        private Context mContexte; 
        private int mResource; 
        private List<User> mItems; 
      
        public UsersAdapter(Context context, int resource, List<User> objects) { 
            super(context, resource, objects); 
            mContexte = context; 
            mResource = resource; 
            mItems = objects; 
        } 
  
        @Override
        public View getView(int position, View view, ViewGroup parent) { 
            if(view == null) { 
                view = LayoutInflater.from(mContexte).inflate(mResource, parent, false); 
            } 
              
            User user = mItems.get(position); 
            TextView tv = (TextView) view.findViewById(android.R.id.text1); 
            tv.setText(user.getName()); 
            return view; 
        } 
      
    } 
      
} 