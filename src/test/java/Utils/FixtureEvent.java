package utils;

import node.BaseNode;
import underlay.packets.Event;

public class FixtureEvent implements Event {
  @Override
  public boolean actionPerformed(BaseNode hostNode) {
    return true;
  }

  @Override
  public String logMessage() {
    return null;
  }

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }
}
