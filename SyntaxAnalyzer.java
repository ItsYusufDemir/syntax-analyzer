/* Authors: Eren Duyuk - 150120509
 *          Selin Aydın - 150120061
 *          Yusuf Demir - 150120032
 *
 * Date: 18.05.2023 10:12
 * 
 * Description: Implementing a syntax analyzer for a simple programming language.
 */


import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private static String TOKEN_FILE_PATH = "tokens.txt"; //Our input file is the output of LexicalAnalyzer
    private static String LEXEME_FILE_PATH = "lexemes.txt"; //Our lexemes file is the output of LexicalAnalyzer
    private static final String OUTPUT_FILE_PATH = "parse_tree.txt"; //Our output file will be the parse tree of the code
    private static FileReader F;
    private static FileReader F2;
    private static FileWriter file;
    private static BufferedReader bufferedReader;
    private static BufferedReader bufferedReader2;
    private static Terminal currentToken; //It keeps the current token
    private static String currentLexeme; //It keeps the current lexeme
    private static String location;  //It keeps the location of the current lexeme
    private static int depthLevel = 0; //It keeps the depth level of the tree to print it correctly


    //OPENING FILES
    static {
        try {  //try-catch block for catching file is not found error
            file = new FileWriter("output.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static{
        try {
            F = new FileReader(TOKEN_FILE_PATH);  //reading file by using FileReader;
        }
        catch (FileNotFoundException e){
            System.out.println("Token file couldn't opened!");
            System.exit(0);
        }
    }

    static{
        try {
            F2 = new FileReader(LEXEME_FILE_PATH);  //reading file by using FileReader;
        }
        catch (FileNotFoundException e){
            System.out.println("Lexeme file couldn't opened!");
            System.exit(0);
        }
    }



    public static void main(String args[]) throws IOException {

        /* We call the LexicalAnalyzer that we have written in previous project
         * It will return the tokens and lexemes as output files.
         */
        LexicalAnalyzer.main(null);


        bufferedReader = new BufferedReader(F);
        bufferedReader2 = new BufferedReader(F2);


        lex(); //We take the first token
        printNonterminal("<Program>"); //Printing the beginning of the tree
        Program(); //We start analyzing the syntax

        file.close(); //Closing the output file.
    }




    //This function reads the next token in the output file of LexicalAnalyzer
    public static void lex() throws IOException {

        String line = bufferedReader.readLine(); //Read the token
        currentLexeme = bufferedReader2.readLine(); //Read the lexeme

        try {
            currentToken = Terminal.valueOf(line.substring(0, line.indexOf(' ')));
            location = line.substring(line.indexOf(' ') + 1);
        }
        catch (Exception e) { //If the token is not in included in the grammar, give error.
            if (line != null){
                System.out.println("The token: " + line.substring(0, line.indexOf(' ')) + " is not known by this SyntaxAnalyzer!");
                System.exit(0);
             }
        }

    }



    //This function gives 1 error. You should give the currentToken as an input.
    public static void error(Terminal token){

        if(token == Terminal.RIGHTPAR)
            System.out.println("SYNTAX ERROR [" + location + "]: ')' is expected");
        else if(token == Terminal.LEFTPAR)
            System.out.println("SYNTAX ERROR [" + location + "]: '(' is expected");
        else
            System.out.println("SYNTAX ERROR [" + location + "]: '" + token.toString() + "' is expected");


        System.exit(0);
    }

    //This function gives many errors like: '(' or 'DEFINE' is expected
    public static void error(List<Terminal> tokens){

        System.out.print("SYNTAX ERROR [" + location + "]: ");

        for(int i = 0; i < tokens.size(); i++){

            if(tokens.get(i) == Terminal.RIGHTPAR)
                System.out.print("'('");
            else if(tokens.get(i) == Terminal.LEFTPAR)
                System.out.print("')'");
            else
                System.out.print("'" + tokens.get(i) + "'");

            if(i != tokens.size() - 1)
                System.out.print("or ");
        }

        System.out.println(" is expected");
        System.exit(0);
    }



    //<Program> --> Epsilon | <TopLevelForm> <Program>
    public static void Program() throws IOException {
        depthLevel++;

        if(currentToken == Terminal.LEFTPAR){
            printNonterminal("<TopLevelForm>");
            TopLevelForm();
            printNonterminal("<Program>");
            Program();
        }
        else{ //Else, do nothing. That means epsilon.
            printNonterminal("_"); //Print underscore to show epsilon
        }

        depthLevel--;
    }




    //<TopLevelForm> --> ( <SecondLevelForm> )
    public static void TopLevelForm() throws IOException {
        depthLevel++;

        if(currentToken == Terminal.LEFTPAR) {
            printTerminal(Terminal.LEFTPAR);
            lex();
            printNonterminal("<SecondLevelForm>");
            SecondLevelForm();
            if (currentToken == Terminal.RIGHTPAR) {
                printTerminal(Terminal.RIGHTPAR);
                lex();
            } else {
                error(Terminal.RIGHTPAR);
            }
        }
        else{
            error(Terminal.LEFTPAR);
         }

        depthLevel--;
    }


    //<SecondLevelForm> --> <Definition> | ( <FunCall> )
    public static void SecondLevelForm() throws IOException {
        depthLevel++;


        if(currentToken == Terminal.DEFINE){
            printNonterminal("<Definition>");
            Definition();
        }
        else if(currentToken == Terminal.LEFTPAR) {
            printTerminal(Terminal.LEFTPAR);
            lex();
            printNonterminal("<FuncCall>");
            FunCall();
            if (currentToken == Terminal.RIGHTPAR) {
                printTerminal(Terminal.RIGHTPAR);
                lex();
            } else {
                error(Terminal.RIGHTPAR);
            }
        }
        else{
            error(Arrays.asList(Terminal.DEFINE, Terminal.LEFTPAR));
        }


        depthLevel--;
    }


    //<Definition> --> DEFINE <DefinitionRight>
    public static void Definition() throws IOException {
        depthLevel++;

          if(currentToken == Terminal.DEFINE){
              printTerminal(Terminal.DEFINE);
              lex();
              printNonterminal("<DefinitionRight>");
              DefinitionRight();
          }
          else{
              error(Terminal.DEFINE);
          }


          depthLevel--;
    }


    //<DefinitionRight> --> IDENTIFIER <Expression> | ( IDENTIFIER <ArgList> ) <Statements>
    public static void DefinitionRight() throws IOException {
        depthLevel++;

          if(currentToken == Terminal.IDENTIFIER){
              printTerminal(Terminal.IDENTIFIER);
              lex();
              printNonterminal("<Expression>");
              Expression();
          }
          else if(currentToken == Terminal.LEFTPAR) {
              printTerminal(Terminal.LEFTPAR);
              lex();
              if (currentToken == Terminal.IDENTIFIER) {
                  printTerminal(Terminal.IDENTIFIER);
                  lex();
                  printNonterminal("<ArgList>");
                  ArgList();
                  if (currentToken == Terminal.RIGHTPAR) {
                      printTerminal(Terminal.RIGHTPAR);
                      lex();
                      printNonterminal("<Statements>");
                      Statements();
                  } else {
                      error(Terminal.RIGHTPAR);
                  }

              } else {
                  error(Terminal.IDENTIFIER);
              }
          }
          else {
              error( Arrays.asList(Terminal.IDENTIFIER, Terminal.LEFTPAR));
          }

          depthLevel--;
    }


    //<ArgList> --> Epsilon | IDENTIFIER <ArgList>
    public static void ArgList() throws IOException {
        depthLevel++;

        if(currentToken == Terminal.IDENTIFIER) {
            printTerminal(Terminal.IDENTIFIER);
            lex();
            printNonterminal("<ArgsList>");
            ArgList();
        }
        else{ //Else, do nothing. That means epsilon
            printNonterminal("_");
        }

        depthLevel--;
    }


    //<Statements> --> <Expression> | <Definition> <Statements>
    public static void Statements() throws IOException {
        depthLevel++;

        if (currentToken == Terminal.IDENTIFIER || currentToken == Terminal.NUMBER || currentToken == Terminal.CHAR
                || currentToken == Terminal.BOOLEAN || currentToken == Terminal.STRING || currentToken == Terminal.LEFTPAR) {
            printNonterminal("<Expression>");
            Expression();
        } else if (currentToken == Terminal.DEFINE) {
            printNonterminal("<Definition>");
            Definition();
            printNonterminal("<Statements>");
            Statements();
        } else {
            error(Arrays.asList(Terminal.IDENTIFIER, Terminal.NUMBER, Terminal.CHAR, Terminal.BOOLEAN, Terminal.STRING,
                    Terminal.LEFTPAR, Terminal.DEFINE));
        }

        depthLevel--;
    }


    // <Expressions> --> Epsilon | <Expression> <Expressions>
    public static void Expressions() throws IOException {
        depthLevel++;

        if(currentToken == Terminal.IDENTIFIER || currentToken == Terminal.NUMBER || currentToken == Terminal.CHAR ||
                currentToken == Terminal.BOOLEAN || currentToken == Terminal.STRING || currentToken == Terminal.LEFTPAR){
            printNonterminal("<Expression>");
            Expression();
            printNonterminal("<Expressions>");
            Expressions();
        }
        else{
            printNonterminal("__");
        }

        depthLevel--;
    }


    //<Expression> --> IDENTIFIER | NUMBER | CHAR | BOOLEAN | STRING | ( <Expr> )
    public static void Expression() throws IOException {
        depthLevel++;

        if(currentToken == Terminal.IDENTIFIER || currentToken == Terminal.NUMBER || currentToken == Terminal.CHAR ||
                currentToken ==  Terminal.BOOLEAN || currentToken == Terminal.STRING){
            printTerminal(currentToken);
            lex();
        } else if (currentToken == Terminal.LEFTPAR) {
            printTerminal(Terminal.LEFTPAR);
            lex();
            printNonterminal("<Expr>");
            Expr();
            if(currentToken == Terminal.RIGHTPAR){
                printTerminal(Terminal.RIGHTPAR);
                lex();
            }
            else{
                error(Terminal.RIGHTPAR);
            }
        }
        else{
            error(Arrays.asList(Terminal.IDENTIFIER, Terminal.NUMBER, Terminal.CHAR, Terminal.BOOLEAN, Terminal.STRING,
                    Terminal.LEFTPAR));
        }

        depthLevel--;
    }


    //<Expr> --> <LetExpression> | <CondExpression> | <IfExpression> | <BeginExpression> | <FunCall>
    public static void Expr() throws IOException {
        depthLevel++;

        if (currentToken == Terminal.LET) {
            printNonterminal("<LetExpression>");
            LetExpression();
        } else if (currentToken == Terminal.COND) {
            printNonterminal("<CondExpression>");
            CondExpression();
        } else if (currentToken == Terminal.IF) {
            printNonterminal("<IfExpression>");
            IfExpression();
        } else if (currentToken == Terminal.BEGIN) {
            printNonterminal("<BeginExpression>");
            BeginExpression();
        } else if (currentToken == Terminal.IDENTIFIER) {
            printNonterminal("<FuncCall>");
            FunCall();
        }
        else{
            error(Arrays.asList(Terminal.LET, Terminal.COND, Terminal.IF, Terminal.BEGIN, Terminal.IDENTIFIER));
        }


        depthLevel--;
    }


    //<VarDef> --> Epsilon | <VarDefs>
    public static void VarDef() throws IOException {
        depthLevel++;

        if(currentToken == Terminal.LEFTPAR) {
            printNonterminal("<VarDefs>");
            VarDefs();
        }
        else{
            printNonterminal("__");
        }

        depthLevel--;
    }


    //<CondBranch> --> Epsilon | ( <Expression> <Statements> )
    public static void CondBranch() throws IOException {
        depthLevel++;

        if(currentToken == Terminal.LEFTPAR){
            printTerminal(currentToken);
            lex();
            printNonterminal("<Expression>");
            Expression();
            printNonterminal("<Statements>");
            Statements();

            if(currentToken == Terminal.RIGHTPAR) {
                printTerminal(currentToken);
                lex();
            }
            else
                error(Terminal.RIGHTPAR); //Give an error like: ')' is expected.
        }

        depthLevel--;
    }


    //<EndExpression> --> Epsilon | <Expression>
    public static void EndExpression() throws IOException {
        depthLevel++;

        if(currentToken == Terminal.IDENTIFIER || currentToken == Terminal.NUMBER || currentToken == Terminal.CHAR ||
                currentToken == Terminal.BOOLEAN || currentToken == Terminal.STRING || currentToken == Terminal.LEFTPAR) {
            printNonterminal("<Expression>");
            Expression();
        }
        else{
            printNonterminal("__");
        }

        depthLevel--;
    }


    //<FunCall> --> IDENTIFIER <Expressions>
    public static void FunCall() throws IOException {
        depthLevel++;

        if(currentToken == Terminal.IDENTIFIER) {
            printTerminal(currentToken);
            lex();
            printNonterminal("<Expressions>");
            Expressions();
        } else {
            error(Terminal.IDENTIFIER);
        }

        depthLevel--;
    }


    //<LetExpression> --> LET <LetExpr>
    public static void LetExpression() throws IOException {
        depthLevel++;

        if(currentToken == Terminal.LET) {
            printTerminal(currentToken);
            lex();
            printNonterminal("<LetExpr>");
            LetExpr();
        } else {
            error(Terminal.LET);
        }

        depthLevel--;
    }

    //<LetExpression> --> ( <VarDefs> ) <Statements> | IDENTIFIER ( <VarDefs> ) <Statements>
    public static void LetExpr() throws IOException {
        depthLevel++;

        if (currentToken == Terminal.LEFTPAR) {
            printTerminal(currentToken);
            lex();
            printNonterminal("<VarDefs>");
            VarDefs();
            if (currentToken == Terminal.RIGHTPAR) {
                printTerminal(currentToken);
                lex();
                printNonterminal("<Statements>");
                Statements();
            } else {
                error(Terminal.RIGHTPAR); //Give an error like: ')' is expected.
            }
        } else if (currentToken == Terminal.IDENTIFIER) {
            printTerminal(currentToken);
            lex();
            if (currentToken == Terminal.LEFTPAR) {
                printTerminal(currentToken);
                lex();
                printNonterminal("<VarDefs>");
                VarDefs();
                if (currentToken == Terminal.RIGHTPAR) {
                    printTerminal(currentToken);
                    lex();
                    printNonterminal("<Statements>");
                    Statements();
                } else {
                    error(Terminal.RIGHTPAR); //Give an error like: ')' is expected.
                }
            } else {
                error(Terminal.LEFTPAR);
            }
        } else {
            error(Arrays.asList(Terminal.LEFTPAR, Terminal.IDENTIFIER));
        }

        depthLevel--;
    }


    //<VarDefs> -->( IDENTIFIER <Expression> ) <VarDef>
    public static void VarDefs() throws IOException {
        depthLevel++;

        if(currentToken == Terminal.LEFTPAR) {
            printTerminal(currentToken);
            lex();
            if(currentToken == Terminal.IDENTIFIER) {
                printTerminal(currentToken);
                lex();
                printNonterminal("<Expression>");
                Expression();
                if(currentToken == Terminal.RIGHTPAR) {
                    printTerminal(currentToken);
                    lex();
                    printNonterminal("<VarDef>");
                    VarDef();
                } else {
                    error(Terminal.RIGHTPAR);
                }
            } else {
                error(Terminal.IDENTIFIER);
            }
        } else {
            error(Terminal.LEFTPAR);
        }

        depthLevel--;
    }


    //<CondExpression> --> COND <CondBranches>
    public static void CondExpression() throws IOException {
        depthLevel++;

        if(currentToken == Terminal.COND) {
            printTerminal(currentToken);
            lex();
            printNonterminal("<CondBranches>");
            CondBranches();
        } else {
            error(Terminal.COND);
        }

        depthLevel--;
    }


    //<CondBranches> --> ( <Expression> <Statements> ) <CondBranches>
    public static void CondBranches() throws IOException {
        depthLevel++;

        if(currentToken == Terminal.LEFTPAR) {
            printTerminal(currentToken);
            lex();
            printNonterminal("<Expression>");
            Expression();
            printNonterminal("<Statements>");
            Statements();
            if(currentToken == Terminal.RIGHTPAR) {
                printTerminal(currentToken);
                lex();
                printNonterminal("<CondBranches>");
                CondBranches();
            } else {
                error(Terminal.RIGHTPAR);
            }
        } else {
            error(Terminal.LEFTPAR);
        }

        depthLevel--;
    }


    //<IfExpression> --> IF <Expression> <Expression> <EndExpression>
    public static void IfExpression() throws IOException {
        depthLevel++;

        if(currentToken == Terminal.IF) {
            printTerminal(currentToken);
            lex();
            printNonterminal("<Expression>");
            Expression();
            printNonterminal("<Expression>");
            Expression();
            printNonterminal("<EndExpressions>");
            EndExpression();
        } else {
            error(Terminal.IF);
        }

        depthLevel--;
    }


    //<BeginExpression> --> BEGIN <Statements>
    public static void BeginExpression() throws IOException {
        depthLevel++;

        if(currentToken == Terminal.BEGIN) {
            printTerminal(currentToken);
            lex();
            printNonterminal("<Statements>");
            Statements();
        } else {
            error(Terminal.BEGIN);
        }

        depthLevel--;
    }


    public static void printNonterminal(String nonterminal) throws IOException {

        for(int i = 0; i < depthLevel; i++) { //Put appropriate number of tabs for current depthLevel
            System.out.print(" ");
             file.write(" ");
         }

        System.out.println(nonterminal);
        file.write(nonterminal + "\n");
    }


    public static void printTerminal(Terminal terminal) throws IOException {

        for(int i = 0; i < depthLevel; i++) { //Put appropriate number of tabs for current depthLevel
            System.out.print(" ");
            file.write(" ");
        }

        System.out.println(terminal.toString() + " (" + currentLexeme + ")");
        file.write(terminal.toString() + " (" + currentLexeme + ")\n");
    }

}
