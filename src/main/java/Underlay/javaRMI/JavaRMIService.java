package underlay.javarmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import underlay.packets.Request;

/**
 * Represents a Java RMI Service. An RMI service only has a single function that dispatches the received request
 * to the local `RequestHandler` instance.
 */
public interface JavaRmiService extends Remote {
  void handleRequest(Request request) throws RemoteException;
}
