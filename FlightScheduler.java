import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;
import java.lang.Math;
import java.io.File; 
import java.io.FileNotFoundException;
import java.lang.NumberFormatException;
import java.io.PrintWriter;
import java.util.Comparator;

public class FlightScheduler {

    private static FlightScheduler instance;

    public static void main(String[] args) {
        instance = new FlightScheduler(args);
        instance.run();
    }

    public static FlightScheduler getInstance() {
        return instance;
    }

    public FlightScheduler(String[] args) {}

    public void run() {
        // Do not use System.exit() anywhere in your code,
        // otherwise it will also exit the auto test suite.
        // Also, do not use static attributes otherwise
        // they will maintain the same values between testcases.

        // START YOUR CODE HERE
        Scanner keyboard = new Scanner(System.in);
        // Lists for Flights and Locations
        ArrayList<Location> locations_ls = new ArrayList<>();
        ArrayList<Flight> flights_ls = new ArrayList<>();
        String help_massage = """
                              FLIGHTS - list all available flights ordered by departure time, then departure location name
                              FLIGHT ADD <departure time> <from> <to> <capacity> - add a flight
                              FLIGHT IMPORT/EXPORT <filename> - import/export flights to csv file
                              FLIGHT <id> - view information about a flight (from->to, departure arrival times, current ticket price, capacity, passengers booked)
                              FLIGHT <id> BOOK <num> - book a certain number of passengers for the flight at the current ticket price, and then adjust the ticket price to reflect the reduced capacity remaining. If no number is given, book 1 passenger. If the given number of bookings is more than the remaining capacity, only accept bookings until the capacity is full.
                              FLIGHT <id> REMOVE - remove a flight from the schedule
                              FLIGHT <id> RESET - reset the number of passengers booked to 0, and the ticket price to its original state.
                              
                              LOCATIONS - list all available locations in alphabetical order
                              LOCATION ADD <name> <lat> <long> <demand_coefficient> - add a location
                              LOCATION <name> - view details about a location (it's name, coordinates, demand coefficient)
                              LOCATION IMPORT/EXPORT <filename> - import/export locations to csv file     
                              SCHEDULE <location_name> - list all departing and arriving flights, in order of the time they arrive/depart
                              DEPARTURES <location_name> - list all departing flights, in order of departure time
                              ARRIVALS <location_name> - list all arriving flights, in order of arrival time
                              
                              TRAVEL <from> <to> [sort] [n] - list the nth possible flight route between a starting location and destination, with a maximum of 3 stopovers. Default ordering is for shortest overall duration. If n is not provided, display the first one in the order. If n is larger than the number of flights available, display the last one in the ordering.
                              
                              can have other orderings:
                              TRAVEL <from> <to> cost - minimum current cost
                              TRAVEL <from> <to> duration - minimum total duration
                              TRAVEL <from> <to> stopovers - minimum stopovers
                              TRAVEL <from> <to> layover - minimum layover time
                              TRAVEL <from> <to> flight_time - minimum flight time
                              
                              HELP - outputs this help string.
                              EXIT - end the program.
                              """;
        String invalid_command_massage = "Invalid command. Type 'help' for a list of commands.\n";
        int largest_id = -1;
        String[] full_name_day_ls = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        String[] day_ls = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        while(true) {
            System.out.print("User: ");
            String instruction = "";
            if(keyboard.hasNext())
                instruction = keyboard.nextLine();

            // EXIT Command
            if(instruction.compareToIgnoreCase("exit") == 0) {
                System.out.println("Application closed.");
                break;
            }
            // HELP Command
            else if(instruction.compareToIgnoreCase("help") == 0) {
                System.out.println(help_massage);
                continue;
            }

            String[] info_ls = instruction.split("\\s+");
            
            // Classify Command
            String[] valid_first_command_ls = {"flight", "flights", "location", "locations", "travel", "schedule", "departures", "arrivals"};
            String Command = null;
            boolean valid_command = false;
            for(String first_command: valid_first_command_ls) {
                if(first_command.compareToIgnoreCase(info_ls[0]) == 0) {
                    Command = first_command;
                    valid_command = true;
                    break;
                }
            }
            if(valid_command == false) {
                System.out.println(invalid_command_massage);
                continue;
            }

            boolean have_second_command = false;
            if(Command.compareToIgnoreCase("flight") == 0 || Command.compareToIgnoreCase("location") == 0) {
                String[] valid_second_command_ls = {"add", "import", "export"};
                if(info_ls.length >=2) {
                    for(String second_command: valid_second_command_ls) {
                        if(second_command.compareToIgnoreCase(info_ls[1]) == 0) {
                            Command += " " + second_command;
                            have_second_command = true;
                            break;
                        }
                    } 
                }
            }
            if(Command.compareToIgnoreCase("flight") == 0 && !have_second_command) {
                String[] valid_third_command_ls = {"book", "remove", "reset"};
                if(info_ls.length >=3) {
                    for(String third_command: valid_third_command_ls) {
                        if(third_command.compareToIgnoreCase(info_ls[2]) == 0) {
                            Command += " " + third_command;
                            break;
                        }
                    } 
                }
            }
            
            // TRAVEL Command
            if(Command.compareToIgnoreCase("travel") == 0) {
                if(info_ls.length < 3) {
                    System.out.println("Usage: TRAVEL <from> <to> [cost/duration/stopovers/layover/flight_time]\n");
                    continue;
                } 
                String source_name = info_ls[1];
                String destination_name = info_ls[2];
                Location source = null;
                Location destination = null;
                for(Location l: locations_ls) {
                    if(source_name.compareToIgnoreCase(l.name) == 0)
                        source = l;
                    if(destination_name.compareToIgnoreCase(l.name) == 0)
                        destination = l;
                }

                if(source == null) {
                    System.out.println("Starting location not found.\n");
                    continue;
                }
                if(destination == null) {
                    System.out.println("Ending location not found.\n");
                    continue;
                }

                String ordering = null;
                String[] valid_order = {"cost", "duration", "stopovers", "layover", "flight_time"};
                int flight_routes_index = 0;
                if(info_ls.length >= 4) {
                    for(String order: valid_order) {
                        if(order.compareToIgnoreCase(info_ls[3]) == 0) {
                            ordering = order;
                            break;
                        }
                    }
                    if(ordering == null) {
                        System.out.println("Invalid sorting property: must be either cost, duration, stopovers, layover, or flight_time.\n");
                        continue;
                    }
                    if(info_ls.length >= 5) {
                        try{
                            flight_routes_index = Integer.parseInt(info_ls[4]);
                        } catch(NumberFormatException e) {
                            flight_routes_index = 0;
                        }
                        if(flight_routes_index < 0) {
                            flight_routes_index = 0;
                        }
                    }
                }

                ArrayList<ArrayList<Flight>> flight_routes = new ArrayList<ArrayList<Flight>>();
                flight_routes.clear();
                String arri_location_name = "";
                for(Flight source_depar_f: source.deparFlights) {
                    arri_location_name = source_depar_f.destination_location.name;
                    if(arri_location_name.compareToIgnoreCase(destination.name) == 0) {
                        ArrayList<Flight> flight_route = new ArrayList<Flight>();
                        flight_route.add(source_depar_f);
                        flight_routes.add(flight_route);
                        continue;
                    }
                    for(Flight stopover1_depar_f: source_depar_f.destination_location.deparFlights) {
                        arri_location_name = stopover1_depar_f.destination_location.name;
                        if(arri_location_name.compareToIgnoreCase(destination.name) == 0) {
                            ArrayList<Flight> flight_route = new ArrayList<Flight>();
                            flight_route.add(source_depar_f);
                            flight_route.add(stopover1_depar_f);
                            flight_routes.add(flight_route);
                            continue;
                        }
                        for(Flight stopover2_depar_f: stopover1_depar_f.destination_location.deparFlights) {
                            arri_location_name = stopover2_depar_f.destination_location.name;
                            if(arri_location_name.compareToIgnoreCase(destination.name) == 0) {
                                ArrayList<Flight> flight_route = new ArrayList<Flight>();
                                flight_route.add(source_depar_f);
                                flight_route.add(stopover1_depar_f);
                                flight_route.add(stopover2_depar_f);
                                flight_routes.add(flight_route);
                                continue;
                            }
                            for(Flight stopover3_depar_f: stopover2_depar_f.destination_location.deparFlights) {
                                arri_location_name = stopover3_depar_f.destination_location.name;
                                if(arri_location_name.compareToIgnoreCase(destination.name) == 0) {
                                    ArrayList<Flight> flight_route = new ArrayList<Flight>();
                                    flight_route.add(source_depar_f);
                                    flight_route.add(stopover1_depar_f);
                                    flight_route.add(stopover2_depar_f);
                                    flight_route.add(stopover3_depar_f);
                                    flight_routes.add(flight_route);
                                }
                            }
                        }
                    }
                }

                Comparator<ArrayList<Flight>> CompareByTotalDuration = new Comparator<ArrayList<Flight>>() {
                    @Override
                    public int compare(ArrayList<Flight> f_route1, ArrayList<Flight> f_route2) {
                        int total_duration1 = getTotalDuration(f_route1);
                        int total_duration2 = getTotalDuration(f_route2);

                        if(total_duration1 < total_duration2)
                            return -1;
                        else if(total_duration1 > total_duration2)
                            return 1;
                        else {
                            double f_route1_totalCost = getTotalCost(f_route1);
                            double f_route2_totalCost = getTotalCost(f_route2);
                            if(f_route1_totalCost < f_route2_totalCost)
                                return -1;
                            else if(f_route1_totalCost > f_route2_totalCost)
                                return 1;
                            else return 0;
                        }
                    }   
                };

                Comparator<ArrayList<Flight>> CompareByTotalCost = new Comparator<ArrayList<Flight>>() {
                    @Override
                    public int compare(ArrayList<Flight> f_route1, ArrayList<Flight> f_route2) {
                        double f_route1_totalCost = getTotalCost(f_route1);
                        double f_route2_totalCost = getTotalCost(f_route2);
                        if(f_route1_totalCost < f_route2_totalCost)
                            return -1;
                        else if(f_route1_totalCost == f_route2_totalCost) {
                            int total_duration1 = getTotalDuration(f_route1);
                            int total_duration2 = getTotalDuration(f_route2);
                            if(total_duration1 < total_duration2)
                                return -1;
                            else if(total_duration1 > total_duration2)
                                return 1;
                            else return 0;
                        } else return 1;

                    }   
                };

                Comparator<ArrayList<Flight>> CompareByStopovers = new Comparator<ArrayList<Flight>>() {
                    @Override
                    public int compare(ArrayList<Flight> f_route1, ArrayList<Flight> f_route2) {
                        int num_stopovers_1 = f_route1.size() - 1;
                        int num_stopovers_2 = f_route2.size() - 1;
                        if(num_stopovers_1 < num_stopovers_2)
                            return -1;
                        else if(num_stopovers_1 == num_stopovers_2) {
                            int total_duration1 = getTotalDuration(f_route1);
                            int total_duration2 = getTotalDuration(f_route2);
                            if(total_duration1 < total_duration2)
                                return -1;
                            else if(total_duration1 > total_duration2)
                                return 1;
                            else {
                                double f_route1_totalCost = getTotalCost(f_route1);
                                double f_route2_totalCost = getTotalCost(f_route2);
                                if(f_route1_totalCost < f_route2_totalCost)
                                    return -1;
                                else if(f_route1_totalCost > f_route2_totalCost) {
                                    return 1;
                                }
                                else return 0;
                            }
                        } else return 1;

                    }   
                };

                Comparator<ArrayList<Flight>> CompareByLayover = new Comparator<ArrayList<Flight>>() {
                    @Override
                    public int compare(ArrayList<Flight> f_route1, ArrayList<Flight> f_route2) {
                        int f_route1_total_layover = 0;
                        for(int i = 0; i<f_route1.size(); i++) {
                            if(i == f_route1.size() - 1)
                                break;
                            Flight f = f_route1.get(i);
                            Flight f_next = f_route1.get(i+1);
                            f_route1_total_layover += Flight.layover(f, f_next);
                        }

                        int f_route2_total_layover = 0;
                        for(int i = 0; i<f_route2.size(); i++) {
                            if(i == f_route2.size() - 1)
                                break;
                            Flight f = f_route2.get(i);
                            Flight f_next = f_route2.get(i+1);
                            f_route2_total_layover += Flight.layover(f, f_next);
                        }
                        if(f_route1_total_layover < f_route2_total_layover)
                            return -1;
                        else if(f_route1_total_layover == f_route2_total_layover) {
                            int total_duration1 = getTotalDuration(f_route1);
                            int total_duration2 = getTotalDuration(f_route2);
                            if(total_duration1 < total_duration2)
                                return -1;
                            else if(total_duration1 > total_duration2)
                                return 1;
                            else {
                                double f_route1_totalCost = getTotalCost(f_route1);
                                double f_route2_totalCost = getTotalCost(f_route2);
                                if(f_route1_totalCost < f_route2_totalCost)
                                    return -1;
                                else if(f_route1_totalCost > f_route2_totalCost) {
                                    return 1;
                                }
                                else return 0;
                            }
                        } else return 1;
                    }   
                };

                Comparator<ArrayList<Flight>> CompareByFlight_Time = new Comparator<ArrayList<Flight>>() {
                    @Override
                    public int compare(ArrayList<Flight> f_route1, ArrayList<Flight> f_route2) {
                        int flight_time1 = getTotalDuration(f_route1);
                        for(int i = 0; i<f_route1.size(); i++) {
                            if(i == f_route1.size() - 1)
                                break;
                            Flight f = f_route1.get(i);
                            Flight f_next = f_route1.get(i+1);
                            flight_time1 -= Flight.layover(f, f_next);
                        }

                        int flight_time2 = getTotalDuration(f_route2);
                        for(int i = 0; i<f_route2.size(); i++) {
                            if(i == f_route2.size() - 1)
                                break;
                            Flight f = f_route2.get(i);
                            Flight f_next = f_route2.get(i+1);
                            flight_time2 -= Flight.layover(f, f_next);
                        }

                        if(flight_time1 < flight_time2)
                            return -1;
                        else if(flight_time1 == flight_time2) {
                            int total_duration1 = getTotalDuration(f_route1);
                            int total_duration2 = getTotalDuration(f_route2);
                            if(total_duration1 < total_duration2)
                                return -1;
                            else if(total_duration1 > total_duration2)
                                return 1;
                            else {
                                double f_route1_totalCost = getTotalCost(f_route1);
                                double f_route2_totalCost = getTotalCost(f_route2);
                                if(f_route1_totalCost < f_route2_totalCost)
                                    return -1;
                                else if(f_route1_totalCost > f_route2_totalCost) {
                                    return 1;
                                }
                                else return 0;
                            }
                        } else return 1;

                    }   
                };

                
                if(flight_routes.size() == 0) {
                    System.out.println("Sorry, no flights with 3 or less stopovers are available from " + source.name + " to " + destination.name + ".\n");
                    continue;
                }
                ArrayList<Flight> final_flight_route = null;
                if(flight_routes_index >= flight_routes.size()) {
                    flight_routes_index = flight_routes.size() - 1;
                }
                if(ordering == null) { 
                    Collections.sort(flight_routes, CompareByTotalDuration);
                } else if(ordering.equals("cost")) {
                    Collections.sort(flight_routes, CompareByTotalCost);
                } else if(ordering.equals("duration")) {
                    Collections.sort(flight_routes, CompareByTotalDuration);
                } else if(ordering.equals("stopovers")) {
                    Collections.sort(flight_routes, CompareByStopovers);
                } else if(ordering.equals("layover")) {
                    Collections.sort(flight_routes, CompareByLayover);
                } else {
                    Collections.sort(flight_routes, CompareByFlight_Time);
                }
                final_flight_route = flight_routes.get(flight_routes_index);

                // Flight Route Massage
                System.out.println(String.format("%-18s", "Legs:") + final_flight_route.size());
                int final_total_duration = getTotalDuration(final_flight_route);
                int total_duration_h = ConvertMtoHandM(final_total_duration)[0];
                int total_duration_m = ConvertMtoHandM(final_total_duration)[1];
                System.out.println(String.format("%1$-18s%2$dh %3$dm", "Total Duration:", total_duration_h, total_duration_m));
                double total_cost = getTotalCost(final_flight_route);
                System.out.println(String.format("%1$-18s$%2$.2f", "Total Cost:", total_cost));
                System.out.println("-------------------------------------------------------------\nID   Cost      Departure   Arrival     Source --> Destination\n-------------------------------------------------------------");
                
                for(int i = 0; i<final_flight_route.size(); i++) {
                    Flight f = final_flight_route.get(i);
                    System.out.println(String.format("%1$4s $ %2$7.2f %3$s %4$02d:%5$02d   %6$s %7$02d:%8$02d   %9$s --> %10$s", f.flight_ID, f.getTicketPrice(), f.departure_day, f.departure_time_h, f.departure_time_m, f.arrival_day, f.arrival_time_h, f.arrival_time_m, f.source.name, f.destination_location.name));
                    if(i != final_flight_route.size() - 1) {
                        Flight f_next = final_flight_route.get(i+1);
                        int layover_time = Flight.layover(f, f_next);
                        // System.out.println(layover_time);
                        int layover_time_h = ConvertMtoHandM(layover_time)[0];
                        int layover_time_m = ConvertMtoHandM(layover_time)[1];
                        System.out.println("LAYOVER " + layover_time_h + "h " + layover_time_m + "m at " + f.destination_location.name);
                    }      
                }
                System.out.println();
                continue;
            }
            
            // FLIGHTS Command
            if(Command.compareToIgnoreCase("flights") == 0) {
                System.out.println("Flights\n-------------------------------------------------------\nID   Departure   Arrival     Source --> Destination\n-------------------------------------------------------");
                if(flights_ls.size() == 0) {
                    System.out.println("(None)\n");
                    continue;
                }
                ArrayList<Flight> sorted_flights_ls = new ArrayList<>();
                sorted_flights_ls.addAll(flights_ls);
                Collections.sort(sorted_flights_ls, Flight.FlightDeparANDNameComparator);
                for(int i = 0; i<sorted_flights_ls.size(); i++) {
                    Flight f = sorted_flights_ls.get(i);
                    System.out.println(String.format("%4s", f.flight_ID) + " " + f.departure_day + " " + String.format("%02d", f.departure_time_h) + ":" + String.format("%02d", f.departure_time_m) + "   " + f.arrival_day + " " + String.format("%02d", f.arrival_time_h) + ":" + String.format("%02d", f.arrival_time_m) + "   " + f.source.name + " --> " + f.destination_location.name);
                }
                System.out.println();
                continue;
            }

            // FLIGHT Command
            if(Command.compareToIgnoreCase("flight") == 0) {
                if(info_ls.length == 1)
                    System.out.println("Usage:\nFLIGHT <id> [BOOK/REMOVE/RESET] [num]\nFLIGHT ADD <departure time> <from> <to> <capacity>\nFLIGHT IMPORT/EXPORT <filename>\n");
                else {
                    try {  
                        Integer.parseInt(info_ls[1]);  
                    } catch(NumberFormatException e){  
                        System.out.println("Invalid Flight ID.\n");
                        continue;
                    }  
                    boolean valid_ID = false;
                    for(int i = 0; i<flights_ls.size(); i++) {
                        Flight f = flights_ls.get(i);
                        if(f.flight_ID.compareTo(info_ls[1]) == 0) {
                            System.out.println("Flight " + info_ls[1]);
                            System.out.println(String.format("%-14s", "Departure:") + f.departure_day + " " + String.format("%1$02d:%2$02d %3$s", f.departure_time_h, f.departure_time_m, f.source.name));
                            System.out.println(String.format("%1$-14s%2$s %3$02d:%4$02d %5$s", "Arrival:", f.arrival_day, f.arrival_time_h, f.arrival_time_m, f.destination_location.name));
                                
                            int distance = (int) Math.round(f.getDistance());
                            System.out.println(String.format("%1$-14s%2$,dkm", "Distance:", distance));
                                
                            int duration_h = f.getDuration()/60;
                            int duration_m = f.getDuration()%60;
                            System.out.println(String.format("%1$-14s%2$dh %3$dm", "Duration:", duration_h, duration_m));

                            System.out.println(String.format("%1$-14s$%2$.2f", "Ticket Cost:", f.getTicketPrice()));
                            System.out.println(String.format("%-14s", "Passengers:") + f.num_passenger_booked + "/" + f.capacity);                                
                            valid_ID = true;
                        }                            
                    }
                    if(valid_ID != true) {
                        System.out.println("Invalid Flight ID.");
                    }
                    System.out.println();
                }
                continue; 
            }

            // FLIGHT EXPORT
            if(Command.compareToIgnoreCase("flight export") == 0) {
                if(info_ls.length == 2)
                    System.out.println("Error writing file.\n");
                else {
                    String file_name = info_ls[2];
                    try {
                        File export_file = new File(file_name);
                        PrintWriter writer = new PrintWriter(export_file);
                        ArrayList<Flight> sorted_flights_ls = new ArrayList<>();
                        sorted_flights_ls.addAll(flights_ls);
                        Collections.sort(sorted_flights_ls, Flight.FlightIDComparator);
                        int num_flight_exported = 0;
                        for(int i = 0; i<sorted_flights_ls.size(); i++) {
                            Flight f = sorted_flights_ls.get(i);
                            String departure_day_full_name = null;
                            for(int x = 0; x<day_ls.length; x++) {
                                if(day_ls[x].compareToIgnoreCase(f.departure_day) == 0) {
                                    departure_day_full_name = full_name_day_ls[x];
                                    break;
                                }
                            }

                            String flight_massage = departure_day_full_name + String.format(" %1$02d:%2$02d", f.departure_time_h, f.departure_time_m) + "," + f.source.name + "," + f.destination_location.name + "," + f.capacity + "," + f.num_passenger_booked;
                            writer.println(flight_massage);
                            num_flight_exported++;
                        }
                        writer.close();
                        if(num_flight_exported == 1)
                            System.out.println("Exported " + num_flight_exported + " flight.\n");
                        else
                            System.out.println("Exported " + num_flight_exported + " flights.\n");
                    } catch(FileNotFoundException e) {
                        System.out.println("Error writing file.\n");
                        continue;
                    }
                }
                continue;
            }

            // FLIGHT IMPORT
            if(Command.compareToIgnoreCase("flight import") == 0) {
                if(info_ls.length == 2)
                    System.out.println("Error reading file.\n");
                else {
                    String file_name = info_ls[2];
                    int invalid_line = 0;
                    int valid_line = 0;
                    try {
                        File import_file = new File(file_name);
                        Scanner myReader = new Scanner(import_file);
                        while (myReader.hasNextLine()) {
                            String[] flight_info = myReader.nextLine().split(",");
                            if(flight_info.length < 5) {
                                continue;
                            }
                            String[] flight_time_info = flight_info[0].split(" ");
                            if(flight_time_info.length != 2){
                                invalid_line++;
                                continue;
                            }

                            String flight_day = flight_time_info[0];
                            boolean valid_day = false;
                            int x = 0;
                            for(String day: full_name_day_ls) {
                                if(day.compareToIgnoreCase(flight_day) == 0) {
                                    valid_day = true;
                                    flight_day = day_ls[x];
                                }
                                x++;
                            }
                            if(valid_day == false) {
                                invalid_line++;
                                continue;
                            }

                            String[] flight_time = flight_time_info[1].split(":");
                            if(flight_time.length != 2) {
                                invalid_line++;
                                continue;
                            }

                            int flight_time_h = -1;
                            int flight_time_m = -1;
                            try{
                                flight_time_h = Integer.parseInt(flight_time[0]);
                                flight_time_m = Integer.parseInt(flight_time[1]);
                            } catch(NumberFormatException e) {
                                invalid_line++;
                                continue;
                            }
                            if(flight_time_h < 0 || flight_time_h > 23 || flight_time_m < 0 || flight_time_m > 59) {
                                invalid_line++;
                                continue;
                            }
                            String flight_source_name = flight_info[1];
                            String flight_destination_name = flight_info[2];
                            if(flight_source_name.compareToIgnoreCase(flight_destination_name) == 0) {
                                invalid_line++;
                                continue;
                            }
                            Location flight_source = null;
                            Location flight_destination = null;
                            for(int i = 0; i<locations_ls.size(); i++) {
                                Location location = locations_ls.get(i);
                                if(location.name.compareToIgnoreCase(flight_source_name) == 0) {
                                    flight_source = location;
                                }
                                else if(location.name.compareToIgnoreCase(flight_destination_name) == 0) {
                                    flight_destination = location;
                                }
                            }
                            if(flight_source == null || flight_destination == null) {
                                invalid_line++;
                                continue;
                            }

                            int capacity = -1;
                            int num_passenger_booked = -1;
                            try{
                                capacity = Integer.parseInt(flight_info[3]);
                                num_passenger_booked = Integer.parseInt(flight_info[4]);
                            } catch(NumberFormatException e) {
                                invalid_line++;
                                continue;
                            }

                            if(capacity <= 0 || num_passenger_booked < 0) {
                                invalid_line++;
                                continue;
                            }
                                    
                            largest_id++;
                            String flight_ID = String.valueOf(largest_id);
                            String departure_daytime = flight_day + " " + String.format("%1$02d:%2$02d", flight_time_h, flight_time_m);
                            Flight flight = new Flight(flight_ID, capacity, num_passenger_booked, flight_source, flight_destination, departure_daytime);
                            if(flight_source.hasRunwayDepartureSpace(flight) == null && flight_destination.hasRunwayArrivalSpace(flight) == null) {
                                flight_source.addDeparture(flight);
                                flight_destination.addArrival(flight);
                            }
                            else {
                                largest_id--;
                                invalid_line++;
                                continue;
                            }

                            flights_ls.add(flight);
                            valid_line++;
                            // System.out.println(flight_info[0] + flight_info[1] + flight_info[2] + flight_info[3] + flight_info[4]);
                        }
                        myReader.close();
                    } catch (FileNotFoundException e) {
                        System.out.println("Error reading file.\n");
                        continue;
                    }

                    if(valid_line == 1)
                        System.out.println("Imported " + valid_line + " flight.");
                    else System.out.println("Imported " + valid_line + " flights.");

                    if(invalid_line != 0) {
                        if(invalid_line == 1)
                            System.out.println(invalid_line + " line was invalid.");
                        else System.out.println(invalid_line + " lines were invalid.");
                    }
                    System.out.println();
                }
                continue;
            }

            // FLIGHT ADD Command
            if(Command.compareToIgnoreCase("flight add") == 0) {
                if(info_ls.length < 7)
                    System.out.println("Usage:   FLIGHT ADD <departure time> <from> <to> <capacity>\nExample: FLIGHT ADD Monday 18:00 Sydney Melbourne 120\n");
                else {
                    String flight_day = info_ls[2];
                    boolean valid_day = false;
                    boolean valid_time = true;

                    int x = 0;
                    for(String day: full_name_day_ls) {
                        if(day.compareToIgnoreCase(flight_day) == 0) {
                            flight_day = day_ls[x];
                            valid_day = true;
                            break;
                        }
                        x++;
                    }

                    String[] time_ls = info_ls[3].split(":");
                    if(time_ls.length != 2) {
                        System.out.println("Invalid departure time. Use the format <day_of_week> <hour:minute>, with 24h time.\n");
                        continue;
                    }
                    int time_h = -1;
                    int time_m = -1;
                    try{
                        time_h = Integer.parseInt(time_ls[0]);
                        time_m = Integer.parseInt(time_ls[1]);
                    } catch(NumberFormatException e) {
                        System.out.println("Invalid departure time. Use the format <day_of_week> <hour:minute>, with 24h time.\n");
                        continue;
                    }
                    if(time_h < 0 || time_h > 23 || time_m < 0 || time_m > 59) {
                        valid_time = false;
                    }

                    if(valid_day == false || valid_time == false) {
                        System.out.println("Invalid departure time. Use the format <day_of_week> <hour:minute>, with 24h time.\n");
                        continue;
                    }

                    String source_name = info_ls[4];
                    String destination_name = info_ls[5];
                    Location source = null;
                    Location destination = null;
                    for(int i = 0; i<locations_ls.size(); i++) {
                        Location l = locations_ls.get(i);
                        if(source_name.compareToIgnoreCase(l.name) == 0)
                            source = l;
                        if(destination_name.compareToIgnoreCase(l.name) == 0)
                            destination = l;
                    }
                    if(source == null) {
                        System.out.println("Invalid starting location.\n");
                        continue;
                    }
                    if(destination == null) {
                        System.out.println("Invalid ending location.\n");
                        continue;
                    }

                    int capacity = -1;
                    try {
                        capacity = Integer.parseInt(info_ls[6]);
                    } catch(NumberFormatException e) {
                        System.out.println("Invalid positive integer capacity.\n");
                        continue;
                    }
                    if(capacity <= 0) {
                         System.out.println("Invalid positive integer capacity.\n");
                        continue;
                    }

                    if(source_name.compareToIgnoreCase(destination_name) == 0) {
                        System.out.println("Source and destination cannot be the same place.\n");
                        continue;
                    }

                    largest_id++;
                    String flight_ID = String.valueOf(largest_id);
                    String departure_daytime = flight_day + " " + String.format("%1$02d:%2$02d", time_h, time_m);
                    Flight flight_adding = new Flight(flight_ID, capacity, 0, source, destination, departure_daytime);

                    String check_source_depar_space_massage = source.hasRunwayDepartureSpace(flight_adding);
                    String check_destination_arri_space_massage = destination.hasRunwayArrivalSpace(flight_adding);
                    if(check_source_depar_space_massage != null) {
                        largest_id--;
                        System.out.println("Scheduling conflict! This flight clashes with " + check_source_depar_space_massage + ".\n");
                        continue;
                    }
                    else if(check_destination_arri_space_massage != null) {
                        largest_id--;
                        System.out.println("Scheduling conflict! This flight clashes with " + check_destination_arri_space_massage + ".\n");
                        continue;
                    }

                    source.addDeparture(flight_adding);
                    destination.addArrival(flight_adding);
                    flights_ls.add(flight_adding);
                    System.out.println("Successfully added Flight " + flight_ID + ".\n");
                }
                continue;
            }

            // FLIGHT BOOK Command
            if(Command.compareToIgnoreCase("flight book") == 0) {
                String flight_ID = info_ls[1];
                Flight flight_booking = null;

                int num_ticket_book = -1;
                if(info_ls.length >= 4) {
                    try {
                    num_ticket_book = Integer.parseInt(info_ls[3]);
                    } catch(NumberFormatException e) {
                        System.out.println("Invalid number of passengers to book.\n");
                        continue;
                    }
                    if(num_ticket_book <= 0) {
                        System.out.println("Invalid number of passengers to book.\n");
                        continue;
                    }
                } else num_ticket_book = 1;

                boolean valid_ID = false;
                for(int i = 0; i<flights_ls.size(); i++) {
                    Flight f = flights_ls.get(i);
                    if(f.flight_ID.compareToIgnoreCase(flight_ID) == 0) {
                        flight_booking = f;
                        valid_ID = true;
                        break;
                    }
                }
                if(valid_ID == false){
                    System.out.println("Invalid Flight ID.\n");
                    continue;
                }

                int sit_avaliable = flight_booking.capacity - flight_booking.num_passenger_booked;
                int num_booked = -1;
                double price = -1;
                if(sit_avaliable < num_ticket_book) {
                    price = flight_booking.book(sit_avaliable);
                    num_booked = sit_avaliable;
                }
                else {
                    price = flight_booking.book(num_ticket_book);
                    num_booked = num_ticket_book;
                }

                if(num_booked == 1)
                    System.out.println("Booked 1 passenger on flight " + flight_ID + " for a total cost of $" + String.format("%.2f", price));
                else 
                    System.out.println("Booked " + num_booked + " passengers on flight " + flight_ID + " for a total cost of $" + String.format("%.2f", price));
                if(flight_booking.isFull() == true)
                    System.out.println("Flight is now full.");
                System.out.println();
                continue;
            }

            // FLIGHT REMOVE Command
            if(Command.compareToIgnoreCase("flight remove") == 0) {
                String flight_ID = info_ls[1];
                Flight flight_removing = null;
                for(int i = 0; i<flights_ls.size(); i++) {
                    if(flights_ls.get(i).flight_ID.compareToIgnoreCase(flight_ID) == 0) {
                        flight_removing = flights_ls.get(i);
                        break;
                    }
                }
                if(flight_removing == null) {
                    System.out.println("Invalid Flight ID.\n");
                    continue;
                }

                Location source = flight_removing.source;
                Location destination = flight_removing.destination_location;
                for(int i = 0; i<source.deparFlights.size(); i++) {
                    if(source.deparFlights.get(i).flight_ID.compareToIgnoreCase(flight_ID) == 0) {
                        source.deparFlights.remove(i);
                        break;
                    }
                }
                for(int i = 0; i<destination.arrivingFlights.size(); i++) {
                    if(destination.arrivingFlights.get(i).flight_ID.compareToIgnoreCase(flight_ID) == 0) {
                        destination.arrivingFlights.remove(i);
                        break;
                    }
                }
                for(int i = 0; i<flights_ls.size(); i++) {
                    if(flights_ls.get(i).flight_ID.compareToIgnoreCase(flight_ID) == 0) {
                        flights_ls.remove(i);
                    }
                }
                System.out.println("Removed Flight " + flight_ID + ", " + flight_removing.departure_day + String.format(" %1$02d:%2$02d ", flight_removing.departure_time_h, flight_removing.departure_time_m) + source.name + " --> " + destination.name + ", from the flight schedule.\n");
                continue;
            }

            // FLIGHT RESET Command
            if(Command.compareToIgnoreCase("flight reset") == 0) {
                String flight_ID = info_ls[1];
                Flight flight_resetting = null;
                for(int i = 0; i<flights_ls.size(); i++) {
                    Flight f = flights_ls.get(i);
                    if(flight_ID.compareToIgnoreCase(f.flight_ID) == 0) {
                        flight_resetting = f;
                        break;
                    }
                }
                if(flight_resetting == null) {
                    System.out.println("Invalid Flight ID.\n");
                    continue;
                }

                flight_resetting.num_passenger_booked = 0;
                System.out.println("Reset passengers booked to 0 for Flight " + flight_ID + ", " + flight_resetting.departure_day + String.format(" %1$02d:%2$02d ", flight_resetting.departure_time_h, flight_resetting.departure_time_m) + flight_resetting.source.name + " --> " + flight_resetting.destination_location.name + ".\n");
                continue;
            }

            // LOCATIONS Command
            if(Command.compareToIgnoreCase("locations") == 0) {
                System.out.println("Locations (" + locations_ls.size() + "):");
                if(locations_ls.size() == 0) {
                    System.out.println("(None)\n");
                    continue;
                }
                ArrayList<Location> sorted_locations_ls = new ArrayList<>();
                sorted_locations_ls.addAll(locations_ls);
                Collections.sort(sorted_locations_ls, Location.SortLocationNameComparator);
                for(int i = 0; i<sorted_locations_ls.size(); i++) {
                    Location l = sorted_locations_ls.get(i);
                    if(i != 0) {
                        System.out.print(", ");
                    }
                    System.out.print(l.name);
                }
                System.out.println("\n");
                continue;
            }

            // LOCATION Command
            if(Command.compareToIgnoreCase("location") == 0) {
                if(info_ls.length == 1)
                    System.out.println("Usage:\nLOCATION <name>\nLOCATION ADD <name> <latitude> <longitude> <demand_coefficient>\nLOCATION IMPORT/EXPORT <filename>\n");
                else {
                    // implement location name
                    String location_name = info_ls[1];
                    boolean valid_name = false;
                    for(int i = 0; i<locations_ls.size(); i++) {
                        Location l = locations_ls.get(i);
                        if(location_name.compareToIgnoreCase(l.name) == 0) {
                            System.out.println(String.format("%-13s", "Location:") + l.name);
                            System.out.println(String.format("%-13s", "Latitude:") + String.format("%.6f", l.lat));
                            System.out.println(String.format("%-13s", "Longitude:") + String.format("%.6f", l.lon));
                            String demand_value = null;
                            if(l.demand > 0)
                                demand_value = "+" + String.format("%.4f", l.demand);
                            else
                                demand_value = String.format("%.4f", l.demand);
                            System.out.println(String.format("%-13s", "Demand:") + demand_value + "\n");
                            valid_name = true;
                            break;
                        }
                    }
                    if(valid_name == false)
                        System.out.println("Invalid location name.\n");
                }
                continue;  
            }

            // LOCATION IMPORT
            if(Command.compareToIgnoreCase("location import") == 0) {
                if(info_ls.length == 2)
                    System.out.println("Error reading file.\n");
                else {
                    String file_name = info_ls[2];
                    int invalid_line = 0;
                    int valid_line = 0;
                    try {
                        File import_file = new File(file_name);
                        Scanner myReader = new Scanner(import_file);
                        while (myReader.hasNextLine()) {
                            String[] location_info = myReader.nextLine().split(",");
                            if(location_info.length < 4) {
                                continue;
                            }

                            String location_name = location_info[0];
                            boolean invalid_name = false;
                            for(int i = 0; i<locations_ls.size(); i++) {
                                Location l = locations_ls.get(i);
                                if(l.name.compareToIgnoreCase(location_name) == 0) {
                                    invalid_name = true;
                                    break;
                                }
                            }
                            if(invalid_name == true) {
                                invalid_line++;
                                continue;
                            }

                            double lat = 200d;
                            double lon = 200d;
                            double demand = 10d;
                            try {
                                lat = Double.parseDouble(location_info[1]);
                                lon = Double.parseDouble(location_info[2]);
                                demand = Double.parseDouble(location_info[3]);
                            } catch(NumberFormatException e) {
                                invalid_line++;
                                continue;
                            }
                            if(lat < -85 || lat > 85 || lon < -180 || lon > 180 || demand < -1 || demand > 1) {
                                invalid_line++;
                                continue;
                            }
                            Location location = new Location(location_name, lat, lon, demand);
                            locations_ls.add(location);
                            valid_line++;
                        }
                        if(valid_line == 1)
                            System.out.println("Imported " + valid_line + " location.");
                        else System.out.println("Imported " + valid_line + " locations.");

                        if(invalid_line != 0) {
                            if(invalid_line == 1)
                                    System.out.println(invalid_line + " line was invalid.");
                            else System.out.println(invalid_line + " lines were invalid.");
                        }
                        System.out.println();
                    } catch (FileNotFoundException e) {
                        System.out.println("Error reading file.\n");
                        continue;
                    }
                }
                continue;
            }

            // LOCATION EXPORT Command
            if(Command.compareToIgnoreCase("location export") == 0) {
                if(info_ls.length == 2)
                     System.out.println("Error writing file.\n");
                else {
                    // implement location export
                    String file_name = info_ls[2];
                    try {
                        File export_file = new File(file_name);
                        PrintWriter writer = new PrintWriter(export_file);
                        ArrayList<Location> sorted_locations_ls = new ArrayList<>();
                        sorted_locations_ls.addAll(locations_ls);
                        Collections.sort(sorted_locations_ls, Location.SortLocationNameComparator);
                        int num_locations_exported = 0;
                        for(int i = 0; i<sorted_locations_ls.size(); i++) {
                            Location l = sorted_locations_ls.get(i);
                            String location_massage = l.name + "," + l.lat + "," + l.lon + "," + l.demand;
                            writer.println(location_massage);
                            num_locations_exported++;
                        }
                        writer.close();
                        if(num_locations_exported == 1)
                            System.out.println("Exported 1 location.\n");
                        else
                            System.out.println("Exported " + num_locations_exported + " locations.\n");
                    } catch(FileNotFoundException e) {
                        System.out.println("Error writing file.\n");
                        continue;
                    }    
                }
                continue;
            }

            // LOCATION ADD Command
            if(Command.compareToIgnoreCase("location add") == 0) {
                if(info_ls.length < 6)
                    System.out.println("Usage:   LOCATION ADD <name> <lat> <long> <demand_coefficient>\nExample: LOCATION ADD Sydney -33.847927 150.651786 0.2\n");
                else {
                    // implement location add
                    String location_name = info_ls[2];
                    boolean location_exist = false;
                    for(int i = 0; i<locations_ls.size(); i++) {
                        if(location_name.compareToIgnoreCase(locations_ls.get(i).name) == 0) {
                            location_exist = true;
                        }
                    }
                    if(location_exist == true) {
                        System.out.println("This location already exists.\n");
                        continue;
                    }
                    // Check latitude
                    double lat = -90d;
                    try{
                        lat = Double.parseDouble(info_ls[3]);
                    } catch(NumberFormatException e) {
                        System.out.println("Invalid latitude. It must be a number of degrees between -85 and +85.\n");
                        continue;
                    }
                    if(lat < -85 || lat > 85) {
                        System.out.println("Invalid latitude. It must be a number of degrees between -85 and +85.\n");
                        continue;
                    }
                    // Check longitude
                    double lon = -200d;
                    try{
                        lon = Double.parseDouble(info_ls[4]);
                    } catch(NumberFormatException e) {
                        System.out.println("Invalid longitude. It must be a number of degrees between -180 and +180.\n");
                        continue;
                    }
                    if(lon < -180 || lon > 180) {
                        System.out.println("Invalid longitude. It must be a number of degrees between -180 and +180.\n");
                        continue;
                    }
                    // Check demand
                    double demand = -10d;
                    try{
                        demand = Double.parseDouble(info_ls[5]);
                    } catch(NumberFormatException e) {
                        System.out.println("Invalid demand coefficient. It must be a number between -1 and +1.\n");
                        continue;
                    }
                    if(demand < -1 || demand > 1) {
                        System.out.println("Invalid demand coefficient. It must be a number between -1 and +1.\n");
                        continue;
                    }

                    Location l = new Location(location_name, lat, lon, demand);
                    locations_ls.add(l);
                    System.out.println("Successfully added location " + location_name + ".\n");
                }
                continue;
            }                

            // SCHEDULE Command
            if(Command.compareToIgnoreCase("schedule") == 0) {
                // implement schedule command;
                if(info_ls.length < 2) {
                    System.out.println("This location does not exist in the system.\n");
                    continue;
                }
                String location_name = info_ls[1];
                boolean valid_name = false;
                for(int i = 0; i<locations_ls.size(); i++) {
                    Location l = locations_ls.get(i);
                    if(location_name.compareToIgnoreCase(l.name) == 0) {
                        ArrayList<Flight> location_flights_ls = new ArrayList<>();
                        location_flights_ls.addAll(l.deparFlights);
                        location_flights_ls.addAll(l.arrivingFlights);
                        ArrayList<Flight> sorted_location_flights_ls = Flight.sortDeparArriFlights(location_flights_ls, l);
                        System.out.println(l.name);
                        System.out.println("-------------------------------------------------------\nID   Time        Departure/Arrival to/from Location\n-------------------------------------------------------");
                        for(Flight f: sorted_location_flights_ls) {
                            if(f.checkArriOrDepar(l) == 1) {
                                System.out.println(String.format("%1$4s %2$s %3$02d:%4$02d   Departure to %5$s", f.flight_ID, f.departure_day, f.departure_time_h, f.departure_time_m, f.destination_location.name));
                            }
                            else if(f.checkArriOrDepar(l) == -1) {
                                System.out.println(String.format("%1$4s %2$s %3$02d:%4$02d   Arrival from %5$s", f.flight_ID, f.arrival_day, f.arrival_time_h, f.arrival_time_m, f.source.name));
                            }
                        }
                        valid_name = true;
                        break;
                    }
                }
                if(valid_name == false)
                    System.out.println("This location does not exist in the system.");
                System.out.println();
                continue;
            }

            // DEPARTURES Command
            if(Command.compareToIgnoreCase("departures") == 0) {
                // implement departures command;
                if(info_ls.length < 2) {
                    System.out.println("This location does not exist in the system.\n");
                    continue;
                }
                String location_name = info_ls[1];
                boolean valid_name = false;
                for(int i = 0; i<locations_ls.size(); i++) {
                    Location l = locations_ls.get(i);
                    if(location_name.compareToIgnoreCase(l.name) == 0) {
                        ArrayList<Flight> depar_flights_ls = new ArrayList<>();
                        depar_flights_ls.addAll(l.deparFlights);
                        Collections.sort(depar_flights_ls, Flight.FlightDeparComparator);
                        System.out.println(l.name);
                        System.out.println("-------------------------------------------------------\nID   Time        Departure/Arrival to/from Location\n-------------------------------------------------------");
                        for(Flight f: depar_flights_ls) {
                            System.out.println(String.format("%1$4s %2$s %3$02d:%4$02d   Departure to %5$s", f.flight_ID, f.departure_day, f.departure_time_h, f.departure_time_m, f.destination_location.name));
                        }
                        valid_name = true;
                        break;
                    }
                }
                if(valid_name == false)
                    System.out.println("This location does not exist in the system.");
                System.out.println();
                continue;
            } 

            // ARRIVALS Command
            if(Command.compareToIgnoreCase("arrivals") == 0) {
                // implement arrivals command;
                if(info_ls.length < 2) {
                    System.out.println("This location does not exist in the system.\n");
                    continue;
                }
                String location_name = info_ls[1];
                boolean valid_name = false;
                for(int i = 0; i<locations_ls.size(); i++) {
                    Location l = locations_ls.get(i);
                    if(location_name.compareToIgnoreCase(l.name) == 0) {
                        ArrayList<Flight> arri_flights_ls = new ArrayList<>();
                        arri_flights_ls.addAll(l.arrivingFlights);
                        Collections.sort(arri_flights_ls, Flight.FlightArriComparator);
                        System.out.println(l.name);
                        System.out.println("-------------------------------------------------------\nID   Time        Departure/Arrival to/from Location\n-------------------------------------------------------");
                        for(Flight f: arri_flights_ls) {
                            System.out.println(String.format("%1$4s %2$s %3$02d:%4$02d   Arrival from %5$s", f.flight_ID, f.arrival_day, f.arrival_time_h, f.arrival_time_m, f.source.name));
                        }
                        valid_name = true;
                        break;
                    }
                }
                if(valid_name == false)
                    System.out.println("This location does not exist in the system.");
                System.out.println();
                continue;
            }
        }
    }

    public static int getTotalDuration(ArrayList<Flight> f_route) {
        int total_duration = 0;
        for(int i = 0; i<f_route.size(); i++) {
            Flight f = f_route.get(i);
            if(i == f_route.size() - 1) {
                total_duration += f.getDuration();
            }
            else {
                Flight f_next = f_route.get(i+1);
                total_duration += f.getDuration() + Flight.layover(f, f_next);
            }
        }

        return total_duration;
    }

    public static double getTotalCost(ArrayList<Flight> f_route) {
        double total_cost = 0d;
        for(Flight f: f_route) {
            total_cost += f.getTicketPrice();
        }
        return total_cost;
    }

    public static int[] ConvertMtoHandM(int total_time_m) {
        int time_h = total_time_m / 60;
        int time_m = total_time_m % 60;
        int[] time_ls = {time_h, time_m};
        return time_ls;
    }
}
