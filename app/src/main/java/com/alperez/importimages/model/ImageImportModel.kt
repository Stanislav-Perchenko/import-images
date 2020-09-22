package com.alperez.importimages.model

import android.os.Parcel
import android.os.Parcelable
import com.alperez.importimages.util.StringJoinerCompat
import org.json.JSONException
import org.json.JSONObject


/**
 * Created by stanislav.perchenko on 22.09.2020 at 20:29.
 */
data class ImageImportModel(

    val localFile: String = "",
    val size: Int = 0,
    val mimeType: String = "*/*",
    val relativeHashName: String,
    val width: Int = 0,
    val height: Int = 0,
    val creationTimestamp: Long = 0


): Parcelable, Cloneable {

    constructor() : this(
        localFile = "",
        size = 0,
        mimeType = "",
        relativeHashName = "",
        width = 0,
        height = 0,
        creationTimestamp = 0
    )

    override fun clone(): ImageImportModel = ImageImportModel(
        localFile = localFile,
        size = size,
        mimeType = mimeType,
        relativeHashName = relativeHashName,
        width = width,
        height = height,
        creationTimestamp = creationTimestamp
    )

    /***************************  Parcelable implementation  **************************************/
    override fun describeContents(): Int =  0

    override fun writeToParcel(dst: Parcel, flags: Int) {
        dst.writeString(localFile)
        dst.writeInt(size)
        dst.writeString(mimeType)
        dst.writeString(relativeHashName)
        dst.writeInt(width)
        dst.writeInt(height)
        dst.writeLong(creationTimestamp)
    }

    private constructor(p: Parcel) : this(
        localFile = p.readString() ?: "",
        size = p.readInt(),
        mimeType = p.readString() ?: "*/*",
        relativeHashName = p.readString() ?: "",
        width = p.readInt(),
        height = p.readInt(),
        creationTimestamp = p.readLong()
    )

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ImageImportModel> = object : Parcelable.Creator<ImageImportModel> {
            override fun createFromParcel(parcel: Parcel) = ImageImportModel(parcel)
            override fun newArray(size: Int) = arrayOfNulls<ImageImportModel>(size)
        }


        fun fromJson(json: String?): ImageImportModel {
            return try {
                fromJsonObject(JSONObject(json))
            } catch (e: JSONException) {
                throw java.lang.RuntimeException(e)
            }
        }

        @Throws(JSONException::class)
        fun fromJsonObject(jObj: JSONObject): ImageImportModel {
            val localFile = jObj.getString("localFile")
            val size = jObj.optInt("size", 0)
            val mimeType = jObj.getString("mimeType")
            val hash = jObj.getString("hash")
            val w = jObj.getInt("w")
            val h = jObj.getInt("h")
            val ts = jObj.getLong("creation")
            return ImageImportModel(
                localFile = localFile,
                size = size,
                mimeType = mimeType,
                relativeHashName = hash,
                width = w,
                height = h,
                creationTimestamp = ts
            )
        }
    }

    /************************  Builder pattern implementation  ************************************/
    class Builder {
        private lateinit var localFile: String
        private var size: Int = -1
        private lateinit var mimeType: String
        private lateinit var relativeHashName: String
        private var width: Int = -1
        private var height: Int = -1
        private var creationTimestamp: Long = 0

        fun setLocalFile(localFile: String) = apply {this.localFile = localFile}
        fun setSize(size: Int) = apply { this.size = size }
        fun setMimeType(mimeType: String) = apply { this.mimeType = mimeType }
        fun setRelativeHashName(relativeHashName: String) = apply { this.relativeHashName = relativeHashName }
        fun setWidth(width: Int) = apply { this.width = width }
        fun setHeight(height: Int) = apply { this.height = height }
        fun setCreationTimestamp(creationTimestamp: Long) = apply { this.creationTimestamp = creationTimestamp }

        fun build(): ImageImportModel {
            val missed = StringJoinerCompat(", ", "", "")

            if (! ::localFile.isInitialized) missed.add("localFile")
            if (size < 0) missed.add("size")
            if (! ::mimeType.isInitialized) missed.add("mimeType")
            if (! ::relativeHashName.isInitialized) missed.add("relativeHashName")
            if (width < 0) missed.add("width")
            if (height < 0) missed.add("height")
            if (creationTimestamp < 0) missed.add("creationTimestamp")

            return if (missed.length() == 0) {
                ImageImportModel(
                    localFile = localFile,
                    size = size,
                    mimeType = mimeType,
                    relativeHashName = relativeHashName,
                    width = width,
                    height = height,
                    creationTimestamp = creationTimestamp
                )
            } else {
                throw IllegalStateException("ImageImportModel.Builder - some fields are missed: $missed")
            }
        }
    }

    /**********************************************************************************************/

    @Throws(JSONException::class)
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("localFile", localFile)
        put("size", size)
        put("mimeType", mimeType)
        put("hash", relativeHashName)
        put("w", width)
        put("h", height)
        put("creation", creationTimestamp)
    }

    fun toJson(): String {
        return try {
            toJsonObject().toString()
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

}
