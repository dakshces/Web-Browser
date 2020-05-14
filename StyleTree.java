package renderingengine;


import java.awt.Font;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * <p>
 * Builds the Style Tree by merging a {@code DOM} with a {@code StyleSheet}
 * <p>
 * 
 * The implementation is based on the tutorial by Matt Brubeck with added features such as text styling and descendant selectors
 * @see <a href=
 *      "https://limpet.net/mbrubeck/2014/08/23/toy-layout-engine-4-style.html">
 *     </a>
 */
public class StyleTree{
	// +--------+----------------------------------
	// | Fields |
	// +--------+
	StyledNode root;

	// +--------------+---------------------------
	// | Constructors |
	// +--------------+

	/**
	 * Constructs a {@code StyleTree} by attaching the specified {@code StyleSheet} with the {@code DOM} 
	 * 
	 * @param dom a {@code DOM}
	 * @param sheet a {@code StyleSheet}
	 */
	public StyleTree(DOM dom, Stylesheet sheet){
		ArrayList<Element> ancestors = new ArrayList<Element>();
		root = StyleNode(dom.root,null,sheet,ancestors);	
	}

	// +---------+--------------------------------------------------
	// | Methods |
	// +---------+

	/**
	 * Creates a {@code StyledNode} from the given {@code Node} by traversing the rules of the {@code StyleSheet}
	 * 
	 * @param n a {@code Node} that needs to be styled
	 * @param parent a {@code StyledNode} is the styled parent of n
	 * @param sheet a {@code StyleSheet} is the stylesheet to be used for styling n
	 * @param ancestors a {@code ArrayList} contains the ancestors of n from the {@code DOM}
	 * @return a {@code StyledNode} representing the node n after matching rules from sheet have been applied
	 */
	public StyledNode StyleNode(Node n, StyledNode parent, Stylesheet sheet, ArrayList<Element> ancestors){
		StyledNode sn = new StyledNode();
		if(n instanceof Element){	
			sn.tagName = ((Element) n).tagName;
			sn.attributes = ((Element) n).attributes;
			sn.specifiedVals = matchingRules((Element) n, sheet,ancestors);
			ancestors.add((Element) n);
		}

		inheritTextStyle(sn, parent);

		if(n instanceof Text){
			sn.cont.txt = ((Text) n).txt;
			styleText(sn);
		}


		for(Node child: n.children){
			int last = ancestors.size();
			sn.children.add(StyleNode(child,sn,sheet,ancestors));
			ancestors.subList(last, ancestors.size()).clear();
		}

		return sn;
	}


	/**
	 * Inherit {@code Font} properties from parent to child {@code StyledNode}
	 * 
	 * @param  sn is a {@code StyledNode} is a child node of parent
	 * @param parent is a {@code StyledNode} 
	 */
	public void inheritTextStyle(StyledNode sn, StyledNode parent){
		if(parent != null){
			Value family = parent.getValue("font-family");
			if(family.exists && !sn.getValue("font-family").exists){
				sn.specifiedVals.put("font-family", family);
			}

			Value style = parent.getValue("font-style");
			if(style.exists && !sn.getValue("font-style").exists){
				sn.specifiedVals.put("font-style", style);
			}

			Value weight = parent.getValue("font-weight");
			if(weight.exists && !sn.getValue("font-weight").exists){
				sn.specifiedVals.put("font-weight", weight);
			}

			Value size = parent.getValue("font-size");
			if(size.exists && !sn.getValue("font-size").exists){
				sn.specifiedVals.put("font-size", size);
			}

			Value color = parent.getValue("color");
			if(color.exists && !sn.getValue("color").exists){
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
		}
	}

	/**
	 * Style the text (if it exists) contained in the current node  
	 * Default font is Arial Plain with size 18pt
	 * @param sn is a {@code StyledNode} whose font attributes have to be applied to the text it contains
	 */
	public void styleText(StyledNode sn){
		Value family0 = sn.getValue("font-family");
		String family = "Arial";
		if(family0.exists){
			family = ((Keyword) family0).name;
		}

		Value style0 =  sn.getValue("font-style");
		int style = Font.PLAIN;
		if(style0.exists){
			String val = ((Keyword) style0).name;

			if(val.equalsIgnoreCase("italic") || val.equalsIgnoreCase("oblique")) 
				style = Font.ITALIC;
		}

		Value weight0 = sn.getValue("font-weight");
		String weight = "";
		if(weight0.exists){
			if(weight0 instanceof Keyword){
				String val = ((Keyword) weight0).name.toLowerCase();
				if(val.contains("bold"))
					weight = "bold";
			}

			else if(weight0 instanceof Length){
				double val = ((Length) weight0).len;
				if(val > 400)
					weight = "bold";
			}	
		}

		Value size0 =  sn.getValue("font-size");
		int size = 	18;
		if(size0.exists){
			if(size0 instanceof Keyword){
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

			else if(size0 instanceof Length){
				size = (int) ((Length) size0).len;
			}	
		}


		try{
			Font font = new Font(family + " " + weight, style, size*3/4); //convert to size to pt from px
			sn.cont.font = font;
		}
		catch(Exception e)
		{
			sn.cont.font = new Font("Arial", Font.PLAIN, 18);
		}
	}

	/**
	 * Checks if a {@code Selector} matches an {@code Element}
	 * 
	 * @param el is a {@code Element} that needs to be matched with the given selector
	 * @param selector is a {@code Selector} 
	 * @param ancestors is a {@code ArrayList} of Elements containing the descendants of el extracted from the {@code DOM}
	 * @return true if el matches selector otherwise false
	 */
	public boolean matches(Element el, Selector selector, ArrayList<Element> ancestors){
		if (selector instanceof SimpleSelector)
			return matchesSimpleSelector(el, (SimpleSelector) selector);

		if(selector instanceof DescendantSelector)
			return matchesDescendantSelector(el, (DescendantSelector) selector, ancestors);

		return false;
	}

	/**
	 * Checks if a {@code SimpleSelector} matches an {@code Element}
	 * 
	 * @param el is a {@code Element} that needs to be matched with the given simple selector
	 * @param selector is a {@code SimpleSelector} 
	 * @return true if el matches selector otherwise false
	 */
	public boolean matchesSimpleSelector(Element el, SimpleSelector selector){
		String elName = el.tagName;
		if(selector.tagName.compareTo("")!= 0 && elName.compareTo(selector.tagName) != 0)
			return false;

		String elId = el.getValue("id").get(0);
		if(selector.id.compareTo("")!= 0 && elId.compareTo(selector.id) != 0)
			return false;

		ArrayList<String> elClasses = el.getValue("class");
		for(String clas: selector.classes){
			boolean flag = false;
			for(String eClas: elClasses){
				if(!validIdentifierChar(eClas.charAt(0)))
					eClas = eClas.substring(1,eClas.length()-1);
				if(eClas.compareTo(clas) == 0){
					flag = true;
					break;
				}
			}
			if(!flag)
				return false;
		}
		return true;
	}

	/**
	 * Checks if a {@code DescendantSelector} matches an {@code Element}
	 * 
	 * @param el is a {@code el} that needs to be matched with the given descendant selector
	 * @param selector is a {@code DescendantSelector} 
	 * @param ancestors is a {@code ArrayList} of Elements containing the descendants of el extracted from the {@code DOM}
	 * @return true if el matches selector otherwise false
	 */
	public boolean matchesDescendantSelector(Element el,  DescendantSelector selector, ArrayList<Element> ancestors){

		if(!matchesSimpleSelector(el, selector.chain.get(selector.chain.size()-1)))
			return false;

		boolean ancestFlag = false;
		SimpleSelector first = selector.chain.get(0);
		for(Element anc: ancestors){
			if(matchesSimpleSelector(anc,first)){
				ancestFlag = true;
				break;
			}
		}
		if(!ancestFlag)
			return false;

		return true;
	}

	/**
	 * Checks if a {@code char} is a valid HTML identifier
	 * 
	 * @param c is a {@code char}
	 * @return true if c is a letter, digit, '-', or '_' otherwise false
	 */
	public boolean validIdentifierChar(char c){
		return (c == '-' || c == '_' || Character.isLetterOrDigit(c));
	}


	/**
	 * Collects all rules matching an {@code Element} in the given {@code StyleSheet}
	 * 
	 * @param el is a {@code Element} that needs to be matched
	 * @param sheet is a {@code StyleSheet} 
	 * @param ancestors is a {@code ArrayList} of Elements containing the descendants of el extracted from the {@code DOM}
	 * @return a {@code HashMap<String,Value>} representing the attribute name and corresponding values that apply to el
	 */
	public HashMap<String, Value> matchingRules(Element el, Stylesheet sheet, ArrayList<Element> ancestors){
		HashMap<String, Value> specifiedVals = new HashMap<String, Value>();
		ArrayList<MatchedRule> match = new ArrayList<MatchedRule>();

		for(Rule rule: sheet.rules){
			for(Selector selector: rule.selectors){
				if(matches(el, selector, ancestors)){
					match.add(new MatchedRule(rule,selector.getSpec()));
					break;
				}
			}
		}
		Collections.sort(match); //Sort by specificity in ascending order

		for(MatchedRule mrule: match){
			for(Declaration decl: mrule.rule.declarations){
				specifiedVals.put(decl.name, decl.value);
			}
		}
		return specifiedVals; 
	}

}

/**
 * <p>
 * {@code StyledNode} is a subclass of {@code Node} that represents a node in the {@code StyleTree} that has been styled
 * <p>
 */
class StyledNode extends Node
{
	// +--------+----------------------------------
	// | Fields |
	// +--------+
	String tagName;
	HashMap<String, Value> specifiedVals; 
	HashMap<String, String> attributes;
	Text cont;

	// +--------------+-------------------------------------------
	// | Constructors |
	// +--------------+

	/**
	 * Constructs a {@code StyledNode} with default initial values
	 */
	public StyledNode(){
		super();
		specifiedVals = new HashMap<String, Value>();
		attributes = new HashMap<String, String>();
		cont = new Text();
		tagName = "";
	}

	/**
	 * Make a copy of the current object that points to the same specifiedVals as current object
	 * and has same tagName too. But a new copy of the cont field is made.
	 * This method is required in splitting text when it is being laid out
	 * @return a {@code StyledNode}
	 */
	public StyledNode copy(){
		StyledNode copy = new StyledNode();
		copy.specifiedVals = specifiedVals;
		copy.cont.font = cont.font;
		copy.cont.txt = cont.txt;
		copy.tagName = tagName;
		copy.attributes = attributes;
		return copy;
	}

	/**
	 * Get value corresponding to key from specifiedVals
	 * @param key is a {@code String}
	 * @return the {@code Value} that key maps to in specifiedVals
	 */
	public Value getValue(String key){
		if(specifiedVals.containsKey(key))
			return specifiedVals.get(key);

		return new Value(false);
	}

	/**
	 * Get display property, with default value of "inline"
	 * @return a {@code String}
	 */
	public String display(){
		Value val = getValue("display");
		if(val instanceof Keyword){
			Keyword prop = (Keyword) val;
			if(prop.name.compareTo("block") == 0 || prop.name.compareTo("none") == 0)
				return prop.name;
		}
		return "inline";
	}

	/**
	 * Lookup specifiedVals for key; if key doesn't exist, try backup;
	 * if backup also doesn't exist, then return fallback
	 * @param key is a {@code String}
	 * @param backup is a {@code String}
	 * @param fallback is a default {@code Value} 
	 * @return a {@code Value}
	 */
	public Value lookup(String key, String backup, Value fallback){
		Value val1 = getValue(key);
		if(val1.exists)
			return val1;
		Value val2 = getValue(backup);
		if(val2.exists)
			return val2;
		return fallback;
	}
}

/**
 * <p>
 * {@code MatchedRule} represents a pair ({@code Rule}, {@code Specificity})
 * It implements comparison with respect to Specificity
 * <p>
 */
class MatchedRule implements Comparable<MatchedRule>{
	// +--------+----------------------------------
	// | Fields |
	// +--------+
	Rule rule;
	Specificity spec;

	// +--------------+-------------------------------------------
	// | Constructors |
	// +--------------+

	/**
	 * Constructs a {@code MatchedRule} object with given values
	 * @param rule a {@code Rule}
	 * @param spec a {@code Specificity}
	 */
	public MatchedRule(Rule rule, Specificity spec){
		this.rule = rule;
		this.spec = spec;
	}

	/**
	 * Compare current object with specified {@code MatchedRule} obj on basis of 
	 * {@code Specificity} in ascending order (with respect to canonical ordering of triples)
	 * @param obj a {@code MatchedRule}
	 * return positive {@code int} if the specificity of current object is greater than passed object's 
	 * 		  {@value 0} if the specificity of current object is equal to passed object's
	 * 		  negative {@code int} otherwise	
	 */	      
	public int compareTo(MatchedRule obj){
		if(obj.spec.a != this.spec.a)
			return this.spec.a - obj.spec.a;

		else if(obj.spec.b != this.spec.b)
			return this.spec.b - obj.spec.b;

		return this.spec.c - obj.spec.c;
	}
}