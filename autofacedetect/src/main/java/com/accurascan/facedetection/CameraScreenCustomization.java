package com.accurascan.facedetection;

import android.os.Parcel;
import android.os.Parcelable;

public final class CameraScreenCustomization implements Parcelable {

    public int backGroundColor = 0xFFC4C4C5;

    public int closeIconColor = 0xFF000000;

    public int feedbackBackGroundColor = 0x00000000;
    public int feedbackTextColor = 0xFF000000;
    public int feedbackTextSize;

    public String feedBackAwayMessage;
    public String feedBackCloserMessage;
    public String feedBackOpenEyesMessage;
    public String feedBackCenterMessage;
    public String feedBackframeMessage;
    public String feedBackMultipleFace;


    public CameraScreenCustomization() {
    }

    protected CameraScreenCustomization(Parcel in) {
        backGroundColor = in.readInt();
        closeIconColor = in.readInt();
        feedbackBackGroundColor = in.readInt();
        feedbackTextColor = in.readInt();
        feedbackTextSize = in.readInt();
        feedBackAwayMessage = in.readString();
        feedBackCloserMessage = in.readString();
        feedBackOpenEyesMessage = in.readString();
        feedBackCenterMessage = in.readString();
        feedBackframeMessage = in.readString();
        feedBackMultipleFace = in.readString();
    }

    public static final Creator<CameraScreenCustomization> CREATOR = new Creator<CameraScreenCustomization>() {
        @Override
        public CameraScreenCustomization createFromParcel(Parcel in) {
            return new CameraScreenCustomization(in);
        }

        @Override
        public CameraScreenCustomization[] newArray(int size) {
            return new CameraScreenCustomization[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(backGroundColor);
        dest.writeInt(closeIconColor);
        dest.writeInt(feedbackBackGroundColor);
        dest.writeInt(feedbackTextColor);
        dest.writeInt(feedbackTextSize);
        dest.writeString(feedBackAwayMessage);
        dest.writeString(feedBackCloserMessage);
        dest.writeString(feedBackOpenEyesMessage);
        dest.writeString(feedBackCenterMessage);
        dest.writeString(feedBackframeMessage);
        dest.writeString(feedBackMultipleFace);
    }
}
