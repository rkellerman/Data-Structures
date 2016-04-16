package search;
import java.io.*;
import java.util.*;


public class EngineDriver {

	public static void main(String[]args){
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter the file containing the documents to be scanned: ");
		String docsFile = sc.next();
		System.out.println("\nEnter the file containing the noise words: ");
		String noiseWordsFile = sc.next();
		LittleSearchEngine engine = new LittleSearchEngine();
		try{
			engine.makeIndex(docsFile, noiseWordsFile);
			
		}
		catch (FileNotFoundException e){
			System.out.println("File not found");
			return;
		}
		
		Set<String> keys = engine.keywordsIndex.keySet();
		Iterator<String> iterator = keys.iterator();
		while (iterator.hasNext()){
			String key = iterator.next();
			System.out.println(key + " ==> " + engine.keywordsIndex.get(key).toString());
		}
		
		System.out.println("Enter the first keyword: ");
		String kw1 = sc.next();
		System.out.println("Enter the second keyword or \"quit\" to exit: ");
		String kw2 = sc.next();
		while (!kw2.toLowerCase().equals("quit")){
			System.out.println(engine.top5search(kw1, kw2));
			System.out.println("Enter the first keyword: ");
			kw1 = sc.next();
			System.out.println("Enter the second keyword or \"quit\" to exit: ");
			kw2 = sc.next();
		}
		
		
		
		
		
		System.out.println("\nEnter item to find frequency or \"quit\" to exit: ");
		String item = sc.next();
		while (!item.toLowerCase().equals("quit")){
			if (engine.keywordsIndex.get(item.toLowerCase()) != null){
				System.out.println(engine.keywordsIndex.get(item.toLowerCase()).toString());
			}
			else {
				System.out.println("Item does not appear in the documents...");
			}
			System.out.println("\nEnter item to find frequency or \"quit\" to exit: ");
			item = sc.next();
		}
		
		sc.close();
	}
}
