package me.offeex.exethirteen.aidl;

import me.offeex.exethirteen.aidl.IShadowsocksServiceCallback;

interface IShadowsocksService {
  int getState();
  String getProfileName();

  void registerCallback(in IShadowsocksServiceCallback cb);
  void startListeningForBandwidth(in IShadowsocksServiceCallback cb, long timeout);
  oneway void stopListeningForBandwidth(in IShadowsocksServiceCallback cb);
  oneway void unregisterCallback(in IShadowsocksServiceCallback cb);
}
