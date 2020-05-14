package renderingengine;

import java.awt.FontMetrics;
import java.util.ArrayList;

/**
 * <p>
 * {@code LayoutBox} objects represents a visual container with content
 * <p>
 * 
 * The first implementation of this was based on the tutorial by Matt Brubeck
 * but many layout procedures have been reimplemented and in particular
 * formatting of inline nodes for text and image content has been added
 * 
 * @see <a href=
 *      "https://limpet.net/mbrubeck/2014/09/08/toy-layout-engine-5-boxes.html">
 *      </a>
 */
public class LayoutBox {

	// +--------+----------------------------------
	// | Fields |
	// +--------+
	Dimensions dim;
	ArrayList<LayoutBox> children;
	int currInlinePosX;
	int currInlinePosY;

	// +--------------+---------------------------
	// | Constructors |
	// +--------------+

	/**
	 * Constructs a {@code LayoutBox} with default initial values
	 */
	public LayoutBox() {
		dim = new Dimensions();
		children = new ArrayList<LayoutBox>();
		currInlinePosX = (int) dim.content.width;
		currInlinePosY = 0;
	}

	/**
	 * Gets the most recent Inline Container ({@code InlineNode} or
	 * {@code AnonymousBlock}) available
	 * 
	 * @return a {@code Layoutbox}, which is the current object if it is an inline
	 *         container, otherwise return a child container (if it exists)
	 *         otherwise create a new AnonymousBlock and add it to children
	 */
	public LayoutBox getInlineContainer() {
		// Return current object if it's Inline/Anonymous
		if (this instanceof InlineNode || this instanceof AnonymousBlock)
			return this;

		// Return existing AnonymousBlock if just created otherwise new one
		if (children.size() == 0)
			children.add(new AnonymousBlock());

		if (!(children.get(children.size() - 1) instanceof AnonymousBlock))
			children.add(new AnonymousBlock());

		return children.get(children.size() - 1);
	}

	/**
	 * Layout the current {@code LayoutBox} within the {@code Dimensions} of the
	 * parent container
	 * 
	 * @param container      a {@code Dimensions} object representing the dimensions
	 *                       of the parent container
	 * @param currInlinePosX an {@code int} representing the current inline x
	 *                       position within the parent container
	 * @param currInlinePosY an {@code int} representing the current inline y
	 *                       position within the parent container
	 */
	public void layout(Dimensions container, int currInlinePosX, int currInlinePosY) {

		if (this instanceof BlockNode) {
			BlockNode curr = (BlockNode) this;
			curr.layoutBlock(container);
		}

		else if (this instanceof InlineNode) {
			this.currInlinePosX = currInlinePosX;
			this.currInlinePosY = currInlinePosY;
			InlineNode curr = (InlineNode) this;
			curr.layoutBlock(container, currInlinePosX, currInlinePosY);
		}

		else {
			this.currInlinePosX = currInlinePosX;
			this.currInlinePosY = currInlinePosY;
			AnonymousBlock curr = (AnonymousBlock) this;
			curr.layoutBlock(container, currInlinePosX, currInlinePosY);
		}

	}

	/**
	 * Layout the children of the current {@code LayoutBox}
	 */
	public void layoutBlockChildren() {

		ArrayList<LayoutBox> add = new ArrayList<LayoutBox>();
		for (LayoutBox child : children) {
			if (child instanceof InlineNode) {
				InlineNode c = (InlineNode) child;
				ArrayList<InlineNode> splits = c.layoutBlock(dim, currInlinePosX, currInlinePosY);
				if (splits != null) {
					add.addAll(splits); // splits represents the text that has been split into smaller string in order
										// to fit into the current container
				}

				dim.content.height += c.updateY - currInlinePosY;
				currInlinePosX = c.updateX;
				currInlinePosY = c.updateY;
			}

			else if (child instanceof AnonymousBlock) {
				AnonymousBlock c = (AnonymousBlock) child;
				c.layoutBlock(dim, currInlinePosX, currInlinePosY);
				dim.content.height += c.currInlinePosY - currInlinePosY;
				currInlinePosX = c.currInlinePosX;
				currInlinePosY = c.currInlinePosY;
			}

			else {
				child.layout(dim, currInlinePosX, currInlinePosY);
				dim.content.height += child.dim.marginBox().height;
				currInlinePosX = (int) dim.content.width;
				currInlinePosY += child.dim.marginBox().height;
			}
		}
		if (add.size() != 0)
			children.addAll(add);
	}
}

/**
 * <p>
 * {@code BlockNode} extends {@code LayoutBox} and represents a block level box
 * which fills up all available width in the parent container
 * <p>
 */
class BlockNode extends LayoutBox {

	// +--------+----------------------------------
	// | Fields |
	// +--------+
	StyledNode stynode;

	// +--------------+---------------------------
	// | Constructors |
	// +--------------+

	/**
	 * Constructs a {@code BlockNode} with default initial values
	 */
	public BlockNode() {
		super();
		stynode = new StyledNode();
	}

	/**
	 * Constructs a {@code BlockNode} with given {@code StyledNode}
	 * 
	 * @param stynode is a {@code StyledNode}
	 */
	public BlockNode(StyledNode stynode) {
		super();
		this.stynode = stynode;
	}

	/**
	 * Lays out the current block node within {@code Dimensions} of parent container
	 * 
	 * @param container is the {@code Dimensions} of parent
	 */
	public void layoutBlock(Dimensions container) {
		calculateWidth(container);
		calculateBlockPosition(container);
		this.currInlinePosX = (int) dim.content.width;
		this.currInlinePosY = 0;
		layoutBlockChildren();
		calculateBlockHeight();
	}

	/**
	 * Calculate width of current block by taking into account margin, border,
	 * padding specified in CSS
	 * 
	 * @param container is a {@code Dimensions} object representing the dimensions
	 *                  of the parent container
	 * 
	 *                  The algorithm for calculating width follows CSS
	 *                  specifications for normal flow
	 * @see <a href= "https://www.w3.org/TR/CSS2/visuren.html#positioning-scheme">
	 *      </a>
	 */
	public void calculateWidth(Dimensions container) {

		Value width = stynode.getValue("width");
		width = (width instanceof Length) ? width : (new Keyword("auto"));

		Length zero = new Length(0, "px");

		Value[] surrounding = { width, stynode.lookup("margin-left", "margin", zero),
				stynode.lookup("margin-right", "margin", zero),
				stynode.lookup("border-left-width", "border-width", zero),
				stynode.lookup("border-right-width", "border-width", zero),
				stynode.lookup("padding-left", "padding", zero), stynode.lookup("padding-right", "padding", zero) };
		double total = 0;
		for (Value val : surrounding) {
			total += val.getLen();
		}

		double underflow = container.content.width - total;
		AutoBooleanTriple curr = new AutoBooleanTriple(new Value[] { surrounding[0], surrounding[1], surrounding[2] });

		if (curr.matches(new boolean[] { false, false, false })) {
			surrounding[2] = new Length(surrounding[2].getLen() + underflow, "px"); // Right margin gets adjusted if
			// none is auto
		}

		else if (curr.matches(new boolean[] { false, false, true })) {
			surrounding[2] = new Length(underflow, "px");
		}

		else if (curr.matches(new boolean[] { false, true, false })) {
			surrounding[1] = new Length(underflow, "px");
		}

		else if (curr.matches(new boolean[] { false, true, true })) {
			surrounding[1] = new Length(underflow / 2, "px");
			surrounding[2] = new Length(underflow / 2, "px");
		}

		else // if width is auto
		{
			if (underflow >= 0)
				surrounding[0] = new Length(underflow, "px");
			else
				surrounding[2] = new Length(surrounding[2].getLen() + underflow, "px");
		}

		dim.content.width = surrounding[0].getLen();

		dim.margin.left = surrounding[1].getLen();
		dim.margin.right = surrounding[2].getLen();

		dim.border.left = surrounding[3].getLen();
		dim.border.right = surrounding[4].getLen();

		dim.padding.left = surrounding[5].getLen();
		dim.padding.right = surrounding[6].getLen();
	}

	/**
	 * Calculate (x,y) position of current block by taking into account margin,
	 * border, padding specified in CSS and position of parent container
	 * 
	 * @param container is a {@code Dimensions} object representing the dimensions
	 *                  of the parent container
	 * 
	 */
	public void calculateBlockPosition(Dimensions container) {
		Length zero = new Length(0, "px");
		dim.margin.top = stynode.lookup("margin-top", "margin", zero).getLen();
		dim.margin.bottom = stynode.lookup("margin-bottom", "margin", zero).getLen();

		dim.border.top = stynode.lookup("border-top-width", "border-width", zero).getLen();
		dim.border.bottom = stynode.lookup("border-bottom-width", "border-width", zero).getLen();

		dim.padding.top = stynode.lookup("padding-top", "padding", zero).getLen();
		dim.padding.bottom = stynode.lookup("padding-bottom", "padding", zero).getLen();

		dim.content.x = container.content.x + dim.margin.left + dim.padding.left + dim.border.left;
		dim.content.y = container.content.y + container.content.height + dim.margin.top + dim.padding.bottom
				+ dim.border.bottom;
	}

	/**
	 * Calculate block height by taking into account CSS height property and heights
	 * of children
	 */
	public void calculateBlockHeight() {
		Value height = stynode.getValue("height");
		if (dim.content.height < height.getLen())
			dim.content.height = height.getLen();
	}
}

/**
 * <p>
 * {@code AutoBooleanTriple} is a boolean triple representing if the components
 * of a {@code Value} triple are set to "auto"
 * <p>
 */
class AutoBooleanTriple {

	// +--------+----------------------------------
	// | Fields |
	// +--------+
	boolean[] bool;

	// +--------------+---------------------------
	// | Constructors |
	// +--------------+

	/**
	 * Constructs a {@code AutoBooleanTriple} using a triple of {@code Value}
	 * 
	 * @param val is a an {@code Array} of {@code Value}
	 * @precondition val.length == 3
	 */
	AutoBooleanTriple(Value[] val) {
		bool = new boolean[3];
		for (int i = 0; i < 3; i++)
			bool[i] = val[i].isAuto();
	}

	/**
	 * Checks if passed boolean triple canonically matches current object's field
	 * bool
	 * 
	 * @param val is a an {@code Array} of boolean
	 * @precondition bool2.length == 3
	 */
	boolean matches(boolean[] bool2) {
		for (int i = 0; i < 3; i++) {
			if (bool[i] != bool2[i])
				return false;
		}
		return true;
	}
}

/**
 * <p>
 * {@code InlineNode} extends {@code LayoutBox} and represents a inline box
 * which wraps content (text and images)
 * <p>
 */
class InlineNode extends LayoutBox {
	// +--------+----------------------------------
	// | Fields |
	// +--------+
	StyledNode stynode;
	int updateX;
	int updateY;

	// +--------------+---------------------------
	// | Constructors |
	// +--------------+

	/**
	 * Constructs a {@code InlineNode} with default initial values
	 */
	public InlineNode() {
		super();
		stynode = new StyledNode();
		updateX = 0;
		updateY = 0;
	}

	/**
	 * Constructs a {@code InlineNode} with specified {@code StyledNode}
	 */
	public InlineNode(StyledNode stynode) {
		super();
		this.stynode = stynode;
	}

	/**
	 * Lays out the current block node within {@code Dimensions} of parent container
	 * beginning at position (currInlinePosX,currInlinePosY)
	 * 
	 * @param container      is the {@code Dimensions} of parent
	 * @param currInlinePosX is an int representing current x inline position within
	 *                       parent
	 * @param currInlinePosY is an int representing current y inline position within
	 *                       parent
	 */
	public ArrayList<InlineNode> layoutBlock(Dimensions container, int currInlinePosX, int currInlinePosY) {

		if (!stynode.cont.txt.equals("")) { // if it contains text content
			String txt = stynode.cont.txt;
			java.awt.Canvas c = new java.awt.Canvas();
			FontMetrics metrics = c.getFontMetrics(stynode.cont.font);
			dim.content.height = metrics.getHeight();
			if (currInlinePosX == container.content.width) {
				currInlinePosX = 0;
				currInlinePosY += dim.content.height;
			}
			dim.content.x = container.content.x + currInlinePosX;
			dim.content.y = container.content.y + currInlinePosY;

			int spaceWidth = metrics.charWidth(' ');
			if (txt.contains("BREAKLIEN")) { // breakline for <br> tag
				int index = txt.indexOf("BREAKLIEN");
				int w = metrics.stringWidth(txt.substring(0, index));
				int spaces = (int) (container.content.width - currInlinePosX - w) / spaceWidth;
				txt = txt.substring(0, index) + makeSpaces(spaces) + "<DELETE>" + txt.substring(index + 9);
				stynode.cont.txt = txt;
			}

			int width = metrics.stringWidth(txt);
			double overflow = currInlinePosX + width - container.content.width;
			if (overflow > 0) { // if text overflows width of container, split text into next line
				ArrayList<InlineNode> splits = new ArrayList<InlineNode>();
				InlineNode prev = this;
				while (overflow > 0) { // keep splitting till all text fits within container
					int len = txt.length();
					int cutoff = (int) (len * (container.content.width - currInlinePosX) / width);
					prev.stynode.cont.txt = txt.substring(0, cutoff);
					InlineNode split = new InlineNode();
					split.stynode = prev.stynode.copy();
					split.stynode.cont.txt = txt.substring(cutoff).replaceAll("\\s*<DELETE>", "");
					split.dim.content.x = container.content.x + 0;
					split.dim.content.y = container.content.y + currInlinePosY + prev.dim.content.height;
					split.dim.content.height = prev.dim.content.height;
					prev.dim.content.width = metrics.stringWidth(prev.stynode.cont.txt);
					currInlinePosX = 0; // starting on next line
					txt = split.stynode.cont.txt;

					if (txt.contains("BREAKLIEN")) { // break lines if <br> exists
						int index = txt.indexOf("BREAKLIEN");
						int w = metrics.stringWidth(txt.substring(0, index));
						int spaces = (int) (container.content.width - currInlinePosX - w) / spaceWidth;
						txt = txt.substring(0, index) + makeSpaces(spaces) + "<DELETE>" + txt.substring(index + 9);
						split.stynode.cont.txt = txt;
					}
					split.dim.content.width = metrics.stringWidth(split.stynode.cont.txt);
					updateX = (int) split.dim.content.width; // update x
					updateY = (int) (currInlinePosY + prev.dim.content.height); // update y
					currInlinePosY = updateY;
					width = (int) split.dim.content.width;
					overflow = currInlinePosX + width - container.content.width;
					splits.add(split);
					prev = split;
				}
				return splits;
			}

			else { // if text doesn't overflow just set current position and update inline position
					// (x,y)
				dim.content.width = width;
				updateX = currInlinePosX + (int) dim.content.width;
				updateY = currInlinePosY;
			}
		}

		else if (stynode.tagName.compareTo("img") == 0) { // if image content is specified
			String width = stynode.attributes.get("width");
			String height = stynode.attributes.get("height");// .replaceAll("\"", "");
			if (width != null && height != null) {
				width = width.replaceAll("\"", "");
				height = height.replaceAll("\"", "");
				double widthD = Double.parseDouble(width);
				double heightD = Double.parseDouble(height);

				double overflow = currInlinePosX + widthD - container.content.width;
				if (overflow > 0) {// if image doesn't fit within current line, advance down by the image height
					dim.content.x = container.content.x + 0;
					dim.content.y = container.content.y + currInlinePosY + heightD;
				}

				else { // if image fits
					dim.content.x = container.content.x + currInlinePosX;
					dim.content.y = container.content.y + currInlinePosY;
				}

				updateX = (int) widthD;
				updateY = (int) (currInlinePosY + heightD);
				this.currInlinePosX = updateX;
				this.currInlinePosY = updateY;
				dim.content.width = (int) widthD;
				dim.content.height = (int) heightD;
			}
		}

		else { // else layout children and update inline position (x,y)
			this.currInlinePosX = currInlinePosX;
			this.currInlinePosY = currInlinePosY;
			dim.content.x = container.content.x;
			dim.content.y = container.content.y;
			dim.content.width = container.content.width;
			layoutBlockChildren();
			updateX = this.currInlinePosX;
			updateY = this.currInlinePosY;
		}
		return null;
	}

	/**
	 * Make specified number of spaces
	 * 
	 * @param spaces is an int specifying number of spaces to be made
	 * @return a {@code String} with specified number of spaces
	 */
	public String makeSpaces(int spaces) {
		String str = "";
		for (int i = 0; i < spaces; i++) {
			str += ' ';
		}
		return str;
	}

}

/**
 * <p>
 * {@code AnonymousBlock} extends {@code LayoutBox} and represents a box which
 * has children but has no attributes of its own
 * <p>
 */
class AnonymousBlock extends LayoutBox {
	// +--------+----------------------------------
	// | Fields |
	// +--------+
	int updateX;
	int updateY;

	// +--------------+---------------------------
	// | Constructors |
	// +--------------+

	/**
	 * Constructs a {@code AnonymousBlock} with default initial values
	 */
	public AnonymousBlock() {
		super();
		updateX = 0;
		updateY = 0;
	}

	/**
	 * Lays out the current block node within {@code Dimensions} of parent container
	 * beginning at position (currInlinePosX,currInlinePosY)
	 * 
	 * @param container      is the {@code Dimensions} of parent
	 * @param currInlinePosX is an int representing current x inline position within
	 *                       parent
	 * @param currInlinePosY is an int representing current y inline position within
	 *                       parent
	 */
	public void layoutBlock(Dimensions container, int currInlinePosX, int currInlinePosY) {
		this.currInlinePosX = currInlinePosX;
		this.currInlinePosY = currInlinePosY;
		dim.content.x = container.content.x;
		dim.content.y = container.content.y;
		dim.content.width = container.content.width;
		layoutBlockChildren();
	}
}

/**
 * <p>
 * {@code Dimensions} represents the dimensions of a box (including padding,
 * border, and margin)
 * <p>
 */
class Dimensions {
	// +--------+----------------------------------
	// | Fields |
	// +--------+
	Rect content;
	EdgeSizes padding;
	EdgeSizes border;
	EdgeSizes margin;

	// +--------------+---------------------------
	// | Constructors |
	// +--------------+

	/**
	 * Constructs a {@code Dimensions} with default initial values
	 */
	public Dimensions() {
		content = new Rect(0, 0, 0, 0);
		padding = new EdgeSizes();
		border = new EdgeSizes();
		margin = new EdgeSizes();
	}

	/**
	 * Constructs a {@code Dimensions} content equal to specified {@code Rect}
	 * 
	 * @param rect a {@code Rect}
	 */
	public Dimensions(Rect rect) {
		content = rect;
		padding = new EdgeSizes();
		border = new EdgeSizes();
		margin = new EdgeSizes();
	}

	/**
	 * Constructs a {@code Rect} representing the box composed of the current box
	 * and the padding
	 * 
	 * @return {@code Rect}
	 */
	public Rect paddingBox() {
		return content.expandedBy(padding);
	}

	/**
	 * Constructs a {@code Rect} representing the box composed of the current box,
	 * the padding, and the border
	 * 
	 * @return {@code Rect}
	 */
	public Rect borderBox() {
		return paddingBox().expandedBy(border);
	}

	/**
	 * Constructs a {@code Rect} representing the box composed of the current box,
	 * the padding, the border, and the margin
	 * 
	 * @return {@code Rect}
	 */
	public Rect marginBox() {
		return borderBox().expandedBy(margin);
	}

}

/**
 * <p>
 * {@code Rect} represents the true dimensions of a box (no padding, border, and
 * margin)
 * <p>
 */
class Rect {
	// +--------+----------------------------------
	// | Fields |
	// +--------+
	double x;
	double y;
	double height;
	double width;

	// +--------------+---------------------------
	// | Constructors |
	// +--------------+

	/**
	 * Constructs a {@code Rect} with specified initial values
	 * 
	 * @param double
	 * @param double
	 * @param double
	 * @param width
	 */
	public Rect(double x, double y, double height, double width) {
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
	}

	/**
	 * Constructs a {@code Rect} by adding specified {@code EdgeSizes} to current
	 * object
	 * 
	 * @param {@code EdgeSizes}
	 * @return {@code Rect}
	 */
	public Rect expandedBy(EdgeSizes e) {
		return new Rect(x - e.left, y - e.top, height + e.top + e.bottom, width + e.left + e.right);

	}
}

/**
 * <p>
 * {@code EdgeSizes} represents the edge sizes of a box
 * <p>
 */
class EdgeSizes {
	// +--------+----------------------------------
	// | Fields |
	// +--------+
	double left;
	double right;
	double bottom;
	double top;

	// +--------------+---------------------------
	// | Constructors |
	// +--------------+

	/**
	 * Constructs a {@code EdgeSizes} with default initial values
	 */
	public EdgeSizes() {
		this.bottom = 0;
		this.top = 0;
		this.left = 0;
		this.right = 0;
	}

}