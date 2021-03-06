package ru.fantlab.android.ui.widgets.htmlview

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.text.Html
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.widget.PopupMenu
import android.widget.TextView
import ru.fantlab.android.R
import ru.fantlab.android.helper.ActivityHelper
import ru.fantlab.android.helper.TypeFaceHelper
import ru.fantlab.android.helper.ViewHelper
import ru.fantlab.android.provider.handler.BetterLinkMovementExtended
import ru.fantlab.android.provider.scheme.LinkParserHelper
import ru.fantlab.android.provider.scheme.SchemeParser
import ru.fantlab.android.ui.widgets.htmlview.drawable.DrawableGetter
import java.util.regex.Pattern
import android.view.ViewTreeObserver
import ru.fantlab.android.ui.widgets.FontTextView

open class HTMLTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FontTextView(context, attrs, defStyleAttr),
		BetterLinkMovementExtended.OnLinkClickListener,
		BetterLinkMovementExtended.OnLinkLongClickListener, ViewTreeObserver.OnGlobalLayoutListener {

	lateinit var betterLinkMovementMethod: BetterLinkMovementExtended

	var html: CharSequence? = null
		set(value) {
			field = value
			render(html)
		}

	init {
		init()
	}

	private fun init() {
		if (isInEditMode) return
		TypeFaceHelper.applyTypeface(this)
		setLinkTextColor(ViewHelper.getSecondaryTextColor(context))
		betterLinkMovementMethod = BetterLinkMovementExtended.linkifyHtml(this)
		betterLinkMovementMethod.setOnLinkClickListener(this)
		betterLinkMovementMethod.setOnLinkLongClickListener(this)
		viewTreeObserver.addOnGlobalLayoutListener(this)
	}

	private fun render(html: CharSequence?) {
		val data = html!!
				.replace(SQUARE_TAG.toRegex(), "<$1>")
				.replace(BBCODES_TAG.toRegex(), "<a href=\"$1$2\">$3</a>")
				.replace(URL_LINK_TAG.toRegex(), "<a href=\"$2\">$3</a>")
				.replace(LIST_TAG.toRegex(), "<ul>$1</ul>")
				.replace(PHOTO_TAG.toRegex(), "")
				.replace(SMILES_TAG.toRegex(), "<img src=\"file:///android_asset/smiles/$1.gif\">")
				.replace(IMG_TAG.toRegex(), "<img src=\"$1\">")
				.replace(LI_TAG, "<li>")
				.replace(UNIQUE_URL_TAG.toRegex(), "<a href=\"$1\">$1</a>")
				.replace("\n", "<br>")
		text = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
			Html.fromHtml(data, Html.FROM_HTML_MODE_LEGACY, DrawableGetter(this, width), CustomTagHandler());
		} else {
			Html.fromHtml(data, DrawableGetter(this, width), CustomTagHandler());
		}
	}

	override fun onTouchEvent(event: MotionEvent): Boolean {
		val action = event.actionMasked
		if (action == MotionEvent.ACTION_CANCEL) {
			betterLinkMovementMethod.isLongPress = false
		}
		if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {

			var x = event.x.toInt()
			var y = event.y.toInt()
			x -= totalPaddingLeft
			y -= totalPaddingTop
			x += scrollX
			y += scrollY

			val layout = layout
			val line = layout.getLineForVertical(y)
			val off = layout.getOffsetForHorizontal(line, x.toFloat())

			val buffer = SpannableString(text)

			val link = buffer.getSpans(off, off, ClickableSpan::class.java)
			if (link.isNotEmpty()) {
				betterLinkMovementMethod.isLongPress = true
				betterLinkMovementMethod.onTouchEvent(this, buffer, event)
				return true
			}
		}
		return false
	}

	override fun onLongClick(view: TextView, link: String, label: String): Boolean {
		view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
		val menu = PopupMenu(view.context, view)
		menu.setOnMenuItemClickListener { menuItem ->
			val url = if (link.contains("http")) link else "${LinkParserHelper.PROTOCOL_HTTPS}://${LinkParserHelper.HOST_DEFAULT}/$link"
			when (menuItem.itemId) {
				R.id.copy -> {
					ActivityHelper.copyToClipboard(view.context, url)
					true
				}
				R.id.open -> {
					SchemeParser.launchUri(view.context, url, label)
					true
				}
				else -> {
					false
				}
			}

		}
		menu.inflate(R.menu.link_popup_menu)
		menu.show()
		return true
	}

	override fun onClick(view: TextView, link: String, label: String): Boolean {
		SchemeParser.launchUri(view.context, link, label)
		return true
	}

	override fun onGlobalLayout() {
		viewTreeObserver.removeOnGlobalLayoutListener(this)
		if (lineCount >= maxLines) {
			val lineEndIndex = layout.getLineEnd(maxLines - 1)
			if (length() > lineEndIndex) {
				val text = text.subSequence(0, lineEndIndex - 3).toString() + "..."
				setText(text)
			}
		}
	}

	companion object {
		private val SQUARE_TAG: Pattern = Pattern.compile("\\[(.*?)\\]")
		private val BBCODES_TAG: Pattern = Pattern.compile("<(autor|author|work|edition|person|user|art|dictor|series|film|translator|pub)=(.*?)>(.*?)<\\/.*>", Pattern.CASE_INSENSITIVE)
		private val URL_LINK_TAG: Pattern = Pattern.compile("<(link|url)=(.*?)>(.*?)<\\/.*>", Pattern.CASE_INSENSITIVE)
		private val PHOTO_TAG: Pattern = Pattern.compile("<PHOTO.*?>")
		private val LIST_TAG: Pattern = Pattern.compile("<list>(.*?)<\\/list>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
		private val LI_TAG = "<*>"
		private val SMILES_TAG: Pattern = Pattern.compile(":([a-z]+):")
		private val IMG_TAG: Pattern = Pattern.compile("<img>(.*?)<\\/img>", Pattern.CASE_INSENSITIVE)
		private val UNIQUE_URL_TAG: Pattern = Pattern.compile("(https?:\\/\\/[^\"<\\s]+)(?![^<>]*>|[^=\"]*?<\\/.*)")
	}

}