package me.offeex.exethirteen.aidl;

import me.offeex.exethirteen.aidl.TrafficStats;

oneway interface IShadowsocksServiceCallback {
  void stateChanged(int state, String profileName, String msg);
  void trafficUpdated(String profileId, in TrafficStats stats);
  // Traffic data has persisted to database, listener should refetch their data from database
  void trafficPersisted(String profileId);
}
