package tcc.hotel;

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

public class HotelReservationSystem {

	// private constructor prevents instantiation
	private HotelReservationSystem() {
	}

	// Container holding singleton instance
	private static class SingletonHolder {
		private static final HotelReservationSystem instance = new HotelReservationSystem();
	}

	// singleton static access point
	public static HotelReservationSystem getInstance() {
		return SingletonHolder.instance;
	}

	// reservation in-memory store
	private Map<UUID, HotelReservation> reservations = new HashMap<UUID, HotelReservation>();

	// we need to guard the reservations map against concurrent write access
	private ReentrantLock lock = new ReentrantLock();

	// task scheduler for enforcing confirmation deadlines
	private Timer timer = new Timer();

	// Maximum number of bookings per day
	private static final int MAXBOOKINGS = 10;

	// Duration for confirming a reservation
	private static final int LEASE = 2 * 60 * 1000; // 1 minute in millis

	/**
	 * Reservation records
	 */
	class HotelReservation {
		UUID id = UUID.randomUUID();
		String name;
		String hotel;
		long date;
		long confirmUntil = System.currentTimeMillis() + LEASE;
		TimerTask removal = scheduleRemoval(id, new Date(confirmUntil));
		boolean confirmed = false;

		public HotelReservation(String name, String hotel, long date) {
			super();
			this.name = name;
			this.hotel = hotel;
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
	public HotelReservation createReservation(String name, String hotel, long date) throws BookingException {
		long normdate = DateUtil.normalize(date);
		int bookings = 1;

		lock.lock();
		try {

			for (Iterator<HotelReservation> iterator = reservations.values().iterator(); iterator.hasNext();) {
				HotelReservation res = (HotelReservation) iterator.next();
				if (res.getBookingDate() == normdate && res.hotel.equals(hotel)) {
					bookings += 1;
				}
			}

			if (bookings < MAXBOOKINGS) {
				HotelReservation reservation = new HotelReservation(name, hotel, date);
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
	public HotelReservation getReservation(UUID id) {
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
	public Collection<HotelReservation> getReservations() {
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
				HotelReservation res = reservations.get(id);
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
