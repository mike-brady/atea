import atea.Atea;
import atea.Abbreviation;
import java.sql.*;
import java.util.ArrayList;

public class Test {
  public static void main(String[] args) {
    String input = "Python is considered to be in the first place in the list of all AI development languages due to the simplicity. The syntaxes belonging to python are very simple and can be easily learnt. Therefore, many AI algorithms can be easily implemented in it. Python takes short development time in comparison to other languages like Java, C++ or Ruby. Python supports object oriented, functional as well as procedure oriented styles of programming. There are plenty of libraries in python, which make our tasks easier. For example: Numpy is a library for python that helps us to solve many scientific computations. Also, we have Pybrain, which is for using machine learning in Python. R is one of the most effective language and environment for analyzing and manipulating the data for statistical purposes. Using R, we can easily produce well-designed publication-quality plot, including mathematical symbols and formulae where needed. Apart from being a general purpose language, R has numerous of packages like RODBC, Gmodels, Class and Tm which are used in the field of machine learning. These packages make the implementation of machine learning algorithms easy, for cracking the business associated problems.";

    try (
         Connection conn = DriverManager.getConnection(
               System.getenv("DB_URL"),
               System.getenv("DB_USER"),
               System.getenv("DB_PASS")
          );
      ) {
         Atea atea = new Atea(conn);
         System.out.println(atea.expand(input));
      } catch(SQLException ex) {
         ex.printStackTrace();
      }
  }
}
