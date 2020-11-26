package lightchain.events;

import Node.BaseNode;
import Underlay.packets.Event;
import lightchain.LightChainNode;

import java.util.UUID;

public class CollectTransactionsEvent implements Event {

  private UUID requester;
  private Integer requiredNumber;

  public CollectTransactionsEvent(UUID requester, Integer requiredNumber) {
    this.requester = requester;
    this.requiredNumber = requiredNumber;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {

    LightChainNode node = (LightChainNode) hostNode;
    node.collectTransactions(this.requester, this.requiredNumber);

    return true;
  }

  @Override
  public String logMessage() {
    return null;
  }
}
