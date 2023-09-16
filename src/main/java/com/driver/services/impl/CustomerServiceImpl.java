package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
		return;
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		List<Customer>customers=customerRepository2.findAll();
		for(Customer customer:customers){
			if(customer.getCustomerId()==customerId){
				customers.remove(customer);
				return;
			}
		}


	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		int minId=Integer.MAX_VALUE;
		Driver newDriver=null;
		Cab cab=null;
		List<Driver>drivers=driverRepository2.findAll();

		for(Driver driver:drivers){
			if(driver.getDriverId()<minId && driver.getCab().getAvailable()==true){
				minId=driver.getDriverId();
				newDriver=driver;
				cab=driver.getCab();

			}
		}

		if(cab==null){
			throw new Exception("No cab available!");
		}

		// for cab attributes
		cab.setAvailable(false); //cab booked



		// for tripbooking attribute

		TripBooking tripBooking=new TripBooking();
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setStatus(TripStatus.CONFIRMED);
		int bill= cab.getPerKmRate()*distanceInKm;
		tripBooking.setBill(bill);

		Customer customer=customerRepository2.findById(customerId).get();
		tripBooking.setCustomer(customer);
		tripBooking.setDriver(newDriver);


		// for drivers attribute

		newDriver.getTripBookings().add(tripBooking);
		newDriver.setCab(cab);

		driverRepository2.save(newDriver);

		// for customers attribute

		customer.getTripBookingList().add(tripBooking);
		customerRepository2.save(customer);



		return tripBooking;



	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.CANCELED);
		tripBooking.getDriver().getCab().setAvailable(true);
		tripBooking.setBill(0);
		tripBooking.setFromLocation(null);
		tripBooking.setToLocation(null);
		tripBooking.setDistanceInKm(0);

		tripBookingRepository2.save(tripBooking);



	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();

		tripBooking.setStatus(TripStatus.COMPLETED);
		tripBookingRepository2.save(tripBooking);






	}
}
