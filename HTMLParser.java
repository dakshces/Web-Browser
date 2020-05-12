package renderingengine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * An extended version of the HTML parser component of the
 * HTML rendering engine tutorial written by Matt Brubeck.
 * 
 * <p> An HTML specific implementation of the abstract class {@code Parser}.
 * 
 * <p> An {@code HTMLParser} reads its input character by character to
 * construct a Document Object Model of its input.
 * 
 * @see 
 * <a href=https://limpet.net/mbrubeck/2014/08/11/toy-layout-engine-2.html>Matt Brubeck</a>
 */
public class HTMLParser extends Parser {

	// +-------+------------------------------------------------------------
	// | Field |
	// +-------+

	DOM dom;

	static ArrayList<String> SingletonTags = new ArrayList<String>();
	static {
		Scanner scr;
		try {
			scr = new Scanner(new File("./singletontags.txt"));
			while (scr.hasNext())
				SingletonTags.add(scr.next());
			scr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// +--------------+-------------------------------------------
	// | Constructors |
	// +--------------+

	/**
	 * Constructs a {@code HTMLParser} that produces a Document-Object-Model from
	 * specified string representation of an HTML file.
	 * 
	 * @param input an html string
	 * @throws Exception
	 */
	public HTMLParser(String input) throws Exception {
		super(input);
		this.dom = parse();
	} // HTML(String)

	// +---------+--------------------------------------------------
	// | Methods |
	// +---------+

	/**
	 * Creates a DOM object tree of the html string input
	 * 
	 * @return a DOM object
	 * @throws Exception
	 */
	public DOM parse() throws Exception {
		ArrayList<Node> nodes = parseNodes();
		if (nodes.size() == 0)
			throw new Exception("DOM is empty");

		return new DOM(nodes.get(0));

	} // parse()

	/**
	 * Creates an array implementation of the DOM tree
	 * 
	 * @return an array implementation of a tree
	 */
	public ArrayList<Node> parseNodes() {
		ArrayList<Node> nodes = new ArrayList<Node>();
		consumeWhiteSpace();
		while (!eof() && !beginsWith("</")) {
			Node n = parseNode();
			if (n != null)
				nodes.add(n);
			// consumeWhiteSpace();

		}
		return nodes;
	} // parseNodes()

	/**
	 * Creates a complete Node and its corresponding children starting from currpos
	 * 
	 * @return a Node
	 */
	public Node parseNode() {
		if (input.charAt(currPos) == '<') {
			return parseElement();
		} else
			return parseText();
	} // parseNode()

	/**
	 * Creates an Element Node along with its corresponding children
	 * 
	 * @return an Element object
	 */
	public Element parseElement() {
		currPos++; // skip opening tag

		String tagName = parseName();
		tagName = tagName.toLowerCase();
		consumeWhiteSpace();
		HashMap<String, String> attributes = parseAttributes();

		currPos++; // skip ending tag
		// first check if tagName corresponds to a single tag element
		if (isSingleTagElement(tagName)) {
			return new Element(new ArrayList<Node>(), tagName, attributes);
		} else {
			ArrayList<Node> children = parseNodes();
			currPos += 2; // skip </
			parseName();
			currPos++; // skip >
			return new Element(children, tagName, attributes);
		}
	} // parseElement()

	/**
	 * Checks if a html tag name corresponds to an html single tag
	 * 
	 * @param tagName a String
	 * @return true if and only if the specified string corresponds to a single tag
	 * 
	 */
	public boolean isSingleTagElement(String tagName) {
		return SingletonTags.contains(tagName);

	} // isSingleTagElement(String)

	/**
	 * Retrieves the attributes of an html tag found in its opening tag
	 * 
	 * @return a HashMap where the keys are attributes and the values are the
	 *         corresponding attribute-values
	 */
	public HashMap<String, String> parseAttributes() {
		HashMap<String, String> attributes = new HashMap<String, String>();
		char curr = input.charAt(currPos);
		while (curr != '>') {
			consumeWhiteSpace();
			String attrName = parseName();
			attrName = attrName.toLowerCase();
			consumeWhiteSpace();
			currPos++; // skip =
			consumeWhiteSpace();
			String value = parseAttrValue();
			currPos++;
			consumeWhiteSpace();
			attributes.put(attrName, value);
			curr = input.charAt(currPos);
			if (curr == ',')
				currPos++;
		}
		return attributes;
	} // parseAttributes()

	/**
	 * Constructs a Text Node
	 * 
	 * @return a Text object
	 */
	public Text parseText() {
		return new Text(consumeWhile(c -> c != '<'));
	} // parseText()

	/**
	 * Parses a name in the input field
	 * 
	 * @return a String
	 */
	public String parseName() {
		return consumeWhile(c -> Character.isLetterOrDigit(c));
	} // parseName()

	/**
	 * Retrieves the value assignment to an attribute within an html tag
	 * 
	 * @return a String
	 */
	public String parseAttrValue() {
		char open = input.charAt(currPos);
		if ((open == '"') | (open == '\'')) {
			currPos++;
			return open + consumeWhile(c -> c != open) + open;
		} else {
			return consumeWhile(c -> !Character.isWhitespace(c));
		}

	} // parseAttrValue()

	/**
	 * Removes the comments from an html string
	 *
	 * @return
	 */
	@Override
	public void clean() {

		input = input.replaceAll("\\s+", " ");
		input = input.replaceAll("(\\s+)=(\\s+)", "=");
		input = input.replaceAll(">(\\s+)<", "><");
		input = input.replaceAll("(?=<!--)([\\s\\S]*?)-->", "");

		input = input.replaceAll("\\s*<br>\\s*", "BREAKLIEN"); // purposely spelt wrong to avoid collision with content
		// input = html.replaceAll("<head>.*</head>", "");
		if (!input.contains("<html>"))
			input = "<html>" + input;
		if (!input.contains("</html>"))
			input += "</html>";
	} // clean()

	public static void main(String[] args) throws Exception {

		HTMLParser test = new HTMLParser(
				"<A HREF=\"http://www.villanova.edu/artsci/mathematics/\", target=\"_blank\"> Villanova\n"
						+ "                                 University</a>.");
	} // main(String[])

} // Class HTMLParser
