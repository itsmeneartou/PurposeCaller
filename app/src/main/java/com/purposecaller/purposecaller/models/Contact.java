package com.purposecaller.purposecaller.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;

@Keep
public class Contact implements Parcelable{
public String name, phoneNumber,uid,photoUrl;



    public static int CONTACT_TYPE_UNKNOWN =1;
    public static int CONTACT_TYPE_INVITE_FRIEND=2;
    public static int CONTACT_TYPE_NORMAL=3;
    public static int CONTACT_TYPE_NEW_CONTACT=4;
    public static int CONTACT_TYPE_LIVE_ROOM=5;
    public Integer contactType;

    public Contact() {

    }

    public Contact(String name, String phoneNumber) {
        this.name = name;

        this.phoneNumber = phoneNumber;
    }

    public Contact(String name, String phoneNumber, int contactType) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.contactType = contactType;
    }

    public Contact(String name, String phoneNumber, String uid) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.uid = uid;
    }

    public Contact(String name, String phoneNumber, String uid, Integer contactType) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.uid = uid;
        this.contactType = contactType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Contact(Parcel parcel){
        this.uid=parcel.readString();
        this.name=parcel.readString();
        this.phoneNumber =parcel.readString();
        this.contactType =parcel.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
          parcel.writeString(this.uid);
          parcel.writeString(this.name);
          parcel.writeString(this.phoneNumber);
          parcel.writeInt(this.contactType);


    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };
}
