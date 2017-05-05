package com.graphhopper.routing.weighting;

import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.FootFlagEncoder;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

public class CrowdWeighting extends PriorityWeighting {

    private int testPoint = 0;
    private final double minFactor;

    public CrowdWeighting(FlagEncoder encoder, PMap pMap) {
        super(encoder, pMap);
        double maxPriority = 1; // BEST / BEST
        minFactor = 1 / (0.5 + maxPriority);
    }

    @Override
    public double getMinWeight(double distance) {
        return minFactor * super.getMinWeight(distance);
    }

    @Override
    public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
        double weight = super.calcWeight(edgeState, reverse, prevOrNextEdgeId);

        if (Double.isInfinite(weight))
            return Double.POSITIVE_INFINITY;

        // Adjust weight
        if (edgeState.getName().equals("Prospect Avenue")) {
            return weight / (0.5 + flagEncoder.getDouble(edgeState.getFlags(), KEY)) * 3;
        }
        if (edgeState.getName().equals("Olden Street")) {
            return weight / (0.5 + flagEncoder.getDouble(edgeState.getFlags(), KEY)) * 3;
        }
        if (edgeState.getName().equals("sdfas")) {
            return weight / (0.5 + flagEncoder.getDouble(edgeState.getFlags(), KEY)) * 3;
        }

        return weight / (0.5 + flagEncoder.getDouble(edgeState.getFlags(), KEY));
    }

    @Override
    public void setData(int test) {
        testPoint = test;
    }

    @Override
    public String getName()
    {
        return "fastest";
    }

}