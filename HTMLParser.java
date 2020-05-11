package renderingengine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTMLParser {
	int currPos;
	String input;
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

	/**
	 * Constructor
	 * 
	 * @param input, a string representation of html
	 * @throws Exception
	 */
	public HTMLParser(String input) throws Exception {
		this.currPos = 0;
		this.input = input;
		clean();
		this.dom = parse();
		int x = 1;
	}

	/**
	 * Creates a DOM object tree of the html string input
	 * 
	 * @return
	 * @throws Exception
	 */
	public DOM parse() throws Exception {
		ArrayList<Node> nodes = parseNodes();
		if (nodes.size() == 0)
			throw new Exception("DOM is empty");

		return new DOM(nodes.get(0));

	}

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
				consumeWhiteSpace();

		}
		return nodes;
	}

	/**
	 * Checks if our parser has reached the end of the html string.
	 * 
	 * @return
	 */
	public boolean eof() {
		return (currPos >= input.length());
	}

	/**
	 * 
	 * @param str
	 * @return
	 */
	public boolean beginsWith(String str) {
		return (input.substring(currPos, currPos + str.length())).compareTo(str) == 0;
	}

	/**
	 * Creates a substring starting with the char at currpos of the String input to
	 * up until the char fails to satisfy charTest
	 * 
	 * @param charTest, a Predicate
	 * @return the first substring starting from currpos consisting only of
	 *         characters satisfying the Predicate charTest
	 */
	public String consumeWhile(Predicate<Character> charTest) {
		String res = "";

		while (!eof() && charTest.test((Character) (input.charAt(currPos)))) {
			char c = input.charAt(currPos);
			if (Character.isWhitespace(c)) {
				res += ' ';
			} else {
				res += c;
			}
			currPos++;
		}
		return res;
	}

	public int advanceWhile(Predicate<Character> charTest)
	{
		int i = 0;
		while(currPos + i < input.length() && charTest.test((Character) (input.charAt(currPos+i))))
		{
			i++;
		}
		if(currPos + i >= input.length())
			return 0;
		return i;
	}


	/**
	 * Creates a complete Node and its corresponding children starting from currpos
	 * 
	 * // this checks for /br tag
			if (input.substring(currPos, currPos + 5).compareTo("</br>") == 0) {
				currPos += 5;
				return new Element(new ArrayList<Node>(), "br", new HashMap<String, String>());
			} else
	 * @return
	 */
	public Node parseNode() {
		char c = input.charAt(currPos);
		if (input.charAt(currPos) == '<')
		{
			return parseElement();
		}
		else
			return parseText();
	}

	/**
	 * Creates an Element Node along with its corresponding children
	 * 
	 * @return
	 */
	public Element parseElement() {
		currPos++; // skip opening tag

		String tagName = parseName();
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
	}

	/**
	 * Checks if a html tag name corresponds to an html single tag
	 * 
	 * @param tagName
	 * (tagName.compareTo("hr") == 0) || (tagName.compareTo("br") == 0) || (tagName.compareTo("IMG") == 0)
				|| (tagName.compareTo("link") == 0);
	 * @return
	 * 
	 */
	public boolean isSingleTagElement(String tagName) {
		return  SingletonTags.contains(tagName);

	}

	/**
	 * Retrieves the attributes of an html tag found in its opening tag
	 * 
	 * @return
	 */
	public HashMap<String, String> parseAttributes() {
		HashMap<String, String> attributes = new HashMap<String, String>();
		char curr = input.charAt(currPos);
		while (curr != '>') {
			consumeWhiteSpace();
			String attrName = parseName();
			consumeWhiteSpace();
			currPos++; // skip =
			consumeWhiteSpace();
			String value = parseAttrValue();
			currPos++;
			consumeWhiteSpace();
			attributes.put(attrName, value);
			curr = input.charAt(currPos);
			if(curr == ',')
				currPos++;
		}
		return attributes;
	}

	/**
	 * Constructs a Text Node
	 * 
	 * @return
	 */
	public Text parseText() {
		return new Text(consumeWhile(c -> c != '<'));
	}

	/**
	 * Changes currpos to the index of the next non-whitespace character. currpos is
	 * unchanged if input.charAt(currpos) is not a whitespace character.
	 */
	public void consumeWhiteSpace() {
		consumeWhile(c -> Character.isWhitespace(c));
	}

	/**
	 * Determines the tag name of the html tag whose '<' is at currpos
	 * 
	 * @return
	 */
	public String parseName() {
		return consumeWhile(c -> Character.isLetterOrDigit(c));
	}

	/**
	 * Retrieves the attributes of the html tag
	 * 
	 * @return
	 */
	public String parseAttrValue() {
		char open = input.charAt(currPos);
		if ((open == '"') | (open == '\'')) {
			currPos++;
			return open + consumeWhile(c -> c != open) + open;
		} else {
			return consumeWhile(c -> !Character.isWhitespace(c));
		}

	}

	/**
	 * Removes the comments from an html string Source :
	 * https://stackoverflow.com/questions/1084741/regexp-to-strip-html-comments
	 * 
	 * @param html
	 * @return
	 */
	public void clean() {

		String html = input;
		html = html.replaceAll("\\s+", " ");
		html = html.replaceAll("(\\s+)=(\\s+)", "=");
		Pattern pxPattern = Pattern.compile("(?=<!--)([\\s\\S]*?)-->");
		Matcher pxMatcher = pxPattern.matcher(html);

		while (pxMatcher.find()) {
			String htmlString = pxMatcher.group();
			html = html.replace(htmlString, "");
		}

		input = html.replaceAll("\\s*<br>\\s*", "BREAKLIEN"); //purposely spelt wrong to avoid collision with content
		//input = html.replaceAll("<head>.*</head>", "");
		if(!input.contains("<html>"))
			input = "<html>" + input;
		if(!input.contains("</html>"))
			input += "</html>";
	}

	public static void main(String[] args) throws Exception {

		HTMLParser test = new HTMLParser(
				"<A HREF=\"http://www.villanova.edu/artsci/mathematics/\", target=\"_blank\"> Villanova\n"
						+ "                                 University</a>.");
		// test.dom.print();
	}

} // HTMLParser
