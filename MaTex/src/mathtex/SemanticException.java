package mathtex;

import node.Token;


public class SemanticException extends RuntimeException {
	private final String message;
	private final Token token;

	public SemanticException( String message, Token token) {
		this.message = message;
		this.token = token;
	}

	@Override
	public String getMessage() {
		return "AT LIGNE "+ this.token.getLine()+" POSITION "+ this.token.getPos()+" "+ this.message;
	}
}
