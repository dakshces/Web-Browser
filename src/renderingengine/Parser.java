package renderingengine;

import java.util.function.Predicate;

/**
 * This abstract class provides a partial implementation of a
 * String parser. 
 * 
 * <p> {@code Parser} parses a specified string by reading each character
 * while keeping track of its position in the string.
 * 
 */
public abstract class Parser {
	
	// +--------+----------------------------------
	// | Fields |
	// +--------+
	int currPos;
	String input;
	
	// +--------------+---------------------------
	// | Constructors |
	// +--------------+
	
	/**
	 * Constructs a parser out of the specified string.
	 * 
	 * @param str a String
	 */
	public Parser(String str) {
		this.currPos = 0;
		this.input = str;
		clean();
	} // Parse(String)
	
	// +---------+--------------------------------------------------
	// | Methods |
	// +---------+
	
	abstract void clean();
	
	/**
	 * Checks if our parser has reached the end of the html string.
	 * 
	 * @return true if and only if we have not reached the end of the string.
	 */
	public boolean eof() {
		return (currPos >= input.length());
	} // eof()
	
	/**
	 * Creates a substring starting with the char at currpos of the String input to
	 * up until the char fails to satisfy charTest
	 * 
	 * @param charTest, a Predicate
	 * @return the first substring starting from currpos consisting only of
	 *         characters satisfying the Predicate charTest
	 */
	public String consumeWhile(Predicate<Character> charTest) {
		String res = "";

		while (!eof() && charTest.test((Character) (input.charAt(currPos)))) {
			char c = input.charAt(currPos);
			if (Character.isWhitespace(c)) {
				res += ' ';
			} else {
				res += c;
			}
			currPos++;
		}
		return res;
	} // consumeWhile()
	
	/**
	 * Changes currpos to the index of the next non-whitespace character. currpos is
	 * unchanged if input.charAt(currpos) is not a whitespace character.
	 */
	public void consumeWhiteSpace() {
		consumeWhile(c -> Character.isWhitespace(c));
	} // consumeWhiteSpace()
	
	/**
	 * Counts the number of characters starting from currPos
	 * that satisfies the predicate charTest
	 * 
	 * @param charTest a {@code Predicate<Character>}
	 * @return the number of characters that satisfy charTest starting from and including
	 * the character at currPos
	 */
	public int advanceWhile(Predicate<Character> charTest)
	{
		int i = 0;
		while(currPos + i < input.length() && charTest.test((Character) (input.charAt(currPos+i))))
		{
			i++;
		}
		if(currPos + i >= input.length())
			return 0;
		return i;
	} // advanceWhile(Predicate<Character>)
	
	/**
	 * Tests if there is a substring in input, beginning at currPos,
	 * that is equivalent to the specified string. 
	 * 
	 * @param str a String
	 * @return boolean
	 */
	public boolean beginsWith(String str) {
		return (input.substring(currPos, currPos + str.length())).compareTo(str) == 0;
	} // beginsWith(String)
	
} // class Parser
