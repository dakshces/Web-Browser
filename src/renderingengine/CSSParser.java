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

	public void consumeWhiteSpace()
	{
		consumeWhile(c -> Character.isWhitespace(c));
	}


	public Rule parseRule()
	{
		return new Rule(parseSelectors(), parseDeclarations());
	}


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
	}

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
	}

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
	}

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
	}

	public String parseIdentifier()
	{
		return consumeWhile(c -> validIdentifierChar(c));
	}


	public boolean validIdentifierChar(char c)
	{
		return (c == '-' || c == '_' || Character.isLetterOrDigit(c));
	}


	public Value parseValue()
	{
		char next = input.charAt(currPos);
		if(Character.isDigit(next))
			return parseLength();
		else if(next =='#')
			return parseColor();

		return new Keyword(parseIdentifier());
	}

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

