package com.example.teamhonk.honk.models;

import java.util.ArrayList;

/**
 * Created by Gughappriya Gnanasekar on 11/27/2015.
 */
public class ContactList
{

    private ArrayList<Contact> contacts = new ArrayList<Contact>();

    public int getCount()
    {
        return this.contacts.size();
    }
    public void addContact(Contact c)
    {
        this.contacts.add(c);
    }
    public void removeContact(Contact c)
    {
        this.contacts.remove(c);
    }
    public void removeContact(int id)
    {
        if(this!=null) {
            for (int i = 0; i < this.getCount(); i++) {
                if (id == Integer.parseInt(this.contacts.get(i).id)) {
                    this.contacts.remove(this.contacts.get(i));
                }
            }
        }
    }
    public Contact getContact(int id)
    {
        Contact tmp=null;
        if(this!=null) {
            for (int i = 0; i < this.getCount(); i++) {
                if (id == Integer.parseInt(this.contacts.get(i).id)) {
                    tmp = new Contact(this.contacts.get(i).id, this.contacts.get(i).name, this.contacts.get(i).phone, this.contacts.get(i).label, this.contacts.get(i).email);
                }
            }
        }
        return tmp;
    }
    public ArrayList<Contact> getContacts()
    {
        return contacts;
    }
    public void setContacts(ArrayList<Contact> c)
    {
        this.contacts=c;
    }

}