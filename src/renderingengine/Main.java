package renderingengine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

//Not sure if we need all of ,net
import java.net.*;

public class Main {
	
	public static String fileToString(File f) throws FileNotFoundException {
		Scanner sc = new Scanner(f);
		String html = "";
		while (sc.hasNextLine()) {
			html += sc.nextLine();
		}
		sc.close();
		return html;
	}
	
	/*
	 * URL: https://stackoverflow.com/questions/31462/how-to-fetch-html-in-java How
	 * to fetch html in java using absolute url path
	 */
	public static String fileFromURLToString(URL url) throws IOException {
		String content = null;
		URLConnection connection = null;

		connection = url.openConnection();
		Scanner scanner = new Scanner(connection.getInputStream());
		scanner.useDelimiter("\\Z");
		content = scanner.next();
		scanner.close();
		
		return content;
	}
	
	public static void main(String[] args) throws Exception {
		
		String html = fileToString(new File("./html.txt"));		
		String css = fileToString(new File("./defaultblock.txt"));

		String htmlFromURL = fileFromURLToString(new URL("https://paulhus.math.grinnell.edu/"));
		String cssFromURL = fileFromURLToString(new URL("https://paulhus.math.grinnell.edu/jen2.css"));
		// point html to htmlFromURL, css to ""
		html = htmlFromURL;
		css =  cssFromURL +"\n" + css;

		// cleans html of comments
		//html = HTMLParser.clean(html); added call in HTMLPARSER constructor
		
		DOM dom = new HTMLParser(html).dom;
		dom.print();
		
		System.out.println(css);
		Stylesheet sheet = new CSSParser(css).sheet;
		System.out.println(sheet);
		
		StyleTree sty = new StyleTree(dom, sheet);
		
		Dimensions bounds = new Dimensions(new Rect(0, 0, 600, 960));
		LayoutTree layout = new LayoutTree(sty, bounds);
		
		new Rendering(layout, bounds.content);
		
	}
}
