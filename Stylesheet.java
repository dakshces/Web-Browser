package renderingengine;

import java.util.ArrayList;

/**
 * An {@code ArrayList} implementation of a CSS Stylesheet model.
 */
public class Stylesheet {
	// +--------+------------------------------
	// | Fields |
	// +--------+
	ArrayList<Rule> rules;

	// +-------------+----------------------------------------------------
	// | Constructor |
	// +-------------+
	
	/**
	 * Constructs a {@code Stylesheet} object with an empty {@code rules} field.
	 */
	public Stylesheet() {
		this.rules = new ArrayList<Rule>();
	} // Stylesheet()

	/**
	 * Constructs a {@code Stylesheet} object out of a specified 
	 * arraylist parameterized to {@code Rule}.
	 * 
	 * @param rules
	 */
	public Stylesheet(ArrayList<Rule> rules) {
		this.rules = rules;
	} // Stylesheet(ArrayList<Rule>)

	// +---------+----------------------------------------------------
	// | Methods |
	// +---------+
	@Override
	public String toString() {
		String str = "";
		for (Rule r : rules) {
			str += r.toString() + "\n";
		}

		return str;
	} // toString()
} // class Stylesheet

/**
 * {@code Rule} is an object implementation of a CSS rule.
 * 
 * <p>
 * A {@code Rule} object stores the selectors and the declarations in seperate
 * arraylists respectively parameterized to {@code Selector} and
 * {@code Declaration}.
 */
class Rule {
	// +--------+-----------------------------
	// | Fields |
	// +--------+
	ArrayList<Selector> selectors;
	ArrayList<Declaration> declarations;

	// +-------------+----------------------------------------------------
	// | Constructor |
	// +-------------+
	
	/**
	 * Constructs a {@code Rule} object using the specified arraylists respectively
	 * parameterized to {@code Selector} and {@code Declaration}.
	 * 
	 * @param selectors a {@code Selector}-parameterized {@code ArrayList}
	 * @param declarations a {@code Declaration}-parameterized {@code ArrayList}
	 */
	public Rule(ArrayList<Selector> selectors, ArrayList<Declaration> declarations) {
		this.selectors = selectors;
		this.declarations = declarations;
	} // Rule(ArrayList<Selector>, ArrayList<Declaration>)

	// +---------+----------------------------------------------------
	// | Methods |
	// +---------+
	@Override
	public String toString() {
		String str = "";
		str += selectors;
		for (int i = 0; i < declarations.size(); i++) {
			str += "\t" + declarations.get(i) + "\n";
		}
		return str;
	} // toString()
} // class Rule

/**
 * A Selector is an representation of a CSS Declaration selector
 */
class Selector implements Comparable<Selector> {
	// +--------+-----------------------------
	// | Fields |
	// +--------+
	Specificity spec;

	// +-------------+----------------------------------------------------
	// | Constructor |
	// +-------------+
	
	/**
	 * Constructs a {@code Selector} object wit
	 */
	public Selector() {
		spec = new Specificity(0, 0, 0);
	} // Selector()

	// +---------+----------------------------------------------------
	// | Methods |
	// +---------+
	
	/**
	 * Finds the {@code Specificity} of {@code this}
	 * @return {@code this.spec}
	 */
	public Specificity getSpec() {
		return this.spec;
	} // getSpec()

	@Override
	public int compareTo(Selector obj) {
		if (obj.spec.a != this.spec.a)
			return obj.spec.a - this.spec.a;

		else if (obj.spec.b != this.spec.b)
			return obj.spec.b - this.spec.b;

		return obj.spec.c - this.spec.c;
	} // compareTo(Selector)

} // class Selector

/**
 * {@code SimpleSelector} represents a basic CSS selector which consists
 * solely of some combination of tag name, id, and classes. 
 */
class SimpleSelector extends Selector {
	// +--------+-----------------------------
	// | Fields |
	// +--------+
	String tagName;
	String id;
	ArrayList<String> classes;

	// +-------------+----------------------------------------------------
	// | Constructor |
	// +-------------+
	
	/**
	 * Constructs a {@code SimpleSelector} with no tag name, id or classes.
	 * The {@code Specificity} of {@code this} is therefore defaulted to assign
	 * 0 to each of its field.
	 */
	public SimpleSelector() {
		super();
		tagName = "";
		id = "";
		classes = new ArrayList<String>();
	} // SimpleSelector()

	// +---------------------+----------------------------------------------------
	// | Getters and Setters |
	// +---------------------+

	/**
	 * Sets {@code this.tagName} to the specified to string.
	 * 
	 * @param name a {@code String}
	 */
	public void setTagName(String name) {
		tagName = name;
		// spec.c++;
	} // setTagName(String)

	/**
	 * Sets {@code this.id} to the specified to string.
	 * 
	 * @param id a {@code String}
	 */
	public void setId(String id) {
		this.id = id;
		// spec.a++;
	} // setId(String)

	/**
	 * Returns the specificity of {@code this}.
	 */
	public Specificity getSpec() {
		if (tagName != "")
			spec.c = 1;
		if (id != "")
			spec.a = 1;

		spec.c = classes.size();

		return spec;
	} // getSpec()

	// +---------+----------------------------------------------------
	// | Methods |
	// +---------+

	/**
	 * Adds the specified string to the {@code ArrayList} classes
	 * 
	 * @param clas a {@code String}
	 */
	public void addClass(String clas) {
		classes.add(clas);
		// spec.b++;
	} // addClass(String)

	@Override
	public String toString() {
		String str = "";
		str += tagName + id + classes;
		return str;
	} // toString()
} // class SimpleSelector

/**
 * {@code DescendantSelector} is
 */
class DescendantSelector extends Selector {
	// +--------+-----------------------------
	// | Fields |
	// +--------+
	ArrayList<SimpleSelector> chain;

	// +-------------+----------------------------------------------------
	// | Constructor |
	// +-------------+
	public DescendantSelector() {
		super();
		chain = new ArrayList<SimpleSelector>();
	} // DescendantSelector()

	// +---------+----------------------------------------------------
	// | Getters |
	// +---------+
	public Specificity getSpec() {
		this.spec = new Specificity(0, 0, 0);

		for (SimpleSelector sel : chain) {
			Specificity part = sel.getSpec();
			this.spec.add(part);
		}

		return this.spec;
	} // getSpec()

} // class DescendentSelector

/**
 * {@code Declaration} represents the CSS property-value assignments in a 
 * CSS stylesheet.
 */
class Declaration {
	// +--------+-----------------------------
	// | Fields |
	// +--------+
	String name;
	Value value;

	// +-------------+----------------------------------------------------
	// | Constructor |
	// +-------------+
	
	/**
	 * Constructs a {@code Declaration} object with an empty property name and null value.
	 */
	public Declaration() {
		name = "";
		value = null;
	} // Declaration()

	/**
	 * Constructs a {@code Declaration} object out of the specified string and value.
	 * 
	 * @param name a {@code String}
	 * @param value a {@code Value}
	 */
	public Declaration(String name, Value value) {
		this.name = name;
		this.value = value;
	} // Declaration(String, Value)

	// +---------+----------------------------------------------------
	// | Setters |
	// +---------+
	
	/**
	 * Sets the name field to the specified string.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	} // setName(String)

	/**
	 * Sets the value field to the specified value.
	 * 
	 * @param value
	 */
	public void setValue(Value value) {
		this.value = value;
	} // setValue(Value)

	// +---------+----------------------------------------------------
	// | Methods |
	// +---------+
	@Override
	public String toString() {
		String str = "";
		str += name + "=" + value;
		return str;
	} // toString()
} // class Declaration

/**
 * This class represents the value assigned to a property
 */
class Value {
	// +--------+-----------------------------
	// | Fields |
	// +--------+
	boolean exists;

	// +-------------+----------------------------------------------------
	// | Constructor |
	// +-------------+
	/**
	 * Constructs a {@code Value} object.
	 */
	public Value() {
		this.exists = true;
	} // Value()

	/**
	 * Constructs a {@code Value} object from the specified boolean.
	 */
	public Value(boolean exists) {
		this.exists = exists;
	} // Value(boolean)

	// +---------+----------------------------------------------------
	// | Getters |
	// +---------+
	
	/**
	 * Gets the length field of a {@code Length} object
	 * 
	 * @return 0 if {@code this} is not a {@code Length} object; otherwise
	 * {@code this.len}.
	 */
	public double getLen() {
		if (this instanceof Length) {
			Length val = (Length) this;
			return val.len;
		}
		return 0;
	} // getLen()

	// +---------+----------------------------------------------------
	// | Methods |
	// +---------+

	/**
	 * Checks if the name of this Keyword is "auto"
	 * 
	 * @return
	 */
	public boolean isAuto() {
		try {
			return ((Keyword) this).name.compareTo("auto") == 0;
		} catch (Exception e) {
			// System.err.println(e);
			return false;
		}
	} // isAuto()
} // class Value

/**
 * {@code Keyword} represents a CSS value that is just a string.
 */
class Keyword extends Value {
	// +--------+-----------------------------
	// | Fields |
	// +--------+
	String name;

	// +-------------+----------------------------------------------------
	// | Constructor |
	// +-------------+
	
	/**
	 * Constructs a {@code Keyword} object out of the specified string.
	 * @param name a {@code String}
	 * @throws NullPointerException
	 */
	public Keyword(String name) throws NullPointerException {
		super();
		if (name != null) {
			this.name = name;
		} else {
			throw new NullPointerException();
		}
	} // Keyword(String)

	// +---------+----------------------------------------------------
	// | Methods |
	// +---------+
	@Override
	public String toString() {
		return name;
	} // toString()
} // class Keyword

/**
 * {@code Keyword} represents a CSS length value that is a number followed by
 * a unit unless the number is 0.
 */
class Length extends Value {
	// +--------+-----------------------------
	// | Fields |
	// +--------+
	double len;
	String unit;

	// +-------------+----------------------------------------------------
	// | Constructor |
	// +-------------+
	
	/**
	 * Constructs a {@code Length} object out of the specified double and string.
	 * @param len a double
	 * @param unit a {@code String}
	 */
	public Length(double len, String unit) {
		super();
		this.len = len;
		this.unit = unit;
	} // Length(double, String)
} // class Length

/**
 * {@code Color} represents a color value in RGBA.
 */
class Color extends Value {
	// +--------+-----------------------------
	// | Fields |
	// +--------+
	int r, g, b, a;

	// +-------------+----------------------------------------------------
	// | Constructor |
	// +-------------+
	/**
	 * Constructs a {@code Color} object with (R, G, B, A) as (0, 0, 0, 0)
	 * with the specified boolean.
	 * 
	 * @param exists a boolean
	 */
	public Color(boolean exists) {
		super(exists);
	} // Color(boolean)

	/**
	 * Constructs a {@code Color} object with the specified r,g,b,a values.
	 * 
	 * @param r an int
	 * @param g an int
	 * @param b an int
	 * @param a an int
	 */
	public Color(int r, int g, int b, int a) {
		super();
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	} // Color(int, int, int, int)

	// +---------+----------------------------------------------------
	// | Methods |
	// +---------+
	@Override
	public String toString() {
		return "(" + r + "," + g + "," + b + ")";
	} // toString()
} // class Color

/**
 * {@code Specificity} is a way to compare the specificity between 
 * to CSS selectors.
 */
class Specificity {
	// +--------+-----------------------------
	// | Fields |
	// +--------+
	int a; // # of id
	int b; // # of classes
	int c; // # of tagname

	// +-------------+----------------------------------------------------
	// | Constructor |
	// +-------------+
	/**
	 * Constructs a {@code Specificity} object to keep track of the number of
	 * id, classes, and tagname references in a selector.
	 * @param a an int that denotes the number of ids
	 * @param b an int that denotes the number of classes
	 * @param c an int that denotes the number of tagnames
	 */
	public Specificity(int a, int b, int c) {
		this.a = a;
		this.b = b;
		this.c = c;
	} // Specificity(int, int, int)

	// +---------+----------------------------------------------------
	// | Methods |
	// +---------+
	/**
	 * 
	 * @param obj a {@code Specificity} object
	 */
	public void add(Specificity obj) {
		this.a += obj.a;
		this.b += obj.b;
		this.c += obj.c;
	} // add(Specificity)

} // class Specificity
