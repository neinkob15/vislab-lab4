package tcc;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import tcc.flight.FlightReservationDoc;
import tcc.hotel.HotelReservationDoc;

/**
 * Simple non-transactional client. Can be used to populate the booking services
 * with some requests.
 */
public class TestClient {
	public static void main(String[] args) {
		try {
			Client client = ClientBuilder.newClient();
			WebTarget target = client.target(TestServer.BASE_URI);

			GregorianCalendar tomorrow = new GregorianCalendar();
			tomorrow.setTime(new Date());
			tomorrow.add(GregorianCalendar.DAY_OF_YEAR, 1);

			// webtargets
			WebTarget webTargetFlight = target.path("flight");
			WebTarget webTargetHotel = target.path("hotel");

			List<String> urlsToConfirm = new ArrayList<String>();

			// the flight
			FlightReservationDoc docFlight = new FlightReservationDoc();
			docFlight.setName("Christian");
			docFlight.setFrom("Karlsruhe");
			docFlight.setTo("Berlin");
			docFlight.setAirline("airberlin");
			docFlight.setDate(tomorrow.getTimeInMillis());
			// the hotel
			HotelReservationDoc docHotel = new HotelReservationDoc();
			docHotel.setName("Christian");
			docHotel.setHotel("Interconti");
			docHotel.setDate(tomorrow.getTimeInMillis());
			
			// try flight reservation
			boolean transactionFailed = false;
			FlightReservationDoc outputFlight = null;
			Response responseFlight = reserveFlight(webTargetFlight, docFlight);
			if (responseFlight.getStatus() != 200) {
				System.out.println("Failed : HTTP error code : " + responseFlight.getStatus());
				transactionFailed = true;
			} else {
				outputFlight = responseFlight.readEntity(FlightReservationDoc.class);
				System.out.println("Output from Server: " + outputFlight);
				urlsToConfirm.add(outputFlight.getUrl());
			}

			if (!transactionFailed) {
				// try hotel reservation
				Response responseHotel = reserveHotel(webTargetHotel, docHotel);
				if (responseHotel.getStatus() != 200) {
					System.out.println("Failed : HTTP error code : " + responseHotel.getStatus());
					transactionFailed = true;

					// cancel the flight reservation
					cancelFlightReservation(outputFlight.getUrl());

				} else {
					HotelReservationDoc outputHotel = responseHotel.readEntity(HotelReservationDoc.class);
					System.out.println("Output from Server: " + outputHotel);
					urlsToConfirm.add(outputHotel.getUrl());
				}
			}

			if (!transactionFailed) {
				// confirm reservation
				for (int i = 0; i < urlsToConfirm.size(); i++) {
					while (!confirmReservation(urlsToConfirm.get(i))) { Thread.sleep(1000); }
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static Response reserveFlight(WebTarget webTargetFlight, FlightReservationDoc docFlight) {
		return webTargetFlight.request().accept(MediaType.APPLICATION_XML)
		.post(Entity.xml(docFlight));
	}
	public static Response reserveHotel(WebTarget webTargetHotel, HotelReservationDoc docHotel) {
		return webTargetHotel.request().accept(MediaType.APPLICATION_XML)
		.post(Entity.xml(docHotel));
	}
	public static void cancelFlightReservation(String url) {
		Response r = ClientBuilder.newClient().target(url).request().accept(MediaType.TEXT_PLAIN).delete();
		System.out.println("status of delete: " + r.getStatus());
	}
	public static boolean confirmReservation(String url) {
		Response r = ClientBuilder.newClient().target(url).request().accept(MediaType.TEXT_PLAIN).put(Entity.xml(""));
		return r.getStatus() == 200;
	}
}
