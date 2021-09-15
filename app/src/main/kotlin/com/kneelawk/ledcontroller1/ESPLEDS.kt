package com.kneelawk.ledcontroller1

import android.os.Parcel
import android.os.Parcelable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant

class ESPLEDS(val ip: String, val name: String, val lastUpdate: Instant) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        Instant.ofEpochSecond(parcel.readLong())
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(ip)
        parcel.writeString(name)
        parcel.writeLong(lastUpdate.epochSecond)
    }

    private suspend fun <T> get(path: String, fromString: (String) -> T): Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                Result.success(
                    fromString(
                        String(
                            URL("http://$ip/$path").openStream().readBytes()
                        ).trim()
                    )
                )
            } catch (e: IOException) {
                Result.failure(e)
            } catch (e: NumberFormatException) {
                Result.failure(e)
            }
        }
    }

    private suspend fun <T> put(
        path: String,
        value: T,
        toString: (T) -> String,
        fromString: (String) -> T
    ): Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                (URL("http://$ip/$path").openConnection() as? HttpURLConnection)?.run {
                    requestMethod = "PUT"
                    doOutput = true
                    setRequestProperty("Content-Type", "text/plain")
                    setRequestProperty("Accept", "text/plain")

                    outputStream.write(toString(value).toByteArray())
                    Result.success(fromString(String(inputStream.readBytes()).trim()))
                } ?: Result.failure(IOException("Unable to open connection"))
            } catch (e: IOException) {
                Result.failure(e)
            } catch (e: NumberFormatException) {
                Result.failure(e)
            }
        }
    }

    suspend fun getBrightness(): Result<Int> {
        return get("brightness", String::toInt)
    }

    suspend fun putBrightness(newBrightness: Int): Result<Int> {
        return put("brightness", newBrightness, Int::toString, String::toInt)
    }

    suspend fun getFrameDuration(): Result<Int> {
        return get("frame-duration", String::toInt)
    }

    suspend fun putFrameDuration(newFrameDuration: Int): Result<Int> {
        return put("frame-duration", newFrameDuration, Int::toString, String::toInt)
    }

    suspend fun getHuePerPixel(): Result<Int> {
        return get("hue-per-pixel", String::toInt)
    }

    suspend fun putHuePerPixel(newHuePerPixel: Int): Result<Int> {
        return put("hue-per-pixel", newHuePerPixel, Int::toString, String::toInt)
    }

    suspend fun getHuePerFrame(): Result<Int> {
        return get("hue-per-frame", String::toInt)
    }

    suspend fun putHuePerFrame(newHuePerFrame: Int): Result<Int> {
        return put("hue-per-frame", newHuePerFrame, Int::toString, String::toInt)
    }

    companion object CREATOR : Parcelable.Creator<ESPLEDS> {
        override fun createFromParcel(parcel: Parcel): ESPLEDS {
            return ESPLEDS(parcel)
        }

        override fun newArray(size: Int): Array<ESPLEDS?> {
            return arrayOfNulls(size)
        }
    }
}
