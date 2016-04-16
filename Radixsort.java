package apps;

import java.io.IOException;
import java.util.Scanner;

import structures.Node;

/**
 * This class sorts a given list of strings which represent numbers in
 * the given radix system. For instance, radix=10 means decimal numbers;
 * radix=16 means hexadecimal numbers. 
 * 
 * @author ru-nb-cs112
 */
public class Radixsort {

	/**
	 * Master list that holds all items, starting with input, and updated after every pass
	 * of the radixsort algorithm. Holds sorted result after the final pass. This is a
	 * circular linked list in which every item is stored in its textual string form (even
	 * though the items represent numbers). This masterListRear field points to the last 
	 * node in the CLL.
	 */
	Node<String> masterListRear;
	
	/**
	 * Array of linked lists that holds the digit-wise distribution of the items during
	 * each pass of the radixsort algorithm. 
	 */
	Node<String>[] buckets;
	
	/** 
	 * The sort radix, defaults to 10.
	 */
	int radix=10;
	
	/**
	 * Initializes this object with the given radix (10 or 16)
	 * 
	 * @param radix
	 */
	public Radixsort() {
		masterListRear = null;
		buckets = null;
	}
	
	/**
	 * Sorts the items in the input file, and returns a CLL containing the sorted result
	 * in ascending order. The first line in the input file is the radix. Every subsequent
	 * line is a number, to be read in as a string.
	 * 
	 * The items in the input are first read and stored in the master list, which is a CLL that is referenced
	 * by the masterListRear field. Next, the max number of digits in the items is determined. Then, 
	 * scatter and gather are called, for each pass through the items. Pass 0 is for the least
	 * significant digit, pass 1 for the second-to-least significant digit, etc. After each pass,
	 * the master list is updated with items in the order determined at the end of that pass.
	 * 
	 * NO NEW NODES are created in the sort process - the nodes of the master list are recycled
	 * through all the intermediate stages of the sorting process.
	 * 
	 * @param sc Scanner that points to the input file of radix + items to be sorted
	 * @return Sorted (in ascending order) circular list of items
	 * @throws IOException If there is an exception in reading the input file
	 */
	public Node<String> sort(Scanner sc) 
	throws IOException {
		// first line is radix
		if (!sc.hasNext()) { // empty file, nothing to sort
			return null;
		}
		// read radix from file, and set up buckets for linked lists
		radix = sc.nextInt();
		buckets = (Node<String>[])new Node[radix];
		
		// create master list from input
		createMasterListFromInput(sc);
		
		// find the string with the maximum length
		int maxDigits = getMaxDigits();
		
		Node<String> savedListRear = null;
		for (int i=0; i < maxDigits; i++) {
			
			Node<String> ptr;
			scatter(i);
			
			if (masterListRear != null){
				if (masterListRear.next == masterListRear){
					if (savedListRear == null){
						savedListRear = masterListRear;
						savedListRear.next = savedListRear;
						masterListRear = null;
					}
					else {
						masterListRear.next = savedListRear.next;
						savedListRear.next = masterListRear;
						savedListRear = masterListRear;
						masterListRear = null;
					}
				}
				else {
					if (savedListRear == null){
						ptr = masterListRear;
						savedListRear = ptr;
						masterListRear = null;
					}
					else {
						ptr = masterListRear.next;
						while (ptr != masterListRear){
							Node<String> temp = ptr.next;
							ptr.next = savedListRear.next;
							savedListRear.next = ptr;
							savedListRear = ptr;
							ptr = temp;;
						}
						ptr.next = savedListRear.next;
						savedListRear.next = ptr;
						savedListRear = ptr;
						masterListRear = null;
					}
				}
			}
			
			gather();
			
		}
		if (masterListRear != null){
			if (masterListRear.next == masterListRear){
				if (savedListRear == null){
					savedListRear = masterListRear;
					savedListRear.next = savedListRear;
				}
				else {
					masterListRear.next = savedListRear.next;
					savedListRear.next = masterListRear;
					savedListRear = masterListRear;
					masterListRear = null;
				}
			}
			else {
				Node<String> ptr = masterListRear.next;
				while (ptr != masterListRear){
					if (savedListRear == null){
						System.out.println("hi");
						ptr = masterListRear.next;
						savedListRear = ptr;
					}
					else {
						Node<String> temp = ptr.next;
						ptr.next = savedListRear.next;
						savedListRear.next = ptr;
						savedListRear = ptr;
						ptr = temp;
					}
				}
				masterListRear.next = savedListRear.next;
				savedListRear.next = masterListRear;
				savedListRear = masterListRear;
			}
		}
		
		return savedListRear;
	}
	
	/**
	 * Reads entries to be sorted from input file and stores them as 
	 * strings in the master CLL (pointed by the instance field masterListRear, 
	 * in the order in which they are read. In other words, the first entry in the linked 
	 * list is the first entry in the input, the second entry in the linked list is the 
	 * second entry in the input, and so on. 
	 * 
	 * @param sc Scanner pointing to the input file
	 * @throws IOException If there is any error in reading the input
	 */
	public void createMasterListFromInput(Scanner sc) 
	throws IOException {
		if (!sc.hasNext()){
			masterListRear = null;
			throw new IOException();
		}
		else {
			while(sc.hasNext()){
				if (masterListRear == null){
					masterListRear = new Node<String>(sc.next().toString(), null);
					masterListRear.next = masterListRear;
				}
				else {
					Node<String> ptr = new Node<String>(sc.next().toString(), masterListRear.next);
					masterListRear.next = ptr;
					masterListRear = ptr;
				}
			}
		}
		
	}
	
	/**
	 * Determines the maximum number of digits over all the entries in the master list
	 * 
	 * @return Maximum number of digits over all the entries
	 */
	public int getMaxDigits() {
		int maxDigits = masterListRear.data.length();
		Node<String> ptr = masterListRear.next;
		while (ptr != masterListRear) {
			int length = ptr.data.length();
			if (length > maxDigits) {
				maxDigits = length;
			}
			ptr = ptr.next;
		}
		return maxDigits;
	}
	
	/**
	 * Scatters entries of master list (referenced by instance field masterListReat) 
	 * to buckets for a given pass.
	 * 
	 * Passes are digit by digit, starting with the rightmost digit -
	 * the rightmost digit is the "0-th", i.e. pass=0 for rightmost digit, pass=1 for 
	 * second to rightmost, and so on. 
	 * 
	 * Each digit is extracted as a character, 
	 * then converted into the appropriate numeric value in the given radix
	 * using the java.lang.Character.digit(char ch, int radix) method
	 * 
	 * @param pass Pass is 0 for rightmost digit, 1 for second to rightmost, etc
	 */
	public void scatter(int pass) {
		for (int i = 0; i < radix; i++){
			buckets[i] = null;
		}
		Node<String> ptr = masterListRear.next;
		Node<String> prev = masterListRear;
		while(ptr != masterListRear){
			if (ptr.data.length() <= pass){
				Node<String> temp = prev;
				prev = ptr;
				temp.next = prev;
				ptr = ptr.next;
				continue;
			}
			int number = ptr.data.length() - 1 - pass;
			char c = ptr.data.charAt(number);
			int digit = Character.digit(c,  radix);
			if (buckets[digit] == null){
				prev.next = ptr.next;
				buckets[digit] = ptr;  
				buckets[digit].next = buckets[digit];
				ptr = prev.next;
			}
			else {
				prev.next = ptr.next;
				Node<String> temp = ptr;
				temp.next = buckets[digit].next;
				buckets[digit].next = temp;
				buckets[digit] = temp;
				ptr = prev.next;
			}
		}
		if (ptr.data.length() <= pass){
			masterListRear = ptr;
			return;
		}
		int number = masterListRear.data.length() - 1 - pass;
		char c = masterListRear.data.charAt(number);
		int digit = Character.digit(c, radix);
		if (buckets[digit] == null){
			if (ptr == ptr.next){
				masterListRear = null;
			}
			prev.next = ptr.next;
			if (masterListRear != null){
				masterListRear = prev;
			}
			buckets[digit] = ptr;
			buckets[digit].next = buckets[digit];
		}
		else {
			Node<String> temp = null;
			if (ptr == ptr.next){
				temp = masterListRear;
				temp.next = buckets[digit].next;
				buckets[digit].next = temp;
				buckets[digit] = temp;
				masterListRear = null;
			}
			else {
				prev.next = ptr.next;
				temp = masterListRear;
				temp.next = buckets[digit].next;
				buckets[digit].next = temp;
				buckets[digit] = temp;
			}
			if (masterListRear != null){
				masterListRear = prev;
			}
		}
	}

	/**
	 * Gathers all the CLLs in all the buckets into the master list, referenced
	 * by the instance field masterListRear
	 * 
	 * @param buckets Buckets of CLLs
	 */
	public void gather() {
		for (int i = 0; i < buckets.length; i++){
			if (buckets[i] == null){
				continue;
			}
			else {
				Node<String> ptr = buckets[i].next;
				while (ptr != buckets[i]){
					if (masterListRear == null){
						Node<String> temp = ptr.next;
						masterListRear = ptr;
						masterListRear.next = masterListRear;
						ptr = temp;
					}
					else {
						Node<String> temp = ptr;
						ptr = ptr.next;
						temp.next = masterListRear.next;
						masterListRear.next = temp;
						masterListRear = temp;
					}
				}
				if (masterListRear == null){
					masterListRear = buckets[i];
					masterListRear.next = masterListRear;
					buckets[i] = null;
				}
				else {
					Node<String> temp = buckets[i];
					temp.next = masterListRear.next;
					masterListRear.next = temp;
					masterListRear = temp;
					buckets[i] = null;
				}
			}
		}
	}	
}

