# Books

This is an example for the course work. \
It is written using legacy technologies: servlets, JDBC, JSP. \
The project uses 'jakarta' namespace and can be run on Tomcat 10.x and above. \
Jetty servers was not tested.

## Details

### Before run

1. Create database using the script: [create-db.sql](sql/create-db.sql) \
   It can be done with 'MySqlWorkbench' or 'mysql' command line utility.
   - The database name is: `books_mvn`
   - You can change the database name in the script if it is necessary.
   > **Warning!** The script stops if a database with name `books_mvn` already exists.
2. Check database connection properties in [context.xml](src/main/webapp/META-INF/context.xml)
   and change the connection url to your database name if it changed.

### How to run

#### Using maven

You need the maven being installed on your computer.

In a terminal go to the project folder and type: `mvn clean verify cargo:run`

By default, it starts on address: `http://localhost:8080`. \
You can change the context path in `context` property of 'cargo-maven3-plugin' in pom.xml.

#### Using IntellijIdea Community

Open the project using maven nature. 
- You can use 'Using maven' approach to run it in IJ terminal.

Or 

- Install SmartTomcat plugin. 
- In 'run' dropdown toolbox choose 'Edit configuration'
- Add SmartTomcat configuration and fill out fields.
- Press 'run'

#### Using Eclipse

Import the project using 'Maven import' wizard.
- You can use 'Using maven' approach to run it in Eclipse terminal.

Or

Right mouse click on the project root and:
- Run as > Run on Server.
  > It's necessary to configure Tomcat 10.x usage in Eclipse for the first time.