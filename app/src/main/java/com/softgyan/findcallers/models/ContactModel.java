package com.softgyan.findcallers.models;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ContactModel implements Serializable {
    private static final String TAG = "ContactModel";
    private int id;
    private String name;
    private String emailId;
    private String address;
    private String image;
    private String tag;
    private String defaultNumber;
    private final List<ContactNumberModel> contactNumbers;

    public ContactModel(String name) {
        this.name = name;
        id = -1;
        emailId = null;
        address = null;
        image = null;
        tag = null;
        defaultNumber = null;

        contactNumbers = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void updateContactNumber(Collection<ContactNumberModel> contactNumberList){
        contactNumbers.clear();
        contactNumbers.addAll(contactNumberList);
    }
    public String getDefaultNumber() {
        return defaultNumber;
    }

    public void setDefaultNumber(String defaultNumber) {
        this.defaultNumber = defaultNumber;
    }

    public List<ContactNumberModel> getContactNumbers() {
        return contactNumbers;
    }

    public void setContactNumbers(ContactNumberModel contactNumber) {
        boolean flag = false;
        for (ContactNumberModel model : contactNumbers) {
            Log.d(TAG, "setMobileInfoModels: " + model.getMobileNumber() + " " + contactNumber.getMobileNumber());
            if (!model.getMobileNumber().equals(contactNumber.getMobileNumber())) {
                this.contactNumbers.add(contactNumber);
                flag = true;
                Log.d("my_tag", "setMobileInfoModels: allReadyExits");
                break;
            }
        }
        if (!flag) {
            this.contactNumbers.add(contactNumber);
            Log.d(TAG, "setMobileInfoModels: mobile number added");
        }
    }

    @Override
    public String toString() {
        return "ContactModel{" +
                "\nid=" + id +
                "\nname='" + name + '\'' +
                "\nemailId='" + emailId + '\'' +
                "\naddress='" + address + '\'' +
                "\nimage='" + image + '\'' +
                "\ntag='" + tag + '\'' +
                "\ndefaultNumber='" + defaultNumber + '\'' +
                "\ncontactNumbers=" + contactNumbers +
                "\n}";
    }
}
