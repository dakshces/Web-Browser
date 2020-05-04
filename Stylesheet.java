import java.util.ArrayList;

public class Stylesheet 
{
	ArrayList<Rule> rules;
	
	public Stylesheet()
	{
		this.rules = new ArrayList<Rule>();
	}
	
	public Stylesheet(ArrayList<Rule> rules)
	{
		this.rules = rules;
	}
}

class Rule
{
	ArrayList<Selector> selectors;
	ArrayList<Declaration> declarations;
	
	public Rule(ArrayList<Selector> selectors, ArrayList<Declaration> declarations)
	{
		this.selectors = selectors;
		this.declarations = declarations;
	}
}

class Selector implements Comparable<Selector>
{
	Specificity spec; 
	
	public Selector()
	{
		spec = new Specificity(0,0,0);
	}
	
	public int compareTo(Selector obj)
	{
		if(obj.spec.a != this.spec.a)
			return obj.spec.a - this.spec.a;
		
		else if(obj.spec.b != this.spec.b)
			return obj.spec.b - this.spec.b;
	
		return obj.spec.c - this.spec.c;
	}

}

class SimpleSelector extends Selector
{
	String tagName;
	String id; 
	ArrayList<String> classes;
	
	public SimpleSelector()
	{
		super();
		tagName = "";
		id = "";
		classes = new ArrayList<String>();
	}
	
	
	public void setTagName(String name)
	{
		tagName = name;
		spec.c++;
	}
	
	public void setId(String id)
	{
		this.id = id;
		spec.a++;
	}
	
	public void addClass(String clas)
	{
		classes.add(clas);
		spec.b++;
	}
}

class Declaration
{
	String name;
	Value value;
	
	public Declaration()
	{
		name = "";
		value = null;
	}
	

	public void setName(String name)
	{
		this.name = name;
	}
	
	public void setValue(Value value)
	{
		this.value = value;
	}
}

class Value
{
	boolean exists;
	
	public Value()
	{
		this.exists = true;
	}
	
	public Value(boolean exists)
	{
		this.exists = exists;
	}
	
	public boolean isAuto()
	{
		if(this instanceof Keyword)
		{
			Keyword val = (Keyword) this;
			return (val.name.compareTo("auto") == 0);
		}
		return false;
	}
	
	public double getLen()
	{
		if(this instanceof Length)
		{
			Length val = (Length) this;
			return val.len;
		}
		return 0;
	}
}

class Keyword extends Value
{
	String name;
	
	public Keyword(String name)
	{
		super();
		this.name = name;
	}
}

class Length extends Value
{
	double len;
	String unit;
	
	public Length(double len, String unit)
	{
		super();
		this.len = len;
		this.unit = unit;
	}
}

class Color extends Value
{
	int r,g,b,a;
	
	public Color(boolean exists)
	{
		super(exists);
	}
	
	public Color(int r, int g, int b, int a)
	{
		super();
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
}

class Specificity
{
	int a; //# of id
	int b; //# of classes
	int c; //# of tagname
	
	public Specificity(int a, int b, int c)
	{
		this.a = a;
		this.b = b;
		this.c = c;
	}

}