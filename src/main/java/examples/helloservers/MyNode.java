package examples.helloservers;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import metrics.MetricsCollector;
import network.MiddleLayer;
import network.packets.Event;
import node.BaseNode;

/**
 * MyNode is a basenode to be fixture node for the hello servers simulation.
 */
public class MyNode implements BaseNode {
  private static final String MESSAGE_COUNT = "MessageCnt";
  private UUID selfId;
  private ArrayList<UUID> allId;
  private MiddleLayer network;
  private MetricsCollector metricsCollector; // TODO: enable metrics

  MyNode(UUID selfId, MiddleLayer network, MetricsCollector metricsCollector) {
    this.selfId = selfId;
    this.network = network;
    this.metricsCollector = metricsCollector;
  }

  public MyNode() {

  }


  @Override
  public void onCreate(ArrayList<UUID> allId) {
    this.allId = allId;
    network.ready();
  }

  @Override
  public void onStart() {
    this.sendNewMessage("Hello");
  }

  /**
   * Sends message to a random node.
   *
   * @param msg msg to send
   */
  public void sendNewMessage(String msg) {
    if (allId.isEmpty()) {
      return;
    }
    Random rand = new Random();
    int ind = rand.nextInt(allId.size());
    SendHello helloMessage = new SendHello(msg, selfId, allId.get(ind));
    network.send(allId.get(ind), helloMessage);
  }

  @Override
  public void onStop() {
  }

  @Override
  public void onNewMessage(UUID originId, Event msg) {
    try {
      Random rand = new Random();
      Thread.sleep(rand.nextInt(1000));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    msg.actionPerformed(this);
  }

  @Override
  public BaseNode newInstance(UUID selfId, String nameSpace, MiddleLayer network, MetricsCollector metricsCollector) {
    return new MyNode(selfId, network, metricsCollector);
  }
}
