package org.faudroids.keepgoing.ui;


import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Fully expands the list view to not show
 * any scrolling bars.
 *
 * Courtesy to http://stackoverflow.com/a/5720141
 */
public class ExpandedListView extends ListView {

	private int oldCount = 0;

	public ExpandedListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setVerticalScrollBarEnabled(false);
		setHorizontalScrollBarEnabled(false);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (getCount() != oldCount) {
			oldCount = getCount();
			android.view.ViewGroup.LayoutParams params = getLayoutParams();
			params.height = getCount() * (oldCount > 0 ? getChildAt(0).getHeight() : 0);
			setLayoutParams(params);
		}

		super.onDraw(canvas);
	}

}