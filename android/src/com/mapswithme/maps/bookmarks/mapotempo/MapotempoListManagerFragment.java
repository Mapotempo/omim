package com.mapswithme.maps.bookmarks.mapotempo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mapswithme.maps.Framework;
import com.mapswithme.maps.R;
import com.mapswithme.maps.bookmarks.ChooseBookmarkCategoryFragment;
import com.mapswithme.maps.bookmarks.data.BookmarkCategory;
import com.mapswithme.maps.bookmarks.data.BookmarkManager;
import com.mapswithme.maps.bookmarks.data.MTRouteListManager;
import com.mapswithme.util.ThemeUtils;
import com.mapswithme.util.concurrency.UiThread;
import com.mapswithme.util.sharing.SharingHelper;

public class MapotempoListManagerFragment extends Fragment implements Framework.MTRouteOptimize
{
  private BookmarkCategory mCategory;
  private boolean isActivate = false;
  private ImageView mListManagerActivate;

  private ProgressBar mOptimProgressBar;
  private boolean mOptimInProgress = false;
  private int mLastProgress = 0;
  private boolean mLastOptimReturnStatus = false;

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
      if(MTRouteListManager.INSTANCE.getStatus() && (mCategory.getId() == MTRouteListManager.INSTANCE.getCurrentBookmark().getCategoryId()))
      {
        isActivate = true;
      }
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

    ImageView listManagerOptim = (ImageView) view.findViewById(R.id.mt_list_manager_optim);
    listManagerOptim.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        if(!mOptimInProgress)
        {
          mOptimProgressBar.setProgress(0);
          mOptimProgressBar.setVisibility(View.VISIBLE);
          MTRouteListManager.nativeOptimiseBookmarkCategory(mCategory.getId());
          mOptimInProgress = true;
        }
      }
    });

    mListManagerActivate = (ImageView) view.findViewById(R.id.mt_list_manager_activate);
    mListManagerActivate.setImageResource(ThemeUtils.isNightTheme() ? isActivate ? R.drawable.ic_bookmark_show_night
                                                                                 : R.drawable.ic_bookmark_hide_night
                                                                    : isActivate ? R.drawable.ic_bookmark_show
                                                                                 : R.drawable.ic_bookmark_hide);
    mListManagerActivate.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        boolean status = false;

        if (mCategory.getBookmark(0) != null
            && !isActivate)
          status = MTRouteListManager.INSTANCE.initRoutingManager(mCategory.getId(), 0);
        else
          MTRouteListManager.INSTANCE.stopRoutingManager();

        mListManagerActivate.setImageResource(ThemeUtils.isNightTheme() ? status ? R.drawable.ic_bookmark_show_night
                                                                                 : R.drawable.ic_bookmark_hide_night
                                                                        : status ? R.drawable.ic_bookmark_show
                                                                                 : R.drawable.ic_bookmark_hide);
        isActivate = status;
      }
    });

    ImageView listManagerShare = (ImageView) view.findViewById(R.id.mt_list_manager_share);
    listManagerShare.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        SharingHelper.shareBookmarksCategory(getActivity(), mCategory.getId());
      }
    });
    return view;
  }

  public boolean onBackPressed()
  {
    boolean res = false;
    if(mOptimInProgress)
    {
      MTRouteListManager.nativeStopCurrentOptimisation();
      mOptimInProgress = false;
      mOptimProgressBar.setVisibility(View.GONE);

      res = true;
    }

    return res;
  }

  // Attention ces "callback" sont appellés depuis un thread.
  // On ne modifie l'uix que sur le thread ui !!!!
  @Override
  public void onMtRouteOptimizeFinish(boolean status)
  {
    mLastOptimReturnStatus = status;
    UiThread.run(new Runnable()
    {
      @Override
      public void run()
      {
        if(!mLastOptimReturnStatus)
        {
          Context context = getContext();
          CharSequence text = "Optimisation fail";
          int duration = Toast.LENGTH_SHORT;

          Toast toast = Toast.makeText(context, text, duration);
          toast.show();
        }

        mOptimInProgress = false;
        mOptimProgressBar.setVisibility(View.GONE);
        // Sub Fragment rebuild
        if (mLastOptimReturnStatus && getView().findViewById(R.id.mt_layout_fragment_container) != null
            && mCategory != null)
        {
          MapotempoListFragment firstFragment = new MapotempoListFragment();
          firstFragment.setArguments(getArguments());
          getFragmentManager().beginTransaction().replace(R.id.mt_layout_fragment_container, firstFragment).commit();
        }
      }
    });
  }

  @Override
  public void onMtRouteOptimizeProgress(int progress)
  {
    mLastProgress = progress;
    UiThread.run(new Runnable()
    {
      @Override
      public void run()
      {
        mOptimProgressBar.setProgress(mLastProgress);
      }
    });
  }
}
