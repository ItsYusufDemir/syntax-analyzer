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



    //<Program> --> Epsilon | <TopLevelForm> <Program>
    public static void Program() throws IOException {


        if(currentToken == Terminal.LEFTPAR){
            lex();
            TopLevelForm();
            Program();
        }

        //Else, do nothing. That means epsilon.
    }




    //<TopLevelForm> --> ( <SecondLevelForm> )
    public static void TopLevelForm() throws IOException {
               if(currentToken == Terminal.LEFTPAR) {
                   lex();
                   SecondLevelForm();
                   if (currentToken == Terminal.RIGHTPAR) {
                       lex();
                   } else {
                       error(Terminal.RIGHTPAR);
                   }
               }
               else{
                 error(Terminal.LEFTPAR);
    }
    }


    //<SecondLevelForm> --> <Definition> | ( <FunCall> )
    public static void SecondLevelForm() throws IOException {
        if(currentToken == Terminal.DEFINE){
            Definition();
        }
        else if(currentToken == Terminal.LEFTPAR) {
            lex();
            FunCall();
            if (currentToken == Terminal.RIGHTPAR) {
                lex();
            } else {
                error(Terminal.RIGHTPAR);
            }
        }
            else{
                error(Terminal.LEFTPAR);
            }
        }

    //<Definition> --> DEFINE <DefinitionRight>
    public static void Definition() throws IOException {
          if(currentToken == Terminal.DEFINE){
              lex();
              DefinitionRight();
          }
          else{
              error(Terminal.DEFINE);
          }
    }

    //<DefinitionRight> --> IDENTIFIER <Expression> | ( IDENTIFIER <ArgList> ) <Statements>
    public static void DefinitionRight() throws IOException {
          if(currentToken == Terminal.IDENTIFIER){
              lex();
              Expression();
          }
          else if(currentToken == Terminal.LEFTPAR) {
              lex();
              if (currentToken == Terminal.IDENTIFIER) {
                  lex();
                  ArgList();
                  if (currentToken == Terminal.RIGHTPAR) {
                      lex();
                      Statements();
                  } else {
                      error(Terminal.RIGHTPAR);
                  }

              } else {
                  error(Terminal.IDENTIFIER);
              }
          }
              else {
                  error(Terminal.IDENTIFIER);
              }
          }

    //<ArgList> --> Epsilon | IDENTIFIER <ArgList>
    public static void ArgList() throws IOException {

        if(currentToken == Terminal.IDENTIFIER) {
            lex();
            ArgList();
        }

        //Else, do nothing. That means epsilon.
    }

    //<Statements> --> <Expression> | <Definition> <Statements>
    public static void Statements() throws IOException {
         if(currentToken == Terminal.IDENTIFIER || currentToken == Terminal.NUMBER || currentToken == Terminal.CHAR || currentToken == Terminal.BOOLEAN || currentToken == Terminal.STRING || currentToken == Terminal.LEFTPAR){
             Expression();
         }
         else if(currentToken == Terminal.DEFINE){
             Definition();
             lex();
             Statements();
         }
    }


    // <Expressions> --> Epsilon | <Expression> <Expressions>
    public static void Expressions() throws IOException {

        if(currentToken == Terminal.IDENTIFIER || currentToken == Terminal.NUMBER || currentToken == Terminal.CHAR ||
                currentToken == Terminal.BOOLEAN || currentToken == Terminal.STRING || currentToken == Terminal.LEFTPAR){
            Expression();
            Expressions();
        }

    }

    //<Expression> --> IDENTIFIER | NUMBER | CHAR | BOOLEAN | STRING | ( <Expr> )
    public static void Expression() throws IOException {

        if(currentToken == Terminal.IDENTIFIER | currentToken == Terminal.NUMBER | currentToken == Terminal.CHAR | currentToken ==  Terminal.BOOLEAN | currentToken == Terminal.STRING){
            lex();

        } else if (currentToken == Terminal.LEFTPAR) {
            lex();
            Expr();
            if(currentToken == Terminal.RIGHTPAR){
                lex();
            }
            else{
                error(Terminal.RIGHTPAR);
            }
        }
        else{
            error(Terminal.IDENTIFIER);
        }

    }
//<Expr> --> <LetExpression> | <CondExpression> | <IfExpression> | <BeginExpression> | <FunCall>
    public static void Expr() throws IOException {
        if (currentToken == Terminal.LET) {
            LetExpression();
        } else if (currentToken == Terminal.COND) {
            CondExpression();
        } else if (currentToken == Terminal.IF) {
            IfExpression();
        } else if (currentToken == Terminal.BEGIN) {
            BeginExpression();
        } else if (currentToken == Terminal.IDENTIFIER) {
            FunCall();
        }
    }

    //<VarDef> --> Epsilon | <VarDefs>
    public static void VarDef() throws IOException {
        if(currentToken == Terminal.LEFTPAR)
            VarDefs();
    }


    //<CondBranch> --> Epsilon | ( <Expression> <Statements> )
    public static void CondBranch() throws IOException {

        if(currentToken == Terminal.LEFTPAR){
            lex();
            Expressions();
            Statements();

            if(currentToken == Terminal.RIGHTPAR)
                lex();
            else
                error(Terminal.RIGHTPAR); //Give an error like: ')' is expected.
        }

    }


    //<EndExpression> --> Epsilon | <Expression>
    public static void EndExpression() throws IOException {

        if(currentToken == Terminal.IDENTIFIER || currentToken == Terminal.NUMBER || currentToken == Terminal.CHAR ||
                currentToken == Terminal.BOOLEAN || currentToken == Terminal.STRING || currentToken == Terminal.LEFTPAR) {
            Expression();
        }
    }

    //<FunCall> --> IDENTIFIER <Expression>
    public static void FunCall() throws IOException {

        if(currentToken == Terminal.IDENTIFIER) {
            lex();
            Expressions();
        } else {
            error(Terminal.IDENTIFIER);
        }
    }

    //<LetExpression> --> LET <LetExpr>
    public static void LetExpression() throws IOException {

        if(currentToken == Terminal.LET) {
            lex();
            LetExpr();
        } else {
            error(Terminal.LET);
        }
    }

    //<LetExpression> --> ( <VarDefs> ) <Statements> | IDENTIFIER ( <VarDefs> ) <Statements>
    public static void LetExpr() throws IOException {
        if (currentToken == Terminal.LEFTPAR) {
            lex();
            varDefs();
            if (currentToken == Terminal.RIGHTPAR) {
                lex();
            } else {
                error(Terminal.RIGHTPAR); //Give an error like: ')' is expected.
            }
        } else if (currentToken == Terminal.IDENTIFIER) {
            if (currentToken == Terminal.LEFTPAR) {
                lex();
                varDefs();
                if (currentToken == Terminal.RIGHTPAR) {
                    lex();
                } else {
                    error(Terminal.RIGHTPAR); //Give an error like: ')' is expected.
                }
            } else {
                error(Terminal.LEFTPAR);
            }
        } else {
            error(Terminal.LEFTPAR);
        }
    }

    //<VarDefs> -->( IDENTIFIER <Expression> ) <VarDef>
    public static void VarDefs() throws IOException {
        if(currentToken == Terminal.LEFTPAR) {
            lex();
            if(currentToken == Terminal.IDENTIFIER) {
                lex();
                Expressions();
                if(currentToken == Terminal.RIGHTPAR) {
                    lex();
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
    }

    //<CondExpression> --> COND <CondBranches>
    public static void CondExpression() throws IOException {
        if(currentToken == Terminal.COND) {
            lex();
            CondBranches();
        } else {
            error(Terminal.COND);
        }
    }

    //<CondBranches> --> ( <Expression> <Statements> ) <CondBranches>
    public static void CondBranches() throws IOException {
        if(currentToken == Terminal.LEFTPAR) {
            lex();
            Expressions();
            Statements();
            if(currentToken == Terminal.RIGHTPAR) {
                lex();
                CondBranches();
            } else {
                error(Terminal.RIGHTPAR);
            }
        } else {
            error(Terminal.LEFTPAR);
        }
    }

    //<IfExpression> --> IF <Expression> <Expression> <EndExpression>
    public static void IfExpression() throws IOException {
        if(currentToken == Terminal.IF) {
            lex();
            Expressions();
            Expressions();
            EndExpression();
        } else {
            error(Terminal.IF);
        }
    }

    //<BeginExpression> --> BEGIN <Statements>
    public static void BeginExpression() throws IOException {
        if(currentToken == Terminal.BEGIN) {
            lex();
            Statements();
        } else {
            error(Terminal.BEGIN);
        }
    }

}
