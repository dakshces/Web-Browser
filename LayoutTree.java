package renderingengine;

public class LayoutTree
{
	LayoutBox root;
	
	public LayoutTree(StyleTree sty, Dimensions bounds)
	{
		root = buildTree(sty.root);
		double tmp = bounds.content.height;
		bounds.content.height = 0;
		root.layout(bounds,0,0);
		bounds.content.height = root.dim.content.height;
	}
	
	public LayoutBox buildTree(StyledNode sn)
	{
		String option = sn.display();
		LayoutBox lb = new LayoutBox();
		if(option.compareTo("block") == 0)
			lb = new BlockNode(sn);
		else if(option.compareTo("inline") == 0)
			lb = new InlineNode(sn);
		else
		{
			System.out.println("Root node has display option: " + option);
			System.exit(1);
		}
		
		for(Node child0: sn.children)
		{
			StyledNode child = (StyledNode) child0;
			option = child.display();
			if(option.compareTo("block") == 0)
				lb.children.add(buildTree(child));
			else if(option.compareTo("inline") == 0)
				//add tree rooted at child to either an existing AnonymousBlock/InlineBlock or a new AnonymousBlock
				lb.getInlineContainer().children.add(buildTree(child)); 
		}
		return lb;
	}
	
}


