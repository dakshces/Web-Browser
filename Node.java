package renderingengine;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Node {
	
	// +--------+------------------------------
	// | Fields |
	// +--------+
	ArrayList<Node> children;

	// +--------------+-------------------------------------------
	// | Constructors |
	// +--------------+

	public Node() {
		children = new ArrayList<Node>();
	} // Node()

	public Node(ArrayList<Node> children) {
		this.children = children;
	} // Node(ArrayList<Node>)

}// class Node

class Text extends Node {

	// +--------+------------------------------
	// | Fields |
	// +--------+
	String txt;
	Font font;

	// +--------------+-------------------------------------------
	// | Constructors |
	// +--------------+
	public Text() {
		super();
		this.txt = "";
		this.font = null;
	}

	public Text(String txt) {
		super();
		this.txt = txt;
		font = null;
	}

	@Override
	public String toString() {
		return txt;
	}
} // class Text

class Element extends Node {
	
	// +--------+------------------------------
	// | Fields |
	// +--------+
	String tagName;
	HashMap<String, String> attributes;

	// +--------------+-------------------------------------------
	// | Constructors |
	// +--------------+
	public Element(String tagName, HashMap<String, String> attributes) {
		super();
		this.tagName = tagName;
		this.attributes = attributes;
	} // Element(String, HashMap<String, String>

	public Element(ArrayList<Node> children, String tagName, HashMap<String, String> attributes) {
		super(children);
		this.tagName = tagName;
		this.attributes = attributes;
	} // Elemenet(ArrayList<Node>, String, HashMap<String, String>


	// +---------+-------------------------------------------
	// | Methods |
	// +---------+
	
	/**
	 * Finds and returns an ArrayList containg the value
	 * assignment to a given property for our Node
	 * @param property
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
