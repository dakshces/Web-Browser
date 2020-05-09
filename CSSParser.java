package renderingengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Predicate;

public class CSSParser
{
	int currPos;
	String input;
	Stylesheet sheet;

	public CSSParser(String input)
	{
		this.currPos = 0;
		this.input = input;
		this.sheet = parse();
		int x = 1;
	}

	public Stylesheet parse()
	{
		ArrayList<Rule> rules = new ArrayList<Rule>();
		consumeWhiteSpace();
		while(!eof())
		{
			Rule r = parseRule();
			if(r != null)
			{
				System.out.println(r);
				rules.add(r);
			}
			
			consumeWhiteSpace();
		}

		return new Stylesheet(rules);
	}

	public boolean eof()
	{
		return (currPos >= input.length());
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

	public int advanceWhile(Predicate<Character> charTest)
	{
		String res = "";
		int i = 0;
		while(!eof() && charTest.test((Character) (input.charAt(currPos+i))))
		{
			res += input.charAt(currPos+i);
			i++;
		}
		return i;
	}


	public void consumeWhiteSpace()
	{
		consumeWhile(c -> Character.isWhitespace(c));
	}


	public Rule parseRule()
	{
		ArrayList<Selector> selectors = parseSelectors();
		ArrayList<Declaration> decl =  parseDeclarations();
		if(selectors == null)
			return null;
		
		return new Rule(selectors, decl);
	}


	public ArrayList<Selector> parseSelectors()
	{
		char next = input.charAt(currPos);
		ArrayList<Selector> selectors = new ArrayList<Selector>();
		do
		{
			consumeWhiteSpace();
			selectors.add(parseSimpleSelector());
			next = input.charAt(currPos);
			if(next == ',')
			{
				currPos++;
				consumeWhiteSpace();
			}

			else if(next == ' ')
			{
				if(input.charAt(currPos + advanceWhile(c -> Character.isWhitespace(c))) != '{')
				{
					SimpleSelector last = (SimpleSelector )selectors.remove(selectors.size()-1);
					selectors.add(parseDescendantSelector(last));
				}
				else
					consumeWhiteSpace();
			}

			else if(next != '{')
			{
//				System.out.println("Unexpected input in selector list: " + next);
//				System.exit(1);
				consumeWhile(c -> c != '}');
				currPos++;
				return null;
			}
		}while(next != '{');

		Collections.sort(selectors); //Sort by specificity in descending order
		System.out.println("Selectors: " + selectors);
		return selectors;
	}

	public DescendantSelector parseDescendantSelector(SimpleSelector last)
	{
		DescendantSelector sel = new DescendantSelector();

		sel.chain.add(last);
		consumeWhiteSpace();
		sel.chain.add(parseSimpleSelector());
		consumeWhiteSpace();
		char next = input.charAt(currPos);
		if(next != '{')
		{
			System.out.println("Unexpected input in selector list: " + next);
			System.exit(1);
		}
		return sel;
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
		double len = parseFloat();
		String unit = parseUnit();
		if(unit.equalsIgnoreCase("pt"))
		{
			len = 4*len/3; 
			unit = "px";
		}
		return new Length(len, unit);
	}

	public double parseFloat()
	{
		return Double.parseDouble(consumeWhile(c -> (Character.isDigit(c) || c == '.')));
	}

	public String parseUnit()
	{
		String unit = parseIdentifier();
		if(unit.compareTo("") != 0 && unit.compareTo("px") != 0 && unit.compareTo("pt") != 0)
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

}