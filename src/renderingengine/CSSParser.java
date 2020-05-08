package renderingengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CSSParser
{
	// +--------+------------------------------------------------------
	// | Fields |
	// +--------+
	int currPos;
	String input;
	Stylesheet sheet;

	// +-------------+-------------------------------------------
	// | Constructor |
	// +-------------+
	
	
	public CSSParser(String input)
	{
		this.currPos = 0;
		this.input = input;
		this.sheet = parse();
		int x = 1;
	}

	// +---------+---------------------------------------------
	// | Methods |
	// +---------+
	
	public Stylesheet parse()
	{
		ArrayList<Rule> rules = new ArrayList<Rule>();
		consumeWhiteSpace();
		while(!eof())
		{
			Rule r = parseRule();
			System.out.println(r);
			rules.add(r);
			consumeWhiteSpace();
		}

		return new Stylesheet(rules);
	}

	/**
	 * Checks if our parser has reached the end of the html string.
	 * @return
	 */
	public boolean eof()
	{
		return (currPos >= input.length());
	} // eof()

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

	/**
	 * Changes currpos to the index of the next non-whitespace character.
	 * currpos is unchanged if input.charAt(currpos) is not a whitespace character.
	 */
	public void consumeWhiteSpace()
	{
		consumeWhile(c -> Character.isWhitespace(c));
	} // consumeWhiteSpace()


	/**
	 * Parses a CSS rule
	 * @return
	 */
	public Rule parseRule()
	{
		return new Rule(parseSelectors(), parseDeclarations());
	} // parseRule()

	/**
	 * Parses the CSS selectors
	 * @return
	 */
	public ArrayList<Selector> parseSelectors()
	{
		char next = input.charAt(currPos);
		ArrayList<Selector> selectors = new ArrayList<Selector>();
		do
		{
			selectors.add(parseSimpleSelector());
			consumeWhiteSpace();
			next = input.charAt(currPos);
			if(next == ',')
			{
				currPos++;
				consumeWhiteSpace();
			}

			else if(next != '{')
			{
				System.out.println("Unexpected input in selector list: " + next);
				System.exit(1);
			}
		}while(next != '{');

		Collections.sort(selectors); //Sort by specificity in descending order
		System.out.println("Selectors: " + selectors);
		return selectors;
	} // parseSelectors()

	/**
	 * Parses simple CSS selectors. One type of selectors
	 * that we neglect for compounded selectors
	 * @return
	 */
	public SimpleSelector parseSimpleSelector()
	{
		char next = input.charAt(currPos);
		SimpleSelector selector = new SimpleSelector();
		while(!eof())
		{
			if(next == '#')
			{
				currPos++;
				selector.setId(parseIdentifier());
			}

			else if(next == '.')
			{
				currPos++;
				selector.addClass(parseIdentifier());
			}

			else if(next == '*')
				currPos++;

			else if(validIdentifierChar(next))
			{
				selector.setTagName(parseIdentifier());
			}

			else
				break;
			
			next = input.charAt(currPos);
		}

		return selector;
	} // parseSimpleSelectors()

	/**
	 * Parses the entire set of declarations
	 * @return
	 */
	public ArrayList<Declaration> parseDeclarations()
	{
		ArrayList<Declaration> declarations = new ArrayList<Declaration>(); 
		consumeWhiteSpace();
		currPos++; //Skip {
		consumeWhiteSpace();
		while(input.charAt(currPos) != '}')
		{
			declarations.add(parseDeclaration());
			consumeWhiteSpace();
		}
		currPos++; //Skip }
		return declarations;
	} // parseDeclarations()

	/**
	 * Parses a single declaration which has the structure 
	 * identifier=value
	 * @return
	 */
	public Declaration parseDeclaration()
	{
		Declaration dec = new Declaration();
		dec.setName(parseIdentifier());
		consumeWhiteSpace();
		currPos++;// Skip :
		consumeWhiteSpace();
		dec.setValue(parseValue());
		consumeWhiteSpace();
		currPos++; //Skip ; (end of declaration)

		return dec;
	} // parseDeclaration()

	/**
	 * Parses the identifier component of the declaration
	 * @return
	 */
	public String parseIdentifier()
	{
		return consumeWhile(c -> validIdentifierChar(c));
	} // parseIdentifier()


	public boolean validIdentifierChar(char c)
	{
		return (c == '-' || c == '_' || Character.isLetterOrDigit(c));
	}

	/**
	 * Parses the value component of the declaration. Supports two types of
	 * declaration identifiers currently : color and padding
	 * @return
	 */
	public Value parseValue()
	{
		char next = input.charAt(currPos);
		if(Character.isDigit(next))
			return parseLength();
		else if(next =='#')
			return parseColor();

		return new Keyword(parseIdentifier());
	} // parseValue()

	public Length parseLength()
	{
		return new Length(parseFloat(), parseUnit());
	}

	public float parseFloat()
	{
		return Float.parseFloat(consumeWhile(c -> (Character.isDigit(c) || c == '.')));
	}

	public String parseUnit()
	{
		String unit = parseIdentifier();
		if(unit.compareTo("px") != 0)
		{
			System.out.println("Given length unit not recognized: " + unit);
			System.exit(1);
		}

		return unit;
	}
	
	public Color parseColor()
	{
		currPos++; //Skip #
		return new Color(parseHexPair(),parseHexPair(),parseHexPair(),255); 
	}
	
	public int parseHexPair()
	{
		int hex = Integer.parseInt(input.substring(currPos,currPos+2), 16);
		currPos += 2;
		return hex;
	}
	
	/**
	 * Removes the comments from an CSS string
	 * We will assume no tags in CSS
	 * Source : https://www.regextester.com/94246
	 * @param css
	 * @return
	 */
	public static String clean(String css) {

		Pattern pxPattern = Pattern.compile("\\/\\*[\\s\\S]*?\\*\\/|([^:]|^)\\/\\/.*");
		Matcher pxMatcher = pxPattern.matcher(css);

		while (pxMatcher.find()) {
			String htmlString = pxMatcher.group();
			css = css.replace(htmlString, "");
		}

		return css;
	} // clean(String)

} // Class CSSParser

/*
 * Notes:
 * If the identifier requires there to be a unit and no unit is
 * provided then the identifier is declaration is ignored.
 * For the example of padding, it requires the value to be numbers separated
 * by spaces with a max of four numbers and minimum of one number, each number
 * must have a unit unless the number is zero.
 * padding dictates a different effect on the element's box depending on the 
 * value recieved (i.e. if the value is one, two, three or four numbers.
 * 
 * The identifier has preconditions for the given value that needs to be met,
 * before it can be used. Maybe we can use Predicates to dictate the preconditions?
 * 
 * Then we need to consider that how we construct the StyleNode out of the element
 * and the Style. Rules contains two vectors : selectors and declarations. parseSelectors
 * seem to be implemented fine. parseDeclarations needs more stuff (nuance?). 
 * 
 * Sooooo.... we only add a declaration into ArrayList<Declaration> declarations
 * only when the preconditions for the specific name is met.
 */
