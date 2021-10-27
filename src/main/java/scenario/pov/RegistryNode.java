package scenario.pov;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import metrics.SimulatorGauge;
import metrics.SimulatorHistogram;
import node.BaseNode;
import org.apache.log4j.Logger;
import scenario.pov.Block;
import scenario.pov.LightChainNode;
import scenario.pov.Transaction;
import scenario.pov.events.DeliverLatestBlockEvent;
import scenario.pov.events.DeliverTransactionsEvent;
import underlay.MiddleLayer;
import underlay.packets.Event;

/**
 *
 */
public class RegistryNode implements BaseNode {

  final int blockIterations = 50;
  final int numValidators = 1;
  /**
   * The registry node represents the underlying skip graph overlay of the RegistryNode, where it keeps the
   * list of inserted transactions and blocks, and receive requests
   * from different nodes to submit and retrieve transactions and blocks.
   */

  private List<UUID> allID;
  private UUID uuid;
  private MiddleLayer network;
  private Logger logger;
  private Integer maximumHeight;
  private Integer totalTransactionCount;
  // only for registry node
  private List<Transaction> availableTransactions;
  private List<Block> insertedBlocks;
  private Map<Integer, Integer> heightToUniquePrevCount;
  private Map<Integer, Map<UUID, Integer>> heightToUniquePrev;

  /**
   * Constructor of Registry Node.
   *
   * @param uuid    ID of the node
   * @param network used to communicate with other nodes
   */
  public RegistryNode(UUID uuid, MiddleLayer network) {
    this.uuid = uuid;
    this.network = network;
    this.logger = Logger.getLogger(LightChainNode.class.getName());

    // for registry nodes
    this.availableTransactions = new ArrayList<>();
    this.insertedBlocks = new ArrayList<>();
    this.heightToUniquePrev = new HashMap<>();
    this.heightToUniquePrevCount = new HashMap<>();
    this.maximumHeight = 0;
    this.totalTransactionCount = 0;
  }

  public RegistryNode() {

  }

  /**
   * On the creation of a LightChain node, first the node checks if it is a registry node or not. A registry node
   * is the node of UUID placed in index 0 of allID list. This convention is pre-defined for LightChainNode. So the
   * node checks in the beginning if its UUID matches the UUID of the 0-index element of allID. If it matches then it
   * sets isRegistry variable to true, or false otherwise. If the node is the registry node, the it appends the genesis
   * block to its the list of blocks.
   *
   * @param allID the IDs of type UUID for all the nodes in the cluster
   */
  @Override
  public void onCreate(ArrayList<UUID> allID) {

    logger.info("Node " + this.uuid + " has been created.");

    this.allID = allID;

    // ensure that number of validators is small than number of nodes
    if (numValidators > this.allID.size() - 1) {
      try {
        throw new Exception("Number of validators must be smaller than number of nodes. NumValidators= " + numValidators + ", numNodes= " + (this.allID.size() - 1));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    double[] linespace = new double[this.allID.size() * this.blockIterations];

    for (int i = 0; i < linespace.length; i++) {
      linespace[i] = i;
    }

    SimulatorGauge.register("transaction_count");
    SimulatorGauge.register("block_height_per_time");
    SimulatorHistogram.register("block_height_histogram", linespace);
    SimulatorHistogram.register("unique_blocks_per_height", linespace);

    new Thread(() -> {
      monitorBlockHeight();
    }).start();

    logger.info("[Registry] The Registry node is " + this.uuid);
    this.appendBlock(new Block(UUID.randomUUID(), 0, this.uuid, UUID.randomUUID(), new ArrayList<>(), new ArrayList<>()));
    logger.info("[Registry] Genesis Block has been appended");


    network.ready();
  }

  /**
   * starts the LightChain Node by starting its iteration to insert transactions and mine blocks.
   */
  @Override
  public void onStart() {
  }

  /**
   * Stops the LightChain Node
   */
  @Override
  public void onStop() {

  }

  /**
   * Performs the action of the message by passing an instance of this LightChain Node
   *
   * @param originID the ID of the sender node
   * @param msg      the content of the message
   */
  @Override
  public void onNewMessage(UUID originID, Event msg) {
    msg.actionPerformed(this);
  }

  /**
   * @param selfID  the ID of the new node
   * @param network communication network for the new node
   * @return a new instance of LightChainNode
   */
  @Override
  public BaseNode newInstance(UUID selfID, MiddleLayer network) {
    return new RegistryNode(selfID, network);
  }

  /**
   * This function randomly chooses validators of a certain transaction using the Reservoir Sampling Algorithm
   * see https://www.geeksforgeeks.org/reservoir-sampling/
   *
   * @return a list of UUID's of randomly chosen nodes from the network
   */
  public List<UUID> getValidators() {

    logger.info("Fetching validators for node " + this.uuid);

    // add the first numValidators nodes
    List<Integer> randomIndexes = new ArrayList<>();
    for (int i = 1; i <= this.numValidators; ++i) {
      randomIndexes.add(i);
    }

    Random rand = new Random();
    for (int i = this.numValidators + 1; i < this.allID.size(); ++i) {
      int j = rand.nextInt(i + 1);
      if (j < this.numValidators) randomIndexes.set(j, i);
    }

    List<UUID> validators = new ArrayList<>();
    for (Integer index : randomIndexes) {
      validators.add(this.allID.get(index));
    }

    return validators;
  }

  /**
   * @return true if this node is a registry node, false otherwise
   */
  public boolean isRegistry() {
    return true;
  }

  /*
  These functions below belong solely to the registry node.
   */

  /**
   * This function is invoked from a node to insert a transaction after it has been validated.
   * It simply appends the transaction to the list of available transactions for collection. It also provides locking
   * to ensure the correctness of adding and removing transactions.
   *
   * @param transaction transaction to be inserted into the network
   */
  public void addTransaction(Transaction transaction) {

    SimulatorGauge.inc("transaction_count", this.uuid);

    logger.info("[Registry] new transaction inserted into network.");

    //  this.transactionLock.writeLock().lock();

    this.availableTransactions.add(transaction);
    this.totalTransactionCount += 1;
    logger.info("[Registry] currently " + this.availableTransactions.size() + " transactions are available");
    logger.info("[Registry] total number of transactions inserted so far " + this.totalTransactionCount);

    //  this.transactionLock.writeLock().unlock();
  }

  /**
   * This function is called from a node that is attempting to collect transaction to create a block. It takes a set
   * of transactions and returns a list of then.
   *
   * @param requester      ID of node requesting transactions
   * @param requiredNumber the required number of transactions
   * @return a list of transactions matching the number required
   */
  public List<Transaction> collectTransactions(UUID requester, Integer requiredNumber) {
    //   this.transactionLock.writeLock().lock();

    List<Transaction> requestedTransactions = new ArrayList<>();
    // a failed collection attempts
    if (this.availableTransactions.size() < requiredNumber) {

      logger.info("[Registry] number of available transactions is less than requested by node " + requester + ", required number: " + requiredNumber + ", available number: " + this.availableTransactions.size());

      //    this.transactionLock.writeLock().unlock();

      network.send(requester, new DeliverTransactionsEvent(requestedTransactions));

      return requestedTransactions;
    }

    for (int i = 0; i < this.availableTransactions.size(); ++i) {
      requestedTransactions.add(this.availableTransactions.get(i));
    }

    List<Transaction> temporary = new ArrayList<>();
    for (int i = requiredNumber; i < this.availableTransactions.size(); ++i) {
      temporary.add(this.availableTransactions.get(i));
    }
    this.availableTransactions = temporary;

    //  this.transactionLock.writeLock().unlock();

    SimulatorGauge.dec("transaction_count", this.uuid, requiredNumber);

    network.send(requester, new DeliverTransactionsEvent(requestedTransactions));

    return null;
  }

}