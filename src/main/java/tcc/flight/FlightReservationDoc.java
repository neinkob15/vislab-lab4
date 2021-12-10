package tcc.flight;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "flightReservation")
public class FlightReservationDoc {

	String name;
	String url;
	String airline;
	String from;
	String to;

	long expires;
	long date;
	long bookingDate;

	boolean confirmed;

	public FlightReservationDoc() {
	}

	@XmlElement
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement
	public String getAirline() {
		return airline;
	}

	public void setAirline(String airline) {
		this.airline = airline;
	}

	@XmlElement
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	@XmlElement
	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	@XmlAttribute
	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	@XmlAttribute
	public long getBookingDate() {
		return bookingDate;
	}

	public void setBookingDate(long bookingDate) {
		this.bookingDate = bookingDate;
	}

	@XmlAttribute
	public long getExpires() {
		return expires;
	}

	public void setExpires(long expires) {
		this.expires = expires;
	}

	@XmlElement
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@XmlElement
	public boolean getConfirmed() {
		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	@Override
	public String toString() {
		return "FlightReservationDoc [name=" + name + ", url=" + url + ", airline=" + airline + ", from=" + from
				+ ", to=" + to + ", expires=" + expires + ", date=" + date + ", bookingDate=" + bookingDate
				+ ", confirmed=" + confirmed + "]";
	}

}
