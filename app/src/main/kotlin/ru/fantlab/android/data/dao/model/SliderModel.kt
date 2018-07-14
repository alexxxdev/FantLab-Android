package ru.fantlab.android.data.dao.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SliderModel(
		val imageUrl : String,
		val text : String
		) : Parcelable