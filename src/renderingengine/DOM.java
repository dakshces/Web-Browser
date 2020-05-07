
public class DOM 
{
	Node root;
	
	public DOM()
	{
		root = null;
	}
	
	public DOM(Node root)
	{
		this.root = root;
	}
	
	public void print()
	{
		print(root); 
	}
	
	public void print(Node n)
	{
		System.out.print(n);
	}

}


