#pragma once

#include "online_absent_fetcher.hpp"
#include "route.hpp"
#include "router.hpp"
#include "router_delegate.hpp"

#include "base/thread.hpp"

#include "std/condition_variable.hpp"
#include "std/map.hpp"
#include "std/mutex.hpp"
#include "std/shared_ptr.hpp"
#include "std/string.hpp"
#include "std/unique_ptr.hpp"

namespace routing
{

/// Dispatches an optimisation calculation on a worker thread
class AsyncOptimizer final
{
public:
  /// Callback takes ownership of passed route.
  using TOptimReadyCallback = function<void(std::pair<std::list<size_t>, size_t> &result, IRouter::ResultCode)>;

  /// AsyncOptimizer is a wrapper class to run routing routines in the different thread
  AsyncOptimizer();
  ~AsyncOptimizer();

  /// Sets a synchronous router, current route calculation will be cancelled
  /// @param router pointer to a router implementation
  /// @param fetcher pointer to a online fetcher
  void SetRouter(unique_ptr<IRouter> && router);//, unique_ptr<IOnlineFetcher> && fetcher);

  /// Main method to calulate new route from startPt to finalPt with start direction
  /// Processed result will be passed to callback. Callback will called at GUI thread.
  ///
  /// @param points target points for route
  /// @param readyCallback function to return routing result
  /// @param progressCallback function to update the router progress
  /// @param timeoutSec timeout to cancel routing. 0 is infinity.
  void OptimizeRoute(vector<m2::PointD> &points, TOptimReadyCallback const & readyCallback,
                      RouterDelegate::TProgressCallback const & progressCallback,
                      uint32_t timeoutSec);


  /// Interrupt routing and clear buffers
  void ClearState();

private:
  /// Worker thread function
  void ThreadFunc();

  /// This function is called in worker thread
  void OptimizeRoute();

  void ResetDelegate();

  void LogCode(IRouter::ResultCode code, double const elapsedSec);

  /// Blocks callbacks when routing has been cancelled
  class RouterDelegateProxy
  {
  public:
    RouterDelegateProxy(TOptimReadyCallback const & onReady,
                        RouterDelegate::TPointCheckCallback const & onPointCheck,
                        RouterDelegate::TProgressCallback const & onProgress,
                        uint32_t timeoutSec);

    void OnReady(std::pair<std::list<size_t>, size_t> &result, IRouter::ResultCode resultCode);
    void Cancel();

    RouterDelegate const & GetDelegate() const { return m_delegate; }

  private:
    void OnProgress(float progress);
    void OnPointCheck(m2::PointD const & pt);

    mutex m_guard;
    TOptimReadyCallback const m_onReady;
    RouterDelegate::TPointCheckCallback const m_onPointCheck;
    RouterDelegate::TProgressCallback const m_onProgress;
    RouterDelegate m_delegate;
  };

private:
  mutex m_guard;

  /// Thread which executes routing calculation
  threads::SimpleThread m_thread;
  condition_variable m_threadCondVar;
  bool m_threadExit;
  bool m_hasRequest;

  /// Current request parameters
  bool m_clearState;
  shared_ptr<RouterDelegateProxy> m_delegate;
  shared_ptr<IRouter> m_router;

  vector<m2::PointD> m_points;
};

}  // namespace routing
