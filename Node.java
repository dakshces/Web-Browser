import java.util.ArrayList;
import java.util.HashMap;

public class Node 
{
	ArrayList<Node> children;

	public Node()
	{
		children = new ArrayList<Node>();
	}
	
	public Node(ArrayList<Node> children)
	{
		this.children = children;
	}
	
	}
	class Text extends Node
	{
		String txt;

		public Text(String txt)
		{
			super();
			this.txt = txt;
		}
	}

	class Element extends Node
	{
		String tagName;
		HashMap<String,String> attributes; 

		public Element(String tagName, HashMap<String,String> attributes)
		{
			super();
			this.tagName = tagName;
			this.attributes = attributes;
		}
		
		public Element(ArrayList<Node> children, String tagName, HashMap<String,String> attributes)
		{
			super(children);
			this.tagName = tagName;
			this.attributes = attributes;
		}
	}




