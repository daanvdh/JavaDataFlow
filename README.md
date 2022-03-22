# JavaDataFlow
Creating Data Flow Graphs from java input classes

With JavaDataFlow you can create data flow graphs. 
This is a directed graph where a node represents data (e.g. fields, inputParameters, etc.) and the edges represent a node influencing the state of another node.
Nodes are always defined within a specific method, constructor, codeBlock. 
We refer to such a code block as owner. 
Every Node has an owner. 
Every owner can also have an owner, for instance a method is owned by a class.
To make it possible to only parse a part of an application we model calls to a method and the dataflow inside the method itself separately. 
At any moment the flow through a method can be linked to a call to that method to follow the flow of data though multiple methods or classes. 


## Example

Let's say we have a class as given as below: 
	
	public class JavaDataFlowExampleInputClass {
	  int a;
	  public int getA() {
	    return a;
	  }
	  public void setA(int inputA) {
	    this.a = inputA;
	  }
	}

Then we can execute the following commands to create a data flow graph from it. 
First define the path to your project and setup the path where JavaDataFlow will look for class files. 
If you have multiple projects where JavaDataFlow needs to look for class files, you can enter multiple project paths there. 
Then create the data flow graph using the absolute input path to the java class. 
A DataFlowGraph represents a single class. 

	String projectPath = "projectPath";
	String input = "/relativePath/Example.java";
	StaticJavaDataFlow.getConfig().setProjectPaths(projectPath);
	DataFlowGraph dfg = JavaDataFlow.create(projectPath + input);

Now if we want to gather all input nodes to this class that can influence the output of the method "getA", we can do that as given below. 
First get the given method. 
Now we need to walk back until we reach a node that is an input parameter of a method, for this we can use the method DataFlowNode::isInputParameter. 
For this example we don't want to go outside this class so we add dfg::owns as scope to the method walkBackUntil. 
The scope determines when to stop walking over the nodes, this can become important multiple data flow graphs are connected to each other. 
However, this is currently not supported yet. 

	DataFlowMethod getA = dfg.getMethods().stream().filter(m -> m.getName().equals("getA")).findFirst().get();
	List<DataFlowNode> inputNodes = getA.getReturnNode().get().walkBackUntil(DataFlowNode::isInputParameter, dfg::owns);
	System.out.println(inputNodes.get(0).getName());

The above code will output the name "inputA". 

## Setup 
Add the dependency below to the pom of your project. 

	<dependency>
	  <groupId>com.github.daanvdh.javadataflow</groupId>
	  <artifactId>JavaDataFlow</artifactId>
	  <version>0.0.5</version>
	</dependency>

## Definitions

- Any **object or primitive** is modelled as a DataFlowNode. 
  A DataFlowNode can have 0 or more input or output DataFlowEdge's.
  Such an edge represents a data flow from one node to another. 
- The **owner** of a DataFlowNode represents structure in which the node is defined. 
  For the method setA, the parameter inputA has a ParameterList as input. 
  The ParameterList has the DataFlowMethod for "setA" as owner. 
  The owner of the method is the DataFlowGraph representing Example.java
- Each **usage of an object** is modelled as a separate DataFlowNode. 
  A DataFlowNode representing the usage of an earlier defined object will contain an edge as input from either the last usage before that or the definition of the object. 
  This way the order in which a node was used is maintained. 
- A NodeCall represents a **call to another code block**, for instance a method call. 
  If a NodeCall was called directly on an object, that object will be modelled as a DataFlowNode and the NodeCall will have a reference to that DataFlowNode via NodeCall::getInstance. 
  That same DataFlowNode will then have a reference to the NodeCall via DataFlowNode::getNodeCall. 
  A DataFlowNode can only have a single NodeCall, since every object usage is modelled as a separate node. 
  If a NodeCall was not directly called on an object, it can be found via DataFlowMethod::getNodeCalls on the method in which the NodeCall was done. 
  

## Features
- JavaDataFlow uses [JavaParser](https://github.com/javaparser/javaparser/) for parsing the input classes. 
  Each DataFlowNode has a representedNode which is the JavaParser Node that it represents. 
  If you have a given JavaParser Node you can get the JavaDataFlowNode via DataFlowGraph::getNode. 
- Collect all methods that where called on a given object by executing DataFlowNode::collectNodeCalls.
  A scope can be added to this method to only find calls within a certain method or graph, you can for example use DataFlowMethod::owns.  

## Roadmap
- Include Constructors in the JavaDataFlow graph. 
- Model if statements. 
- Model primitive functions like: + - * / < >. 
- Model for and while loops. 
- Connect multiple JavaDataFlow graphs to each other so that we can walk from class to class. 

## License

JavaDataFlow is available under the terms of the Apache License. http://www.apache.org/licenses/LICENSE-2.0