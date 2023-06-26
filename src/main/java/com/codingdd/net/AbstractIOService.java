package com.codingdd.net;

import com.codingdd.net.Statistics.StatisticsV2;

public abstract class AbstractIOService implements IOService{
    
    private StatisticsV2 statistics;
    
    @Override
    public void setStatistics(StatisticsV2 statistics) {
        this.statistics = statistics;
    }

    @Override
    public StatisticsV2 getStatistics() {
        return this.statistics;
    }

}
