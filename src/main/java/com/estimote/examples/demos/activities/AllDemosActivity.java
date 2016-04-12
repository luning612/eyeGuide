package com.estimote.examples.demos.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.estimote.examples.demos.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Shows all available demos.
 *
 * @author wiktor@estimote.com (Wiktor Gworek)
 */
public class AllDemosActivity extends Activity implements View.OnClickListener {
    private LinkedHashMap<String, HeaderInfo> myCategoriesList = new LinkedHashMap<String, HeaderInfo>();
    private ArrayList<HeaderInfo> categoryList = new ArrayList<HeaderInfo>();

    private MyListAdapter listAdapter;
    private ExpandableListView myList;

  @Override protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

//      Spinner spinner = (Spinner) findViewById(R.id.category);
//      // Create an ArrayAdapter using the string array and a default spinner layout
//      ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
//              R.array.dept_array, android.R.layout.simple_spinner_item);
//      // Specify the layout to use when the list of choices appears
//      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//      // Apply the adapter to the spinner
//      spinner.setAdapter(adapter);

      //Just add some data to start with
      loadData();

      //get reference to the ExpandableListView
      myList = (ExpandableListView) findViewById(R.id.locationList);
      //create the adapter by passing your ArrayList data
      listAdapter = new MyListAdapter(AllDemosActivity.this, categoryList);
      //attach the adapter to the list
      myList.setAdapter(listAdapter);

      //expand all Groups
      expandAll();
//
//      //add new item to the List
//      Button add = (Button) findViewById(R.id.add);
//      add.setOnClickListener(this);

      //listener for child row click
      myList.setOnChildClickListener(myListItemClicked);
      //listener for group heading click
      myList.setOnGroupClickListener(myListGroupClicked);
  }
    public void onClick(View v) {

        switch (v.getId()) {

//            //add entry to the List
//            case R.id.add:

//                Spinner spinner = (Spinner) findViewById(R.id.department);
//                String department = spinner.getSelectedItem().toString();
//                EditText editText = (EditText) findViewById(R.id.product);
//                String product = editText.getText().toString();
//                editText.setText("");

                //add a new item to the list
//                int groupPosition = addProduct(department,product);
                //notify the list so that changes can take effect
//                listAdapter.notifyDataSetChanged();
//
//                //collapse all groups
//                collapseAll();
//                //expand the group where item was just added
//                myList.expandGroup(groupPosition);
//                //set the current group to be selected so that it becomes visible
//                myList.setSelectedGroup(groupPosition);
//
//                break;

            // More buttons go here (if any) ...

        }
    }


    //method to expand all groups
    private void expandAll() {
        int count = listAdapter.getGroupCount();
        for (int i = 0; i < count; i++){
            myList.expandGroup(i);
        }
    }

    //method to collapse all groups
    private void collapseAll() {
        int count = listAdapter.getGroupCount();
        for (int i = 0; i < count; i++){
            myList.collapseGroup(i);
        }
    }

    //load some initial data into out list
    private void loadData(){
        addLocation("Dining","Din Tai Fung");
        addLocation("Dining","Crystal Jade");
        addLocation("Dining","Hai Di Lao");
        addLocation("Apparel","Topshop");
        addLocation("Apparel","Forever21");
    }

    //our child listener
    private ExpandableListView.OnChildClickListener myListItemClicked =  new ExpandableListView.OnChildClickListener() {

        public boolean onChildClick(ExpandableListView parent, View v,
                                    int groupPosition, int childPosition, long id) {

            //get the group header
            HeaderInfo headerInfo = categoryList.get(groupPosition);
            //get the child info
            DetailInfo detailInfo =  headerInfo.getLocationList().get(childPosition);
            //display it or do something with it
            Toast.makeText(getBaseContext(), "Clicked on Detail " + headerInfo.getCategoryName()
                    + "/" + detailInfo.getName(), Toast.LENGTH_LONG).show();
            return false;
        }

    };

    //our group listener
    private ExpandableListView.OnGroupClickListener myListGroupClicked =  new ExpandableListView.OnGroupClickListener() {

        public boolean onGroupClick(ExpandableListView parent, View v,
                                    int groupPosition, long id) {

            //get the group header
            HeaderInfo headerInfo = categoryList.get(groupPosition);
            //display it or do something with it
            Toast.makeText(getBaseContext(), "Child on Header " + headerInfo.getCategoryName(),
                    Toast.LENGTH_LONG).show();

            return false;
        }

    };
    //here we maintain our products in various departments
    private int addLocation(String categoryName, String locationName) {

        int groupPosition = 0;

        //check the hash map if the group already exists
        HeaderInfo headerInfo = myCategoriesList.get(categoryName);
        //add the group if doesn't exists
        if (headerInfo == null) {
            headerInfo = new HeaderInfo();
            headerInfo.setCategoryName(categoryName);
            myCategoriesList.put(categoryName, headerInfo);
            categoryList.add(headerInfo);
        }

        //get the children for the group
        ArrayList<DetailInfo> locationList = headerInfo.getLocationList();
        //size of the children list
        int listSize = locationList.size();
        //add to the counter
        listSize++;

        //create a new child and add that to the group
        DetailInfo detailInfo = new DetailInfo();
        detailInfo.setSequence(String.valueOf(listSize));
        detailInfo.setName(locationName);
        locationList.add(detailInfo);
        headerInfo.setLocationList(locationList);

        //find the group position inside the list
        groupPosition = categoryList.indexOf(headerInfo);
        return groupPosition;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scan_menu, menu);
        return true;
    }

      // Old Codes
//    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//    toolbar.setTitle(getTitle());
//
//    findViewById(R.id.distance_demo_button).setOnClickListener(new View.OnClickListener() {
//      @Override public void onClick(View v) {
//        startListBeaconsActivity(LocationActivity.class.getName());
//      }
//    });
/*
    findViewById(R.id.notify_demo_button).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        startListBeaconsActivity(NotifyDemoActivity.class.getName());
      }
    });

    findViewById(R.id.characteristics_demo_button).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        startListBeaconsActivity(CharacteristicsDemoActivity.class.getName());
      }
    });

    findViewById(R.id.update_demo_button).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        startListBeaconsActivity(UpdateDemoActivity.class.getName());
      }
    });

    findViewById(R.id.eddystone_demo_button).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Intent intent = new Intent(AllDemosActivity.this, ListEddystoneActivity.class);
        intent.putExtra(ListEddystoneActivity.EXTRAS_TARGET_ACTIVITY, EddystoneDemoActivity.class.getName());
        startActivity(intent);
      }
    });

    findViewById(R.id.nearables_demo_button).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Intent intent = new Intent(AllDemosActivity.this, ListNearablesActivity.class);
        intent.putExtra(ListNearablesActivity.EXTRAS_TARGET_ACTIVITY, NearablesDemoActivity.class.getName());
        startActivity(intent);
      }
    });

    findViewById(R.id.sensors_demo_button).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        startListBeaconsActivity(SensorsActivity.class.getName());
      }
    });
    */
  //}

  private void startListBeaconsActivity(String extra) {
    Intent intent = new Intent(AllDemosActivity.this, LocationActivity.class);
    //intent.putExtra(LocationActivity.EXTRAS_TARGET_ACTIVITY, extra);
      Toast.makeText(getApplicationContext(),extra, Toast.LENGTH_LONG).show();
    startActivity(intent);
  }
}
