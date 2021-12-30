package scenario.integrita.historytree;

import org.w3c.dom.Node;

/**
 * holds the address of a node of the history tree.
 * position represents the operation number.
 * level indicates at which height of the history tree the node is located.
 * level ranges from 0 to log2(position)+1.
 */
public class NodeAddress {
  int position;
  int level;

  public NodeAddress(){

  }

  public NodeAddress(int position, int level) {
    this.position = position;
    this.level = level;
  }

  /**
   * checks whether the address fields are valid
   */
  public static boolean isValid(NodeAddress addr){
    if (addr.position < 1) {
      return false;
    }
    if (addr.level < 0){
      return false;
    }
    double maxLevel = Math.ceil(Math.log(addr.position) / Math.log(2));
    if (addr.level > maxLevel) {
      return false;
    }
    return true;
  }
  /**
   * checks if the supplied address `addr` belongs to a full node.
   * A full node is a node whose left and right sub-trees are full.
   */
  public static boolean isFull(NodeAddress addr) {
    if (!isValid(addr)) return false;
    int modulus = (int) Math.pow(2, addr.level);
    int remainder = Math.floorMod(addr.position, modulus);
    return (remainder == 0);
  }

  /**
   * checks if the supplied address `addr` belongs to a temporary node i.e., not a full node.
   */
  public static boolean isTemporary(NodeAddress addr) {
    if (!isValid(addr)) return false;
    return !isFull(addr);
  }

  /**
   * checks if the supplied address `addr` belongs to a history tree root.
   */
  public static boolean isTreeDigest(NodeAddress addr) {
    if (!isValid(addr)) return false;
    double denom = Math.ceil(Math.log(addr.position) / Math.log(2));
    return (addr.level == denom);
  }
}
