import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Scanner;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Command-line application for querying the YaTV Database in MySQL.
 *
 * @derbinsky - Cited sqlite-java-app (ChinookApp) and SQLite Java Demo.
 */
public class YatvApp {

  /**
   * Allowed query types
   */
  private static enum QueryTypes {
    RegisterUser, SubscribeUser, AddToMyList, UpdatePlatformVersion, AddLatestVideo,
    MostWatchedShowsByApp, FindFreeVideosByPlatform, FindLongVideosNoShow,
    AppRevenueByCountry, TopThreeWatchedTags, HighestCustomer, LowestApp, MostProfitableVideos,
    MostWatchedEpisodes, MobileAppsRevenueRanked
  }

  /**
   * Query type and parameter value (null if not appropriate)
   */
  private static class QueryData {

    final public QueryTypes queryType;

    public QueryData(QueryTypes qn) {
      queryType = qn;
    }

    @Override
    public String toString() {
      return String.format("%s" + queryType);
    }
  }

  /**
   * Usage statement, then exit
   *
   * @return null (to make other code easier)
   */
  private static QueryData _usage() {
    System.out.printf("Usage: java %s <path to database> <query #> [parameter values]%n%n",
        YatvApp.class.getCanonicalName());
    System.out.printf("1) Register a new user [parameter values] %n");
    System.out.printf("2) Subscribe a user [parameter value] to an app [parameter value] %n");
    System.out.printf("3) Add a show to a user’s [parameter value] “My List” ? %n");
    System.out.printf("4) Update an app’s [parameter value] version on a platform %n");
    System.out.printf("5) Add a new video [parameter value] (with all associated meta data),"
        + " which is the latest in a show’s current season %n");
    System.out.printf("6) Produce a ranked list of the top-10 most watched shows,"
        + " each with the corresponding app %n");
    System.out.printf("7) Find all free videos on a particular platform [parameter value] %n");
    System.out.printf("8) Find all long videos that were released this year"
        + " and aren’t part of any show %n");
    System.out.printf("9) Produce a ranked list of revenue generated"
        + " by apps in a country [parameter value] %n");
    System.out.printf("10) Produce a ranked list of watch counts from the top-3 video tags %n");
    System.out.printf("REPORT 1- Find the customer with the highest revenue"
        + " for a certain country [parameter value] %n");
    System.out.printf("REPORT 2- Find the app with the LOWEST revenue (by subscription)"
        + " for a certain country [parameter value] %n");
    System.out.printf("REPORT 3- Find the Top 3 most watched videos for the"
        + " most profitable app in a certain country [parameter value] %n");
    System.out
        .printf("REPORT 4- What is the most watched episode from [parameter value] a show %n");
    System.out
        .printf("REPORT 5- Produce an ordered list of apps revenue (rounded to the nearest tenth)"
            + " by country for mobile users %n");

    System.exit(0);
    return null;
  }

  /**
   * Validates command-line arguments
   *
   * @param args command-line arguments
   * @return query data, or null if invalid
   * @throws ClassNotFoundException cannot find JDBC driver
   */
  private static QueryData validateInputs(String[] args) throws ClassNotFoundException {
    // Must have at least one argument
    if (args.length < 1) {
      return _usage();
    }

    // Make sure the argument is a valid query number
    try {
      final int queryNum = Integer.valueOf(args[0]);

      if (queryNum == 1) {
        return new QueryData(QueryTypes.RegisterUser);
      } else if (queryNum == 2) {
        return new QueryData(QueryTypes.SubscribeUser);
      } else if (queryNum == 3) {
        return new QueryData(QueryTypes.AddToMyList);
      } else if (queryNum == 4) {
        return new QueryData(QueryTypes.UpdatePlatformVersion);
      } else if (queryNum == 5) {
        return new QueryData(QueryTypes.AddLatestVideo);
      } else if (queryNum == 6) {
        return new QueryData(QueryTypes.MostWatchedShowsByApp);
      } else if (queryNum == 7) {
        return new QueryData(QueryTypes.FindFreeVideosByPlatform);
      } else if (queryNum == 8) {
        return new QueryData(QueryTypes.FindLongVideosNoShow);
      } else if (queryNum == 9) {
        return new QueryData(QueryTypes.AppRevenueByCountry);
      } else if (queryNum == 10) {
        return new QueryData(QueryTypes.TopThreeWatchedTags);
      } else if (queryNum == 11) {
        return new QueryData(QueryTypes.HighestCustomer);
      } else if (queryNum == 12) {
        return new QueryData(QueryTypes.LowestApp);
      } else if (queryNum == 13) {
        return new QueryData(QueryTypes.MostProfitableVideos);
      } else if (queryNum == 14) {
        return new QueryData((QueryTypes.MostWatchedEpisodes));
      } else if (queryNum == 15) {
        return new QueryData((QueryTypes.MobileAppsRevenueRanked));
      } else {
        return _usage();
      }

    } catch (NumberFormatException e) {
      return _usage();
    }
  }

  /**
   * Command-line Chinook utility
   *
   * @param args command-line arguments
   * @throws ClassNotFoundException cannot find JDBC driver
   * @throws SQLException           SQL gone bad
   */
  public static void main(String[] args) throws ClassNotFoundException, SQLException {

    // Validates the inputs, exits if bad
    final QueryData qd = validateInputs(args);

    // Makes a connection to the database
    try (final Connection connection =
        DriverManager.getConnection("jdbc:mysql://localhost/Project?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=EST", "root", "");
        final Scanner input = new Scanner(System.in)) {

      // QUERY 1- User Registration
      if (qd.queryType == QueryTypes.RegisterUser) {

        System.out.printf("Enter Your First Name: ");
        final String fname = input.nextLine();
        System.out.printf("Enter Your Last Name: ");
        final String lname = input.nextLine();
        System.out.printf("Enter Your Country: ");
        final String country = input.nextLine();
        System.out.printf("Enter Your Email: ");
        final String email = input.nextLine();
        System.out.printf("Enter Your Password: ");
        final String password = input.nextLine();

        final String sql =
            "INSERT INTO User (UserId, FirstName, LastName, Country, Email, Password) "
                + "VALUES (DEFAULT, ?, ?, ?, ?, ?)";
        final String sql2 = "SELECT * FROM User u WHERE u.Email=?";
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
          final PreparedStatement stmt2 = connection.prepareStatement(sql2);

          //Hashing User's Password (Salt: 9)
          String hashedPass = BCrypt.hashpw(password, BCrypt.gensalt(9));

          // Executing the INSERT
          stmt.setString(1, fname);
          stmt.setString(2, lname);
          stmt.setString(3, country);
          stmt.setString(4, email);
          stmt.setString(5, hashedPass);
          stmt.executeUpdate();

          // Displaying the results of the INSERT statement
          stmt2.setString(1, email);
          try (final ResultSet res = stmt2.executeQuery()) {
            while (res.next()) {
              System.out
                  .printf("REGISTERED! %n"
                          + "USER ID: %d, FIRST NAME: %s, LAST NAME: %s, COUNTRY: %s, EMAIL: %s",
                      res.getInt("UserID"), res.getString("FirstName"),
                      res.getString("LastName"), res.getString("Country"),
                      res.getString("Email"));
            }
          }
        }
      }

      // QUERY 2- Subscribing a User to an App
      else if (qd.queryType == QueryTypes.SubscribeUser) {

        System.out.printf("Enter Your UserID: ");
        final Integer userId = input.nextInt();

        System.out.printf("Available Apps: %n");
        final String options = "SELECT a.AppID AS AppID, a.Name AS Name FROM App a";
        try (final PreparedStatement apps = connection.prepareStatement(options)) {
          try (final ResultSet res4 = apps.executeQuery()) {
            while (res4.next()) {
              System.out.printf("ID: %d Name: %s %n",
                  res4.getInt("AppID"), res4.getString("Name"));
            }
          }
        }
        System.out.printf("Enter the AppID that you would like to subscribe to: ");
        final Integer appId = input.nextInt();
        System.out.printf("How many months would you like to uphold this subscription? ");
        final Integer months = input.nextInt();

        //Get current date and add # of months into the future
        LocalDate expDates = LocalDate.now().plusMonths(months);

        final String findCost = "SELECT a.MonthlyCost AS monthlyCost FROM App a WHERE a.AppID= ?";
        final String ins = "INSERT INTO Subscription(UserID, Cost, ExpDate, AppID)"
            + " VALUES (?, ?, ?, ?)";
        final String display =
            "SELECT u.UserID AS UserID, a.Name AS Name, s.Cost AS Cost, s.ExpDate AS ExpDate"
                + " FROM Subscription s JOIN User u ON u.UserID = s.UserID"
                + " JOIN App a ON s.AppID = a.AppID"
                + " WHERE s.UserID = ?";
        try (final PreparedStatement stmt = connection.prepareStatement(findCost)) {
          final PreparedStatement stmt2 = connection.prepareStatement(ins);
          final PreparedStatement stmt3 = connection.prepareStatement(display);

          // Finding the Cost
          stmt.setInt(1, appId);
          try (final ResultSet res = stmt.executeQuery()) {
            int cost = 0;
            while (res.next()) {
              cost = res.getInt("monthlyCost") * months;
            }

            // Executing the INSERT
            stmt2.setInt(1, userId);
            stmt2.setDouble(2, cost);
            stmt2.setDate(3, Date.valueOf(expDates));
            stmt2.setInt(4, appId);
            stmt2.executeUpdate();

            System.out.printf("Success! Current Subscriptions: %n");
            // Displaying the Results
            stmt3.setInt(1, userId);
            try (final ResultSet res3 = stmt3.executeQuery()) {
              while (res3.next()) {
                System.out.printf("User ID: %d, App: %s, Cost: %d. ExpDate: %tF %n",
                    res3.getInt("UserID"), res3.getString("Name"),
                    res3.getInt("Cost"), res3.getDate("ExpDate"));
              }
            }
          }
        }
      }

      // QUERY 3- Add a Show to User's List
      else if (qd.queryType == QueryTypes.AddToMyList) {

        System.out.printf("Enter your UserID: ");
        final Integer userId = input.nextInt();

        System.out.printf("Available Shows: %n");
        final String options = ("SELECT ShowID, Title FROM Shows");
        try (final PreparedStatement stmt6 = connection.prepareStatement(options)) {
          try (final ResultSet res6 = stmt6.executeQuery()) {
            while (res6.next()) {
              System.out.printf("ID: %d Name: %s %n", res6.getInt("ShowID"),
                  res6.getString("Title"));
            }
          }
        }
        System.out.printf("Enter the ShowID that you would like to add to your list: ");
        final Integer showId = input.nextInt();

        final String sql = "INSERT INTO MyListShow (UserId, ShowID)"
            + " VALUES(?, ?)";
        final String sql1 = "SELECT *"
            + " FROM MyListShow m JOIN Shows s ON m.ShowID=s.ShowID"
            + " WHERE m.UserID = ?";
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
          final PreparedStatement stmt2 = connection.prepareStatement(sql1);

          // Executing the INSERT
          stmt.setInt(1, userId);
          stmt.setInt(2, showId);
          stmt.executeUpdate();

          // Displaying the Results
          System.out.printf("Success! Current Show(s) on Your List: %n");
          stmt2.setInt(1, userId);
          try (final ResultSet res2 = stmt2.executeQuery()) {
            while (res2.next()) {
              //Displaying current shows for this user's Mylist
              System.out.printf(
                  "USER ID: %d SHOW: %s %n",
                  res2.getInt("UserID"),
                  res2.getString("Title"));

            }
          }
        }
      }

      // QUERY 4- Update an App's Version Number on a Platform
      else if (qd.queryType == QueryTypes.UpdatePlatformVersion) {

        System.out.printf("Available Apps: %n");
        final String options = "SELECT a.AppID AS AppID, a.Name AS Name FROM App a";
        try (final PreparedStatement apps = connection.prepareStatement(options)) {
          try (final ResultSet res4 = apps.executeQuery()) {
            while (res4.next()) {
              System.out.printf("ID: %d Name: %s %n",
                  res4.getInt("AppID"), res4.getString("Name"));
            }
          }
        }
        System.out.printf("Enter the AppID of the App that you are Updating: ");
        final Integer appId = input.nextInt();

        final String appPlatform = "SELECT p.Name AS platName, p.PlatformID AS platID"
            + " FROM AppPlatform ap JOIN App a ON ap.AppID=a.AppID"
            + " JOIN Platform p ON ap.PlatformID=p.PlatformID"
            + " WHERE ap.AppID=?";
        System.out.printf("This App is available on Platform(s): %n");

        // Displaying the available Platforms for the selected App
        try (final PreparedStatement stmt2 = connection.prepareStatement(appPlatform)) {
          stmt2.setInt(1, appId);
          try (final ResultSet res = stmt2.executeQuery()) {
            while (res.next()) {
              System.out.printf("PlatformID: %d, Platform: %s %n",
                  res.getInt("platID"), res.getString("platName"));
            }
          }
        }
        System.out.printf(
            "Enter the PlatformID of the Platform on which you want to perform the update: ");
        final Integer platId = input.nextInt();

        System.out.printf("Enter the updated Version Number: ");
        final Float verNum = input.nextFloat();

        final String insert = "UPDATE AppPlatform"
            + " SET VersionNum = ?"
            + " WHERE AppID = ? AND PlatformID = ?";
        final String display =
            "SELECT a.Name AS appName, p.Name as platName, ap.VersionNum AS verNum"
                + " FROM AppPlatform ap JOIN App a ON ap.AppID=a.AppID"
                + " JOIN Platform p ON ap.PlatformID=p.PlatformID"
                + " WHERE ap.AppID = ? AND ap.PlatformID = ?";

        try (final PreparedStatement stmt = connection.prepareStatement(insert)) {
          final PreparedStatement stmt1 = connection.prepareStatement(display);

          // Executing the INSERT
          stmt.setFloat(1, verNum);
          stmt.setInt(2, appId);
          stmt.setInt(3, platId);
          stmt.executeUpdate();

          // Displaying the Results
          stmt1.setInt(1, appId);
          stmt1.setInt(2, platId);
          try (final ResultSet res1 = stmt1.executeQuery()) {
            while (res1.next()) {
              System.out.printf(
                  "Success! Current Version of this App on this Platform: %n" +
                      "APP: %s, PLATFORM: %s, Version Number: %.2f %n",
                  res1.getString("appName"), res1.getString("platName"),
                  res1.getFloat("verNum"));


            }
          }
        }
      }

      // QUERY 5- Add the Latest Video in a Show's Current Season
      else if (qd.queryType == QueryTypes.AddLatestVideo) {

        System.out.printf("Available Shows: %n");
        final String options = ("SELECT ShowID, Title FROM Shows");
        try (final PreparedStatement stmt6 = connection.prepareStatement(options)) {
          try (final ResultSet res6 = stmt6.executeQuery()) {
            while (res6.next()) {
              System.out.printf("ID: %d Name: %s %n", res6.getInt("ShowID"),
                  res6.getString("Title"));
            }
          }
        }
        System.out.printf("Enter the ShowID: ");
        final Integer showId = input.nextInt();
        input.nextLine();
        System.out.printf("Enter the Title of the Video: ");
        final String title = input.nextLine();
        System.out.printf("Enter the Description of the Video: ");
        final String desc = input.nextLine();
        System.out.printf("Enter the Duration of the Video (in seconds): ");
        final Integer duration = input.nextInt();
        System.out.printf("Is a Subscription Required for this Video? (True or False): ");
        final Boolean sub = input.nextBoolean();
//        System.out.printf("Enter the Episode Number of this Video: ");
//        final Integer epNum = input.nextInt();
        input.nextLine();
        System.out.printf("Enter the Release Date as YYYY-MM-DD: ");
        final String releaseDate = input.nextLine();

        final String getApp = "SELECT a.AppID AS appID"
            + " FROM Video v JOIN App a ON a.AppID = v.AppID"
            + " WHERE v.VideoID IN ("
            + " SELECT s.VideoID FROM Seasons s WHERE s.ShowID = ? GROUP BY s.ShowID)"
            + " GROUP BY a.AppID";
        final String video =
            "INSERT INTO Video (VideoID, Title, Description, Duration, AppID, SubNeeded, ReleaseDate, ShowID) VALUES"
                + " (DEFAULT, ?, ?, ?, ?, ?, ?, ?) ";
        final String getVidId = "SELECT v.VideoId AS vidId FROM Video v WHERE v.Title = ?";
        final String currSeason =
            "SELECT MAX(s.SeasonNum) AS currSeason, MAX(s.EpisodeNum) AS maxEp"
                + " FROM Seasons s"
                + " WHERE s.ShowID = ?";
        final String season =
            "INSERT INTO Seasons (SeasonID, ShowID, VideoID, SeasonNum, EpisodeNum) VALUES"
                + " (DEFAULT, ?, ?, ?, ?) ";
        final String display = "SELECT sh.Title as showName, v.Title as vidName,"
            + " s.SeasonNum as seasonNum, s.EpisodeNum as epNum"
            + " FROM Seasons s JOIN Shows sh ON s.ShowID=sh.ShowID"
            + " JOIN Video v ON v.VideoID=s.VideoID"
            + " WHERE sh.ShowID=?";

        try (final PreparedStatement stmt = connection.prepareStatement(video)) {
          final PreparedStatement stmt2 = connection.prepareStatement(getVidId);
          final PreparedStatement stmt3 = connection.prepareStatement(season);
          final PreparedStatement stmt4 = connection.prepareStatement(currSeason);
          final PreparedStatement stmt5 = connection.prepareStatement(display);
          final PreparedStatement stmt6 = connection.prepareStatement(getApp);

          // Finding the Shows AppID
          stmt6.setInt(1, showId);
          int appId = 0;
          try (final ResultSet res = stmt6.executeQuery()) {
            while (res.next()) {
              appId = res.getInt("appID");
            }

            // Executing the INSERT into Video
            stmt.setString(1, title);
            stmt.setString(2, desc);
            stmt.setInt(3, duration);
            stmt.setInt(4, appId);
            stmt.setBoolean(5, sub);
            stmt.setString(6, releaseDate);
            stmt.setInt(7, showId);
            stmt.executeUpdate();

            // Getting the VideoID
            int vidId = 0;
            stmt2.setString(1, title);
            try (final ResultSet res2 = stmt2.executeQuery()) {
              while (res2.next()) {
                vidId = res2.getInt("vidId");
              }

              // Getting the Current Season
              int curSea = 0;
              int maxEpisode = 0;
              stmt4.setInt(1, showId);
              try (final ResultSet res3 = stmt4.executeQuery()) {
                while (res3.next()) {
                  curSea = res3.getInt("currSeason");
                  maxEpisode = res3.getInt("maxEp") + 1;
                }

                // Executing the INSERT into Seasons
                stmt3.setInt(1, showId);
                stmt3.setInt(2, vidId);
                stmt3.setInt(3, curSea);
                stmt3.setInt(4, maxEpisode);
                stmt3.executeUpdate();
              }

              System.out.printf("Success! Episodes in the Current Season: %n");
              // Displaying the Results
              stmt5.setInt(1, showId);
              try (final ResultSet res4 = stmt5.executeQuery()) {
                while (res4.next()) {
                  System.out.printf("SHOW: %s, VIDEO: %s, SEASON: %d, EPISODE: %d %n",
                      res4.getString("showName"), res4.getString("vidName"),
                      res4.getInt("seasonNum"), res4.getInt("epNum"));
                }
              }
            }
          }
        }
      }

      // QUERY 6- Produce a Ranked List of the Top 10 Most Watched Shows (each with its corresponding app).
      else if (qd.queryType == QueryTypes.MostWatchedShowsByApp) {
        final String sql =
            "SELECT q1.countWatch AS watchCount, q1.ShowName AS showName, a.Name AS appName"
                + " FROM App a"
                + " JOIN (SELECT COUNT(sh.ShowID) AS countWatch, sh.Title AS showName, v.AppID AS AppID"
                + " FROM UserVideoWatched uw JOIN Video v ON v.VideoID = uw.VideoID"
                + " JOIN Seasons se ON se.VideoID = v.VideoID"
                + " JOIN Shows sh ON sh.ShowID = se.ShowID"
                + " GROUP BY sh.ShowID) q1 ON a.AppID = q1.AppID"
                + " ORDER BY q1.countWatch DESC LIMIT 10";

        // Executing the Query
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
          try (final ResultSet res = stmt.executeQuery()) {
            while (res.next()) {
              System.out.printf("WATCH COUNT: %s SHOW: %s APP: %s %n",
                  res.getString("watchCount"),
                  res.getString("showName"),
                  res.getString("appName"));
            }
          }
        }
      }

      // QUERY 7- Find All Free Videos on a Particular Platform
      else if (qd.queryType == QueryTypes.FindFreeVideosByPlatform) {

        System.out.printf("Available Platforms: %n");
        final String options = "SELECT p.PlatformID AS platID, p.Name AS Name FROM Platform p";
        try (final PreparedStatement plats = connection.prepareStatement(options)) {
          try (final ResultSet res4 = plats.executeQuery()) {
            while (res4.next()) {
              System.out.printf("ID: %d Name: %s %n",
                  res4.getInt("platID"), res4.getString("Name"));
            }
          }
        }
        System.out.printf("Enter the PlatformID: ");
        final String platformId = input.nextLine();

        final String sql = " SELECT p.Name as PlatformName, v.Title AS VideoTitle"
            + " FROM AppPlatform ap JOIN Platform p ON p.PlatformID = ap.PlatformID "
            + " JOIN App a ON a.AppID = ap.AppID "
            + " JOIN Video v ON v.AppID = a.AppID "
            + " WHERE v.SubNeeded = 0 AND p.PlatformID = ?";

        // Executing the query
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
          // Setting the param to PlatformID
          stmt.setString(1, platformId);
          try (final ResultSet res = stmt.executeQuery()) {
            while (res.next()) {
              System.out.printf("PLATFORM: %s, VIDEO NAME: %s %n",
                  res.getString("PlatformName"),
                  res.getString("VideoTitle"));
            }
          }
        }
      }

      // QUERY 8- Find All Long Videos Released This Year That Are Not Part of Any Show
      // Long Videos are Videos with a duration of over 1000
      else if (qd.queryType == QueryTypes.FindLongVideosNoShow) {
        final String sql =
            "SELECT v.VideoID AS videoID, v.Title AS videoTitle, v.Duration AS duration"
                + " FROM Video v"
                + " WHERE (v.VideoID NOT IN (SELECT s.VideoID FROM Seasons s))"
                + " AND (v.ReleaseDate LIKE '%2020%') "
                + " AND (v.Duration > 1000)";

        // Executing the Query
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
          try (final ResultSet res = stmt.executeQuery()) {
            while (res.next()) {
              System.out.printf("VIDEO ID: %s, TITLE: %s, DURATION: %d %n",
                  res.getString("videoID"),
                  res.getString("videoTitle"),
                  res.getInt("duration"));

            }
          }
        }
      }

      // QUERY 9- Produce a Ranked List of Revenue Generated by Apps in a Country
      else if (qd.queryType == QueryTypes.AppRevenueByCountry) {

        System.out.printf("Available Countries: %n");
        final String options = ("SELECT DISTINCT Country FROM User");
        try (final PreparedStatement stmt6 = connection.prepareStatement(options)) {
          try (final ResultSet res6 = stmt6.executeQuery()) {
            while (res6.next()) {
              System.out.printf("COUNTRY: %s %n", res6.getString("Country"));
            }
          }
        }
        System.out.printf("Enter the Country: ");
        final String country = input.nextLine();

        final String sql =
            "SELECT u.Country AS Country, SUM(s.Cost) AS Revenue, a.Name AS AppName"
                + " FROM Subscription s JOIN User u ON s.UserID = u.UserID"
                + " JOIN App a ON a.AppID = s.AppID"
                + " WHERE u.Country = ?"
                + " GROUP BY a.AppID"
                + " ORDER BY Revenue DESC";

        // Executing the Query
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
          // Setting the param to Country
          stmt.setString(1, country);
          try (final ResultSet res = stmt.executeQuery()) {
            while (res.next()) {
              System.out.printf("COUNTRY: %s, APP: %s, REVENUE: %s %n",
                  res.getString("Country"),
                  res.getString("AppName"),
                  res.getString("Revenue"));
            }
          }
        }
      }

      // QUERY 10- Produce a Ranked List of Watch Count from the Top 3 Video Tags
      else if (qd.queryType == QueryTypes.TopThreeWatchedTags) {
        final String sql = "SELECT (COUNT(uvm.VideoID)) AS viewCount,"
            + " t.Tag AS tagName FROM Tag t"
            + " INNER JOIN Video v ON v.VideoID = t.VideoID"
            + " JOIN UserVideoWatched uvm ON v.VideoID = uvm.VideoID"
            + " GROUP BY tagName"
            + " ORDER by viewCount DESC LIMIT 3";

        // Executing the Query
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
          try (final ResultSet res = stmt.executeQuery()) {
            while (res.next()) {
              System.out.printf("VIEW COUNT: %s, TAG NAME: %s %n",
                  res.getString("viewCount"),
                  res.getString("tagName"));
            }
          }
        }
      }

      // REPORT QUERY 1 (QUERY 11)
      // Find the Customer with the Highest Revenue for a certain Country.
      // There is:
      // - 2 aggregate function SUM, MAX
      // - Strong Motivation/Justification: Finding the customer with the highest sales revenue can help with
      //   customer service and such
      // - 1 non-aggregate function ROUND
      // - 1 Grouping
      // - 1 Subquery
      // - 3 Tables Joined
      
      else if (qd.queryType == QueryTypes.HighestCustomer) {

        System.out.printf("Available Countries: %n");
        final String options = ("SELECT DISTINCT Country FROM User");
        try (final PreparedStatement stmt6 = connection.prepareStatement(options)) {
          try (final ResultSet res6 = stmt6.executeQuery()) {
            while (res6.next()) {
              System.out.printf("COUNTRY: %s %n", res6.getString("Country"));
            }
          }
        }
        System.out.printf("Enter the Country: ");
        final String country = input.nextLine();

        final String sql = "SELECT q1.ID AS UserID, q1.FirstName AS firstName,"
            + " q1.LastName AS lastName, MAX(q1.Revenue) as Revenue"
            + " FROM (SELECT u.UserID AS ID, u.FirstName as FirstName, u.LastName as LastName,"
            + " ROUND(SUM(s.Cost), 2) as Revenue, u.Country as Country"
            + " FROM User u JOIN Subscription s ON u.UserID =s.UserID"
            + " GROUP BY u.UserID) q1"
            + " WHERE q1.Country = ? ";

        // Executing the Query
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
          // Setting the param to Country
          stmt.setString(1, country);
          try (final ResultSet res = stmt.executeQuery()) {
            while (res.next()) {
              System.out.printf("UserID: %d %nFirstName: %s %nLastName: %s %nRevenue: %s%n",
                  res.getInt("UserID"), res.getString("firstName"),
                  res.getString("lastName"), res.getString("Revenue"));
            }
          }
        }
      }

      // REPORT QUERY 2 (QUERY 12)
      // Find the App with the Lowest Revenue for a certain Country.
      // THERE IS:
      // - 3 tables JOINED
      // - A SubQuery
      // - An Aggregate function SUM
      // - A Non-aggregate function ROUND
      // - A Grouping
      // - Strong Justification/Motivation: Finding the APP with the Lowest subscription revenue can help with 
      //   budgeting and marketing decisions
      else if (qd.queryType == QueryTypes.LowestApp) {

        System.out.printf("Available Countries: %n");
        final String options = ("SELECT DISTINCT Country FROM User");
        try (final PreparedStatement stmt6 = connection.prepareStatement(options)) {
          try (final ResultSet res6 = stmt6.executeQuery()) {
            while (res6.next()) {
              System.out.printf("COUNTRY: %s %n", res6.getString("Country"));
            }
          }
        }
        System.out.printf("Enter the Country: ");
        final String country = input.nextLine();

        final String sql =
            "SELECT q1.Revenue as Revenue, q1.AppID as AppID, q1.AppName as AppName, q1.Country as Country "
                + " FROM (SELECT ROUND(SUM(s.Cost), 2) as Revenue, a.AppID as AppID, a.Name as AppName, u.Country as Country "
                + " FROM Subscription s JOIN App a ON a.AppID = s.AppID"
                + " JOIN User u ON s.UserID = u.UserID "
                + " GROUP BY s.AppID) q1 "
                + " WHERE q1.Country = ? "
                + " ORDER BY Revenue LIMIT 1";

        // Executing the Query
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
          // Setting the param to Country
          stmt.setString(1, country);
          try (final ResultSet res = stmt.executeQuery()) {
            while (res.next()) {
              System.out.printf("App ID: %s %nApp Name: %s %nRevenue: %s ",
                  res.getInt("AppID"), res.getString("AppName"),
                  res.getString("Revenue"));
            }
          }
        }
      }

      // REPORT QUERY 3 (QUERY 13)
      // Find the Top 3 Most Watched Videos for the Most Profitable App in a certain Country
      // There is:
      // - 2 aggregate function COUNT and SUM
      // - 2 Ordering Fields
      // - Strong Motivation/Justification: Important information about viewership in the most profitable apps,
      //    this could be very useful to predict what new videos/shows can bring in the most viewership
      // - 1 Grouping
      // - 1 Subquery
      // - 3 tables JOINED
      else if (qd.queryType == QueryTypes.MostProfitableVideos) {

        System.out.printf("Available Countries: %n");
        final String options = ("SELECT DISTINCT Country FROM User");
        try (final PreparedStatement stmt6 = connection.prepareStatement(options)) {
          try (final ResultSet res6 = stmt6.executeQuery()) {
            while (res6.next()) {
              System.out.printf("COUNTRY: %s %n", res6.getString("Country"));
            }
          }
        }
        System.out.printf("Enter the Country: ");
        final String country = input.nextLine();

        final String sql =
            "SELECT COUNT(uw.VideoID) as WatchCount, v.Title as VideoName, a.Name as AppName"
                + " FROM (SELECT SUM(s.Cost) as Revenue, s.AppID as AppID"
                + " FROM Subscription s JOIN User u ON u.UserID = s.UserID "
                + " WHERE u.Country = ?"
                + " GROUP BY s.AppID "
                + " ORDER BY Revenue DESC LIMIT 1) q1"
                + " JOIN Video v ON v.AppID = q1.AppID"
                + " JOIN App a ON a.AppID = q1.AppID"
                + " JOIN UserVideoWatched uw ON v.VideoID = uw.VideoID"
                + " GROUP BY uw.VideoID "
                + " ORDER BY WatchCount DESC, VideoName LIMIT 3";

        // Executing the Query
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
          // Setting the param to Country
          stmt.setString(1, country);
          try (final ResultSet res = stmt.executeQuery()) {
            while (res.next()) {
              System.out.printf("APP: %s, VIDEO: %s, WATCH COUNT: %d %n",
                  res.getString("AppName"), res.getString("VideoName"),
                  res.getInt("WatchCount"));
            }
          }
        }
      }

      // REPORT QUERY 4 (QUERY 14)
      // Finding the Top 3 Most Watched Episodes for a certain Show.
      // There is:
      // - An aggregate function COUNT
      // - A Grouping
      // - A Subquery
      // - Strong Motivation/Justification: Important information about viewership of a show,
      //   this could be useful when gauging audience interest
      // - More than 3 tables JOINED
      // - 2 Ordering Fields
      else if (qd.queryType == QueryTypes.MostWatchedEpisodes) {

        System.out.printf("Available Shows: %n");
        final String options = ("SELECT ShowID, Title FROM Shows");
        try (final PreparedStatement stmt6 = connection.prepareStatement(options)) {
          try (final ResultSet res6 = stmt6.executeQuery()) {
            while (res6.next()) {
              System.out.printf("ID: %d Name: %s %n", res6.getInt("ShowID"),
                  res6.getString("Title"));
            }
          }
        }
        System.out.printf("Enter the ShowID: ");
        final Integer showId = input.nextInt();

        final String sql = "SELECT q1.showName as showName, v.Title as videoName,"
            + " q1.watchCount as watchCount"
            + " FROM ("
            + " SELECT sh.ShowID as ShowID, sh.Title AS showName, s.VideoID as VideoID,"
            + " COUNT(uw.VideoID) as watchCount"
            + " FROM UserVideoWatched uw JOIN Seasons s ON s.VideoID=uw.VideoID "
            + " JOIN Shows sh ON sh.ShowID = s.ShowID"
            + " GROUP BY uw.VideoID) q1 "
            + " JOIN Shows sh on sh.ShowID = q1.ShowID"
            + " JOIN Video v ON v.VideoID = q1.VideoID"
            + " WHERE sh.ShowID = ? "
            + " ORDER BY watchCount DESC, showName LIMIT 3 ";

        // Executing the Query
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
          // Setting the param to ShowID
          stmt.setInt(1, showId);
          try (final ResultSet res = stmt.executeQuery()) {
            while (res.next()) {
              System.out.printf("SHOW: %s, EPISODE: %s, VIEW COUNT: %d %n",
                  res.getString("showName"), res.getString("videoName"),
                  res.getInt("watchCount"));
            }
          }
        }
      }

      // REPORT QUERY 5 (QUERY 15)
      // Produce a Ranked List of Revenue by App for Mobile Platforms for a certain Country.
      // There is:
      // - An aggregate function SUM
      // - A Grouping
      // - A non-Aggregate Function ROUND
      // - Strong Motivation/Justification: Important information about profits for mobile users, could be 
      // useful for investment decisions
      // - 3 tables JOINED
      // - 2 Ordering Fields Revenue and AppName
      else if (qd.queryType == QueryTypes.MobileAppsRevenueRanked) {

        System.out.printf("Available Countries with Apps that Have Mobile Platforms: %n");
        final String options = ("SELECT DISTINCT u.Country AS Country"
            + " FROM User u JOIN Subscription s ON s.UserID = u.UserID"
            + " JOIN AppPlatform ap ON ap.AppID = s.AppID JOIN Platform p ON p.PlatformID = ap.PlatformID"
            + " WHERE P.Mobile = 1");
        try (final PreparedStatement stmt6 = connection.prepareStatement(options)) {
          try (final ResultSet res6 = stmt6.executeQuery()) {
            while (res6.next()) {
              System.out.printf("COUNTRY: %s %n", res6.getString("Country"));
            }
          }
        }
        System.out.printf("Enter the Country: ");
        final String country = input.nextLine();
        final String sql =
            "SELECT a.Name as AppName,  a.AppID as AppID, ROUND(SUM(S.Cost), 0) as Revenue"
                + " FROM Platform p JOIN AppPlatform ap ON p.PlatformID = ap.PlatformID JOIN App a ON a.AppID = ap.AppID "
                + " JOIN Subscription s ON s.AppID = a.AppID JOIN User u ON s.UserID = u.UserID"
                + " WHERE u.Country = ? AND p.Mobile=1"
                + " GROUP BY ap.AppID"
                + " ORDER BY Revenue DESC, AppName ";

        // Executing the Query
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
          // Setting the param to Country
          stmt.setString(1, country);
          try (final ResultSet res = stmt.executeQuery()) {
            while (res.next()) {
              System.out.printf("APP: %s, REVENUE: %d %n",
                  res.getString("AppName"), res.getInt("Revenue"));
            }
          }
        }
      }
    } catch (
        Exception e) {
      System.out.println(e);
    }
  }
}
