package renderingengine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

//Not sure if we need all of ,net
import java.net.*;

public class Main {

	/**
	 * Creates a string out of a local file
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
	 * Retrieves the links to stylesheets found in the children of the head
	 * tag.
	 * @param d
	 * @param URL
	 * @return
	 */
	public static ArrayList<String> getStylesheetLinks(DOM d, String URL) {
		
		ArrayList<String> StylesheetLinks = new ArrayList<String>();
		Node root = d.root;
		for (Node child : root.children) {
			if (child instanceof Element) {
				Element possibleHead = (Element) child;
				if (possibleHead.tagName.compareTo("head") == 0) {
					// loop through head's children for links
					for (Node child2 : child.children) {
						if (child2 instanceof Element) {
							Element possibleLink = (Element) child2;
							if (possibleLink.tagName.compareTo("link") == 0) {
								// we found a link element
								// check if we have a stylesheet
								String rel = possibleLink.attributes.get("rel");
								String href = possibleLink.attributes.get("href");
								String type = possibleLink.attributes.get("type");
								// remove " characters
								rel = rel.replaceAll("[\"]", "");
								href = href.replaceAll("[\"]", "");
								type = type.replaceAll("[\"]", "");
								if (rel != null & href != null & type != null) {
									if (type.compareTo("text/css")==0 && rel.compareTo("stylesheet")==0){
										// We expect proper links and link relations
										StylesheetLinks.add(urlExtension(URL, href));
									}
								}
							}
						}
					}
					break;
				}
			}
		}
		return StylesheetLinks;
	} // getStylesheetLinks(DOM, String)
	
	/**
	 * Tests if there is a substring in input, beginning at currPos,
	 * that is equivalent to the given String str 
	 * @param str
	 * @return
	 */
	public static boolean beginsWith(String str, String prefix) {
		return (str.substring(0, prefix.length())).compareTo(prefix) == 0;
	} // beginsWith(String)
	
	/**
	 * NOT SAFE. If not enough directories in url, may fail
	 * @param url
	 * @param ext
	 * @return
	 */
	public static String urlExtension(String url, String ext) {
		// turn url into a string for a directory
		url = url.replaceFirst("/[^/]*$", "/");
		if(beginsWith(ext, "./")) {	
			ext = ext.replaceFirst("^./", "");
			return urlExtension(url, ext);
		}else if ((beginsWith(ext, "../"))) {
			// go back one directory
			url = url.replaceFirst("/[^/]+/$", "/");
			ext = ext.replace("../", "");
			return urlExtension(url, ext);
		}else {
			return url + ext;
		}
	} // urlExtension(String, String)


	/**
	 * Grabs the html file from link. if url string does not end
	 * in .html, then we default to index.html
	 * @param url
	 * @return
	 * @throws IOException
	 * @source https://stackoverflow.com/questions/31462/how-to-fetch-html-in-java 
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
	
	/**
	 * Adds the stylesheet css files found in the url links in StylesheetLinks
	 * onto the String css
	 * @param css
	 * @param StylesheetLinks
	 * @return
	 * @throws Exception
	 */
	public static String CompleteCSSString(String css, ArrayList<String> StylesheetLinks) throws Exception{
		String completeCSS = css;
		for (String url : StylesheetLinks) {
			completeCSS =  completeCSS + "\n" + fileFromURLToString(new URL(url));
		}
		return completeCSS;
	} // CompleteCSSString(String, ArrayList<String>)

	/**
	 * Main function
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		// +----------------------+----------------------------------------------
		// | INITIALIZE VARIABLES |
		// +----------------------+
		
		String html, css, siteURLString;
		DOM dom;
		ArrayList<String> StylesheetLinks;
		Stylesheet sheet;
		StyleTree sty;
		Dimensions bounds;
		LayoutTree layout;		
		
		/* Local Testing
		 * To test on a local file named html.txt.
		 * If we want to test on a different local file change the String input
		 * If we want to test on a local file, uncomment the code line immediately
		 * below and comment out or remove the current assignment to String html
		 */
		// html = = fileToString(new File("./html.txt"));
		
		/* Main Run
		 * By default, we run on the url https://paulhus.math.grinnell.edu/
		 * If run on a different url, change the assignment of
		 * siteURLString to a different url. 
		 * Other urls that you can try to see the render:
		 * 
		 * "https://mileti.math.grinnell.edu/"
		 * "https://paulhus.math.grinnell.edu/teaching.html"
		 */
		siteURLString = "https://paulhus.math.grinnell.edu/";
		html = fileFromURLToString(new URL(siteURLString));

		// Create the DOM
		dom = new HTMLParser(html).dom;
		// Create the complete CSS String
		StylesheetLinks = getStylesheetLinks(dom, siteURLString);
		css = fileToString(new File("./defaultblock.txt"));
		css = CompleteCSSString(css, StylesheetLinks);
		// Create the Style Sheet
		sheet = new CSSParser(css).sheet;
		// Create the Style Tree
		sty = new StyleTree(dom, sheet);
		// Set default total dimensions
		bounds = new Dimensions(new Rect(0, 0, 600, 960));
		// Create Layout Tree
		layout = new LayoutTree(sty, bounds);
		// Render!!!
		new Rendering(layout, bounds.content);

	} // main(String[])
} // Class Main
