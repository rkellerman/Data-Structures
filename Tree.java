package structures;

import java.util.*;

/**
 * This class implements an HTML DOM Tree. Each node of the tree is a TagNode, with fields for
 * tag/text, first child and sibling.
 * 
 */
public class Tree {
	
	/**
	 * Root node
	 */
	TagNode root=null;
	
	/**
	 * Scanner used to read input HTML file when building the tree
	 */
	Scanner sc;
	
	/**
	 * Initializes this tree object with scanner for input HTML file
	 * 
	 * @param sc Scanner for input HTML file
	 */
	public Tree(Scanner sc) {
		this.sc = sc;
		root = null;
	}
	
	/**
	 * Builds the DOM tree from input HTML file. The root of the 
	 * tree is stored in the root field.
	 */
	public void build() {
		Stack<TagNode> stack = new Stack<TagNode>();
		String str;
		while (sc.hasNextLine()){
			str = sc.nextLine();
			if (str.length() == 0){
				continue;
			}
			else if (str.charAt(0) == '<'){
				if (str.charAt(1) != '/'){  // when we have "<....>"
					if (stack.isEmpty()){
						TagNode temp = new TagNode(str.substring(1, str.length()-1), null, null);
						stack.push(temp);
						
					}
					else if (stack.peek().firstChild == null){
						TagNode temp = new TagNode(str.substring(1, str.length()-1), null, null);
						stack.peek().firstChild = temp;
						stack.push(temp);
						
					}
					else {
						TagNode temp = new TagNode(str.substring(1, str.length()-1), null, null);
						TagNode ptr = stack.peek().firstChild;
						while (ptr.sibling != null){
							ptr = ptr.sibling;
						}
						ptr.sibling = temp;
						stack.push(temp);
					}
				}
				else { // when we have "</....>"
					TagNode temp = stack.pop();
					
					if (temp.tag.equals("html")){
						root = temp;
					}
				}
			}
			else { // when it is just plain text
				if (stack.peek().firstChild == null){
					TagNode first = new TagNode(str, null, null);
					stack.peek().firstChild = first;
				}
				else {
					TagNode first = new TagNode(str, null, null);
					TagNode ptr = stack.peek().firstChild;
					while (ptr.sibling != null){
						ptr = ptr.sibling;
					}
					ptr.sibling = first;
					
				}
			}
		}
	
		
	}
	
	
	/**
	 * Replaces all occurrences of an old tag in the DOM tree with a new tag
	 * 
	 * @param oldTag Old tag
	 * @param newTag Replacement tag
	 */
	public void replaceTag(String oldTag, String newTag) {
		replace(root, oldTag, newTag);
	}
	
	private void replace(TagNode root, String oldTag, String newTag){
		if (root.firstChild != null){
			replace(root.firstChild, oldTag, newTag);
		}
		if (root.tag.equals(oldTag)){
			root.tag = newTag;
		}
		if (root.sibling != null){
			replace(root.sibling, oldTag, newTag);
		}
		
	}
	
	/**
	 * Boldfaces every column of the given row of the table in the DOM tree. The boldface (b)
	 * tag appears directly under the td tag of every column of this row.
	 * 
	 * @param row Row to bold, first row is numbered 1 (not 0).
	 */
	public void boldRow(int row) {
		int count = 0;
		TagNode ptr;
		ptr = search(root, "table");
		if (ptr == null){
			System.out.println("\tNo table exists");
			return;
		}
		// here we are at the table node
		ptr = ptr.firstChild;
		count++;
		while (count != row){
			ptr = ptr.sibling;
			if (ptr == null){
				System.out.println("\tThe given row does not exist");
				return;
			}
			count++;
		}
		ptr = ptr.firstChild; // now at the first column of row
		while (ptr.sibling != null){
			TagNode temp = ptr.firstChild;
			ptr.firstChild = new TagNode("b", null, null);
			ptr.firstChild.firstChild = temp;
			ptr = ptr.sibling;
		}
		TagNode temp = ptr.firstChild;
		ptr.firstChild = new TagNode("b", null, null);
		ptr.firstChild.firstChild = temp;
		
	}
	
	private TagNode search(TagNode root, String target){
		TagNode ptr = root;
		if (root.firstChild != null){
			ptr = search(root.firstChild, target);
		}
		if (root.tag.equals("table")){
			return root;
		}
		if (root.sibling != null){
			ptr = search(root.sibling, target);
		}
		if (ptr == root){
			return null;
		}
		return ptr;
	}
	
	/**
	 * Remove all occurrences of a tag from the DOM tree. If the tag is p, em, or b, all occurrences of the tag
	 * are removed. If the tag is ol or ul, then All occurrences of such a tag are removed from the tree, and, 
	 * in addition, all the li tags immediately under the removed tag are converted to p tags. 
	 * 
	 * @param tag Tag to be removed, can be p, em, b, ol, or ul
	 */
	public void removeTag(String tag) {
		if (tag.equals("b") || tag.equals("em") || tag.equals("p")){ // case1
			remove1(root, tag, null, false, false);
		}
		else { //case2
			remove2(root, tag, null, false);
		}
	}
	
	private void remove2(TagNode root, String tag, TagNode pre, boolean fromParent){
		if (root.firstChild != null){
			remove2(root.firstChild, tag, root, true);
		}
		if (root.tag.equals(tag)){
			if (fromParent == true){
				TagNode temp = root.firstChild;
				TagNode after = root.sibling;
				TagNode ptr = temp;
				while (ptr.sibling != null){
					if (ptr.tag.equals("li")){
						
						ptr.tag = "p";
					}
					ptr = ptr.sibling;
				}
				if (ptr.tag.equals("li")){
					
					ptr.tag = "p";
				}
				ptr.sibling = after;
				pre.firstChild = temp;
				root = ptr;
			}
			else{ // from sibling
				TagNode temp = root.firstChild;
				TagNode after = root.sibling;
				TagNode ptr = temp;
				while (ptr.sibling != null){
					if (ptr.tag.equals("li")){
						
						ptr.tag = "p";
					}
					ptr = ptr.sibling;
				}
				if (ptr.tag.equals("li")){
					
					ptr.tag = "p";
				}
				ptr.sibling = after;
				pre.sibling = temp;
				root = ptr;
			}
		}
		if (root.sibling != null){
			remove2(root.sibling, tag, root, false);
		}
	}
	
	private void remove1(TagNode root, String tag, TagNode pre, boolean fromParent, boolean fromSibling){
		if (root.firstChild != null){
			TagNode temp = root;
			remove1(root.firstChild, tag, temp, true, false);
		}
		if (root.tag.equals(tag)){
			if (fromParent == true){
				if (root.sibling == null){ // when tag is the only child
					TagNode taggedText = root.firstChild;
					pre.firstChild = taggedText;
					root = taggedText;
				}
				else { // when tag is the first child
					if (root.sibling.firstChild == null){
						
						TagNode taggedText = root.firstChild;
						TagNode otherText = root.sibling;
						TagNode after = otherText.sibling;
						TagNode newTag;
						if (pre.firstChild == null){
							pre.tag = pre.tag;
							pre.sibling = taggedText;
							taggedText.sibling = otherText;
							otherText.sibling = after;
						}
						else{
							newTag = new TagNode(taggedText.tag, null, otherText);
							otherText.sibling = after;
							pre.sibling = newTag;
						}
						root = pre.sibling;
					}
					else {
						TagNode nextTag = root.sibling;
						TagNode taggedText = root.firstChild;
						
						pre.firstChild = taggedText;
						TagNode ptr = taggedText;
						while (ptr.sibling != null){
							ptr = ptr.sibling;
						}
						ptr.sibling = nextTag;
						root = taggedText;
					}
				}
			}
			else { // from sibling 
				if (root.sibling == null){
					TagNode taggedText = root.firstChild;
					pre.sibling = taggedText;
					root = taggedText;
				}
				else {
					if (root.sibling.firstChild == null){
						TagNode taggedText = root.firstChild;
						TagNode otherText = root.sibling;
						TagNode after = otherText.sibling;
						TagNode newTag;
						if (pre.firstChild == null){
							pre.tag = pre.tag;
							pre.sibling = taggedText;
							taggedText.sibling = otherText;
							otherText.sibling = after;
						}
						else{
							newTag = new TagNode(taggedText.tag, null, otherText);
							otherText.sibling = after;
							pre.sibling = newTag;
						}
						root = pre.sibling;
					}
					else {
						
						TagNode nextTag = root.sibling;
						TagNode taggedText = root.firstChild;
						pre.sibling = taggedText;
						TagNode ptr = taggedText;
						while (ptr.sibling != null){
							ptr = ptr.sibling;
						}
						ptr.sibling = nextTag;
						root = ptr;
					}
				}
			}
		}
		if (root != null){	
			if (root.sibling != null){
				TagNode temp = root;
				remove1(root.sibling, tag, temp, false, true);
			}
		}
	}
	
	
	/**
	 * Adds a tag around all occurrences of a word in the DOM tree.
	 * 
	 * @param word Word around which tag is to be added
	 * @param tag Tag to be added
	 */
	public void addTag(String word, String tag) {
		addTag(root, word.toLowerCase(),tag, null, false);
		
	}
	
	private void addTag(TagNode root, String word, String tag, TagNode pre, boolean fromParent){
		
		if (root.firstChild != null){
			addTag(root.firstChild, word, tag, root, true);
		}
		if (root.tag.toLowerCase().indexOf(word) != -1){
			int index = root.tag.toLowerCase().indexOf(word);
			if (fromParent == true){ // from a parent
				if (index == 0){ // when the word starts at the very beginning of tag
					if (root.tag.length() == word.length()){ // tag is the only word
						TagNode target = root;
						TagNode sibling = root.sibling;
						pre.firstChild = new TagNode(tag, target, sibling);
						target.sibling = null;
						root = pre.firstChild;
					}
					else {
						char c = root.tag.charAt(word.length());
						if (c == ' '){
							TagNode target = new TagNode(root.tag.substring(0, word.length()), null, null);
							TagNode sibling = new TagNode(root.tag.substring(word.length()), null, null);
							TagNode next = root.sibling;
							pre.firstChild = new TagNode(tag, target, sibling);
							pre.firstChild.sibling.sibling = next;
							target.sibling = null;
							root = pre.firstChild;
						}
						else if (c == '!' || c == '?' || c == '.' || c == ',' || c == ';' || c == ':'){
							if (root.tag.length() == word.length() + 1){ // when c is the last character
								TagNode target = root;
								TagNode sibling = root.sibling;
								pre.firstChild = new TagNode(tag, target, sibling);
								target.sibling = null;
								root = pre.firstChild;
							}
							else { // when c is not the last character
								TagNode target = new TagNode(root.tag.substring(0, word.length() + 1), null, null);
								TagNode sibling = new TagNode(root.tag.substring(word.length() +1), null, null);
								TagNode next = root.sibling;
								pre.firstChild = new TagNode(tag, target, sibling);
								pre.firstChild.sibling.sibling = next;
								target.sibling = null;
								root = pre.firstChild;
							}
						}
					}
				}
				
				else { // when index of word is not at the beginning of tag
					if (root.tag.length() == index + word.length()){ // word is the last word of tag
						TagNode next = root.sibling;
						TagNode first = new TagNode(root.tag.substring(0, index), null, null);
						TagNode target = new TagNode(root.tag.substring(index), null, null);
						pre.firstChild = first;
						first.sibling = new TagNode(tag, target, next);
						target.sibling = null;
						root = first.sibling;
					}
					else {
						char c = root.tag.charAt(index + word.length());
						if (c == ' '){
							TagNode next = root.sibling;
							TagNode first = new TagNode(root.tag.substring(0, index), null, null);
							TagNode target = new TagNode(root.tag.substring(index, index + word.length()), null, null);
							TagNode second = new TagNode(root.tag.substring(index + word.length()), null, null);
							second.sibling = next;
							first.sibling = new TagNode(tag, target, second);
							pre.firstChild = first;
							target.sibling = null;
							root = first.sibling;
							
						}
						else if (c == '!' || c == '?' || c == '.' || c == ',' || c == ';' || c == ':'){
							if (root.tag.length() == index + word.length() + 1){ // c is the last character of tag
								TagNode first = new TagNode(root.tag.substring(0, index), null, null);
								TagNode next = root.sibling;
								TagNode target = new TagNode(root.tag.substring(index), null, null);
								first.sibling = new TagNode(tag, target, next);
								pre.firstChild = first;
								target.sibling = null;
								root = first.sibling;
							}
							else {
								TagNode next = root.sibling;
								TagNode first = new TagNode(root.tag.substring(0, index), null, null);
								TagNode target = new TagNode(root.tag.substring(index, index + word.length() + 1), null, null);
								TagNode second = new TagNode(root.tag.substring(index + word.length() + 1), null, null);
								second.sibling = next;
								first.sibling = new TagNode(tag, target, second);
								pre.firstChild = first;
								target.sibling = null;
								root = first.sibling;
							}
						}
					}
				}
			}
			else { // from a sibling
				if (index == 0){ // when the word starts at the very beginning of tag
					if (root.tag.length() == word.length()){ // tag is the only word
						TagNode target = root;
						TagNode sibling = root.sibling;
						pre.sibling = new TagNode(tag, target, sibling);
						target.sibling =  null;
						root = pre.sibling;
					}
					else {
						char c = root.tag.charAt(word.length());
						if (c == ' '){
							TagNode target = new TagNode(root.tag.substring(0, word.length()), null, null);
							TagNode sibling = new TagNode(root.tag.substring(word.length()), null, null);
							TagNode next = root.sibling;
							pre.sibling = new TagNode(tag, target, sibling);
							pre.sibling.sibling.sibling = next;
							target.sibling = null;
							root = pre.sibling;
						}
						else if (c == '!' || c == '?' || c == '.' || c == ',' || c == ';' || c == ':'){
							if (root.tag.length() == word.length() + 1){ // when c is the last character
								TagNode target = root;
								TagNode sibling = root.sibling;
								pre.sibling = new TagNode(tag, target, sibling);
								target.sibling = null;
								root = pre.sibling;
							}
							else { // when c is not the last character
								TagNode target = new TagNode(root.tag.substring(0, word.length() + 1), null, null);
								TagNode sibling = new TagNode(root.tag.substring(word.length() +1), null, null);
								TagNode next = root.sibling;
								pre.sibling = new TagNode(tag, target, sibling);
								pre.sibling.sibling.sibling = next;
								target.sibling = null;
								root = pre.sibling;
							}
						}
					}
				}
				
				else { // when index of word is not at the beginning of tag
					if (root.tag.length() == index + word.length()){ // word is the last word of tag
						TagNode next = root.sibling;
						TagNode first = new TagNode(root.tag.substring(0, index), null, null);
						TagNode target = new TagNode(root.tag.substring(index), null, null);
						pre.sibling = first;
						first.sibling = new TagNode(tag, target, next);
						target.sibling = null;
						root = first.sibling;
					}
					else {
						char c = root.tag.charAt(index + word.length());
						if (c == ' '){
							TagNode next = root.sibling;
							TagNode first = new TagNode(root.tag.substring(0, index), null, null);
							TagNode target = new TagNode(root.tag.substring(index, index + word.length()), null, null);
							TagNode second = new TagNode(root.tag.substring(index + word.length()), null, null);
							second.sibling = next;
							first.sibling = new TagNode(tag, target, second);
							pre.sibling = first;
							target.sibling = null;
							root = first.sibling;
							
						}
						else if (c == '!' || c == '?' || c == '.' || c == ',' || c == ';' || c == ':'){
							if (root.tag.length() == index + word.length() + 1){ // c is the last character of tag
								TagNode first = new TagNode(root.tag.substring(0, index), null, null);
								TagNode next = root.sibling;
								TagNode target = new TagNode(root.tag.substring(index), null, null);
								first.sibling = new TagNode(tag, target, next);
								pre.sibling = first;
								target.sibling = null;
								root = first.sibling;
							}
							else {
								TagNode next = root.sibling;
								TagNode first = new TagNode(root.tag.substring(0, index), null, null);
								TagNode target = new TagNode(root.tag.substring(index, index + word.length() + 1), null, null);
								TagNode second = new TagNode(root.tag.substring(index + word.length() + 1), null, null);
								second.sibling = next;
								first.sibling = new TagNode(tag, target, second);
								pre.sibling = first;
								target.sibling = null;
								root = first.sibling;
							}
						}
					}
				}
			}
		}
		if (root.sibling != null){
			addTag(root.sibling, word, tag, root, false);
		}
		
	}
	/**
	 * Gets the HTML represented by this DOM tree. The returned string includes
	 * new lines, so that when it is printed, it will be identical to the
	 * input file from which the DOM tree was built.
	 * 
	 * @return HTML string, including new lines. 
	 */
	public String getHTML() {
		StringBuilder sb = new StringBuilder();
		getHTML(root, sb);
		return sb.toString();
	}
	
	private void getHTML(TagNode root, StringBuilder sb) {
		
		for (TagNode ptr=root; ptr != null;ptr=ptr.sibling) {
			if (ptr.firstChild == null) {
			
				sb.append(ptr.tag);
				sb.append("\n");
			} else {
				sb.append("<");
				sb.append(ptr.tag);
				sb.append(">\n");
				getHTML(ptr.firstChild, sb);
				sb.append("</");
				sb.append(ptr.tag);
				sb.append(">\n");	
			}
		}
	}
	
}
