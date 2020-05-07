import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main 
{
	public static void main(String [] args) throws FileNotFoundException
	{
		Scanner sc = new Scanner(new File("html.txt"));
		String html = "";
		while (sc.hasNextLine()) 
		{
			html += sc.nextLine();
		}
		
		DOM dom = new HTMLParser(html).dom;
		
		sc = new Scanner(new File("css.txt"));
		String css = "";
		while (sc.hasNextLine()) 
		{
			css += sc.nextLine();
		}
		
		Stylesheet sheet = new CSSParser(css).sheet;
		StyleTree sty = new StyleTree(dom,sheet);
		Dimensions bounds = new Dimensions(new Rect(0,0,600,800));
		LayoutTree layout = new LayoutTree(sty,bounds);
		new Rendering(layout,bounds.content);
	}
}
