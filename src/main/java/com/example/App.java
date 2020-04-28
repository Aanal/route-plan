package com.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import com.opencsv.exceptions.CsvValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(final String[] args) {
        Logger logger = LoggerFactory.getLogger("RoutePlan");

        Location originLocation = Location.newInstance(0.19068094e2, 0.72822846e2);
        Location endLocation = Location.newInstance(0.19065389e2, 0.72828483e2);

        // specify service - which involves one stop
        // final Service service =
        // Service.Builder.newInstance("serviceId").setName("myService")
        // .setLocation(originLocation).build();

        // specify type of both vehicles
        VehicleTypeImpl vehicleType = VehicleTypeImpl.Builder.newInstance("vehicleType").build();

        // specify vehicle1 with different start and end locations
        VehicleImpl vehicle1 = VehicleImpl.Builder.newInstance("vehicle1Id").setType(vehicleType)
                .setStartLocation(originLocation).setEndLocation(endLocation).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle1);

        File csvFile = null;

        if (args.length > 0) {
            csvFile = new File(args[0]);
        } else {
            logger.error("Could not read csvfile argument");
            System.exit(-2);
        }

        try {
            for (Location dropLocation : LocationReader.getCoordinates(csvFile)) {
                logger.debug("Location [lat= {}, long= {}]", dropLocation.getCoordinate().getX(), dropLocation.getCoordinate().getY());

                Shipment shipment = Shipment.Builder.newInstance(Utils.randomString()).setName("myShipment")
                    .setPickupLocation(originLocation)
                    .setDeliveryLocation(dropLocation)
                    .build();
    
                vrpBuilder.addJob(shipment);
            }

            if (vrpBuilder.getAddedJobs().isEmpty()) {
                logger.error("Could not read any rows from input csv");
                System.exit(-1);
            }
        } catch (IOException | CsvValidationException e) {
            logger.error("Could not read from csv: {}", csvFile);
            e.printStackTrace();
            System.exit(-2);
        }

        vrpBuilder.setFleetSize(FleetSize.FINITE);
        VehicleRoutingProblem problem =  vrpBuilder.build();

        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);

        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        // get best
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        if (bestSolution == null) {
            logger.error("Could not find solution");
        }

        int unfinishedBusiness;

        if ((unfinishedBusiness = bestSolution.getUnassignedJobs().size()) > 0) {
            logger.error("Could not finish {} jobs", unfinishedBusiness);
        }
        
        if (!bestSolution.getRoutes().isEmpty()) {
            VehicleRoute bestRoute = bestSolution.getRoutes().toArray(new VehicleRoute[0])[0];

            for (TourActivity activity : bestRoute.getActivities()) {
                Coordinate coordinate = activity.getLocation().getCoordinate();
                logger.info("lat: {} long: {}", coordinate.getX(), coordinate.getY());
            }
        } else {
            logger.error("Could not find routes");
        }
    }
}
