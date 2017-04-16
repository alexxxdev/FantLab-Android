package org.odddev.fantlab.autors

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import org.odddev.fantlab.R
import org.odddev.fantlab.databinding.AutorsFragmentBinding

/**
 * @author kenrube
 * *
 * @since 10.12.16
 */

class AutorsFragment : MvpAppCompatFragment(), IAutorsView {

	private lateinit var binding: AutorsFragmentBinding
	private lateinit var adapter: AutorsAdapter

	@InjectPresenter
	lateinit var presenter: AutorsPresenter

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
							  savedInstanceState: Bundle?): View? {
		binding = AutorsFragmentBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		presenter.getAutors()

		initToolbar()
		setHasOptionsMenu(true)

		initRecyclerView()
	}

	private fun initToolbar() {
		val activity = activity as AppCompatActivity
		activity.setSupportActionBar(binding.toolbar)
		val actionBar = activity.supportActionBar
		if (actionBar != null) {
			actionBar.setTitle(R.string.nav_awards)
			actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp)
			actionBar.setDisplayHomeAsUpEnabled(true)
		}
	}

	private fun initRecyclerView() {
		val layoutManager = LinearLayoutManager(context)
		binding.autors.layoutManager = layoutManager
		adapter = AutorsAdapter()
		binding.autors.adapter = adapter
	}

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		inflater.inflate(R.menu.action_bar_awards, menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			android.R.id.home -> (activity.findViewById(R.id.drawer_layout) as DrawerLayout)
					.openDrawer(GravityCompat.START)
		}
		return super.onOptionsItemSelected(item)
	}

	override fun showAutors(autors: List<Autor>) {
		adapter.setAutors(autors)
	}

	override fun showError(message: String) {
		Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
	}
}
