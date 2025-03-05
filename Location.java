import java.lang.Math;
import java.util.ArrayList;
import java.util.Comparator;

public class Location {
	public String name;
	public double lat;
	public double lon;
	public double demand;
	public ArrayList<Flight> arrivingFlights = new ArrayList<>();
	public ArrayList<Flight> deparFlights = new ArrayList<>();

	public Location(String name, double lat, double lon, double demand) {
		this.name = name;
		this.lat = lat;
		this.lon = lon;
		this.demand = demand;
	}

    //Implement the Haversine formula - return value in kilometres
    public static double distance(Location l1, Location l2) {
		double lat_1 = l1.lat * Math.PI / 180;
		double lon_1 = l1.lon * Math.PI / 180;
		double lat_2 = l2.lat * Math.PI / 180;
		double lon_2 = l2.lon * Math.PI / 180;

		// Radius of Earth in km
		double R = 6371d;
		double lat_difference = lat_1 - lat_2;
		double lon_difference = lon_1 - lon_2;
		
		double a = Math.pow(Math.sin(lat_difference / 2), 2) + (Math.cos(lat_1) * Math.cos(lat_2) * Math.pow(Math.sin(lon_difference / 2), 2));
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = R * c;

		return distance;
    }

    public void addArrival(Flight f) {
		arrivingFlights.add(f);
	}
	
	public void addDeparture(Flight f) {
		deparFlights.add(f);
	}
	
	/**
	 * Check to see if Flight f can depart from this location.
	 * If there is a clash, the clashing flight string is returned, otherwise null is returned.
	 * A conflict is determined by if any other flights are arriving or departing at this location within an hour of this flight's departure time.
	 * @param f The flight to check.
	 * @return "Flight <id> [departing/arriving] from <name> on <clashingFlightTime>". Return null if there is no clash.
	 */
	public String hasRunwayDepartureSpace(Flight f) {
		String re_massage = null;
		Flight conflic_flight = this.checkConflicFlightInLS(f.departure_day, f.departure_time_h, f.departure_time_m);

		if(conflic_flight != null) {
			String[] full_name_day_ls = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
			String[] day_ls = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
			String conflic_day = null;

			re_massage = "Flight " + conflic_flight.flight_ID + " ";
			if(conflic_flight.checkArriOrDepar(this) == 1) {
				int x = 0;
				for(String day: day_ls) {
					if(day.compareToIgnoreCase(conflic_flight.departure_day) == 0) {
						conflic_day = full_name_day_ls[x];
						break;
					}
					x++;
				}
				re_massage += "departing from " + this.name + " on " + conflic_day + " " + String.format("%02d", conflic_flight.departure_time_h) + ":" + String.format("%02d", conflic_flight.departure_time_m);
			}
			else if(conflic_flight.checkArriOrDepar(this) == -1) {
				int x = 0;
				for(String day: day_ls) {
					if(day.compareToIgnoreCase(conflic_flight.arrival_day) == 0) {
						conflic_day = full_name_day_ls[x];
						break;
					}
					x++;
				}
				re_massage += "arriving at " + this.name + " on " + conflic_day + " " + String.format("%02d", conflic_flight.arrival_time_h) + ":" + String.format("%02d", conflic_flight.arrival_time_m);
			}
		}
		return re_massage;
    }

    // /**
	//  * Check to see if Flight f can arrive at this location.
	//  * A conflict is determined by if any other flights are arriving or departing at this location within an hour of this flight's arrival time.
	//  * @param f The flight to check.
	//  * @return String representing the clashing flight, or null if there is no clash. Eg. "Flight <id> [departing/arriving] from <name> on <clashingFlightTime>"
	//  */
	public String hasRunwayArrivalSpace(Flight f) {
		String re_massage = null;
		Flight conflic_flight = this.checkConflicFlightInLS(f.arrival_day, f.arrival_time_h, f.arrival_time_m);
			
		if(conflic_flight != null) {
			String[] full_name_day_ls = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
			String[] day_ls = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
			String conflic_day = null;

			re_massage = "Flight " + conflic_flight.flight_ID + " ";
			if(conflic_flight.checkArriOrDepar(this) == 1) {
				int x = 0;
				for(String day: day_ls) {
					if(day.compareToIgnoreCase(conflic_flight.departure_day) == 0) {
						conflic_day = full_name_day_ls[x];
						break;
					}
					x++;
				}
				re_massage += "departing from " + this.name + " on " + conflic_day + " " + String.format("%02d", conflic_flight.departure_time_h) + ":" + String.format("%02d", conflic_flight.departure_time_m);
			}
			else if(conflic_flight.checkArriOrDepar(this) == -1) {
				int x = 0;
				for(String day: day_ls) {
					if(day.compareToIgnoreCase(conflic_flight.arrival_day) == 0) {
						conflic_day = full_name_day_ls[x];
						break;
					}
					x++;
				}
				re_massage += "arriving at " + this.name + " on " + conflic_day + " " + String.format("%02d", conflic_flight.arrival_time_h) + ":" + String.format("%02d", conflic_flight.arrival_time_m);
			}
		}
		return re_massage;
    }


	private Flight checkConflicFlightInLS(String f_day, int f_h, int f_m) {
		Flight conflic_flight = null;
		ArrayList<Flight> flights_ls = new ArrayList<>();
		flights_ls.addAll(this.deparFlights);
		flights_ls.addAll(this.arrivingFlights);
		flights_ls = Flight.sortDeparArriFlights(flights_ls, this);

		String[] day_ls = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
		String conflic_depar_day1 = f_day;
		String conflic_depar_day2 = f_day;
		int conflic_depar_time_h1 = f_h - 1;
		int conflic_depar_time_h2 = f_h + 1;
		int conflic_depar_time_m = f_m;
		if(f_h == 23) {
			for(int x=0; x<day_ls.length; x++) {
				if(day_ls[x].equals(f_day)) {
					if(x == day_ls.length - 1) {
						conflic_depar_time_h2 = 0;
						conflic_depar_day2 = day_ls[0];
					}
					else {
						conflic_depar_day2 = day_ls[x+1];
						conflic_depar_time_h2 = 0;
					}
					break;
				}
			}
		}
		else if(f_h == 0) {
			for(int x=0; x<day_ls.length; x++) {
				if(day_ls[x].equals(f_day)) {
					if(x == 0) {
						conflic_depar_time_h1 = 23;
						conflic_depar_day1 = day_ls[6];
					}
					else {
						conflic_depar_day1 = day_ls[x-1];
						conflic_depar_time_h1 = 23;
					}
					break;
				}
			}
		}
		
		boolean conflic_depar_flight_finded = false;
		for(int i=0; i<flights_ls.size(); i++) {
			Flight flight_in_ls = flights_ls.get(i);
			String flight_operating_day = null;
			int flight_operating_h = -1;
			int flight_operating_m = -1;
			boolean flight_in_ls_departing = false;

			if(flight_in_ls.checkArriOrDepar(this) == 1) {
				flight_operating_day = flight_in_ls.departure_day;
				flight_operating_h = flight_in_ls.departure_time_h;
				flight_operating_m = flight_in_ls.departure_time_m;
				flight_in_ls_departing = true;
			}
			else if(flight_in_ls.checkArriOrDepar(this) == -1) {
				flight_operating_day = flight_in_ls.arrival_day;
				flight_operating_h = flight_in_ls.arrival_time_h;
				flight_operating_m = flight_in_ls.arrival_time_m;
				if(conflic_depar_flight_finded == true)
					continue;
			}
			else {
				continue;
			}

			if(conflic_depar_day1.equals(conflic_depar_day2)) {
				if(flight_operating_day.equals(conflic_depar_day1)) {
					if(flight_operating_h == conflic_depar_time_h1) {
						if(flight_operating_m > conflic_depar_time_m) {
							conflic_flight = flight_in_ls;
							if(flight_in_ls_departing == true)
								conflic_depar_flight_finded = true;
						}
					}
					else if(flight_operating_h == conflic_depar_time_h2) {
						if(flight_operating_m < conflic_depar_time_m) {
							conflic_flight = flight_in_ls;
							if(flight_in_ls_departing == true)
								conflic_depar_flight_finded = true;
						}
					}
					else if(flight_operating_h == f_h) {
						conflic_flight = flight_in_ls;
						if(flight_in_ls_departing == true)
							conflic_depar_flight_finded = true;
					}
				}
			}
			else {
				if(flight_operating_day.equals(conflic_depar_day1)) {
					if(flight_operating_h == conflic_depar_time_h1) {
						if(flight_operating_m > conflic_depar_time_m) {
							conflic_flight = flight_in_ls;
							if(flight_in_ls_departing == true)
								conflic_depar_flight_finded = true;
						}
					}
					else if(flight_operating_h > conflic_depar_time_h1) {
						conflic_flight = flight_in_ls;
						if(flight_in_ls_departing == true)
							conflic_depar_flight_finded = true;
					}
				}
				else if(flight_operating_day.equals(conflic_depar_day2)) {
					if(flight_operating_h == conflic_depar_time_h2) {
						if(flight_operating_m < conflic_depar_time_m) {
							conflic_flight = flight_in_ls;
							if(flight_in_ls_departing == true)
								conflic_depar_flight_finded = true;
						}
					}
					else if(flight_operating_h < conflic_depar_time_h2) {
						conflic_flight = flight_in_ls;
						if(flight_in_ls_departing == true)
							conflic_depar_flight_finded = true;
					}
				}
			}
		}
		return conflic_flight;
	}

	public static Comparator<Location> SortLocationNameComparator = new Comparator<Location>() {
        public int compare(Location l1, Location l2) {
			if(l1.name.compareTo(l2.name) < 0) {
				return -1;
			}
			else {
				return 1;
			}
		}
	};
}
