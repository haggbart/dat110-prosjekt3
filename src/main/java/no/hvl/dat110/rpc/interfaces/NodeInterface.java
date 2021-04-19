package no.hvl.dat110.rpc.interfaces;

/**
 * dat110
 *
 * @author tdoy
 */

import no.hvl.dat110.middleware.Message;

import java.math.BigInteger;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;


public interface NodeInterface extends Remote {

    BigInteger getNodeID() throws RemoteException;

    Set<BigInteger> getNodeKeys() throws RemoteException;

    String getNodeName() throws RemoteException;

    int getPort() throws RemoteException;

    NodeInterface getPredecessor() throws RemoteException;

    void setPredecessor(NodeInterface pred) throws RemoteException;

    NodeInterface getSuccessor() throws RemoteException;

    void setSuccessor(NodeInterface succ) throws RemoteException;

    void addKey(BigInteger id) throws RemoteException;

    void removeKey(BigInteger id) throws RemoteException;

    NodeInterface findSuccessor(BigInteger key) throws RemoteException;

    void notify(NodeInterface pred) throws RemoteException;

    Message getFilesMetadata(BigInteger fileID) throws RemoteException;

    Map<BigInteger, Message> getFilesMetadata() throws RemoteException;

    void saveFileContent(String filename, BigInteger fileID, byte[] bytesOfFile, boolean primary) throws RemoteException;

    void updateFileContent(List<Message> updates) throws RemoteException;

    void broadcastUpdatetoPeers(byte[] bytesOfFile) throws RemoteException;

    /** Remote-Write Protocol */
    void requestRemoteWriteOperation(byte[] updates, NodeInterface primary, Set<Message> activenodes) throws RemoteException;

    /** Concerns mutual exclusion algorithm*/

    boolean requestMutexWriteOperation(Message message, byte[] updates, Set<Message> messages) throws RemoteException;

    void acquireLock() throws RemoteException;

    void releaseLocks() throws RemoteException;

    void multicastReleaseLocks(Set<Message> activenodes) throws RemoteException;

    void onMutexAcknowledgementReceived(Message message) throws RemoteException;

    void onMutexRequestReceived(Message message) throws RemoteException;

}
