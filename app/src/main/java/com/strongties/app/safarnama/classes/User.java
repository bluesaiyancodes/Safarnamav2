package com.strongties.app.safarnama.classes;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;


public class User implements Parcelable {

    private String email;
    private String user_id;
    private String username;
    private String photo;
    private String avatar;
    @ServerTimestamp
    private Date dateofjoin;
    @ServerTimestamp
    private Date lastlogin;

    public User(String email, String username, String photo, String user_id, String avatar, Date dateofjoin, Date lastlogin) {
        this.email = email;
        this.user_id = user_id;
        this.username = username;
        this.photo = photo;
        this.avatar = avatar;
        this.dateofjoin = dateofjoin;
        this.lastlogin = lastlogin;
    }

    public User() {

    }

    protected User(Parcel in) {
        email = in.readString();
        user_id = in.readString();
        username = in.readString();
        photo = in.readString();
        avatar = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String avatar) {
        this.photo = avatar;
    }

    public static Creator<User> getCREATOR() {
        return CREATOR;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Date getDateofjoin() {
        return dateofjoin;
    }

    public void setDateofjoin(Date dateofjoin) {
        this.dateofjoin = dateofjoin;
    }

    public Date getLastlogin() {
        return lastlogin;
    }

    public void setLastlogin(Date lastlogin) {
        this.lastlogin = lastlogin;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", user_id='" + user_id + '\'' +
                ", username='" + username + '\'' +
                ", photo='" + photo + '\'' +
                ", avatar='" + avatar + '\'' +
                ", dateofjoin=" + dateofjoin +
                ", lastlogin=" + lastlogin +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(user_id);
        dest.writeString(username);
        dest.writeString(photo);
        dest.writeString(avatar);
    }
}

