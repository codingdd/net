package com.codingdd.net;

import com.codingdd.net.Statistics.StatisticsV2;

public interface IOService {
    public void setStatistics(StatisticsV2 statistics);

    public StatisticsV2 getStatistics();
}
