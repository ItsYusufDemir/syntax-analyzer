import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;


/* Authors: Eren Duyuk - 150120509
 *          Selin Aydın - 150120061
 *          Yusuf Demir - 150120032
 *
 * Date: 8.04.2023 10:42
 *
 * Description: Making a simple lexical analyzer.
 */


public class LexicalAnalyzer {

    //  ---GLOBAL VARIABLES---
    static char previousChar = ' '; //previous char
    static char currentChar = ' ';  //current char
    static String currentLexeme = ""; //keeps current token
    static int line = 1; //keeps line index of the token
    static int column = 0; //keeps column index of the token
    static int tokenStartingColumn = 0;  //keeps starting column index of the token
    static FileReader F;
    static FileWriter file; //defining global variable file that keeps the output
    static FileWriter file2; //defining global variable file that keeps the lexemes as output

    static {
        try {  //try-catch block for catching file is not found error
            file = new FileWriter("tokens.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try {  //try-catch block for catching file is not found error
            file2 = new FileWriter("lexemes.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }




    public static void main(String[]args) throws IOException{


        //TAKING THE FILE NAME
        Scanner input = new Scanner(System.in);
        System.out.println("Enter the file name (ex. Correct_Input.txt) : ");
        String fileName = input.next(); //Taking the file name

        try {
            F = new FileReader(fileName);  //reading file by using FileReader;
        }
        catch (FileNotFoundException e){
            System.out.println("File couldn't found! Please check your file and file name.");
            System.exit(0);
        }



        // THIS IS THE BIG WHILE LOOP. EVERYTHING IS READ HERE
        while(currentChar != '\uffff') {  //.read() method that we use inside the lex() method returns -1 while
            //there is no more character in the input file
            // -1 is considered as \uffff  when it is type cast to char

            if (currentChar == ' ' || currentChar == '\t' || currentChar == '\n' || currentChar == '\r'){
                //If the current char is space, tab or new line or skip then we call lex() method which will read the file char by char
                lex(F);
                continue;
            }


            //BRACKET READING
            if(isParenthesis(currentChar)) {   //If the current character is a parenthesis, enter this if statement

                tokenStartingColumn = column;  //Record its starting column for potential usage
                currentLexeme += currentChar;  //Record the lexeme

                if (currentChar == '(') {
                    printToken("LEFTPAR");
                } else if (currentChar == ')') {
                    printToken("RIGHTPAR");
                } else if (currentChar == '[') {
                    printToken("LEFTSQUAREB");
                } else if (currentChar == ']') {
                    printToken("RIGHTSQUAREB");
                } else if (currentChar == '{') {
                    printToken("LEFTCURLYB");
                } else if (currentChar == '}') {
                    printToken("RIGHTCURLYB");
                }

                lex(F);
                currentLexeme = "";
                continue;
            }


            /* CHAR READING
             *
             * Char can be given like: 'c'   'd'   '\''   '\n' etc.
             */
            if(currentChar == '\''){ //if taken character is single quote which means data type is char

                tokenStartingColumn = column; //Record its starting column for potential usage
                boolean haveError = false;
                currentLexeme += currentChar; //Record the lexeme
                lex(F); //read next character


                while (currentChar != ' ' && currentChar != '\uffff' && currentChar != '\n' && currentChar != '\r' && !isParenthesis(currentChar)){ //Read until space character
                    currentLexeme += currentChar;
                    lex(F);

                }

                if(currentLexeme.length() == 3) {  //Case: 'c'
                    if(currentLexeme.charAt(1) == ' ' || currentLexeme.charAt(1) == '\''){
                        haveError = true; //we have an error if it is ' ' or '''
                    }
                }
                else if(currentLexeme.length() == 4){  //Case: '\''   or   '\n'   or '\r'
                    if(currentLexeme.charAt(1) != '\\')
                        haveError = true;

                    if(!(currentLexeme.charAt(2) == 'n' || currentLexeme.charAt(2) == 'r'))
                        haveError = true; //we have an error if n or r don't come after \
                }
                else //If the length is not 3 or 4, give error
                    haveError = true;

                if(haveError) //print error message
                    printError(currentLexeme);
                else //print token as CHAR
                    printToken("CHAR");

                currentLexeme = ""; //Reset recording
                continue;
            }




            /*STRING READING
            * Not correct string examples: "ab\\\cd"
            *                              "abc"def"
            *                              "abc"\\e"
            */
            if(currentChar == '\"') {  //if is starts with double quote
                tokenStartingColumn = column; //Record its starting column for potential usage
                boolean haveError = false; //set error to false
                currentLexeme += currentChar; //Record the lexeme
                lex(F); //read next char by using lex() method

                while ( (currentChar != '\uFFFF' && currentChar == '\"' && previousChar== '\\') || (currentChar != '\"' &&  currentChar != '\uFFFF')){
                    //if currentChar is double quote,previousChar is backslash and it is not end of the file continue while loop
                    //or if currentChar is something but double quote and it is not end of the file continue while loop
                    currentLexeme += currentChar; //continue recording token
                    lex(F); //read next char
                }
                if(currentChar ==  '\"') { //if the last char is double quote
                    currentLexeme = currentLexeme + '\"'; //add double quote to token's end
                }else {  //if token is not ending with a double quote, set error to true
                    haveError = true;
                }

                if (currentLexeme.charAt(currentLexeme.length() - 1) == '\"') { //if the last char is double quote continue this block
                    for (int i = 1; i < currentLexeme.length() - 1; i++) {
                        if (currentLexeme.charAt(i) == '\\' ) { //if current char is backslash character
                            if (currentLexeme.charAt(i + 1) == '\"' && i != currentLexeme.length()-2) { //if next character is a double quote, set error to false
                                haveError = false;
                            } else if (currentLexeme.charAt(i + 1) == '\\'  ) { //if next character also a backslash, set error to false and increase i by one not to check second matching backslash
                                haveError = false;
                                i++;
                            }
                            else if (currentLexeme.charAt(i+1) == 'n' || currentLexeme.charAt(i+1) == 't' || currentLexeme.charAt(i+1) == 'r') {
                                //if the next character is \n \t or \r , set error to false
                                haveError = false;
                            }
                            else { //otherwise set error to true
                                haveError = true;
                            }
                        }
                    }
                }

                if (haveError == true) { //if it has error, print token
                    printError(currentLexeme);
                } else {
                    printToken("STRING"); //if it hasnot error, print STRING
                }
                currentLexeme = ""; //set current token to blank
                lex(F); //read next token since we add double quote abovee
                continue; //continue the code
            }



            /* COMMENT READING
             * Comments start with tilde(~) and continue to the end of the line
             * Scanner must ignore comments
             */
            if(currentChar == '~'){ //if currentChar is tilde
                boolean haveError = false;
                currentLexeme += currentChar; //Record the lexeme
                lex(F); //read next character
                while(currentChar != '\uffff' && currentChar != '\n' && currentChar != '\r') { //if it is not end of the line
                    currentLexeme += currentChar; //continue recording
                    lex(F); //read next char
                }
                currentLexeme = ""; //Reset recording
                continue;
            }




            /* IDENTIFIER AND KEYWORDS
             *
             * First read the lexeme, then check if it is a keyword or not. If not, then print IDENTIFIER.
             * If it is a keyword, then print its name.
             */
            if(currentChar == '!' || currentChar == '*' || currentChar == '/' || currentChar == ':' || currentChar == '<'
                    || currentChar == '=' || currentChar == '>' || currentChar == '!' || currentChar == '?' || isLetter(currentChar) ){
                //current char can be one of above

                tokenStartingColumn = column; //Record its starting column for potential usage
                boolean haveError = false;  //initializing haveError
                currentLexeme += currentChar; //Start recording the lexeme
                lex(F); //read next char inside lex() method



                while(currentChar != ' ' && currentChar != '\uffff' && currentChar != '\n' && currentChar != '\r' && !isParenthesis(currentChar)
                        && currentChar != '"' && currentChar != '\'' && currentChar != '~' ){

                    if(isLetter(currentChar) || isDecDigit(currentChar) || currentChar == '.' || currentChar == '+' || currentChar == '-') {
                        currentLexeme += currentChar; //Continue recording
                        lex(F);
                    }
                    else {
                        haveError = true;
                        //lex(F);
                    }


                }

                if(haveError) //If an error is occurred, print error message
                    printError(currentLexeme);
                else{

                    if(isKeyword(currentLexeme)){  //checking lexeme is a keyword or not

                        if(isBoolean(currentLexeme)) //checking if  it is a boolean expression(true or false) which is
                            // also a keyword, if it is: print BOOLEAN
                            printToken("BOOLEAN");
                        else //otherwise it is a regular keyword, print token in uppercase
                            printToken(findUpperCase(currentLexeme)); //

                    }
                    else{  //if it is none of above, it is an identifier
                        printToken("IDENTIFIER");
                    }

                }

                currentLexeme = ""; //Reset recording
                continue;
            }


            //NUMBER READING
            else if(isDecDigit(currentChar) || currentChar == '+' || currentChar == '-' || currentChar == '.') {
                //checking weather currentChar starts with a digit, '+', '-' or '.' signs
                tokenStartingColumn = column;

                boolean isIdentifier = false; //initializing isIdentifier and haveError as false
                boolean haveError = false;

                currentLexeme += currentChar; //starts recording the token
                lex(F); //reads next char

                if(!isParenthesis(currentChar)){
                    currentLexeme += currentChar;
                }

                //checking currentToken consists of only +,-,., or a digit such as 0,1,2,3,4,5,6,7,8,9
                if(currentChar == ' ' || currentChar == '\t' || currentChar == '\n' || currentChar == '\r' || currentChar == '\uffff' || isParenthesis(currentChar)) {
                    //checking previous char if it consists of only one of the +,- or . signs; then it cannot be a number
                    //it can only be identifier
                    if (previousChar == '.' || previousChar == '+' || previousChar == '-') {
                        isIdentifier = true;
                    }
                }

                //Checks for two consecutive transaction operators, if it is set error to true
                if((previousChar == '+' && currentChar == '+') || (previousChar == '-' && currentChar == '+') ||
                        (previousChar == '-' && currentChar == '-') || (previousChar == '+' && currentChar == '-') ) {
                    haveError = true;
                }



                //If number is binary, enters this block: 0b...
                if(previousChar == '0' && currentChar == 'b') {
                    lex(F); //reads next char //??????
                    currentLexeme += currentChar; //continue recording token
                    while(true) {
                        lex(F); //continue reading next char
                        if(currentChar == ' ' || currentChar == '\t' || currentChar == '\n' || currentChar == '\r' || currentChar == '\uffff' || isParenthesis(currentChar)) {
                            break; //stop the code if it is blank or new line
                        } else if(!isBinaryDigit(currentChar)) { //if it is not a binary digit there should be an error
                            currentLexeme += currentChar; //keep recording token since we print it
                            haveError = true; //set error to true
                        } else if(isBinaryDigit(currentChar)) { //if it is a binary digit there is no error
                            currentLexeme += currentChar; //keep recording token
                        }
                    }
                }

                //If number is hexadecimal enters this block: 0x...
                else if(previousChar == '0' && currentChar == 'x') {
                    lex(F);
                    currentLexeme += currentChar; //continue recording token
                    while(true) {
                        lex(F); //reads next char
                        if(currentChar == ' ' || currentChar == '\t' || currentChar == '\n' || currentChar == '\r' || currentChar == '\uffff' || isParenthesis(currentChar)) {
                            break; //stop the code if it is blank or new line
                        } else if(!isHexDigit(currentChar)) { //if it is not a hexadecimal digit there should be an error
                            currentLexeme += currentChar; //keep recording token since we print it
                            haveError = true; //set error to true
                        } else if(isHexDigit(currentChar)) { //if it is a hexadecimal digit
                            currentLexeme += currentChar; //keep recording token
                        }
                    }
                }

                //If number is decimal or float enters this block
                else if (!(currentChar == ' ' || currentChar == '\t' || currentChar == '\n' || currentChar == '\r' || currentChar == '\uffff' || isParenthesis(currentChar))){
                    while(true && !isParenthesis(currentChar) && !isIdentifier) {
                        if(!(isDecDigit(currentChar) || currentChar == '+' || currentChar == '-' || currentChar == '.' || currentChar == 'e' || currentChar == 'E')) {
                            haveError = true;
                            while(true) {
                                lex(F); //reads next char

                                if(currentChar == ' ' || currentChar == '\t' || currentChar == '\n' || currentChar == '\r' || currentChar == '\uffff' || isParenthesis(currentChar)) {
                                    break; //stop the code if it is blank or new line
                                }
                                currentLexeme += currentChar;
                            }
                        }
                        lex(F); //reads next char
                        //Checks for two consecutive transaction operators
                        if((previousChar == '+' && currentChar == '+') || (previousChar == '-' && currentChar == '+') ||
                                (previousChar == '-' && currentChar == '-') || (previousChar == '+' && currentChar == '-')) {
                            haveError = true;
                        }

                        //Checks if the token has reached the end
                        if(currentChar == ' ' || currentChar == '\t' || currentChar == '\n' || currentChar == '\r' || currentChar == '\uffff' || isParenthesis(currentChar)) {
                            //Checks for errors in e and transaction operators
                            if (previousChar == 'e' || previousChar == 'E' || previousChar == '+' || previousChar == '-') {
                                haveError = true;
                            }
                            break;
                        }
                        //Checks for errors in e and transaction operators example 334Ee+2, ++4536
                        else if ((previousChar == 'E' && currentChar == 'e') || (previousChar == 'e' && currentChar == 'E') ||
                                (previousChar == 'E' && currentChar == 'E') || (previousChar == 'E' && currentChar == 'E') ||
                                (previousChar == 'E' && currentChar == '.') || (previousChar == '.' && currentChar == 'E') ||
                                (previousChar == '.' && currentChar == '+') || (previousChar == '+' && currentChar == '.') ||
                                (previousChar == '.' && currentChar == '-') || (previousChar == '-' && currentChar == '.')) {
                            currentLexeme += currentChar;
                            haveError = true;
                        }
                        else {
                            currentLexeme += currentChar;
                        }
                        //Checks for errors in e and transaction operators example 432E+34E-2
                        if (!isHaveOne(currentLexeme, '.') || !isHaveOne(currentLexeme, 'E') || !isHaveTwo(currentLexeme)) {
                            haveError = true;
                        }

                    }
                }

                //Checks for errors in e example 354.42E-2
                if((currentLexeme.contains("E") && currentLexeme.contains("."))) {
                    if(currentLexeme.indexOf('E') < currentLexeme.indexOf('.')) {
                        haveError = true;
                    }
                }

                //Checks for errors in e example 354.42e-2
                if((currentLexeme.contains("e") && currentLexeme.contains("."))) {
                    if(currentLexeme.indexOf('e') < currentLexeme.indexOf('.')) {
                        haveError = true;
                    }
                }

                //Finally, it checks the token and acts accordingly
                if (haveError) {
                    printError(currentLexeme);
                }
                else if (isIdentifier) {
                    printToken("IDENTIFIER");
                }
                else {
                    printToken("NUMBER");
                }

                currentLexeme = ""; //Reset recording
            }//number block end

        }//big while loop end


        F.close();
        file.close();
        file2.close();
    }


    //This function updates the global variable currentChar and keeps track of location
    public static void lex(FileReader F) throws IOException {
        previousChar = currentChar; //assign value of current char to previous char
        currentChar = (char) F.read(); //update current char by reading file

        if(currentChar != '\n')
            column++; //in each read of char, column index should be increased by one
        if((currentChar == '\n' || currentChar == '\r' ) && previousChar != '\r'){  //if we go to new line
            column = 0; //assign column index to 0 since it turns back to the beginning of the line
            line++;  //increase line index by 1
        }
    }

    // this method checks token is a binary digit or not
    public static boolean isBinaryDigit(char c){

        if(c == '0' || c == '1')   //if it is  0 or 1, returns true
            return true;
        else
            return false;
    }

    // this method checks token is a decimal digit or not
    public static boolean isDecDigit(char c){

        if( c >= '0' && c <= '9')  // if it is one of 0,1,2,3,4,5,6,7,8,9 returns true
            return true;
        else
            return false;
    }


    //this method checks token is a hexadecimal digit or not
    public static boolean isHexDigit(char c){

        if( (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))  //it can be a decimal digit from 0
            //up to 9, or a letter from a to f, or again a letter from A to F
            return true;
        else
            return false;
    }

    //this method checks weather token is a letter or not
    public static boolean isLetter(char c){

        if(c >= 'a' && c <= 'z')
            return true;
        else
            return false;
    }


    //Printing the error
    public static void printError(String token) throws IOException {
        if(currentChar == '\n' || currentChar == '\r') {//If after the token is new line, than line is: line -1 (since it is incremented by 1)
            System.out.println("LEXICAL ERROR" + " [" + (line - 1) + ":" + tokenStartingColumn + "]: Invalid token '" + token + "'");
            file.write("LEXICAL ERROR" + " [" + (line - 1) + ":" + tokenStartingColumn + "]: Invalid token '" + token + "'\n");
        }
        else {
            System.out.println("LEXICAL ERROR" + " [" + line + ":" + tokenStartingColumn + "]: Invalid token '" + token + "'");
            file.write("LEXICAL ERROR" + " [" + line + ":" + tokenStartingColumn + "]: Invalid token '" + token + "'\n");
        }
        F.close();
        file.close();
        System.exit(0);
    }



    //Printing the token
    public static void printToken(String token) throws IOException {


        if(currentChar == '\n' || currentChar == '\r') {
            //System.out.println(token + " " + (line - 1) + ":" + tokenStartingColumn);
            file.write(token + " " + (line - 1) + ":" + tokenStartingColumn +"\n");
        }
        else {
            //System.out.println(token + " " + line + ":" + tokenStartingColumn);
            file.write(token + " " + line + ":" + tokenStartingColumn + "\n");
        }

        file2.write(currentLexeme + "\n"); //Writing the current lexeme
    }


    //This method checks weather a token is keyword or not
    public static boolean isKeyword(String token){

        String keywords[] = {"define", "let", "cond", "if", "begin", "true", "false"};

        for(int i = 0; i < keywords.length; i++){  //comparing token with keywords
            if(token.equals(keywords[i]))
                return true;
        }

        return false;
    }


    //This method checks token is a boolean expression or not
    public static boolean isBoolean(String token){
        if(token.equals("true") || token.equals("false"))
            return true;
        else
            return false;
    }


    //Returns false if there is more than one given char in the String
    public static boolean isHaveOne(String str, char ch) {
        str = str.toUpperCase(); //set string to uppercase
        int count = 0;
        for (int i = 0; i < str.length(); i++) { //counts the occurrence of given char
            if (str.charAt(i) == ch) {
                count++;
            }
        }

        if(count > 1) {
            return false;
        } else {
            return true;
        }
    }

    //Returns false if there is more than two + or - in the String.
    public static boolean isHaveTwo(String str) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) { //counts the occurrence of + or -
            if (str.charAt(i) == '+' || str.charAt(i) == '-') {
                count++;
            }
        }

        if(count > 2) {
            return false;
        } else {
            return true;
        }
    }


    public static boolean isParenthesis(char c){
        if(c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}')
            return true;
        else
            return false;
    }

    /* String.toUpperCase depends on the language of a system.
     * For example, define might be converted to DEFİNE in Turkish language. So we create our own function.
     */
    public static String findUpperCase(String str){

        switch (str){
            case "define": return "DEFINE";
            case "let": return "LET";
            case "cond": return "COND";
            case "if": return "IF";
            default: return "BEGIN";
        }
    }
}
