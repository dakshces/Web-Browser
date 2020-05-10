package renderingengine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CSSParser
{
	int currPos;
	String input;
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


	public static String fileToString(String path) throws FileNotFoundException {
		File f = new File(path);
		Scanner sc = new Scanner(f);
		String css = "";
		while (sc.hasNextLine()) {
			css += sc.nextLine();
		}
		sc.close();
		return css;
	}


	public CSSParser(String input)
	{
		this.currPos = 0;
		this.input = input;
		clean();
		this.sheet = parse();
		int x = 1;
	}

	public void clean()
	{
		input = input.replaceAll("/\\*.+\\*/", "");
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
		char next = input.charAt(currPos);
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
				consumeWhiteSpace();
				next = input.charAt(currPos);
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
			declarations.addAll(parseDeclaration());
			consumeWhiteSpace();
			char next = input.charAt(currPos);
			int x = 1;
		}
		currPos++; //Skip }
		return declarations;
	}

	public ArrayList<Declaration> parseDeclaration()
	{
		Declaration dec = new Declaration();
		dec.setName(parseIdentifier());
		consumeWhiteSpace();
		currPos++;// Skip :
		consumeWhiteSpace();
		if(dec.name.equals("padding") || dec.name.contains("border") || dec.name.equals("margin"))
			return parseShorthandDec(dec.name);

		dec.setValue(parseValue(dec.name));
		consumeWhiteSpace();
		if(input.charAt(currPos) == ';')
			currPos++; //Skip ; (end of declaration)

		ArrayList<Declaration> lst = new ArrayList<Declaration>();
		lst.add(dec);
		return lst;
	}

	public String parseIdentifier()
	{
		return consumeWhile(c -> validIdentifierChar(c));
	}


	public boolean validIdentifierChar(char c)
	{
		return (c == '-' || c == '_' || Character.isLetterOrDigit(c) || c == '%');
	}


	public Value parseValue(String name)
	{
		char next = input.charAt(currPos);
		if(Character.isDigit(next))
			return parseLength();
		else if(next =='#')
			return parseColor(0);

		else if(name.contains("color"))
			return parseColor(1);

		return new Keyword(parseIdentifier());
	}

	public ArrayList<Declaration> parseShorthandDec(String name)
	{
		ArrayList<Declaration> lst = new ArrayList<Declaration>();
		if(name.equals("padding"))
		{
			String[] values = consumeWhile(c -> (c != ';')).split(" ");
			if(values.length >= 1)
			{
				Length len1 = parseLength(values[0]);
				Declaration top = new Declaration("padding-top", len1);
				Declaration right = new Declaration("padding-right", len1);
				Declaration bottom = new Declaration("padding-bottom", len1);
				Declaration left = new Declaration("padding-left", len1);

				if(values.length >= 2)
				{
					Length len2 = parseLength(values[1]);
					right.setValue(len2);
					left.setValue(len2);

					if(values.length >= 3)
					{
						Length len3 = parseLength(values[2]);
						bottom.setValue(len3);

						if(values.length >= 4)
						{
							Length len4 = parseLength(values[3]);
							left.setValue(len4);
						}
					}
				}

				Collections.addAll(lst,new Declaration[] {top,right,bottom,left});
			}
		}
		else if(name.contains("border"))
		{
			String[] values = consumeWhile(c -> (c != ';')).split(" ");
			if(values.length >= 1)
			{
				boolean indicator = false;
				if(Character.isDigit(values[0].charAt(0)))
				{
					Length width = parseLength(values[0]);
					lst.add(new Declaration(name+"-width",width));
					indicator = true;
				}

				if(values.length >= 2 && !indicator)
				{
					String value = values[values.length-1];
					Color color = parseColor(value,value.charAt(0) == '#'? 0:1);
					lst.add(new Declaration(name+"-color",color));
				}
			}
		}

		else if(name.equals("margin"))
		{
			String[] values = consumeWhile(c -> (c != ';')).split(" ");
			if(values.length >= 1)
			{
				Length len1 = parseLength(values[0]);
				Declaration top = new Declaration("margin-top", len1);
				Declaration right = new Declaration("margin-right", len1);
				Declaration bottom = new Declaration("margin-bottom", len1);
				Declaration left = new Declaration("margin-left", len1);

				if(values.length >= 2)
				{
					Length len2 = parseLength(values[1]);
					right.setValue(len2);
					left.setValue(len2);

					if(values.length >= 3)
					{
						Length len3 = parseLength(values[2]);
						bottom.setValue(len3);

						if(values.length >= 4)
						{
							Length len4 = parseLength(values[3]);
							left.setValue(len4);
						}
					}
				}

				Collections.addAll(lst,new Declaration[] {top,right,bottom,left});
			}
		}
		currPos++; //skip ;
		return lst;
	}

	public Length parseLength(String str)
	{
		String dup = input;
		int dupPos = currPos;
		input = str;
		currPos = 0;

		double len = parseFloat();
		if(eof())
		{
			input = dup;
			currPos = dupPos;
			return new Length(len, "");
		}

		String unit = parseUnit();
		if(unit.equalsIgnoreCase("pt"))
		{
			len = 4*len/3; 
			unit = "px";
		}

		input = dup;
		currPos = dupPos;
		return new Length(len, unit);
	}

	public Length parseLength()
	{
		double len = parseFloat();
		if(eof())
			return new Length(len, "");

		String unit = parseUnit();
		if(unit.equalsIgnoreCase("pt"))
		{
			len = 4*len/3; 
			unit = "px";
		}

		if(unit.equals("%"))
			len = 18;
		return new Length(len, unit);
	}

	public double parseFloat()
	{
		return Double.parseDouble(consumeWhile(c -> (Character.isDigit(c) || c == '.')));
	}

	public String parseUnit()
	{
		String unit = parseIdentifier();
		//		if(unit.compareTo("") != 0 && unit.compareTo("px") != 0 && unit.compareTo("pt") != 0)
		//		{
		//			System.out.println("Given length unit not recognized: " + unit);
		//			System.exit(1);
		//		}

		return unit;
	}

	public Color parseColor(String str, int option)
	{
		String dup = input;
		int dupPos = currPos;
		input = str;
		currPos = 0;

		if(option == 0) //read by code
		{
			currPos++; //Skip #
			int p1 = parseHexPair();
			int p2 = parseHexPair();
			int p3 = parseHexPair();
			input = dup;
			currPos = dupPos;
			return new Color(p1,p2,p3,255); 
		}

		String stringColor = parseIdentifier();
		Pattern pattern = Pattern.compile("[0-9]+\\s+" + stringColor);
		Matcher matcher = pattern.matcher(stringColorToHex);
		String [] codes = stringColorToHex.substring(matcher.start(),matcher.end()).split("//s+");

		input = dup;
		currPos = dupPos;
		return new Color(Integer.parseInt(codes[0]),Integer.parseInt(codes[1]),Integer.parseInt(codes[2]),255);

	}

	public Color parseColor(int option)
	{
		if(option == 0) //read by code
		{
			currPos++; //Skip #
			return new Color(parseHexPair(),parseHexPair(),parseHexPair(),255); 
		}

		String stringColor = parseIdentifier();
		Pattern pattern = Pattern.compile("[0-9]+\\s+" + stringColor);
		Matcher matcher = pattern.matcher(stringColorToHex);
		String [] codes = stringColorToHex.substring(matcher.start(),matcher.end()).split("//s+");
		return new Color(Integer.parseInt(codes[0]),Integer.parseInt(codes[1]),Integer.parseInt(codes[2]),255);

	}

	public int parseHexPair()
	{
		int hex = Integer.parseInt(input.substring(currPos,currPos+2), 16);
		currPos += 2;
		return hex;
	}

}