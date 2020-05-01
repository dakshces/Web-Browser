import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Predicate;

public class HTMLParser 
{
	int currPos;
	String input;
	DOM dom;

	public HTMLParser(String input)
	{
		this.currPos = 0;
		this.input = input;
		this.dom = parse();
		int x = 1;
	}


	public DOM parse()
	{
		ArrayList<Node> nodes = parseNodes();
		if(nodes.size() == 1)
			return new DOM(nodes.get(0));
		
		return new DOM(new Element(nodes, "html", new HashMap<String,String>()));

	}

	public ArrayList<Node> parseNodes()
	{
		ArrayList<Node> nodes = new ArrayList<Node>();
		consumeWhiteSpace();
		while(!eof() && !beginsWith("</"))
		{
			nodes.add(parseNode());
			consumeWhiteSpace();
		}
		return nodes;
	}
	
	public boolean eof()
	{
		return (currPos >= input.length());
	}


	public boolean beginsWith(String str)
	{
		return (input.substring(currPos, currPos+str.length())).compareTo(str) == 0;
	}

	public String consumeWhile(Predicate<Character> charTest)
	{
		String res = "";
		while(!eof() && charTest.test((Character) (input.charAt(currPos))))
		{
			res += input.charAt(currPos);
			currPos++;
		}
		return res;
	}

	public Node parseNode()
	{
		char c = input.charAt(currPos);
		if(input.charAt(currPos) == '<')
			return parseElement();
		else
			return parseText();
	}

	public Element parseElement()
	{
		currPos++; //skip opening tag
		String tagName = parseName();
		HashMap<String,String> attributes = parseAttributes();
		currPos++; //skip ending tag
		ArrayList<Node> children = parseNodes();
		currPos+=2; //skip </
		parseName();
		currPos++; //skip >
		return new Element(children, tagName, attributes);
	}

	public HashMap<String,String> parseAttributes()
	{
		HashMap<String,String> attributes = new HashMap<String,String>();
		while(input.charAt(currPos) != '>')
		{
			consumeWhiteSpace();
			String attrName = parseName();
			consumeWhiteSpace();
			currPos++ ;
			consumeWhiteSpace();
			String value =  parseAttrValue();
			currPos++;
			attributes.put(attrName, value);
			
		}
		return attributes;
	}

	public Text parseText()
	{
		return new Text(consumeWhile(c -> c != '<'));
	}

	public void consumeWhiteSpace()
	{
		consumeWhile(c -> Character.isWhitespace(c));
	}

	public String parseName()
	{
		return consumeWhile(c -> Character.isLetterOrDigit(c));
	}
	
	public String parseAttrValue()
	{
		char open = input.charAt(currPos);
		currPos++;
		return open + consumeWhile(c -> c != open) + open;
		
	}
	

	public static void main(String[] args)
	{
		new HTMLParser("<html>\n" + 
				"    <body>\n" + 
				"        <h1>Title</h1>\n" + 
				"        <div id=\"main\" class=\"test\">\n" + 
				"            <p>Hello <em>world</em>!</p>\n" + 
				"        </div>\n" + 
				"    </body>\n" + 
				"</html>");
		
		new HTMLParser("<html>\n" + 
				"<body>\n" + 
				"\n" + 
				"<h1>My First Heading</h1>\n" + 
				"\n" + 
				"<p>My first paragraph.</p>\n" + 
				"\n" + 
				"</body>\n" + 
				"</html>\n" + 
				"\n" + 
				"");
	}

}
