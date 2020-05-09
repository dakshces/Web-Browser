package renderingengine;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
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
		Font font;

		public Text()
		{
			super();
			this.txt = "";
			this.font = null;
		}

		public Text(String txt)
		{
			super();
			this.txt = txt;
			font = null;
		}
		
		public String toString() 
		{
			return txt;
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
		
		public ArrayList<String> getValue(String key)
		{
			ArrayList<String> value = new ArrayList<String>();
			if(!attributes.containsKey(key))
			{
				value.add("");
			}
			
			else if(key.compareTo("id") == 0)
			{
				value.add(attributes.get(key));
			}
			
			else if(key.compareTo("class") == 0)
			{
				String [] classes = attributes.get(key).split(" ");
				Collections.addAll(value, classes);
			}
			
			return value;
		}
		
		@Override
		public String toString() {
			return tagName + " " + attributes.toString();
		}
	}




