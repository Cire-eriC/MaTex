package mathtex;

import java.io.*;

import lexer.*;
import node.*;
import parser.*;

public class Main {

	public static void main(String[] args) throws FileNotFoundException, ParserException, LexerException, IOException {


		//Node ast = new Parser(new Lexer(new PushbackReader(new FileReader(args[0]), 1024))).parse();
		try {
			Parser parser = new Parser(new Lexer(new PushbackReader(new FileReader(args[0]), 1024)));
			Node ast = parser.parse();
			//System.out.println(ast);
			ast.apply(new CodeGenerator());
		}
		catch (ParserException e) {
			System.err.println("Syntax error on "
					+ e.getToken().getClass().getSimpleName() + " token: "
					+ e.getToken());
			System.err.println(e.getMessage());
		}

/*		
		Lexer lexer = new Lexer(new PushbackReader(
				new FileReader(args[0]), 1024));
		while(true) {
			Token token = lexer.next();
			if(token instanceof EOF) {
				break;
			}
			System.out.print(token.getClass().getSimpleName() + ": \n");
			afficherString(token.getText());
			System.out.println("\nFIN TOKEN " + token.getClass().getSimpleName()+"\n");
		}
	}
		 
	
	static void afficherString(String unString){
		System.out.print('	');
		for (int i = 0 ; i < unString.length() ;++i ){
			if (unString.charAt(i) != '\n'){
				System.out.print(unString.charAt(i));
			}else{
				System.out.print("\n	");
			}			
	}
*/	
	}
	
}
