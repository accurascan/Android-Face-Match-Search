package com.accurascan.facedetection;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorInt;

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
    public String feedBackMultipleFaceMessage;
    public String feedBackHeadStraightMessage;
    public String feedBackLowLightMessage;
    public String feedBackBlurFaceMessage;
    public String feedBackGlareFaceMessage;

    public int lowLightPercentage = 39;
    public int blurPercentage = 75;
    public int glareMinPercentage = 6;
    public int glareMaxPercentage = 99;


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
        feedBackMultipleFaceMessage = in.readString();
        feedBackHeadStraightMessage = in.readString();
        feedBackLowLightMessage = in.readString();
        feedBackBlurFaceMessage = in.readString();
        feedBackGlareFaceMessage = in.readString();
        lowLightPercentage = in.readInt();
        blurPercentage = in.readInt();
        glareMinPercentage = in.readInt();
        glareMaxPercentage = in.readInt();
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

    public void setLowLightPercentage(int lowLightPercentage){
        this.lowLightPercentage = lowLightPercentage;
    }

    public void setBlurPercentage(int blurPercentage){
        this.blurPercentage = blurPercentage;
    }

    public void setGlarePercentage(int glareMinPercentage, int glareMaxPercentage){
        this.glareMinPercentage = glareMinPercentage;
        this.glareMaxPercentage = glareMaxPercentage;
    }

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
        dest.writeString(feedBackMultipleFaceMessage);
        dest.writeString(feedBackHeadStraightMessage);
        dest.writeString(feedBackLowLightMessage);
        dest.writeString(feedBackBlurFaceMessage);
        dest.writeString(feedBackGlareFaceMessage);
        dest.writeInt(lowLightPercentage);
        dest.writeInt(blurPercentage);
        dest.writeInt(glareMinPercentage);
        dest.writeInt(glareMaxPercentage);
    }
}
