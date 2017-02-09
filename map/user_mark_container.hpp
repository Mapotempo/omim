#pragma once

#include "map/user_mark.hpp"

#include "drape_frontend/user_marks_provider.hpp"

#include "geometry/point2d.hpp"
#include "geometry/rect2d.hpp"
#include "geometry/any_rect2d.hpp"

#include "std/deque.hpp"
#include "std/bitset.hpp"

#include "std/noncopyable.hpp"
#include "std/unique_ptr.hpp"

class Framework;

enum class UserMarkType
{
  SEARCH_MARK,
  API_MARK,
  DEBUG_MARK,
  BOOKMARK_MARK
};

enum class UserMarkEventType
{
  CREATE_MARK,
  DELETE_MARK,
  CLEAR_MARK,
  REVERSE_MARK,
  MOVE_MARK
};

struct UserMarkEvent{
  size_t old_index;
  size_t new_index;
  UserMarkEventType actionType;
};

class UserMarksController
{
public:
  virtual size_t GetUserMarkCount() const = 0;
  virtual UserMarkType GetType() const = 0;
  virtual void SetIsDrawable(bool isDrawable) = 0;
  virtual void SetIsVisible(bool isVisible) = 0;

  virtual UserMark * CreateUserMark(m2::PointD const & ptOrg) = 0;
  virtual UserMark const * GetUserMark(size_t index) const = 0;
  virtual UserMark * GetUserMarkForEdit(size_t index) = 0;
  virtual void DeleteUserMark(size_t index) = 0;
  virtual void Clear(size_t skipCount = 0) = 0;
  virtual void Update() = 0;
  virtual void ReverseUserMarks() = 0;
  virtual bool MoveUserMarkOrder(size_t oldIndex, size_t newIndex) = 0;
  virtual bool UserMarksOrders(std::list<size_t> list) = 0;
};

class UserMarkContainer : public df::UserMarksProvider
                        , protected UserMarksController
                        , private noncopyable
{
public:
  using TUserMarksList = deque<unique_ptr<UserMark>>;
  using TUserMarksContainerListener = function<void (UserMarkEvent userMarkEvent)>;


  UserMarkContainer(double layerDepth, UserMarkType type, Framework & fm);
  virtual ~UserMarkContainer();

  // If not found mark on rect result is nullptr
  // If mark is found in "d" return distance from rect center
  // In multiple select choose mark with min(d)
  UserMark const * FindMarkInRect(m2::AnyRectD const & rect, double & d) const;

  static void InitStaticMarks(UserMarkContainer * container);
  static PoiMarkPoint * UserMarkForPoi();
  static MyPositionMarkPoint * UserMarkForMyPostion();

  /// never save reference on UserMarksController
  UserMarksController & RequestController();
  void ReleaseController();

  /// Render info
  size_t GetUserPointCount() const override;
  df::UserPointMark const * GetUserPointMark(size_t index) const override;

  size_t GetUserLineCount() const override;
  df::UserLineMark const * GetUserLineMark(size_t index) const override;

  float GetPointDepth() const;

  bool IsVisible() const;
  bool IsDrawable() const override;
  size_t GetUserMarkCount() const override;
  UserMark const * GetUserMark(size_t index) const override;
  UserMarkType GetType() const override final;

  bool MoveUserMarkOrder(size_t oldIndex, size_t newIndex) override;
  bool UserMarksOrders(std::list<size_t> list) override;

  // Mapotempo Usage : Event Listener
  size_t AddEventListener(TUserMarksContainerListener const & userMarksEventListener)
  {
    m_eventListenersKeyCounter++;
    m_eventListeners[m_eventListenersKeyCounter] = userMarksEventListener;
    return m_eventListenersKeyCounter;
  };

  bool DeleteEventListener(size_t key)
  {
    m_eventListeners.erase(key);
    return true;
  };

  void notify(UserMarkEvent userMarkEvent)
  {
    for(std::map<size_t,TUserMarksContainerListener>::iterator it=m_eventListeners.begin(); it!=m_eventListeners.end(); ++it)
    {
      it->second(userMarkEvent);
    }
  }

protected:
  /// UserMarksController implementation
  UserMark * CreateUserMark(m2::PointD const & ptOrg) override;
  UserMark * GetUserMarkForEdit(size_t index) override;
  void DeleteUserMark(size_t index) override;
  void Clear(size_t skipCount = 0) override;
  void SetIsDrawable(bool isDrawable) override;
  void SetIsVisible(bool isVisible) override;
  void Update() override;
  void ReverseUserMarks() override;

  virtual UserMark * AllocateUserMark(m2::PointD const & ptOrg) = 0;

  Framework & m_framework;

private:
  bitset<4> m_flags;
  double m_layerDepth;
  TUserMarksList m_userMarks;
  UserMarkType m_type;
  size_t m_eventListenersKeyCounter;
  std::map<size_t, TUserMarksContainerListener> m_eventListeners;
};

class SearchUserMarkContainer : public UserMarkContainer
{
public:
  SearchUserMarkContainer(double layerDepth, Framework & framework);

protected:
  UserMark * AllocateUserMark(m2::PointD const & ptOrg) override;
};

class DebugUserMarkContainer : public UserMarkContainer
{
public:
  DebugUserMarkContainer(double layerDepth, Framework & framework);

protected:
  UserMark * AllocateUserMark(m2::PointD const & ptOrg) override;
};

class ApiUserMarkContainer : public UserMarkContainer
{
public:
  ApiUserMarkContainer(double layerDepth, Framework & framework);

protected:
  UserMark * AllocateUserMark(m2::PointD const & ptOrg) override;
};
