package com.mapswithme.maps.widget.mapotempo;

import android.support.v4.app.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapswithme.maps.R;

import com.mapswithme.maps.bookmarks.ChooseBookmarkCategoryFragment;
import com.mapswithme.maps.bookmarks.data.BookmarkCategory;
import com.mapswithme.maps.bookmarks.data.BookmarkManager;
import com.mapswithme.maps.bookmarks.data.MTRouteListManager;
import com.woxthebox.draglistview.DragItem;
import com.woxthebox.draglistview.DragListView;

public class MapotempoListFragment extends Fragment
{
  private int mCategoryIndex = -1;
  BookmarkCategory mCurrentCategory;
  private DragListView mDragListView;

  public static MapotempoListFragment newInstance() {
    return new MapotempoListFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if(getArguments() != null)
      mCategoryIndex = getArguments().getInt(ChooseBookmarkCategoryFragment.CATEGORY_ID, -1);
  }

  @Override
  public void onStart()
  {
    super.onStart();
    setupListRecyclerView();
  }

  @Override
  public void onResume()
  {
    super.onResume();
    setupListRecyclerView();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.mapotempo_list, container, false);
    mDragListView = (DragListView) view.findViewById(R.id.drag_list_view);
    mDragListView.getRecyclerView().setVerticalScrollBarEnabled(true);
    mDragListView.setDragEnabled(true);
    mDragListView.setDragListListener(new DragListView.DragListListener() {
      @Override
      public void onItemDragStarted(int position) {
      }

      @Override
      public void onItemDragEnded(int fromPosition, int toPosition) {
        if (fromPosition != toPosition) {
          BookmarkManager.INSTANCE.changeBookmarkOrder(mCurrentCategory.getId(), fromPosition, toPosition);
        }
      }

      @Override
      public void onItemDragging(int itemPosition, float x, float y)
      {
      }
    });
    return view;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
  }

  private void setupListRecyclerView() {
    MapotempoListAdapter listAdapter;
    if(mCategoryIndex >= 0)
    {
      mCurrentCategory = BookmarkManager.INSTANCE.getCategory(mCategoryIndex);
      listAdapter = new MapotempoListAdapter(getActivity(), mCurrentCategory, R.layout.item_mapotempo_bookmark, R.id.iv__bookmark_drag, true);
    }
    else if(MTRouteListManager.INSTANCE.getStatus())
    {
      mCurrentCategory = BookmarkManager.INSTANCE.getCategory(MTRouteListManager.INSTANCE.getCurrentBookmark().getCategoryId());
      listAdapter = new MapotempoListAdapter(getActivity(), mCurrentCategory, R.layout.item_mapotempo_bookmark, R.id.iv__bookmark_drag, true);
    }
    else
    {
      listAdapter = new MapotempoListAdapter(getActivity(), R.layout.item_mapotempo_bookmark, R.id.iv__bookmark_drag, true);
    }

    mDragListView.setAdapter(listAdapter, false);

    mDragListView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
    mDragListView.setCanDragHorizontally(false);
    mDragListView.setCustomDragItem(new MyDragItem(getActivity().getApplicationContext(), R.layout.item_mapotempo_bookmark));
  }

  private static class MyDragItem extends DragItem {

    public MyDragItem(Context context, int layoutId) {
      super(context, layoutId);
    }

    @Override
    public void onBindDragView(View clickedView, View dragView)
    {
      dragView.setBackgroundColor(R.color.background_material_light);
      dragView.setAlpha((float)0.7);

      ImageView imgDrag = (ImageView) dragView.findViewById(R.id.iv__bookmark_color);
      ImageView imgClic = (ImageView) clickedView.findViewById(R.id.iv__bookmark_color);
      imgDrag.setImageDrawable(imgClic.getDrawable());

      TextView txtDrag = (TextView) dragView.findViewById(R.id.tv__bookmark_name);
      TextView txtClic = (TextView) clickedView.findViewById(R.id.tv__bookmark_name);
      txtDrag.setTextColor(txtClic.getCurrentTextColor());
      txtDrag.setText(txtClic.getText());

      ImageView imageButtonDrag = (ImageView) dragView.findViewById(R.id.iv__bookmark_drag);
      imageButtonDrag.setEnabled(true);
    }
  }
}
