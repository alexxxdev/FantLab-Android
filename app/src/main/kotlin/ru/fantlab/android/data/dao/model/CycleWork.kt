package ru.fantlab.android.data.dao.model

import android.os.Parcelable
import android.support.annotation.Keep
import kotlinx.android.parcel.Parcelize
import ru.fantlab.android.R
import ru.fantlab.android.ui.widgets.treeview.LayoutItemType

@Keep
@Parcelize
class CycleWork(
		var id: Int?,
		var authors: List<String>,
		var name: String,
		var nameOrig: String,
		val description: String?,
		var year: Int?,
		var responseCount: Int?,
		var votersCount: String?,
		var rating: Float?,
		var mark: Int?
) : LayoutItemType, Parcelable {

	override val layoutId = R.layout.author_cycle_work_row_item

}