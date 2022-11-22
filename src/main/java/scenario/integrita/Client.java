package scenario.integrita;

import java.util.ArrayList;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import metrics.MetricsCollector;
import modules.logger.Logger;
import modules.logger.OperaLogger;
import network.MiddleLayer;
import network.packets.Event;
import node.BaseNode;
import node.Identifier;
import scenario.integrita.events.Push;
import scenario.integrita.historytree.HistoryTreeNode;
import scenario.integrita.user.User;


/**
 * Integrita client implementation.
 */
public class Client extends User implements BaseNode {
  private Logger logger;
  Identifier id;
  MiddleLayer network;
  ArrayList<Identifier> ids; // all ids inclding self

  public Client() {

  }

  /**
   * Constructor of Client.
   *
   * @param selfId identifier of the node.
   * @param network network of the node.
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to expose internal state of MiddleLayer")
  public Client(Identifier selfId, MiddleLayer network) {
    this.id = selfId;
    this.network = network;
    this.logger = OperaLogger.getLoggerForNodeComponent(this.getClass().getCanonicalName(), selfId, "integrita_client");
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to expose internal state of allId")
  public void onCreate(ArrayList<Identifier> allId) {
    this.ids = allId;
    this.network.ready();
  }

  @Override
  public void onStart() {
    for (Identifier receiver : ids) {
      if (receiver.equals(this.id)) {
        continue;
      }
      // create an empty node
      Push pushMsg = new Push(new HistoryTreeNode(), "Hello");
      network.send(receiver, pushMsg);
    }
  }

  @Override
  public void onStop() {

  }

  @Override
  public void onNewMessage(Identifier originId, Event msg) {
    this.logger.info("received message from {} with content {}", originId, msg.logMessage());
  }

  @Override
  public BaseNode newInstance(Identifier selfId, String nameSpace, MiddleLayer network, MetricsCollector metrics) {
    return new Client(selfId, network);
  }
}
