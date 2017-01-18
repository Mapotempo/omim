#include "cross_mwm_matrix.hpp"

namespace routing
{

namespace
{
  double constexpr kOptimPointsFoundProgress = 25.0f;
  double constexpr kOptimMatrixFoundProgress = 60.0f;
} //  namespace


  void CrossMatrix::setStartNodes(TRoutingNodes &startGraphNodes)
  {
    mStartGraphNodes = startGraphNodes;
    for(size_t i =0; i < mStartGraphNodes.size(); i++)
    {
      TRoutingNodesByMappingContainer::iterator lb = mStartNodesByMappingContainer.find(mStartGraphNodes[i].mwmId);
      if(lb != mStartNodesByMappingContainer.end() && !(mStartNodesByMappingContainer.key_comp()(mStartGraphNodes[i].mwmId, lb->first)))
      {
        lb->second.addNode(mStartGraphNodes[i], i);
      }
      else
      {
        auto newLb = mStartNodesByMappingContainer.insert(
          std::pair<Index::MwmId, TRoutingNodesByMapping>(
            mStartGraphNodes[i].mwmId,
            TRoutingNodesByMapping(mStartGraphNodes[i].mwmId)));
        newLb.first->second.addNode(mStartGraphNodes[i], i);
      }
    }
    validity = false;
  }

  void CrossMatrix::setFinalNodes(TRoutingNodes &finalGraphNodes)
  {
    mFinalGraphNodes = finalGraphNodes;
    for(size_t i =0; i < mFinalGraphNodes.size(); i++)
    {
      TRoutingNodesByMappingContainer::iterator lb = mFinalNodesByMappingContainer.find(mFinalGraphNodes[i].mwmId);
      if(lb != mFinalNodesByMappingContainer.end() && !(mFinalNodesByMappingContainer.key_comp()(mFinalGraphNodes[i].mwmId, lb->first)))
      {
        lb->second.addNode(mFinalGraphNodes[i], i);
      }
      else
      {
        auto newLb = mFinalNodesByMappingContainer.insert(
          std::pair<Index::MwmId, TRoutingNodesByMapping>(
            mFinalGraphNodes[i].mwmId,
            TRoutingNodesByMapping(mStartGraphNodes[i].mwmId)));
        newLb.first->second.addNode(mFinalGraphNodes[i], i);
      }
    }
    validity = false;
  }

  IRouter::ResultCode CrossMatrix::CalculateCrossMwmMatrix(vector<EdgeWeight> &res, RouterDelegate const & delegate)
  {
    if(!(mStartGraphNodes.size() > 0))
      return IRouter::ResultCode::StartPointNotFound;

    if(!(mFinalGraphNodes.size() > 0))
      return IRouter::ResultCode::EndPointNotFound;

    if(validity)
    {
      res = mWeights;
      return IRouter::ResultCode::NoError;
    }

    // 1. Feed mother matrix with mwm nodes matrix
    mWeights.resize(mStartGraphNodes.size() * mFinalGraphNodes.size());
    fill(mWeights.begin(), mWeights.end(), INT_MAX);

    for(auto startNodesByMapping : mStartNodesByMappingContainer)
    {
      if (delegate.IsCancelled())
        return IRouter::Cancelled;

      TRoutingNodesByMappingContainer::iterator finalNodesByMapping = mFinalNodesByMappingContainer.find(startNodesByMapping.first);
      if(finalNodesByMapping != mFinalNodesByMappingContainer.end())
      {
        // 1.1 Calcul single mwm matrix.
        TRoutingMappingPtr startMapping = m_indexManager.GetMappingById(startNodesByMapping.first);
        MappingGuard startMappingGuard(startMapping);
        UNUSED_VALUE(startMappingGuard);

        if (!startMapping->IsValid())
          return IRouter::ResultCode::Cancelled;
        vector<EdgeWeight> tmp;
        size_t counter = 0;
        FindWeightsMatrix(startNodesByMapping.second.mNodes, finalNodesByMapping->second.mNodes, startMapping->m_dataFacade, tmp);

        // 1.2 Feed the mother matrix.
        LOG(LDEBUG, ("matrix for region : ", startNodesByMapping.first.GetInfo()->GetLocalFile()));
        for(size_t i = 0; i < finalNodesByMapping->second.mNodes.size(); i++)
        {
          for(size_t j = 0; j < startNodesByMapping.second.mNodes.size(); j++)
          {
            size_t original_position = (finalNodesByMapping->second.mOriginalPosition[i] * mFinalGraphNodes.size()) + startNodesByMapping.second.mOriginalPosition[j];
            mWeights[original_position] = tmp[counter];
            counter++;
            LOG(LDEBUG, (i, "-", j,
                         " | ", finalNodesByMapping->second.mOriginalPosition[i], "-", startNodesByMapping.second.mOriginalPosition[j],
                         " | ", original_position,
                         " | ", tmp[counter]));
          }
          LOG(LDEBUG, ("-----------------------------------------------"));
        }
      }
    }

    // 2. Check and complet mother matrix
    size_t counter = 0;
    for(size_t i = 0; i < mStartGraphNodes.size(); i++)
    {
      for(size_t j = 0; j < mFinalGraphNodes.size(); j++)
      {
        if (delegate.IsCancelled())
          return IRouter::Cancelled;

        delegate.OnProgress(kOptimPointsFoundProgress +
                            counter * kOptimMatrixFoundProgress / (mFinalGraphNodes.size() * mStartGraphNodes.size()));

        if(mWeights[counter] == INT_MAX)
        {
          TRoutingNodes start, final;
          start.push_back(mStartGraphNodes[i]);
          final.push_back(mFinalGraphNodes[j]);
          double crossCost = 0;
          TCheckedPath finalPath;
          RouterDelegate localDelegate;

          IRouter::ResultCode status = CalculateCrossMwmPath(start, final, m_indexManager, crossCost,
                                                       localDelegate, finalPath);

          // Pour ne pas être bloquant pour le moment si on ne trouve pas le cross mwm path on prend la distance en directe (vol d'oiseau).
          if(status == IRouter::ResultCode::NoError)
            mWeights[counter] = crossCost;
          else
            mWeights[counter] = MercatorBounds::DistanceOnEarth(mStartGraphNodes[i].segmentPoint, mFinalGraphNodes[j].segmentPoint);
        }
        LOG(LDEBUG, (counter, " ", i, "-", j , " : ", mWeights[counter]));
        counter++;
      }
      LOG(LDEBUG, ("-----------------------------------------------"));
    }

    res = mWeights;
    validity = true;
    return IRouter::ResultCode::NoError;
  }
}  // namespace routing
