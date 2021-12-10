package tcc.hotel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tcc.BookingException;
import tcc.TestServer;
import tcc.BookingException.BookingError;
import tcc.hotel.HotelReservationSystem.HotelReservation;

@Path("/hotel")
public class HotelService {

	Logger logger = LoggerFactory.getLogger(HotelService.class);

	HotelReservationSystem hrs = HotelReservationSystem.getInstance();

	// internal url pattern
	private String createReservationUrl(UUID id) {
		return TestServer.BASE_URI + "hotel/" + id.toString();
	}

	// internal data mapping
	private HotelReservationDoc reservationToReservationDoc(HotelReservation in) {
		HotelReservationDoc out = new HotelReservationDoc();
		out.setDate(in.date);
		out.setBookingDate(in.getBookingDate());
		out.setExpires(in.confirmUntil);
		out.setName(in.name);
		out.setHotel(in.hotel);
		out.setUrl(createReservationUrl(in.id));
		out.setConfirmed(in.confirmed);
		return out;
	}

	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response reserveBooking(HotelReservationDoc docIn) {
		logger.info("POST request received on /hotel");
		Response.Status stat = Response.Status.OK;
		HotelReservationDoc docOut = docIn;
		try {
			docOut = reservationToReservationDoc(hrs.createReservation(docIn.name, docIn.hotel, docIn.date));
		} catch (BookingException hex) {
			logger.warn("Exception caught: " + hex.getMessage());
			if (hex.error == BookingError.MAX_NUM_OF_BOOKINGS) {
				stat = Response.Status.CONFLICT;
			}
		}
		return Response.status(stat).entity(docOut).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_XML)
	public List<HotelReservationDoc> getBookings() throws BookingException {
		logger.info("GET request received on /hotel");
		List<HotelReservationDoc> result = new LinkedList<HotelReservationDoc>();
		for (Iterator<HotelReservation> iterator = hrs.getReservations().iterator(); iterator.hasNext();) {
			result.add(reservationToReservationDoc((HotelReservation) iterator.next()));
		}
		return result;
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_XML)
	public HotelReservationDoc getBooking(@PathParam("id") String id) {
		logger.info("GET request received on /hotel/" + id);
		return reservationToReservationDoc(hrs.getReservation(UUID.fromString(id)));
	}

	@PUT
	@Path("{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response confirmReservation(@PathParam("id") String id) {
		logger.info("PUT request received on /hotel/" + id);
		Response.Status stat = Response.Status.OK;
		String msg = "Reservation was confirmed.";
		try {
			hrs.confirmReservation(UUID.fromString(id));
		} catch (BookingException hex) {
			logger.warn("Exception caught: " + hex.getMessage());
			if (hex.error == BookingError.UNKNOWN_ID) {
				stat = Response.Status.CONFLICT;
				msg = "Could not confirm unknown booking.";
			}
		}
		return Response.status(stat).entity(msg).build();
	}

	@DELETE
	@Path("{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response cancelReservation(@PathParam("id") String id) {
		logger.info("DELETE request received on /hotel/" + id);
		Response.Status stat = Response.Status.OK;
		String msg = "Reservation was canceled.";
		try {
			hrs.cancelReservation(UUID.fromString(id));
		} catch (BookingException hex) {
			logger.warn("Exception caught: " + hex.getMessage());
			if (hex.error == BookingError.CANNOT_CANCEL_CONFIRMED_BOOKING) {
				stat = Response.Status.CONFLICT;
				msg = "Could not cancel confirmed booking.";
			}
		}
		return Response.status(stat).entity(msg).build();
	}

}
