/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class PizzaStore {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of PizzaStore
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public PizzaStore(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end PizzaStore

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            PizzaStore.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      PizzaStore esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the PizzaStore object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new PizzaStore (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Profile");
                System.out.println("2. Update Profile");
                System.out.println("3. View Menu");
                System.out.println("4. Place Order"); //make sure user specifies which store
                System.out.println("5. View Full Order ID History");
                System.out.println("6. View Past 5 Order IDs");
                System.out.println("7. View Order Information"); //user should specify orderID and then be able to see detailed information about the order
                System.out.println("8. View Stores"); 

                //**the following functionalities should only be able to be used by drivers & managers**
                System.out.println("9. Update Order Status");

                //**the following functionalities should ony be able to be used by managers**
                System.out.println("10. Update Menu");
                System.out.println("11. Update User");

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewProfile(esql); break;
                   case 2: updateProfile(esql); break;
                   case 3: viewMenu(esql); break;
                   case 4: placeOrder(esql); break;
                   case 5: viewAllOrders(esql); break;
                   case 6: viewRecentOrders(esql); break;
                   case 7: viewOrderInfo(esql); break;
                   case 8: viewStores(esql); break;
                   case 9: updateOrderStatus(esql); break;
                   case 10: updateMenu(esql); break;
                   case 11: updateUser(esql); break;



                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
public static void CreateUser(PizzaStore esql){
      try {
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         
         //check if user already exists
         String checkQuery = String.format("SELECT * FROM Users WHERE login = '%s'", login);
         if(esql.executeQuery(checkQuery) > 0) {
            System.out.println("User already exists!");
            return;
         }
         
         System.out.print("\tEnter password: ");
         String password = in.readLine();
         
         System.out.print("\tEnter phone number: ");
         String phoneNum = in.readLine();
         
         System.out.print("\tEnter role (customer/driver/manager): ");
         String role = in.readLine().toLowerCase();
         
         //validate role
         if (!role.equals("customer") && !role.equals("driver") && !role.equals("manager")) {
            System.out.println("Invalid role! Must be customer, driver, or manager.");
            return;
         }
         
         //insert new user
         String query = String.format("INSERT INTO Users (login, password, role, phoneNum) VALUES ('%s', '%s', '%s', '%s')",
                                    login, password, role, phoneNum);
         esql.executeUpdate(query);
         System.out.println("User successfully created!");
         
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/

public static String LogIn(PizzaStore esql){
      try {
         System.out.print("\tEnter login: ");
         String login = in.readLine();
         
         System.out.print("\tEnter password: ");
         String password = in.readLine();
         
         //check credentials
         String query = String.format("SELECT * FROM Users WHERE login = '%s' AND password = '%s'", login, password);
         if(esql.executeQuery(query) > 0) {
            System.out.println("Login successful!");
            return login;
         } else {
            System.out.println("Invalid login or password!");
            return null;
         }
         
      } catch(Exception e) {
         System.err.println(e.getMessage());
         return null;
      }
   }//end LogIn
   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/

// Rest of the functions definition go in here

public static void viewProfile(PizzaStore esql){
      try{
         System.out.print("Enter your username: ");
         String login = in.readLine();
         
         String query = String.format("SELECT login, role, favoriteItems, phoneNum FROM Users WHERE login = '%s'", login);
         int userCount = esql.executeQuery(query);
         
         if (userCount == 0) {
            System.out.println("User not found.");
            return;
         }
         
         //the executeQueryAndPrintResult method will automatically print the results
         esql.executeQueryAndPrintResult(query);
         
      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }//end viewProfile

public static void updateProfile(PizzaStore esql){
      try {
         System.out.print("\tEnter your login: ");
         String login = in.readLine();
         
         //check if user exists
         String checkQuery = String.format("SELECT * FROM Users WHERE login = '%s'", login);
         if(esql.executeQuery(checkQuery) == 0) {
            System.out.println("User does not exist!");
            return;
         }
         
         System.out.println("\nWhat would you like to update?");
         System.out.println("1. Password");
         System.out.println("2. Phone Number");
         System.out.println("3. Favorite Items");
         System.out.print("Enter your choice: ");
         
         int choice = Integer.parseInt(in.readLine());
         String updateField = "";
         String newValue = "";
         
         switch(choice) {
            case 1:
               System.out.print("\tEnter new password: ");
               newValue = in.readLine();
               updateField = "password";
               break;
            case 2:
               System.out.print("\tEnter new phone number: ");
               newValue = in.readLine();
               updateField = "phoneNum";
               break;
            case 3:
               System.out.print("\tEnter new favorite items (comma-separated): ");
               newValue = in.readLine();
               updateField = "favoriteItems";
               break;
            default:
               System.out.println("Invalid choice!");
               return;
         }
         
         String query = String.format("UPDATE Users SET %s = '%s' WHERE login = '%s'", 
                                    updateField, newValue, login);
         esql.executeUpdate(query);
         System.out.println("Profile updated successfully!");
         
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }//end updateProfile

   public static void viewMenu(PizzaStore esql){
      try {
         while(true) {
            System.out.println("\nMenu Options:");
            System.out.println("1. View all items");
            System.out.println("2. Filter by type");
            System.out.println("3. Filter by price");
            System.out.println("4. Sort by price (low to high)");
            System.out.println("5. Sort by price (high to low)");
            System.out.println("6. Back to main menu");
            System.out.print("Enter your choice: ");
            
            int choice = Integer.parseInt(in.readLine());
            String query = "";
            
            if(choice == 6) {
               return;
            }
            
            switch(choice) {
               case 1:
                  query = "SELECT itemName, typeOfItem, price, description FROM Items ORDER BY typeOfItem, itemName";
                  break;
                  
               case 2: //small bug where you it is displaying "typeofItem" and you have to hit space before entering the type in order to get the list
                  System.out.println("\nAvailable types:");
                  String typeQuery = "SELECT DISTINCT typeOfItem FROM Items ORDER BY typeOfItem";
                  esql.executeQueryAndPrintResult(typeQuery);
                  
                  System.out.print("\nEnter type to filter by: ");
                  String type = in.readLine();
                  
                  query = String.format("SELECT itemName, typeOfItem, price, description FROM Items WHERE typeOfItem = '%s' ORDER BY itemName", type);
                  break;
                  
               case 3:
                  System.out.print("\nEnter maximum price: $");
                  double maxPrice = Double.parseDouble(in.readLine());
                  
                  query = String.format("SELECT itemName, typeOfItem, price, description FROM Items WHERE price <= %.2f ORDER BY typeOfItem, itemName", maxPrice);
                  break;
                  
               case 4:
                  query = "SELECT itemName, typeOfItem, price, description FROM Items ORDER BY price ASC";
                  break;
                  
               case 5:
                  query = "SELECT itemName, typeOfItem, price, description FROM Items ORDER BY price DESC";
                  break;
                  
               default:
                  System.out.println("Invalid choice!");
                  continue;
            }
            
            System.out.println("\nMenu Items:");
            System.out.println("----------------------------------------");
            esql.executeQueryAndPrintResult(query);
            System.out.println("\nPress Enter to continue...");
            in.readLine();
         }
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }//end viewMenu

//todo: fix available stores call, implement rest of the functions to complete implementation
   public static void placeOrder(PizzaStore esql){

      //  String userQuery = "SELECT login, role FROM Users ORDER BY login";
         
      //    //check if user is a manager
      //    System.out.print("\tEnter your login: ");
      //    String login = in.readLine();
         
      //    String roleCheckQuery = String.format("SELECT role FROM Users WHERE login = '%s'", login);
      //    List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleCheckQuery);
      try {
         //first show available stores

         // String isOPen = "Select isOpen FROM Store  "
         // System.out.println("\nAvailable Stores:");
         // String storeQuery = String.format("SELECT storeID, address, city, state FROM Store WHERE isOpen = %o", );
         // int storeCount = esql.executeQueryAndPrintResult(storeQuery);
         
         // if(storeCount == 0) {
         //    System.out.println("No stores are currently open!");
         //    return;
         // }
         
         System.out.print("\nEnter store ID: ");
         int storeID = Integer.parseInt(in.readLine());

         String isOpen = String.format("SELECT isOpen FROM Store WHERE storeID = %o", storeID);
         List<List<String>> isOpenResult = esql.executeQueryAndReturnResult(isOpen); // store result in nested list and grab the first element of the first list
         String checkIsOpen = isOpenResult.get(0).get(0).trim();
         System.out.println(checkIsOpen);
         
         if (checkIsOpen.equals("no")) {
            System.out.println("Sorry. The store you have entered is not open.");
            return;
         }
         
         //check if store exists and is open

         // String checkStoreQuery = String.format("SELECT * FROM Store WHERE storeID = %d", storeID);
         // List<List<String>> storeResult = esql.executeQueryAndReturnResult(checkStoreQuery, storeID);

         // if(esql.executeQuery(checkStoreQuery) == 0) {
         //    System.out.println("Invalid store ID or store is closed!");
         //    return;
         // }

         // String checkStoreQuery = String.format("SELECT * FROM store WHERE storeID = %d " , storeID);
         // List<List<String>> storeResult = esql.executeQueryAndReturnResult(checkStoreQuery, storeID);

         // if (storeResult.isEmpty()) {
         //    System.out.println("Store ID not found! Try again.");
         //    break;
         // }

         // String isOpen = storeResult.get(0).get(0).trim();
         // if (!isOpen.equalsIgnoreCase("true")) {
         //       System.out.println("Sorry, this store is currently CLOSED. Please choose another.");
         //    continue;
         // }
         
         //show menu
         System.out.print("\tEnter Login Name: ");
         String current_user = in.readLine();

         String roleCheckQuery = String.format("SELECT role FROM Users WHERE login = '%s'", current_user);
         //List<List<String>> current_user = esql.executeQueryAndReturnResult(roleCheckQuery);

         System.out.println("\nMenu Items:");
         String menuQuery = "SELECT itemName, price FROM Items ORDER BY itemName";
         int menuCount = esql.executeQueryAndPrintResult(menuQuery);
         
         if(menuCount == 0) {
            System.out.println("No menu items available!");
            return;
         }
         
         //get order items
         List<String> items = new ArrayList<>();
         List<Integer> quantities = new ArrayList<>();
         double totalPrice = 0.0;
         
         while(true) {
            System.out.print("\nEnter item name (or 'done' to finish): ");
            String itemName = in.readLine();
            
            if(itemName.equalsIgnoreCase("done")) {
               break;
            }
            
            //check if item exists
            String checkItemQuery = String.format("SELECT price FROM Items WHERE itemName = '%s'", itemName);
            List<List<String>> result = esql.executeQueryAndReturnResult(checkItemQuery);
            if(result.isEmpty()) {
               System.out.println("Invalid item name!");
               continue;
            }
            
            System.out.print("Enter quantity: ");
            int quantity = Integer.parseInt(in.readLine());
            
            if(quantity <= 0) {
               System.out.println("Quantity must be positive!");
               continue;
            }
            
            items.add(itemName);
            quantities.add(quantity);
            totalPrice += Double.parseDouble(result.get(0).get(0)) * quantity;
         }
         
         if(items.isEmpty()) {
            System.out.println("No items in order!");
            return;
         }
         
         //create order
         // String 
         // String queryOrder = "INSERT INTO FoodOrder (orderID, login, storeID, totalPrice, orderTimestamp, orderStatus) VALUES (" + orderID + ", '" + userAccount + "', " + selectedStore + ", " + totalSum + ", '" + orderTimestamp + "', 'Ordered Placed');";
         // esql.executeUpdate(queryOrder);

         String orderQuery = String.format(
            "INSERT INTO FoodOrder (orderID, login, storeID, totalPrice, orderTimestamp, orderStatus) " +
            "VALUES ((SELECT COALESCE(MAX(orderID), 0) + 1 FROM FoodOrder), '%s', %d, %.2f, CURRENT_TIMESTAMP, 'pending')",
            current_user, storeID, totalPrice);
         esql.executeUpdate(orderQuery);
         
         //get the order ID
         String getOrderIDQuery = "SELECT MAX(orderID) FROM FoodOrder";
         List<List<String>> orderIDResult = esql.executeQueryAndReturnResult(getOrderIDQuery);
         int orderID = Integer.parseInt(orderIDResult.get(0).get(0));
         
         //add items to order
         for(int i = 0; i < items.size(); i++) {
            String itemQuery = String.format(
               "INSERT INTO ItemsInOrder (orderID, itemName, quantity) VALUES (%d, '%s', %d)",
               orderID, items.get(i), quantities.get(i));
            esql.executeUpdate(itemQuery);
         }
         
         System.out.printf("Order placed successfully! Order ID: %d, Total Price: $%.2f\n", orderID, totalPrice);
         
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }//end placeOrder

   public static void viewAllOrders(PizzaStore esql) {
      //print out the order history
      try{
         //check if user is a manager
         System.out.print("\tEnter your login: ");
         String login = in.readLine();
         
         String roleCheckQuery = String.format("SELECT role FROM Users WHERE login = '%s'", login);
         List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleCheckQuery);

         String viewOrderRole = roleResult.get(0).get(0).trim();

         if(viewOrderRole.isEmpty()){
            System.out.print("invaild role");
            return;
         }
         else if(viewOrderRole.equals("customer")){
         String query = "SELECT fd.orderID FROM FoodOrder fd, Users u WHERE fd.login = u.login ";
         int orderCount = esql.executeQuery(query);

         if(orderCount == 0){
            System.out.println("\nNo Orders");
            return;
         }
         
            System.out.println("\nOrder Hisotry");
            String allOrderQuery = "SELECT fd.orderID AS All_Orders FROM foodOrder fd, Users u WHERE fd.login = u.login";
            esql.executeQueryAndPrintResult(allOrderQuery);
         }
         else if(viewOrderRole.equals("manager") || viewOrderRole.equals("driver")){
            System.out.println("\nAll Orders");
            String allOrderQuery = "SELECT fd.orderID FROM foodOrder fd";
            esql.executeQueryAndPrintResult(allOrderQuery);
         } 

      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void viewRecentOrders(PizzaStore esql) {
      
              try{
         //check if user is a manager
         System.out.print("\tEnter your login: ");
         String login = in.readLine();
         
         String roleCheckQuery = String.format("SELECT role FROM Users WHERE login = '%s'", login);
         List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleCheckQuery);

         String viewOrderRole = roleResult.get(0).get(0).trim();

         if(viewOrderRole.isEmpty()){
            System.out.print("invaild role");
            return;
         }
         else if(viewOrderRole.equals("customer")){
         String query = "SELECT fd.orderID FROM FoodOrder fd, Users u WHERE fd.login = u.login ";
         int orderCount = esql.executeQuery(query);

         if(orderCount == 0){
            System.out.println("\nNo Orders");
            return;
         }
         
            System.out.println("\nOrder Hisotry");
            String allOrderQuery = "SELECT fd.orderID FROM foodOrder fd, Users u WHERE fd.login = u.login ORDER BY fd.orderTimeStamp DESC LIMIT 5";
            esql.executeQueryAndPrintResult(allOrderQuery);
         }
         else if(viewOrderRole.equals("manager") || viewOrderRole.equals("driver")){
            System.out.println("\nAll Orders");
            String allOrderQuery = "SELECT fd.orderID FROM foodOrder fd ORDER BY fd.orderTimeStamp DESC LIMIT 5";
            esql.executeQueryAndPrintResult(allOrderQuery);
         } 

      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void viewOrderInfo(PizzaStore esql) {
      try {
         // String query;
         // System.out.println("Enter the Order Number: ");
         // int orderNum = Integer.parseInt(in.readLine());

         System.out.print("\tEnter your login: ");
         String login = in.readLine();
         
         String roleCheckQuery = String.format("SELECT role FROM Users WHERE login = '%s'", login);
         List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleCheckQuery);

         String OrderRole = roleResult.get(0).get(0).trim();

         if(OrderRole.equals("customer")){
            String Custquery = String.format("Select fo.orderTimestamp, fo.totalPrice, fo.orderStatus, io.itemName, io.quantity, io.orderID FROM FoodOrder fo NATURAL JOIN ItemsInOrder io WHERE fo.login = '%s'", login); //ordernum
            esql.executeQueryAndPrintResult(Custquery);
         }
         else if( OrderRole.equals("manager") || OrderRole.equals("driver")){
                        String Custquery = String.format("Select fo.orderTimestamp, fo.totalPrice, fo.orderStatus, io.itemName, io.quantity, io.orderID FROM FoodOrder fo NATURAL JOIN ItemsInOrder io"); //ordernum
            esql.executeQueryAndPrintResult(Custquery);
         }
      

      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }
   

public static void viewStores(PizzaStore esql) {
      try {
         System.out.println("\nAll Stores:");
         System.out.println("----------------------------------------");
         
         //uery to get all store information
         String query = "SELECT storeID, address, city, state, isOpen, reviewScore FROM Store ORDER BY storeID";
         int storeCount = esql.executeQueryAndPrintResult(query);
         
         if(storeCount == 0) {
            System.out.println("No stores found in the system!");
         }
         
         System.out.println("\nPress Enter to continue...");
         in.readLine();
         
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }//end viewStores

   public static void updateOrderStatus(PizzaStore esql) {

      //gets the order ID
      String getOrderIDQuery = "SELECT MAX(orderID) FROM FoodOrder";

      try {

         System.out.print("\tEnter your login: ");
         String login = in.readLine();
         
         String roleCheckQuery = String.format("SELECT role FROM Users WHERE login = '%s'", login);
         List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleCheckQuery);

         String userRole = roleResult.get(0).get(0).trim();


          if(roleResult.isEmpty()){
            System.out.println("User does not exsist.");
            return;
         }

         if(!userRole.equals("manager")){
            System.out.println("Access denied! Only managers can update users.");
            return;
         }


         System.out.println("Enter OrderID: ");
         //int currOrderID = in.readLine();
         
         while(true) {
            System.out.println("1. Update Order Time Stamp");
            System.out.println("2. Update Order Status");
            System.out.println("3. Back to main menu");
            System.out.print("Enter your choice: ");
            
            int choice = Integer.parseInt(in.readLine());

            // String UpdateTimeStampQuery;
            // String UpdateOrderStatuspQuery;

            if(choice == 3){
               return;
            }
            switch(choice){
               case 1:
               System.out.print("Enter OrderID: ");
               int oID = Integer.parseInt(in.readLine());

               String checkQuery = String.format("SELECT orderID FROM foodOrder WHERE orderID = '%s'", oID);
               int orderIDCount = esql.executeQuery(checkQuery);

               if(orderIDCount == 0){
                  System.out.println("Invaid OrderID.");
                  break;
               }
               
                  System.out.println("\n Enter the new Time Stamp (YYYY-MM-DD HH:MI:SS): ");
                  String newTimeStamp = in.readLine();
                  //UpdateTimeStampQuery = String.format("SELECT * FROM FoodOrder WHERE orderID = '%i'", newTimeStamp); 
                  String updateTimeStamp = String.format(
                  "l", 
                  newTimeStamp, oID);
                  esql.executeUpdate(updateTimeStamp);
                  break;
            
               case 2:
               System.out.print("Enter OrderID: ");
               int oIDs = Integer.parseInt(in.readLine());

               String checkQuerys = String.format("SELECT orderID FROM foodOrder WHERE orderID = '%s'", oIDs);
               int orderIDCounts = esql.executeQuery(checkQuerys);

               if(orderIDCounts == 0){
                  System.out.println("Invaid OrderID.");
               }
            
                  System.out.println("\n Enter the new Order Status (Complete or Incomplete or Pending): ");
                  String newOrderStatus = in.readLine();
                  String UpdateOrderStatuspQuery = String.format("UPDATE FoodOrder SET orderStatus = '%s' WHERE orderID = '%s'", newOrderStatus, oIDs); 
                  esql.executeUpdate(UpdateOrderStatuspQuery);
                  break;
      

            }
         } 
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }

public static void updateMenu(PizzaStore esql){
      try {
         //define shared queries at the beginning
         String menuQuery = "SELECT itemName, typeOfItem, price FROM Items ORDER BY itemName";
         
         //check if user is a manager
         System.out.print("\tEnter your login: ");
         String login = in.readLine();
         
         String roleCheckQuery = String.format("SELECT role FROM Users WHERE login = '%s'", login);
         List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleCheckQuery);

         String usersRole = roleResult.get(0).get(0).trim(); //remove white space

         if(roleResult.isEmpty()){
            System.out.println("User does not exsist.");
            return;
         }

         if(!usersRole.equals("manager")){
            System.out.println("Access denied! Only managers can update users.");
            return;
         }
         
         //cannot get the security to work for just manager (both updateMenu and updateUser)
         // if(roleResult.isEmpty() || roleResult.get(0).get(0).equals("manager")) {
         //    System.out.println("Access denied! Only managers can update the menu.");
         //    return;
         // }
         
         while(true) {
            System.out.println("\nMenu Update Options:");
            System.out.println("1. Add new item");
            System.out.println("2. Update existing item");
            System.out.println("3. Delete item");
            System.out.println("4. Back to main menu");
            System.out.print("Enter your choice: ");
            
            int choice = Integer.parseInt(in.readLine());
            
            if(choice == 4) {
               return;
            }
            
            switch(choice) {
               case 1:
                  System.out.print("\tEnter item name: ");
                  String itemName = in.readLine();
                  
                  //check if item already exists
                  String checkQuery = String.format("SELECT * FROM Items WHERE itemName = '%s'", itemName);
                  if(esql.executeQuery(checkQuery) > 0) {
                     System.out.println("Item already exists!");
                     continue;
                  }
                  
                  System.out.print("\tEnter ingredients: ");
                  String ingredients = in.readLine();
                  
                  System.out.print("\tEnter type of item (e.g., drinks, entree, sides): ");
                  String typeOfItem = in.readLine().toLowerCase();
                  
                  //validate type
                  if (!typeOfItem.equals("drinks") && !typeOfItem.equals("entree") && !typeOfItem.equals("sides")) {
                     System.out.println("Invalid type! Must be drinks, entree, or sides.");
                     continue;
                  }
                  
                  System.out.print("\tEnter price: $");
                  double price = Double.parseDouble(in.readLine());
                  
                  if(price <= 0) {
                     System.out.println("Price must be greater than 0!");
                     continue;
                  }
                  
                  System.out.print("\tEnter description: ");
                  String description = in.readLine();
                  
                  String insertQuery = String.format(
                     "INSERT INTO Items (itemName, ingredients, typeOfItem, price, description) " +
                     "VALUES ('%s', '%s', '%s', %.2f, '%s')",
                     itemName, ingredients, typeOfItem, price, description);
                  esql.executeUpdate(insertQuery);
                  System.out.println("Item added successfully!");
                  break;
                  
               case 2:
                  System.out.println("\nCurrent Menu Items:");
                  esql.executeQueryAndPrintResult(menuQuery);
                  
                  System.out.print("\nEnter item name to update: ");
                  String updateItemName = in.readLine();
                  
                  //check if item exists
                  String checkUpdateQuery = String.format("SELECT * FROM Items WHERE itemName = '%s'", updateItemName);
                  if(esql.executeQuery(checkUpdateQuery) == 0) {
                     System.out.println("Item not found!");
                     continue;
                  }
                  
                  System.out.println("\nWhat would you like to update?");
                  System.out.println("1. Ingredients");
                  System.out.println("2. Type");
                  System.out.println("3. Price");
                  System.out.println("4. Description");
                  System.out.print("Enter your choice: ");
                  
                  int updateChoice = Integer.parseInt(in.readLine());
                  String updateField = "";
                  String newValue = "";
                  
                  switch(updateChoice) {
                     case 1:
                        System.out.print("\tEnter new ingredients: ");
                        newValue = in.readLine();
                        updateField = "ingredients";
                        break;
                     case 2:
                        System.out.print("\tEnter new type: ");
                        newValue = in.readLine().toLowerCase();
                        updateField = "typeOfItem";
                        
                        //validate type
                        if (!newValue.equals("drinks") && !newValue.equals("entree") && !newValue.equals("sides")) {
                           System.out.println("Invalid type! Must be drinks, entree, or sides.");
                           continue;
                        }
                        break;
                     case 3:
                        System.out.print("\tEnter new price: $");
                        double newPrice = Double.parseDouble(in.readLine());
                        if(newPrice <= 0) {
                           System.out.println("Price must be greater than 0!");
                           continue;
                        }
                        newValue = String.valueOf(newPrice);
                        updateField = "price";
                        break;
                     case 4:
                        System.out.print("\tEnter new description: ");
                        newValue = in.readLine();
                        updateField = "description";
                        break;
                     default:
                        System.out.println("Invalid choice!");
                        continue;
                  }
                  
                  String updateQuery = String.format(
                     "UPDATE Items SET %s = '%s' WHERE itemName = '%s'",
                     updateField, newValue, updateItemName);
                  esql.executeUpdate(updateQuery);
                  System.out.println("Item updated successfully!");
                  break;
                  
               case 3:
                  System.out.println("\nCurrent Menu Items:");
                  esql.executeQueryAndPrintResult(menuQuery);
                  
                  System.out.print("\nEnter item name to delete: ");
                  String deleteItemName = in.readLine();
                  
                  //check if item exists
                  String checkDeleteQuery = String.format("SELECT * FROM Items WHERE itemName = '%s'", deleteItemName);
                  if(esql.executeQuery(checkDeleteQuery) == 0) {
                     System.out.println("Item not found!");
                     continue;
                  }
                  
                  //check if item is in any orders
                  String checkOrderQuery = String.format(
                     "SELECT COUNT(*) FROM ItemsInOrder WHERE itemName = '%s'", deleteItemName);
                  List<List<String>> orderResult = esql.executeQueryAndReturnResult(checkOrderQuery);
                  if(Integer.parseInt(orderResult.get(0).get(0)) > 0) {
                     System.out.println("Cannot delete item: it is part of existing orders!");
                     continue;
                  }
                  
                  String deleteQuery = String.format("DELETE FROM Items WHERE itemName = '%s'", deleteItemName);
                  esql.executeUpdate(deleteQuery);
                  System.out.println("Item deleted successfully!");
                  break;
                  
               default:
                  System.out.println("Invalid choice!");
                  break;
            }
            
            System.out.println("\nPress Enter to continue...");
            in.readLine();
         }
         
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }//end updateMenu

public static void updateUser(PizzaStore esql){
      try {
         //define shared queries at the beginning
         String userQuery = "SELECT login, role FROM Users ORDER BY login";

         //check if user is a manager
         System.out.print("\tEnter your login: ");
         String login = in.readLine();
         
         String roleCheckQuery = String.format("SELECT role FROM Users WHERE login = '%s'", login);
         List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleCheckQuery);

         String usersRole = roleResult.get(0).get(0).trim(); //remove white space

         if(roleResult.isEmpty()){
            System.out.println("User does not exsist.");
            return;
         }

         if(!usersRole.equals("manager")){
            System.out.println("Access denied! Only managers can update users.");
            return;
         }
         
         // if(roleResult.isEmpty() || roleResult.get(0).get(0).equals("manager")4) {
         //    System.out.println("Access denied! Only managers can update users.");
         //    return;
         // }
         
         while(true) {
            System.out.println("\nUser Update Options:");
            System.out.println("1. Update user role");
            System.out.println("2. Update user login");
            System.out.println("3. Back to main menu");
            System.out.print("Enter your choice: ");
            
            int choice = Integer.parseInt(in.readLine());
            
            if(choice == 3) {
               return;
            }
            
            switch(choice) {
               case 1:
                  System.out.println("\nCurrent Users:");
                  esql.executeQueryAndPrintResult(userQuery);
                  
                  System.out.print("\nEnter user login to update: ");
                  String updateLogin = in.readLine();
                  
                  //check if user exists
                  String checkUserQuery = String.format("SELECT * FROM Users WHERE login = '%s'", updateLogin);
                  if(esql.executeQuery(checkUserQuery) == 0) {
                     System.out.println("User not found!");
                     continue;
                  }
                  
                  System.out.print("\tEnter new role (customer/driver/manager): ");
                  String newRole = in.readLine().toLowerCase();
                  
                  //validate role
                  if (!newRole.equals("customer") && !newRole.equals("driver") && !newRole.equals("manager")) {
                     System.out.println("Invalid role! Must be customer, driver, or manager.");
                     continue;
                  }
                  
                  String updateRoleQuery = String.format(
                     "UPDATE Users SET role = '%s' WHERE login = '%s'",
                     newRole, updateLogin);
                  esql.executeUpdate(updateRoleQuery);
                  System.out.println("User role updated successfully!");
                  break;
                  
               case 2:
                  System.out.println("\nCurrent Users:");
                  esql.executeQueryAndPrintResult(userQuery);
                  
                  System.out.print("\nEnter current user login to update: ");
                  String currentLogin = in.readLine();
                  
                  //check if user exists
                  String checkLoginUserQuery = String.format("SELECT * FROM Users WHERE login = '%s'", currentLogin);
                  if(esql.executeQuery(checkLoginUserQuery) == 0) {
                     System.out.println("User not found!");
                     continue;
                  }
                  
                  System.out.print("\tEnter new login: ");
                  String newLogin = in.readLine();
                  
                  //check if new login already exists
                  String checkNewLoginQuery = String.format("SELECT * FROM Users WHERE login = '%s'", newLogin);
                  if(esql.executeQuery(checkNewLoginQuery) > 0) {
                     System.out.println("Login already exists! Please choose a different login.");
                     continue;
                  }
                  
                  //update login in Users table
                  String updateLoginQuery = String.format(
                     "UPDATE Users SET login = '%s' WHERE login = '%s'",
                     newLogin, currentLogin);
                  esql.executeUpdate(updateLoginQuery);
                  
                  //update login in FoodOrder table
                  String updateOrderLoginQuery = String.format(
                     "UPDATE FoodOrder SET login = '%s' WHERE login = '%s'",
                     newLogin, currentLogin);
                  esql.executeUpdate(updateOrderLoginQuery);
                  
                  System.out.println("User login updated successfully!");
                  break;
                  
               default:
                  System.out.println("Invalid choice!");
                  break;
            }
            
            System.out.println("\nPress Enter to continue...");
            in.readLine();
         }
         
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }//end updateUser

};
//end PizzaStore

