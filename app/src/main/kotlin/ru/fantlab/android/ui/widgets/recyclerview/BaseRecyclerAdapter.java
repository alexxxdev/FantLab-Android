package ru.fantlab.android.ui.widgets.recyclerview;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ru.fantlab.android.helper.AnimHelper;
import ru.fantlab.android.helper.PrefGetter;

/**
 * Created by Kosh on 17 May 2016, 7:10 PM
 */
public abstract class BaseRecyclerAdapter<M, VH extends BaseViewHolder,
	P extends BaseViewHolder.OnItemClickListener<M>> extends RecyclerView.Adapter<VH> {

	private final static int PROGRESS_TYPE = 2017;

	@NonNull
	private List<M> data;
	@Nullable
	private P listener;
	private int lastKnowingPosition = -1;
	private boolean enableAnimation = PrefGetter.INSTANCE.isRVAnimationEnabled();
	private boolean showedGuide;
	private GuideListener guideListener;
	private boolean progressAdded;
	private int rowWidth;

	protected BaseRecyclerAdapter() {
		this(new ArrayList<M>());
	}

	protected BaseRecyclerAdapter(@NonNull List<M> data) {
		this(data, null);
	}

	protected BaseRecyclerAdapter(@NonNull List<M> data, @Nullable P listener) {
		this.data = data;
		this.listener = listener;
	}

	protected BaseRecyclerAdapter(@Nullable P listener) {
		this(new ArrayList<M>(), listener);
	}

	@NonNull
	public List<M> getData() {
		return data;
	}

	public M getItemByPosition(int position) {
		return data.get(position);
	}

	@SuppressWarnings("unchecked")
	@Override
	public VH onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == PROGRESS_TYPE) {
			addSpanLookup(parent);
			return (VH) ProgressBarViewHolder.Companion.newInstance(parent);
		} else {
			return viewHolder(parent, viewType);
		}
	}

	@Override
	public void onBindViewHolder(@NonNull VH holder, int position) {
		if (holder instanceof ProgressBarViewHolder) {
			if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
				StaggeredGridLayoutManager.LayoutParams layoutParams =
					(StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
				layoutParams.setFullSpan(true);
			}
		} else if (getItem(position) != null) {
			animate(holder, position);
			onBindView(holder, position);
			onShowGuide(holder, position);
		}
	}

	@Override
	public int getItemViewType(int position) {
		if (getItem(position) == null) {
			return PROGRESS_TYPE;
		}
		return super.getItemViewType(position);
	}

	@Override
	public int getItemCount() {
		return data.size();
	}

	@Override
	public void onViewDetachedFromWindow(VH holder) {
		holder.onViewIsDetaching();
		super.onViewDetachedFromWindow(holder);
	}

	private void animate(@NonNull VH holder, int position) {
		if (isEnableAnimation() && position > lastKnowingPosition) {
			AnimHelper.INSTANCE.startBeatsAnimation(holder.itemView);
			lastKnowingPosition = position;
		}
	}

	protected abstract void onBindView(VH holder, int position);

	@SuppressWarnings("unchecked")
	private void onShowGuide(
		@NonNull VH holder,
		int position
	) {// give the flexibility to other adapters to override this
		if (position == 0 && !isShowedGuide() && guideListener != null) {
			guideListener.onShowGuide(holder.itemView, getItem(position));
			showedGuide = true;
		}
	}

	@SuppressWarnings("WeakerAccess")
	public boolean isEnableAnimation() {
		return enableAnimation;
	}

	public void setEnableAnimation(boolean enableAnimation) {
		this.enableAnimation = enableAnimation;
		notifyDataSetChanged();
	}

	private boolean isShowedGuide() {
		return showedGuide;
	}

	private void addSpanLookup(ViewGroup parent) {
		if (parent instanceof RecyclerView) {
			if (((RecyclerView) parent).getLayoutManager() instanceof GridLayoutManager) {
				final GridLayoutManager layoutManager =
					((GridLayoutManager) ((RecyclerView) parent).getLayoutManager());
				layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
					@Override
					public int getSpanSize(int position) {
						return getItemViewType(position) == PROGRESS_TYPE ? layoutManager.getSpanCount() : 1;
					}
				});
			}
		}
	}

	protected abstract VH viewHolder(ViewGroup parent, int viewType);

	public M getItem(int position) {
		return data.get(position);
	}

	public void insertItems(@NonNull List<M> items) {
		data.clear();
		data.addAll(items);
		notifyDataSetChanged();
		progressAdded = false;
	}

	public void addItem(M item, int position) {
		data.add(position, item);
		notifyItemInserted(position);
	}

	@SuppressWarnings("WeakerAccess")
	public void addItems(@NonNull List<M> items) {
		removeProgress();
		data.addAll(items);
		notifyItemRangeInserted(getItemCount(), (getItemCount() + items.size()) - 1);
	}

	private void removeProgress() {
		if (!isEmpty()) {
			M m = getItem(getItemCount() - 1);
			if (m == null) {
				removeItem(getItemCount() - 1);
			}
			progressAdded = false;
		}
	}

	public boolean isEmpty() {
		return data.isEmpty();
	}

	@SuppressWarnings("WeakerAccess")
	public void removeItem(int position) {
		data.remove(position);
		notifyItemRemoved(position);
	}

	public void removeItem(M item) {
		int position = data.indexOf(item);
		if (position != -1) {
			removeItem(position);
		}
	}

	public void removeItems(@NonNull List<M> items) {
		int prevSize = getItemCount();
		data.removeAll(items);
		notifyItemRangeRemoved(prevSize, Math.abs(data.size() - prevSize));
	}

	public void swapItem(@NonNull M model) {
		int index = getItem(model);
		swapItem(model, index);
	}

	public int getItem(M t) {
		return data.indexOf(t);
	}

	public void swapItem(@NonNull M model, int position) {
		if (position != -1) {
			data.set(position, model);
			notifyItemChanged(position);
		}
	}

	public void subList(int fromPosition, int toPosition) {
		if (data.isEmpty()) {
			return;
		}
		data.subList(fromPosition, toPosition).clear();
		notifyItemRangeRemoved(fromPosition, toPosition);
	}

	public void clear() {
		progressAdded = false;
		data.clear();
		notifyDataSetChanged();
	}

	@SuppressWarnings("WeakerAccess")
	@Nullable
	public P getListener() {
		return listener;
	}

	public void setListener(@Nullable P listener) {
		this.listener = listener;
		notifyDataSetChanged();
	}

	public void setGuideListener(GuideListener guideListener) {
		this.guideListener = guideListener;
	}

	public int getRowWidth() {
		return rowWidth;
	}

	public void setRowWidth(int rowWidth) {
		if (this.rowWidth == 0) {
			this.rowWidth = rowWidth;
			notifyDataSetChanged();
		}
	}

	public void addProgress() {
		if (!progressAdded && !isEmpty()) {
			addItem(null);
			progressAdded = true;
		}
	}

	public void addItem(M item) {
		removeProgress();
		data.add(item);
		if (data.size() == 0) {
			notifyDataSetChanged();
		} else {
			notifyItemInserted(data.size() - 1);
		}
	}

	public boolean isProgressAdded() {
		return progressAdded;
	}

	public interface GuideListener<M> {
		void onShowGuide(@NonNull View itemView, @NonNull M model);
	}
}
