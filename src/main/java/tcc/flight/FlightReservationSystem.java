package tcc.flight;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import tcc.BookingException;
import tcc.BookingException.BookingError;
import tcc.DateUtil;

public class FlightReservationSystem {

	// private constructor prevents instantiation
	private FlightReservationSystem() {
	}

	// Container holding singleton instance
	private static class SingletonHolder {
		private static final FlightReservationSystem instance = new FlightReservationSystem();
	}

	// singleton static access point
	public static FlightReservationSystem getInstance() {
		return SingletonHolder.instance;
	}

	// reservation in-memory store
	private Map<UUID, FlightReservation> reservations = new HashMap<UUID, FlightReservation>();

	// we need to guard the reservations map against concurrent write access
	private ReentrantLock lock = new ReentrantLock();

	// task scheduler for enforcing confirmation deadlines
	private Timer timer = new Timer();

	// Maximum number of bookings per day
	private static final int MAXBOOKINGS = 20;

	// Duration for confirming a reservation
	private static final int LEASE = 1 * 60 * 1000; // 1 minute in millis

	/**
	 * Reservation records
	 */
	class FlightReservation {
		UUID id = UUID.randomUUID();
		String name;
		String airline;
		String from;
		String to;
		long date;
		long confirmUntil = System.currentTimeMillis() + LEASE;
		TimerTask removal = scheduleRemoval(id, new Date(confirmUntil));
		boolean confirmed = false;

		public FlightReservation(String name, String airline, String from, String to, long date) {
			super();
			this.name = name;
			this.airline = airline;
			this.from = from;
			this.to = to;
			this.date = date;
		}

		long getBookingDate() {
			return DateUtil.normalize(date);
		}

		boolean isValidReservation() {
			return System.currentTimeMillis() < confirmUntil;
		}
	}

	// create a scheduled tasks to remove reservations after a deadline
	private TimerTask scheduleRemoval(UUID id, Date date) {
		final UUID resId = id;
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				try {
					cancelReservation(resId);
				} catch (BookingException e) {
					// eat
				}
			}
		};
		timer.schedule(task, date);
		return task;
	}

	/**
	 * Create a new reservation
	 */
	public FlightReservation createReservation(String name, String airline, String from, String to, long date)
			throws BookingException {
		long normdate = DateUtil.normalize(date);
		int bookings = 1;

		lock.lock();
		try {

			for (Iterator<FlightReservation> iterator = reservations.values().iterator(); iterator.hasNext();) {
				FlightReservation res = (FlightReservation) iterator.next();
				if (res.getBookingDate() == normdate && res.from.equals(from) && res.to.equals(to)
						&& res.airline.equals(airline)) {
					bookings += 1;
				}
			}

			if (bookings < MAXBOOKINGS) {
				FlightReservation reservation = new FlightReservation(name, airline, from, to, date);
				reservations.put(reservation.id, reservation);

				return reservation;
			} else {
				throw new BookingException(BookingError.MAX_NUM_OF_BOOKINGS);
			}

		} finally {
			lock.unlock();
		}
	}

	/**
	 * Read a reservation
	 */
	public FlightReservation getReservation(UUID id) {
		lock.lock();
		try {

			return reservations.get(id);

		} finally {
			lock.unlock();
		}
	}

	/**
	 * Read all reservations
	 */
	public Collection<FlightReservation> getReservations() {
		lock.lock();
		try {

			return reservations.values();

		} finally {
			lock.unlock();
		}
	}

	/**
	 * Confirm a pending reservation
	 */
	public void confirmReservation(UUID id) throws BookingException {
		lock.lock();
		try {

			if (!reservations.containsKey(id)) {
				throw new BookingException(BookingError.UNKNOWN_ID);
			} else {
				FlightReservation res = reservations.get(id);
				res.removal.cancel();
				res.confirmed = true;
			}

		} finally {
			lock.unlock();
		}
	}

	/**
	 * Cancel a pending reservation
	 */
	public void cancelReservation(UUID id) throws BookingException {
		lock.lock();
		try {

			if (!reservations.containsKey(id)) {
				throw new BookingException(BookingError.UNKNOWN_ID);
			} else if (reservations.get(id).confirmed) {
				throw new BookingException(BookingError.CANNOT_CANCEL_CONFIRMED_BOOKING);
			} else {
				reservations.remove(id);
			}

		} finally {
			lock.unlock();
		}
	}

}
