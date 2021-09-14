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

    suspend fun getBrightness(): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                Result.success(
                    String(
                        URL("http://$ip/brightness").openStream().readBytes()
                    ).trim().toInt()
                )
            } catch (e: IOException) {
                Result.failure(e)
            } catch (e: NumberFormatException) {
                Result.failure(e)
            }
        }
    }

    suspend fun putBrightness(newBrightness: Int): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                (URL("http://$ip/brightness").openConnection() as? HttpURLConnection)?.run {
                    requestMethod = "PUT"
                    doOutput = true
                    setRequestProperty("Content-Type", "text/plain")
                    setRequestProperty("Accept", "text/plain")

                    outputStream.write("$newBrightness".toByteArray())
                    Result.success(String(inputStream.readBytes()).trim().toInt())
                } ?: Result.failure(IOException("Unable to open connection"))
            } catch (e: IOException) {
                Result.failure(e)
            } catch (e: NumberFormatException) {
                Result.failure(e)
            }
        }
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
