#pragma once

#include "osrm_engine.hpp"
#include "router.hpp"
#include "routing/routing_mapping.hpp"
#include "routing/cross_mwm_router.hpp"

#include "std/string.hpp"
#include "std/vector.hpp"
#include "std/map.hpp"

namespace routing
{
class CrossMatrix
{
struct TRoutingNodesByMapping {
  TRoutingNodes mNodes;
  vector<int> mOriginalPosition;
  Index::MwmId mMwmId;

  TRoutingNodesByMapping(Index::MwmId mwmId): mNodes(), mOriginalPosition(), mMwmId(mwmId) {};

  void addNode(FeatureGraphNode &feature, int originalPosition)
  {
    mNodes.push_back(feature);
    mOriginalPosition.push_back(originalPosition);
  };
};

using TRoutingNodesByMappingContainer = map<Index::MwmId, TRoutingNodesByMapping>;

public :
  explicit CrossMatrix(RoutingIndexManager & indexManager)
  : m_indexManager(indexManager), mStartGraphNodes(), mFinalGraphNodes(), mWeights(), validity(false){};

  void setStartNodes(TRoutingNodes &startGraphNodes);
  void setFinalNodes(TRoutingNodes &finalGraphNodes);

  /*!
  * \brief CalculateCrossMwmPath function for calculating path through several maps.
  * \return NoError if the path exists, error code otherwise.
  */
  IRouter::ResultCode CalculateCrossMwmMatrix(vector<EdgeWeight> &res, RouterDelegate const & delegate);
private :

  bool validity;
  mutable RoutingIndexManager m_indexManager;

  vector<EdgeWeight> mWeights;

  TRoutingNodes mStartGraphNodes;
  TRoutingNodes mFinalGraphNodes;

  TRoutingNodesByMappingContainer mStartNodesByMappingContainer;
  TRoutingNodesByMappingContainer mFinalNodesByMappingContainer; 
};
}  // namespace routing
