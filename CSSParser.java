package renderingengine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An extended version of the CSS parser component of the HTML rendering engine
 * tutorial written by Matt Brubeck.
 * 
 * <p>
 * A CSS specific implementation of the abstract class {@code Parser}.
 * 
 * <p>
 * A {@code CSSParser} reads its input character by character to construct a
 * Stylesheet representation of its input.
 * 
 * @see <a href=
 *      "https://limpet.net/mbrubeck/2014/08/13/toy-layout-engine-3-css.html">Matt
 *      Brubeck</a>
 */
public class CSSParser extends Parser {
	// +--------+----------------------------------
	// | Fields |
	// +--------+
	Stylesheet sheet;

	static String stringColorToHex;

	static {
		try {
			stringColorToHex = fileToString("./rgb.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// +--------------+---------------------------
	// | Constructors |
	// +--------------+

	/**
	 * Constructors a {@code CSSParser} out of the specified string. 
	 * Characters in the string are read successively to construct
	 * individual {@code Rule} objects that will added into {@code this.sheet}.
	 * 
	 * @param input a {@code String}
	 */
	public CSSParser(String input) {
		super(input);
		this.sheet = parse();
	} // CSSParser(String)

	// +---------+--------------------------------------------------
	// | Methods |
	// +---------+

	/**
	 * Creates a string representation of each line in the file of the specified
	 * path string. 
	 * 
	 * @param path a {@code String} that denotes the path of the desired file
	 * @return a string representation of the file.
	 * @throws FileNotFoundException when the file specified in {@code path} is not
	 *                               found.
	 */
	public static String fileToString(String path) throws FileNotFoundException {
		File f = new File(path);
		Scanner sc = new Scanner(f);
		String css = "";
		while (sc.hasNextLine()) {
			css += sc.nextLine();
		}
		sc.close();
		return css;
	} // fileToString(String)

	@Override
	public void clean() {
		input = input.replaceAll("/\\*.+\\*/", "");
	} // clean()

	/**
	 * Parses input to create the Stylesheet object.
	 * 
	 * @return
	 */
	public Stylesheet parse() {
		ArrayList<Rule> rules = new ArrayList<Rule>();
		consumeWhiteSpace();
		while (!eof()) {
			Rule r = parseRule();
			if (r != null) {
				System.out.println(r);
				rules.add(r);
			}
			consumeWhiteSpace();
		}
		return new Stylesheet(rules);
	} // parse()

	/**
	 * Parses a rule to add into the Stylesheet.
	 * 
	 * @return
	 */
	public Rule parseRule() {
		ArrayList<Selector> selectors = parseSelectors();
		ArrayList<Declaration> decl = parseDeclarations();
		if (selectors == null)
			return null;

		return new Rule(selectors, decl);
	} // parseRule()

	/**
	 * Parses the selectors to add into the Rule.
	 * 
	 * @return an {@code ArrayList} parameterized to {@code Selector}
	 * which contains all the selectors of the specific CSS rule.
	 */
	public ArrayList<Selector> parseSelectors() {
		char next = input.charAt(currPos);
		ArrayList<Selector> selectors = new ArrayList<Selector>();
		do {
			consumeWhiteSpace();
			selectors.add(parseSimpleSelector());
			next = input.charAt(currPos);
			if (next == ',') {
				currPos++;
				consumeWhiteSpace();
			}

			else if (next == ' ') {
				if (input.charAt(currPos + advanceWhile(c -> Character.isWhitespace(c))) != '{') {
					SimpleSelector last = (SimpleSelector) selectors.remove(selectors.size() - 1);
					selectors.add(parseDescendantSelector(last));

				}
				consumeWhiteSpace();
				next = input.charAt(currPos);
			}

			else if (next != '{') {
				// System.out.println("Unexpected input in selector list: " + next);
				// System.exit(1);
				consumeWhile(c -> c != '}');
				currPos++;
				return null;
			}
		} while (next != '{');

		Collections.sort(selectors); // Sort by specificity in descending order
		System.out.println("Selectors: " + selectors);
		return selectors;
	} // parseSelectors()


	public DescendantSelector parseDescendantSelector(SimpleSelector last) {
		DescendantSelector sel = new DescendantSelector();

		sel.chain.add(last);
		consumeWhiteSpace();
		sel.chain.add(parseSimpleSelector());
		consumeWhiteSpace();
		char next = input.charAt(currPos);
		if (next != '{') {
			System.out.println("Unexpected input in selector list: " + next);
			System.exit(1);
		}
		return sel;
	}

	/**
	 * Retrieves a simple selector from the current position 
	 * of the parser in the input string.
	 * 
	 * @return a {@code SimpleSelector}
	 */
	public SimpleSelector parseSimpleSelector() {
		char next = input.charAt(currPos);
		SimpleSelector selector = new SimpleSelector();
		while (!eof()) {
			if (next == '#') {
				currPos++;
				selector.setId(parseIdentifier());
			}

			else if (next == '.') {
				currPos++;
				selector.addClass(parseIdentifier());
			}

			else if (next == '*')
				currPos++;

			else if (validIdentifierChar(next)) {
				selector.setTagName(parseIdentifier());
			}

			else
				break;

			next = input.charAt(currPos);
		}

		return selector;
	} // parseSimpleSelector()

	/**
	 * Retrieves a set the declarations in input.
	 * 
	 * @return a {@code ArrayList} parameterized to {@code Declaration} which contains 
	 * the declarations corresponding to the rule at point of method call.
	 */
	public ArrayList<Declaration> parseDeclarations() {
		ArrayList<Declaration> declarations = new ArrayList<Declaration>();
		consumeWhiteSpace();
		currPos++; // Skip {
		consumeWhiteSpace();
		while (input.charAt(currPos) != '}') {
			declarations.addAll(parseDeclaration());
			consumeWhiteSpace();

		}
		currPos++; // Skip }
		return declarations;
	} // parseDeclarations()

	/**
	 * Retrieves a declaration in input.
	 * 
	 * @return a {@code Declaration} 
	 */
	public ArrayList<Declaration> parseDeclaration() {
		Declaration dec = new Declaration();
		dec.setName(parseIdentifier());
		consumeWhiteSpace();
		currPos++;// Skip :
		consumeWhiteSpace();
		if (dec.name.equals("padding") || dec.name.contains("border") || dec.name.equals("margin"))
			return parseShorthandDec(dec.name);

		dec.setValue(parseValue(dec.name));
		dec = modifyIfRelativeLength(dec);
		consumeWhiteSpace();
		if (input.charAt(currPos) == ';')
			currPos++; // Skip ; (end of declaration)

		ArrayList<Declaration> lst = new ArrayList<Declaration>();
		lst.add(dec);
		return lst;
	} // parseDeclaration()

	
	
	
	public Declaration modifyIfRelativeLength(Declaration dec) {
		if (dec.value instanceof Length) {
			Length val = (Length) dec.value;
			if (val.unit.equals("%"))
				dec.setName("REL-" + dec.name);
		}
		return dec;
	}
	
	
	

	/**
	 * Parses the identifier of a selector
	 * 
	 * @return the name of the selector
	 */
	public String parseIdentifier() {
		return consumeWhile(c -> validIdentifierChar(c));
	} // parseIdentifier()

	/**
	 * Checks if the specified character can exist in an identifier
	 * 
	 * @param c a character
	 * @return {@code true} if and only if the specified character can be
	 * in a selector's name.
	 */
	public boolean validIdentifierChar(char c) {
		return (c == '-' || c == '_' || Character.isLetterOrDigit(c) || c == '%');
	} // validIdentifierChar(char)

	/**
	 * Parses the value of a declaration.
	 * 
	 * @param name a {@code String}
	 * @return a {@code Length} object if the next character is a digit,
	 * a {@code Color} object if the next character is '#' or the specified string contains
	 * "color"; otherwise creates a {@code Keyword} object to represent the value.
	 */
	public Value parseValue(String name) {
		char next = input.charAt(currPos);
		if (Character.isDigit(next))
			return parseLength();
		else if (next == '#')
			return parseColor(0);

		else if (name.contains("color"))
			return parseColor(1);

		return new Keyword(parseIdentifier());
	} // parseValue(String)
	
	

	public ArrayList<Declaration> parseShorthandDec(String name) {
		ArrayList<Declaration> lst = new ArrayList<Declaration>();
		if (name.equals("padding")) {
			String[] values = consumeWhile(c -> (c != ';')).split(" ");
			if (values.length >= 1) {
				Length len1 = parseLength(values[0]);
				Declaration top = new Declaration("padding-top", len1);
				Declaration right = new Declaration("padding-right", len1);
				Declaration bottom = new Declaration("padding-bottom", len1);
				Declaration left = new Declaration("padding-left", len1);

				if (values.length >= 2) {
					Length len2 = parseLength(values[1]);
					right.setValue(len2);
					left.setValue(len2);

					if (values.length >= 3) {
						Length len3 = parseLength(values[2]);
						bottom.setValue(len3);

						if (values.length >= 4) {
							Length len4 = parseLength(values[3]);
							left.setValue(len4);
						}
					}
				}

				Collections.addAll(lst, new Declaration[] { top, right, bottom, left });
			}
		} else if (name.contains("border")) {
			String[] values = consumeWhile(c -> (c != ';')).split(" ");
			if (values.length >= 1) {
				boolean indicator = false;
				if (Character.isDigit(values[0].charAt(0))) {
					Length width = parseLength(values[0]);
					lst.add(new Declaration(name + "-width", width));
					indicator = true;
				}

				if (values.length >= 2 && !indicator) {
					String value = values[values.length - 1];
					Color color = parseColor(value, value.charAt(0) == '#' ? 0 : 1);
					lst.add(new Declaration(name + "-color", color));
				}
			}
		}

		else if (name.equals("margin")) {
			String[] values = consumeWhile(c -> (c != ';')).split(" ");
			if (values.length >= 1) {
				Length len1 = parseLength(values[0]);
				Declaration top = new Declaration("margin-top", len1);
				Declaration right = new Declaration("margin-right", len1);
				Declaration bottom = new Declaration("margin-bottom", len1);
				Declaration left = new Declaration("margin-left", len1);

				if (values.length >= 2) {
					Length len2 = parseLength(values[1]);
					right.setValue(len2);
					left.setValue(len2);

					if (values.length >= 3) {
						Length len3 = parseLength(values[2]);
						bottom.setValue(len3);

						if (values.length >= 4) {
							Length len4 = parseLength(values[3]);
							left.setValue(len4);
						}
					}
				}

				Collections.addAll(lst, new Declaration[] { top, right, bottom, left });
			}
		}
		currPos++; // skip ;
		return lst;
	}

	
	
	/**
	 * Constructs a {@code Length} object out of the specified string
	 * while moving the parser along the input string.
	 * 
	 * @param str a {@code String}
	 * @return a {@code Length} object
	 */
	public Length parseLength(String str) {
		String dup = input;
		int dupPos = currPos;
		input = str;
		currPos = 0;

		double len = parseFloat();
		if (eof()) {
			input = dup;
			currPos = dupPos;
			return new Length(len, "");
		}

		String unit = parseUnit();
		if (unit.equalsIgnoreCase("pt")) {
			len = 4 * len / 3;
			unit = "px";
		}

		input = dup;
		currPos = dupPos;
		return new Length(len, unit);
	} // parseLength(String)

	/**
	 * Constructs a {@code Length} object starting from the parser's current position
	 * in the input string.
	 * 
	 * @return a {@code Length} object
	 */
	public Length parseLength() {
		double len = parseFloat();
		if (eof())
			return new Length(len, "");

		String unit = parseUnit();
		if (unit.equalsIgnoreCase("pt")) {
			len = 4 * len / 3;
			unit = "px";
		}
//
//		if(unit.equals("%"))
//			len = 18;
		return new Length(len, unit);
	} // parseLength()

	/**
	 * Parses a double from input starting from the current position of
	 * the parser in the string.
	 * 
	 * @return a double
	 */
	public double parseFloat() {
		return Double.parseDouble(consumeWhile(c -> (Character.isDigit(c) || c == '.')));
	} // parseFloat()

	/**
	 * Parses a string from input starting from that represents the unit corresponding to a number in the
	 * input string. NOTSAFE: Our parser currently only supports three units: px, pt, %
	 * 
	 * @return a string
	 */
	public String parseUnit() {
		String unit = parseIdentifier();
		// if(unit.compareTo("") != 0 && unit.compareTo("px") != 0 &&
		// unit.compareTo("pt") != 0)
		// {
		// System.out.println("Given length unit not recognized: " + unit);
		// System.exit(1);
		// }

		return unit;
	} // parseUnit()

	/**
	 * Constructs a {@code Color} object out of the specified string. 
	 * The construction can be done differently depending on the form of 
	 * the specified string by passing an integer. If the specified
	 * integer is zero, then the color value is of form #RRGGBB; otherwise, the color
	 * value is of form int int int.
	 * 
	 * @param str a {@code String}
	 * @param option an {@code int}
	 * @return a {@code Color} object
	 */
	public Color parseColor(String str, int option) {
		String dup = input;
		int dupPos = currPos;
		input = str;
		currPos = 0;

		if (option == 0) // read by code
		{
			currPos++; // Skip #
			int p1 = parseHexPair();
			int p2 = parseHexPair();
			int p3 = parseHexPair();
			input = dup;
			currPos = dupPos;
			return new Color(p1, p2, p3, 255);
		}

		String stringColor = parseIdentifier();
		Pattern pattern = Pattern.compile("[0-9]+\\s+" + stringColor);
		Matcher matcher = pattern.matcher(stringColorToHex);
		String[] codes = stringColorToHex.substring(matcher.start(), matcher.end()).split("//s+");

		input = dup;
		currPos = dupPos;
		return new Color(Integer.parseInt(codes[0]), Integer.parseInt(codes[1]), Integer.parseInt(codes[2]), 255);

	} // parseColor(String, int)

	/**
	 * Constructs a {@code Color} object starting from the parser's current position
	 * in the input string. The construction can be done differently depending on the form of 
	 * the specified string by passing an integer. If the specified
	 * integer is zero, then the color value is of form #RRGGBB; otherwise, the color
	 * value is of form int int int.
	 * 
	 * @param option
	 * @return
	 */
	public Color parseColor(int option) {
		if (option == 0) // read by code
		{
			currPos++; // Skip #
			return new Color(parseHexPair(), parseHexPair(), parseHexPair(), 255);
		}

		String stringColor = parseIdentifier();
		Pattern pattern = Pattern.compile("[0-9]+\\s+" + stringColor);
		Matcher matcher = pattern.matcher(stringColorToHex);
		String[] codes = stringColorToHex.substring(matcher.start(), matcher.end()).split("//s+");
		return new Color(Integer.parseInt(codes[0]), Integer.parseInt(codes[1]), Integer.parseInt(codes[2]), 255);

	} // parseColor(int)

	/**
	 * Converts the substring of the next two characters from hex to decimal.
	 * 
	 * @return an int
	 */
	public int parseHexPair() {
		int hex = Integer.parseInt(input.substring(currPos, currPos + 2), 16);
		currPos += 2;
		return hex;
	} // parseHexPair()

}