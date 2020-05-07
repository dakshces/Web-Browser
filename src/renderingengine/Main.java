package renderingengine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

//Not sure if we need all of ,net
import java.net.*;

public class Main {
	public static void main(String[] args) throws MalformedURLException, IOException {
		Scanner sc = new Scanner(new File("./html.txt"));
		String html = "";
		while (sc.hasNextLine()) {
			html += sc.nextLine();
		}

		/*
		 * URL: https://stackoverflow.com/questions/31462/how-to-fetch-html-in-java How
		 * to fetch html in java using absolute url path
		 */

		String content = null;
		URLConnection connection = null;

		connection = new URL("https://paulhus.math.grinnell.edu/").openConnection();
		Scanner scanner = new Scanner(connection.getInputStream());
		scanner.useDelimiter("\\Z");
		content = scanner.next();
		scanner.close();

		// point html to content
		html = content;
		System.out.println("String:\n" + html);
		
		html = HTMLParser.clean(html);
		System.out.println("------------------------------------");
		System.out.println(html);
		DOM dom = new HTMLParser(html).dom;
		dom.print();
		
		
		
		
		sc = new Scanner(new File("css.txt"));
		String css = "";
		while (sc.hasNextLine()) {
			css += sc.nextLine();
		}

		Stylesheet sheet = new CSSParser(css).sheet;
		StyleTree sty = new StyleTree(dom, sheet);
		Dimensions bounds = new Dimensions(new Rect(0, 0, 600, 800));
		LayoutTree layout = new LayoutTree(sty, bounds);
		new Rendering(layout, bounds.content);
	}
}
