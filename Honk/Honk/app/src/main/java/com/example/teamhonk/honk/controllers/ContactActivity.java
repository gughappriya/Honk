package com.example.teamhonk.honk.controllers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;

import com.example.teamhonk.honk.R;

import com.example.teamhonk.honk.models.Contact;
import com.example.teamhonk.honk.models.ContactList;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ContactActivity extends Activity {

    ContactListAdapter cladapter;
    ListView list_Contact;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        list_Contact = (ListView) findViewById(R.id.lst_contactList);

        cladapter = new ContactListAdapter(this,new ContactList());
        list_Contact.setAdapter(cladapter);

        try
        {
//Running AsyncLoader with adapter and blank filter
            new AsyncContactLoader(cladapter).execute("%");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        EditText srchBox = (EditText) findViewById(R.id.txt_searchContact);

        //Adding text change listener for filtering contacts
        srchBox.addTextChangedListener(new TextWatcher(){

        @Override
        public void afterTextChanged(Editable s) {
// TODO Auto-generated method stub

    }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
// TODO Auto-generated method stub

    }
        @Override
        public void onTextChanged(CharSequence s, int start, int before,int count)
        {
            String filter=s.toString().trim()+"%";


//Running AsyncLoader with adapter and search text as parameters

            try
            {
                new AsyncContactLoader(cladapter).execute(filter);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

    });

    //Code to return selected contacts...
    Button btnDone = (Button) findViewById(R.id.btnDone);

    btnDone.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {



            Intent intent = new Intent();
            if(cladapter.selectedContacts.getCount()>0)
            {

                String[][] sel_cons = new String[cladapter.selectedContacts.getCount()][5];
                for(int i=0;i<cladapter.selectedContacts.getCount();i++)
                {
                    sel_cons[i][0] = cladapter.selectedContacts.getContacts().get(i).id;
                    sel_cons[i][1] = cladapter.selectedContacts.getContacts().get(i).name;
                    sel_cons[i][2] = cladapter.selectedContacts.getContacts().get(i).phone;
                    sel_cons[i][3] = cladapter.selectedContacts.getContacts().get(i).label;
                    sel_cons[i][4] = cladapter.selectedContacts.getContacts().get(i).email;
                }



                //Bundling up the contacts to pass
                Bundle data_to_pass = new Bundle();

                data_to_pass.putSerializable("selectedContacts", sel_cons);

                intent.putExtras(data_to_pass);
                setResult(RESULT_OK,intent);
                Log.v("Result", "ok");
            }
            else
            {
//If user presses back button without selecting any contact
                Log.v("Result", "cancelled");
                setResult(RESULT_CANCELED,intent);
            }
//Ending Activity and passing result

            finish();

        }
    });

}

class ContactListAdapter extends BaseAdapter
{
    Context context;
    ContactList gcl;
    ContactList selectedContacts;

    public ContactListAdapter(Context context,ContactList gcl)
    {
        super();
        this.context = context;
        this.gcl=gcl;
        selectedContacts = new ContactList();

    }
    /*Custom View Generation(You may modify this to include other Views) */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view_row = inflater.inflate(R.layout.activity_contact_picker, parent,false);
        CheckBox chk_contact = (CheckBox) view_row.findViewById(R.id.chkbxContact);
        chk_contact.setId(Integer.parseInt(gcl.getContacts().get(position).id));
        //Text to display near checkbox [Here, Contact_Name (Number Label : Phone Number)]
        chk_contact.setText(gcl.getContacts().get(position).name.toString() + " ( "+gcl.getContacts().get(position).label+" : " + gcl.getContacts().get(position).phone.toString() + ")"+ " ( Email : " + gcl.getContacts().get(position).email.toString() + ")");

        if(alreadySelected(gcl.getContacts().get(position)))
        {
            chk_contact.setChecked(true);
        }

        //Code to get Selected Contacts.
        chk_contact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {

                Contact t = gcl.getContact(arg0.getId());
                if(t!=null && arg1)
                {
                    if(!alreadySelected(t))
                        selectedContacts.addContact(t);
                }
                else if(!arg1 && t!=null)
                {
                    selectedContacts.removeContact(arg0.getId());
                }


            }

        });

        return view_row;
    }
    public boolean alreadySelected(Contact t)
    {
        boolean ret = false;

        if(selectedContacts.getContact(Integer.parseInt(t.id))!=null)
            ret=true;

        return ret;
    }
    @Override
    public int getCount() {

        return gcl.getCount();
    }

    @Override
    public Contact getItem(int arg0) {
        // TODO Auto-generated method stub
        return gcl.getContacts().get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return Long.parseLong(gcl.getContacts().get(arg0).id);
    }
}



class AsyncContactLoader extends AsyncTask<String,ContactList,ContactList>
{
    ContactListAdapter contactsAdapter;
    ProgressDialog progressDialog;
    AsyncContactLoader(ContactListAdapter adap)
    {
        //initiating AsyncLoader with the ListView Adapter

        contactsAdapter = adap;
    }

    protected void onPreExecute()
    {


        progressDialog = ProgressDialog.show(ContactActivity.this, "Please Wait", "Loading Contacts",true);
    }


//Loading Contacts

    @Override
    protected ContactList doInBackground(String... filters )
    {
        ContactList glst=null;

//Filter = text in search textbox

        String filter = filters[0];
        ContentResolver cr = getContentResolver();
        int count=0;

//Code to fetch contacts...

        Uri uri = ContactsContract.Contacts.CONTENT_URI;

//Fields to select from database
        String[] projection = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER

        };

/*Querying database (Select fields in projection from database where contact name like 'filter%', sort by name, in ascending order)*/
        Cursor cursor = cr.query(uri, projection,  ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?", new  String[] {filter.toString()},  ContactsContract.Contacts.DISPLAY_NAME+ " ASC");

        //Log.v("", "Contacts : "+cursor.getCount());


        if(cursor.getCount()>0)
        {

            glst=new ContactList();

            while(cursor.moveToNext())
            {

                //Filtering Contacts with Phone Numbers

                if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)))>0)
                {

                    String id =  cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    //Phone numbers lies in a separate table. Querying that table with Contact ID

                    Cursor contactCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +"=?", new String[] {id}, null);
                    while(contactCursor.moveToNext())
                    {

                        String phId = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
                        String customLabel = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));
                        String label = (String)ContactsContract.CommonDataKinds.Phone.getTypeLabel(getResources(),contactCursor.getInt(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)),customLabel);
                        String ph_no = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Cursor emailCursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", new String[]{id}, null);
                        while(emailCursor.moveToNext()) {
                            String email = emailCursor.getString(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                            Contact tmp = new Contact(phId, name, ph_no, label, email);
                            glst.addContact(tmp);
                            count++;
                            if (count == 100) {
                                publishProgress(glst);
                                count = 0;
                            }
                        }
                        emailCursor.close();

                    }
                    contactCursor.close();
                }


            }
            cursor.close();

        }


        return glst;
    }

//Code to refresh list view

    @Override
    protected void onProgressUpdate(ContactList... glsts )
    {
        if(progressDialog.isShowing())
            progressDialog.dismiss();
        contactsAdapter.gcl = glsts[0];
        contactsAdapter.notifyDataSetChanged();

    }

    @Override
//Loading contacts finished, refresh list view to load any missed out contacts

    protected void onPostExecute(ContactList result)
    {
        if(progressDialog.isShowing())
            progressDialog.dismiss();
        contactsAdapter.gcl=result;
        contactsAdapter.notifyDataSetChanged();

    }


}
}