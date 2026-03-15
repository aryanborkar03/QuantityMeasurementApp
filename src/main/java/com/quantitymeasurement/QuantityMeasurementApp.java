package com.quantitymeasurement;

import com.quantitymeasurement.controller.QuantityMeasurementController;
import com.quantitymeasurement.dto.QuantityDTO;
import com.quantitymeasurement.repository.IQuantityMeasurementRepository;
import com.quantitymeasurement.repository.QuantityMeasurementCacheRepository;
import com.quantitymeasurement.service.QuantityMeasurementServiceImpl;

/**
 * QuantityMeasurementApp
 *
 * Main application class responsible for initializing and
 * running the Quantity Measurement system.
 *
 * This class acts as the Application Layer entry point and
 * coordinates the initialization of core components following
 * an N-Tier architecture.
 *
 * Responsibilities of this class include:
 * <ul>
 * <li>Initializing the repository layer</li>
 * <li>Creating the service layer</li>
 * <li>Creating the controller layer</li>
 * <li>Providing a singleton application instance</li>
 * <li>Executing sample quantity measurement operations</li>
 * </ul>
 *
 * The application demonstrates various operations such as:
 * <ul>
 * <li>Comparison</li>
 * <li>Unit conversion</li>
 * <li>Addition</li>
 * <li>Subtraction</li>
 * <li>Division</li>
 * </ul>
 *
 * The results of operations are persisted using the
 * repository layer for later retrieval.
 *
 * This class follows the Singleton pattern to ensure that
 * only one instance of the application is created.
 */
public class QuantityMeasurementApp {
	
	private static QuantityMeasurementApp instance;
	
	public QuantityMeasurementController controller;
	
	public IQuantityMeasurementRepository repository;
	
	/**
	 * Private constructor used to initialize the
	 * application components.
	 *
	 * The constructor creates:
	 * <ul>
	 * <li>Repository instance</li>
	 * <li>Service implementation</li>
	 * <li>Controller instance</li>
	 * </ul>
	 */
	private QuantityMeasurementApp() {
		this.repository = QuantityMeasurementCacheRepository.getInstance();
		QuantityMeasurementServiceImpl service = new QuantityMeasurementServiceImpl(
			this.repository
		);
		this.controller = new QuantityMeasurementController(service);
	}
	
	/**
	 * Returns the singleton instance of the application.
	 *
	 * @return application instance
	 */
	public static QuantityMeasurementApp getInstance() {
		if(instance == null) {
			instance = new QuantityMeasurementApp();
		}
		return instance;
	}
 	
	/**
	 * Entry point of the Quantity Measurement Application.
	 *
	 * Demonstrates execution of different measurement operations
	 * including comparison, conversion, arithmetic calculations,
	 * and retrieval of stored measurement records.
	 *
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
 
	    System.out.println("**** Quantity Measurement Application Started ****\n");
 
	    QuantityMeasurementApp app = QuantityMeasurementApp.getInstance();
	    QuantityMeasurementController controller = app.controller;
 
	    /**
	     * Example 1: Length Equality Demonstration 
	     */
	    
	    QuantityDTO quantity1 = new QuantityDTO(
	        2,
	        QuantityDTO.LengthUnit.FEET.getUnitName(),
	        QuantityDTO.LengthUnit.FEET.getMeasurementType()
	    );
 
	    QuantityDTO quantity2 = new QuantityDTO(
	        24,
	        QuantityDTO.LengthUnit.INCHES.getUnitName(),
	        QuantityDTO.LengthUnit.INCHES.getMeasurementType()
	    );
 
	    controller.demonstrateComparison(quantity1, quantity2);
 
	    System.out.println();
 
	    /**
	     * Example 2: Temperature Conversion
	     */
	    
	    QuantityDTO temp1 = new QuantityDTO(
	        0,
	        QuantityDTO.TemperatureUnit.CELSIUS.getUnitName(),
	        QuantityDTO.TemperatureUnit.CELSIUS.getMeasurementType()
	    );
 
	    QuantityDTO temp2 = new QuantityDTO(
	        0,
	        QuantityDTO.TemperatureUnit.FAHRENHEIT.getUnitName(),
	        QuantityDTO.TemperatureUnit.FAHRENHEIT.getMeasurementType()
	    );
 
	    controller.demonstrateConversion(temp1, temp2);
 
	    System.out.println();
 
	    /**
	     * Example 3: Temperature Addition Attempt — error expected
	     */
	    
	    QuantityDTO tempTarget = new QuantityDTO(
	        0,
	        QuantityDTO.TemperatureUnit.CELSIUS.getUnitName(),
	        QuantityDTO.TemperatureUnit.CELSIUS.getMeasurementType()
	    );
 
	    controller.demonstrateAddition(temp1, temp2, tempTarget);
 
	    System.out.println();
 
	    /**
	     * Example 4: Cross-Category Operation Prevention — error expected
	     */
	    
	    QuantityDTO weightQuantity = new QuantityDTO(
	        10,
	        QuantityDTO.WeightUnit.KILOGRAM.getUnitName(),
	        QuantityDTO.WeightUnit.KILOGRAM.getMeasurementType()
	    );
 
	    controller.demonstrateAddition(quantity1, weightQuantity);
 
	    System.out.println();
 
	    
	    /**
	     * Full operation suite — length
	     */
 
	    QuantityDTO yardsTarget = new QuantityDTO(
	        0,
	        QuantityDTO.LengthUnit.YARDS.getUnitName(),
	        QuantityDTO.LengthUnit.YARDS.getMeasurementType()
	    );
 
	    controller.demonstrateConversion(quantity2, yardsTarget);
	    System.out.println();
 
	    controller.demonstrateAddition(quantity1, quantity2);
	    System.out.println();
 
	    controller.demonstrateAddition(quantity1, quantity2, yardsTarget);
	    System.out.println();
 
	    controller.demonstrateSubtraction(quantity1, quantity2);
	    System.out.println();
 
	    controller.demonstrateDivision(quantity1, quantity2);
	    System.out.println();

	    /**
	     * Stored measurements
	     */
 
	    System.out.println("---- Stored Measurements ----");
 
	    app.repository
	        .getAllMeasurements()
	        .forEach(System.out::println);
 
	    System.out.println("\n**** Quantity Measurement Application Stopped ****");
	}
}