#pragma once

#include "map/bookmark.hpp"
#include "map/bookmark_manager.hpp"

#include "std/function.hpp"
#include "std/unique_ptr.hpp"

#include "routing/async_optimizer.hpp"

//#include "base/mutex.hpp"

// Have a JNI mirror into MTRoutePLanningMananger.java
  enum PlanningManagerStatus {
    FOLLOW_PLANNING = 0,
    FOLLOW_EMPTY_PLANNING = 1,
    CLOSE = 2
  };

/**
 * TODO JMF à améliorer.
 * La classe MTRoutePlanningManager permet de gerer une liste de bookmark (MTRoutePlanning).
 * En héritant du BookmarkManager elle gere la suppression et le déplacement
 * du signet dans sa liste, ainsi que le chargement des bookmarks depuis les
 * kml au chargement de l'application et leurs affichage sur la carte.
 */
class MTRoutePlanningManager : public BookmarkManager
{
  using TOptimisationFinishFn = function<void (bool)>;
  /// Called to notify UI that mapotempo routing is deactivate;
  using TOptimisationProgessFn = function<void (float)>;

public :

  static const size_t INVALIDE_VALUE;

  MTRoutePlanningManager(Framework & f) : BookmarkManager(f),
  m_framework(f),
  m_indexCurrentBmCat(INVALIDE_VALUE),
  m_optimizer(nullptr),
  m_optimisationFinishFn(nullptr),
  m_optimisationProgressFn(nullptr){};

  ~MTRoutePlanningManager() {};

  void SetRouter(unique_ptr<routing::IRouter> && router);

  // Route status manager
  void StopFollow();
  PlanningManagerStatus FollowPlanning(size_t indexBmCat);
  PlanningManagerStatus GetStatus();

  // Get current bookmark planning
  size_t GetFollowedBookmarkCategoryID() const {return m_indexCurrentBmCat;}
  BookmarkCategory * GetFollowedBookmarkCategory();
  bool CheckCurrentBookmarkStatus(const double & curLat, const double & curLon);

  // Optimisation
  bool optimiseBookmarkCategory(size_t indexBmCat);
  void stopCurrentOptimisation();
  void SetOptimisationListeners(TOptimisationFinishFn const & finishListener,
                                TOptimisationProgessFn const & progressListener);


private :
  static const size_t MT_DISTANCE_BOOKMARK_DONE = 50;

  Framework &m_framework;

  size_t m_indexCurrentBmCat;

  unique_ptr<routing::AsyncOptimizer> m_optimizer;
  TOptimisationFinishFn m_optimisationFinishFn;
  TOptimisationProgessFn m_optimisationProgressFn;

  // Guard mutex categoryManager and category
  // This is a guard on the current bookmark category to prevent another thread from deleting during optimization.
  mutable threads::Mutex m_routeListManagerMutex;

public :
  // Override BookmarkManager virtual method
  bool DeleteBmCategory(size_t index) override;
  size_t CreateBmCategory(string const & name) override;
  void LoadBookmark(string const & filePath) override;

private :
  void save_status();
};
