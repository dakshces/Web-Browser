# Web-Browser

This project is a Java Implementaion of the Browser Rendering Engine built on top of the one described in a tutorial written by Matt Brubeck. It can render using either locally stored HTML and CSS files or a provided url link to a webpage. The final output is a jpg rendering of the provided webpage as a file named WebPage.jpg. Because our HTML parser and CSS parser are not very error tolerant the given webpage should have correctly written HTML and CSS code. We also support a limited range of CSS properties and HTML tags. Currently we only support static pages because we lack a JavaScript engine to support dynamic pages. A page that renders well is [Professor Paulhus' homepage](https://paulhus.math.grinnell.edu/).

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. 

### Prerequisites

What things you need to run the project
```
Java : at least openjdk version "11.0.7" 
Eclipse IDE
```

### Installing

* [Github Link](https://github.com/dakshces/Web-Browser)
Go to the mentioned link and download the finished Eclipse Java project from the master branch.

Eclipse Instructions
--------------------

* **Prerequisites:**
    * Install [Eclipse](http://www.eclipse.org/downloads/) and optionally the [GitHub plugin](http://eclipse.github.com/).

* Import a project in Eclipse by`
    * Import existing project into workspace
    * Navigate to the location of Web-Browser
    * Click on Finish
        
* Run
    * Navigate to src/renderingengine/Main.java
    * Right-click on Main.java
    * Run As > Java Application
      * The default settings render the homepage of [Professor Jennifer Paulhus](https://paulhus.math.grinnell.edu/) with her permission.
      * Instructions on rendering other websites as well as locally stored websites can be found in the documentation within the Main.java file in the src folder.

## Authors

* **Daksh Aggarwal**
* **Davin Lin**  

## Acknowledgments

Much thanks to Professor Fahmida Hamid for inspiring the project idea, Professor Jennifer Paulhus for allowing the use of her homepage in our default settings, and Matt Brubeck for providing the base design of this project.

* [Professor Fahmida Hamid](https://www.cs.grinnell.edu/~hamidfah/)
* [Professor Jennifer Paulhus](https://paulhus.math.grinnell.edu/)
* [Matt Brubeck](https://limpet.net/mbrubeck/) 
* [ReadMe Template](https://gist.github.com/PurpleBooth/109311bb0361f32d87a2)
* [ReadMe Template](https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/storage/xml-api/cmdline-sample/README.md)


