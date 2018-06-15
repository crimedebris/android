package com.example.elegy1004.player;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class FilePickerParcelObject implements Parcelable {//实现parcelable接口，将对象序列化（转化成二进制流）

	public String path="";
	public ArrayList<String> names=new ArrayList<String>();
	public int count=0;

	public FilePickerParcelObject(String path, ArrayList<String> names, int count) {
		this.path=path;
		this.names=names;
		this.count=count;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel parcel, int flags) {//序列化
		parcel.writeString(path);
		parcel.writeStringList(names);
		parcel.writeInt(count);
	}

	public static final Creator<FilePickerParcelObject> CREATOR = new Creator<FilePickerParcelObject>() {
		public FilePickerParcelObject createFromParcel(Parcel in) {
			return new FilePickerParcelObject(in);
		}

		public FilePickerParcelObject[] newArray(int size) {
			return new FilePickerParcelObject[size];
		}
	};

	private FilePickerParcelObject(Parcel parcel) {
		path = parcel.readString();
		parcel.readStringList(names);
		count = parcel.readInt();
	}

}
