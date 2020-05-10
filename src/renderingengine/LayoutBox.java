package renderingengine;

import java.util.ArrayList;

public class LayoutBox
{
	Dimensions dim;
	ArrayList<LayoutBox> children;

	public LayoutBox()
	{
		dim = new Dimensions();
		children = new ArrayList<LayoutBox>();
	}

	public LayoutBox getInlineContainer()
	{
		//Return current object if it's Inline/Anonymous
		if(this instanceof InlineNode || this instanceof AnonymousBlock)
			return this;

		//Return existing AnonymousBlock if just created otherwise new one
		if(children.size() == 0)
			children.add(new AnonymousBlock());

		if(!(children.get(children.size()-1) instanceof AnonymousBlock))
			children.add(new AnonymousBlock());

		return children.get(children.size()-1);
	}

	public void layout(Dimensions container)
	{
		if(this instanceof BlockNode)
		{
			BlockNode curr = (BlockNode) this;
			curr.layoutBlock(container);
			//currInlineWidth = curr.dim.content.x;
		}

		else if(this instanceof InlineNode)
		{
			InlineNode curr = (InlineNode) this;
			curr.layoutBlock(container);
		}

		else
		{
			AnonymousBlock curr = (AnonymousBlock) this;
			curr.layoutBlock(container);
		}

	}
	public void layoutBlockChildren()
	{
		for(LayoutBox child: children)
		{
			child.layout(dim);
			dim.content.height += child.dim.marginBox().height;
			double height = dim.content.height;
			int x = 1;
		}
	}
}

class BlockNode extends LayoutBox
{
	StyledNode stynode;

	public BlockNode()
	{
		super();
		stynode = new StyledNode( );
	}

	public BlockNode(StyledNode stynode)
	{
		super();
		this.stynode = stynode;
	}

	public void layoutBlock(Dimensions container)
	{
		calculateWidth(container);
		calculateBlockPosition(container);
		layoutBlockChildren();
		calculateBlockHeight();
	}

	public void calculateWidth(Dimensions container)
	{

		Value width = stynode.getValue("width");
		width = (width instanceof Length)? width : (new Keyword("auto"));

		Length zero = new Length(0,"px");

		Value [] surrounding  = {width, stynode.lookup("margin-left","margin",zero),stynode.lookup("margin-right","margin",zero),
				stynode.lookup("border-left-width","border-width",zero),stynode.lookup("border-right-width","border-width",zero),
				stynode.lookup("padding-left","padding",zero),stynode.lookup("padding-right","padding",zero)};
		double total = 0;
		for(Value val: surrounding)
		{
			total += val.getLen();
		}

		//		if(width != zero && total > container.content.width)
		//		{
		//			for(int i = 1; i < 3; i++) //if margins are "auto" treat them as zero
		//			{
		//				if(surrounding[i].isAuto())
		//					surrounding[i] = zero;
		//			}
		//		}

		double underflow = container.content.width - total;
		AutoBooleanTriple curr = new AutoBooleanTriple(new Value[]{surrounding[0],surrounding[1],surrounding[2]});

		if(curr.matches(new boolean[] {false,false,false}))
		{
			surrounding[2] = new Length(surrounding[2].getLen() + underflow, "px"); //Right margin gets adjusted if none is auto
		}

		else if(curr.matches(new boolean[] {false,false,true}))
		{
			surrounding[2] = new Length(underflow, "px");
		}

		else if(curr.matches(new boolean[] {false,true,false}))
		{
			surrounding[1] = new Length(underflow, "px");
		}

		else if(curr.matches(new boolean[] {false,true,true}))
		{
			surrounding[1] = new Length(underflow/2, "px");
			surrounding[2] = new Length(underflow/2, "px");
		}

		else //if width is auto
		{
			if(underflow >= 0)
				surrounding[0] = new Length(underflow,"px");
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

	public void calculateBlockPosition(Dimensions container)
	{
		Length zero = new Length(0,"px");
		dim.margin.top = stynode.lookup("margin-top", "margin", zero).getLen();
		dim.margin.bottom = stynode.lookup("margin-bottom", "margin", zero).getLen();

		dim.border.top = stynode.lookup("border-top-width", "border-width", zero).getLen();		
		dim.border.bottom = stynode.lookup("border-bottom-width", "border-width", zero).getLen();

		dim.padding.top = stynode.lookup("padding-top", "padding", zero).getLen();
		dim.padding.bottom = stynode.lookup("padding-bottom", "padding", zero).getLen();

		dim.content.x = container.content.x + dim.margin.left + dim.padding.left + dim.border.left;
		dim.content.y = container.content.y + container.content.height + dim.margin.top + dim.padding.bottom + dim.border.bottom;
	}

	public void calculateBlockHeight()
	{
		Value height = stynode.getValue("height");
		if(dim.content.height < height.getLen())
			dim.content.height = height.getLen();
	}

}

class AutoBooleanTriple
{
	boolean [] bool;
	AutoBooleanTriple(Value[] val)
	{
		bool = new boolean[3];
		for(int i = 0; i < 3; i++)
			bool[i] = val[i].isAuto();
	}

	boolean matches(boolean [] bool2)
	{
		for(int i = 0; i < 3; i++)
		{
			if(bool[i] != bool2[i])
				return false;
		}
		return true;
	}
}

class InlineNode extends LayoutBox
{
	StyledNode stynode;

	public InlineNode()
	{
		super();
		stynode = new StyledNode();
	}

	public InlineNode(StyledNode stynode)
	{
		super();
		this.stynode = stynode;
	}

	public void layoutBlock(Dimensions container)
	{
		//dim.content.x = currInlineWidth;
		//dim.content.y = container.content.y;
		dim.content.x = container.content.x;
		dim.content.y = container.content.y;
		dim.content.width = container.content.width;
		layoutBlockChildren();
	}

}

class AnonymousBlock extends LayoutBox
{
	public void layoutBlock(Dimensions container)
	{
		//dim.content = container.content;
		dim.content.x = container.content.x;
		dim.content.y = container.content.y;
		dim.content.width = container.content.width;
		layoutBlockChildren();
	}
}


class Dimensions
{
	Rect content;
	EdgeSizes padding;
	EdgeSizes border;
	EdgeSizes margin;

	public Dimensions()
	{
		content = new Rect(0,0,0,0);
		padding = new EdgeSizes();
		border = new EdgeSizes();
		margin = new EdgeSizes();
	}

	public Dimensions(Rect rect)
	{
		content = rect;
		padding = new EdgeSizes();
		border = new EdgeSizes();
		margin = new EdgeSizes();
	}

	public Rect paddingBox()
	{
		return content.expandedBy(padding);
	}

	public Rect borderBox()
	{
		return paddingBox().expandedBy(border);
	}

	public Rect marginBox()
	{
		return borderBox().expandedBy(margin);
	}

}

class Rect
{
	double x;
	double y;
	double height;
	double width;

	public Rect(double x, double y, double height, double width)
	{
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
	}

	public Rect expandedBy(EdgeSizes e)
	{	//not sure about y-e.top
		return new Rect(x-e.left, y-e.top, height+e.top+e.bottom, width+e.left+e.right);

	}
}

class EdgeSizes
{
	double left;
	double right;
	double bottom;
	double top;
}