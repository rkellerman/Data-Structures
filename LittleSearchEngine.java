package search;

import java.io.*;
import java.util.*;

/**
 * This class encapsulates an occurrence of a keyword in a document. It stores the
 * document name, and the frequency of occurrence in that document. Occurrences are
 * associated with keywords in an index hash table.
 * 
 * @author Sesh Venugopal
 * 
 */
class Occurrence {
	/**
	 * Document in which a keyword occurs.
	 */
	String document;
	
	/**
	 * The frequency (number of times) the keyword occurs in the above document.
	 */
	int frequency;
	
	/**
	 * Initializes this occurrence with the given document,frequency pair.
	 * 
	 * @param doc Document name
	 * @param freq Frequency
	 */
	public Occurrence(String doc, int freq) {
		document = doc;
		frequency = freq;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(" + document + "," + frequency + ")";
	}
}

/**
 * This class builds an index of keywords. Each keyword maps to a set of documents in
 * which it occurs, with frequency of occurrence in each document. Once the index is built,
 * the documents can searched on for keywords.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in descending
	 * order of occurrence frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash table of all noise words - mapping is from word to itself.
	 */
	HashMap<String,String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashMap<String,String>(100,2.0f);
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.put(word,word);
		}
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeyWords(docFile);
			mergeKeyWords(kws);
		}
	}

	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeyWords(String docFile) 
	throws FileNotFoundException {
		HashMap<String,Occurrence> hash = new HashMap<String,Occurrence>(1000, 2.0f);
		docFile = docFile.trim();
		BufferedReader br = new BufferedReader(new FileReader(docFile));
		String line = null;
		Occurrence o;
		try{
			while ((line = br.readLine()) != null){
				StringTokenizer st = new StringTokenizer(line);
				while (st.hasMoreTokens()){
					String word = st.nextToken();
					String key = getKeyWord(word);
					if (key == null){
						continue;
					}
					o = hash.get(key);
					if (o != null){
						o.frequency++;
						hash.put(key, o);
					}
					else {
						o = new Occurrence(docFile, 1);
						hash.put(key,  o);
					}
				}
			}
			br.close();
		}
		catch (IOException e){
			System.out.println("IO Error");
			return null;
		}
		return hash;
	}
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeyWords(HashMap<String,Occurrence> kws) {
		if (kws == null){
			return;
		}
		Set<String> keys = kws.keySet();
		Iterator<String> iterator = keys.iterator();
		while (iterator.hasNext()){
			String key = iterator.next();
			
			Occurrence o = kws.get(key);
			ArrayList<Occurrence> list;
			if (o != null){
				list = keywordsIndex.get(key);
				if (list == null){
					list = new ArrayList<Occurrence>();
				}
				
				list.add(o);
				list.trimToSize();
				//System.out.println(insertLastOccurrence(list));
				Occurrence item = list.remove(list.size()-1);
				list.trimToSize();
				int left = 0;
				int right = list.size()-1;
				int mid = 0;
				boolean added = false;
				while (left < right){
					mid = (left+right)/2;
					if (list.get(mid).frequency == item.frequency){
						list.add(mid, item);
						added = true;
						break;
					}
					else if (list.get(mid).frequency > item.frequency){
						left = mid + 1;
					}
					else {
						right = mid;
					}
				}
				if (added == true){
					keywordsIndex.put(key,  list);
					continue;
				}
				if (list.size() == 0){
					list.add(item);
					keywordsIndex.put(key,  list);
					continue;
				}
				if (list.get(list.size()-1).frequency >= item.frequency){
					list.add(item);
				}
				else {
					list.add(left, item);
				}
				keywordsIndex.put(key, list);
			}
		}
	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * TRAILING punctuation, consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyWord(String word) {
		word = word.toLowerCase();
		String key = "";
		for (int i = 0; i < word.length(); i++){
			char c = word.charAt(i);
			if (i == 0 && (c == '\'' || c == '\"' || c == '(' || c == '[' || c == '{')){
				continue;
			}
			if (Character.isLetter(c)){
				key += c;
			}
			else {
				while (i < word.length()-1){
					char c_next = word.charAt(i+1);
					if (Character.isLetter(c_next)){
						key = null;
						break;
					}
					i++;
				}
				if (key == null){
					break;
				}
			}
		}
		String str = noiseWords.get(key);
		if (str != null){
			if (str.equals(key)){
				key = null;
			}
		}
		if (key != null){
			if (key.equals("")){
				key = null;
			}
		}
		return key;
	}
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * same list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion of the last element
	 * (the one at index n-1) is done by first finding the correct spot using binary search, 
	 * then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		ArrayList<Occurrence> temp = new ArrayList<Occurrence>(occs.size());
		for (int i = 0; i < occs.size(); i++){
			temp.add(occs.get(i));
		}
		if (occs.size() <= 1){
			return null;
		}
		Occurrence item = occs.remove(occs.size()-1);
		occs.trimToSize();
		ArrayList<Integer> intList = new ArrayList<Integer>();
		int left = 0;
		int right = occs.size()-1;
		while (left <= right){
			int mid = (left+right)/2;
			intList.add(mid);
			if (occs.get(mid).frequency == item.frequency){
				break;
			}
			else if (occs.get(mid).frequency > item.frequency){
				left = mid + 1;
			}
			else {
				right = mid-1;
			}
		}
		occs.clear();
		for (int i = 0; i < temp.size(); i++){
			occs.add(temp.get(i));
		}
		occs.trimToSize();
		
		return intList;
	}
	
	
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of occurrence frequencies. (Note that a
	 * matching document will only appear once in the result.) Ties in frequency values are broken
	 * in favor of the first keyword. (That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2
	 * also with the same frequency f1, then doc1 will appear before doc2 in the result. 
	 * The result set is limited to 5 entries. If there are no matching documents, the result is null.
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of NAMES of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matching documents,
	 *         the result is null.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
		
		ArrayList<Occurrence> save1 = keywordsIndex.get(kw1);
		ArrayList<Occurrence> save2 = keywordsIndex.get(kw2);
		
		if (save1 == null && save2 == null){
			return null;
		}
		if (save1 == null){
			ArrayList<String> strings = new ArrayList<String>(save2.size());
			for (int i = 0; i < save2.size(); i++){
				if (i == 5){
					return strings;
				}
				strings.add(save2.get(i).document);
			}
			return strings;
		}
		if (save2 == null){
			ArrayList<String> strings = new ArrayList<String>(save1.size());
			for (int i = 0; i < save1.size(); i++){
				if (i == 5){
					return strings;
				}
				strings.add(save1.get(i).document);
			}
			return strings;
		}
		ArrayList<Occurrence> list1 = new ArrayList<Occurrence>(save1.size());
		for (int i = 0; i < save1.size(); i++){
			Occurrence o = new Occurrence(save1.get(i).document, save1.get(i).frequency);
			list1.add(o);
		}
		ArrayList<Occurrence> list2 = new ArrayList<Occurrence>(save2.size());
		for (int i = 0; i < save2.size(); i++){
			Occurrence o = new Occurrence(save2.get(i).document, save2.get(i).frequency);
			list2.add(o);
		}
		ArrayList<Occurrence> occs = new ArrayList<Occurrence>();
		for (int i = 0; i < list1.size(); i++){
			occs.add(list1.get(i));
		}
		list1.trimToSize();
		list2.trimToSize();
		occs.trimToSize();
		
		for (int i = 0; i < list2.size(); i++){
			Occurrence o = list2.get(i);
			boolean found = false;
			for (int j = 0; j < occs.size(); j++){
				if (occs.get(j).document.equals(o.document)){
					occs.get(j).frequency += o.frequency;
					found = true;
					break;
				}
			}
			if (!found){
				occs.add(o);
			}
		}
		for (int i = 0; i < occs.size(); i++){
			int position = i;
			Occurrence current = occs.get(i);
			while (position > 0 && occs.get(position-1).frequency < current.frequency){
				occs.set(position,  occs.get(position-1));
				position -= 1;
			}
			occs.set(position,  current);
			
		}
		ArrayList<String> strings = new ArrayList<String>(5);
		for (int i = 0; i < occs.size(); i++){
			if (i == 5){
				break;
			}
			strings.add(occs.get(i).document);
		}
		return strings;
		
		
		
	}
}








