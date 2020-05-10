package renderingengine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
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
									if (type.compareTo("text/css")==0 & rel.compareTo("stylesheet")==0){
										// we expect href to be of form "directories/nameOfFile.css"
										StylesheetLinks.add(URL + href);
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
	
	public static String CompleteCSSString(String css, ArrayList<String> StylesheetLinks) throws Exception{
		String completeCSS = css;
		for (String url : StylesheetLinks) {
			completeCSS = fileFromURLToString(new URL(url)) + "\n" + completeCSS;
		}
		return completeCSS;
	}

	public static void main(String[] args) throws Exception {

		// = fileToString(new File("./html.txt"));
		String html;
		String css = fileToString(new File("./defaultblock.txt"));
		
		String siteURL = "https://paulhus.math.grinnell.edu/";
	
		String htmlFromURL = fileFromURLToString(new URL(siteURL));

		html = htmlFromURL;

		DOM dom = new HTMLParser(html).dom;
		dom.print();
		ArrayList<String> StylesheetLinks = getStylesheetLinks(dom, siteURL);
		css = CompleteCSSString(css, StylesheetLinks);
		Stylesheet sheet = new CSSParser(css).sheet;
		// System.out.println(sheet);

		StyleTree sty = new StyleTree(dom, sheet);

		Dimensions bounds = new Dimensions(new Rect(0, 0, 600, 960));
		LayoutTree layout = new LayoutTree(sty, bounds);

		new Rendering(layout, bounds.content);

	}
}
