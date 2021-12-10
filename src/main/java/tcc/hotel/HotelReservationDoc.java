package tcc.hotel;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "hotelReservation")
public class HotelReservationDoc {

	String name;
	String url;
	String hotel;
	long expires;
	long date;
	long bookingDate;
	boolean confirmed;

	public HotelReservationDoc() {
	}

	@XmlElement
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement
	public String getHotel() {
		return hotel;
	}

	public void setHotel(String hotel) {
		this.hotel = hotel;
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
		return "HotelReservationDoc [name=" + name + ", url=" + url + ", hotel=" + hotel + ", expires=" + expires
				+ ", date=" + date + ", bookingDate=" + bookingDate + ", confirmed=" + confirmed + "]";
	}

}
