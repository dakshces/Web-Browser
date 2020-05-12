package renderingengine;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * An ArrayList implementation of a general {@code DOM} node.
 * 
 */
public class Node {
	
	// +--------+------------------------------
	// | Fields |
	// +--------+
	ArrayList<Node> children;

	// +--------------+-------------------------------------------
	// | Constructors |
	// +--------------+
	/**
	 * Constructs a {@code Node} with an empty children {@code ArrayList} parameterized
	 * to {@code Node}.
	 */
	public Node() {
		children = new ArrayList<Node>();
	} // Node()

	/**
	 * Constructs a {@code Node} whose children is the specified {@code ArrayList}
	 * 
	 * @param children a {@code Node}-parameterized {@code ArrayList}
	 */
	public Node(ArrayList<Node> children) {
		this.children = children;
	} // Node(ArrayList<Node>)

}// class Node

/**
 * {@code Text} is a subclass of {@code Node} that represents the
 * non-tag components within an {@code HTMl} file.
 * 
 */
class Text extends Node {

	// +--------+------------------------------
	// | Fields |
	// +--------+
	String txt;
	Font font = null;

	// +--------------+-------------------------------------------
	// | Constructors |
	// +--------------+
	
	/**
	 * Constructs an empty {@code Text} object.
	 */
	public Text() {
		super();
		this.txt = "";
	} // Text()

	/**
	 * Constructs a {@code Text} using the specified string.
	 * @param txt a String
	 */
	public Text(String txt) {
		super();
		this.txt = txt;
	} // Text(String)

	@Override
	public String toString() {
		return txt;
	} // toString()
} // class Text

/**
 * {@code Element} is a subclass of {@code Node} that represents the
 * tag elements components within an {@code HTMl} file.
 * 
 */
class Element extends Node {
	
	// +--------+------------------------------
	// | Fields |
	// +--------+
	String tagName;
	HashMap<String, String> attributes;

	// +--------------+-------------------------------------------
	// | Constructors |
	// +--------------+
	
	/**
	 * Constructs an {@code Element} object out of a specified string and
	 * specified hashmap. 
	 * 
	 * @param tagName a {@code String} that represents the name of the tag
	 * @param attributes a {@code HashMap} that represents the attribute-value
	 * pairs that are specified in the tag element.
	 */
	public Element(String tagName, HashMap<String, String> attributes) {
		super();
		this.tagName = tagName;
		this.attributes = attributes;
	} // Element(String, HashMap<String, String>

	/**
	 * Constructs an {@code Element} object out of a specified arraylist, string, and
	 * hashmap.
	 * 
	 * @param children a {@code Node}-parameterized {@code ArrayList} 
	 * @param tagName a {@code String} that denotes the name of the tag element
	 * @param attributes a {@code HashMap} that represents the attribute-value
	 * pairs that are specified in the tag element.
	 */
	public Element(ArrayList<Node> children, String tagName, HashMap<String, String> attributes) {
		super(children);
		this.tagName = tagName;
		this.attributes = attributes;
	} // Element(ArrayList<Node>, String, HashMap<String, String>


	// +---------+-------------------------------------------
	// | Methods |
	// +---------+
	
	/**
	 * Finds and returns an ArrayList containing the value
	 * assignment to a given property for our Node
	 * 
	 * @param property a String
	 * @return
	 */
	public ArrayList<String> getValue(String property) {
		ArrayList<String> value = new ArrayList<String>();
		if (!attributes.containsKey(property)) {
			value.add("");
		}

		else if (property.compareTo("id") == 0) {
			value.add(attributes.get(property));
		}

		else if (property.compareTo("class") == 0) {
			String[] classes = attributes.get(property).split(" ");
			Collections.addAll(value, classes);
		}

		return value;
	} // getValue(String)

	@Override
	public String toString() {
		return tagName + " " + attributes.toString();
	} // toString()
	
} // class Element
