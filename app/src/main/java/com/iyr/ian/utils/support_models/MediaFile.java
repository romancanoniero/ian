package com.iyr.ian.utils.support_models;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.iyr.ian.dao.models.User;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;


public class MediaFile implements Parcelable, Serializable  {


    public static final int MEDIA_FILE_STATUS_UNMODIFIED = 0;
    public static final int MEDIA_FILE_STATUS_NEW = 1;
    public static final int MEDIA_FILE_STATUS_DELETED = 2;
    public static final int MEDIA_FILE_STATUS_MODIFIED = 3;

    public MediaTypesEnum media_type;
    public static final Creator<MediaFile> CREATOR = new Creator<MediaFile>() {
        @Override
        public MediaFile createFromParcel(Parcel in) {
            return new MediaFile(in);
        }

        @Override
        public MediaFile[] newArray(int size) {
            return new MediaFile[size];
        }
    };
    public boolean isFavorite;
    public String file_name;
    public long time;
    public int duration;
    @NotNull
    public Integer status;

    public String bytesB64;
    @NotNull
    public String text = "";
    @com.google.firebase.database.annotations.Nullable
    public User user;
    @Nullable
    public Integer width ;

    @Exclude
    public String localFullPath;


    public MediaFile() {
    }

    public MediaFile(@NotNull MediaTypesEnum mediaType) {
        this.media_type = mediaType;
    }

    /**
     * Crea un MediaFile con los datos de un archivo local
     * @param mediaType   VIDEO,IMAGE,AUDIO
     * @param localFullPath  -> Ubicacion local del archivo (Solo se usa al agregar un nuevo archivo)
     * @param fileName -> nombre del archivo que incluye subcarpetas adicionales
     */
    public MediaFile(@NotNull MediaTypesEnum mediaType, String localFullPath,  String fileName) {
        this.media_type = mediaType;
        this.file_name = fileName;
        this.localFullPath = localFullPath;
    }



    public MediaFile(@NotNull MediaTypesEnum image,  String path) {
        this.media_type = image;
        this.file_name = path;
    }

    public MediaFile(@NotNull MediaTypesEnum image,  String path, int duration) {
        this.media_type = image;
        this.file_name = path;
        this.duration = duration;
    }

    @Nullable
    public Integer height;

    protected MediaFile(Parcel in) {
        file_name = in.readString();
        isFavorite = in.readByte() != 0;
        if (in.readByte() == 0) {
            status = null;
        } else {
            status = in.readInt();
        }
        time = in.readLong();
        duration = in.readInt();
        text = in.readString();
        bytesB64 = in.readString();
        if (in.readByte() == 0) {
            width = null;
        } else {
            width = in.readInt();
        }
        if (in.readByte() == 0) {
            height = null;
        } else {
            height = in.readInt();
        }
    }


    public static MediaFile getFavorite(@Nullable ArrayList<MediaFile> files) {
        for (MediaFile file : Objects.requireNonNull(files)) {
            if (file.isFavorite) {
                return file;
            }
        }
        if (files.size() > 0) {
            return files.get(0);
        }
        return null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(file_name);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
        if (status == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(status);
        }
        dest.writeLong(time);
        dest.writeInt(duration);
        dest.writeString(text);
        dest.writeString(bytesB64);
        if (width == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(width);
        }
        if (height == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(height);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        //if (!super.equals(o)) return false;
        MediaFile mediaFile = (MediaFile) o;
        return media_type == mediaFile.media_type &&
                Objects.equals(file_name, mediaFile.file_name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), media_type, file_name);
    }

}

