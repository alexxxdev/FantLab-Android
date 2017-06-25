package org.odddev.fantlab.autors

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class AutorDeserializer : JsonDeserializer<Autor> {

	override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext?): Autor {
		val jsonObject = json.asJsonObject

		return Autor(
				id = jsonObject.get("autor_id").asString.toInt(),
				isFv = jsonObject.get("is_fv").asString.toInt() == 1,
				name = jsonObject.get("name").asString,
				nameOrig = jsonObject.get("name_orig")?.asString ?: "",
				nameRp = jsonObject.get("name_rp").asString,
				nameShort = jsonObject.get("name_short").asString,
				type = jsonObject.get("type").asString
		)
	}
}