/**
 * 
 */
package no.hvl.dat110.middleware;

import no.hvl.dat110.util.LamportClock;
import no.hvl.dat110.util.Util;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author tdoy
 *
 */
public class MutualExclusion {
		
	/** lock variables */
	private boolean CS_BUSY = false;						// indicate to be in critical section (accessing a shared resource) 
	private boolean WANTS_TO_ENTER_CS = false;				// indicate to want to enter CS
	private List<Message> queueack; 						// queue for acknowledged messages
	private List<Message> mutexqueue;						// queue for storing process that are denied permission. We really don't need this for quorum-protocol
	
	private LamportClock clock;								// lamport clock
	private Node node;
	
	public MutualExclusion(Node node) throws RemoteException {
		this.node = node;
		
		clock = new LamportClock();
		queueack = new ArrayList<>();
		mutexqueue = new ArrayList<>();
	}
	
	public void acquireLock() {
		CS_BUSY = true;
	}
	
	public void releaseLocks() {
		WANTS_TO_ENTER_CS = false;
		CS_BUSY = false;
	}

	public boolean doMutexRequest(Message message, byte[] updates) throws RemoteException {
		
		System.out.println(node.nodename + " wants to access CS");
		
		// clear the queueack before requesting for votes
		queueack.clear();
		
		// clear the mutexqueue
		mutexqueue.clear();
		
		// increment clock
		clock.increment();
		
		// adjust the clock on the message, by calling the setClock on the message
		message.setClock(clock.getClock());
				
		// wants to access resource - set the appropriate lock variable
		WANTS_TO_ENTER_CS = true;
		
		// start MutualExclusion algorithm

		
		// first, removeDuplicatePeersBeforeVoting. A peer can contain 2 replicas of a file. This peer will appear twice
		List<Message> messages = removeDuplicatePeersBeforeVoting();

		// multicast the message to activenodes (hint: use multicastMessage)
		multicastMessage(message, messages);
		
		// check that all replicas have replied (permission)
		boolean permission = areAllMessagesReturned(messages.size());
		if (permission) {
			// if yes, acquireLock
			acquireLock();

			// node.broadcastUpdatetoPeers
			node.broadcastUpdatetoPeers(updates);

			// clear the mutexqueue
			mutexqueue.clear();
		}

		// return permission
		return permission;
	}
	
	// multicast message to other processes including self
	private void multicastMessage(Message message, List<Message> activenodes) throws RemoteException {
		
		// iterate over the activenodes
		for (var node : activenodes) {
			// obtain a stub for each node from the registry
			var stub = Util.getProcessStub(node.getNodeIP(), node.getPort());
			// call onMutexRequestReceived()
			assert stub != null;
			stub.onMutexRequestReceived(node);
		}
	}
	
	public void onMutexRequestReceived(Message message) throws RemoteException {
		
		// increment the local clock
		clock.increment();
		
		// if message is from self, acknowledge, and call onMutexAcknowledgementReceived()
		if (message.getNodeIP().equals(node.getNodeName())) {
			message.setAcknowledged(true);
			onMutexAcknowledgementReceived(message);
			return;
		}
			
		int caseid;
		
		// write if statement to transition to the correct caseid

		// caseid=0: Receiver is not accessing shared resource and does not want to (send OK to sender)
		if (CS_BUSY && !WANTS_TO_ENTER_CS) caseid = 0;
		// caseid=1: Receiver already has access to the resource (dont reply but queue the request)
		else if (!CS_BUSY) caseid = 1;
		// caseid=2: Receiver wants to access resource but is yet to - compare own message clock to received message's clock
		else caseid = 2;

		// check for decision
		doDecisionAlgorithm(message, mutexqueue, caseid);
	}
	
	public void doDecisionAlgorithm(Message message, List<Message> queue, int condition) throws RemoteException {
		
		String procName = message.getNodeIP();			// this is the same as nodeName in the Node class
		int port = message.getPort();					// port on which the registry for this stub is listening
		var stub = Util.getProcessStub(procName, port);
		assert stub != null;

		switch(condition) {
		
			/** case 1: Receiver is not accessing shared resource and does not want to (send OK to sender) */
			case 0: {
				// get a stub for the sender from the registry
				// acknowledge message
				message.setAcknowledged(true);
				// send acknowledgement back by calling onMutexAcknowledgementReceived()

				stub.onMutexAcknowledgementReceived(message);
				
				break;
			}
		
			/** case 2: Receiver already has access to the resource (dont reply but queue the request) */
			case 1: {
				// queue this message
				queue.add(message);
				break;
			}
			
			/**
			 *  case 3: Receiver wants to access resource but is yet to (compare own message clock to received message's clock
			 *  the message with lower timestamp wins) - send OK if received is lower. Queue message if received is higher
			 */
			case 2: {
				// check the clock of the sending process
				int sendingClock = message.getClock();

				// own clock for the multicast message
				int clock = this.clock.getClock();

				// compare clocks, the lowest wins

				// if clocks are the same, compare nodeIDs, the lowest wins
				if (clock == sendingClock) {
					if (node.getNodeID().compareTo(message.getNodeID()) < 0) {
						queue.add(message);
					} else {
						// if sender wins, acknowledge the message, obtain a stub and call onMutexAcknowledgementReceived()
						message.setAcknowledged(true);
						stub.onMutexAcknowledgementReceived(message);
					}

				} else if (sendingClock < clock) {
					message.setAcknowledged(true);
					stub.onMutexAcknowledgementReceived(message);
				} else {
					// if sender looses, queue it
					queue.add(message);
				}

				break;
			}
			
			default: break;
		}
		
	}
	
	public void onMutexAcknowledgementReceived(Message message) throws RemoteException {
		
		// add message to queueack
		queueack.add(message);
		
	}
	
	// multicast release locks message to other processes including self
	public void multicastReleaseLocks(Set<Message> activenodes) {
		
		// iterate over the activenodes
		for (var message : activenodes) {
			// obtain a stub for each node from the registry
			var stub = Util.getProcessStub(message.getNodeIP(), message.getPort());
			try {
				assert stub != null;
				// call releaseLocks()
				stub.releaseLocks();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	private boolean areAllMessagesReturned(int numvoters) throws RemoteException {

		// check if the size of the queueack is same as the numvoters
		if (queueack.size() == numvoters) {
			// clear the queueack
			queueack.clear();
			// return true if yes and false if no
			return true;
		}

		return false;
	}
	
	private List<Message> removeDuplicatePeersBeforeVoting() {
		
		List<Message> uniquepeer = new ArrayList<>();
		for(Message p : node.activenodesforfile) {
			boolean found = false;
			for(Message p1 : uniquepeer) {
				if(p.getNodeIP().equals(p1.getNodeIP())) {
					found = true;
					break;
				}
			}
			if(!found)
				uniquepeer.add(p);
		}		
		return uniquepeer;
	}
}
