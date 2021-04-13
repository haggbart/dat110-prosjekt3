package no.hvl.dat110.util;

/**
 * project 3
 * @author tdoy
 *
 */

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash { 
	
	private static BigInteger hashint;

	public static BigInteger hashOf(String entity) {		
		
		// Task: Hash a given string using MD5 and return the result as a BigInteger.

		MessageDigest md;

		try {
			// we use MD5 with 128 bits digest
			md = MessageDigest.getInstance("MD5");

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		
		// compute the hash of the input 'entity'
		byte[] digest = md.digest(entity.getBytes(StandardCharsets.UTF_8));

		// convert the hash into hex format
		String hex = toHex(digest);
		
		// convert the hex into BigInteger
		hashint = new BigInteger(hex, 16);

		return hashint;
	}
	
	public static BigInteger addressSize() {

		// Task: compute the address size of MD5

		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}

		// get the digest length
		int digestLength = md.getDigestLength();

		// compute the number of bits = digest length * 8
		int numberOfBits = digestLength * 8;
		
		// compute the address size = 2 ^ number of bits
		return new BigInteger("2").pow(numberOfBits);
	}
	
	public static int bitSize() {

		// find the digest length
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return 0;
		}
		
		return md.getDigestLength() * 8;
	}
	
	public static String toHex(byte[] digest) {
		StringBuilder strbuilder = new StringBuilder();
		for(byte b : digest) {
			strbuilder.append(String.format("%02x", b&0xff));
		}
		return strbuilder.toString();
	}
}
