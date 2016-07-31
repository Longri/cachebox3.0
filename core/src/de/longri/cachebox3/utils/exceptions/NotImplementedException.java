package de.longri.cachebox3.utils.exceptions;

public class NotImplementedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private String argumentName;

	public String getArgumentName() {

		return argumentName;

	}

	public NotImplementedException(String argumentName) {

		super(getMessage(argumentName));

		this.argumentName = argumentName;

	}

	private static String getMessage(String argumentName) {

		if (argumentName == null) {

			throw new IllegalArgumentException("The NullArgumentException constructor \"argumentName\" cannot be null!");

		}

		if (argumentName.trim().length() == 0) {

			throw new IllegalArgumentException(

					"The NullArgumentException constructor \"argumentName\" cannot be an Empty (zero-length) String!");

		}

		return "the methode \"" + argumentName + "\" is not implemented!";

	}
}
