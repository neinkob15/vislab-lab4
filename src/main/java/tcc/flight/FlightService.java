package tcc.flight;

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
import tcc.flight.FlightReservationSystem.FlightReservation;

@Path("/flight")
public class FlightService {

	Logger logger = LoggerFactory.getLogger(FlightService.class);

	FlightReservationSystem frs = FlightReservationSystem.getInstance();

	// internal url pattern
	private String createReservationUrl(UUID id) {
		return TestServer.BASE_URI + "flight/" + id.toString();
	}

	// internal data mapping
	private FlightReservationDoc reservationToReservationDoc(FlightReservation in) {
		FlightReservationDoc out = new FlightReservationDoc();
		out.setDate(in.date);
		out.setBookingDate(in.getBookingDate());
		out.setExpires(in.confirmUntil);
		out.setName(in.name);
		out.setUrl(createReservationUrl(in.id));
		out.setConfirmed(in.confirmed);
		out.setFrom(in.from);
		out.setTo(in.to);
		out.setAirline(in.airline);
		return out;
	}

	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response reserveBooking(FlightReservationDoc docIn) {
		logger.info("POST request received on /flight");
		Response.Status stat = Response.Status.OK;
		FlightReservationDoc docOut = docIn;
		try {
			docOut = reservationToReservationDoc(frs.createReservation(docIn.name, docIn.airline, docIn.from, docIn.to,
					docIn.date));
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
	public List<FlightReservationDoc> getBookings() throws BookingException {
		logger.info("GET request received on /flight");
		List<FlightReservationDoc> result = new LinkedList<FlightReservationDoc>();
		for (Iterator<FlightReservation> iterator = frs.getReservations().iterator(); iterator.hasNext();) {
			result.add(reservationToReservationDoc((FlightReservation) iterator.next()));
		}
		return result;
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_XML)
	public FlightReservationDoc getBooking(@PathParam("id") String id) {
		logger.info("GET request received on /flight/" + id);
		return reservationToReservationDoc(frs.getReservation(UUID.fromString(id)));
	}

	@PUT
	@Path("{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response confirmReservation(@PathParam("id") String id) {
		logger.info("PUT request received on /flight/" + id);
		Response.Status stat = Response.Status.OK;
		String msg = "Reservation was confirmed.";
		try {
			frs.confirmReservation(UUID.fromString(id));
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
		logger.info("DELETE request received on /flight/" + id);
		Response.Status stat = Response.Status.OK;
		String msg = "Reservation was canceled.";
		try {
			frs.cancelReservation(UUID.fromString(id));
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
