package org.matsim.preparation;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PersonUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class createPopulation {

    public static void main(String[] args) {
//        MATSim related variables
        Config config = ConfigUtils.createConfig();
        config.global().setCoordinateSystem("EPSG:31370");
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Population population = scenario.getPopulation();
        PopulationFactory populationFactory = population.getFactory();

//        Load relevant files
        String activitiesFile = "scenarios/BrusselsPopulationFromR/BrusselsActivities.csv";
        String attributesFile = "scenarios/BrusselsPopulationFromR/BrusselsAttribute.csv";
        String legFile = "scenarios/BrusselsPopulationFromR/BrusselsLegs.csv";
        String noTripAgents = "scenarios/BrusselsPopulationFromR/BrusselsNoTripAgents.csv";

//        Create hashmap to store <personID, activity arrayList>
        HashMap<Integer, ArrayList<String>> idAndAllActivities = new HashMap<>();
        try {
            BufferedReader activityReader = new BufferedReader(new FileReader(activitiesFile));
            String agentActivity = null;

            while ((agentActivity = activityReader.readLine()) != null){
                String[] activitySpilted = agentActivity.split(",");
                int activityAgentID = Integer.parseInt(activitySpilted[0]);

                if (idAndAllActivities.containsKey(activityAgentID)){
                    ArrayList<String> existentActivities = idAndAllActivities.get(activityAgentID);
                    existentActivities.add(agentActivity);
                } else {
                    ArrayList<String> activities = new ArrayList<String>();
                    activities.add(agentActivity);
                    idAndAllActivities.put(activityAgentID, activities);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Finish reading the activity");

//        Create hashmap to store ,personID, leg arrayList>

        HashMap<Integer, ArrayList<String>> idAndAllLegs = new HashMap<>();
        try {
            BufferedReader legReader = new BufferedReader(new FileReader(legFile));
            String agentLeg = null;

            while ((agentLeg = legReader.readLine()) != null){
                String[] legSpilted = agentLeg.split(",");
                int legAgentID = Integer.parseInt(legSpilted[0]);

                if (idAndAllLegs.containsKey(legAgentID)){
                    ArrayList<String> existentLegs = idAndAllLegs.get(legAgentID);
                    existentLegs.add(agentLeg);
                } else {
                    ArrayList<String> legs = new ArrayList<String>();
                    legs.add(agentLeg);
                    idAndAllLegs.put(legAgentID, legs);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Finish reading the legs");

//        Reading the attribute file
        try {
            BufferedReader attributeReader = new BufferedReader(new FileReader(attributesFile));
            String agent = null;
            while ((agent = attributeReader.readLine()) != null) {
                String[] attributeSpilted = agent.split(",");
                int attributeAgentID = Integer.parseInt(attributeSpilted[0]);
                String agentHousehold = attributeSpilted[1];
                String agentHomeNB = attributeSpilted[2];

                String agentAgeGroup = attributeSpilted[3];
                String agentGender = attributeSpilted[4].equals("1") ? "m" :"f";
                String agentHighestDegree = attributeSpilted[5];
                String agentIncomeLevel = attributeSpilted[6];
                String agentCarAvailability = attributeSpilted[7];
                String agentWorkingStatus = attributeSpilted[8];

//                Set attributes for agents
                Person person = populationFactory.createPerson(Id.createPersonId(attributeAgentID));
                person.getAttributes().putAttribute("householdID", agentHousehold);
                person.getAttributes().putAttribute("homeNB", agentHomeNB);
                PersonUtils.setSex(person, agentGender);
                person.getAttributes().putAttribute("ageGroup", agentAgeGroup);
                person.getAttributes().putAttribute("highestDegree", agentHighestDegree);
                person.getAttributes().putAttribute("incomeLevel", agentIncomeLevel);
                PersonUtils.setCarAvail(person, agentCarAvailability);
                person.getAttributes().putAttribute("workingStatus", agentWorkingStatus);
                person.getAttributes().putAttribute("subpopulation", "person");

//                Find the relative activities and legs according to agentID
                ArrayList<String> agentActivities = idAndAllActivities.get(attributeAgentID);
                ArrayList<String> agentLegs = idAndAllLegs.get(attributeAgentID);

                Plan plan = populationFactory.createPlan();

//                For all legs and not last activities
                for (int i = 0; i < agentLegs.size(); i++){
//                    First processing activity
                    String activityInArray = agentActivities.get(i);
                    String[] activity = activityInArray.split(",");

//                    Get the attributes of activities
                    String activityPurpose = activity[6].concat("_").concat(activity[7]);
                    double activityXCoord = Double.parseDouble(activity[8]);
                    double activityYCoord = Double.parseDouble(activity[9]);
                    int activityStartTime = Integer.parseInt(activity[2]) * 3600 + Integer.parseInt(activity[3]) * 60;
                    int activityEndTime = Integer.parseInt(activity[4]) * 3600 + Integer.parseInt(activity[5]) * 60;

//                    Set activity attribute in MATSim
                    Activity actMATSim = populationFactory.createActivityFromCoord(activityPurpose, new Coord(activityXCoord, activityYCoord));
                    actMATSim.setStartTime(activityStartTime);
                    actMATSim.setEndTime(activityEndTime);

//                    Then processing relative legs
                    String legInArray = agentLegs.get(i);
                    String[] leg = legInArray.split(",");

//                    Get the attributes of legs (departure time and travel time)
                    String legMode = leg[4];
                    int legDepartureTime = Integer.parseInt(leg[2]) * 3600 + Integer.parseInt(leg[3]) * 60;
                    int legTravelTime = Integer.parseInt(leg[5]) * 3600 + Integer.parseInt(leg[6]) * 60;

//                    Set leg attribute in MATSim
                    Leg legMATSim = populationFactory.createLeg(legMode);
                    legMATSim.setDepartureTime(legDepartureTime);
                    legMATSim.setTravelTime(legTravelTime);

                    plan.addActivity(actMATSim);
                    plan.addLeg(legMATSim);
                }

//                As there are always one more activities than legs, need to additionally process the last activities
                String lastActivityInArray = agentActivities.get(agentLegs.size());
                String[] lastActivity = lastActivityInArray.split(",");

                String lastActivitiyPurpose = lastActivity[6].concat("_").concat(lastActivity[7]);
                double lastActivityXCoord = Double.parseDouble(lastActivity[8]);
                double lastActivityYCoord = Double.parseDouble(lastActivity[9]);
                int lastActivityStartTime = Integer.parseInt(lastActivity[2]) * 3600 + Integer.parseInt(lastActivity[3]) * 60;

                Activity lastActMATSim = populationFactory.createActivityFromCoord(lastActivitiyPurpose, new Coord(lastActivityXCoord, lastActivityYCoord));
                lastActMATSim.setStartTime(lastActivityStartTime);

                plan.addActivity(lastActMATSim);

                person.addPlan(plan);
                population.addPerson(person);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//        Add the plan for the agents without any activity in a day
        try {
            BufferedReader noActivityReader = new BufferedReader(new FileReader(noTripAgents));
            String noActivityAgent = null;

            while ((noActivityAgent = noActivityReader.readLine()) != null){
                String[] noActivityAgentSplited = noActivityAgent.split(",");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//       write the population to corresponding directory
        PopulationWriter populationWriter = new PopulationWriter(population);
        populationWriter.write("scenarios/BrusselsScenario/plan.xml.gz");

    }

}
