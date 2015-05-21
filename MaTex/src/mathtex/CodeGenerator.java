package mathtex;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import node.*;
import analysis.DepthFirstAdapter;

public class CodeGenerator extends DepthFirstAdapter {
	private PrintWriter writer = null;
	private String result;
	private String callVerbatim;
	private final Map<String,String> variableData = new LinkedHashMap <String,String>();
	private final Map<String,String> dynamicFunctionList = new LinkedHashMap <String,String>(); 
	private final Map<String,Integer> dynamicFunctionListSize = new LinkedHashMap <String,Integer>();
	private final ArrayList<String> argumentsValues = new ArrayList <String>();

	private void visit( Node node ){ if ( node != null ){node.apply( this );} }

	public CodeGenerator (){
		try {
			this.writer = new PrintWriter("output.tex", "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/*********************************************************************************************/
	/**                                          file                                           **/
	/*********************************************************************************************/

	@Override
	public void caseAMatexonFile(AMatexonFile node) {
		this.writer.print("%% use_matex;");
		visit(node.getSources());
		this.writer.close();
	}

	@Override
	public void caseAMatexoffFile(AMatexoffFile node) {
		this.writer.close();
	}


	/*********************************************************************************************/
	/**                                         sources                                         **/
	/*********************************************************************************************/

	/*********************************************************************************************/
	/**                                          source                                         **/
	/*********************************************************************************************/

	/*********************************************************************************************/
	/**                                       verbatim_latex                                    **/
	/*********************************************************************************************/
	@Override
	public void caseANormalVerbatimLatex(ANormalVerbatimLatex node) {
		this.writer.print(node.getCodelatex().getText());	
	}

	@Override
	public void caseACommentVerbatimLatex(ACommentVerbatimLatex node) {
		this.writer.print(node.getLatexComment().getText());
	}

	/*********************************************************************************************/
	/**                                       verbatim_matex                                    **/
	/*********************************************************************************************/

	@Override
	public void caseAVariableTypeVerbatimMatex(AVariableTypeVerbatimMatex node) {
		this.result = "";
		visit(node.getVariable());
		this.writer.print(this.result);
		this.result = "";
	}

	@Override
	public void caseAPrimitiveTypeFuncVerbatimMatex(APrimitiveTypeFuncVerbatimMatex node) {
		this.result = "";
		visit(node.getPrimitiveFunction());
		this.writer.print(this.result);
		this.result = "";
	}

	@Override
	public void caseADynamicFunctionTypeVerbatimMatex(ADynamicFunctionTypeVerbatimMatex node) {
		this.result = "";
		visit(node.getDynamicFunction());
		this.writer.print(this.result);
		this.result = "";
	}



	/*********************************************************************************************/
	/**                                          variable                                       **/
	/*********************************************************************************************/

	@Override
	public void caseAVariableEvalVariable(AVariableEvalVariable node) {
		this.result = "";
		String variabelId = node.getId().getText();
		visit(node.getPrimitiveFunction());

		if ( this.variableData.containsKey(variabelId) ){
			throw new SemanticException("VARIABLE:" + node.getId().getText() + " IS ALREADY DECLARED.", node.getId());
		}else{


			this.variableData.put(node.getId().getText(), this.result );
		}
		this.result = "%% var " + variabelId + " = " + this.callVerbatim + ";";

	}

	@Override
	public void caseAVariableDeclarationVariable(AVariableDeclarationVariable node) {
		String variableId = node.getId().getText();
		String temp = node.getStringLiteral().toString().substring(	1 , node.getStringLiteral().toString().length()-2);
		this.result = "%% var " + variableId + " = \"" + temp + "\";";

		if ( this.variableData.containsKey(variableId) ){
			throw new SemanticException("VARIABLE:" + node.getId().getText() + " IS ALREADY DECLARED.", node.getId());
		}else{
			this.variableData.put(node.getId().getText(), temp);
		}
	}

	@Override
	public void caseAVariableSwapVariable(AVariableSwapVariable node) {
		String variableId = node.getId().getText();
		String literal;
		this.result = "";

		if(!this.variableData.containsKey(node.getId().getText())){
			throw new SemanticException("VARIABLE \"" + variableId  + "\" IS NOT DECLARED.", node.getId());
		}else{
			literal = this.variableData.get(node.getId().getText());

			for (int i = 0; i<literal.length();++i){ // Pour ne pas afficher le caractere d'échapement
				if (literal.charAt(i) == '\\' && i < literal.length() && literal.charAt(i+1) == '"' ){
					literal = literal.substring(0, i) + literal.substring(i+1);
				}

			}
			this.result = literal; 
		}
	}

	/*********************************************************************************************/
	/**                                    primitive_function                                  **/
	/*********************************************************************************************/

	@Override
	public void caseAInternMatrixPrimitiveFunction(AInternMatrixPrimitiveFunction node) {
		int nombreDeLigne = 0 , nombreDeColonne = 0;
		this.result = "";

		try {
			nombreDeLigne = Integer.parseInt(node.getNumberOfLine().toString().trim());
			nombreDeColonne = Integer.parseInt(node.getNumberOfColumn().toString().trim());
		} catch (NumberFormatException e) {
			System.err.println("INVALID NUMBER AT POSITION  [" + node.getNumberOfLine().getLine() + 
					"," + node.getNumberOfColumn().getPos() + "]");
		}

		if ( nombreDeLigne == 0 || nombreDeLigne > 10){
			throw new SemanticException("LINE NUMBER MUST BE BETWEEN 1 AND 10 : " + nombreDeLigne + "!",node.getNumberOfLine());

		}
		if ( nombreDeColonne == 0 || nombreDeColonne > 10){
			throw new SemanticException("LINE NUMBER MUST BE BETWEEN 1 AND 10 : " + nombreDeColonne + "!",node.getNumberOfColumn());
		}

		this.callVerbatim = "Matrix(" + nombreDeLigne + "," + nombreDeColonne + ")";

		this.result = "\n% AUTOMATIC CODE GENERERATION FOR: " + this.callVerbatim +";\n";
		this.result +="\\left[ {\\begin{array}{";

		for ( int i = 0 ; i < nombreDeColonne ; ++i ){			
			this.result += "c";
		}
		this.result +="}\n";

		for(int i = 0; i < nombreDeLigne ; ++i ){
			this.result += "   ";
			for(int j = 0 ; j < nombreDeColonne-1 ; ++j ){
				this.result += " &";
			}
			this.result += " \\\\\n";
		}

		this.result += "\\end{array} } \\right]\n";
		this.result += "% END OF AUTOMATIC CODE GENERERATION";

	}

	@Override
	public void caseAInternBasicSumPrimitiveFunction(AInternBasicSumPrimitiveFunction node) {
		this.callVerbatim = "Sum()";
		this.result = "\\sum\\limits_{i = index\\_start}^{index\\_end}";
	}

	@Override
	public void caseAInternIntegralPrimitiveFunction(AInternIntegralPrimitiveFunction node) {
		this.argumentsValues.clear(); // Si aucun arg il ne clear pas les valeurs !!!		
		visit(node.getArgs());

		if (this.argumentsValues.size() == 0){
			this.result = "\\int_{index\\_start}^{index\\_end} \\mathrm{d} \\, variable\\_id";
		}else if (this.argumentsValues.size()==2){
			this.result = "\\int_{" +this.argumentsValues.get(0) + "}^{" +this.argumentsValues.get(1) + "} \\mathrm{d} \\, variable\\_id";
		}else if (this.argumentsValues.size()==3){
			this.result = "\\int_{" +this.argumentsValues.get(0) + "}^{" +this.argumentsValues.get(1) 
					+ "} \\mathrm{d} " + this.argumentsValues.get(2);
		}else if (this.argumentsValues.size()==4){
			this.result = "\\int_{" +this.argumentsValues.get(0) + "}^{" +this.argumentsValues.get(1) 
					+ "}"+ this.argumentsValues.get(2)+" \\mathrm{d} " + this.argumentsValues.get(3);
		}else{
			throw new SemanticException("THE FUNCTION Int HAVE 0,2 or 3 ARGUMENTS NOT " + this.argumentsValues.size()+".",node.getInt());
		}
	}


	@Override
	public void caseAInternIMatrixPrimitiveFunction(AInternIMatrixPrimitiveFunction node) {
		int taille = 0;
		int i ,j;

		try {
			taille = Integer.parseInt(node.getSize().toString().trim());
		} catch (NumberFormatException e) {
			System.err.println("INVALID NUMBER AT POSITION [" + node.getSize().getLine() + 
					"," + node.getSize().getPos() + "]");
		}

		if ( taille == 0 || taille > 10){
			throw new SemanticException("SIZE MUST BE BETWEEN 1 AND 10: " + taille + "!",node.getSize());

		}
		this.callVerbatim = "MatrixI(" + taille + ")";
		this.result ="\n% AUTOMATIC CODE GENERERATION FOR: " + this.callVerbatim + ";\n";
		this.result +="\\left[ {\\begin{array}{";

		for ( i = 0 ; i < taille ; ++i ){			
			this.result +="c";
		}
		this.result +="}\n";

		for( i = 0; i < taille ; ++i ){
			this.result += "   ";
			for( j = 0 ; j < taille-1 ; ++j ){
				if (i==j){
					this.result +=" 1 &";	
				}else{
					this.result += " 0 &";
				}	
			}

			if (i==j){
				this.result += " 1 ";	
			}else{
				this.result += " 0 ";
			}

			this.result +=" \\\\\n";
		}

		this.result += "\\end{array} } \\right]\n";
		this.result += "% END OF AUTOMATIC CODE GENERERATION";

	}

	/*********************************************************************************************/
	/**                                     dynamic_function                                    **/
	/*********************************************************************************************/

	@Override
	public void caseADynamicFuncDefDynamicFunction(	ADynamicFuncDefDynamicFunction node) {
		String functionId = node.getId().getText();
		int numberOfArguments = Integer.parseInt(node.getNumber().toString().trim());
		String dynamiclitteral = node.getStringLiteral().toString();
		boolean flag = true;

		if (numberOfArguments > 10){
			throw new SemanticException("FUNCTION \"" + functionId + "\" MUST A MAXIMUM OF 10 ARGUMENT.", node.getId());
		}


		if ( this.dynamicFunctionList.containsKey(functionId) ){
			throw new SemanticException("FUNCTION \"" + functionId + "\" IS ALREADY DECLARED.", node.getId());
		}else{
			this.dynamicFunctionList.put(node.getId().getText(), dynamiclitteral.substring(1,dynamiclitteral.length()-2));
		}
		//System.out.println("->" +numberOfArguments);
		this.dynamicFunctionListSize.put(node.getId().getText(),numberOfArguments); // Pas super a creer une classe pour tout englober !!

		while (flag){
			flag = false;
			for (int i = 0; i < dynamiclitteral.length() ; ++i ){
				if (dynamiclitteral.charAt(i) == '\n' && dynamiclitteral.charAt(i+1) != '%'){
					dynamiclitteral = dynamiclitteral.substring(0, i) + '%' + dynamiclitteral.substring(i+1);
					flag = true;
				}
			}
		}

		if ( dynamiclitteral.contains("arg[0]") ){
			throw new SemanticException("FUNCTION ARGUMENT IDENTIFIER arg[0] IS INVALID, INDEX STARTS AT 1." , node.getId());
		}

		for (int i = 1 ; i <= numberOfArguments ;++i){
			String arg = "arg[" + i + "]";
			if (!dynamiclitteral.contains(arg)){
				throw new SemanticException("FUNCTION \"" + functionId + "\" CONTAINS NO arg[" + i + "].", node.getId());
			}
		}

		for (int i = numberOfArguments+1 ; i <= 20 ;++i){
			String arg = "arg[" + i + "]";
			if (dynamiclitteral.contains(arg)){
				throw new SemanticException("FUNCTION \"" + functionId + "\" CONTAINS arg[" + i + "], NUMBER OF ARGUMENTS SPECIFIED IS " 
						+ numberOfArguments + ".", node.getId());
			}
		}
		this.result = "%% fun " + functionId +"(" + numberOfArguments + ")"+ " = " + dynamiclitteral.trim() + ";";
	}

	@Override
	public void caseADynamicFuncCallDynamicFunction(ADynamicFuncCallDynamicFunction node) {
		String functionId = node.getId().getText();
		String dynamicGeneration;

		if(!this.dynamicFunctionList.containsKey(node.getId().getText())){
			throw new SemanticException("FUNCTION \"" + functionId  + "\" IS NOT DECLARED.", node.getId());
		}else{
			dynamicGeneration = this.dynamicFunctionList.get(node.getId().getText());
			dynamicGeneration = dynamicGeneration.replaceAll("\n%","\n");

		}

		this.argumentsValues.clear();
		visit (node.getArgs());
		if (this.argumentsValues.size() != this.dynamicFunctionListSize.get(functionId)){
			throw new SemanticException("FUNCTION \"" + functionId  + "\" HAS " + this.dynamicFunctionListSize.get(functionId) + 
					" ARGUMENTS AND YOU CALL IT WITH " + this.argumentsValues.size() + " ARGUMENTS.",  node.getId());
		}

		for (int i = 1 ; i <= this.argumentsValues.size() ;++i){
			String argumentsSwap = "arg\\[" + i +"\\]";
			
			dynamicGeneration = dynamicGeneration.replaceAll(argumentsSwap, this.argumentsValues.get(i-1).replaceAll("\\\\","\\\\\\\\" ));//POURQUOI CA MARCHE???
			//System.out.println(this.argumentsValues.get(i-1));
		}

		for (int i = 0; i < dynamicGeneration.length();++i){ // Pour ne pas afficher le caractere d'échapement
			if (dynamicGeneration.charAt(i) == '\\' && i < dynamicGeneration.length() && dynamicGeneration.charAt(i+1) == '"' ){
				dynamicGeneration = dynamicGeneration.substring(0, i) + dynamicGeneration.substring(i+1);
			}
		}
		
		//System.out.println(dynamicGeneration);
		
		
		this.result = dynamicGeneration;
	}

	/*********************************************************************************************/
	/**                                          args                                           **/
	/*********************************************************************************************/
	@Override
	public void caseAArgs(AArgs node) {
		this.argumentsValues.clear();
		visit(node.getArg());
		this.argumentsValues.add(this.result);

		for(int i = 0 ;i < node.getAdditionalArgs().size() ; ++i){
			visit(node.getAdditionalArgs().get(i));
			this.argumentsValues.add(this.result);
		}
	}

	/*********************************************************************************************/
	/**                                     additional_arg                                      **/
	/*********************************************************************************************/

	/*********************************************************************************************/
	/**                                         arg                                             **/
	/*********************************************************************************************/

	@Override
	public void caseAVariableIdArg(AVariableIdArg node) {
		if(!this.variableData.containsKey(node.getId().getText())){
			throw new SemanticException("VARIABLE \"" + node.getId().getText()  + "\" IS NOT DECLARED.", node.getId());
		}else{
			//System.out.println(this.variableData.get(node.getId().getText()));
			this.result = this.variableData.get(node.getId().getText());
			//this.result = this.result.replaceAll("\\\\", "\\\\\\\\"); // Comprends pas pourquoi 8 backslash ! A VERIFIER 
		}
	}

	@Override
	public void caseALatexDirectArg(ALatexDirectArg node) {
		this.result = node.getStringLiteral().toString().substring(1, node.getStringLiteral().toString().length()-2);

	}

	@Override
	public void caseAPrimitiveFunctionArg(APrimitiveFunctionArg node) {
		visit(node.getPrimitiveFunction());
		//this.result = this.result.replaceAll("\\\\", "\\\\\\\\"); // Comprends pas pourquoi 8 backslash ! A VERIFIER 
	}

} // END CodeGenerator class

