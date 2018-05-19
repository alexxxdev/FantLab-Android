package ru.fantlab.android.data.dao.newmodel

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AuthorInList(
		val id: Int,
		val name: String,
		@SerializedName("name_orig") val nameOrig: String,
		@SerializedName("name_rp") val nameRp: String,
		@SerializedName("name_short") val nameShort: String,
		val type: String,
		val url: String
) : Parcelable