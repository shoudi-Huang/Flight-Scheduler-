import java.util.Comparator;
import java.util.Collections;
import java.util.ArrayList;

public class Flight {
    public String flight_ID;
    private double ticket_price;
    public int capacity;
    public Location source;
    public Location destination_location;
    public String departure_day;
    public int departure_time_h;
    public int departure_time_m;
    public String arrival_day;
    public int arrival_time_h;
    public int arrival_time_m;
    public int num_passenger_booked;
    private Location sort_depar_arri_flights_location;

    public Flight(String flight_ID,int capacity, int num_passenger_booked, Location source, Location destination_location, String departure_datetime) {
        this.sort_depar_arri_flights_location = null;
        this.flight_ID = flight_ID;
        this.capacity = capacity;
        this.source = source;
        this.destination_location = destination_location;
        this.num_passenger_booked = num_passenger_booked;
        this.ticket_price = this.getTicketPrice();

        String departure_day = departure_datetime.split(" ")[0];
        int hour = Integer.parseInt(departure_datetime.split(" ")[1].split(":")[0]);
        int minutes = Integer.parseInt(departure_datetime.split(" ")[1].split(":")[1]);
        this.departure_day = departure_day;
        this.departure_time_h = hour;
        this.departure_time_m = minutes;

        int duration_h = this.getDuration() / 60;
        int duration_m = this.getDuration() % 60;
        int round_up_h = 0;
        int round_up_day = 0;

        this.arrival_time_m = (this.departure_time_m + duration_m) % 60;
        if((this.departure_time_m + duration_m) / 60 != 0){
            round_up_h = (this.departure_time_m + duration_m) / 60;
        }

        int arrival_time_h = this.departure_time_h + duration_h + round_up_h;
        if(arrival_time_h > 23) {
            round_up_day = arrival_time_h / 24;
            arrival_time_h = arrival_time_h % 24;
        }
        this.arrival_time_h = arrival_time_h;

        if(round_up_day != 0) {
            String[] day_ls = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            int x = 0;
            for(String day: day_ls) {
                if(day.equals(this.departure_day)) {
                    this.arrival_day = day_ls[(x + round_up_day) % 7];
                    break;
                }
                x++;
            }
        }
        else {
            this.arrival_day = this.departure_day;
        }
    }
    
    //get the number of minutes this flight takes (round to nearest whole number)
    public int getDuration() {
        // speed = 720km/h --> 
        double speed = 720 / 60;
        double distance = this.getDistance();
        int duration = (int) Math.round(distance / speed);
        return duration;
        
    }

    //implement the ticket price formula
    public double getTicketPrice() {
        double distance = this.getDistance();
        double initial_ticket_price = (distance / 100) * (30 + 4*(destination_location.demand - source.demand));
        double percentage_booked = this.num_passenger_booked / (double) this.capacity;
        double y = 1;
        if(percentage_booked <= 0.5) {
            y = -0.4 * percentage_booked + 1;
        }
        else if(percentage_booked <= 0.7) {
            y = percentage_booked + 0.3;
        }
        else {
            y = (0.2 / Math.PI) * Math.atan(20 * percentage_booked - 14) + 1;
        }
        double ticket_price = y * initial_ticket_price;
        return ticket_price;
    }

    // book the given number of passengers onto this flight, returning the total cost
    public double book(int num) {
        double total_cost = 0d;
        for(int i = 1; i<= num; i++) {
            double price = this.getTicketPrice();
            total_cost += price;
            this.num_passenger_booked += 1;
        }
        this.ticket_price = this.getTicketPrice();
        return total_cost;
    }

    //return whether or not this flight is full
    public boolean isFull() {
		if(this.capacity - this.num_passenger_booked == 0) {
            return true;
        }
        else {
            return false;
        }
	}

    //get the distance of this flight in km
    public double getDistance() {
		return Location.distance(source, destination_location);
	}

    //get the layover time, in minutes, between two flights
    public static int layover(Flight x, Flight y) {
        int layover_m = 0;
        int day_difference = 0;
        String[] day_ls = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        int x_index = -1;
        int y_index = -1;
        int i = 0;
        for(String day: day_ls) {
            if(day.equals(x.arrival_day)) {
                x_index = i;
            }
            if(day.equals(y.departure_day)) {
                y_index = i;
            }
            i++;
        }
        if(x_index > y_index || (x_index == y_index && x.arrival_time_h > y.departure_time_h)) {
            day_difference = 6 - x_index + y_index + 1;
            // if(y.flight_ID.equals("105"))
            //     System.out.println(x_index + " " + y_index + " " + y.departure_day + " " + day_difference);
        }
        else {
            day_difference = y_index - x_index;
        }
        // if(y.flight_ID.equals("105"))
        //     System.out.println(day_difference);
        layover_m = (60 * (24 * day_difference + y.departure_time_h) + y.departure_time_m) - (60 * x.arrival_time_h + x.arrival_time_m);

        return layover_m;
    }

    public int checkArriOrDepar(Location l) {
        if(this.source.equals(l)) {
            return 1;
        }
        else if(this.destination_location.equals(l)) {
            return -1;
        }
        else {
            return 0;
        }
    }

    public static ArrayList<Flight> sortDeparArriFlights(ArrayList<Flight> flights_ls, Location l) {
        for(int i = 0; i<flights_ls.size(); i++) {
            flights_ls.get(i).sort_depar_arri_flights_location = l;
        }
        Collections.sort(flights_ls, Flight.FlightDeparArriComparator);
        for(int i = 0; i<flights_ls.size(); i++) {
            flights_ls.get(i).sort_depar_arri_flights_location = null;
        }
        return flights_ls;
    }

    public static Comparator<Flight> FlightIDComparator = new Comparator<Flight>() {
        public int compare(Flight f, Flight f1) {
            int f_id = Integer.parseInt(f.flight_ID);
            int f1_id = Integer.parseInt(f1.flight_ID);
            if(f_id < f1_id) {
                return -1;
            }
            else {
                return 1;
            }
        }
    };

    public static Comparator<Flight> FlightDeparANDNameComparator = new Comparator<Flight>() {
        public int compare(Flight f, Flight f1) {
            if(f.departure_day.equals(f1.departure_day) == false) {
                String[] day_ls = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
                int f_day_index = 0;
                int f1_day_index = 0;
                int x = 0;
                for(String day: day_ls) {
                    if(day.equals(f.departure_day)) {
                        f_day_index = x;
                    }
                    else if(day.equals(f1.departure_day)) {
                        f1_day_index = x;
                    }
                    x++;
                }
                if(f_day_index > f1_day_index) {
                    return 1;
                }
                else {
                    return -1;
                }
            }

            if(f.departure_time_h > f1.departure_time_h) {
                return 1;
            }
            else if(f.departure_time_h == f1.departure_time_h) {
                if(f.departure_time_m > f1.departure_time_m)
                    return 1;
                else if(f.departure_time_m == f1.departure_time_m) {
                    if(f.source.name.compareTo(f1.source.name) > 0)
                        return 1;
                    else if(f.source.name.compareTo(f1.source.name) < 0)
                        return -1;
                    else return 0;
                }
                else return -1;
            }
            else return -1;
        }
    };

    public static Comparator<Flight> FlightDeparComparator = new Comparator<Flight>() {
        public int compare(Flight f, Flight f1) {
            if(f.departure_day.equals(f1.departure_day) == false) {
                String[] day_ls = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
                int f_day_index = 0;
                int f1_day_index = 0;
                int x = 0;
                for(String day: day_ls) {
                    if(day.equals(f.departure_day)) {
                        f_day_index = x;
                    }
                    else if(day.equals(f1.departure_day)) {
                        f1_day_index = x;
                    }
                    x++;
                }
                if(f_day_index > f1_day_index) {
                    return 1;
                }
                else {
                    return -1;
                }
            }

            if(f.departure_time_h > f1.departure_time_h) {
                return 1;
            }
            else if(f.departure_time_h == f1.departure_time_h) {
                if(f.departure_time_m > f1.departure_time_m)
                    return 1;
                else if(f.departure_time_m == f1.departure_time_m) {
                    return 0;
                }
                else return -1;
            }
            else return -1;
        }
    };

    public static Comparator<Flight> FlightArriComparator = new Comparator<Flight>() {
        public int compare(Flight f, Flight f1) {
            if(f.arrival_day.equals(f1.arrival_day) == false) {
                String[] day_ls = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
                int f_day_index = 0;
                int f1_day_index = 0;
                int x = 0;
                for(String day: day_ls) {
                    if(day.equals(f.arrival_day)) {
                        f_day_index = x;
                    }
                    else if(day.equals(f1.arrival_day)) {
                        f1_day_index = x;
                    }
                    x++;
                }
                if(f_day_index > f1_day_index) {
                    return 1;
                }
                else {
                    return -1;
                }
            }

            if(f.arrival_time_h > f1.arrival_time_h) {
                return 1;
            }
            else if(f.arrival_time_h == f1.arrival_time_h) {
                if(f.arrival_time_m > f1.arrival_time_m)
                    return 1;
                else if(f.arrival_time_m == f1.arrival_time_m) {
                    return 0;
                }
                else return -1;
            }
            else return -1;
        }
    };

    private static Comparator<Flight> FlightDeparArriComparator = new Comparator<Flight>() {
        public int compare(Flight f, Flight f1) {
            String f_operation_day = null;
            int f_operation_h = -1;
            int f_operation_m = -1;
            String f1_operation_day = null;
            int f1_operation_h = -1;
            int f1_operation_m = -1;

            if(f.checkArriOrDepar(f.sort_depar_arri_flights_location) == 1) {
                f_operation_day = f.departure_day;
                f_operation_h = f.departure_time_h;
                f_operation_m = f.departure_time_m;
            }
            else if(f.checkArriOrDepar(f.sort_depar_arri_flights_location) == -1) {
                f_operation_day = f.arrival_day;
                f_operation_h = f.arrival_time_h;
                f_operation_m = f.arrival_time_m;
            }

            if(f1.checkArriOrDepar(f1.sort_depar_arri_flights_location) == 1) {
                f1_operation_day = f1.departure_day;
                f1_operation_h = f1.departure_time_h;
                f1_operation_m = f1.departure_time_m;
            }
            else if(f1.checkArriOrDepar(f1.sort_depar_arri_flights_location) == -1) {
                f1_operation_day = f1.arrival_day;
                f1_operation_h = f1.arrival_time_h;
                f1_operation_m = f1.arrival_time_m;
            }

            if(f_operation_day.equals(f1_operation_day) == false) {
                String[] day_ls = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
                int f_day_index = 0;
                int f1_day_index = 0;
                int x = 0;
                for(String day: day_ls) {
                    if(day.equals(f_operation_day)) {
                        f_day_index = x;
                    }
                    else if(day.equals(f1_operation_day)) {
                        f1_day_index = x;
                    }
                    x++;
                }
                if(f_day_index > f1_day_index) {
                    return 1;
                }
                else {
                    return -1;
                }
            }

            if(f_operation_h > f1_operation_h) {
                return 1;
            }
            else if(f_operation_h == f1_operation_h) {
                if(f_operation_m > f1_operation_m) {
                    return 1;
                }
                else if(f_operation_m == f1_operation_m) {
                    return 0;
                }
                else {
                    return -1;
                }
            }
            else {
                return -1;
            }
        }
    };
}
