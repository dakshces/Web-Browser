package renderingengine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

//Not sure if we need all of ,net
import java.net.*;

public class Main {
	
	/**
	 * Constructs a string aggregate of each line in a file
	 * @param f
	 * @return
	 * @throws FileNotFoundException
	 */
	public static String fileToString(File f) throws FileNotFoundException {
		Scanner sc = new Scanner(f);
		String html = "";
		while (sc.hasNextLine()) {
			html += sc.nextLine();
		}
		sc.close();
		return html;
	} // fileToString(File)
	
	/**
	 * Returns a String representation of a file from a URL
	 * Source : https://stackoverflow.com/questions/31462/how-to-fetch-html-in-java
	 * @return
	 * @throws IOException
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
	} // fileFromURLToString(URL)
	
	public static void main(String[] args) throws Exception {
		
		/* URLs that are good for testing
		 * "https://paulhus.math.grinnell.edu/"
		 * "https://paulhus.math.grinnell.edu/jen2.css"
		 *
		 */
		
		/*
		 * String representations of URLs
		 */
		String htmlUrl = "https://www.cs.grinnell.edu/~hamidfah/";
		String cssUrl = "https://www.cs.grinnell.edu/~hamidfah/style_files/my_style.css";
		String htmlFromURL = fileFromURLToString(new URL(htmlUrl));
		String cssFromURL = fileFromURLToString(new URL(cssUrl));

		/*
		 * String representations of local files
		 */
		String html = fileToString(new File("./fahmidaHtml.txt"));		
		String css = fileToString(new File("./hamidCSS.txt"));

		html = HTMLParser.clean(html);
		css = CSSParser.clean(css);
		System.out.println("CSS\n" + css);
		DOM dom = new HTMLParser(html).dom;
		dom.print();
		
		Stylesheet sheet = new CSSParser(css).sheet;
		System.out.println(sheet);
		
		StyleTree sty = new StyleTree(dom, sheet);
		
		Dimensions bounds = new Dimensions(new Rect(0, 0, 600, 800));
		LayoutTree layout = new LayoutTree(sty, bounds);
		
		new Rendering(layout, bounds.content);
		
	} // main(String[])
} // Class Main
