package scenario.finalita.events;

import network.packets.Event;
import scenario.finalita.Block;


/**
 * Validates the given block for the node.
 * Size: Returns number of encoded bytes.
 */
public class ValidateBlockEvent implements Event {
  private final Block block;

  public ValidateBlockEvent(Block block) {
    this.block = block;
  }

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }

  public Block getBlock() {
    return block;
  }
}
