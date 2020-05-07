package renderingengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTMLParser {
	int currPos;
	Queue elementTagsSeen = new PriorityQueue<Node>();
	String input;
	DOM dom;

	public HTMLParser(String input) throws Exception {
		this.currPos = 0;
		this.input = input;
		this.dom = parse();
		int x = 1;
	}

	public DOM parse() throws Exception{
		ArrayList<Node> nodes = parseNodes();
		if (nodes.size() == 0)
			throw new Exception("DOM is empty");

		return new DOM(nodes.get(0));

	}

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

	public boolean eof() {
		return (currPos >= input.length());
	}

	public boolean beginsWith(String str) {
		return (input.substring(currPos, currPos + str.length())).compareTo(str) == 0;
	}

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

	public Node parseNode() {
		char c = input.charAt(currPos);
		if (input.charAt(currPos) == '<')

			// this checks for /br tag
			if (input.substring(currPos, currPos + 5).compareTo("</br>") == 0) {
				currPos += 5;
				return new Element(new ArrayList<Node>(), "br", new HashMap<String, String>());
			} else
				return parseElement();
		else
			return parseText();
	}

	public Element parseElement() {
		currPos++; // skip opening tag

		String tagName = parseName();
		HashMap<String, String> attributes = parseAttributes();
		currPos++; // skip ending tag
		// first check if tagName corresponds to a single tag element
		if (isSingleTagElement(tagName)) {
			Element e = new Element(new ArrayList<Node>(), tagName, attributes);
			//System.out.println(e);
			return e;
		} else {
			ArrayList<Node> children = parseNodes();
			currPos += 2; // skip </
			parseName();
			currPos++; // skip >
			Element e = new Element(children, tagName, attributes);
			//System.out.println(e);
			return e;
		}
	}

	// this could be improved to check
	// a file containing single tag elements
	public boolean isSingleTagElement(String tagName) {
		return (tagName.compareTo("br") == 0) || (tagName.compareTo("IMG") == 0) || (tagName.compareTo("link") == 0);
	}

	public HashMap<String, String> parseAttributes() {
		HashMap<String, String> attributes = new HashMap<String, String>();
		char curr = input.charAt(currPos);
		while (curr != '>') {
			consumeWhiteSpace();
			String attrName = parseName();
			consumeWhiteSpace();
			currPos++;
			consumeWhiteSpace();
			String value = parseAttrValue();
			currPos++;
			consumeWhiteSpace();
			attributes.put(attrName, value);
			curr = input.charAt(currPos);
		}
		return attributes;
	}

	public Text parseText() {
		return new Text(consumeWhile(c -> c != '<'));
	}

	public void consumeWhiteSpace() {
		consumeWhile(c -> Character.isWhitespace(c));
	}

	public String parseName() {
		return consumeWhile(c -> Character.isLetterOrDigit(c));
	}

	public String parseAttrValue() {
		char open = input.charAt(currPos);
		if ((open == '"') | (open == '\'')) {
			currPos++;
			return open + consumeWhile(c -> c != open) + open;
		} else {
			return consumeWhile(c -> !Character.isWhitespace(c));
		}

	}

	// Strips html string of comments
	// source
	// https://stackoverflow.com/questions/1084741/regexp-to-strip-html-comments
	public static String clean(String html) {

		Pattern pxPattern = Pattern.compile("(?=<!--)([\\s\\S]*?)-->");
		Matcher pxMatcher = pxPattern.matcher(html);

		while (pxMatcher.find()) {
			//System.out.println(pxMatcher.group());
			String htmlString = pxMatcher.group();
			html = html.replace(htmlString, "");

		}

		return html;

	}

	public static void main(String[] args) throws Exception {
		new HTMLParser("<html>\n" + "    <body>\n" + "        <h1>Title</h1>\n"
				+ "        <div id=\"main\" class=\"test\">\n" + "            <p>Hello <em>world</em>!</p>\n"
				+ "        </div>\n" + "    </body>\n" + "</html>");

		new HTMLParser("<html>\n" + "<body>\n" + "\n" + "<h1>My First Heading</h1>\n" + "\n"
				+ "<p>My first paragraph.</p>\n" + "\n" + "</body>\n" + "</html>\n" + "\n" + "");
		
		

	}

}
