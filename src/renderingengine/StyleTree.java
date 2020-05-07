import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class StyleTree
{
	StyledNode root;

	public StyleTree(DOM dom, Stylesheet sheet)
	{
		root = StyleNode(dom.root,sheet);	
		int x = 1;
	}

	public StyledNode StyleNode(Node n, Stylesheet sheet)
	{
		StyledNode sn = new StyledNode();
		if(n instanceof Element)
			sn.specifiedVals = matchingRules((Element) n, sheet);


		for(Node child: n.children)
			sn.children.add(StyleNode(child,sheet));

		return sn;
	}

	public boolean matches(Element el, Selector selector)
	{
		if (selector instanceof SimpleSelector)
			return mathchesSimpleSelector(el, (SimpleSelector) selector);

		return false;
	}

	public boolean mathchesSimpleSelector(Element el, SimpleSelector selector)
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


	public HashMap<String, Value> matchingRules(Element el, Stylesheet sheet)
	{
		HashMap<String, Value> specifiedVals = new HashMap<String, Value>();
		ArrayList<MatchedRule> match = new ArrayList<MatchedRule>();

		for(Rule rule: sheet.rules)
		{
			for(Selector selector: rule.selectors)
			{
				if(matches(el, selector))
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

	public StyledNode()
	{
		super();
		specifiedVals = new HashMap<String, Value>();
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