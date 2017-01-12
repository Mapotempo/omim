package com.mapswithme.maps.widget.mapotempo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.mapswithme.maps.Framework;
import com.mapswithme.maps.R;
import com.mapswithme.maps.bookmarks.ChooseBookmarkCategoryFragment;
import com.mapswithme.maps.bookmarks.data.BookmarkCategory;
import com.mapswithme.maps.bookmarks.data.BookmarkManager;
import com.mapswithme.maps.bookmarks.data.RouteListManager;

public class MapotempoListManagerFragment extends Fragment implements Framework.MTRouteOptimize
{
  private BookmarkCategory mCategory;

  private ProgressBar mOptimProgressBar;

  public static MapotempoListManagerFragment newInstance() {
    return new MapotempoListManagerFragment();
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    Framework.nativeSetMTOptimRouteListener(this);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    int categoryIndex = getArguments().getInt(ChooseBookmarkCategoryFragment.CATEGORY_ID, -1);

    if(categoryIndex >= 0)
    {
      mCategory = BookmarkManager.INSTANCE.getCategory(categoryIndex);
    }
  }

  @Override
  public void onStart()
  {
    super.onStart();

    // Sub Fragment list
    if (getView().findViewById(R.id.mt_layout_fragment_container) != null
        && mCategory != null)
    {
      MapotempoListFragment firstFragment = new MapotempoListFragment();
      firstFragment.setArguments(getArguments());
      getFragmentManager().beginTransaction().add(R.id.mt_layout_fragment_container, firstFragment).commit();
    }
  }

  @Override
  public void onResume()
  {
    super.onResume();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.mapotempo_list_manager, container, false);

    mOptimProgressBar = (ProgressBar) view.findViewById(R.id.mt_optim_progress_bar);
    mOptimProgressBar.setMax(100);

    ImageView optim_launcher = (ImageView) view.findViewById(R.id.mt_list_manager_optim);
    optim_launcher.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        mOptimProgressBar.setProgress(0);
        mOptimProgressBar.setVisibility(View.VISIBLE);
        RouteListManager.nativeOptimiseBookmarkCategory(mCategory.getId());
      }
    });

    return view;
  }

  // Attention ses méthodes appelles du code thread on le racroche ton au thread ui !!!!
  // FIXME A l'arrache !!!!! A refaire

  boolean v;
  @Override
  public void onMtRouteOptimizeFinish(boolean status)
  {
    v = status;
    getActivity().runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        mOptimProgressBar.setVisibility(View.GONE);
        // Sub Fragment rebuild
        if (getView().findViewById(R.id.mt_layout_fragment_container) != null
            && mCategory != null)
        {
          MapotempoListFragment firstFragment = new MapotempoListFragment();
          firstFragment.setArguments(getArguments());
          getFragmentManager().beginTransaction().replace(R.id.mt_layout_fragment_container, firstFragment).commit();
        }
      }
    });
  }

  int p;
  @Override
  public void onMtRouteOptimizeProgress(int progress)
  {
    p = progress;
    getActivity().runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        mOptimProgressBar.setProgress(p);
      }
    });
  }
}
