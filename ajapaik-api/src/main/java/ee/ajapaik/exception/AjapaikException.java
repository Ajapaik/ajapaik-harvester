package ee.ajapaik.exception;


/**
 * Default exception class used in various cases
 * 
 * @author <a href="mailto:kaido@quest.ee?subject=BillingException">Kaido Kalda</a>
 */
public class AjapaikException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public enum Type {
		DATABASE_ERROR(1), PLATFORM_ERROR(2);
		
		private int code;

		Type(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}
	}
	
	private Type type;
	private transient Exception cause;

	public AjapaikException(String message, Type type, Exception cause) {
		this(message, type);
		this.cause = cause;
	}
	
	public AjapaikException(String message, Type type) {
		super(message);
		this.type = type;
	}

	@Override
	public String toString() {
		return "AjapaikException [type=" + type + ", message="
				+ getMessage() + "]";
	}

	public Type getType() {
		return type;
	}

	public Exception getCause() {
		return cause;
	}
}
