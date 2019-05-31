package com.purposecaller.purposecaller.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;

import java.util.HashMap;

@Keep
public class Room implements Parcelable{
   public static final int OPEN_ROOM_STATE=1;
    public static final int JOINED_ROOM_STATE=2;
    public static final int ROOM_CREATOR=1;
    public static final int ROOM_JOINER=2;

   public HashMap<String,Player> players=new HashMap<>();
   public double currentRoomState=OPEN_ROOM_STATE,purposeCode,interactVia;
    public String roomName;
    public Object GameData;

    public Room() {
    }

    public Room(String roomName, double purposeCode, double interactVia) {
        this.roomName = roomName;
        this.purposeCode=purposeCode;
        this.interactVia=interactVia;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Room(Parcel parcel){
       this.purposeCode=parcel.readDouble();
        this.interactVia=parcel.readDouble();
        this.roomName=parcel.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(this.purposeCode);
        parcel.writeDouble(this.interactVia);
        parcel.writeString(roomName);


    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Room createFromParcel(Parcel in) {
            return new Room(in);
        }

        public Room[] newArray(int size) {
            return new Room[size];
        }
    };
}
