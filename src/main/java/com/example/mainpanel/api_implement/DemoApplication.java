package com.example.mainpanel.api_implement;

import com.example.mainpanel.back_end.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class DemoApplication {
	// Spring API design
	// good example https://howtodoinjava.com/spring-boot2/rest-api-example/
	// https://www.tutorialspoint.com/spring_boot/spring_boot_building_restful_web_services.htm
	// https://github.com/spring-guides/tut-react-and-spring-data-rest/tree/master/basic
	// https://spring.io/guides/tutorials/react-and-spring-data-rest/

	private SimultaionRun monitorSim;
	private InfoMap lawnMap;
	private Report report;
	private MowerStates[] mowerStates;

	// open browser: 0.read file 1.initialize map 2.report 3.Mowers States
	@PostMapping(value = "/simulation")
	public String startApplication(@RequestBody FilePath filePath) throws JsonProcessingException {
		String[] mowerAction = new String[2];
		// 0. read file
		monitorSim = new SimultaionRun(filePath);

		// 1.initialize map
		lawnMap = monitorSim.getLawnMap();

		// 2.report
		report = monitorSim.generateReport();

		// 3.mowers states
		mowerStates = monitorSim.getMowerState();

		// object --> JSON
		// Spring uses Jackson ObjectMapper class to do Json Serialization and Deserialization.
		ObjectMapper objectMapper = new ObjectMapper();
		String mapAsString = objectMapper.writeValueAsString(lawnMap);
		String reportAsString = objectMapper.writeValueAsString(report);
		String stateAsString = objectMapper.writeValueAsString(mowerStates);
		String mowerAsString = objectMapper.writeValueAsString(mowerAction);

		return "{\"map\":" + mapAsString + ", \"report\":" + reportAsString + ", \"mowerStates\":" + stateAsString + ", \"mowerAction\":" + mowerAsString + "}";

	}

	// stop: 1. terminate application 2. map 3. report 4. Mowers States
	@DeleteMapping(value = "/stop")
	public String stopRun() throws JsonProcessingException {
		String[] mowerAction = new String[2];
		// 1. terminate application
		monitorSim.stopRun();

		// 2. map
		lawnMap = monitorSim.getLawnMap();

		// 3.report
		report = monitorSim.generateReport();

		// 4.mowers states
		mowerStates = monitorSim.getMowerState();

		// object --> JSON
		ObjectMapper objectMapper = new ObjectMapper();
		String mapAsString = objectMapper.writeValueAsString(lawnMap);
		String reportAsString = objectMapper.writeValueAsString(report);
		String stateAsString = objectMapper.writeValueAsString(mowerStates);
		String mowerAsString = objectMapper.writeValueAsString(mowerAction);

		return "{\"map\":" + mapAsString + ", \"report\":" + reportAsString + ", \"mowerStates\":" + stateAsString + ", \"mowerAction\":" + mowerAsString + "}";
	}

	// next: 1. move mower to next 2. map 3. report 4. Mowers States
	@PatchMapping(value = "/next")
	public String nextRun() throws JsonProcessingException {
		// 1. move mower to next - return current move Mower ID
		String[] mowerAction = monitorSim.moveNext();

		// 2. map
		lawnMap = monitorSim.getLawnMap();

		// 3.report
		report = monitorSim.generateReport();

		// 4.mowers states
		mowerStates = monitorSim.getMowerState();

		// object --> JSON

		ObjectMapper objectMapper = new ObjectMapper();
		String mapAsString = objectMapper.writeValueAsString(lawnMap);
		String reportAsString = objectMapper.writeValueAsString(report);
		String stateAsString = objectMapper.writeValueAsString(mowerStates);
		String mowerAsString = objectMapper.writeValueAsString(mowerAction);

		return "{\"map\":" + mapAsString + ", \"report\":" + reportAsString + ", \"mowerStates\":" + stateAsString + ", \"mowerAction\":" + mowerAsString + "}";
	}
}
