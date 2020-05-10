package renderingengine;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class StyleTree
{
	StyledNode root;

	public StyleTree(DOM dom, Stylesheet sheet)
	{
		ArrayList<Element> ancestors = new ArrayList<Element>();
		root = StyleNode(dom.root,null,sheet,ancestors);	
		int x = 1;
	}

	
	public StyledNode StyleNode(Node n, StyledNode parent, Stylesheet sheet, ArrayList<Element> ancestors)
	{
		StyledNode sn = new StyledNode();
		if(n instanceof Element)
		{	
			sn.tagName = ((Element) n).tagName;
			sn.specifiedVals = matchingRules((Element) n, sheet,ancestors);
			sn = modifyIfPhraseTag(sn);
			ancestors.add((Element) n);
		}

		inheritTextStyle(sn, parent);

		if(n instanceof Text)
		{
			sn.cont.txt = ((Text) n).txt;
			styleText(sn);
		}


		for(Node child: n.children)
			sn.children.add(StyleNode(child,sn,sheet,ancestors));

		return sn;
	}

	

	public StyledNode modifyIfPhraseTag(StyledNode sn)
	{
		if(sn.tagName.equals("em"))
		{
			sn.specifiedVals.put("display", new Keyword ("inline"));
			sn.specifiedVals.put("font-style", new Keyword("italic"));
		}

		if(sn.tagName.equals("bold"))
		{
			sn.specifiedVals.put("display", new Keyword ("inline"));
			sn.specifiedVals.put("font-weight", new Keyword("bold"));
		}
		
		return sn;
	}
	
	public void inheritTextStyle(StyledNode sn, StyledNode parent)
	{
		if(parent != null)
		{
			Value family = parent.getValue("font-family");
			if(family.exists && !sn.getValue("font-family").exists)
			{
				sn.specifiedVals.put("font-family", family);
			}

			Value style = parent.getValue("font-style");
			if(style.exists && !sn.getValue("font-style").exists)
			{
				sn.specifiedVals.put("font-style", style);
			}

			Value weight = parent.getValue("font-weight");
			if(weight.exists && !sn.getValue("font-weight").exists)
			{
				sn.specifiedVals.put("font-weight", weight);
			}

			Value size = parent.getValue("font-size");
			if(size.exists && !sn.getValue("font-size").exists)
			{
				sn.specifiedVals.put("font-size", size);
			}
			
			Value color = parent.getValue("color");
			if(color.exists && !sn.getValue("color").exists)
			{
				sn.specifiedVals.put("color", color);
			}
			
			
			for(Entry<String,Value> e: sn.specifiedVals.entrySet())
			{
				if(e.getKey().contains("REL-"))
				{
					String key = e.getKey().replace("REL-", "");
					if(parent.specifiedVals.containsKey(key))
					{
						Length childVal = (Length) e.getValue();
						Length parentVal = (Length) parent.getValue(key);
						e.setValue(new Length(childVal.len*childVal.len/100,parentVal.unit));
					}
				}
						
			}

			//			Value font = parent.getValue("font");
			//			if(family.exists && !sn.getValue("font").exists)
			//			{
			//				sn.specifiedVals.put("font", font);
			//			}
		}
	}
	
	public void styleText(StyledNode sn)
	{
		Value family0 = sn.getValue("font-family");
		String family = "Arial";
		if(family0.exists)
		{
			family = ((Keyword) family0).name;
		}

		Value style0 =  sn.getValue("font-style");
		int style = Font.PLAIN;
		if(style0.exists)
		{
			String val = ((Keyword) style0).name;

			if(val.equalsIgnoreCase("italic") || val.equalsIgnoreCase("oblique")) 
				style = Font.ITALIC;
		}


		Value weight0 = sn.getValue("font-weight");
		String weight = "";
		if(weight0.exists)
		{
			if(weight0 instanceof Keyword)
			{
				String val = ((Keyword) weight0).name.toLowerCase();
				if(val.contains("bold"))
					weight = "bold";
			}

			else if(weight0 instanceof Length)
			{
				double val = ((Length) weight0).len;
				if(val > 400)
					weight = "bold";
			}	
		}

		Value size0 =  sn.getValue("font-size");
		int size = 	18;
		if(size0.exists)
		{
			if(size0 instanceof Keyword)
			{
				String val = ((Keyword) weight0).name.toLowerCase();
				if(val.contains("xx-small"))
					size = 8;

				else if(val.contains("x-small"))
					size = 13;

				else if(val.contains("small"))
					size = 18;

				else if(val.contains("medium"))
					size = 23;

				else if(val.contains("xx-large"))
					size = 36;

				else if(val.contains("x-large"))
					size = 31;

				else if(val.contains("large"))
					size = 28;
			}

			else if(size0 instanceof Length)
			{
				size = (int) ((Length) size0).len;
			}	
		}
		

		try
		{
			Font font = new Font(family + " " + weight, style, size*3/4); //convert to size to pt
			sn.cont.font = font;
		}
		catch(Exception e)
		{
			sn.cont.font = new Font("Arial", Font.PLAIN, 18);
		}
	}

	public boolean matches(Element el, Selector selector, ArrayList<Element> ancestors)
	{
		if (selector instanceof SimpleSelector)
			return matchesSimpleSelector(el, (SimpleSelector) selector);
		
		if(selector instanceof DescendantSelector)
			return matchesDescendantSelector(el, (DescendantSelector) selector, ancestors);

		return false;
	}
	
	public boolean matchesDescendantSelector(Element el,  DescendantSelector selector, ArrayList<Element> ancestors)
	{
		
		if(!matchesSimpleSelector(el, selector.chain.get(selector.chain.size()-1)))
			return false;
		
		boolean ancestFlag = false;
		SimpleSelector first = selector.chain.get(0);
		for(Element anc: ancestors)
		{
			if(matchesSimpleSelector(anc,first))
			{
				ancestFlag = true;
				break;
			}
		}
		if(!ancestFlag)
			return false;
		
		return true;
	}

	public boolean matchesSimpleSelector(Element el, SimpleSelector selector)
	{
		String elName = el.tagName;
		if(selector.tagName.compareTo("")!= 0 && elName.compareTo(selector.tagName) != 0)
			return false;

		String elId = el.getValue("id").get(0);
		if(selector.id.compareTo("")!= 0 && elId.compareTo(selector.id) != 0)
			return false;

		ArrayList<String> elClasses = el.getValue("class");
		for(String clas: selector.classes)
		{
			boolean flag = false;
			for(String eClas: elClasses)
			{
				if(!validIdentifierChar(eClas.charAt(0)))
					eClas = eClas.substring(1,eClas.length()-1);
				if(eClas.compareTo(clas) == 0)
				flag = true;
			}
			if(!flag)
				return false;
		}
		return true;
	}
	
	public boolean validIdentifierChar(char c)
	{
		return (c == '-' || c == '_' || Character.isLetterOrDigit(c));
	}


	public HashMap<String, Value> matchingRules(Element el, Stylesheet sheet, ArrayList<Element> ancestors)
	{
		HashMap<String, Value> specifiedVals = new HashMap<String, Value>();
		ArrayList<MatchedRule> match = new ArrayList<MatchedRule>();

		for(Rule rule: sheet.rules)
		{
			for(Selector selector: rule.selectors)
			{
				if(matches(el, selector, ancestors))
				{
					match.add(new MatchedRule(rule,selector.spec));
					break;
				}
			}
		}
		Collections.sort(match); //Sort by specificity in ascending order

		for(MatchedRule mrule: match)
		{
			for(Declaration decl: mrule.rule.declarations)
			{
				specifiedVals.put(decl.name, decl.value);
			}
		}
		return specifiedVals; 
	}

}

class StyledNode extends Node
{
	HashMap<String, Value> specifiedVals;
	Text cont;
	String tagName;

	public StyledNode()
	{
		super();
		specifiedVals = new HashMap<String, Value>();
		cont = new Text();
		tagName = "";
	}

	public StyledNode copy()
	{
		StyledNode copy = new StyledNode();
		copy.specifiedVals = specifiedVals;
		copy.cont.font = cont.font;
		copy.cont.txt = cont.txt;
		copy.tagName = tagName;
		return copy;
	}
	public Value getValue(String key)
	{
		if(specifiedVals.containsKey(key))
			return specifiedVals.get(key);

		return new Value(false);
	}

	public String display()
	{
		Value val = getValue("display");
		if(val instanceof Keyword)
		{
			Keyword prop = (Keyword) val;
			if(prop.name.compareTo("block") == 0 || prop.name.compareTo("none") == 0)
				return prop.name;
		}
		return "inline";
	}
	
	public Value lookup(String key, String backup, Value fallback)
	{
		Value val1 = getValue(key);
		if(val1.exists)
			return val1;
		Value val2 = getValue(backup);
		if(val2.exists)
			return val2;
		return fallback;
	}

}

class MatchedRule implements Comparable<MatchedRule>
{
	Rule rule;
	Specificity spec;

	public MatchedRule(Rule rule, Specificity spec)
	{
		this.rule = rule;
		this.spec = spec;
	}

	public int compareTo(MatchedRule obj)
	{
		if(obj.spec.a != this.spec.a)
			return this.spec.a - obj.spec.a;

		else if(obj.spec.b != this.spec.b)
			return this.spec.b - obj.spec.b;

		return this.spec.c - obj.spec.c;
	}
}