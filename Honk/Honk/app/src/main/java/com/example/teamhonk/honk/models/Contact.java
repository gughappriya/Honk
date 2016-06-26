package com.example.teamhonk.honk.models;
public class Contact
{

    public String id,name,phone,label,email;

   public Contact(String id, String name,String phone,String label,String email)
    {
        this.id=id;
        this.name=name;
        this.phone=phone;
        this.label=label;
        this.email = email;
    }
}