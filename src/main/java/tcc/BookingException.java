package tcc;

public class BookingException extends Exception {

	private static final long serialVersionUID = 1L;

	public enum BookingError {
		UNKNOWN_ID, CANNOT_CANCEL_CONFIRMED_BOOKING, MAX_NUM_OF_BOOKINGS, UNKNOWN
	}

	public BookingError error;

	public BookingException(BookingError error) {
		super(error.name());
		this.error = error;
	}

}
