package no.hvl.dat110;




import no.hvl.dat110.rpc.interfaces.NodeInterface;
import no.hvl.dat110.util.Hash;
import no.hvl.dat110.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DHTTestFindSuccessor {
	
	@BeforeEach
	void setUp() throws Exception {
		

	}

	@Test
	void test() throws InterruptedException, RemoteException {
		
		// retrieve the process stubs to be contacted to resolve a key
		NodeInterface p1 = Util.getProcessStub("process1", 9091);
		
		BigInteger key1 = Hash.hashOf("file10");
		BigInteger key2 = Hash.hashOf("file20");
		BigInteger key3 = Hash.hashOf("file31"); 
		
		// expected
		BigInteger key1expected = new BigInteger("121411138451101288395601026024677976156");
		BigInteger key2expected = new BigInteger("210821560651360572675896360671414673172");
		BigInteger key3expected = new BigInteger("121411138451101288395601026024677976156");
		
		BigInteger key1actual = p1.findSuccessor(key1).getNodeID();
		BigInteger key2actual = p1.findSuccessor(key2).getNodeID();
		BigInteger key3actual = p1.findSuccessor(key3).getNodeID();
		
		assertEquals(key1expected, key1actual);
		assertEquals(key2expected, key2actual);
		assertEquals(key3expected, key3actual);
		
	}

}
