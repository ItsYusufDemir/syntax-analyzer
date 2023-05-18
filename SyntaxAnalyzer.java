/* Authors: Eren Duyuk - 150120509
 *          Selin Aydın - 150120061
 *          Yusuf Demir - 150120032
 *
 * Date: 18.05.2023 10:12
 * 
 * Description: Implementing a syntax analyzer for a simple programming language.
 */


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SyntaxAnalyzer {

    //ENUMERATORS
    enum Terminal{ //We have the terminals here, this will make easy to read and write the code
        DEFINE,
        IDENTIFIER,
        NUMBER,
        CHAR,
        BOOLEAN,
        STRING,
        LET,
        COND,
        IF,
        BEGIN,
        LEFTPAR,
        RIGHTPAR
    }

    //GLOBAL VARIABLES
    private static String INPUT_FILE_PATH = "output.txt"; //Our input file is the output of LexicalAnalyzer
    private static final String OUTPUT_FILE_PATH = "parse_tree.txt"; //Our ouput file will be the parse tree of the code
    private static FileReader F;
    private static BufferedReader bufferedReader;
    private static Terminal currentToken;
    private static String location;


    public static void main(String args[]) throws IOException {

        LexicalAnalyzer.main(null); //We call the LexicalAnaylzer that we have written in previous project

        try {
            F = new FileReader(INPUT_FILE_PATH);  //reading file by using FileReader;
        }
        catch (FileNotFoundException e){
            System.out.println("Token file couldn't opened!");
            System.exit(0);
        }


        bufferedReader = new BufferedReader(F);
        lex(); //We take the first token

        Program(); //We start analyzing the syntax

    }




    //This function reads the next token in the output file of LexicalAnaylzer
    public static void lex() throws IOException {
        String line = bufferedReader.readLine();

        try {
            currentToken = Terminal.valueOf(line.substring(0, line.indexOf(' ')));
            location = line.substring(line.indexOf(' ') + 1);
        }
        catch (Exception e){ //If the token is different that we defined above, give error.
            System.out.println("The token: " + line.substring(0, line.indexOf(' ')) + " is not known by this SyntaxAnalyzer!" );
            System.exit(0);
        }

    }



    //Give the error. You should give the currentToken as as input.
    public static void error(Terminal token){

        if(token == Terminal.RIGHTPAR)
            System.out.println("SYNTAX ERROR [" + location + "]: ')' is expected");
        else if(token == Terminal.LEFTPAR)
            System.out.println("SYNTAX ERROR [" + location + "]: '(' is expected");
        else
            System.out.println("SYNTAX ERROR [" + location + "]: '" + token.toString() + "' is expected");

    }



    //<Program> --> <TopLevelForm> <Program>
    public static void Program() throws IOException {


        if(currentToken == Terminal.LEFTPAR){
            lex();
            TopLevelForm();
            Program();
        }

        //Else, do nothing. That means epsilon.
    }




    //<TopLevelForm> --> ( <SecondLevelForm> )
    public static void TopLevelForm(){



    }



}
